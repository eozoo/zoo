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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

/**
 *
 * @author shanhuiming
 *
 */
class StringArrayHandler {

    public static <K, T extends Collection<K>> T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return getCollection(rs.getString(columnName));
    }

    public static <K, T extends Collection<K>> T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return getCollection(rs.getString(columnIndex));
    }

    public static <K, T extends Collection<K>> T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return getCollection(cs.getString(columnIndex));
    }

    private static <K, T extends Collection<K>> T getCollection(String data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        return JSON.parseObject(data, new TypeReference<>() {});
    }
}
