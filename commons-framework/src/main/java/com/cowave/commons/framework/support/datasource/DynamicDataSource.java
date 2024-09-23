/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.support.datasource;

import java.util.Map;
import javax.sql.DataSource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 *
 * @author shanhuiming
 *
 */
public class DynamicDataSource extends AbstractRoutingDataSource{

    private final Map<Object, Object> dataSourceMap;

    public DynamicDataSource(DataSource defaultTargetDataSource, Map<Object, Object> targetDataSources){
        super.setDefaultTargetDataSource(defaultTargetDataSource);
        super.setTargetDataSources(targetDataSources);
        super.afterPropertiesSet();
        this.dataSourceMap = targetDataSources;
    }

    public Map<Object, Object> getDataSourceMap() {
        return dataSourceMap;
    }

    @Override
    protected Object determineCurrentLookupKey(){
        return get();
    }

    private static final ThreadLocal<String> HOLDER = new ThreadLocal<>();

    public static String get(){
        return HOLDER.get();
    }

    public static void set(String dataSourceName){
        HOLDER.set(dataSourceName);
    }

    public static void clear(){
    	HOLDER.remove();
    }
}
