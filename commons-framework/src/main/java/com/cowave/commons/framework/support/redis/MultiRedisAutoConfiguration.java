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
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
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
@AutoConfigureBefore(RedisAutoConfiguration.class)
@ConditionalOnClass(RedisOperations.class)
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

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();
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
    public RedisHelper privateRedisHelper(@Qualifier("privateRedisTemplate") RedisTemplate<Object, Object> privateRedisTemplate,
                                          @Qualifier("privateStringRedisTemplate") StringRedisTemplate privateStringRedisTemplate){
        return RedisHelper.newRedisHelper(privateRedisTemplate, privateStringRedisTemplate);
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

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);

        // Hash的key也采用StringRedisSerializer的序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        return template;
    }

    @ConditionalOnBean(name = "publicRedisTemplate")
    @Bean
    public RedisHelper publicRedisHelper(@Qualifier("publicRedisTemplate") RedisTemplate<Object, Object> publicRedisTemplate,
                                         @Qualifier("publicStringRedisTemplate") StringRedisTemplate publicStringRedisTemplate){
        return RedisHelper.newRedisHelper(publicRedisTemplate, publicStringRedisTemplate);
    }
}
