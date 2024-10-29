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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.cowave.commons.tools.Asserts;
import com.cowave.commons.tools.Collections;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.redis.connection.RedisConnectionCommands;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author shanhuiming
 *
 */
public class StringRedisHelper {

    public static final String LUA_CLEAN = """
            local cursor = "0"
            repeat
                local result = redis.call("SCAN", cursor, "MATCH", ARGV[1], "COUNT", 100)
                cursor = result[1]
                for _, key in ipairs(result[2]) do
                    redis.call("DEL", key)
                end
            until cursor == "0"
            """;

    private final StringRedisTemplate stringRedisTemplate;

    public static StringRedisHelper newStringRedisHelper(StringRedisTemplate stringRedisTemplate){
        return new StringRedisHelper(stringRedisTemplate);
    }

    public StringRedisHelper(StringRedisTemplate stringRedisTemplate){
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public StringRedisTemplate getRedisTemplate() {
        return stringRedisTemplate;
    }

    public Properties info(){
        return stringRedisTemplate.execute((RedisCallback<Properties>) RedisServerCommands::info);
    }

    public String ping(){
        return stringRedisTemplate.execute(RedisConnectionCommands::ping);
    }

    public Collection<String> keys(final String pattern){
        return stringRedisTemplate.keys(pattern);
    }

    public void delete(final String key){
        stringRedisTemplate.delete(key);
    }

    public void delete(final Collection<String> keys){
        stringRedisTemplate.delete(keys);
    }

    public Boolean expire(final String key, final long timeout, final TimeUnit unit){
        return stringRedisTemplate.expire(key, timeout, unit);
    }

    public Map<String, Object> pipeline(Map<String, Consumer<RedisOperations<String, Object>>> operationMap){
        List<String> keys = new ArrayList<>(operationMap.keySet());
        List<Object> results = stringRedisTemplate.executePipelined(new SessionCallback<>() {
            @Override
			public Object execute(@NotNull RedisOperations redisOperations) {
				operationMap.forEach((key, consumer) -> consumer.accept(redisOperations));
				return null;
			}
        });
        return IntStream.range(0, keys.size()).boxed().collect(Collectors.toMap(keys::get, results::get));
    }

    public void luaClean(String pattern){
        DefaultRedisScript<Void> luaScript = new DefaultRedisScript<>();
        luaScript.setScriptText(LUA_CLEAN);
        luaScript.setResultType(Void.class);
        stringRedisTemplate.execute(luaScript, java.util.Collections.emptyList(), pattern);
    }

    public <T> T luaExec(String lua, Class<T> resultType, List<String> keys, Object... args){
        DefaultRedisScript<T> luaScript = new DefaultRedisScript<>();
        luaScript.setScriptText(lua);
        luaScript.setResultType(resultType);
        return stringRedisTemplate.execute(luaScript, keys, args);
    }

	/* ******************************************
	 * opsForValue
	 * ******************************************/

    public String getValue(final String key){
        ValueOperations<String, String> operation = stringRedisTemplate.opsForValue();
        return operation.get(key);
    }

    public <T> T getValue(final String key, final Class<T> clazz){
        ValueOperations<String, String> operation = stringRedisTemplate.opsForValue();
        String value = operation.get(key);
        if(value != null){
            return JSON.parseObject(value, clazz);
        }
        return null;
    }

    public <T> List<T> getArrayValue(final String key, final Class<T> clazz) {
        return JSONArray.parseArray(getValue(key), clazz);
    }

    public List<String> getMultiValue(final Collection<String> keys){
        ValueOperations<String, String> operation = stringRedisTemplate.opsForValue();
        return operation.multiGet(keys);
    }

    public <T> List<T> getMultiValue(final Collection<String> keys, final Class<T> clazz){
        ValueOperations<String, String> operation = stringRedisTemplate.opsForValue();
        List<String> values = operation.multiGet(keys);
        List<T> list = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(values)){
            for(String value : values){
                list.add(JSON.parseObject(value, clazz));
            }
        }
        return list;
    }

    public void putValue(final String key, Object value){
        stringRedisTemplate.opsForValue().set(key, toStringValue(value));
    }

