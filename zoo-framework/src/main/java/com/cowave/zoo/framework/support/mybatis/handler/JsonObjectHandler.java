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

import com.alibaba.fastjson.JSON;
import com.cowave.zoo.framework.support.mybatis.DatabaseProvider;
import com.cowave.zoo.tools.SpringContext;
import com.cowave.zoo.tools.json.JacksonUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

/**
 * <p>数据库：json
 * <p>Java: Object
 *
 * @author shanhuiming
 */
public class JsonObjectHandler<T> extends BaseTypeHandler<T> {

    private final Class<T> clazz;

    private final boolean isCurrentPg;

    public JsonObjectHandler(Class<T> clazz) {
        DatabaseProvider provider = SpringContext.getBean(DatabaseProvider.class);
        String dataBaseId = provider.getCurrentDatabase();
        if ("postgres".equals(dataBaseId)) {
            this.isCurrentPg = true;
        } else {
            this.isCurrentPg = false;
        }
        this.clazz = clazz;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
        if (isCurrentPg) {
            PgJsonHandler.setNonNullParameter(ps, i, parameter, jdbcType);
        } else {
            ps.setString(i, JacksonUtils.writeValue(parameter));
        }
    }

    @Override
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toObject(rs.getString(columnName), clazz);
    }

    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toObject(rs.getString(columnIndex), clazz);
    }

    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toObject(cs.getString(columnIndex), clazz);
    }

    private static <T> T toObject(String content, Class<T> clazz) {
        if (content != null && !content.isEmpty()) {
            try {
                return JSON.parseObject(content, clazz);
            } catch (Exception e) {
                content = content.substring(1, content.length() - 1).replace("\\\"", "\"");
                return JSON.parseObject(content, clazz);
            }
        } else {
            return null;
        }
    }
}
