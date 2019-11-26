/**
 * Copyright (C), 2019, 安徽雪影实业有限公司
 * FileName: SocketServer
 * Author:   xugongzhi
 * Date:     2019/11/26 8:08
 * Description: 处理Socket事件
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package red.fuyun.chat.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import red.fuyun.chat.constant.RedisKey;
import red.fuyun.chat.constant.SocketIoEvent;
import red.fuyun.chat.entiy.MatcheDo;
import red.fuyun.chat.entiy.MessageDo;
import red.fuyun.chat.entiy.UserInfo;
import red.fuyun.chat.enums.MIME;
import red.fuyun.chat.util.SocketIoUitls;

import java.io.IOException;
import java.util.*;

/**
 * 〈处理Socket事件〉
 *
 * @author xugongzhi
 * @date 2019/11/26
 * @since 1.0.0
 */
@Service
public class SocketEventHandle {

    @Autowired
    private RedisTemplate<String, MessageDo> messageRedisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    /**
     *  所有已连接的客户端
     */
    private HashMap<String,SocketIOClient> allClients = new HashMap<>();


    /**
     *  所有已匹配的客户端
     */
//    private HashMap<String,SocketIOClient> matched = new HashMap<>();

    /**
     *  所有待匹配的客户端
     */
//    private HashMap<String,SocketIOClient> matching = new HashMap<>();

    /**
     *  所有已连接的用户信息
     */
    private HashMap<String, UserInfo> userInfo = new HashMap<>();


    /**
     *  l维护连接信息
     */
//    private HashMap<String, String> mapping = new HashMap<>();

    /**
     *  待匹配的用户的key
     */
    private Queue<String> matchingKeys = new LinkedList<>();



    /**
     * 建立连接时把用户信息保存到Map集合内 Key为token
     * redis 中每个连接 维护一个key为Token Value为 匹配历史List 作用记录查询匹配记录与相应的聊天记录
     * 聊天记录的key 为 sourceToken+targetToken Value 为每条聊天信息记录
     *每条聊天信息记录格式: 消息类型&消息内容&消息时间戳
     * {"me":false,"mime":0,"message":"1321\n","time":1574746443008}
     *
     */

    public void onConnect(SocketIOClient client) {
        String sourceToken = SocketIoUitls.getSingleUrlParam(client,"token");
        int age = Integer.valueOf(SocketIoUitls.getSingleUrlParam(client,"age"));
        if(age>0 && age<=100){
            age = 18;
        }

        String sex = SocketIoUitls.getSingleUrlParam(client,"sex");
        boolean bool = "男".equals(sex) || "女".equals(sex);
        if(!bool){
            sex = "男";
        }
        userInfo.put(sourceToken,new UserInfo(sex,age));
        allClients.put(sourceToken,client);
        System.out.println( "-------------------------" + "客户端已连接:");


    }


    /**
     * 历史消息查询
     */
    private void historyMessage(SocketIOClient sourceClient) throws IOException {

        System.out.println("-------------------historyMessage-----------------------");
        String sourceToken = SocketIoUitls.getSingleUrlParam(sourceClient,"token");
        String targetToken = this.userInfo.get(sourceToken).getToken();
        SocketIOClient targetClient = allClients.get(targetToken);

        String key1 = RedisKey.MESSAGE+sourceToken+targetToken;
        String key2 = RedisKey.MESSAGE+targetToken+sourceToken;
        ListOperations<String, String> opsList = stringRedisTemplate.opsForList();
        Long key1Size = opsList.size(key1);
        Long key2Size = opsList.size(key2);
        if((key1Size+key2Size) == 0){
            return;
        }
        List<String> allKey1 = opsList.range(key1, 0, key1Size);
        List<String> allKey2 = opsList.range(key2, 0, key2Size);
        int key1Index = 0;
        int key2Index = 0;
        for (int i = 0; i < allKey1.size()+allKey2.size() ; i++) {
            MessageDo messageDo1 = null;
            MessageDo messageDo2= null;

            if(key1Index < allKey1.size()){
                messageDo1 = SocketIoUitls.jsonToMessageDo(allKey1.get(key1Index));
            }

            if(key2Index < allKey2.size()){
                messageDo2 = SocketIoUitls.jsonToMessageDo(allKey2.get(key2Index));
            }

            if(messageDo1 != null && messageDo2 != null){
                if(messageDo1.getTime() < messageDo2.getTime()){
                    key1Index++;
                    SocketIoUitls.historyMessage(sourceClient,targetClient,messageDo1,true);
                }else{
                    key2Index++;
                    SocketIoUitls.historyMessage(sourceClient,targetClient,messageDo2,false);
                }

            }

            if (messageDo1 == null){
                key2Index++;
                SocketIoUitls.historyMessage(sourceClient,targetClient,messageDo2,false);
            }

            if (messageDo2 == null){
                key1Index++;
                SocketIoUitls.historyMessage(sourceClient,targetClient,messageDo1,true);
            }
        }
        System.out.println("-------------------historyMessageEND-----------------------");
    }



