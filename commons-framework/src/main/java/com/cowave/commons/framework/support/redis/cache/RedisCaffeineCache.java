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
import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.support.AbstractValueAdaptingCache;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author shanhuiming
 *
 */
@Slf4j
public class RedisCaffeineCache extends AbstractValueAdaptingCache {

    private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    private final CacheProperties cacheProperties;

    private final String cacheName;

    private final RedisHelper redisHelper;

    private final Cache<Object, Object> local;

    protected RedisCaffeineCache(String cacheName, CacheProperties cacheProperties,
                                 RedisHelper redisHelper, Cache<Object, Object> local) {
        super(true);
        this.cacheProperties = cacheProperties;
        this.cacheName = cacheName;
        this.local = local;
        this.redisHelper = redisHelper;
    }

    @Override
    public String getName() {
        return cacheName;
    }

    @Override
    public Object getNativeCache() {
        return this;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        Object value = lookup(key);
        if (value != null) {
            return (T) value;
        }

        ReentrantLock lock = locks.get(key.toString());
        if (lock == null) {
            lock = locks.computeIfAbsent(key.toString(), k -> new ReentrantLock());
        }
        try {
            lock.lock();
            value = lookup(key);
            if (value != null) {
                return (T) value;
            }

            value = valueLoader.call();
            Object storeValue = toStoreValue(value);
            put(key, storeValue);
            return (T) value;
        } catch (Exception e) {
            throw new ValueRetrievalException(key, valueLoader, e.getCause());
        } finally {
            lock.unlock();
        }
    }

    @Override
    protected Object lookup(Object key) {
        if(!cacheProperties.isL2Enable()){
            Object value = local.getIfPresent(key);
            log.debug("L1 cache get, key={}, value={}", key, value);
            return value;
        }

        if(cacheProperties.isL2First(cacheName)){
            return l2FirstLookup(key);
        }else{
            return l1FirstLookup(key);
        }
    }

    private Object l2FirstLookup(Object key) {
        // L2获取
        Object value = redisGet(key);
        if (value != null) {
            return value;
        }
        // L1获取
        value = local.getIfPresent(key);
        log.debug("L1 cache get, key={}, value={}", key, value);
        // L1同步到L2
        if (value != null) {
            redisPut(key, value);
        }
        return value;
    }

    private Object l1FirstLookup(Object key) {
        // L1获取
        Object value = local.getIfPresent(key);
        log.debug("L1 cache get, key={}, value={}", key, value);
        if (value != null) {
            return value;
        }
        // L2获取
        value = redisGet(key);
        // L2同步到L1
        if (value != null) {
            log.debug("L1 cache put, key={}, value={}", key, value);
            local.put(key, toStoreValue(value));
        }
        return value;
    }

    @Override
    public void put(Object key, Object value) {
        if(value == null){
          return;
        }
        local.put(key, toStoreValue(value));
        log.debug("L1 cache put, key={}, value={}", key, value);
        if(cacheProperties.isL2Enable()){
            redisPut(key, value);
        }
    }

    @Override
    public void evict(Object key) {
        redisHelper.delete(getKey(key));
        local.invalidate(key);
    }

    @Override
    public void clear() {
        Collection<String> keys = redisHelper.keys(this.cacheName.concat(":*"));
        for (String key : keys) {
            redisHelper.delete(key);
        }
        local.invalidateAll();
    }

    private Object redisGet(Object key){
        String redisKey = getKey(key);
        Object value = null;
        try{
            value = redisHelper.getValue(redisKey);
            log.debug("L2 cache get, key={}, value={}", redisKey, value);
        }catch (Exception e){
            log.error("L2 cache get failed, key={}", redisKey);
        }
        return value;
    }

    private void redisPut(Object key, Object value){
        String redisKey = getKey(key);
        int expire = cacheProperties.getExpire(cacheName);
        try{
            if (expire > 0) {
                redisHelper.putExpireValue(redisKey, toStoreValue(value), expire, TimeUnit.SECONDS);
            } else {
                redisHelper.putValue(redisKey, toStoreValue(value));
            }
            log.debug("L2 cache put, key={}, value={}", redisKey, value);
        }catch(Exception e){
            log.error("L2 cache put failed, key={}", redisKey);
        }
    }

    private String getKey(Object key) {
        return this.cacheName.concat(":").concat(key.toString());
    }
}
