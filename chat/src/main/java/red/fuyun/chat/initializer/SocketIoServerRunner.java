package red.fuyun.chat.initializer;

import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

//@Order(1)
//@Component
public class SocketIoServerRunner implements CommandLineRunner {

    @Autowired
    SocketIOServer server;

    public void run(String... args) throws Exception {
        server.start();
        System.out.println("socket.io启动成功！");
    }
}
