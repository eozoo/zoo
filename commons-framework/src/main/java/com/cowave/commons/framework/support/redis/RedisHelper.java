/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.support.redis;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnectionCommands;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.*;

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

	private final StringRedisTemplate stringRedisTemplate;

	public RedisHelper(RedisTemplate redisTemplate, StringRedisTemplate stringRedisTemplate){
		this.redisTemplate = redisTemplate;
		this.stringRedisTemplate = stringRedisTemplate;
	}

	public RedisTemplate getRedisTemplate(){
		return redisTemplate;
	}

	public StringRedisTemplate getStringRedisTemplate() {
		return stringRedisTemplate;
	}

	public <T> T getValue(final String key){
		ValueOperations<String, T> operation = redisTemplate.opsForValue();
		return operation.get(key);
	}

	public <T> List<T> getMultiValue(final String ... keys){
		ValueOperations<String, T> operation = redisTemplate.opsForValue();
		return operation.multiGet(List.of(keys));
	}

	public <T> void putValue(final String key, final T value){
		redisTemplate.opsForValue().set(key, value);
	}

	public <T> void putMultiValue(final Map<String, T> map){
		redisTemplate.opsForValue().multiSet(map);
	}

	public <T> void putExpireValue(final String key, final T value, final Integer timeout, final TimeUnit timeUnit){
		redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
	}

	public <T> Map<String, T> getMap(final String key){
		return redisTemplate.opsForHash().entries(key);
	}

	public <T> T getMapValue(final String key, final String hKey){
		HashOperations<String, String, T> opsForHash = redisTemplate.opsForHash();
		return opsForHash.get(key, hKey);
	}

	public <T> List<T> getMultiMapValue(final String key, final Collection<Object> hKeys){
		return redisTemplate.opsForHash().multiGet(key, hKeys);
	}

	public <T> Cursor<Map.Entry<String, T>> scanMap(String key, ScanOptions scanOptions) {
		HashOperations<String, String, T> hashOps = redisTemplate.opsForHash();
		return hashOps.scan(key, scanOptions);
	}

	public <T> void putMapValue(final String key, final String hKey, final T value){
		redisTemplate.opsForHash().put(key, hKey, value);
	}

	public <T> void putMapAll(final String key, final Map<String, T> dataMap){
		if(dataMap == null || dataMap.isEmpty()) {
			return;
		}
		redisTemplate.opsForHash().putAll(key, dataMap);
	}

	public <T> List<T> getList(final String key, int start, int end){
		return redisTemplate.opsForList().range(key, start, end);
	}

	public <T> long pushList(final String key, final List<T> dataList){
		Long count = redisTemplate.opsForList().rightPushAll(key, dataList);
		return count == null ? 0 : count;
	}

	public <T> Set<T> getSet(final String key){
		return redisTemplate.opsForSet().members(key);
	}

	public Boolean isSetMember(String key, Object member) {
		return redisTemplate.opsForSet().isMember(key, member);
	}

	public <T> Cursor<T> scanSet(String key, ScanOptions scanOptions) {
		return redisTemplate.opsForSet().scan(key, scanOptions);
	}

	public <T> void putSet(final String key, final Set<T> dataSet){
		BoundSetOperations<String, T> setOperation = redisTemplate.boundSetOps(key);
		for (T t : dataSet) {
			setOperation.add(t);
		}
	}

	public <T> void putSet(final String key, final T value){
		BoundSetOperations<String, T> setOperation = redisTemplate.boundSetOps(key);
		setOperation.add(value);
	}

	public <T> T stringGetValue(final Class<T> clazz, final String key){
		ValueOperations<String, String> operation = stringRedisTemplate.opsForValue();
		String value = operation.get(key);
		if(value != null){
			return JSON.parseObject(value, clazz);
		}
		return null;
	}

	public <T> List<T> stringGetMultiValue(final Class<T> clazz, final String ... keys){
		ValueOperations<String, String> operation = stringRedisTemplate.opsForValue();
		List<String> values = operation.multiGet(List.of(keys));
		List<T> list = new ArrayList<>();
		if(CollectionUtils.isNotEmpty(values)){
			for(String value : values){
				list.add(JSON.parseObject(value, clazz));
			}
		}
		return list;
	}

	public void stringPutValue(final String key, final String value){
		stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(value));
	}

	public void stringPutMultiValue(final Map<String, String> map){
		stringRedisTemplate.opsForValue().multiSet(map);
	}

	public void stringPutExpireValue(final String key, final String value, final Integer timeout, final TimeUnit timeUnit){
		stringRedisTemplate.opsForValue().set(key, value, timeout, timeUnit);
	}

	public <T> Map<String, T> stringGetMap(final Class<T> clazz, final String key){
		HashOperations<String, String, String> operations = stringRedisTemplate.opsForHash();
		Map<String, String> mapValue = operations.entries(key);
		Map<String, T> map =  new HashMap<>();
		if(MapUtils.isNotEmpty(mapValue)){
			for(Map.Entry<String, String> entry : mapValue.entrySet()){
				map.put(entry.getKey(), JSON.parseObject(entry.getValue(), clazz));
			}
		}
		return map;
	}

	public <T> T stringGetMapValue(final Class<T> clazz, final String key, final String hKey){
		HashOperations<String, String, String> opsForHash = stringRedisTemplate.opsForHash();
		String value = opsForHash.get(key, hKey);
		if(value != null){
			return JSON.parseObject(value, clazz);
		}
		return null;
	}

	public <T> List<T> stringGetMultiMapValue(final Class<T> clazz, final String key, final Collection<String> hKeys){
		HashOperations<String, String, String> opsForHash = stringRedisTemplate.opsForHash();
		List<String> values = opsForHash.multiGet(key, hKeys);
		List<T> list = new ArrayList<>();
		if(CollectionUtils.isNotEmpty(values)){
			for(String value : values){
				list.add(JSON.parseObject(value, clazz));
			}
		}
		return list;
	}

	public Cursor<Map.Entry<String, String>> stringScanMap(String key, ScanOptions scanOptions) {
		HashOperations<String, String, String> hashOps = stringRedisTemplate.opsForHash();
		return hashOps.scan(key, scanOptions);
	}

	public void stringPutMapValue(final String key, final String hKey, final String value){
		stringRedisTemplate.opsForHash().put(key, hKey, value);
	}

	public void stringPutMapAll(final String key, final Map<String, String> dataMap){
		if(dataMap == null || dataMap.isEmpty()) {
			return;
		}
		stringRedisTemplate.opsForHash().putAll(key, dataMap);
	}

	public <T> List<T> stringGetList(final Class<T> clazz, final String key, int start, int end){
		List<String> values = stringRedisTemplate.opsForList().range(key, start, end);
		List<T> list = new ArrayList<>();
		if(CollectionUtils.isNotEmpty(values)){
			for(String value : values){
				list.add(JSON.parseObject(value, clazz));
			}
		}
		return list;
	}

	public long stringPushList(final String key, final List<String> dataList){
		Long count = stringRedisTemplate.opsForList().rightPushAll(key, dataList);
		return count == null ? 0 : count;
	}

	public <T> Set<T> stringGetSet(final Class<T> clazz, final String key){
		Set<String> values = stringRedisTemplate.opsForSet().members(key);
		Set<T> set = new HashSet<>();
		if(CollectionUtils.isNotEmpty(values)){
			for(String value : values){
				set.add(JSON.parseObject(value, clazz));
			}
		}
		return set;
	}

	public Boolean stringIsSetMember(String key, String member) {
		return stringRedisTemplate.opsForSet().isMember(key, member);
	}

	public Cursor<String> stringScanSet(String key, ScanOptions scanOptions) {
		return stringRedisTemplate.opsForSet().scan(key, scanOptions);
	}

	public void stringPutSet(final String key, final Set<String> dataSet){
		BoundSetOperations<String, String> setOperation = stringRedisTemplate.boundSetOps(key);
		for (String t : dataSet) {
			setOperation.add(t);
		}
	}

	public void stringPutSet(final String key, final String value){
		BoundSetOperations<String, String> setOperation = stringRedisTemplate.boundSetOps(key);
		setOperation.add(value);
	}

	public Properties info(){
		return (Properties)redisTemplate.execute((RedisCallback<Object>) RedisServerCommands::info);
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

	public void delete(final Collection<?> collection){
		redisTemplate.delete(collection);
	}

	public void deleteMapValue(final String key, final String hKey){
		HashOperations hashOperations = redisTemplate.opsForHash();
		hashOperations.delete(key, hKey);
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

	public Map<String, Object> stringPipeline(Map<String, Consumer<RedisOperations<String, Object>>> operationMap){
		List<String> keys = new ArrayList<>();
		List<Object> results = redisTemplate.executePipelined(new SessionCallback<>() {
			@Override
			public Object execute(@NotNull RedisOperations redisOperations) throws DataAccessException {
				operationMap.forEach((key, consumer) -> {
					keys.add(key);
					consumer.accept(redisOperations);
				});
				return null;
			}
		});

		Map<String, Object> operationResult = new HashMap<>();
		for(int i = 0; i < keys.size(); i++){
			operationResult.put(keys.get(i), results.get(i));
		}
		return operationResult;
	}

	public static RedisHelper newRedisHelper(RedisTemplate<Object, Object> template, StringRedisTemplate stringRedisTemplate){
		return new RedisHelper(template, stringRedisTemplate);
	}
}
