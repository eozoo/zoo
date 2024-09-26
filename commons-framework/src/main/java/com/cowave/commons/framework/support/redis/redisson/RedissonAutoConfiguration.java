/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.support.redis.redisson;

import com.cowave.commons.framework.support.redis.connection.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.api.RedissonRxClient;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.redisson.spring.starter.RedissonProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisOperations;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author shanhuiming
 *
 */
@Slf4j
@AutoConfigureBefore({org.redisson.spring.starter.RedissonAutoConfiguration.class, LettuceAutoConfiguration.class, JedisAutoConfiguration.class})
@ConditionalOnClass({Redisson.class, RedisOperations.class})
@RequiredArgsConstructor
@Configuration
@EnableConfigurationProperties({RedisProperties.class, RedissonProperties.class})
public class RedissonAutoConfiguration {

    @Primary
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(ApplicationContext ctx, RedisProperties redisProperties, RedissonProperties redissonProperties,
                                         @Nullable List<RedissonAutoConfigurationCustomizer> redissonAutoConfigurationCustomizers,
                                         @Value("${spring.redis.exitOnConnectionFailed:false}") boolean exitOnConnectionFailed) throws IOException {
        return new CheckedRedissonClient(ctx, redisProperties, redissonProperties, redissonAutoConfigurationCustomizers, exitOnConnectionFailed);
    }

    @Primary
    @Lazy
    @Bean
    public RedissonReactiveClient redissonReactiveClient(RedissonClient redissonClient) {
        return redissonClient.reactive();
    }

    @Primary
    @Lazy
    @Bean
    public RedissonRxClient redissonRxClient(RedissonClient redissonClient) {
        return redissonClient.rxJava();
    }

    @Conditional(CommonRedisCondition.class)
    @Bean(destroyMethod = "shutdown")
    public RedissonClient commonRedissonClient(ApplicationContext ctx, Environment environment,
                                               @Nullable List<RedissonAutoConfigurationCustomizer> redissonAutoConfigurationCustomizers,
                                               @Value("${common.redis.exitOnConnectionFailed:false}") boolean exitOnConnectionFailed) throws IOException {
        RedisProperties redisProperties = Binder.get(environment).bind("common.redis", RedisProperties.class).get();
        RedissonProperties redissonProperties = Binder.get(environment).bind("common.redis.redisson", RedissonProperties.class).orElse(null);
        return new CheckedRedissonClient(ctx, redisProperties, redissonProperties, redissonAutoConfigurationCustomizers, exitOnConnectionFailed);
    }

    @ConditionalOnBean(name = "commonRedissonClient")
    @Lazy
    @Bean
    public RedissonReactiveClient publicRedissonReactive(@Qualifier("commonRedissonClient") RedissonClient redissonClient) {
        return redissonClient.reactive();
    }

    @ConditionalOnBean(name = "commonRedissonClient")
    @Lazy
    @Bean
    public RedissonRxClient publicredissonRxJava(@Qualifier("commonRedissonClient") RedissonClient redissonClient) {
        return redissonClient.rxJava();
    }
}
