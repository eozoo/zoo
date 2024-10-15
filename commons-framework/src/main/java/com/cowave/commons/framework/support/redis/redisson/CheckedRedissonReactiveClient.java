/*
 * Copyright (c) 2017～2024 Cowave All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.commons.framework.support.redis.redisson;

import lombok.NoArgsConstructor;
import org.redisson.api.*;
import org.redisson.client.codec.Codec;
import org.redisson.config.Config;

import java.util.List;

/**
 *
 * @author shanhuiming
 *
 */
@NoArgsConstructor
public class CheckedRedissonReactiveClient implements RedissonReactiveClient {

    private RedissonReactiveClient redissonReactiveClient;

    public CheckedRedissonReactiveClient(RedissonClient redissonClient){
        if(redissonClient != null){
            redissonReactiveClient = redissonClient.reactive();
        }
    }

    /**
     * Returns time-series instance by <code>name</code>
     *
     * @param name - name of instance
     * @return RTimeSeries object
     */
    @Override
    public <V> RTimeSeriesReactive<V> getTimeSeries(String name) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getTimeSeries(name);
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
    public <V> RTimeSeriesReactive<V> getTimeSeries(String name, Codec codec) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getTimeSeries(name, codec);
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
    public <K, V> RStreamReactive<K, V> getStream(String name) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getStream(name);
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
    public <K, V> RStreamReactive<K, V> getStream(String name, Codec codec) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getStream(name, codec);
    }

    /**
     * Returns geospatial items holder instance by <code>name</code>.
     *
     * @param name - name of object
     * @return Geo object
     */
    @Override
    public <V> RGeoReactive<V> getGeo(String name) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getGeo(name);
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
    public <V> RGeoReactive<V> getGeo(String name, Codec codec) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getGeo(name, codec);
    }

    /**
     * Returns rate limiter instance by <code>name</code>
     *
     * @param name of rate limiter
     * @return RateLimiter object
     */
    @Override
    public RRateLimiterReactive getRateLimiter(String name) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getRateLimiter(name);
    }

    /**
     * Returns binary stream holder instance by <code>name</code>
     *
     * @param name of binary stream
     * @return BinaryStream object
     */
    @Override
    public RBinaryStreamReactive getBinaryStream(String name) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getBinaryStream(name);
    }

    /**
     * Returns semaphore instance by name
     *
     * @param name - name of object
     * @return Semaphore object
     */
    @Override
    public RSemaphoreReactive getSemaphore(String name) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getSemaphore(name);
    }

    /**
     * Returns semaphore instance by name.
     * Supports lease time parameter for each acquired permit.
     *
     * @param name - name of object
     * @return PermitExpirableSemaphore object
     */
    @Override
    public RPermitExpirableSemaphoreReactive getPermitExpirableSemaphore(String name) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getPermitExpirableSemaphore(name);
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
    public RReadWriteLockReactive getReadWriteLock(String name) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getReadWriteLock(name);
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
    public RLockReactive getFairLock(String name) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getFairLock(name);
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
    public RLockReactive getLock(String name) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getLock(name);
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
    public RLockReactive getSpinLock(String name) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getSpinLock(name);
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
    public RLockReactive getSpinLock(String name, LockOptions.BackOff backOff) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getSpinLock(name, backOff);
    }

    /**
     * Returns MultiLock instance associated with specified <code>locks</code>
     *
     * @param locks - collection of locks
     * @return MultiLock object
     */
    @Override
    public RLockReactive getMultiLock(RLock... locks) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getMultiLock(locks);
    }

    @Override
    public RLockReactive getRedLock(RLock... locks) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getRedLock(locks);
    }

    /**
     * Returns CountDownLatch instance by name.
     *
     * @param name - name of object
     * @return CountDownLatch object
     */
    @Override
    public RCountDownLatchReactive getCountDownLatch(String name) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getCountDownLatch(name);
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
    public <V> RSetCacheReactive<V> getSetCache(String name) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getSetCache(name);
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
    public <V> RSetCacheReactive<V> getSetCache(String name, Codec codec) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getSetCache(name, codec);
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
    public <K, V> RMapCacheReactive<K, V> getMapCache(String name, Codec codec) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getMapCache(name, codec);
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
    public <K, V> RMapCacheReactive<K, V> getMapCache(String name, Codec codec, MapOptions<K, V> options) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getMapCache(name, codec, options);
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
    public <K, V> RMapCacheReactive<K, V> getMapCache(String name) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getMapCache(name);
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
    public <K, V> RMapCacheReactive<K, V> getMapCache(String name, MapOptions<K, V> options) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getMapCache(name, options);
    }

    /**
     * Returns object holder instance by name
     *
     * @param name - name of object
     * @return Bucket object
     */
    @Override
    public <V> RBucketReactive<V> getBucket(String name) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getBucket(name);
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
    public <V> RBucketReactive<V> getBucket(String name, Codec codec) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getBucket(name, codec);
    }

    /**
     * Returns interface for mass operations with Bucket objects.
     *
     * @return Buckets
     */
    @Override
    public RBucketsReactive getBuckets() {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getBuckets();
    }

    /**
     * Returns interface for mass operations with Bucket objects
     * using provided codec for object.
     *
     * @param codec - codec for bucket objects
     * @return Buckets
     */
    @Override
    public RBucketsReactive getBuckets(Codec codec) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getBuckets(codec);
    }

    /**
     * Returns a list of object holder instances by a key pattern
     *
     * @param pattern - pattern for name of buckets
     * @return list of buckets
     */
    @Override
    public <V> List<RBucketReactive<V>> findBuckets(String pattern) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.findBuckets(pattern);
    }

    /**
     * Returns HyperLogLog instance by name.
     *
     * @param name - name of object
     * @return HyperLogLog object
     */
    @Override
    public <V> RHyperLogLogReactive<V> getHyperLogLog(String name) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getHyperLogLog(name);
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
    public <V> RHyperLogLogReactive<V> getHyperLogLog(String name, Codec codec) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getHyperLogLog(name, codec);
    }

    /**
     * Returns id generator by name.
     *
     * @param name - name of object
     * @return IdGenerator object
     */
    @Override
    public RIdGeneratorReactive getIdGenerator(String name) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getIdGenerator(name);
    }

    /**
     * Returns list instance by name.
     *
     * @param name - name of object
     * @return List object
     */
    @Override
    public <V> RListReactive<V> getList(String name) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getList(name);
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
    public <V> RListReactive<V> getList(String name, Codec codec) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getList(name, codec);
    }

    /**
     * Returns List based Multimap instance by name.
     *
     * @param name - name of object
     * @return ListMultimap object
     */
    @Override
    public <K, V> RListMultimapReactive<K, V> getListMultimap(String name) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getListMultimap(name);
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
    public <K, V> RListMultimapReactive<K, V> getListMultimap(String name, Codec codec) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getListMultimap(name, codec);
    }

    /**
     * Returns Set based Multimap instance by name.
     *
     * @param name - name of object
     * @return SetMultimap object
     */
    @Override
    public <K, V> RSetMultimapReactive<K, V> getSetMultimap(String name) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getSetMultimap(name);
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
    public <K, V> RSetMultimapReactive<K, V> getSetMultimap(String name, Codec codec) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getSetMultimap(name, codec);
    }

    /**
     * Returns map instance by name.
     *
     * @param name - name of object
     * @return Map object
     */
    @Override
    public <K, V> RMapReactive<K, V> getMap(String name) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getMap(name);
    }

    /**
     * Returns map instance by name.
     *
     * @param name    - name of object
     * @param options - map options
     * @return Map object
     */
    @Override
    public <K, V> RMapReactive<K, V> getMap(String name, MapOptions<K, V> options) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getMap(name, options);
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
    public <K, V> RMapReactive<K, V> getMap(String name, Codec codec) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getMap(name, codec);
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
    public <K, V> RMapReactive<K, V> getMap(String name, Codec codec, MapOptions<K, V> options) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getMap(name, codec, options);
    }

    /**
     * Returns set instance by name.
     *
     * @param name - name of object
     * @return Set object
     */
    @Override
    public <V> RSetReactive<V> getSet(String name) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getSet(name);
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
    public <V> RSetReactive<V> getSet(String name, Codec codec) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getSet(name, codec);
    }

    /**
     * Returns Redis Sorted Set instance by name.
     * This sorted set sorts objects by object score.
     *
     * @param name of scored sorted set
     * @return ScoredSortedSet object
     */
    @Override
    public <V> RScoredSortedSetReactive<V> getScoredSortedSet(String name) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getScoredSortedSet(name);
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
    public <V> RScoredSortedSetReactive<V> getScoredSortedSet(String name, Codec codec) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getScoredSortedSet(name, codec);
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
    public RLexSortedSetReactive getLexSortedSet(String name) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getLexSortedSet(name);
    }

    /**
     * Returns topic instance by name.
     *
     * @param name - name of object
     * @return Topic object
     */
    @Override
    public RTopicReactive getTopic(String name) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getTopic(name);
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
    public RTopicReactive getTopic(String name, Codec codec) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getTopic(name, codec);
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
    public RReliableTopicReactive getReliableTopic(String name) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getReliableTopic(name);
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
    public RReliableTopicReactive getReliableTopic(String name, Codec codec) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getReliableTopic(name, codec);
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
    public RPatternTopicReactive getPatternTopic(String pattern) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getPatternTopic(pattern);
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
    public RPatternTopicReactive getPatternTopic(String pattern, Codec codec) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getPatternTopic(pattern, codec);
    }

    /**
     * Returns queue instance by name.
     *
     * @param name - name of object
     * @return Queue object
     */
    @Override
    public <V> RQueueReactive<V> getQueue(String name) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getQueue(name);
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
    public <V> RQueueReactive<V> getQueue(String name, Codec codec) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getQueue(name, codec);
    }

    /**
     * Returns RingBuffer based queue.
     *
     * @param name - name of object
     * @return RingBuffer object
     */
    @Override
    public <V> RRingBufferReactive<V> getRingBuffer(String name) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getRingBuffer(name);
    }

    /**
     * Returns RingBuffer based queue.
     *
     * @param name  - name of object
     * @param codec - codec for values
     * @return RingBuffer object
     */
    @Override
    public <V> RRingBufferReactive<V> getRingBuffer(String name, Codec codec) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getRingBuffer(name, codec);
    }

    /**
     * Returns blocking queue instance by name.
     *
     * @param name - name of object
     * @return BlockingQueue object
     */
    @Override
    public <V> RBlockingQueueReactive<V> getBlockingQueue(String name) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getBlockingQueue(name);
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
    public <V> RBlockingQueueReactive<V> getBlockingQueue(String name, Codec codec) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getBlockingQueue(name, codec);
    }

    /**
     * Returns unbounded blocking deque instance by name.
     *
     * @param name - name of object
     * @return BlockingDeque object
     */
    @Override
    public <V> RBlockingDequeReactive<V> getBlockingDeque(String name) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getBlockingDeque(name);
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
    public <V> RBlockingDequeReactive<V> getBlockingDeque(String name, Codec codec) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getBlockingDeque(name, codec);
    }

    /**
     * Returns transfer queue instance by name.
     *
     * @param name - name of object
     * @return TransferQueue object
     */
    @Override
    public <V> RTransferQueueReactive<V> getTransferQueue(String name) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getTransferQueue(name);
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
    public <V> RTransferQueueReactive<V> getTransferQueue(String name, Codec codec) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getTransferQueue(name, codec);
    }

    /**
     * Returns deque instance by name.
     *
     * @param name - name of object
     * @return Deque object
     */
    @Override
    public <V> RDequeReactive<V> getDeque(String name) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getDeque(name);
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
    public <V> RDequeReactive<V> getDeque(String name, Codec codec) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getDeque(name, codec);
    }

    /**
     * Returns "atomic long" instance by name.
     *
     * @param name of the "atomic long"
     * @return AtomicLong object
     */
    @Override
    public RAtomicLongReactive getAtomicLong(String name) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getAtomicLong(name);
    }

    /**
     * Returns "atomic double" instance by name.
     *
     * @param name of the "atomic double"
     * @return AtomicLong object
     */
    @Override
    public RAtomicDoubleReactive getAtomicDouble(String name) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getAtomicDouble(name);
    }

    /**
     * Returns object for remote operations prefixed with the default name (redisson_remote_service)
     *
     * @return RemoteService object
     */
    @Override
    public RRemoteService getRemoteService() {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getRemoteService();
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
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getRemoteService(codec);
    }

    /**
     * Returns object for remote operations prefixed with the specified name
     *
     * @param name - the name used as the Redis key prefix for the services
     * @return RemoteService object
     */
    @Override
    public RRemoteService getRemoteService(String name) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getRemoteService(name);
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
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getRemoteService(name, codec);
    }

    /**
     * Returns bitSet instance by name.
     *
     * @param name - name of object
     * @return BitSet object
     */
    @Override
    public RBitSetReactive getBitSet(String name) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getBitSet(name);
    }

    /**
     * Returns script operations object
     *
     * @return Script object
     */
    @Override
    public RScriptReactive getScript() {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getScript();
    }

    /**
     * Returns script operations object using provided codec.
     *
     * @param codec - codec for params and result
     * @return Script object
     */
    @Override
    public RScriptReactive getScript(Codec codec) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getScript(codec);
    }

    /**
     * Creates transaction with <b>READ_COMMITTED</b> isolation level.
     *
     * @param options - transaction configuration
     * @return Transaction object
     */
    @Override
    public RTransactionReactive createTransaction(TransactionOptions options) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.createTransaction(options);
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
    public RBatchReactive createBatch(BatchOptions options) {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.createBatch(options);
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
    public RBatchReactive createBatch() {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.createBatch();
    }

    /**
     * Returns keys operations.
     * Each of Redis/Redisson object associated with own key
     *
     * @return Keys object
     */
    @Override
    public RKeysReactive getKeys() {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getKeys();
    }

    /**
     * Shuts down Redisson instance <b>NOT</b> Redis server
     */
    @Override
    public void shutdown() {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        redissonReactiveClient.shutdown();
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
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getConfig();
    }

    /**
     * Get Redis nodes group for server operations
     *
     * @return NodesGroup object
     */
    @Override
    public NodesGroup<Node> getNodesGroup() {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getNodesGroup();
    }

    /**
     * Get Redis cluster nodes group for server operations
     *
     * @return NodesGroup object
     */
    @Override
    public NodesGroup<ClusterNode> getClusterNodesGroup() {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getClusterNodesGroup();
    }

    /**
     * Returns {@code true} if this Redisson instance has been shut down.
     *
     * @return <code>true</code> if this Redisson instance has been shut down otherwise <code>false</code>
     */
    @Override
    public boolean isShutdown() {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.isShutdown();
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
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.isShuttingDown();
    }

    /**
     * Returns id of this Redisson instance
     *
     * @return id
     */
    @Override
    public String getId() {
        if(redissonReactiveClient == null){
            throw new UnsupportedOperationException("redisson not connected");
        }
        return redissonReactiveClient.getId();
    }
}
