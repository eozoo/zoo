/*
 * Copyright (c) 2017～2024 Cowave All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.commons.framework.helper.dict;

import java.util.*;

import com.cowave.commons.framework.support.redis.RedisHelper;
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
	private static final String KEY = "sys-dict:";
    private static final String KEY_DICT = "sys-dict:dict:";
    private static final String KEY_TYPE = "sys-dict:type:";
    private static final String KEY_GROUP = "sys-dict:group:";

    private final RedisHelper redisHelper;

    /**
     * 存入字典缓存
     */
    public void put(Dict dict) {
        if(dict.getGroupCode() == null){
            throw new AssertsException("{frame.dict.notnull.groupcode}");
        }
        if(dict.getTypeCode() == null){
            throw new AssertsException("{frame.dict.notnull.typecode}");
        }
        if(dict.getDictCode() == null){
            throw new AssertsException("{frame.dict.notnull.code}");
        }
        if(!"dict_root".equals(dict.getTypeCode())){
            redisHelper.putMap(KEY_GROUP + dict.getGroupCode(), dict.getDictCode(), dict);
            if(!"dict_root".equals(dict.getGroupCode())){
                redisHelper.putMap(KEY_TYPE + dict.getTypeCode(), dict.getDictCode(), dict);
            }
        }
        redisHelper.putValue(KEY_DICT + dict.getDictCode(), dict);
    }

    /**
     * 获取某个分组字典
     */
    public <T extends Dict> List<T> getGroup(String groupCode) {
        if(StringUtils.isBlank(groupCode)){
            return new ArrayList<>();
        }
        Map<String, T> map = redisHelper.getMap(KEY_GROUP + groupCode);
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
    public <T extends Dict> List<T> getType(String typeCode) {
        if(StringUtils.isBlank(typeCode)){
            return new ArrayList<>();
        }
        Map<String, T> map = redisHelper.getMap(KEY_TYPE + typeCode);
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
    public <T extends Dict> T getDict(String dictCode) {
        if(StringUtils.isBlank(dictCode)){
            return null;
        }
        return redisHelper.getValue(KEY_DICT + dictCode);
    }

    /**
     * 获取字典Label
     */
    public String getDictLabel(String dictCode) {
        Dict dict = getDict(dictCode);
        if(dict == null){
            return null;
        }
        return dict.getDictLabel();
    }

    /**
     * 获取字典值
     */
    public <T> T getDictValue(String dictCode) {
        Dict dict = getDict(dictCode);
        if(dict == null){
            return null;
        }
        return (T)dict.getDictValue();
    }

    /**
     * 删除字典
     */
    public void removeDict(String dictCode) {
        if(StringUtils.isBlank(dictCode)){
            return;
        }
        Dict dict = redisHelper.getValue(KEY_DICT + dictCode);
        if(dict == null){
            return;
        }
        redisHelper.delete(KEY_DICT + dictCode);
        redisHelper.deleteMap(KEY_TYPE + dict.getTypeCode(), dictCode);
        redisHelper.deleteMap(KEY_GROUP + dict.getGroupCode(), dictCode);
    }

    /**
     * 删除字典类型
     *
     * <p>删除sys-dict:dict:{dictCode}
     * <p>从sys-dict:dict:{groupCode}中删除
     *
     * <p>删除sys-dict:type:{typeCode}
     * <p>从sys-dict:group:dict_group中删除类型
     */
    public void removeType(String typeCode) {
        if(StringUtils.isBlank(typeCode)){
            return;
        }
        Map<String, Dict> dictMap = redisHelper.getMap(KEY_TYPE + typeCode);
        if(dictMap != null){
            for(Dict dict : dictMap.values()){
                redisHelper.delete(KEY_DICT + dict.getDictCode());
                redisHelper.deleteMap(KEY_GROUP + dict.getGroupCode(), dict.getDictCode());
            }
        }
        redisHelper.delete(KEY_TYPE + typeCode);
        redisHelper.deleteMap(KEY_GROUP + "dict_group", typeCode);
    }

    /**
     * 删除字典分组
     *
     * <p>删除sys-dict:type:{groupCode}
     * <p>从sys-dict:group:dict_group中删除类型
     *
     * <p>删除sys-dict:dict:{dictCode}
     *
     * <p>删除sys-dict:group:{groupCode}
     * <p>从sys-dict:group:dict_root中删除分组
     */
    public void removeGroup(String groupCode) {
        if(StringUtils.isBlank(groupCode)){
            return;
        }
        Map<String, Dict> typeMap = redisHelper.getMap(KEY_TYPE + groupCode);
        if(typeMap != null){
            redisHelper.delete(KEY_TYPE + groupCode);
            for(Dict type : typeMap.values()){
                redisHelper.deleteMap(KEY_GROUP + "dict_group", type.getDictCode());
            }
        }
        Map<String, Dict> dictMap = redisHelper.getMap(KEY_GROUP + groupCode);
        if(dictMap != null){
            for(Dict dict : dictMap.values()){ //
                redisHelper.delete(KEY_DICT + dict.getDictCode());
            }
        }
        redisHelper.delete(KEY_GROUP + groupCode);
        redisHelper.deleteMap(KEY_GROUP + "dict_root", groupCode);
    }

    /**
     * 清空字典缓存
     */
    public void clear() {
        Collection<String> keys = redisHelper.keys(KEY + "*");
        redisHelper.delete(keys);
    }
}
