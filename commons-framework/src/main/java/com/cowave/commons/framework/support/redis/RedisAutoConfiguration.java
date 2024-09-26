/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.support.redis;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionCommands;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 *
 * @author shanhuiming
 *
 */
@AutoConfigureBefore(org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class)
@ConditionalOnClass(RedisOperations.class)
public class RedisAutoConfiguration {

    @Primary
    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        // Hash的key也采用StringRedisSerializer的序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        return template;
    }

    @Primary
    @Bean
    public RedisHelper redisHelper(RedisTemplate<Object, Object> redisTemplate){
        return RedisHelper.newRedisHelper(redisTemplate);
    }

    @Bean
    @Primary
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory);
    }

    @Primary
    @Bean
    public StringRedisHelper stringRedisHelper(StringRedisTemplate stringRedisTemplate,
            @Value("${spring.redis.exitOnConnectionFailed:false}") boolean exitOnConnectionFailed){
        if(exitOnConnectionFailed && !"PONG".equals(stringRedisTemplate.execute(RedisConnectionCommands::ping))){
            throw new IllegalStateException("Redis connection failed");
        }
        return StringRedisHelper.newStringRedisHelper(stringRedisTemplate);
    }

    @ConditionalOnBean(name = "commonRedisConnectionFactory")
    @Bean
    public RedisTemplate<Object, Object> commonRedisTemplate(
            @Qualifier("commonRedisConnectionFactory") RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        // Hash的key也采用StringRedisSerializer的序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        return template;
    }

    @ConditionalOnBean(name = "commonRedisTemplate")
    @Bean
    public RedisHelper commonRedisHelper(
            @Qualifier("commonRedisTemplate") RedisTemplate<Object, Object> redisTemplate){
        return RedisHelper.newRedisHelper(redisTemplate);
    }

    @ConditionalOnBean(name = "commonRedisConnectionFactory")
    @Bean
    public StringRedisTemplate commonStringRedisTemplate(
            @Qualifier("commonRedisConnectionFactory") RedisConnectionFactory redisConnectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory);
    }

    @ConditionalOnBean(name = "commonStringRedisTemplate")
    @Bean
    public StringRedisHelper commonStringRedisHelper(
            @Qualifier("commonStringRedisTemplate") StringRedisTemplate stringRedisTemplate,
            @Value("${common.redis.exitOnConnectionFailed:false}") boolean exitOnConnectionFailed){
        if(exitOnConnectionFailed && !"PONG".equals(stringRedisTemplate.execute(RedisConnectionCommands::ping))){
            throw new IllegalStateException("Redis connection failed");
        }
        return StringRedisHelper.newStringRedisHelper(stringRedisTemplate);
    }
}
