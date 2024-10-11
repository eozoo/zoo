/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.support.redis.cache;

import com.cowave.commons.framework.support.redis.RedisHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nullable;

/**
 *
 * @author shanhuiming
 *
 */
@EnableCaching
@RequiredArgsConstructor
@ConditionalOnClass(name = {"com.github.benmanes.caffeine.cache.Cache", "org.springframework.data.redis.core.RedisTemplate"})
@EnableConfigurationProperties(CacheProperties.class)
@Configuration
public class RedisCaffeineCacheConfiguration {

    private final CacheProperties cacheProperties;

    @Nullable
    private final RedisHelper redisHelper;

    @ConditionalOnMissingBean(RedisCaffeineCacheManager.class)
    @Bean
    public RedisCaffeineCacheManager cacheManager() {
        return new RedisCaffeineCacheManager(cacheProperties, redisHelper);
    }
}
