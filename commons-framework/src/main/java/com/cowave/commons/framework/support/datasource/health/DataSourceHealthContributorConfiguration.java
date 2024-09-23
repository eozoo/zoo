package com.cowave.commons.framework.support.datasource.health;

import java.util.HashMap;
import java.util.Map;

import com.cowave.commons.framework.support.datasource.DynamicDataSource;
import com.cowave.commons.framework.support.datasource.DynamicDataSourceProperties;
import com.cowave.commons.framework.support.datasource.druid.DruidDynamicDataSourceConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.autoconfigure.health.CompositeHealthContributorConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 *
 * @author shanhuiming
 *
 */
@ConditionalOnClass(AbstractRoutingDataSource.class)
@ConditionalOnEnabledHealthIndicator("db")
@AutoConfiguration(after = { DruidDynamicDataSourceConfiguration.class })
@EnableConfigurationProperties({DataSourceProperties.class, DynamicDataSourceProperties.class})
@RequiredArgsConstructor
public class DataSourceHealthContributorConfiguration
extends CompositeHealthContributorConfiguration<DataSourceHealthIndicator, DataSourceBean> {

	@Resource
	private DataSourceProperties dataSourceProperties;

	@Resource
	private DynamicDataSourceProperties dynamicDataSourceProperties;

	@Bean
	public HealthContributor dbHealthContributor(DynamicDataSource dataSource) {
		Map<String, DataSourceBean> map = new HashMap<>();
		Map<String, DataSourceProperties> propertiesMap = dynamicDataSourceProperties.getDynamic();
		if(propertiesMap != null && !propertiesMap.isEmpty()){
			for(Map.Entry<String, DataSourceProperties> entry : propertiesMap.entrySet()){
				String dataSourceName = entry.getKey();
				DataSourceBean dataSourceBean = new DataSourceBean();
				dataSourceBean.setDataSourceProperties(entry.getValue());
				dataSourceBean.setDataSource((DataSource)dataSource.getDataSourceMap().get(dataSourceName));
				map.put(dataSourceName, dataSourceBean);
			}
		}else{
			DataSourceBean dataSourceBean = new DataSourceBean();
			dataSourceBean.setDataSourceProperties(dataSourceProperties);
			dataSourceBean.setDataSource((DataSource)dataSource.getDataSourceMap().get("primary"));
			map.put("primary", dataSourceBean);
		}
		return createContributor(map);
	}
}
