/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.support.redis.redisson;

import lombok.NoArgsConstructor;
import org.redisson.api.*;
import org.redisson.client.codec.Codec;
import org.redisson.config.Config;

/**
 *
 * @author shanhuiming
 *
 */
@NoArgsConstructor
public class CheckedRedissonRxClient implements RedissonRxClient {

    private RedissonRxClient redissonRxClient;

    public CheckedRedissonRxClient(RedissonClient redissonClient){
        if(redissonClient != null){
            redissonRxClient = redissonClient.rxJava();
        }
    }

    /**
     * Returns time-series instance by <code>name</code>
     *
     * @param name - name of instance
     * @return RTimeSeries object
     */
    @Override
    public <V> RTimeSeriesRx<V> getTimeSeries(String name) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getTimeSeries(name);
    }

    /**
     * Returns time-series instance by <code>name</code>
     * using provided <code>codec</code> for values.
     *
     * @param name  - name of instance
     * @param codec - codec for values
     * @return RTimeSeries object
     */
    @Override
    public <V> RTimeSeriesRx<V> getTimeSeries(String name, Codec codec) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getTimeSeries(name, codec);
    }

    /**
     * Returns stream instance by <code>name</code>
     * <p>
     * Requires <b>Redis 5.0.0 and higher.</b>
     *
     * @param name of stream
     * @return RStream object
     */
    @Override
    public <K, V> RStreamRx<K, V> getStream(String name) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getStream(name);
    }

    /**
     * Returns stream instance by <code>name</code>
     * using provided <code>codec</code> for entries.
     * <p>
     * Requires <b>Redis 5.0.0 and higher.</b>
     *
     * @param name  - name of stream
     * @param codec - codec for entry
     * @return RStream object
     */
    @Override
    public <K, V> RStreamRx<K, V> getStream(String name, Codec codec) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getStream(name, codec);
    }

    /**
     * Returns geospatial items holder instance by <code>name</code>.
     *
     * @param name - name of object
     * @return Geo object
     */
    @Override
    public <V> RGeoRx<V> getGeo(String name) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getGeo(name);
    }

    /**
     * Returns geospatial items holder instance by <code>name</code>
     * using provided codec for geospatial members.
     *
     * @param name  - name of object
     * @param codec - codec for value
     * @return Geo object
     */
    @Override
    public <V> RGeoRx<V> getGeo(String name, Codec codec) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getGeo(name, codec);
    }

    /**
     * Returns rate limiter instance by <code>name</code>
     *
     * @param name of rate limiter
     * @return RateLimiter object
     */
    @Override
    public RRateLimiterRx getRateLimiter(String name) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getRateLimiter(name);
    }

    /**
     * Returns binary stream holder instance by <code>name</code>
     *
     * @param name of binary stream
     * @return BinaryStream object
     */
    @Override
    public RBinaryStreamRx getBinaryStream(String name) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getBinaryStream(name);
    }

    /**
     * Returns semaphore instance by name
     *
     * @param name - name of object
     * @return Semaphore object
     */
    @Override
    public RSemaphoreRx getSemaphore(String name) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getSemaphore(name);
    }

    /**
     * Returns semaphore instance by name.
     * Supports lease time parameter for each acquired permit.
     *
     * @param name - name of object
     * @return PermitExpirableSemaphore object
     */
    @Override
    public RPermitExpirableSemaphoreRx getPermitExpirableSemaphore(String name) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getPermitExpirableSemaphore(name);
    }

    /**
     * Returns ReadWriteLock instance by name.
     * <p>
     * To increase reliability during failover, all operations wait for propagation to all Redis slaves.
     *
     * @param name - name of object
     * @return Lock object
     */
    @Override
    public RReadWriteLockRx getReadWriteLock(String name) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getReadWriteLock(name);
    }

    /**
     * Returns Lock instance by name.
     * <p>
     * Implements a <b>fair</b> locking so it guarantees an acquire order by threads.
     * <p>
     * To increase reliability during failover, all operations wait for propagation to all Redis slaves.
     *
     * @param name - name of object
     * @return Lock object
     */
    @Override
    public RLockRx getFairLock(String name) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getFairLock(name);
    }

    /**
     * Returns Lock instance by name.
     * <p>
     * Implements a <b>non-fair</b> locking so doesn't guarantees an acquire order by threads.
     * <p>
     * To increase reliability during failover, all operations wait for propagation to all Redis slaves.
     *
     * @param name - name of object
     * @return Lock object
     */
    @Override
    public RLockRx getLock(String name) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getLock(name);
    }

    /**
     * Returns Spin lock instance by name.
     * <p>
     * Implements a <b>non-fair</b> locking so doesn't guarantees an acquire order by threads.
     * <p>
     * Lock doesn't use a pub/sub mechanism
     *
     * @param name - name of object
     * @return Lock object
     */
    @Override
    public RLockRx getSpinLock(String name) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getSpinLock(name);
    }

    /**
     * Returns Spin lock instance by name with specified back off options.
     * <p>
     * Implements a <b>non-fair</b> locking so doesn't guarantees an acquire order by threads.
     * <p>
     * Lock doesn't use a pub/sub mechanism
     *
     * @param name    - name of object
     * @param backOff
     * @return Lock object
     */
    @Override
    public RLockRx getSpinLock(String name, LockOptions.BackOff backOff) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getSpinLock(name, backOff);
    }

    /**
     * Returns MultiLock instance associated with specified <code>locks</code>
     *
     * @param locks - collection of locks
     * @return MultiLock object
     */
    @Override
    public RLockRx getMultiLock(RLock... locks) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getMultiLock(locks);
    }

    @Override
    public RLockRx getRedLock(RLock... locks) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getRedLock(locks);
    }

    /**
     * Returns CountDownLatch instance by name.
     *
     * @param name - name of object
     * @return CountDownLatch object
     */
    @Override
    public RCountDownLatchRx getCountDownLatch(String name) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getCountDownLatch(name);
    }

    /**
     * Returns set-based cache instance by <code>name</code>.
     * Supports value eviction with a given TTL value.
     *
     * <p>If eviction is not required then it's better to use regular map {@link #getSet(String, Codec)}.</p>
     *
     * @param name - name of object
     * @return SetCache object
     */
    @Override
    public <V> RSetCacheRx<V> getSetCache(String name) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getSetCache(name);
    }

    /**
     * Returns set-based cache instance by <code>name</code>.
     * Supports value eviction with a given TTL value.
     *
     * <p>If eviction is not required then it's better to use regular map {@link #getSet(String, Codec)}.</p>
     *
     * @param name  - name of object
     * @param codec - codec for values
     * @return SetCache object
     */
    @Override
    public <V> RSetCacheRx<V> getSetCache(String name, Codec codec) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getSetCache(name, codec);
    }

    /**
     * Returns map-based cache instance by name
     * using provided codec for both cache keys and values.
     * Supports entry eviction with a given MaxIdleTime and TTL settings.
     * <p>
     * If eviction is not required then it's better to use regular map {@link #getMap(String, Codec)}.
     *
     * @param name  - name of object
     * @param codec - codec for values
     * @return MapCache object
     */
    @Override
    public <K, V> RMapCacheRx<K, V> getMapCache(String name, Codec codec) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getMapCache(name, codec);
    }

    /**
     * Returns map-based cache instance by <code>name</code>
     * using provided <code>codec</code> for both cache keys and values.
     * Supports entry eviction with a given MaxIdleTime and TTL settings.
     * <p>
     * If eviction is not required then it's better to use regular map {@link #getMap(String, Codec, MapOptions)}.
     *
     * @param name    - object name
     * @param codec   - codec for keys and values
     * @param options - map options
     * @return MapCache object
     */
    @Override
    public <K, V> RMapCacheRx<K, V> getMapCache(String name, Codec codec, MapOptions<K, V> options) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getMapCache(name, codec, options);
    }

    /**
     * Returns map-based cache instance by name.
     * Supports entry eviction with a given MaxIdleTime and TTL settings.
     * <p>
     * If eviction is not required then it's better to use regular map {@link #getMap(String)}.
     *
     * @param name - name of object
     * @return MapCache object
     */
    @Override
    public <K, V> RMapCacheRx<K, V> getMapCache(String name) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getMapCache(name);
    }

    /**
     * Returns map-based cache instance by name.
     * Supports entry eviction with a given MaxIdleTime and TTL settings.
     * <p>
     * If eviction is not required then it's better to use regular map {@link #getMap(String, MapOptions)}.</p>
     *
     * @param name    - name of object
     * @param options - map options
     * @return MapCache object
     */
    @Override
    public <K, V> RMapCacheRx<K, V> getMapCache(String name, MapOptions<K, V> options) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getMapCache(name, options);
    }

    /**
     * Returns object holder instance by name
     *
     * @param name - name of object
     * @return Bucket object
     */
    @Override
    public <V> RBucketRx<V> getBucket(String name) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getBucket(name);
    }

    /**
     * Returns object holder instance by name
     * using provided codec for object.
     *
     * @param name  - name of object
     * @param codec - codec for value
     * @return Bucket object
     */
    @Override
    public <V> RBucketRx<V> getBucket(String name, Codec codec) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getBucket(name, codec);
    }

    /**
     * Returns interface for mass operations with Bucket objects.
     *
     * @return Buckets
     */
    @Override
    public RBucketsRx getBuckets() {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getBuckets();
    }

    /**
     * Returns interface for mass operations with Bucket objects
     * using provided codec for object.
     *
     * @param codec - codec for bucket objects
     * @return Buckets
     */
    @Override
    public RBucketsRx getBuckets(Codec codec) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getBuckets(codec);
    }

    /**
     * Returns HyperLogLog instance by name.
     *
     * @param name - name of object
     * @return HyperLogLog object
     */
    @Override
    public <V> RHyperLogLogRx<V> getHyperLogLog(String name) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getHyperLogLog(name);
    }

    /**
     * Returns HyperLogLog instance by name
     * using provided codec for hll objects.
     *
     * @param name  - name of object
     * @param codec - codec of values
     * @return HyperLogLog object
     */
    @Override
    public <V> RHyperLogLogRx<V> getHyperLogLog(String name, Codec codec) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getHyperLogLog(name, codec);
    }

    /**
     * Returns id generator by name.
     *
     * @param name - name of object
     * @return IdGenerator object
     */
    @Override
    public RIdGeneratorRx getIdGenerator(String name) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getIdGenerator(name);
    }

    /**
     * Returns list instance by name.
     *
     * @param name - name of object
     * @return List object
     */
    @Override
    public <V> RListRx<V> getList(String name) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getList(name);
    }

    /**
     * Returns list instance by name
     * using provided codec for list objects.
     *
     * @param name  - name of object
     * @param codec - codec for values
     * @return List object
     */
    @Override
    public <V> RListRx<V> getList(String name, Codec codec) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getList(name, codec);
    }

    /**
     * Returns List based Multimap instance by name.
     *
     * @param name - name of object
     * @return ListMultimap object
     */
    @Override
    public <K, V> RListMultimapRx<K, V> getListMultimap(String name) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getListMultimap(name);
    }

    /**
     * Returns List based Multimap instance by name
     * using provided codec for both map keys and values.
     *
     * @param name  - name of object
     * @param codec - codec for keys and values
     * @return RListMultimapReactive object
     */
    @Override
    public <K, V> RListMultimapRx<K, V> getListMultimap(String name, Codec codec) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getListMultimap(name, codec);
    }

    /**
     * Returns List based Multimap cache instance by name.
     * Supports key eviction by specifying a time to live.
     * If eviction is not required then it's better to use regular list multimap {@link #getListMultimap(String)}.
     *
     * @param name - name of object
     * @return RListMultimapCacheRx object
     */
    @Override
    public <K, V> RListMultimapCacheRx<K, V> getListMultimapCache(String name) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getListMultimapCache(name);
    }

    /**
     * Returns List based Multimap cache instance by name using provided codec for both map keys and values.
     * Supports key eviction by specifying a time to live.
     * If eviction is not required then it's better to use regular list multimap {@link #getListMultimap(String, Codec)}.
     *
     * @param name  - name of object
     * @param codec - codec for keys and values
     * @return RListMultimapCacheRx object
     */
    @Override
    public <K, V> RListMultimapCacheRx<K, V> getListMultimapCache(String name, Codec codec) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getListMultimapCache(name, codec);
    }

    /**
     * Returns Set based Multimap instance by name.
     *
     * @param name - name of object
     * @return SetMultimap object
     */
    @Override
    public <K, V> RSetMultimapRx<K, V> getSetMultimap(String name) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getSetMultimap(name);
    }

    /**
     * Returns Set based Multimap instance by name
     * using provided codec for both map keys and values.
     *
     * @param name  - name of object
     * @param codec - codec for keys and values
     * @return SetMultimap object
     */
    @Override
    public <K, V> RSetMultimapRx<K, V> getSetMultimap(String name, Codec codec) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getSetMultimap(name, codec);
    }

    /**
     * Returns Set based Multimap cache instance by name.
     * Supports key eviction by specifying a time to live.
     * If eviction is not required then it's better to use regular set multimap {@link #getSetMultimap(String)}.
     *
     * @param name - name of object
     * @return RSetMultimapCacheRx object
     */
    @Override
    public <K, V> RSetMultimapCacheRx<K, V> getSetMultimapCache(String name) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getSetMultimapCache(name);
    }

    /**
     * Returns Set based Multimap cache instance by name using provided codec for both map keys and values.
     * Supports key eviction by specifying a time to live.
     * If eviction is not required then it's better to use regular set multimap {@link #getSetMultimap(String, Codec)}.
     *
     * @param name  - name of object
     * @param codec - codec for keys and values
     * @return RSetMultimapCacheRx object
     */
    @Override
    public <K, V> RSetMultimapCacheRx<K, V> getSetMultimapCache(String name, Codec codec) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getSetMultimapCache(name, codec);
    }

    /**
     * Returns map instance by name.
     *
     * @param name - name of object
     * @return Map object
     */
    @Override
    public <K, V> RMapRx<K, V> getMap(String name) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getMap(name);
    }

    /**
     * Returns map instance by name.
     *
     * @param name    - name of object
     * @param options - map options
     * @return Map object
     */
    @Override
    public <K, V> RMapRx<K, V> getMap(String name, MapOptions<K, V> options) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getMap(name, options);
    }

    /**
     * Returns map instance by name
     * using provided codec for both map keys and values.
     *
     * @param name  - name of object
     * @param codec - codec for keys and values
     * @return Map object
     */
    @Override
    public <K, V> RMapRx<K, V> getMap(String name, Codec codec) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getMap(name, codec);
    }

    /**
     * Returns map instance by name
     * using provided codec for both map keys and values.
     *
     * @param name    - name of object
     * @param codec   - codec for keys and values
     * @param options - map options
     * @return Map object
     */
    @Override
    public <K, V> RMapRx<K, V> getMap(String name, Codec codec, MapOptions<K, V> options) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getMap(name, codec, options);
    }

    /**
     * Returns set instance by name.
     *
     * @param name - name of object
     * @return Set object
     */
    @Override
    public <V> RSetRx<V> getSet(String name) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getSet(name);
    }

    /**
     * Returns set instance by name
     * using provided codec for set objects.
     *
     * @param name  - name of set
     * @param codec - codec for values
     * @return Set object
     */
    @Override
    public <V> RSetRx<V> getSet(String name, Codec codec) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getSet(name, codec);
    }

    /**
     * Returns Redis Sorted Set instance by name.
     * This sorted set sorts objects by object score.
     *
     * @param name of scored sorted set
     * @return ScoredSortedSet object
     */
    @Override
    public <V> RScoredSortedSetRx<V> getScoredSortedSet(String name) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getScoredSortedSet(name);
    }

    /**
     * Returns Redis Sorted Set instance by name
     * using provided codec for sorted set objects.
     * This sorted set sorts objects by object score.
     *
     * @param name  - name of scored sorted set
     * @param codec - codec for values
     * @return ScoredSortedSet object
     */
    @Override
    public <V> RScoredSortedSetRx<V> getScoredSortedSet(String name, Codec codec) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getScoredSortedSet(name, codec);
    }

    /**
     * Returns String based Redis Sorted Set instance by name
     * All elements are inserted with the same score during addition,
     * in order to force lexicographical ordering
     *
     * @param name - name of object
     * @return LexSortedSet object
     */
    @Override
    public RLexSortedSetRx getLexSortedSet(String name) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getLexSortedSet(name);
    }

    /**
     * Returns topic instance by name.
     *
     * @param name - name of object
     * @return Topic object
     */
    @Override
    public RTopicRx getTopic(String name) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getTopic(name);
    }

    /**
     * Returns topic instance by name
     * using provided codec for messages.
     *
     * @param name  - name of object
     * @param codec - codec for message
     * @return Topic object
     */
    @Override
    public RTopicRx getTopic(String name, Codec codec) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getTopic(name, codec);
    }

    /**
     * Returns reliable topic instance by name.
     * <p>
     * Dedicated Redis connection is allocated per instance (subscriber) of this object.
     * Messages are delivered to all listeners attached to the same Redis setup.
     * <p>
     * Requires <b>Redis 5.0.0 and higher.</b>
     *
     * @param name - name of object
     * @return ReliableTopic object
     */
    @Override
    public RReliableTopicRx getReliableTopic(String name) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getReliableTopic(name);
    }

    /**
     * Returns reliable topic instance by name
     * using provided codec for messages.
     * <p>
     * Dedicated Redis connection is allocated per instance (subscriber) of this object.
     * Messages are delivered to all listeners attached to the same Redis setup.
     * <p>
     * Requires <b>Redis 5.0.0 and higher.</b>
     *
     * @param name  - name of object
     * @param codec - codec for message
     * @return ReliableTopic object
     */
    @Override
    public RReliableTopicRx getReliableTopic(String name, Codec codec) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getReliableTopic(name, codec);
    }

    /**
     * Returns topic instance satisfies by pattern name.
     * <p>
     * Supported glob-style patterns:
     * h?llo subscribes to hello, hallo and hxllo
     * h*llo subscribes to hllo and heeeello
     * h[ae]llo subscribes to hello and hallo, but not hillo
     *
     * @param pattern of the topic
     * @return PatternTopic object
     */
    @Override
    public RPatternTopicRx getPatternTopic(String pattern) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getPatternTopic(pattern);
    }

    /**
     * Returns topic instance satisfies by pattern name
     * using provided codec for messages.
     * <p>
     * Supported glob-style patterns:
     * h?llo subscribes to hello, hallo and hxllo
     * h*llo subscribes to hllo and heeeello
     * h[ae]llo subscribes to hello and hallo, but not hillo
     *
     * @param pattern of the topic
     * @param codec   - codec for message
     * @return PatternTopic object
     */
    @Override
    public RPatternTopicRx getPatternTopic(String pattern, Codec codec) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getPatternTopic(pattern, codec);
    }

    /**
     * Returns queue instance by name.
     *
     * @param name - name of object
     * @return Queue object
     */
    @Override
    public <V> RQueueRx<V> getQueue(String name) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getQueue(name);
    }

    /**
     * Returns queue instance by name
     * using provided codec for queue objects.
     *
     * @param name  - name of object
     * @param codec - codec for values
     * @return Queue object
     */
    @Override
    public <V> RQueueRx<V> getQueue(String name, Codec codec) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getQueue(name, codec);
    }

    /**
     * Returns RingBuffer based queue.
     *
     * @param name - name of object
     * @return RingBuffer object
     */
    @Override
    public <V> RRingBufferRx<V> getRingBuffer(String name) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getRingBuffer(name);
    }

    /**
     * Returns RingBuffer based queue.
     *
     * @param name  - name of object
     * @param codec - codec for values
     * @return RingBuffer object
     */
    @Override
    public <V> RRingBufferRx<V> getRingBuffer(String name, Codec codec) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getRingBuffer(name, codec);
    }

    /**
     * Returns blocking queue instance by name.
     *
     * @param name - name of object
     * @return BlockingQueue object
     */
    @Override
    public <V> RBlockingQueueRx<V> getBlockingQueue(String name) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getBlockingQueue(name);
    }

    /**
     * Returns blocking queue instance by name
     * using provided codec for queue objects.
     *
     * @param name  - name of object
     * @param codec - code for values
     * @return BlockingQueue object
     */
    @Override
    public <V> RBlockingQueueRx<V> getBlockingQueue(String name, Codec codec) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getBlockingQueue(name, codec);
    }

    /**
     * Returns unbounded blocking deque instance by name.
     *
     * @param name - name of object
     * @return BlockingDeque object
     */
    @Override
    public <V> RBlockingDequeRx<V> getBlockingDeque(String name) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getBlockingDeque(name);
    }

    /**
     * Returns unbounded blocking deque instance by name
     * using provided codec for deque objects.
     *
     * @param name  - name of object
     * @param codec - deque objects codec
     * @return BlockingDeque object
     */
    @Override
    public <V> RBlockingDequeRx<V> getBlockingDeque(String name, Codec codec) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getBlockingDeque(name, codec);
    }

    /**
     * Returns transfer queue instance by name.
     *
     * @param name - name of object
     * @return TransferQueue object
     */
    @Override
    public <V> RTransferQueueRx<V> getTransferQueue(String name) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getTransferQueue(name);
    }

    /**
     * Returns transfer queue instance by name
     * using provided codec for queue objects.
     *
     * @param name  - name of object
     * @param codec - code for values
     * @return TransferQueue object
     */
    @Override
    public <V> RTransferQueueRx<V> getTransferQueue(String name, Codec codec) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getTransferQueue(name, codec);
    }

    /**
     * Returns deque instance by name.
     *
     * @param name - name of object
     * @return Deque object
     */
    @Override
    public <V> RDequeRx<V> getDeque(String name) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getDeque(name);
    }

    /**
     * Returns deque instance by name
     * using provided codec for deque objects.
     *
     * @param name  - name of object
     * @param codec - coded for values
     * @return Deque object
     */
    @Override
    public <V> RDequeRx<V> getDeque(String name, Codec codec) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getDeque(name, codec);
    }

    /**
     * Returns "atomic long" instance by name.
     *
     * @param name of the "atomic long"
     * @return AtomicLong object
     */
    @Override
    public RAtomicLongRx getAtomicLong(String name) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getAtomicLong(name);
    }

    /**
     * Returns "atomic double" instance by name.
     *
     * @param name of the "atomic double"
     * @return AtomicLong object
     */
    @Override
    public RAtomicDoubleRx getAtomicDouble(String name) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getAtomicDouble(name);
    }

    /**
     * Returns object for remote operations prefixed with the default name (redisson_remote_service)
     *
     * @return RemoteService object
     */
    @Override
    public RRemoteService getRemoteService() {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getRemoteService();
    }

    /**
     * Returns object for remote operations prefixed with the default name (redisson_remote_service)
     * and uses provided codec for method arguments and result.
     *
     * @param codec - codec for response and request
     * @return RemoteService object
     */
    @Override
    public RRemoteService getRemoteService(Codec codec) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getRemoteService(codec);
    }

    /**
     * Returns object for remote operations prefixed with the specified name
     *
     * @param name - the name used as the Redis key prefix for the services
     * @return RemoteService object
     */
    @Override
    public RRemoteService getRemoteService(String name) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getRemoteService(name);
    }

    /**
     * Returns object for remote operations prefixed with the specified name
     * and uses provided codec for method arguments and result.
     *
     * @param name  - the name used as the Redis key prefix for the services
     * @param codec - codec for response and request
     * @return RemoteService object
     */
    @Override
    public RRemoteService getRemoteService(String name, Codec codec) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getRemoteService(name, codec);
    }

    /**
     * Returns bitSet instance by name.
     *
     * @param name - name of object
     * @return BitSet object
     */
    @Override
    public RBitSetRx getBitSet(String name) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getBitSet(name);
    }

    /**
     * Returns script operations object
     *
     * @return Script object
     */
    @Override
    public RScriptRx getScript() {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getScript();
    }

    /**
     * Returns script operations object using provided codec.
     *
     * @param codec - codec for params and result
     * @return Script object
     */
    @Override
    public RScriptRx getScript(Codec codec) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getScript(codec);
    }

    /**
     * Creates transaction with <b>READ_COMMITTED</b> isolation level.
     *
     * @param options - transaction configuration
     * @return Transaction object
     */
    @Override
    public RTransactionRx createTransaction(TransactionOptions options) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.createTransaction(options);
    }

    /**
     * Return batch object which executes group of
     * command in pipeline.
     * <p>
     * See <a href="http://redis.io/topics/pipelining">http://redis.io/topics/pipelining</a>
     *
     * @param options - batch configuration
     * @return Batch object
     */
    @Override
    public RBatchRx createBatch(BatchOptions options) {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.createBatch(options);
    }

    /**
     * Return batch object which executes group of
     * command in pipeline.
     * <p>
     * See <a href="http://redis.io/topics/pipelining">http://redis.io/topics/pipelining</a>
     *
     * @return Batch object
     */
    @Override
    public RBatchRx createBatch() {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.createBatch();
    }

    /**
     * Returns keys operations.
     * Each of Redis/Redisson object associated with own key
     *
     * @return Keys object
     */
    @Override
    public RKeysRx getKeys() {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getKeys();
    }

    /**
     * Shuts down Redisson instance <b>NOT</b> Redis server
     */
    @Override
    public void shutdown() {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        redissonRxClient.shutdown();
    }

    /**
     * Allows to get configuration provided
     * during Redisson instance creation. Further changes on
     * this object not affect Redisson instance.
     *
     * @return Config object
     */
    @Override
    public Config getConfig() {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getConfig();
    }

    /**
     * Get Redis nodes group for server operations
     *
     * @return NodesGroup object
     */
    @Override
    public NodesGroup<Node> getNodesGroup() {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getNodesGroup();
    }

    /**
     * Get Redis cluster nodes group for server operations
     *
     * @return NodesGroup object
     */
    @Override
    public NodesGroup<ClusterNode> getClusterNodesGroup() {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getClusterNodesGroup();
    }

    /**
     * Returns {@code true} if this Redisson instance has been shut down.
     *
     * @return <code>true</code> if this Redisson instance has been shut down otherwise <code>false</code>
     */
    @Override
    public boolean isShutdown() {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.isShutdown();
    }

    /**
     * Returns {@code true} if this Redisson instance was started to be shutdown
     * or was shutdown {@link #isShutdown()} already.
     *
     * @return <code>true</code> if this Redisson instance was started to be shutdown
     * or was shutdown {@link #isShutdown()} already otherwise <code>false</code>
     */
    @Override
    public boolean isShuttingDown() {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.isShuttingDown();
    }

    /**
     * Returns id of this Redisson instance
     *
     * @return id
     */
    @Override
    public String getId() {
        if(redissonRxClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonRxClient.getId();
    }
}
