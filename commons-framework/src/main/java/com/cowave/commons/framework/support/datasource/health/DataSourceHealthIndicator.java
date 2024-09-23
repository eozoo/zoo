package com.cowave.commons.framework.support.datasource.health;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author shanhuiming
 *
 */
public class DataSourceHealthIndicator extends AbstractHealthIndicator {

    private final DataSourceBean dataSourceBean;

    public DataSourceHealthIndicator(DataSourceBean dataSourceBean) {
        this.dataSourceBean = dataSourceBean;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        if(dataSourceBean.isEnable()){
            builder.up();
        }else{
            builder.down();
        }
        Map<String, String> info = new HashMap<>();
        info.put("url", dataSourceBean.getUrl());
        info.put("username", dataSourceBean.getUsername());
        info.put("driver", dataSourceBean.getDriver());
        builder.withDetails(info);
    }
}
