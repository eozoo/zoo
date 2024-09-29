/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.support.redis.redisson.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author aKuang
 *
 */
@Slf4j
@RequiredArgsConstructor
public class RedissonLockHelper {

    private final RedissonClient redisson;

    // 以进程为单位，不区分线程
    private final Map<String, RLock> lockMap = new ConcurrentHashMap<>();

    /**
     * 永久加锁
     * @param awaitTime 等待时间
     * @param timeUnit  时间单位
     * @param name  锁名称
     * @param keys  锁标识信息
     */
    public boolean tryLock(long awaitTime, TimeUnit timeUnit, String name, String... keys) {
        return tryLock(awaitTime, -1, timeUnit, name, keys);
    }

    /**
     * 加锁
     * @param awaitTime 等待时间
     * @param leaseTime 存续时间（-1表示永久）
     * @param timeUnit  时间单位
     * @param name  锁名称
     * @param keys  锁标识信息
     */
    public boolean tryLock(long awaitTime, long leaseTime, TimeUnit timeUnit, String name, String... keys) {
        String lockKey = key(name, keys);
        if(lockMap.containsKey(lockKey)){
            return true;
        }

        try {
            RLock lock = redisson.getLock(lockKey);
            boolean locked = lock.tryLock(awaitTime, leaseTime, timeUnit);
            if (locked) {
                log.debug("(redisson) lock success, key={}", lockKey);
                if(leaseTime == -1L){
                    lockMap.put(lockKey, lock);
                }
            }else{
                log.debug("(redisson) lock failed, key={}", lockKey);
            }
            return locked;
        } catch (Exception e) {
            throw new RuntimeException("", e);
        }
    }

    /**
     * 释放锁
     */
    public void releaseLock(String name, String... keys) {
        RLock rLock = lockMap.remove(key(name, keys));
        if (rLock != null) {
            rLock.forceUnlock();
        }
    }

    private String key(String name, String... keys) {
        return String.join(":", name, String.join(":", keys));
    }
}