    /**
     *用户匹配方法
     * sourceClient 触发事件的用户连接
     * data 触发事件时携带的数据
     */

    public void matche(SocketIOClient sourceClient, String data) throws JsonProcessingException {
        String sourceToken = SocketIoUitls.getSingleUrlParam(sourceClient,"token");
        UserInfo sourceUserInfo = this.userInfo.get(sourceToken);
        String targetToken = sourceUserInfo.getToken();
        if(targetToken !=  null){
            return;
        }
        if(matchingKeys.size() == 0){
            matchingKeys.add(sourceToken);
            return;
        }

        //被匹配的连接
        SocketIOClient targetClient = allClients.get(matchingKeys.poll());

        targetToken = SocketIoUitls.getSingleUrlParam(targetClient,"token");
        sourceUserInfo.setToken(targetToken);

        UserInfo targetUserInfo = this.userInfo.get(targetToken);
        targetUserInfo.setToken(sourceToken);

        MatcheDo sourceMatcheDo = new MatcheDo(true,sourceUserInfo);
        MatcheDo targetMatcheDo = new MatcheDo(true,targetUserInfo);

        String source = SocketIoUitls.beanToJson(sourceMatcheDo);
        String target = SocketIoUitls.beanToJson(targetMatcheDo);
        sourceClient.sendEvent(SocketIoEvent.MATCHE,target);
        targetClient.sendEvent(SocketIoEvent.MATCHE,source);

        SetOperations<String, String> opsSet = stringRedisTemplate.opsForSet();

        String sourceKey = RedisKey.MATCHED+sourceToken;
        String targetKey = RedisKey.MATCHED+targetToken;
        opsSet.add(sourceKey,targetToken);
        opsSet.add(targetKey,sourceToken);

        try {
            historyMessage(sourceClient);
        } catch (IOException e) {
            System.out.println("json转对象错误");
            e.printStackTrace();
        }
        System.out.println("--------------------匹配事件");
    }

    public void sendMsg(SocketIOClient sourceClient, String data) throws JsonProcessingException {
        String sourceToken = SocketIoUitls.getSingleUrlParam(sourceClient,"token");
        String targetToken = this.userInfo.get(sourceToken).getToken();
        if(targetToken == null){
            return;
        }
        MessageDo messageDo = null;
        try {
            messageDo = SocketIoUitls.jsonToMessageDo(data);
        } catch (IOException e) {
            messageDo = new MessageDo(false, MIME.TEXT.ordinal(),"对方信息发送失败！！！",System.currentTimeMillis());
            e.printStackTrace();
        }

        SocketIOClient targetClient = allClients.get(targetToken);
        messageDo.setMe(false);
        String message = SocketIoUitls.beanToJson(messageDo);
        targetClient.sendEvent(SocketIoEvent.SEND_MESSAGE, message);
        ListOperations listOperations = messageRedisTemplate.opsForList();
        String key = RedisKey.MESSAGE+sourceToken+targetToken;
        listOperations.rightPush(key, messageDo);

        System.out.println(SocketIoEvent.SEND_MESSAGE+data);
    }


    /**
     * 逻辑上断开连接
     */

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


    /**
     * 断开连接
     */
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
