package com.cowave.commons.framework.support.redis;

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.connection.RedisConnectionCommands;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/**
 *
 * @author shanhuiming
 *
 */
@SuppressWarnings(value = { "unchecked", "rawtypes" })
public class RedisHelper{

	private final RedisTemplate redisTemplate;

	public RedisHelper(RedisTemplate redisTemplate){
		this.redisTemplate = redisTemplate;
	}

	public RedisTemplate getRedisTemplate(){
		return redisTemplate;
	}

	public void ping(){
		redisTemplate.execute((RedisCallback<String>) RedisConnectionCommands::ping);
	}

	public Properties info(){
		return (Properties)redisTemplate.execute((RedisCallback<Object>) RedisServerCommands::info);
	}

	public Collection<String> keys(final String pattern){
		return redisTemplate.keys(pattern);
	}

	public <T> T getValue(final String key){
		ValueOperations<String, T> operation = redisTemplate.opsForValue();
		return operation.get(key);
	}

	public <T> void putValue(final String key, final T value){
		redisTemplate.opsForValue().set(key, value);
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

	public <T> void putMapValue(final String key, final String hKey, final T value){
		redisTemplate.opsForHash().put(key, hKey, value);
	}

	public <T> void putMapAll(final String key, final Map<String, T> dataMap){
		if(dataMap == null || dataMap.isEmpty()) {
			return;
		}
		redisTemplate.opsForHash().putAll(key, dataMap);
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

	public void delete(final String key){
		redisTemplate.delete(key);
	}

	public void delete(final Collection<?> collection){
		redisTemplate.delete(collection);
	}

	public <T> List<T> getList(final String key){
		return redisTemplate.opsForList().range(key, 0, -1);
	}

	public <T> long pushList(final String key, final List<T> dataList){
		Long count = redisTemplate.opsForList().rightPushAll(key, dataList);
		return count == null ? 0 : count;
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

	public <T> void putSet(final String key, final T value){
		BoundSetOperations<String, T> setOperation = redisTemplate.boundSetOps(key);
		setOperation.add(value);
	}

	public static RedisHelper newRedisHelper(RedisTemplate<Object, Object> template){
		return new RedisHelper(template);
	}
}
