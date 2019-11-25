package red.fuyun.chat.util;

import com.corundumstudio.socketio.SocketIOClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import red.fuyun.chat.entiy.MessageDto;

import java.io.IOException;

public class SocketIoUitls {

    private static  ObjectMapper objectMapper = new ObjectMapper();

    public static String BenaToJson(Object value) throws JsonProcessingException {
        return objectMapper.writeValueAsString(value);
    }

    public static MessageDto JsonToBena(String value) throws IOException {
        return objectMapper.readValue(value, MessageDto.class);
    }
    public static String getToken(SocketIOClient client){
        return client.getHandshakeData().getSingleUrlParam("token");
    }

    public static String getSingleUrlParam(SocketIOClient client, String name){
        return client.getHandshakeData().getSingleUrlParam(name);
    }


}
