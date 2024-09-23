/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.support.datasource.druid;

import java.util.List;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.pool.DruidDataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author shanhuiming
 *
 */
@ConditionalOnClass(DruidDataSource.class)
@Scope("prototype")
@Component
@ConfigurationProperties("spring.datasource.druid")
public class DruidDataSourceWrapper extends DruidDataSource {

    private final DataSourceProperties druidProperties;

    public DruidDataSourceWrapper(DataSourceProperties druidProperties) {
        this.druidProperties = druidProperties;
    }

    public DataSourceProperties getBasicProperties() {
        return druidProperties;
    }

    public void afterPropertiesSet() throws Exception {
        //if not found prefix 'spring.datasource.druid' jdbc properties ,'spring.datasource' prefix jdbc properties will be used.
        if (super.getUsername() == null) {
            super.setUsername(druidProperties.determineUsername());
        }
        if (super.getPassword() == null) {
            super.setPassword(druidProperties.determinePassword());
        }
        if (super.getUrl() == null) {
            super.setUrl(druidProperties.determineUrl());
        }
        if (super.getDriverClassName() == null) {
            super.setDriverClassName(druidProperties.getDriverClassName());
        }
    }

    @Autowired(required = false)
    public void autoAddFilters(List<Filter> filters) {
        super.filters.addAll(filters);
    }

    /**
     * Ignore the 'maxEvictableIdleTimeMillis &lt; minEvictableIdleTimeMillis' validate,
     * it will be validated again in {@link DruidDataSource#init()}.
     * for fix issue #3084, #2763
     *
     * @since 1.1.14
     */
    @Override
    public void setMaxEvictableIdleTimeMillis(long maxEvictableIdleTimeMillis) {
        try {
            super.setMaxEvictableIdleTimeMillis(maxEvictableIdleTimeMillis);
        } catch (IllegalArgumentException ignore) {
            super.maxEvictableIdleTimeMillis = maxEvictableIdleTimeMillis;
        }
    }
}
