/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.support.mybatis;

import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import com.cowave.commons.tools.Converts;
import com.cowave.commons.tools.Collections;
import com.cowave.commons.tools.json.JacksonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * @author jiangbo
 * @date 2024/1/8
 */
public class JacksonCollectionTypeHandler<K, T extends Collection<K>> extends AbstractJsonTypeHandler<T> {

    private final Class<K> clazz;

    public JacksonCollectionTypeHandler(Class<K> clazz) {
        this.clazz = clazz;
    }

    private Collection<K> toObject(String content, Class<K> clazz) {
        if (StringUtils.isNotBlank(content)) {
            try {
                ObjectMapper mapper = JacksonUtils.MAPPER;
                JsonNode jsonNode = mapper.readTree(content);
                if (!jsonNode.isArray()) {
                    return createCollection(clazz);
                }
                Collection<K> collection = createCollection(clazz);
                for (JsonNode json : jsonNode) {
                    if (json == null) {
                        collection.add(null);
                    } else {
                        TextNode cl = Converts.cast(json.get("clazz"));
                        JsonNode obj = json.get("item");
                        Class<K> kClass = Converts.cast(Class.forName(cl.asText()));
                        K k = mapper.treeToValue(obj, kClass);
                        collection.add(k);
                    }
                }
                return collection;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }

    @Override
    protected T parse(String json) {
        return Converts.cast(toObject(json, clazz));
    }

    @Override
    protected String toJson(T obj) {
        List<ItemInfo> itemInfoList = Collections.copyToList(Converts.cast(obj), ItemInfo::apply);
        return JacksonUtils.writeValue(itemInfoList);
    }

    public Collection<K> createCollection(Class<K> clazz) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (!Collection.class.isAssignableFrom(clazz)) {
            throw new UnsupportedOperationException("必须是集合类型");
        }
        if (List.class.equals(clazz)) {
            return new ArrayList<>();
        }
        if (Set.class.equals(clazz)) {
            return new HashSet<>();
        }
        return Converts.cast(clazz.getConstructor().newInstance());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class ItemInfo {

        private Class<?> clazz;

        private Object item;

        public static ItemInfo apply(Object item) {
            if (item == null) {
                return null;
            }
            return new ItemInfo(item.getClass(), item);
        }

    }
}
