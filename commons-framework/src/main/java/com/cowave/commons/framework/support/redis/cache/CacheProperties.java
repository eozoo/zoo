/*
 * Copyright (c) 2017～2024 Cowave All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.commons.framework.support.redis.cache;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author shanhuiming
 *
 */
@Data
@ConfigurationProperties(prefix = "spring.cache")
public class CacheProperties {

    private Map<String, L2Properties> l2 = new HashMap<>();

    private boolean l2Enable;

    /** 访问后过期时间，单位秒*/
    private long expireAfterAccess;

    /** 写入后过期时间，单位秒*/
    private long expireAfterWrite;

    /** 写入后刷新时间，单位秒*/
    private long refreshAfterWrite;

    /** 初始化大小*/
    private int initialCapacity;

    /** 最大缓存对象个数，超过此数量时之前放入的缓存将失效*/
    private long maximumSize;

    public Integer getExpire(String cacheKey){
        L2Properties l2Properties = l2.get(cacheKey);
        if(l2Properties != null){
            return l2Properties.getExpire();
        }
        return 0;
    }

    public boolean isL2First(String cacheKey){
        L2Properties l2Properties = l2.get(cacheKey);
        if(l2Properties != null){
            return l2Properties.isL2First();
        }
        return false;
    }

    @Data
    public static class L2Properties {
        private int expire;
        private boolean l2First;
    }
}
