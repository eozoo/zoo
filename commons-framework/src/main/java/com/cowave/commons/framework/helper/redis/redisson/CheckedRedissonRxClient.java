/*
 * Copyright (c) 2017～2024 Cowave All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.commons.framework.helper.redis.redisson;

import lombok.NoArgsConstructor;
import org.redisson.api.*;
import org.redisson.client.codec.Codec;
import org.redisson.config.Config;

import java.util.Optional;

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
        return Optional.ofNullable(redissonRxClient).map(client -> client.<V>getTimeSeries(name))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.<V>getTimeSeries(name, codec))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.<K, V>getStream(name))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.<K, V>getStream(name, codec))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
    }

    /**
     * Returns geospatial items holder instance by <code>name</code>.
     *
     * @param name - name of object
     * @return Geo object
     */
    @Override
    public <V> RGeoRx<V> getGeo(String name) {
        return Optional.ofNullable(redissonRxClient).map(client -> client.<V>getGeo(name))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.<V>getGeo(name, codec))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
    }

    /**
     * Returns rate limiter instance by <code>name</code>
     *
     * @param name of rate limiter
     * @return RateLimiter object
     */
    @Override
    public RRateLimiterRx getRateLimiter(String name) {
        return Optional.ofNullable(redissonRxClient).map(client -> client.getRateLimiter(name))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
    }

    /**
     * Returns binary stream holder instance by <code>name</code>
     *
     * @param name of binary stream
     * @return BinaryStream object
     */
    @Override
    public RBinaryStreamRx getBinaryStream(String name) {
        return Optional.ofNullable(redissonRxClient).map(client -> client.getBinaryStream(name))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
    }

    /**
     * Returns semaphore instance by name
     *
     * @param name - name of object
     * @return Semaphore object
     */
    @Override
    public RSemaphoreRx getSemaphore(String name) {
        return Optional.ofNullable(redissonRxClient).map(client -> client.getSemaphore(name))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.getPermitExpirableSemaphore(name))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.getReadWriteLock(name))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.getFairLock(name))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.getLock(name))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.getSpinLock(name))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.getSpinLock(name, backOff))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
    }

    /**
     * Returns MultiLock instance associated with specified <code>locks</code>
     *
     * @param locks - collection of locks
     * @return MultiLock object
     */
    @Override
    public RLockRx getMultiLock(RLock... locks) {
        return Optional.ofNullable(redissonRxClient).map(client -> client.getMultiLock(locks))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
    }

    @Override
    public RLockRx getRedLock(RLock... locks) {
        return Optional.ofNullable(redissonRxClient).map(client -> client.getRedLock(locks))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
    }

    /**
     * Returns CountDownLatch instance by name.
     *
     * @param name - name of object
     * @return CountDownLatch object
     */
    @Override
    public RCountDownLatchRx getCountDownLatch(String name) {
        return Optional.ofNullable(redissonRxClient).map(client -> client.getCountDownLatch(name))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.<V>getSetCache(name))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.<V>getSetCache(name, codec))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.<K, V>getMapCache(name, codec))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.getMapCache(name, codec, options))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.<K, V>getMapCache(name))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.getMapCache(name, options))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
    }

    /**
     * Returns object holder instance by name
     *
     * @param name - name of object
     * @return Bucket object
     */
    @Override
    public <V> RBucketRx<V> getBucket(String name) {
        return Optional.ofNullable(redissonRxClient).map(client -> client.<V>getBucket(name))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.<V>getBucket(name, codec))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
    }

    /**
     * Returns interface for mass operations with Bucket objects.
     *
     * @return Buckets
     */
    @Override
    public RBucketsRx getBuckets() {
        return Optional.ofNullable(redissonRxClient).map(client -> client.getBuckets())
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.getBuckets(codec))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
    }

    /**
     * Returns HyperLogLog instance by name.
     *
     * @param name - name of object
     * @return HyperLogLog object
     */
    @Override
    public <V> RHyperLogLogRx<V> getHyperLogLog(String name) {
        return Optional.ofNullable(redissonRxClient).map(client -> client.<V>getHyperLogLog(name))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.<V>getHyperLogLog(name, codec))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
    }

    /**
     * Returns id generator by name.
     *
     * @param name - name of object
     * @return IdGenerator object
     */
    @Override
    public RIdGeneratorRx getIdGenerator(String name) {
        return Optional.ofNullable(redissonRxClient).map(client -> client.getIdGenerator(name))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
    }

    /**
     * Returns list instance by name.
     *
     * @param name - name of object
     * @return List object
     */
    @Override
    public <V> RListRx<V> getList(String name) {
        return Optional.ofNullable(redissonRxClient).map(client -> client.<V>getList(name))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.<V>getList(name, codec))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
    }

    /**
     * Returns List based Multimap instance by name.
     *
     * @param name - name of object
     * @return ListMultimap object
     */
    @Override
    public <K, V> RListMultimapRx<K, V> getListMultimap(String name) {
        return Optional.ofNullable(redissonRxClient).map(client -> client.<K, V>getListMultimap(name))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.<K, V>getListMultimap(name, codec))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.<K, V>getListMultimapCache(name))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.<K, V>getListMultimapCache(name, codec))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
    }

    /**
     * Returns Set based Multimap instance by name.
     *
     * @param name - name of object
     * @return SetMultimap object
     */
    @Override
    public <K, V> RSetMultimapRx<K, V> getSetMultimap(String name) {
        return Optional.ofNullable(redissonRxClient).map(client -> client.<K, V>getSetMultimap(name))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.<K, V>getSetMultimap(name, codec))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.<K, V>getSetMultimapCache(name))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.<K, V>getSetMultimapCache(name, codec))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
    }

    /**
     * Returns map instance by name.
     *
     * @param name - name of object
     * @return Map object
     */
    @Override
    public <K, V> RMapRx<K, V> getMap(String name) {
        return Optional.ofNullable(redissonRxClient).map(client -> client.<K, V>getMap(name))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.getMap(name, options))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.<K, V>getMap(name, codec))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.getMap(name, codec, options))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
    }

    /**
     * Returns set instance by name.
     *
     * @param name - name of object
     * @return Set object
     */
    @Override
    public <V> RSetRx<V> getSet(String name) {
        return Optional.ofNullable(redissonRxClient).map(client -> client.<V>getSet(name))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.<V>getSet(name, codec))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.<V>getScoredSortedSet(name))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.<V>getScoredSortedSet(name, codec))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.getLexSortedSet(name))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
    }

    /**
     * Returns topic instance by name.
     *
     * @param name - name of object
     * @return Topic object
     */
    @Override
    public RTopicRx getTopic(String name) {
        return Optional.ofNullable(redissonRxClient).map(client -> client.getTopic(name))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.getTopic(name, codec))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.getReliableTopic(name))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.getReliableTopic(name, codec))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.getPatternTopic(pattern))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.getPatternTopic(pattern, codec))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
    }

    /**
     * Returns queue instance by name.
     *
     * @param name - name of object
     * @return Queue object
     */
    @Override
    public <V> RQueueRx<V> getQueue(String name) {
        return Optional.ofNullable(redissonRxClient).map(client -> client.<V>getQueue(name))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.<V>getQueue(name, codec))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
    }

    /**
     * Returns RingBuffer based queue.
     *
     * @param name - name of object
     * @return RingBuffer object
     */
    @Override
    public <V> RRingBufferRx<V> getRingBuffer(String name) {
        return Optional.ofNullable(redissonRxClient).map(client -> client.<V>getRingBuffer(name))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.<V>getRingBuffer(name, codec))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
    }

    /**
     * Returns blocking queue instance by name.
     *
     * @param name - name of object
     * @return BlockingQueue object
     */
    @Override
    public <V> RBlockingQueueRx<V> getBlockingQueue(String name) {
        return Optional.ofNullable(redissonRxClient).map(client -> client.<V>getBlockingQueue(name))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.<V>getBlockingQueue(name, codec))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
    }

    /**
     * Returns unbounded blocking deque instance by name.
     *
     * @param name - name of object
     * @return BlockingDeque object
     */
    @Override
    public <V> RBlockingDequeRx<V> getBlockingDeque(String name) {
        return Optional.ofNullable(redissonRxClient).map(client -> client.<V>getBlockingDeque(name))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.<V>getBlockingDeque(name, codec))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
    }

    /**
     * Returns transfer queue instance by name.
     *
     * @param name - name of object
     * @return TransferQueue object
     */
    @Override
    public <V> RTransferQueueRx<V> getTransferQueue(String name) {
        return Optional.ofNullable(redissonRxClient).map(client -> client.<V>getTransferQueue(name))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.<V>getTransferQueue(name, codec))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
    }

    /**
     * Returns deque instance by name.
     *
     * @param name - name of object
     * @return Deque object
     */
    @Override
    public <V> RDequeRx<V> getDeque(String name) {
        return Optional.ofNullable(redissonRxClient).map(client -> client.<V>getDeque(name))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.<V>getDeque(name, codec))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
    }

    /**
     * Returns "atomic long" instance by name.
     *
     * @param name of the "atomic long"
     * @return AtomicLong object
     */
    @Override
    public RAtomicLongRx getAtomicLong(String name) {
        return Optional.ofNullable(redissonRxClient).map(client -> client.getAtomicLong(name))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
    }

    /**
     * Returns "atomic double" instance by name.
     *
     * @param name of the "atomic double"
     * @return AtomicLong object
     */
    @Override
    public RAtomicDoubleRx getAtomicDouble(String name) {
        return Optional.ofNullable(redissonRxClient).map(client -> client.getAtomicDouble(name))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
    }

    /**
     * Returns object for remote operations prefixed with the default name (redisson_remote_service)
     *
     * @return RemoteService object
     */
    @Override
    public RRemoteService getRemoteService() {
        return Optional.ofNullable(redissonRxClient).map(RedissonRxClient::getRemoteService)
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.getRemoteService(codec))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
    }

    /**
     * Returns object for remote operations prefixed with the specified name
     *
     * @param name - the name used as the Redis key prefix for the services
     * @return RemoteService object
     */
    @Override
    public RRemoteService getRemoteService(String name) {
        return Optional.ofNullable(redissonRxClient).map(client -> client.getRemoteService(name))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.getRemoteService(name, codec))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
    }

    /**
     * Returns bitSet instance by name.
     *
     * @param name - name of object
     * @return BitSet object
     */
    @Override
    public RBitSetRx getBitSet(String name) {
        return Optional.ofNullable(redissonRxClient).map(client -> client.getBitSet(name))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
    }

    /**
     * Returns script operations object
     *
     * @return Script object
     */
    @Override
    public RScriptRx getScript() {
        return Optional.ofNullable(redissonRxClient).map(RedissonRxClient::getScript)
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
    }

    /**
     * Returns script operations object using provided codec.
     *
     * @param codec - codec for params and result
     * @return Script object
     */
    @Override
    public RScriptRx getScript(Codec codec) {
        return Optional.ofNullable(redissonRxClient).map(client -> client.getScript(codec))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
    }

    /**
     * Creates transaction with <b>READ_COMMITTED</b> isolation level.
     *
     * @param options - transaction configuration
     * @return Transaction object
     */
    @Override
    public RTransactionRx createTransaction(TransactionOptions options) {
        return Optional.ofNullable(redissonRxClient).map(client -> client.createTransaction(options))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(client -> client.createBatch(options))
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(RedissonRxClient::createBatch)
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
    }

    /**
     * Returns keys operations.
     * Each of Redis/Redisson object associated with own key
     *
     * @return Keys object
     */
    @Override
    public RKeysRx getKeys() {
        return Optional.ofNullable(redissonRxClient).map(RedissonRxClient::getKeys)
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
    }

    /**
     * Shuts down Redisson instance <b>NOT</b> Redis server
     */
    @Override
    public void shutdown() {
        Optional.ofNullable(redissonRxClient).ifPresentOrElse(
                RedissonRxClient::shutdown, () -> { throw new UnsupportedOperationException("redisson not connected"); });
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
        return Optional.ofNullable(redissonRxClient).map(RedissonRxClient::getConfig)
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
    }

    /**
     * Get Redis nodes group for server operations
     *
     * @return NodesGroup object
     */
    @Override
    public NodesGroup<Node> getNodesGroup() {
        return Optional.ofNullable(redissonRxClient).map(RedissonRxClient::getNodesGroup)
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
    }

    /**
     * Get Redis cluster nodes group for server operations
     *
     * @return NodesGroup object
     */
    @Override
    public NodesGroup<ClusterNode> getClusterNodesGroup() {
        return Optional.ofNullable(redissonRxClient).map(RedissonRxClient::getClusterNodesGroup)
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
    }

    /**
     * Returns {@code true} if this Redisson instance has been shut down.
     *
     * @return <code>true</code> if this Redisson instance has been shut down otherwise <code>false</code>
     */
    @Override
    public boolean isShutdown() {
        return Optional.ofNullable(redissonRxClient).map(RedissonRxClient::isShutdown)
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
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
        return Optional.ofNullable(redissonRxClient).map(RedissonRxClient::isShuttingDown)
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
    }

    /**
     * Returns id of this Redisson instance
     *
     * @return id
     */
    @Override
    public String getId() {
        return Optional.ofNullable(redissonRxClient).map(RedissonRxClient::getId)
            .orElseThrow(() -> new UnsupportedOperationException("redisson not connected"));
    }
}
