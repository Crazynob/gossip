package red.fuyun.chat.util;

import com.corundumstudio.socketio.SocketIOClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SocketIoUitls {

    private static  ObjectMapper objectMapper = new ObjectMapper();

    public static String BenaToJson(Object value) throws JsonProcessingException {
        return objectMapper.writeValueAsString(value);
    }
    public static String getToken(SocketIOClient client){
        return client.getHandshakeData().getSingleUrlParam("token");
    }


}
