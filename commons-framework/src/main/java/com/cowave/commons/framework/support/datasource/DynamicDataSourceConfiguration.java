/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.support.datasource;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author shanhuiming
 *
 */
@ConditionalOnMissingClass("com.alibaba.druid.pool.DruidDataSource")
@ConditionalOnClass(AbstractRoutingDataSource.class)
@EnableConfigurationProperties({DataSourceProperties.class, DynamicDataSourceProperties.class})
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@RequiredArgsConstructor
public class DynamicDataSourceConfiguration {

    private final DataSourceProperties dataSourceProperties;

    private final DynamicDataSourceProperties dynamicDataSourceProperties;

    @ConditionalOnMissingBean(DataSource.class)
    @Primary
    @Bean
    public DataSource dataSource() {
        Map<String, DataSourceProperties> propertiesMap = dynamicDataSourceProperties.getDynamic();
        if(propertiesMap != null && !propertiesMap.isEmpty()){
            DataSource primary = null;
            Map<Object, Object> dataSourceMap = new HashMap<>();
            for(Map.Entry<String, DataSourceProperties> entry : propertiesMap.entrySet()){
                DataSourceProperties properties = entry.getValue();
                DataSource dataSource = DataSourceBuilder.create()
                        .driverClassName(properties.getDriverClassName())
                        .type(properties.getType())
                        .url(properties.getUrl())
                        .username(properties.getUsername())
                        .password(properties.getPassword()).build();
                dataSourceMap.put(entry.getKey(), dataSource);
                if(Objects.equals(entry.getKey(), "primary")){
                    primary = dataSource;
                }
            }
            return new DynamicDataSource(primary, dataSourceMap);
        }else{
            DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
            if(dataSourceProperties.getType() != null){
                dataSourceBuilder.type(dataSourceProperties.getType());
            }
            if(dataSourceProperties.getDriverClassName() != null){
                dataSourceBuilder.driverClassName(dataSourceProperties.getDriverClassName());
            }
            if(dataSourceProperties.getUrl() != null){
                dataSourceBuilder.url(dataSourceProperties.getUrl());
            }
            if(dataSourceProperties.getUsername() != null){
                dataSourceBuilder.username(dataSourceProperties.getUsername());
            }
            if(dataSourceProperties.getPassword() != null){
                dataSourceBuilder.password(dataSourceProperties.getPassword());
            }
            DataSource dataSource = dataSourceBuilder.build();
            Map<Object, Object> dataSourceMap = new HashMap<>();
            dataSourceMap.put("primary", dataSource);
            return new DynamicDataSource(dataSource, dataSourceMap);
        }
    }
}
