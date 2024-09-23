package com.cowave.commons.framework.support.datasource.health;

import lombok.Data;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author shanhuiming
 *
 */
@Data
public class DataSourceBean {

    private DataSource dataSource;

    private DataSourceProperties dataSourceProperties;

    public boolean isEnable() {
        try(Connection ignored = dataSource.getConnection()){
            return true;
        }catch (SQLException e){
            return false;
        }
    }

    public String getUrl(){
        return dataSourceProperties.getUrl();
    }

    public String getUsername(){
        return dataSourceProperties.getUsername();
    }

    public String getDriver(){
        return dataSourceProperties.getDriverClassName();
    }
}
