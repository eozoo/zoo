package com.cowave.commons.framework.support.redis;

import com.cowave.commons.framework.support.redis.connection.JedisAutoConfiguration;
import com.cowave.commons.framework.support.redis.connection.LettuceAutoConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 *
 * @author shanhuiming
 *
 */
@AutoConfigureBefore(RedisAutoConfiguration.class)
@ConditionalOnClass(RedisOperations.class)
@Import({ JedisAutoConfiguration.class, LettuceAutoConfiguration.class })
public class MultiRedisAutoConfiguration {

    @ConditionalOnBean(name = "privateRedisConnectionFactory")
    @Bean
    public StringRedisTemplate privateStringRedisTemplate(
            @Qualifier("privateRedisConnectionFactory") RedisConnectionFactory publicRedisConnectionFactory) {
        return new StringRedisTemplate(publicRedisConnectionFactory);
    }

    @ConditionalOnBean(name = "privateRedisConnectionFactory")
    @Primary
    @Bean
    public RedisTemplate<Object, Object> privateRedisTemplate(
            @Qualifier("privateRedisConnectionFactory") RedisConnectionFactory privateRedisConnectionFactory){
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(privateRedisConnectionFactory);

        RedisJsonSerializer<?> serializer = new RedisJsonSerializer<>(Object.class);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);

        // Hash的key也采用StringRedisSerializer的序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        return template;
    }

    @ConditionalOnBean(name = "privateRedisTemplate")
    @Primary
    @Bean
    public RedisHelper privateRedisHelper(@Qualifier("privateRedisTemplate") RedisTemplate<Object, Object> privateRedisTemplate){
        return RedisHelper.newRedisHelper(privateRedisTemplate);
    }

    @ConditionalOnBean(name = "publicRedisConnectionFactory")
    @Bean
    public StringRedisTemplate publicStringRedisTemplate(
            @Qualifier("publicRedisConnectionFactory") RedisConnectionFactory publicRedisConnectionFactory) {
        return new StringRedisTemplate(publicRedisConnectionFactory);
    }

    @ConditionalOnBean(name = "publicRedisConnectionFactory")
    @Bean
    public RedisTemplate<Object, Object> publicRedisTemplate(
            @Qualifier("publicRedisConnectionFactory") RedisConnectionFactory publicRedisConnectionFactory){
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(publicRedisConnectionFactory);

        RedisJsonSerializer<?> serializer = new RedisJsonSerializer<>(Object.class);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);

        // Hash的key也采用StringRedisSerializer的序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        return template;
    }

    @ConditionalOnBean(name = "publicRedisTemplate")
    @Bean
    public RedisHelper publicRedisHelper(@Qualifier("publicRedisTemplate") RedisTemplate<Object, Object> publicRedisTemplate){
        return RedisHelper.newRedisHelper(publicRedisTemplate);
    }
}
