/*
 * Copyright (c) 2017～2024 Cowave All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.commons.framework.support.redis.dict;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.cowave.commons.framework.support.redis.RedisHelper;
import com.cowave.commons.framework.support.redis.StringRedisHelper;
import com.cowave.commons.tools.AssertsException;
import org.apache.commons.lang3.StringUtils;

import lombok.RequiredArgsConstructor;

/**
 *
 * @author shanhuiming
 *
 */
@RequiredArgsConstructor
public class DictHelper {

    private final Map<String, DictValueParser> parserMap = new ConcurrentHashMap<>();

    private final RedisHelper redisHelper;

    private final StringRedisHelper stringRedisHelper;

    private String getGroupKey(String namespace){
        if(StringUtils.isBlank(namespace)){
            return "dict:group:";
        }else if(namespace.endsWith(":")){
            return namespace + "dict:group:";
        }else{
            return namespace + ":dict:group:";
        }
    }

    private String getTypeKey(String namespace){
        if(StringUtils.isBlank(namespace)){
            return "dict:type:";
        }else if(namespace.endsWith(":")){
            return namespace + "dict:type:";
        }else{
            return namespace + ":dict:type:";
        }
    }

    private String getDictKey(String namespace){
        if(StringUtils.isBlank(namespace)){
            return "dict:code:";
        }else if(namespace.endsWith(":")){
            return namespace + "dict:code:";
        }else{
            return namespace + ":dict:code:";
        }
    }

    /**
     * 存入字典缓存
     */
    public void put(String namespace, Dict dict) {
        if(dict.getGroupCode() == null){
            throw new AssertsException("{frame.dict.notnull.groupcode}");
        }
        if(dict.getTypeCode() == null){
            throw new AssertsException("{frame.dict.notnull.typecode}");
        }
        if(dict.getDictCode() == null){
            throw new AssertsException("{frame.dict.notnull.code}");
        }

        String parserClazz = dict.getValueParser();
        if(StringUtils.isNotBlank(parserClazz)){
            DictValueParser parser = parserMap.get(parserClazz);
            if(parser == null){
                try{
                    parser = (DictValueParser)Class.forName(parserClazz).getDeclaredConstructor().newInstance();
                }catch(Exception e){
                    throw new AssertsException("{frame.dict.parse.failed}", dict.getDictCode());
                }
                parserMap.put(parserClazz, parser);
            }
            dict.setDictValue(parser.parse(String.valueOf(dict.getDictValue()), dict.getValueParam()));

        }

        if(!"dict_root".equals(dict.getTypeCode())){
            redisHelper.putMap(getGroupKey(namespace) + dict.getGroupCode(), dict.getDictCode(), dict);
            if(!"dict_root".equals(dict.getGroupCode())){
                redisHelper.putMap(getTypeKey(namespace) + dict.getTypeCode(), dict.getDictCode(), dict);
            }
        }
        redisHelper.putValue(getDictKey(namespace) + dict.getDictCode(), dict);
    }

    /**
     * 获取某个分组字典
     */
    public <T extends Dict> List<T> getGroup(String namespace, String groupCode) {
        if(StringUtils.isBlank(groupCode)){
            return new ArrayList<>();
        }
        Map<String, T> map = redisHelper.getMap(getGroupKey(namespace) + groupCode);
        if(map == null){
            return new ArrayList<>();
        }
        List<T> list = new ArrayList<>(map.values());
        list.sort(Comparator.comparingInt(Dict::getDictOrder));
        return list;
    }

    /**
     * 获取某个类型字典
     */
    public <T extends Dict> List<T> getType(String namespace, String typeCode) {
        if(StringUtils.isBlank(typeCode)){
            return new ArrayList<>();
        }
        Map<String, T> map = redisHelper.getMap(getTypeKey(namespace) + typeCode);
        if(map == null){
            return new ArrayList<>();
        }
        List<T> list = new ArrayList<>(map.values());
        list.sort(Comparator.comparingInt(Dict::getDictOrder));
        return list;
    }

