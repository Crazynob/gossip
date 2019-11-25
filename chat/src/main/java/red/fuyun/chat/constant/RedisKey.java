/**
 * Copyright (C), 2019, 安徽雪影实业有限公司
 * FileName: redisKey
 * Author:   xugongzhi
 * Date:     2019/11/25 16:28
 * Description: redis的Key前缀
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package red.fuyun.chat.constant;

/**
 * 〈redis的Key前缀〉
 *
 * @author xugongzhi
 * @date 2019/11/25
 * @since 1.0.0
 */
public class RedisKey {
    /**
     * 消息的前缀
     */
    public static final String MESSAGE = "message:";
    /**
     * 已连接的前缀
     */
    public static final String MATCHED = "matched:";
}
