package red.fuyun.chat.server;


import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import red.fuyun.chat.constant.SocketIoEvent;
import red.fuyun.chat.service.SocketEventHandle;

import javax.annotation.PostConstruct;

/***
 * Socket服务
 */
@Component
public class SocketServer {

    private SocketIOServer server;

    @Autowired
    private SocketEventHandle socketEventHandle;

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
    public void onConnect(SocketIOClient sourceClient) {
        socketEventHandle.onConnect(sourceClient);
    }




    /**
     *用户匹配方法
     * sourceClient 触发事件的用户连接
     * data 触发事件时携带的数据
     */
    @OnEvent(value = SocketIoEvent.MATCHE)
    public void matche(SocketIOClient sourceClient, String data) throws JsonProcessingException {
        socketEventHandle.matche(sourceClient,data);
    }

    @OnEvent(value = SocketIoEvent.SEND_MESSAGE)
    public void sendMsg(SocketIOClient sourceClient, String data) throws JsonProcessingException {
        socketEventHandle.sendMsg(sourceClient,data);
    }


    /**
     * 逻辑上断开连接
     */
    @OnEvent(value = SocketIoEvent.Virtual_DISCONNECTION)
    public void disc(SocketIOClient sourceClient){
        socketEventHandle.disc(sourceClient);
    }


    /**
     * 断开连接
     */
    @OnDisconnect
    public void onDisconnect(SocketIOClient sourceClient) {
        socketEventHandle.onDisconnect(sourceClient);
    }
}