    /**
     * 获取字典
     */
    public <T extends Dict> T getDict(String namespace, String dictCode) {
        if(StringUtils.isBlank(dictCode)){
            return null;
        }
        return redisHelper.getValue(getDictKey(namespace) + dictCode);
    }

    /**
     * 获取字典Label
     */
    public String getDictLabel(String namespace, String dictCode) {
        Dict dict = getDict(dictCode, namespace);
        if(dict == null){
            return null;
        }
        return dict.getDictLabel();
    }

    /**
     * 获取字典值
     */
    public <T> T getDictValue(String namespace, String dictCode) {
        Dict dict = getDict(dictCode, namespace);
        if(dict == null){
            return null;
        }
        return (T)dict.getDictValue();
    }

    /**
     * 删除字典
     */
    public void removeDict(String namespace, String dictCode) {
        if(StringUtils.isBlank(dictCode)){
            return;
        }
        Dict dict = redisHelper.getValue(getDictKey(namespace) + dictCode);
        if(dict == null){
            return;
        }
        redisHelper.delete(getDictKey(namespace) + dictCode);
        redisHelper.deleteMap(getTypeKey(namespace) + dict.getTypeCode(), dictCode);
        redisHelper.deleteMap(getGroupKey(namespace) + dict.getGroupCode(), dictCode);
    }

    /**
     * 删除类型
     *
     * <p>删除sys-dict:dict:{dictCode}
     * <p>从sys-dict:dict:{groupCode}中删除
     *
     * <p>删除sys-dict:type:{typeCode}
     * <p>从sys-dict:group:dict_group中删除类型
     */
    public void removeType(String namespace, String typeCode) {
        if(StringUtils.isBlank(typeCode)){
            return;
        }
        Map<String, Dict> dictMap = redisHelper.getMap(getTypeKey(namespace) + typeCode);
        if(dictMap != null){
            for(Dict dict : dictMap.values()){
                redisHelper.delete(getDictKey(namespace) + dict.getDictCode());
                redisHelper.deleteMap(getGroupKey(namespace) + dict.getGroupCode(), dict.getDictCode());
            }
        }
        redisHelper.delete(getTypeKey(namespace) + typeCode);
        redisHelper.deleteMap(getGroupKey(namespace) + "dict_group", typeCode);
    }

    /**
     * 删除分组
     *
     * <p>删除sys-dict:type:{groupCode}
     * <p>从sys-dict:group:dict_group中删除类型
     *
     * <p>删除sys-dict:dict:{dictCode}
     *
     * <p>删除sys-dict:group:{groupCode}
     * <p>从sys-dict:group:dict_root中删除分组
     */
    public void removeGroup(String namespace, String groupCode) {
        if(StringUtils.isBlank(groupCode)){
            return;
        }
        Map<String, Dict> typeMap = redisHelper.getMap(getTypeKey(namespace) + groupCode);
        if(typeMap != null){
            redisHelper.delete(getTypeKey(namespace) + groupCode);
            for(Dict type : typeMap.values()){
                redisHelper.deleteMap(getGroupKey(namespace) + "dict_group", type.getDictCode());
            }
        }
        Map<String, Dict> dictMap = redisHelper.getMap(getGroupKey(namespace) + groupCode);
        if(dictMap != null){
            for(Dict dict : dictMap.values()){ //
                redisHelper.delete(getDictKey(namespace) + dict.getDictCode());
            }
        }
        redisHelper.delete(getGroupKey(namespace) + groupCode);
        redisHelper.deleteMap(getGroupKey(namespace) + "dict_root", groupCode);
    }

    /**
     * 清空字典
     */
    public void clear(String namespace) {
        if(StringUtils.isBlank(namespace)){
            namespace = "dict:*:";
        }else if(namespace.endsWith(":")){
            namespace = namespace + "dict:*";
        }else{
            namespace = namespace + ":dict:*";
        }
        stringRedisHelper.luaClean(namespace);
    }
}
