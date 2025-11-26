/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.zoo.framework.support.mybatis.handler;

import java.sql.*;
import java.util.List;

import com.cowave.zoo.framework.support.mybatis.DatabaseProvider;
import com.cowave.zoo.tools.SpringContext;
import com.cowave.zoo.tools.json.JacksonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

/**
 * <p>数据库：array
 * <p>Java: List
 *
 * @author shanhuiming
 */
public class ArrayListHandler<T> extends BaseTypeHandler<List<T>> {

    private final boolean isCurrentPg;

    public ArrayListHandler() {
        DatabaseProvider provider = SpringContext.getBean(DatabaseProvider.class);
        String dataBaseId = provider.getCurrentDatabase();
        if ("postgres".equals(dataBaseId)) {
            this.isCurrentPg = true;
        } else {
            this.isCurrentPg = false;
        }
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<T> parameter, JdbcType jdbcType) throws SQLException {
        if(CollectionUtils.isEmpty(parameter)) {
            ps.setArray(i, null);
            return;
        }

        if (isCurrentPg) {
            PgArrayHandler.setBasicArrayParameter(ps, i, parameter, jdbcType);
        } else {
            ps.setString(i, JacksonUtils.writeValue(parameter));
        }
    }

    @Override
    public List<T> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        if (isCurrentPg) {
            return PgArrayHandler.getNullableResult(rs, columnName);
        } else {
            return StringArrayHandler.getNullableResult(rs, columnName);
        }
    }

    @Override
    public List<T> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        if (isCurrentPg) {
            return PgArrayHandler.getNullableResult(rs, columnIndex);
        } else {
            return StringArrayHandler.getNullableResult(rs, columnIndex);
        }
    }

    @Override
    public List<T> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        if (isCurrentPg) {
            return PgArrayHandler.getNullableResult(cs, columnIndex);
        } else {
            return StringArrayHandler.getNullableResult(cs, columnIndex);
        }
    }
}
