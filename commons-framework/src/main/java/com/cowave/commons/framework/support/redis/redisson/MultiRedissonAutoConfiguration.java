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
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.redisson.spring.starter.RedissonProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
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
@RequiredArgsConstructor
@Configuration
@ConditionalOnClass({Redisson.class, RedisOperations.class})
@AutoConfigureBefore({RedissonAutoConfiguration.class, LettuceAutoConfiguration.class, JedisAutoConfiguration.class})
public class MultiRedissonAutoConfiguration {

    private final ApplicationContext ctx;

    @Nullable
    private final List<RedissonAutoConfigurationCustomizer> redissonAutoConfigurationCustomizers;

    @Value("${spring.redis.private.exitOnConnectionFailed:false}")
    private boolean privateExitOnConnectionFailed;

    @Value("${spring.redis.public.exitOnConnectionFailed:false}")
    private boolean publicExitOnConnectionFailed;

    @Conditional(MultiPrivateRedisCondition.class)
    @Primary
    @Bean(destroyMethod = "shutdown")
    public RedissonClient privateRedisson(Environment environment) throws IOException {
        RedisProperties redisProperties = Binder.get(environment).bind("spring.redis.private", RedisProperties.class).get();
        RedissonProperties redissonProperties = Binder.get(environment).bind("spring.redis.private.redisson", RedissonProperties.class).orElse(null);
        return new CheckedRedissonClient(ctx, redisProperties, redissonProperties,
                redissonAutoConfigurationCustomizers, privateExitOnConnectionFailed);
    }

    @Conditional(MultiPrivateRedisCondition.class)
    @Primary
    @Lazy
    @Bean
    public RedissonReactiveClient privateRedissonReactive(RedissonClient redisson) {
        return redisson.reactive();
    }

    @Conditional(MultiPrivateRedisCondition.class)
    @Primary
    @Lazy
    @Bean
    public RedissonRxClient privateRedissonRxJava(RedissonClient redisson) {
        return redisson.rxJava();
    }

    @Conditional(MultiPublicRedisCondition.class)
    @Bean(destroyMethod = "shutdown")
    public RedissonClient publicRedisson(Environment environment) throws IOException {
        RedisProperties redisProperties = Binder.get(environment).bind("spring.redis.public", RedisProperties.class).get();
        RedissonProperties redissonProperties = Binder.get(environment).bind("spring.redis.public.redisson", RedissonProperties.class).orElse(null);
        return new CheckedRedissonClient(ctx, redisProperties, redissonProperties,
                redissonAutoConfigurationCustomizers, publicExitOnConnectionFailed);
    }

    @Conditional(MultiPublicRedisCondition.class)
    @Lazy
    @Bean
    public RedissonReactiveClient publicRedissonReactive(@Qualifier("publicRedisson") RedissonClient redisson) {
        return redisson.reactive();
    }

    @Conditional(MultiPublicRedisCondition.class)
    @Lazy
    @Bean
    public RedissonRxClient publicredissonRxJava(@Qualifier("publicRedisson") RedissonClient redisson) {
        return redisson.rxJava();
    }
}
