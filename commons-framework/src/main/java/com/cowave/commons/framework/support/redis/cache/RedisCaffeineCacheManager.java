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
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author shanhuiming
 *
 */
@RequiredArgsConstructor
public class RedisCaffeineCacheManager implements CacheManager {

    private final ConcurrentMap<String, Cache> caches = new ConcurrentHashMap<>();

    private final CacheProperties cacheProperties;

    private final RedisHelper redisHelper;

    @Override
    public Collection<String> getCacheNames() {
        return caches.keySet();
    }

    @Override
    public Cache getCache(String cacheName) {
        Cache cache = caches.get(cacheName);
        if(cache != null) {
            return cache;
        }
        return caches.computeIfAbsent(cacheName,
                k -> new RedisCaffeineCache(cacheName, cacheProperties, redisHelper, caffeine()));
    }

    private com.github.benmanes.caffeine.cache.Cache<Object, Object> caffeine(){
        Caffeine<Object, Object> caffeineBuilder = Caffeine.newBuilder();
        if(cacheProperties.getExpireAfterAccess() > 0){
            caffeineBuilder.expireAfterAccess(cacheProperties.getExpireAfterAccess(), TimeUnit.SECONDS);
        }
        if(cacheProperties.getExpireAfterWrite() > 0){
            caffeineBuilder.expireAfterWrite(cacheProperties.getExpireAfterWrite(), TimeUnit.SECONDS);
        }
        if(cacheProperties.getInitialCapacity() > 0){
            caffeineBuilder.initialCapacity(cacheProperties.getInitialCapacity());
        }
        if(cacheProperties.getMaximumSize() > 0){
            caffeineBuilder.maximumSize(cacheProperties.getMaximumSize());
        }
        if(cacheProperties.getRefreshAfterWrite() > 0){
            caffeineBuilder.refreshAfterWrite(cacheProperties.getRefreshAfterWrite(), TimeUnit.SECONDS);
        }
        return caffeineBuilder.build();
    }
}
