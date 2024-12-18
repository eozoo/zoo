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
import com.cowave.commons.response.exception.Asserts;
import com.cowave.commons.tools.Collections;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionCommands;
import org.springframework.data.redis.connection.RedisListCommands;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;

import javax.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
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

    public RedisSerializer<String> getStringSerializer(){
        return stringRedisTemplate.getStringSerializer();
    }

    /**
     * @see <a href="https://redis.io/commands/info">Redis Documentation: INFO</a>
     */
    public Properties info(){
        return stringRedisTemplate.execute((RedisCallback<Properties>) RedisServerCommands::info);
    }

    /**
     * @see <a href="https://redis.io/commands/ping">Redis Documentation: PING</a>
     */
    public String ping(){
        return stringRedisTemplate.execute(RedisConnectionCommands::ping);
    }

    /**
     * @see <a href="https://redis.io/commands/keys">Redis Documentation: KEYS</a>
     */
    public Collection<String> keys(final String pattern){
        List<String> keys = new ArrayList<>();
        stringRedisTemplate.execute((RedisConnection connection) -> {
            Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match(pattern).count(100).build());
            while (cursor.hasNext()) {
                keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
            }
            return null;
        });
        return keys;
    }

    /**
     * @see <a href="https://redis.io/commands/del">Redis Documentation: DEL</a>
     */
    public void delete(final String... keys){
        if(ArrayUtils.isEmpty(keys)){
            return;
        }
        stringRedisTemplate.delete(List.of(keys));
    }

    /**
     * @see <a href="https://redis.io/commands/del">Redis Documentation: DEL</a>
     */
    public void delete(final Collection<String> keys){
        stringRedisTemplate.delete(keys);
    }

    /**
     * @see <a href="https://redis.io/commands/pexpire">Redis Documentation: PEXPIRE</a>
     */
    public Boolean expire(final String key, final long timeout, final TimeUnit unit){
        return stringRedisTemplate.expire(key, timeout, unit);
    }

    public Map<String, Object> pipeline(Map<String, java.util.function.Consumer<RedisOperations<String, Object>>> operationMap){
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

    /**
     * @see <a href="https://redis.io/commands/get">Redis Documentation: GET</a>
     */
    public String getValue(final String key){
        ValueOperations<String, String> operation = stringRedisTemplate.opsForValue();
        return operation.get(key);
    }

    /**
     * @see <a href="https://redis.io/commands/get">Redis Documentation: GET</a>
     */
    public <T> T getValue(final String key, final Class<T> clazz){
        return JSON.parseObject(getValue(key), clazz);
    }

    /**
     * @see <a href="https://redis.io/commands/get">Redis Documentation: GET</a>
     */
    public <T> List<T> getArrayValue(final String key, final Class<T> clazz) {
        return JSONArray.parseArray(getValue(key), clazz);
    }

    /**
     * @see <a href="https://redis.io/commands/getdel">Redis Documentation: GETDEL</a>
     */
    public String getValueAndDelete(final String key){
        ValueOperations<String, String> operation = stringRedisTemplate.opsForValue();
        return operation.getAndDelete(key);
    }

    /**
     * @see <a href="https://redis.io/commands/getdel">Redis Documentation: GETDEL</a>
     */
    public <T> T getValueAndDelete(final String key, final Class<T> clazz){
        return JSON.parseObject(getValueAndDelete(key), clazz);
    }

    /**
     * @see <a href="https://redis.io/commands/getdel">Redis Documentation: GETDEL</a>
     */
    public <T> List<T> getArrayValueAndDelete(final String key, final Class<T> clazz){
        return JSONArray.parseArray(getValueAndDelete(key), clazz);
    }

    /**
     * @see <a href="https://redis.io/commands/getset">Redis Documentation: GETSET</a>
     */
    public String getValueAndPut(final String key, final String value){
        ValueOperations<String, String> operation = stringRedisTemplate.opsForValue();
        return operation.getAndSet(key, value);
    }

    /**
     * @see <a href="https://redis.io/commands/getset">Redis Documentation: GETSET</a>
     */
    public <T> T getValueAndPut(final String key, final String value, final Class<T> clazz){
        return JSON.parseObject(getValueAndPut(key, value), clazz);
    }

    /**
     * @see <a href="https://redis.io/commands/getset">Redis Documentation: GETSET</a>
     */
    public <T> List<T> getArrayValueAndPut(final String key, final String value, final Class<T> clazz){
        return JSON.parseArray(getValueAndPut(key, value), clazz);
    }

    /**
     * @see <a href="https://redis.io/commands/getex">Redis Documentation: GETEX</a>
     */
    public String getValueAndExpire(final String key, final long timeout, final TimeUnit timeUnit){
        ValueOperations<String, String> operation = stringRedisTemplate.opsForValue();
        return operation.getAndExpire(key, timeout, timeUnit);
    }

    /**
     * @see <a href="https://redis.io/commands/getex">Redis Documentation: GETEX</a>
     */
    public <T> T getValueAndExpire(final String key, final long timeout, final TimeUnit timeUnit, final Class<T> clazz){
        return JSON.parseObject(getValueAndExpire(key, timeout, timeUnit), clazz);
    }

    /**
     * @see <a href="https://redis.io/commands/getex">Redis Documentation: GETEX</a>
     */
    public <T> List<T> getArrayValueAndExpire(final String key, final long timeout, final TimeUnit timeUnit, final Class<T> clazz){
        return JSON.parseArray(getValueAndExpire(key, timeout, timeUnit), clazz);
    }

    /**
     * @see <a href="https://redis.io/commands/getex">Redis Documentation: GETEX</a>
     */
    public String getValueAndPersist(final String key){
        ValueOperations<String, String> operation = stringRedisTemplate.opsForValue();
        return operation.getAndPersist(key);
    }

    /**
     * @see <a href="https://redis.io/commands/getex">Redis Documentation: GETEX</a>
     */
    public <T> T getValueAndPersist(final String key, final Class<T> clazz){
        return JSON.parseObject(getValueAndPersist(key), clazz);
    }

    /**
     * @see <a href="https://redis.io/commands/getex">Redis Documentation: GETEX</a>
     */
    public <T> List<T> getArrayValueAndPersist(final String key, final Class<T> clazz){
        return JSON.parseArray(getValueAndPersist(key), clazz);
    }

    /**
     * @see <a href="https://redis.io/commands/mget">Redis Documentation: MGET</a>
     */
    public List<String> getMultiValue(final String... keys){
        ValueOperations<String, String> operation = stringRedisTemplate.opsForValue();
        return operation.multiGet(List.of(keys));
    }

    /**
     * @see <a href="https://redis.io/commands/mget">Redis Documentation: MGET</a>
     */
    public <T> List<T> getMultiValue(final Class<T> clazz, final String... keys){
        ValueOperations<String, String> operation = stringRedisTemplate.opsForValue();
        List<String> values = operation.multiGet(List.of(keys));
        if(CollectionUtils.isEmpty(values)){
            return java.util.Collections.emptyList();
        }
        return Collections.copyToList(values, v -> JSON.parseObject(v, clazz));
    }

    /**
     * @see <a href="https://redis.io/commands/mget">Redis Documentation: MGET</a>
     */
    public List<String> getMultiValue(final Collection<String> keys){
        ValueOperations<String, String> operation = stringRedisTemplate.opsForValue();
        return operation.multiGet(keys);
    }

    /**
     * @see <a href="https://redis.io/commands/mget">Redis Documentation: MGET</a>
     */
    public <T> List<T> getMultiValue(final Collection<String> keys, final Class<T> clazz){
        ValueOperations<String, String> operation = stringRedisTemplate.opsForValue();
        List<String> values = operation.multiGet(keys);
        if(CollectionUtils.isEmpty(values)){
            return java.util.Collections.emptyList();
        }
        return Collections.copyToList(values, v -> JSON.parseObject(v, clazz));
    }

    /**
     * @see <a href="https://redis.io/commands/set">Redis Documentation: SET</a>
     */
    public <T> void putValue(final String key, T value){
        stringRedisTemplate.opsForValue().set(key, toJson(value));
    }

    /**
     * @see <a href="https://redis.io/commands/setex">Redis Documentation: SETEX</a>
     */
    public <T> void putExpire(final String key, final T value, final Integer timeout, final TimeUnit timeUnit){
        stringRedisTemplate.opsForValue().set(key, toJson(value), timeout, timeUnit);
    }

    /**
     * @see <a href="https://redis.io/commands/setnx">Redis Documentation: SETNX</a>
     */
    public <T> Boolean putValueIfAbsent(final String key, final T value){
        Asserts.notNull(value, "redis value can't be bull");
        return stringRedisTemplate.opsForValue().setIfAbsent(key, toJson(value));
    }

    /**
     * @see <a href="https://redis.io/commands/set">Redis Documentation: SET</a>
     */
    public <T> Boolean putExpireIfAbsent(final String key, final T value, final long timeout, final TimeUnit timeUnit){
        Asserts.notNull(value, "redis value can't be bull");
        return stringRedisTemplate.opsForValue().setIfAbsent(key, toJson(value), timeout, timeUnit);
    }

    /**
     * @see <a href="https://redis.io/commands/set">Redis Documentation: SET</a>
     */
    public <T> Boolean putValueIfPresent(final String key, final T value){
        Asserts.notNull(value, "redis value can't be bull");
        return stringRedisTemplate.opsForValue().setIfPresent(key, toJson(value));
    }

    /**
     * @see <a href="https://redis.io/commands/set">Redis Documentation: SET</a>
     */
    public <T> Boolean putExpireIfPresent(final String key, final T value, final long timeout, final TimeUnit timeUnit){
        Asserts.notNull(value, "redis value can't be bull");
        return stringRedisTemplate.opsForValue().setIfPresent(key, toJson(value), timeout, timeUnit);
    }

    /**
     * @see <a href="https://redis.io/commands/mset">Redis Documentation: MSET</a>
     */
    public void putMultiValue(final Map<String, ?> map){
        if(map == null || map.isEmpty()) {
            return;
        }
        stringRedisTemplate.opsForValue().multiSet(Collections.copyToMap(
                map.entrySet(), Map.Entry::getKey, entry -> toJson(entry.getValue())));
    }

    /**
     * @see <a href="https://redis.io/commands/incrby">Redis Documentation: INCRBY</a>
     */
    public Long incrementValue(final String key, final int step){
        return stringRedisTemplate.opsForValue().increment(key, step);
    }

    /**
     * @see <a href="https://redis.io/commands/decrby">Redis Documentation: DECRBY</a>
     */
    public Long decrementValue(final String key, final int step){
        return stringRedisTemplate.opsForValue().decrement(key, step);
    }

    /* ******************************************
     * opsForHash
     * ******************************************/

    /**
     * @see <a href="https://redis.io/commands/hlen">Redis Documentation: HLEN</a>
     */
    public Long sizeOfMap(String key) {
        return stringRedisTemplate.opsForHash().size(key);
    }

    /**
     * @see <a href="https://redis.io/commands/hscan">Redis Documentation: HSCAN</a>
     */
    public Cursor<Map.Entry<String, String>> scanMap(String key, ScanOptions scanOptions) {
        HashOperations<String, String, String> hashOps = stringRedisTemplate.opsForHash();
        return hashOps.scan(key, scanOptions);
    }

    /**
     * @see <a href="https://redis.io/commands/hexits">Redis Documentation: HEXISTS</a>
     */
    public Boolean hasKeyInMap(final String key, final String hKey){
        return stringRedisTemplate.opsForHash().hasKey(key, hKey);
    }

    /**
     * @see <a href="https://redis.io/commands/hgetall">Redis Documentation: HGETALL</a>
     */
    public Map<String, String> getMap(final String key){
        HashOperations<String, String, String> operations = stringRedisTemplate.opsForHash();
        return operations.entries(key);
    }

    /**
     * @see <a href="https://redis.io/commands/hgetall">Redis Documentation: HGETALL</a>
     */
    public <T> Map<String, T> getMap(final String key, final Class<T> clazz){
        HashOperations<String, String, String> operations = stringRedisTemplate.opsForHash();
        Map<String, String> mapValue = operations.entries(key);
        return Collections.copyToMap(
                mapValue.entrySet(), Map.Entry::getKey, entry -> JSON.parseObject(entry.getValue(), clazz));
    }

    /**
     * @see <a href="https://redis.io/commands/hget">Redis Documentation: HGET</a>
     */
    public String getMap(final String key, final String hKey){
        HashOperations<String, String, String> opsForHash = stringRedisTemplate.opsForHash();
        return opsForHash.get(key, hKey);
    }

    /**
     * @see <a href="https://redis.io/commands/hget">Redis Documentation: HGET</a>
     */
    public <T> T getMap(final String key, final String hKey, final Class<T> clazz){
        HashOperations<String, String, String> opsForHash = stringRedisTemplate.opsForHash();
        String value = opsForHash.get(key, hKey);
        if(value != null){
            return JSON.parseObject(value, clazz);
        }
        return null;
    }

    /**
     * @see <a href="https://redis.io/commands/hmget">Redis Documentation: HMGET</a>
     */
    public List<String> getMultiMap(final String key, final Collection<String> hKeys){
        HashOperations<String, String, String> opsForHash = stringRedisTemplate.opsForHash();
        return opsForHash.multiGet(key, hKeys);
    }

    /**
     * @see <a href="https://redis.io/commands/hmget">Redis Documentation: HMGET</a>
     */
    public <T> List<T> getMultiMap(final String key, final Collection<String> hKeys, final Class<T> clazz){
        HashOperations<String, String, String> opsForHash = stringRedisTemplate.opsForHash();
        List<String> values = opsForHash.multiGet(key, hKeys);
        return Collections.copyToList(values, value -> JSON.parseObject(value, clazz));
    }

    /**
     * @see <a href="https://redis.io/commands/hset">Redis Documentation: HSET</a>
     */
    public <T> void putMap(final String key, final String hKey, final T value){
        stringRedisTemplate.opsForHash().put(key, hKey, toJson(value));
    }

    /**
     * @see <a href="https://redis.io/commands/hmset">Redis Documentation: HMSET</a>
     */
    public void putMap(final String key, final Map<String, ?> dataMap){
        if(dataMap == null || dataMap.isEmpty()) {
            return;
        }
        Map<String, String> stringMap = Collections.copyToMap(
                dataMap.entrySet(), Map.Entry::getKey, entry -> toJson(entry.getValue()));
        stringRedisTemplate.opsForHash().putAll(key, stringMap);
    }

    /**
     * @see <a href="https://redis.io/commands/hsetnx">Redis Documentation: HSETNX</a>
     */
    public Boolean putMapIfAbsent(final String key, final String hKey, final String value){
        HashOperations<String, String, String> hashOps = stringRedisTemplate.opsForHash();
        return hashOps.putIfAbsent(key, hKey, value);
    }

    /**
     * @see <a href="https://redis.io/commands/hincrby">Redis Documentation: HINCRBY</a>
     */
    public Long incrementMap(final String key, final String hKey, long delta) {
        return stringRedisTemplate.opsForHash().increment(key, hKey, delta);
    }

    /**
     * @see <a href="https://redis.io/commands/hdel">Redis Documentation: HDEL</a>
     */
    public void removeFromMap(final String key, final String hKey){
        HashOperations<String, String, String> hashOperations = stringRedisTemplate.opsForHash();
        hashOperations.delete(key, hKey);
    }

    /* ******************************************
     * opsForList
     * ******************************************/

    /**
     * @see <a href="https://redis.io/commands/llen">Redis Documentation: LLEN</a>
     */
    public Long sizeOfList(String key) {
        return stringRedisTemplate.opsForList().size(key);
    }

    /**
     * @see <a href="https://redis.io/commands/lpos">Redis Documentation: LPOS</a>
     */
    public <T> Long indexOfList(final String key, final T value){
        return stringRedisTemplate.opsForList().indexOf(key, toJson(value));
    }

    /**
     * @see <a href="https://redis.io/commands/lpos">Redis Documentation: LPOS</a>
     */
    public <T> Long lastIndexOfList(final String key, final T value){
        return stringRedisTemplate.opsForList().lastIndexOf(key, toJson(value));
    }

    /**
     * @see <a href="https://redis.io/commands/lindex">Redis Documentation: LINDEX</a>
     */
    public String indexValueOfList(final String key, final long index){
        return stringRedisTemplate.opsForList().index(key, index);
    }

    /**
     * @see <a href="https://redis.io/commands/lindex">Redis Documentation: LINDEX</a>
     */
    public <T> T indexValueOfList(final String key, final long index, final Class<T> clazz){
        String value = stringRedisTemplate.opsForList().index(key, index);
        return JSON.parseObject(value, clazz);
    }

    /**
     * @see <a href="https://redis.io/commands/lrange">Redis Documentation: LRANGE</a>
     */
    public List<String> rangeOfList(final String key, int start, int end){
        return stringRedisTemplate.opsForList().range(key, start, end);
    }

    /**
     * @see <a href="https://redis.io/commands/lrange">Redis Documentation: LRANGE</a>
     */
    public <T> List<T> rangeOfList(final String key, int start, int end, final Class<T> clazz){
        List<String> values = stringRedisTemplate.opsForList().range(key, start, end);
        return Collections.copyToList(values, value -> JSON.parseObject(value, clazz));
    }

    /**
     * @see <a href="https://redis.io/commands/lset">Redis Documentation: LSET</a>
     */
    public <T> void insertListByIndex(final String key, final long index, T value){
        stringRedisTemplate.opsForList().set(key, index, toJson(value));
    }

    /**
     * @see <a href="https://redis.io/commands/linsert">Redis Documentation: LINSERT</a>
     */
    public <T> long insertListBefore(final String key, final T pivot, final T value){
        Long count = stringRedisTemplate.opsForList().leftPush(key, toJson(pivot), toJson(value));
        return count == null ? 0 : count;
    }

    /**
     * @see <a href="https://redis.io/commands/linsert">Redis Documentation: LINSERT</a>
     */
    public <T> long insertListAfter(final String key, final T pivot, final T value){
        Long count = stringRedisTemplate.opsForList().rightPush(key, toJson(pivot), toJson(value));
        return count == null ? 0 : count;
    }

    /**
     * @see <a href="https://redis.io/commands/lpush">Redis Documentation: LPUSH</a>
     */
    public <T> long pushListFromLeft(final String key, final T value){
        Long count = stringRedisTemplate.opsForList().leftPush(key, toJson(value));
        return count == null ? 0 : count;
    }

    /**
     * @see <a href="https://redis.io/commands/lpushx">Redis Documentation: LPUSHX</a>
     */
    public <T> long pushListFromLeftIfPresent(final String key, final T value){
        Long count = stringRedisTemplate.opsForList().leftPushIfPresent(key, toJson(value));
        return count == null ? 0 : count;
    }

    /**
     * @see <a href="https://redis.io/commands/lpush">Redis Documentation: LPUSH</a>
     */
    public long pushAllListFromLeft(final String key, final Object... values){
        Long count = stringRedisTemplate.opsForList().leftPushAll(key, Collections.arrayToList(values, this::toJson));
        return count == null ? 0 : count;
    }

    /**
     * @see <a href="https://redis.io/commands/lpush">Redis Documentation: LPUSH</a>
     */
    public long pushAllListFromLeft(final String key, final Collection<Object> values){
        Long count = stringRedisTemplate.opsForList().leftPushAll(key, Collections.copyToList(values, this::toJson));
        return count == null ? 0 : count;
    }

    /**
     * @see <a href="https://redis.io/commands/rpush">Redis Documentation: RPUSH</a>
     */
    public <T> long pushListFromRight(final String key, final T value){
        Long count = stringRedisTemplate.opsForList().rightPush(key, toJson(value));
        return count == null ? 0 : count;
    }

    /**
     * @see <a href="https://redis.io/commands/rpushx">Redis Documentation: RPUSHX</a>
     */
    public <T> long pushListFromRightIfPresent(final String key, final T value){
        Long count = stringRedisTemplate.opsForList().rightPushIfPresent(key, toJson(value));
        return count == null ? 0 : count;
    }

    /**
     * @see <a href="https://redis.io/commands/rpush">Redis Documentation: RPUSH</a>
     */
    public final <T> long pushAllListFromRight(final String key, final T... values){
        Long count = stringRedisTemplate.opsForList().rightPushAll(key, Collections.arrayToList(values, this::toJson));
        return count == null ? 0 : count;
    }

    /**
     * @see <a href="https://redis.io/commands/rpush">Redis Documentation: RPUSH</a>
     */
    public long pushAllListFromRight(final String key, final Collection<Object> values){
        Long count = stringRedisTemplate.opsForList().rightPushAll(key, Collections.copyToList(values, this::toJson));
        return count == null ? 0 : count;
    }

    /**
     * @see <a href="https://redis.io/commands/lpop">Redis Documentation: LPOP</a>
     */
    public String popListFromLeft(final String key){
        return stringRedisTemplate.opsForList().leftPop(key);
    }

    /**
     * @see <a href="https://redis.io/commands/lpop">Redis Documentation: LPOP</a>
     */
    public <T> T popListFromLeft(final String key, final Class<T> clazz){
        return JSON.parseObject(popListFromLeft(key), clazz);
    }

    /**
     * @see <a href="https://redis.io/commands/blpop">Redis Documentation: BLPOP</a>
     */
    public String popListFromLeft(final String key, final long timeout, final TimeUnit timeUnit){
        return stringRedisTemplate.opsForList().leftPop(key, timeout, timeUnit);
    }

    /**
     * @see <a href="https://redis.io/commands/blpop">Redis Documentation: BLPOP</a>
     */
    public <T> T popListFromLeft(final String key, final long timeout, final TimeUnit timeUnit, final Class<T> clazz){
        return JSON.parseObject(popListFromLeft(key, timeout, timeUnit), clazz);
    }

    /**
     * @see <a href="https://redis.io/commands/rpop">Redis Documentation: RPOP</a>
     */
    public String popListFromRight(final String key){
        return stringRedisTemplate.opsForList().rightPop(key);
    }

    /**
     * @see <a href="https://redis.io/commands/rpop">Redis Documentation: RPOP</a>
     */
    public <T> T popListFromRight(final String key, final Class<T> clazz){
        return JSON.parseObject(popListFromRight(key), clazz);
    }

    /**
     * @see <a href="https://redis.io/commands/brpop">Redis Documentation: BRPOP</a>
     */
    public String popListFromRight(final String key, final long timeout, final TimeUnit timeUnit){
        return stringRedisTemplate.opsForList().rightPop(key, timeout, timeUnit);
    }

    /**
     * @see <a href="https://redis.io/commands/brpop">Redis Documentation: BRPOP</a>
     */
    public <T> T popListFromRight(final String key, final long timeout, final TimeUnit timeUnit, final Class<T> clazz){
        return JSON.parseObject(popListFromRight(key, timeout, timeUnit), clazz);
    }

    /**
     * @see <a href="https://redis.io/commands/rpoplpush">Redis Documentation: RPOPLPUSH</a>
     */
    public String popListFromRightToLeft(String rightKey, String leftKey){
        return stringRedisTemplate.opsForList().rightPopAndLeftPush(rightKey, leftKey);
    }

    /**
     * @see <a href="https://redis.io/commands/rpoplpush">Redis Documentation: RPOPLPUSH</a>
     */
    public <T> T popListFromRightToLeft(String rightKey, String leftKey, final Class<T> clazz){
        return JSON.parseObject(popListFromRightToLeft(rightKey, leftKey), clazz);
    }

    /**
     * @see <a href="https://redis.io/commands/brpoplpush">Redis Documentation: BRPOPLPUSH</a>
     */
    public String popListFromRightToLeft(String rightKey, String leftKey, long timeout, TimeUnit timeUnit){
        return stringRedisTemplate.opsForList().rightPopAndLeftPush(rightKey, leftKey, timeout, timeUnit);
    }

    /**
     * @see <a href="https://redis.io/commands/brpoplpush">Redis Documentation: BRPOPLPUSH</a>
     */
    public <T> T popListFromRightToLeft(String rightKey, String leftKey, long timeout, TimeUnit timeUnit, final Class<T> clazz){
        return JSON.parseObject(popListFromRightToLeft(rightKey, leftKey, timeout, timeUnit), clazz);
    }

    /**
     * @see <a href="https://redis.io/commands/lmove">Redis Documentation: LMOVE</a>
     */
    public String moveList(String srcKey, RedisListCommands.Direction from, String destKey, RedisListCommands.Direction to){
        return stringRedisTemplate.opsForList().move(srcKey, from, destKey, to);
    }

    /**
     * @see <a href="https://redis.io/commands/lmove">Redis Documentation: LMOVE</a>
     */
    public <T> T moveList(String srcKey, RedisListCommands.Direction from, String destKey, RedisListCommands.Direction to, final Class<T> clazz){
        return JSON.parseObject(moveList(srcKey, from, destKey, to), clazz);
    }

    /**
     * @see <a href="https://redis.io/commands/blmove">Redis Documentation: BLMOVE</a>
     */
    public String moveList(String srcKey, RedisListCommands.Direction from, String destKey, RedisListCommands.Direction to, long timeout, TimeUnit timeUnit){
        return stringRedisTemplate.opsForList().move(srcKey, from, destKey, to, timeout, timeUnit);
    }

    /**
     * @see <a href="https://redis.io/commands/blmove">Redis Documentation: BLMOVE</a>
     */
    public <T> T moveList(String srcKey, RedisListCommands.Direction from, String destKey, RedisListCommands.Direction to, long timeout, TimeUnit timeUnit, final Class<T> clazz){
        return JSON.parseObject(moveList(srcKey, from, destKey, to, timeout, timeUnit), clazz);
    }

    /**
     * @see <a href="https://redis.io/commands/lrem">Redis Documentation: LREM</a>
     */
    public <T> Long removeFromList(final String key, final T value, final long count){
        return stringRedisTemplate.opsForList().remove(key, count, toJson(value));
    }

    /* ******************************************
     * opsForSet
     * ******************************************/

    /**
     * @see <a href="https://redis.io/commands/scard">Redis Documentation: SCARD</a>
     */
    public Long sizeOfSet(final String key) {
        return stringRedisTemplate.opsForSet().size(key);
    }

    /**
     * @see <a href="https://redis.io/commands/scan">Redis Documentation: SCAN</a>
     */
    public Cursor<String> scanSet(String key, ScanOptions scanOptions) {
        return stringRedisTemplate.opsForSet().scan(key, scanOptions);
    }

    /**
     * @see <a href="https://redis.io/commands/sismember">Redis Documentation: SISMEMBER</a>
     */
    public <T> Boolean memberOfSet(String key, T member) {
        return stringRedisTemplate.opsForSet().isMember(key, toJson(member));
    }

    /**
     * @see <a href="https://redis.io/commands/smembers">Redis Documentation: SMEMBERS</a>
     */
    public Set<String> getSet(final String key){
        return stringRedisTemplate.opsForSet().members(key);
    }

    /**
     * @see <a href="https://redis.io/commands/smembers">Redis Documentation: SMEMBERS</a>
     */
    public <T> Set<T> getSet(final String key, final Class<T> clazz){
        Set<String> values = stringRedisTemplate.opsForSet().members(key);
        return Collections.copyToSet(values, v -> JSON.parseObject(v, clazz));
    }

    /**
     * @see <a href="https://redis.io/commands/sadd">Redis Documentation: SADD</a>
     */
    public <T> void offerSet(final String key, final Set<T> dataSet){
        Asserts.notNull(dataSet, "redis value can't be bull");
        BoundSetOperations<String, String> setOperation = stringRedisTemplate.boundSetOps(key);
        for (T data : dataSet) {
            setOperation.add(toJson(data));
        }
    }

    /**
     * @see <a href="https://redis.io/commands/sadd">Redis Documentation: SADD</a>
     */
    public <T> void offerSet(final String key, final T... values){
        Asserts.notNull(values, "redis value can't be bull");
        BoundSetOperations<String, String> setOperation = stringRedisTemplate.boundSetOps(key);
        for (T t : values) {
            setOperation.add(toJson(t));
        }
    }

    /**
     * @see <a href="https://redis.io/commands/spop">Redis Documentation: SPOP</a>
     */
    public List<String> popSet(final String key, final int count){
        return stringRedisTemplate.opsForSet().pop(key, count);
    }

    /**
     * @see <a href="https://redis.io/commands/spop">Redis Documentation: SPOP</a>
     */
    public <T> List<T> popSet(final String key, final int count, final Class<T> clazz){
        List<String> list = stringRedisTemplate.opsForSet().pop(key, count);
        return Collections.copyToList(list, v -> JSON.parseObject(v, clazz));
    }

    /**
     * @see <a href="https://redis.io/commands/sinter">Redis Documentation: SINTER</a>
     */
    public Set<String> intersectSet(Collection<String> keys){
        return stringRedisTemplate.opsForSet().intersect(keys);
    }

    /**
     * @see <a href="https://redis.io/commands/sinter">Redis Documentation: SINTER</a>
     */
    public Set<String> intersectSet(String key, Collection<String> others){
        return stringRedisTemplate.opsForSet().intersect(key, others);
    }

    /**
     * @see <a href="https://redis.io/commands/sinter">Redis Documentation: SINTER</a>
     */
    public Set<String> intersectSet(String key, String... others){
        return stringRedisTemplate.opsForSet().intersect(key, List.of(others));
    }

    /**
     * @see <a href="https://redis.io/commands/sinter">Redis Documentation: SINTER</a>
     */
    public <T> Set<T> intersectSet(Collection<String> keys, final Class<T> clazz){
        Set<String> set = stringRedisTemplate.opsForSet().intersect(keys);
        if(CollectionUtils.isEmpty(set)){
            return java.util.Collections.emptySet();
        }
        return Collections.copyToSet(set, v -> JSON.parseObject(v, clazz));
    }

    /**
     * @see <a href="https://redis.io/commands/sinter">Redis Documentation: SINTER</a>
     */
    public <T> Set<T> intersectSet(String key, Collection<String> others, final Class<T> clazz){
        Set<String> set = stringRedisTemplate.opsForSet().intersect(key, others);
        if(CollectionUtils.isEmpty(set)){
            return java.util.Collections.emptySet();
        }
        return Collections.copyToSet(set, v -> JSON.parseObject(v, clazz));
    }

    /**
     * @see <a href="https://redis.io/commands/sinter">Redis Documentation: SINTER</a>
     */
    public <T> Set<T> intersectSet(final Class<T> clazz, String key, String... others){
        Set<String> set = stringRedisTemplate.opsForSet().intersect(key, List.of(others));
        if(CollectionUtils.isEmpty(set)){
            return java.util.Collections.emptySet();
        }
        return Collections.copyToSet(set, v -> JSON.parseObject(v, clazz));
    }

    /**
     * @see <a href="https://redis.io/commands/sinterstore">Redis Documentation: SINTERSTORE</a>
     */
    public Long intersectSetAndStore(String destKey, Collection<String> keys){
        return stringRedisTemplate.opsForSet().intersectAndStore(keys, destKey);
    }

    /**
     * @see <a href="https://redis.io/commands/sinterstore">Redis Documentation: SINTERSTORE</a>
     */
    public Long intersectSetAndStore(String destKey, String... keys){
        return stringRedisTemplate.opsForSet().intersectAndStore(List.of(keys), destKey);
    }

    /**
     * @see <a href="https://redis.io/commands/sunion">Redis Documentation: SUNION</a>
     */
    public Set<String> unionSet(Collection<String> keys){
        return stringRedisTemplate.opsForSet().union(keys);
    }

    /**
     * @see <a href="https://redis.io/commands/sunion">Redis Documentation: SUNION</a>
     */
    public Set<String> unionSet(String key, Collection<String> others){
        return stringRedisTemplate.opsForSet().union(key, others);
    }

    /**
     * @see <a href="https://redis.io/commands/sunion">Redis Documentation: SUNION</a>
     */
    public Set<String> unionSet(String key, String... others){
        return stringRedisTemplate.opsForSet().union(key, List.of(others));
    }

    /**
     * @see <a href="https://redis.io/commands/sunion">Redis Documentation: SUNION</a>
     */
    public <T> Set<T> unionSet(Collection<String> keys, final Class<T> clazz){
        Set<String> set = stringRedisTemplate.opsForSet().union(keys);
        if(CollectionUtils.isEmpty(set)){
            return java.util.Collections.emptySet();
        }
        return Collections.copyToSet(set, v -> JSON.parseObject(v, clazz));
    }

    /**
     * @see <a href="https://redis.io/commands/sunion">Redis Documentation: SUNION</a>
     */
    public <T> Set<T> unionSet(String key, Collection<String> others, final Class<T> clazz){
        Set<String> set = stringRedisTemplate.opsForSet().union(key, others);
        if(CollectionUtils.isEmpty(set)){
            return java.util.Collections.emptySet();
        }
        return Collections.copyToSet(set, v -> JSON.parseObject(v, clazz));
    }

    /**
     * @see <a href="https://redis.io/commands/sunion">Redis Documentation: SUNION</a>
     */
    public <T> Set<T> unionSet(final Class<T> clazz, String key, String... others){
        Set<String> set = stringRedisTemplate.opsForSet().union(key, List.of(others));
        if(CollectionUtils.isEmpty(set)){
            return java.util.Collections.emptySet();
        }
        return Collections.copyToSet(set, v -> JSON.parseObject(v, clazz));
    }

    /**
     * @see <a href="https://redis.io/commands/sunionstore">Redis Documentation: SUNIONSTORE</a>
     */
    public Long unionSetAndStore(String destKey, Collection<String> keys){
        return stringRedisTemplate.opsForSet().unionAndStore(keys, destKey);
    }

    /**
     * @see <a href="https://redis.io/commands/sunionstore">Redis Documentation: SUNIONSTORE</a>
     */
    public Long unionSetAndStore(String destKey, String... keys){
        return stringRedisTemplate.opsForSet().unionAndStore(List.of(keys), destKey);
    }

    /**
     * @see <a href="https://redis.io/commands/sdiff">Redis Documentation: SDIFF</a>
     */
    public Set<String> diffSet(String key, String... others){
        return stringRedisTemplate.opsForSet().difference(key, List.of(others));
    }

    /**
     * @see <a href="https://redis.io/commands/sdiff">Redis Documentation: SDIFF</a>
     */
    public Set<String> diffSet(String key, Collection<String> others){
        return stringRedisTemplate.opsForSet().difference(key, others);
    }

    /**
     * @see <a href="https://redis.io/commands/sdiff">Redis Documentation: SDIFF</a>
     */
    public <T> Set<T> diffSet(final Class<T> clazz, String key, String... others){
        Set<String> set = stringRedisTemplate.opsForSet().difference(key, List.of(others));
        if(CollectionUtils.isEmpty(set)){
            return java.util.Collections.emptySet();
        }
        return Collections.copyToSet(set, v -> JSON.parseObject(v, clazz));
    }

    /**
     * @see <a href="https://redis.io/commands/sdiff">Redis Documentation: SDIFF</a>
     */
    public <T> Set<T> diffSet(String key, Collection<String> others, final Class<T> clazz){
        Set<String> set = stringRedisTemplate.opsForSet().difference(key, others);
        if(CollectionUtils.isEmpty(set)){
            return java.util.Collections.emptySet();
        }
        return Collections.copyToSet(set, v -> JSON.parseObject(v, clazz));
    }

    /**
     * @see <a href="https://redis.io/commands/sdiffstore">Redis Documentation: SDIFFSTORE</a>
     */
    public Long diffSetAndStore(String destKey, String key, String... others){
        return stringRedisTemplate.opsForSet().differenceAndStore(key, List.of(others), destKey);
    }

    /**
     * @see <a href="https://redis.io/commands/sdiffstore">Redis Documentation: SDIFFSTORE</a>
     */
    public Long diffSetAndStore(String destKey, String key, Collection<String> others){
        return stringRedisTemplate.opsForSet().differenceAndStore(key, others, destKey);
    }

    /**
     * @see <a href="https://redis.io/commands/srem">Redis Documentation: SREM</a>
     */
    public void removeFromSet(final String key, final Object... values){
        BoundSetOperations<String, String> setOperation = stringRedisTemplate.boundSetOps(key);
        setOperation.remove(values);
    }

    /* ******************************************
     * opsForZSet
     * ******************************************/

    /**
     * @see <a href="https://redis.io/commands/zcard">Redis Documentation: ZCARD</a>
     */
    public Long sizeOfZset(final String key) {
        return stringRedisTemplate.opsForZSet().size(key);
    }

    /**
     * @see <a href="https://redis.io/commands/zcount">Redis Documentation: ZCOUNT</a>
     */
    public Long countZsetByScore(final String key, double min, double max) {
        return stringRedisTemplate.opsForZSet().count(key, min, max);
    }

    /**
     * @see <a href="https://redis.io/commands/zrank">Redis Documentation: ZRANK</a>
     */
    public <T> Long rankOfZset(final String key, final T value){
        return stringRedisTemplate.opsForZSet().rank(key, toJson(value));
    }

    /**
     * @see <a href="https://redis.io/commands/zscore">Redis Documentation: ZSCORE</a>
     */
    public <T> Boolean memberOfZset(final String key, final T value) {
        return stringRedisTemplate.opsForZSet().score(key, toJson(value)) != null;
    }

    /**
     * @see <a href="https://redis.io/commands/zrange">Redis Documentation: ZRANGE</a>
     */
    public String firstOfZset(final String key){
        Set<String> set = stringRedisTemplate.opsForZSet().range(key, 0, 0);
        if (set != null && !set.isEmpty()) {
            return set.iterator().next();
        }
        return null;
    }

    /**
     * @see <a href="https://redis.io/commands/zrange">Redis Documentation: ZRANGE</a>
     */
    public <T> T firstOfZset(final String key, final Class<T> clazz){
        Set<String> set = stringRedisTemplate.opsForZSet().range(key, 0, 0);
        if (set != null && !set.isEmpty()) {
            return JSON.parseObject(set.iterator().next(), clazz);
        }
        return null;
    }

    /**
     * @see <a href="https://redis.io/commands/zrange">Redis Documentation: ZRANGE</a>
     */
    public Set<String> rangeOfZset(final String key, final long start, final long end) {
        return stringRedisTemplate.opsForZSet().range(key, start, end);
    }

    /**
     * @see <a href="https://redis.io/commands/zrange">Redis Documentation: ZRANGE</a>
     */
    public <T> Set<T> rangeOfZset(final String key, final long start, final long end, final Class<T> clazz){
        Set<String> values = stringRedisTemplate.opsForZSet().range(key, start, end);
        if(CollectionUtils.isEmpty(values)){
            return java.util.Collections.emptySet();
        }
        return Collections.copyToSet(values, v -> JSON.parseObject(v, clazz));
    }

    /**
     * @see <a href="https://redis.io/commands/zrangebyscore">Redis Documentation: ZRANGEBYSCORE</a>
     */
    public Set<String> rangeOfZsetByScore(final String key, double min, double max){
        return stringRedisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    /**
     * @see <a href="https://redis.io/commands/zrangebyscore">Redis Documentation: ZRANGEBYSCORE</a>
     */
    public <T> Set<T> rangeOfZsetByScore(final String key, double min, double max, final Class<T> clazz){
        Set<String> values = stringRedisTemplate.opsForZSet().rangeByScore(key, min, max);
        if(CollectionUtils.isEmpty(values)){
            return java.util.Collections.emptySet();
        }
        return Collections.copyToSet(values, v -> JSON.parseObject(v, clazz));
    }

    /**
     * @see <a href="https://redis.io/commands/zpopmin">Redis Documentation: ZPOPMIN</a>
     */
    public ZSetOperations.TypedTuple<String> popMinOfZset(final String key){
        return stringRedisTemplate.opsForZSet().popMin(key);
    }

    /**
     * @see <a href="https://redis.io/commands/bzpopmin">Redis Documentation: BZPOPMIN</a>
     */
    public ZSetOperations.TypedTuple<String> popMinOfZset(final String key, long timeout, TimeUnit timeUnit){
        return stringRedisTemplate.opsForZSet().popMin(key, timeout, timeUnit);
    }

    /**
     * @see <a href="https://redis.io/commands/zpopmin">Redis Documentation: ZPOPMAX</a>
     */
    public ZSetOperations.TypedTuple<String> popMaxOfZset(final String key){
        return stringRedisTemplate.opsForZSet().popMax(key);
    }

    /**
     * @see <a href="https://redis.io/commands/bzpopmin">Redis Documentation: BZPOPMAX</a>
     */
    public ZSetOperations.TypedTuple<String> popMaxOfZset(final String key, long timeout, TimeUnit timeUnit){
        return stringRedisTemplate.opsForZSet().popMax(key, timeout, timeUnit);
    }

    /**
     * @see <a href="https://redis.io/commands/zadd">Redis Documentation: ZADD</a>
     */
    public <T> void putZset(final String key, final T value, final double score){
        stringRedisTemplate.opsForZSet().add(key, toJson(value), score);
    }

    /**
     * @see <a href="https://redis.io/commands/zrem">Redis Documentation: ZREM</a>
     */
    public void removeFromZset(final String key, final Object... values){
        stringRedisTemplate.opsForZSet().remove(key, values);
    }

    /**
     * @see <a href="https://redis.io/commands/zremrangebyscore">Redis Documentation: ZREMRANGEBYSCORE</a>
     */
    public void removeFromZsetByScore(final String key, final double min, final double max){
        stringRedisTemplate.opsForZSet().removeRangeByScore(key, min, max);
    }

    /* ******************************************
     * opsForStream
     * ******************************************/

    public StreamInfo.XInfoStream streamInfo(String key){
        return stringRedisTemplate.opsForStream().info(key);
    }

    public String createStreamGroup(String key, String group) {
        return stringRedisTemplate.opsForStream().createGroup(key, group);
    }

    /**
     * @see <a href="https://redis.io/commands/xadd">Redis Documentation: XADD</a>
     */
    public <T> RecordId publishStream(Record<String, T> record) {
        return stringRedisTemplate.opsForStream().add(record);
    }

    /**
     * @see <a href="https://redis.io/commands/xread">Redis Documentation: XREAD</a>
     */
    public List<MapRecord<String, Object, Object>> subscribeStream(Consumer consumer, StreamReadOptions readOptions, StreamOffset<String>... streams){
        return stringRedisTemplate.opsForStream().read(consumer, readOptions, streams);
    }

    /**
     * @see <a href="https://redis.io/commands/xread">Redis Documentation: XREAD</a>
     */
    public <V> List<ObjectRecord<String, V>> subscribeStream(Class<V> targetType, Consumer consumer, StreamReadOptions readOptions, StreamOffset<String>... streams) {
        return stringRedisTemplate.opsForStream().read(targetType, consumer, readOptions, streams);
    }

    /**
     * @see <a href="https://redis.io/commands/xack">Redis Documentation: XACK</a>
     */
    public Long ackStream(String key, String group, RecordId... recordIds){
        return stringRedisTemplate.opsForStream().acknowledge(key, group, recordIds);
    }

    /**
     * @see <a href="https://redis.io/commands/xdel">Redis Documentation: XDEL</a>
     */
    public Long deleteFromStream(String key, RecordId... recordIds){
        return stringRedisTemplate.opsForStream().delete(key, recordIds);
    }

    /* ******************************************
     * channel
     * ******************************************/

    /**
     * @see <a href="https://redis.io/commands/publish">Redis Documentation: PUBLISH</a>
     */
    public <T> void sendChannel(String channel, T message) {
        stringRedisTemplate.convertAndSend(channel, toJson(message));
    }

    private String toJson(Object value){
        Asserts.notNull(value, "redis value can't be bull");
        if(String.class.isAssignableFrom(value.getClass())){
            return value.toString();
        }else{
            return JSON.toJSONString(value);
        }
    }
}
