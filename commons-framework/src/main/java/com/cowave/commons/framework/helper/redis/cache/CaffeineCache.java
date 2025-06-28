package com.cowave.commons.framework.helper.redis.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 *
 * @author shanhuiming
 *
 */
@ConditionalOnProperty("spring.cache.l1-enable")
@ConditionalOnClass(name = "com.github.benmanes.caffeine.cache.Cache")
@Component
@Slf4j
public class CaffeineCache {

    private final Cache<Object, Object> cache;

    public CaffeineCache(CacheProperties cacheProperties) {
        Caffeine<Object, Object> caffeineBuilder = Caffeine.newBuilder();
        if(cacheProperties.l1ExpireAfterAccess() > 0){
            caffeineBuilder.expireAfterAccess(cacheProperties.l1ExpireAfterAccess(), TimeUnit.SECONDS);
        }
        if(cacheProperties.l1ExpireAfterWrite() > 0){
            caffeineBuilder.expireAfterWrite(cacheProperties.l1ExpireAfterWrite(), TimeUnit.SECONDS);
        }
        if(cacheProperties.l1InitialCapacity() > 0){
            caffeineBuilder.initialCapacity(cacheProperties.l1InitialCapacity());
        }
        if(cacheProperties.l1MaximumSize() > 0){
            caffeineBuilder.maximumSize(cacheProperties.l1MaximumSize());
        }
        if(cacheProperties.l1RefreshAfterWrite() > 0){
            caffeineBuilder.refreshAfterWrite(cacheProperties.l1RefreshAfterWrite(), TimeUnit.SECONDS);
        }
        this.cache = caffeineBuilder.build();
    }

    public Object get(Object key){
        Object value = cache.getIfPresent(key);
        log.debug("Cache caffeine get, {}={}", key, value);
        return value;
    }

    public void put(Object key, Object value){
        cache.put(key, value);
        log.debug("Cache caffeine put, {}={}", key, value);
    }

    public void evict(Object key) {
        cache.invalidate(key);
    }

    public void clear() {
        cache.invalidateAll();
    }
}
