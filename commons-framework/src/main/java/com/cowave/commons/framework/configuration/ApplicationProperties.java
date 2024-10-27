package com.cowave.commons.framework.configuration;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 *
 * @author shanhuiming
 *
 */
@Data
@ConfigurationProperties(prefix = "spring.application")
public class ApplicationProperties {

    /**
     * 应用名称
     */
    private String name;

    /**
     * 应用版本
     */
    private String version;

    /**
     * 命名空间 Redis缓存
     */
    private String namespace;

    /**
     * 集群id
     */
    private int clusterId = 10;

    /**
     * 集群等级
     */
    private int clusterLevel = 1;

    /**
     * 集群名称
     */
    private String clusterName = "default";

    /**
     * 集群属性
     */
    private Map<String, Object> clusterProperties;

    public String getTokenNamespace(){
        if(StringUtils.isBlank(namespace)){
            return "token:";
        }
        if(namespace.endsWith(":")){
            return namespace + "token:";
        }else{
            return namespace + ":token:";
        }
    }

    public String getCacheNamespace(){
        if(StringUtils.isBlank(namespace)){
            return "cache:";
        }
        if(namespace.endsWith(":")){
            return namespace + "cache:";
        }else{
            return namespace + ":cache:";
        }
    }

    public String getDictNamespace(){
        if(StringUtils.isBlank(namespace)){
            return "dict:";
        }
        if(namespace.endsWith(":")){
            return namespace + "dict:";
        }else{
            return namespace + ":dict:";
        }
    }

    public String getLockNamespace(){
        if(StringUtils.isBlank(namespace)){
            return "lock:";
        }
        if(namespace.endsWith(":")){
            return namespace + "lock:";
        }else{
            return namespace + ":lock:";
        }
    }
}
