/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.configuration;

import com.cowave.commons.framework.filter.xss.XssFilter;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.spring.EnableDynamicTp;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.feign.annotation.FeignScan;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import lombok.Data;

import java.util.List;

/**
 *
 * @author shanhuiming
 *
 */
@Slf4j
@Data
@EnableDynamicTp
@EnableAspectJAutoProxy(exposeProxy = true)
@FeignScan(basePackages = "com.cowave")
@ComponentScan(basePackages = "com.cowave")
@ConfigurationProperties(prefix = "spring.application")
public class ApplicationConfiguration implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private String name;

    private String version;

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        String infoPath = "/META-INF/info.yml";
        Resource infoResource = new ClassPathResource(infoPath);
        if(!infoResource.exists()){
            return;
        }

        try{
            log.info("prepare to load: META-INF/info.yml");
            List<PropertySource<?>> list = new YamlPropertySourceLoader().load(infoPath, infoResource);
            for(PropertySource<?> source : list){
                environment.getPropertySources().addLast(source);
            }
        }catch (Exception e){
            log.error("failed to load: META-INF/info.yml", e);
            System.exit(-1);
        }
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");              // 任意地址
        config.addAllowedHeader("*");                     // 任意请求头
        config.addAllowedMethod("*");                     // 任意请求方法
        config.setMaxAge(1800L);                          // 有效期 1800秒
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);  // 任意url
        return new CorsFilter(source);
    }

    // @Bean
    public XssFilter xssFilter() {
        return new XssFilter();
    }
}
