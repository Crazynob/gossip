package red.fuyun.chat.server;


import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import red.fuyun.chat.constant.SocketIoEvent;
import red.fuyun.chat.constant.RedisKey;
import red.fuyun.chat.entiy.MatcheDto;
import red.fuyun.chat.entiy.MessageDto;
import red.fuyun.chat.entiy.UserInfo;
import red.fuyun.chat.util.SocketIoUitls;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

/***
 * Socket服务
 */
@Component
public class SocketServer {
    private SocketIOServer server;

    @Autowired
    private RedisTemplate<String,MessageDto> messageRedisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    /**
     *  所有已连接的客户端
     */
    private HashMap<String,SocketIOClient> allClients = new HashMap<>();


    /**
     *  所有已匹配的客户端
     */
    private HashMap<String,SocketIOClient> matched = new HashMap<>();

    /**
     *  所有待匹配的客户端
     */
    private HashMap<String,SocketIOClient> matching = new HashMap<>();

    /**
     *  所有已连接的用户信息
     */
    private HashMap<String, UserInfo> userInfo = new HashMap<>();


    /**
     *  l维护连接信息
     */
    private HashMap<String, String> mapping = new HashMap<>();

    /**
     *  待匹配的用户的key
     */
    private Queue<String> matchingKeys = new LinkedList<>();





    public SocketServer(SocketIOServer server) {
        this.server = server;
    }

    /**
     * 启动Socket服务
     */
    @PostConstruct
    public void start(){
        server.start();
        System.currentTimeMillis();
    }


    /**
     * 建立连接时把用户信息保存到Map集合内 Key为token
     * redis 中每个连接 维护一个key为Token Value为 匹配历史List 作用记录查询匹配记录与相应的聊天记录
     * 聊天记录的key 为 sourceToken+targetToken Value 为每条聊天信息记录
     *每条聊天信息记录格式: 消息类型&消息内容&消息时间
     *
     */
    @OnConnect
    public void onConnect(SocketIOClient client) {
        String sourceToken = SocketIoUitls.getSingleUrlParam(client,"token");
        String age = SocketIoUitls.getSingleUrlParam(client,"age");
        String sex = SocketIoUitls.getSingleUrlParam(client,"sex");

        userInfo.put(sourceToken,new UserInfo(sex, Integer.valueOf(age)));
        allClients.put(sourceToken,client);
        System.out.println( "-------------------------" + "客户端已连接:");


    }




    /**
     *用户匹配方法
     * sourceClient 触发事件的用户连接
     * data 触发事件时携带的数据
     */
    @OnEvent(value = SocketIoEvent.MATCHE)
    public void onEvent(SocketIOClient sourceClient, String data) throws JsonProcessingException {
        String sourceToken = SocketIoUitls.getSingleUrlParam(sourceClient,"token");
        UserInfo sourceUserInfo = this.userInfo.get(sourceToken);
        String token = sourceUserInfo.getToken();
        if(token !=  null){
            return;
        }
        if(matchingKeys.size() == 0){
            matchingKeys.add(sourceToken);
            return;
        }

        //被匹配的连接
        SocketIOClient targetClient = allClients.get(matchingKeys.poll());

        String targetToken = SocketIoUitls.getSingleUrlParam(targetClient,"token");
        sourceUserInfo.setToken(targetToken);

        UserInfo targetUserInfo = this.userInfo.get(targetToken);
        targetUserInfo.setToken(sourceToken);

        MatcheDto sourceMatcheDto = new MatcheDto(true,sourceUserInfo);
        MatcheDto targetMatcheDto = new MatcheDto(true,targetUserInfo);

        String source = SocketIoUitls.BenaToJson(sourceMatcheDto);
        String target = SocketIoUitls.BenaToJson(targetMatcheDto);
        sourceClient.sendEvent(SocketIoEvent.MATCHE,target);
        targetClient.sendEvent(SocketIoEvent.MATCHE,source);

        ListOperations<String, String> opsList = stringRedisTemplate.opsForList();
        String sourceKey = RedisKey.MATCHED+sourceToken;
        String targetKey = RedisKey.MATCHED+targetToken;
        opsList.rightPush(sourceKey,targetToken);
        opsList.rightPush(targetKey,sourceToken);
        System.out.println("--------------------匹配事件");
    }

    @OnEvent(value = SocketIoEvent.SEND_MESSAGE)
    public void sendMsg(SocketIOClient sourceClient, String data){
        String sourceToken = SocketIoUitls.getSingleUrlParam(sourceClient,"token");
        String targetToken = this.userInfo.get(sourceToken).getToken();
        if(targetToken == null){
            return;
        }
        MessageDto messageDto = null;
        try {
            messageDto = SocketIoUitls.JsonToBena(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SocketIOClient targetClient = allClients.get(targetToken);
        targetClient.sendEvent(SocketIoEvent.SEND_MESSAGE, messageDto.getMessage());
        ListOperations listOperations = messageRedisTemplate.opsForList();
        String key = RedisKey.MESSAGE+sourceToken+targetToken;
        Long row = listOperations.rightPush(key, messageDto);

        System.out.println(SocketIoEvent.SEND_MESSAGE+data);
    }


    @OnEvent(value = SocketIoEvent.Virtual_DISCONNECTION)
    public void disc(SocketIOClient sourceClient){
        String sourceToken = SocketIoUitls.getSingleUrlParam(sourceClient,"token");
        UserInfo sourceUserInfo = this.userInfo.get(sourceToken);


        String targetToken = sourceUserInfo.getToken();
        UserInfo targetUserInfo = this.userInfo.get(targetToken);

        sourceUserInfo.setToken(null);
        targetUserInfo.setToken(null);
        SocketIOClient socketIOClient = allClients.get(targetToken);
        socketIOClient.sendEvent(SocketIoEvent.Virtual_DISCONNECTION);


    }


    @OnDisconnect
    public void onDisconnect(SocketIOClient sourceClient) {
        String sourceToken = SocketIoUitls.getSingleUrlParam(sourceClient,"token");
        UserInfo userInfo = this.userInfo.remove(sourceToken);
        String targetToken = userInfo.getToken();
        if(targetToken != null){
            UserInfo targetUserInfo = this.userInfo.get(targetToken);
            targetUserInfo.setToken(null);
            SocketIOClient targetClient = allClients.get(targetToken);
            targetClient.sendEvent(SocketIoEvent.Virtual_DISCONNECTION);
        }

        allClients.remove(sourceToken);

        System.out.println( "-------------------------" + "客户端已断开连接");
    }
}
