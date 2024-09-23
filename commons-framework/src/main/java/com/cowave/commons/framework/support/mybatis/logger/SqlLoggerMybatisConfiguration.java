/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.support.mybatis.logger;

import lombok.RequiredArgsConstructor;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.List;

/**
 *
 * @author shanhuiming
 *
 */
@ConditionalOnProperty("logging.sql-enabled")
@ConditionalOnClass(MybatisAutoConfiguration.class)
@AutoConfigureAfter(MybatisAutoConfiguration.class)
@RequiredArgsConstructor
public class SqlLoggerMybatisConfiguration implements InitializingBean {

    private final List<SqlSessionFactory> sqlSessionFactoryList;

    @Override
    public void afterPropertiesSet() {
        SqlLogger interceptor = new SqlLogger();
        for (SqlSessionFactory sqlSessionFactory : sqlSessionFactoryList) {
            org.apache.ibatis.session.Configuration configuration = sqlSessionFactory.getConfiguration();
            if (!containsInterceptor(configuration, interceptor)) {
                configuration.addInterceptor(interceptor);
            }
        }
    }

    private boolean containsInterceptor(org.apache.ibatis.session.Configuration configuration, Interceptor sqlInterceptor) {
        return configuration.getInterceptors().stream().anyMatch(interceptor -> sqlInterceptor.getClass().isAssignableFrom(interceptor.getClass()));
    }
}
