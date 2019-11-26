/**
 * Copyright (C), 2019, 安徽雪影实业有限公司
 * FileName: MessageDto
 * Author:   xugongzhi
 * Date:     2019/11/25 14:37
 * Description: 传输消息的实体类
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package red.fuyun.chat.entiy;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 〈传输消息的实体类〉
 *
 * @author xugongzhi
 * @date 2019/11/25
 * @since 1.0.0
 */
@AllArgsConstructor
@Data
public class MessageDo {
    /**
     *信息归属谁 true自己
     */
    private boolean me;

    /**
     *信息的类型
     */
    private int mime;

    /**
     *信息内容
     */
    private String message;

    /**
     *发送信息的时间
     */
    private Long time;
}
