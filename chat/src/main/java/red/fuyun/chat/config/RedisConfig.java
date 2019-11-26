/**
 * Copyright (C), 2019, 安徽雪影实业有限公司
 * FileName: RedisConfig
 * Author:   xugongzhi
 * Date:     2019/11/25 16:32
 * Description: Redis的配置类
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package red.fuyun.chat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import red.fuyun.chat.entiy.MessageDo;

import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

/**
 * 〈Redis的配置类〉
 *
 * @author xugongzhi
 * @date 2019/11/25
 * @since 1.0.0
 */
@Configuration
public class RedisConfig {

    /**
     * 配置对象写入到redis中的序列化规则
     */
    @Bean
    public RedisTemplate<String, MessageDo> messageRedisTemplate(RedisConnectionFactory redisConnectionFactory) throws UnknownHostException {
        RedisTemplate<String, MessageDo> template = new RedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer(StandardCharsets.UTF_8));
        template.setValueSerializer(new Jackson2JsonRedisSerializer(MessageDo.class));
        return template;
    }

}
