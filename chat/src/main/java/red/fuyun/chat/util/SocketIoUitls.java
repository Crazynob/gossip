package red.fuyun.chat.util;

import com.corundumstudio.socketio.SocketIOClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import red.fuyun.chat.constant.SocketIoEvent;
import red.fuyun.chat.entiy.MessageDo;

import java.io.IOException;


/**
 * Socket服务的工具类
 */
public class SocketIoUitls {

    private static  ObjectMapper objectMapper = new ObjectMapper();


    /**
     * 确定消息归属于谁
     * sourceClient : 当前触发事件的用户的连接
     * targetClient : 与当前触发事件的用户所配对的连接
     * message ： 消息实体
     * source ： 以当前触发事件的用户为起点 是触发事件的用户的连接则为true  代表当前消息属于谁
     */
    public static void historyMessage(SocketIOClient sourceClient,SocketIOClient targetClient, MessageDo message, boolean source) throws JsonProcessingException {
        message.setMe(source);
        sourceClient.sendEvent(SocketIoEvent.SEND_MESSAGE, SocketIoUitls.beanToJson(message));
        message.setMe(!source);
        targetClient.sendEvent(SocketIoEvent.SEND_MESSAGE, SocketIoUitls.beanToJson(message));
    }

    /**
     * 实体转为JSON字符串
     */
    public static String beanToJson(Object value) throws JsonProcessingException {
        return objectMapper.writeValueAsString(value);
    }


    /**
     * JSON字符串转为MessageDo对象
     */
    public static MessageDo jsonToMessageDo(String value) throws IOException {
        return objectMapper.readValue(value, MessageDo.class);
    }



    public static String getSingleUrlParam(SocketIOClient client, String name){
        return client.getHandshakeData().getSingleUrlParam(name);
    }


}
