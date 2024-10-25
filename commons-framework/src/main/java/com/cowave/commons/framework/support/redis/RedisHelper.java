/*
 * Copyright (c) 2017～2024 Cowave All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.commons.framework.support.redis;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.alibaba.fastjson.parser.ParserConfig;
import com.cowave.commons.tools.Asserts;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnectionCommands;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import javax.validation.constraints.NotNull;

/**
 *
 * @author shanhuiming
 *
 */
@SuppressWarnings(value = { "unchecked", "rawtypes" })
public class RedisHelper{

	static {
		ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
	}

	private final RedisTemplate redisTemplate;

	public RedisHelper(RedisTemplate redisTemplate){
		this.redisTemplate = redisTemplate;
	}

	public RedisTemplate getRedisTemplate(){
		return redisTemplate;
	}

	public <T> T getValue(final String key){
		ValueOperations<String, T> operation = redisTemplate.opsForValue();
		return operation.get(key);
	}

	public <T> List<T> getMultiValue(final Collection<String> keys){
		ValueOperations<String, T> operation = redisTemplate.opsForValue();
		return operation.multiGet(keys);
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

	public <T> List<T> getList(final String key, int start, int end){
		return redisTemplate.opsForList().range(key, start, end);
	}

	public <T> Set<T> getSet(final String key){
		return redisTemplate.opsForSet().members(key);
	}

	public <T> List<T> popSet(final String key, final int count){
		return redisTemplate.opsForSet().pop(key, count);
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

	public <T> void offerSet(final String key, final T value){
		Asserts.notNull(value, "redis value can't be bull");
		BoundSetOperations<String, T> setOperation = redisTemplate.boundSetOps(key);
		setOperation.add(value);
	}

	public <T> void putSet(final String key, final Set<T> dataSet){
		BoundSetOperations<String, T> setOperation = redisTemplate.boundSetOps(key);
		for (T t : dataSet) {
			setOperation.add(t);
		}
	}

	public <T> long pushList(final String key, final List<T> dataList){
		Long count = redisTemplate.opsForList().rightPushAll(key, dataList);
		return count == null ? 0 : count;
	}

	public void delete(final String key){
		redisTemplate.delete(key);
	}

	public void delete(final Collection<String> collection){
		redisTemplate.delete(collection);
	}

	public void deleteMap(final String key, final String hKey){
		HashOperations hashOperations = redisTemplate.opsForHash();
		hashOperations.delete(key, hKey);
	}

	public void deleteSet(final String key, final Object... values){
		BoundSetOperations<String, Object> setOperation = redisTemplate.boundSetOps(key);
		setOperation.remove(values);
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

	public boolean expire(final String key, final long timeout){
		return expire(key, timeout, TimeUnit.SECONDS);
	}

	public Boolean expire(final String key, final long timeout, final TimeUnit unit){
		return redisTemplate.expire(key, timeout, unit);
	}

	public Long increment(final String key, final int step){
		return redisTemplate.opsForValue().increment(key, step);
	}

	public Long decrement(final String key, final int step){
		return redisTemplate.opsForValue().decrement(key, step);
	}

	public Boolean memberOfSet(String key, Object member) {
		return redisTemplate.opsForSet().isMember(key, member);
	}

	public <T> Cursor<T> scanSet(String key, ScanOptions scanOptions) {
		return redisTemplate.opsForSet().scan(key, scanOptions);
	}

	public <T> Cursor<Map.Entry<String, T>> scanMap(String key, ScanOptions scanOptions) {
		HashOperations<String, String, T> hashOps = redisTemplate.opsForHash();
		return hashOps.scan(key, scanOptions);
	}

	public Long sizeOfList(String key) {
		return redisTemplate.opsForList().size(key);
	}

	public Long sizeOfMap(String key) {
		return redisTemplate.opsForHash().size(key);
	}

	public Long sizeOfSet(String key) {
		return redisTemplate.opsForSet().size(key);
	}

	public Long sizeOfSortedSet(String key) {
		return redisTemplate.opsForZSet().size(key);
	}

	public <T> Map<String, T> pipeline(Map<String, Consumer<RedisOperations<String, Object>>> operationMap){
		List<String> keys = new ArrayList<>();
		List<T> results = redisTemplate.executePipelined(new SessionCallback<>() {
			@Override
			public Object execute(@NotNull RedisOperations redisOperations) throws DataAccessException {
				operationMap.forEach((key, consumer) -> {
					keys.add(key);
					consumer.accept(redisOperations);
				});
				return null;
			}
		});

		Map<String, T> operationResult = new HashMap<>();
		for(int i = 0; i < keys.size(); i++){
			operationResult.put(keys.get(i), results.get(i));
		}
		return operationResult;
	}

	public static RedisHelper newRedisHelper(RedisTemplate<Object, Object> template){
		return new RedisHelper(template);
	}
}
