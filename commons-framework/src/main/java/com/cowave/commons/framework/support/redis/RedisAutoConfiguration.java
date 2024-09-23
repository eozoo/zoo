package com.cowave.commons.framework.support.redis;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.cowave.commons.framework.support.redis.connection.JedisAutoConfiguration;
import com.cowave.commons.framework.support.redis.connection.LettuceAutoConfiguration;

/**
 *
 * @author shanhuiming
 *
 */
@ConditionalOnClass(RedisOperations.class)
@Import({ JedisAutoConfiguration.class, LettuceAutoConfiguration.class })
public class RedisAutoConfiguration {

	@ConditionalOnMissingBean(RedisTemplate.class)
    @Primary
    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        RedisJsonSerializer<?> serializer = new RedisJsonSerializer<>(Object.class);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);

        // Hash的key也采用StringRedisSerializer的序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        return template;
    }

	@ConditionalOnMissingBean(RedisHelper.class)
    @Primary
    @Bean
    public RedisHelper redisHelper(RedisTemplate<Object, Object> redisTemplate){
        return RedisHelper.newRedisHelper(redisTemplate);
    }
}
