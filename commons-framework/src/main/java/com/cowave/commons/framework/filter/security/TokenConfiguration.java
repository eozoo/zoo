package com.cowave.commons.framework.filter.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 *
 * @author shanhuiming
 *
 */
@Data
@Configuration(proxyBeanMethods = false)
@ConfigurationProperties("spring.application.token")
public class TokenConfiguration {

    /**
     * header名称
     */
    private String header = "Authorization";

    /**
     * 秘钥
     */
    private String salt = "admin@cowave.com";

    /**
     * 是否检查冲突
     */
    private boolean conflict = false;

    /**
     * 应用令牌认证超时[单位秒]
     */
    private int appExpire = 60;

    /**
     * 客户端超时[单位秒]
     */
    private int clientExpire = 3600;

    /**
     * 服务端超时[单位秒]
     */
    private int serverExpire = 36000;

    /**
     * 忽略鉴权的url
     */
    private List<String> ignoreUrls = new ArrayList<>();

}