    public void putMultiValue(final Map<String, ?> map){
        if(map == null || map.isEmpty()) {
            return;
        }
        stringRedisTemplate.opsForValue().multiSet(Collections.copyToMap(
                map.entrySet(), Map.Entry::getKey, entry -> toStringValue(entry.getValue())));
    }

    public void putExpireValue(final String key, final Object value, final Integer timeout, final TimeUnit timeUnit){
        stringRedisTemplate.opsForValue().set(key, toStringValue(value), timeout, timeUnit);
    }

    public Long increment(final String key, final int step){
        return stringRedisTemplate.opsForValue().increment(key, step);
    }

    public Long decrement(final String key, final int step){
        return stringRedisTemplate.opsForValue().decrement(key, step);
    }

	/* ******************************************
	 * opsForHash
	 * ******************************************/

    public Long sizeOfMap(String key) {
        return stringRedisTemplate.opsForHash().size(key);
    }

    public Cursor<Map.Entry<String, String>> scanMap(String key, ScanOptions scanOptions) {
        HashOperations<String, String, String> hashOps = stringRedisTemplate.opsForHash();
        return hashOps.scan(key, scanOptions);
    }

    public Map<String, String> getMap(final String key){
        HashOperations<String, String, String> operations = stringRedisTemplate.opsForHash();
        return operations.entries(key);
    }

    public <T> Map<String, T> getMap(final String key, final Class<T> clazz){
        HashOperations<String, String, String> operations = stringRedisTemplate.opsForHash();
        Map<String, String> mapValue = operations.entries(key);
        return Collections.copyToMap(
                mapValue.entrySet(), Map.Entry::getKey, entry -> JSON.parseObject(entry.getValue(), clazz));
    }

    public String getMap(final String key, final String hKey){
        HashOperations<String, String, String> opsForHash = stringRedisTemplate.opsForHash();
        return opsForHash.get(key, hKey);
    }

    public <T> T getMap(final String key, final String hKey, final Class<T> clazz){
        HashOperations<String, String, String> opsForHash = stringRedisTemplate.opsForHash();
        String value = opsForHash.get(key, hKey);
        if(value != null){
            return JSON.parseObject(value, clazz);
        }
        return null;
    }

    public List<String> getMultiMap(final String key, final Collection<String> hKeys){
        HashOperations<String, String, String> opsForHash = stringRedisTemplate.opsForHash();
        return opsForHash.multiGet(key, hKeys);
    }

    public <T> List<T> getMultiMap(final String key, final Collection<String> hKeys, final Class<T> clazz){
        HashOperations<String, String, String> opsForHash = stringRedisTemplate.opsForHash();
        List<String> values = opsForHash.multiGet(key, hKeys);
        return Collections.copyToList(values, value -> JSON.parseObject(value, clazz));
    }

    public void putMap(final String key, final String hKey, final Object value){
        stringRedisTemplate.opsForHash().put(key, hKey, toStringValue(value));
    }

    public void putMap(final String key, final Map<String, ?> dataMap){
        if(dataMap == null || dataMap.isEmpty()) {
            return;
        }
        Map<String, String> stringMap = Collections.copyToMap(
                dataMap.entrySet(), Map.Entry::getKey, entry -> toStringValue(entry.getValue()));
        stringRedisTemplate.opsForHash().putAll(key, stringMap);
    }

    public void deleteMap(final String key, final String hKey){
        HashOperations hashOperations = stringRedisTemplate.opsForHash();
        hashOperations.delete(key, hKey);
    }

	/* ******************************************
	 * opsForList
	 * ******************************************/

    public Long sizeOfList(String key) {
        return stringRedisTemplate.opsForList().size(key);
    }

    public List<String> rangeOfList(final String key, int start, int end){
        return stringRedisTemplate.opsForList().range(key, start, end);
    }

    public <T> List<T> rangeOfList(final String key, int start, int end, final Class<T> clazz){
        List<String> values = stringRedisTemplate.opsForList().range(key, start, end);
        return Collections.copyToList(values, value -> JSON.parseObject(value, clazz));
    }

    public long pushList(final String key, final List<Object> dataList){
        List<String> stringList = Collections.copyToList(dataList, this::toStringValue);
        Long count = stringRedisTemplate.opsForList().rightPushAll(key, stringList);
        return count == null ? 0 : count;
    }

