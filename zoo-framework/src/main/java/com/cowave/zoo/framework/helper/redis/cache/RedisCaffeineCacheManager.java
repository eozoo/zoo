/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.zoo.framework.helper.redis.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author shanhuiming
 *
 */
@RequiredArgsConstructor
public class RedisCaffeineCacheManager implements CacheManager {
    private final ConcurrentMap<String, Cache> caches = new ConcurrentHashMap<>();
    private final CacheProperties cacheProperties;
    private final CaffeineCache caffeineCache;
    private final RedisCache redisCache;

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
                k -> new RedisCaffeineCache(cacheName, cacheProperties, caffeineCache, redisCache));
    }
}
