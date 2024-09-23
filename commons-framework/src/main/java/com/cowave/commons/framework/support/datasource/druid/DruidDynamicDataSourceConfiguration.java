package com.cowave.commons.framework.support.datasource.druid;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.servlet.*;
import javax.sql.DataSource;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.stat.DruidFilterConfiguration;
import com.alibaba.druid.spring.boot.autoconfigure.stat.DruidSpringAopConfiguration;
import com.alibaba.druid.spring.boot.autoconfigure.stat.DruidStatViewServletConfiguration;
import com.alibaba.druid.spring.boot.autoconfigure.stat.DruidWebStatFilterConfiguration;
import com.alibaba.druid.util.Utils;
import com.cowave.commons.framework.support.datasource.DynamicDataSource;
import com.cowave.commons.framework.support.datasource.DynamicDataSourceProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import com.alibaba.druid.spring.boot.autoconfigure.properties.DruidStatProperties;

/**
 *
 * @author shanhuiming
 *
 */
@ConditionalOnClass(DruidDataSource.class)
@Import({DruidSpringAopConfiguration.class, DruidStatViewServletConfiguration.class,
		DruidWebStatFilterConfiguration.class, DruidFilterConfiguration.class})
@EnableConfigurationProperties({DataSourceProperties.class, DynamicDataSourceProperties.class, DruidStatProperties.class})
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@RequiredArgsConstructor
public class DruidDynamicDataSourceConfiguration {

	private final DataSourceProperties dataSourceProperties;

	private final DynamicDataSourceProperties dynamicDataSourceProperties;

	private final ApplicationContext applicationContext;

	@Primary
	@Bean
	public DataSource dataSource() throws Exception {
		Map<String, DataSourceProperties> propertiesMap = dynamicDataSourceProperties.getDynamic();
		if(propertiesMap != null && !propertiesMap.isEmpty()){
			DruidDataSource primary = null;
			Map<Object, Object> dataSourceMap = new HashMap<>();
			for(Map.Entry<String, DataSourceProperties> entry : propertiesMap.entrySet()){
				DruidDataSourceWrapper dataSource = applicationContext.getBean(DruidDataSourceWrapper.class, entry.getValue());
				dataSource.afterPropertiesSet();
				dataSource.init();
				dataSourceMap.put(entry.getKey(), dataSource);
				if(Objects.equals(entry.getKey(), "primary")){
					primary = dataSource;
				}
			}
			return new DynamicDataSource(primary, dataSourceMap);
		}else{
			DruidDataSourceWrapper dataSource = applicationContext.getBean(DruidDataSourceWrapper.class, dataSourceProperties);
			dataSource.afterPropertiesSet();
			dataSource.init();
			Map<Object, Object> dataSourceMap = new HashMap<>();
			dataSourceMap.put("primary", dataSource);
			return new DynamicDataSource(dataSource, dataSourceMap);
		}
	}
}
