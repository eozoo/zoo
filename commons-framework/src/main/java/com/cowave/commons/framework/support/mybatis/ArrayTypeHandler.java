/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.support.mybatis;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

/**
 *
 * @author shanhuiming
 *
 */
public class ArrayTypeHandler<T> extends BaseTypeHandler<List<T>> {

    private static final String TYPE_NAME_VARCHAR = "varchar";

    private static final String TYPE_NAME_BOOLEAN = "boolean";

    private static final String TYPE_NAME_INTEGER = "integer";

    private static final String TYPE_NAME_LONG = "bigint";

    private static final String TYPE_NAME_NUMERIC = "numeric";

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<T> parameter, JdbcType jdbcType) throws SQLException {
        if(CollectionUtils.isEmpty(parameter)) {
            ps.setArray(i, null);
        }else {
            T t = parameter.get(0);
            String typeName = TYPE_NAME_VARCHAR;
            if (t instanceof Integer) {
                typeName = TYPE_NAME_INTEGER;
            } else if (t instanceof Boolean) {
                typeName = TYPE_NAME_BOOLEAN;
            } else if (t instanceof Double) {
                typeName = TYPE_NAME_NUMERIC;
            } else if (t instanceof Long) {
                typeName = TYPE_NAME_LONG;
            }
            Connection conn = ps.getConnection();
            Array array = conn.createArrayOf(typeName, parameter.toArray());
            ps.setArray(i, array);
        }
    }

    @Override
    public List<T> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return getArray(rs.getArray(columnName));
    }

    @Override
    public List<T> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return getArray(rs.getArray(columnIndex));
    }

    @Override
    public List<T> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return getArray(cs.getArray(columnIndex));
    }

    @SuppressWarnings("unchecked")
    private List<T> getArray(Array array) {
        if (array == null) {
            return new ArrayList<>();
        }
        try {
            return Arrays.asList((T[]) array.getArray());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
