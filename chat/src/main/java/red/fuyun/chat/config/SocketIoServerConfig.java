package red.fuyun.chat.config;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.SpringAnnotationScanner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *Netty-SicketIo的配置类
 */
@Configuration
public class SocketIoServerConfig {
    /**
     *Socket连接地址
     */
    @Value("${socketio.hostname}")
    private String hostname;

    /**
     *Socket连接端口
     */
    @Value("${socketio.port}")
    private Integer port;


    /**
     *注册Socket服务的bean到容器中去
     */
    @Bean
    public SocketIOServer socketIOServer(){
        com.corundumstudio.socketio.Configuration configuration = new com.corundumstudio.socketio.Configuration();
        configuration.setPort(port);
        configuration.setHostname(hostname);
        final SocketIOServer server = new SocketIOServer(configuration);
        return server;
    }


    /**
     *用于扫描netty-socketio的注解，比如 @OnConnect、@OnEvent
     */
    @Bean
    public SpringAnnotationScanner springAnnotationScanner() {
        return new SpringAnnotationScanner(socketIOServer());
    }



}
