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

import lombok.Data;
import red.fuyun.chat.constant.MIME;

/**
 * 〈传输消息的实体类〉
 *
 * @author xugongzhi
 * @date 2019/11/25
 * @since 1.0.0
 */
@Data
public class MessageDto {
    private int mime;
    private String message;
    private Long time;
}
