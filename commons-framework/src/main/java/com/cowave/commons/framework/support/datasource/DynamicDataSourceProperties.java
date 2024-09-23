package com.cowave.commons.framework.support.datasource;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * 
 * @author shanhuiming
 *
 */
@ConfigurationProperties("spring.datasource")
public class DynamicDataSourceProperties {

	private Map<String, DataSourceProperties> dynamic;

	public Map<String, DataSourceProperties> getDynamic() {
		return dynamic;
	}

	public void setDynamic(Map<String, DataSourceProperties> dynamic) {
		this.dynamic = dynamic;
	}
}
