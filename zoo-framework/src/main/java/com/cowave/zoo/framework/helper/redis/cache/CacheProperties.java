/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.zoo.framework.helper.redis.cache;

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

    /**
     * 是否启用L1缓存
     */
    private boolean l1Enable;

    /**
     * 是否启用L2缓存
     */
    private boolean l2Enable;

    /**
     * 是否优先使用L2缓存
     */
    private boolean l2First;

    /**
     * L2访问后过期时间，单位秒
     */
    private int l2ExpireAfterAccess;

    /**
     * L2写入后过期时间，单位秒
     */
    private int l2ExpireAfterWrite;

    /**
     * L1 本地缓存配置
     */
    private L1Properties l1 = new L1Properties();

    /**
     * L2缓存Key配置
     */
    private Map<Object, L2Properties> l2 = new HashMap<>();

    public boolean l1Enable(){
        return l1Enable;
    }

    public long l1ExpireAfterAccess(){
        return l1.expireAfterAccess;
    }

    public long l1ExpireAfterWrite(){
        return l1.expireAfterWrite;
    }

    public long l1RefreshAfterWrite(){
        return l1.refreshAfterWrite;
    }

    public int l1InitialCapacity(){
        return l1.initialCapacity;
    }

    public long l1MaximumSize(){
        return l1.maximumSize;
    }

    public boolean l2Enable(Object cacheKey){
        L2Properties l2Properties = l2.get(cacheKey);
        if(l2Properties != null){
            return l2Properties.enable;
        }
        return l2Enable;
    }

    public boolean l2First(Object cacheKey){
        L2Properties l2Properties = l2.get(cacheKey);
        if(l2Properties != null){
            return l2Properties.first;
        }
        return l2First;
    }

    public int l2ExpireAfterAccess(String cacheKey){
        L2Properties l2Properties = l2.get(cacheKey);
        if(l2Properties != null){
            return l2Properties.expireAfterAccess;
        }
        return l2ExpireAfterAccess;
    }

    public int l2ExpireAfterWrite(String cacheKey){
        L2Properties l2Properties = l2.get(cacheKey);
        if(l2Properties != null){
            return l2Properties.expireAfterWrite;
        }
        return l2ExpireAfterWrite;
    }

    @Data
    public static class L1Properties {

        /**
         * 访问后过期时间，单位秒
         */
        private long expireAfterAccess = -1;

        /**
         * 写入后过期时间，单位秒
         */
        private long expireAfterWrite = -1;

        /**
         * 写入后刷新时间，单位秒
         */
        private long refreshAfterWrite = -1;

        /**
         * 初始化大小
         */
        private int initialCapacity = -1;

        /**
         * 最大缓存对象个数，超过此数量时之前放入的缓存将失效
         */
        private long maximumSize = -1;

    }

    @Data
    public static class L2Properties {

        /**
         * 启用二级缓存
         */
        private boolean enable;

        /**
         * 优先二级缓存
         */
        private boolean first;

        /**
         * 访问后过期时间，单位秒
         */
        private int expireAfterAccess;

        /**
         * 写入后过期时间，单位秒
         */
        private int expireAfterWrite;
    }
}
