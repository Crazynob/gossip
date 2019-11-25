/**
 * Copyright (C), 2019, 安徽雪影实业有限公司
 * FileName: SocketIoEvent
 * Author:   xugongzhi
 * Date:     2019/11/25 8:18
 * Description: 123
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package red.fuyun.chat.constant;

/**
 * 〈SocketIo自定义事件〉
 *
 * @author xugongzhi
 * @date 2019/11/25
 * @since 1.0.0
 */
public class SocketIoEvent {

    /**
     * 发送信息事件
     */
    public static final String SEND_MESSAGE = "sendMsg";

    /**
     * 匹配事件
     */
    public static final String MATCHE = "matche";


    /**
     * 逻辑上断开连接事件
     */
    public static final String Virtual_DISCONNECTION = "disc";
}
