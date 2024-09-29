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
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

/**
 *
 * @author aKuang
 *
 */
@Aspect
@RequiredArgsConstructor
public class RedissonLockAspect {

    private final RedissonLockHelper lockHelper;

    @Pointcut("@annotation(com.cowave.commons.framework.support.redis.redisson.lock.RedissonLock)")
    public void pointCut() {
    }

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        RedissonLock redissonLock = signature.getMethod().getAnnotation(RedissonLock.class);
        String lockName = redissonLock.name();
        String lockKey = redissonLock.key();
        long awaitTime = redissonLock.await();
        long leaseTime = redissonLock.lease();
        try{
            if(lockHelper.tryLock(awaitTime, leaseTime, redissonLock.timeUnit(), lockName, lockKey)){
                return point.proceed();
            }
        }finally {
            if(leaseTime != -1){
                lockHelper.releaseLock(lockName, lockKey);
            }
        }
        return null;
    }
}