	/* ******************************************
	 * opsForSet
	 * ******************************************/

    public Long sizeOfSet(String key) {
        return stringRedisTemplate.opsForSet().size(key);
    }

    public Cursor<String> scanSet(String key, ScanOptions scanOptions) {
        return stringRedisTemplate.opsForSet().scan(key, scanOptions);
    }

    public Boolean memberOfSet(String key, Object member) {
        return stringRedisTemplate.opsForSet().isMember(key, JSON.toJSONString(member));
    }

    public Set<String> getSet(final String key){
        return stringRedisTemplate.opsForSet().members(key);
    }

    public <T> Set<T> getSet(final String key, final Class<T> clazz){
        Set<String> values = stringRedisTemplate.opsForSet().members(key);
        Set<T> set = new HashSet<>();
        if(CollectionUtils.isNotEmpty(values)){
            for(String value : values){
                set.add(JSON.parseObject(value, clazz));
            }
        }
        return set;
    }

    public <T> void putSet(final String key, final Set<T> dataSet){
        BoundSetOperations<String, String> setOperation = stringRedisTemplate.boundSetOps(key);
        for (T data : dataSet) {
            setOperation.add(toStringValue(data));
        }
    }

    public List<String> popSet(final String key, final int count){
        return stringRedisTemplate.opsForSet().pop(key, count);
    }

    public <T> List<T> popSet(final String key, final int count, final Class<T> clazz){
        List<String> list = stringRedisTemplate.opsForSet().pop(key, count);
        return Collections.copyToList(list, e -> JSON.parseObject(e, clazz));
    }

    public void offerSet(final String key, final Object value){
        BoundSetOperations<String, String> setOperation = stringRedisTemplate.boundSetOps(key);
        setOperation.add(toStringValue(value));
    }

    public void deleteSet(final String key, final Object... values){
        BoundSetOperations<String, String> setOperation = stringRedisTemplate.boundSetOps(key);
        setOperation.remove(values);
    }

	/* ******************************************
	 * opsForSet
	 * ******************************************/

    public Long sizeOfZset(final String key) {
        return stringRedisTemplate.opsForZSet().size(key);
    }

    public Boolean memberOfZset(final String key, final String value) {
        return stringRedisTemplate.opsForZSet().score(key, toStringValue(value)) != null;
    }

    public String firstOfZset(final String key){
        Set<String> set = stringRedisTemplate.opsForZSet().range(key, 0, 0);
        if (set != null && !set.isEmpty()) {
            return set.iterator().next();
        }
        return null;
    }

    public <T> T firstOfZset(final String key, final Class<T> clazz){
        Set<String> set = stringRedisTemplate.opsForZSet().range(key, 0, 0);
        if (set != null && !set.isEmpty()) {
            return JSON.parseObject(set.iterator().next(), clazz);
        }
        return null;
    }

    public Long rankOfZset(final String key, final String value){
        return stringRedisTemplate.opsForZSet().rank(key, value);
    }

    public Set<String> rangeOfZset(final String key, final long start, final long end) {
        return stringRedisTemplate.opsForZSet().range(key, start, end);
    }

	public <T> Set<T> rangeOfZset(final String key, final long start, final long end, final Class<T> clazz){
        Set<String> values = stringRedisTemplate.opsForZSet().range(key, start, end);
        Set<T> set = new HashSet<>();
        if(CollectionUtils.isNotEmpty(values)){
            for(String value : values){
                set.add(JSON.parseObject(value, clazz));
            }
        }
        return set;
    }

    public void putZset(final String key, final String value, final double score){
        stringRedisTemplate.opsForZSet().add(key, value, score);
    }

    public void deleteZset(final String key, final String... values){
        stringRedisTemplate.opsForZSet().remove(key, values);
    }

    public void deleteZsetByScore(final String key, final double min, final double max){
        stringRedisTemplate.opsForZSet().removeRangeByScore(key, min, max);
    }

    private String toStringValue(Object value){
        Asserts.notNull(value, "redis value can't be bull");
        if(String.class.isAssignableFrom(value.getClass())){
            return value.toString();
        }else{
            return JSON.toJSONString(value);
        }
    }
}
