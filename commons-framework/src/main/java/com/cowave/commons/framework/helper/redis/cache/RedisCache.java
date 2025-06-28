package com.cowave.commons.framework.helper.redis.cache;

import com.cowave.commons.framework.helper.redis.RedisHelper;
import com.cowave.commons.framework.helper.redis.StringRedisHelper;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@ConditionalOnProperty("spring.cache.l2-enable")
@ConditionalOnClass(name = "org.springframework.data.redis.core.RedisTemplate")
@Component
@Slf4j
public class RedisCache {

    private final CacheProperties cacheProperties;

    private final RedisHelper redisHelper;

    private final StringRedisHelper stringRedisHelper;

    public Object get(String cacheName, Object key){
        String redisKey = cacheName + ":" + key.toString();
        Object value = null;
        try{
            value = redisHelper.getValue(redisKey);
            int expireAfterAccess = cacheProperties.l2ExpireAfterAccess(cacheName);
            // 重置缓存时间
            if(value != null && expireAfterAccess > 0){
                redisHelper.expire(redisKey, expireAfterAccess, TimeUnit.SECONDS);
            }
            log.debug("Cache redis get, {}={}", redisKey, value);
        }catch (Exception e){
            log.error("Cache redis get failed, {}", redisKey, e);
        }
        return value;
    }

    public void put(String cacheName, Object key, Object value){
        String redisKey = cacheName + ":" + key.toString();
        int expireAfterWrite = cacheProperties.l2ExpireAfterWrite(cacheName);
        try{
            if (expireAfterWrite > 0) {
                redisHelper.putExpire(redisKey, value, expireAfterWrite, TimeUnit.SECONDS);
            } else {
                redisHelper.putValue(redisKey, value);
            }
            log.debug("Cache redis put, {}={}", redisKey, value);
        }catch(Exception e){
            log.error("Cache redis put failed, {}", redisKey, e);
        }
    }

    public void evict(String cacheName, Object key) {
        redisHelper.delete(cacheName + ":" + key.toString());
    }

    public void clear(String cacheName) {
        stringRedisHelper.luaClean(cacheName + ":*");
    }
}
