package red.fuyun.chat.server;


import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import red.fuyun.chat.entiy.MatcheDto;
import red.fuyun.chat.entiy.UserInfo;
import red.fuyun.chat.util.SocketIoUitls;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

/***
 * Socket服务
 */
@Component
public class SocketServer {
    private SocketIOServer server;
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
    }


//    建立连接时把用户信息保存到Map集合内 Key为token
    @OnConnect
    public void onConnect(SocketIOClient client) {

        String sourceToken = client.getHandshakeData().getSingleUrlParam("token");
        String age = client.getHandshakeData().getSingleUrlParam("age");
        String sex = client.getHandshakeData().getSingleUrlParam("sex");

        userInfo.put(sourceToken,new UserInfo(sex, Integer.valueOf(age)));
        allClients.put(sourceToken,client);
        System.out.println( "-------------------------" + "客户端已连接:");


    }




    @OnEvent(value = "matche")
    public void onEvent(SocketIOClient sourceClient, String data) throws JsonProcessingException {
//        String sourceToken = sourceClient.getHandshakeData().getSingleUrlParam("token");
        String sourceToken = SocketIoUitls.getToken(sourceClient);


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

        String targetToken = targetClient.getHandshakeData().getSingleUrlParam("token");
//        UserInfo sourceUserInfo = this.userInfo.get(sourceToken);
        sourceUserInfo.setToken(targetToken);

        UserInfo targetUserInfo = this.userInfo.get(targetToken);
        targetUserInfo.setToken(sourceToken);

        MatcheDto sourceMatcheDto = new MatcheDto(true,sourceUserInfo);
        MatcheDto targetMatcheDto = new MatcheDto(true,targetUserInfo);
//        ObjectMapper objectMapper = new ObjectMapper();
//        String source = objectMapper.writeValueAsString(sourceMatcheDto);
        String source = SocketIoUitls.BenaToJson(sourceMatcheDto);
        String target = SocketIoUitls.BenaToJson(targetMatcheDto);

        sourceClient.sendEvent("matche",target);
        targetClient.sendEvent("matche",source);

        System.out.println("--------------------匹配事件");
    }

    @OnEvent(value = "sendMsg")
    public void sendMsg(SocketIOClient sourceClient, String data){
//        String sourceToken = sourceClient.getHandshakeData().getSingleUrlParam("token");
        String sourceToken = SocketIoUitls.getToken(sourceClient);
        UserInfo sourceUserInfo = this.userInfo.get(sourceToken);
        String targetToken = sourceUserInfo.getToken();
        if(targetToken == null){
            return;
        }
        SocketIOClient targetClient = allClients.get(targetToken);

        targetClient.sendEvent("sendMsg", data);
        System.out.println("sendMsg:"+data);
    }


    @OnEvent(value = "disc")
    public void disc(SocketIOClient sourceClient){
//        String sourceToken = sourceClient.getHandshakeData().getSingleUrlParam("token");
        String sourceToken = SocketIoUitls.getToken(sourceClient);
        UserInfo sourceUserInfo = this.userInfo.get(sourceToken);


        String targetToken = sourceUserInfo.getToken();
        UserInfo targetUserInfo = this.userInfo.get(targetToken);

        sourceUserInfo.setToken(null);
        targetUserInfo.setToken(null);

        SocketIOClient socketIOClient = allClients.get(targetToken);
        socketIOClient.sendEvent("disc");


    }


    @OnDisconnect
    public void onDisconnect(SocketIOClient sourceClient) {
//        String token = sourceClient.getHandshakeData().getSingleUrlParam("token");
        String sourceToken = SocketIoUitls.getToken(sourceClient);
        UserInfo userInfo = this.userInfo.get(sourceToken);
        String targetToken = userInfo.getToken();
        SocketIOClient targetClient = allClients.get(targetToken);
        targetClient.sendEvent("disc");
        System.out.println( "-------------------------" + "客户端已断开连接");

    }
}
