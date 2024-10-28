/*
 * Copyright (c) 2017～2024 Cowave All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.commons.framework.helper.redis;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.cowave.commons.tools.Asserts;
import org.springframework.data.redis.connection.RedisConnectionCommands;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.*;

import javax.validation.constraints.NotNull;

/**
 *
 * @author shanhuiming
 *
 */
@SuppressWarnings(value = { "unchecked", "rawtypes" })
public class RedisHelper{

	private final RedisTemplate redisTemplate;

	public static RedisHelper newRedisHelper(RedisTemplate<Object, Object> template){
		return new RedisHelper(template);
	}

	public RedisHelper(RedisTemplate redisTemplate){
		this.redisTemplate = redisTemplate;
	}

	public RedisTemplate getRedisTemplate(){
		return redisTemplate;
	}

	public Properties info(){
		return (Properties) redisTemplate.execute((RedisCallback<Properties>) RedisServerCommands::info);
	}

	public void ping(){
		redisTemplate.execute((RedisCallback<String>) RedisConnectionCommands::ping);
	}

	public Collection<String> keys(final String pattern){
		return redisTemplate.keys(pattern);
	}

	public void delete(final String key){
		redisTemplate.delete(key);
	}

	public void delete(final Collection<String> collection){
		redisTemplate.delete(collection);
	}

	public Boolean expire(final String key, final long timeout, final TimeUnit unit){
		return redisTemplate.expire(key, timeout, unit);
	}

	public <T> Map<String, T> pipeline(Map<String, Consumer<RedisOperations<String, Object>>> operationMap) {
		List<String> keys = new ArrayList<>(operationMap.keySet());
		List<T> results = redisTemplate.executePipelined(new SessionCallback<>() {
			@Override
			public Object execute(@NotNull RedisOperations redisOperations) {
				operationMap.forEach((key, consumer) -> {
					consumer.accept(redisOperations);
				});
				return null;
			}
		});
		return IntStream.range(0, keys.size()).boxed().collect(Collectors.toMap(keys::get, results::get));
	}

	/* ******************************************
	 * opsForValue
	 * ******************************************/

	public <T> T getValue(final String key){
		ValueOperations<String, T> operation = redisTemplate.opsForValue();
		return operation.get(key);
	}

	public <T> List<T> getMultiValue(final Collection<String> keys){
		ValueOperations<String, T> operation = redisTemplate.opsForValue();
		return operation.multiGet(keys);
	}

	public <T> void putValue(final String key, final T value){
		Asserts.notNull(value, "redis value can't be bull");
		redisTemplate.opsForValue().set(key, value);
	}

	public <T> void putMultiValue(final Map<String, T> map){
		redisTemplate.opsForValue().multiSet(map);
	}

	public <T> void putExpireValue(final String key, final T value, final Integer timeout, final TimeUnit timeUnit){
		Asserts.notNull(value, "redis value can't be bull");
		redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
	}

	public Long increment(final String key, final int step){
		return redisTemplate.opsForValue().increment(key, step);
	}

	public Long decrement(final String key, final int step){
		return redisTemplate.opsForValue().decrement(key, step);
	}

	/* ******************************************
	 * opsForHash
	 * ******************************************/

	public Long sizeOfMap(String key) {
		return redisTemplate.opsForHash().size(key);
	}

	public <T> Cursor<Map.Entry<String, T>> scanMap(String key, ScanOptions scanOptions) {
		HashOperations<String, String, T> hashOps = redisTemplate.opsForHash();
		return hashOps.scan(key, scanOptions);
	}

	public <T> Map<String, T> getMap(final String key){
		return redisTemplate.opsForHash().entries(key);
	}

	public <T> T getMap(final String key, final String hKey){
		HashOperations<String, String, T> opsForHash = redisTemplate.opsForHash();
		return opsForHash.get(key, hKey);
	}

	public <T> List<T> getMultiMap(final String key, final Collection<String> hKeys){
		return redisTemplate.opsForHash().multiGet(key, hKeys);
	}

	public <T> void putMap(final String key, final String hKey, final T value){
		Asserts.notNull(value, "redis value can't be bull");
		redisTemplate.opsForHash().put(key, hKey, value);
	}

	public <T> void putMap(final String key, final Map<String, T> dataMap){
		if(dataMap == null || dataMap.isEmpty()) {
			return;
		}
		redisTemplate.opsForHash().putAll(key, dataMap);
	}

	public void deleteMap(final String key, final String hKey){
		HashOperations hashOperations = redisTemplate.opsForHash();
		hashOperations.delete(key, hKey);
	}

	/* ******************************************
	 * opsForList
	 * ******************************************/

	public Long sizeOfList(String key) {
		return redisTemplate.opsForList().size(key);
	}

	public <T> List<T> getList(final String key, int start, int end){
		return redisTemplate.opsForList().range(key, start, end);
	}

	public <T> long pushList(final String key, final List<T> dataList){
		Long count = redisTemplate.opsForList().rightPushAll(key, dataList);
		return count == null ? 0 : count;
	}

	/* ******************************************
	 * opsForSet
	 * ******************************************/

	public Long sizeOfSet(String key) {
		return redisTemplate.opsForSet().size(key);
	}

	public <T> Cursor<T> scanSet(String key, ScanOptions scanOptions) {
		return redisTemplate.opsForSet().scan(key, scanOptions);
	}

	public Boolean memberOfSet(String key, Object member) {
		return redisTemplate.opsForSet().isMember(key, member);
	}

	public <T> Set<T> getSet(final String key){
		return redisTemplate.opsForSet().members(key);
	}

	public <T> void putSet(final String key, final Set<T> dataSet){
		BoundSetOperations<String, T> setOperation = redisTemplate.boundSetOps(key);
		for (T t : dataSet) {
			setOperation.add(t);
		}
	}

	public <T> List<T> popSet(final String key, final int count){
		return redisTemplate.opsForSet().pop(key, count);
	}

	public <T> void offerSet(final String key, final T value){
		Asserts.notNull(value, "redis value can't be bull");
		BoundSetOperations<String, T> setOperation = redisTemplate.boundSetOps(key);
		setOperation.add(value);
	}

	public void deleteSet(final String key, final Object... values){
		BoundSetOperations<String, Object> setOperation = redisTemplate.boundSetOps(key);
		setOperation.remove(values);
	}

	/* ******************************************
	 * opsForSet
	 * ******************************************/

	public Long sizeOfZset(final String key) {
		return redisTemplate.opsForZSet().size(key);
	}

	public Boolean memberOfZset(final String key, final String value) {
        return redisTemplate.opsForZSet().score(key, value) != null;
    }

    public <T> T firstOfZset(final String key){
        Set<T> set = redisTemplate.opsForZSet().range(key, 0, 0);
        if (set != null && !set.isEmpty()) {
            return set.iterator().next();
        }
        return null;
    }

    public Long rankOfZset(final String key, final String value){
        return redisTemplate.opsForZSet().rank(key, value);
    }

	public <T> Set<T> getZset(final String key, final long start, final long end){
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    public void putZset(final String key, final String value, final double score){
        redisTemplate.opsForZSet().add(key, value, score);
    }

    public void deleteZset(final String key, final String... values){
        redisTemplate.opsForZSet().remove(key, values);
    }

    public void deleteZsetByScore(final String key, final double min, final double max){
        redisTemplate.opsForZSet().removeRangeByScore(key, min, max);
    }
}
