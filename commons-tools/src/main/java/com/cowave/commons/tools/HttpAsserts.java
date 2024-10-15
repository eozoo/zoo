/*
 * Copyright (c) 2017～2024 Cowave All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.commons.tools;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 *
 * @author shanhuiming
 *
 */
public class HttpAsserts {

    public static void isTrue(boolean expression, int status, String code, String message, Object... args) {
        if (!expression) {
            throw new HttpException(status, code, message, args);
        }
    }

    public static void isTrue(boolean expression, int status, String code, Supplier<String> errorSupplier) {
        if (!expression) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }

    public static void isFalse(boolean expression, int status, String code, String message, Object... args) {
        if (expression) {
            throw new HttpException(status, code, message, args);
        }
    }

    public static void isFalse(boolean expression, int status, String code, Supplier<String> errorSupplier) {
        if (expression) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }

    public static void notEquals(Object a, Object b, int status, String code, String message, Object... args) {
        if (Objects.equals(a, b)) {
            throw new HttpException(status, code, message, args);
        }
    }

    public static void notEquals(Object a, Object b, int status, String code, Supplier<String> errorSupplier) {
        if (Objects.equals(a, b)) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }

    public static void equals(Object a, Object b, int status, String code, String message, Object... args) {
        if (!Objects.equals(a, b)) {
            throw new HttpException(status, code, message, args);
        }
    }

    public static void equals(Object a, Object b, int status, String code, Supplier<String> errorSupplier) {
        if (!Objects.equals(a, b)) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }

    public static void notBlank(String text, int status, String code, String message, Object... args) {
        if (StringUtils.isBlank(text)) {
            throw new HttpException(status, code, message, args);
        }
    }

    public static void notBlank(String text, int status, String code, Supplier<String> errorSupplier) {
        if (StringUtils.isBlank(text)) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }

    public static void isBlank(String text, int status, String code, String message, Object... args) {
        if (StringUtils.isBlank(text)) {
            throw new HttpException(status, code, message, args);
        }
    }

    public static void isBlank(String text, int status, String code, Supplier<String> errorSupplier) {
        if (StringUtils.isBlank(text)) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }

    public static void notNull(Object object, int status, String code, String message, Object... args) {
        if (object == null) {
            throw new HttpException(status, code, message, args);
        }
    }

    public static void notNull(Object object, int status, String code, Supplier<String> errorSupplier) {
        if (object == null) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }

    public static void isNull(Object object, int status, String code, String message, Object... args) {
        if (object != null) {
            throw new HttpException(status, code, message, args);
        }
    }

    public static void isNull(Object object, int status, String code, Supplier<String> errorSupplier) {
        if (object != null) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }

    public static void notEmpty(Map<?, ?> map, int status, String code, String message, Object... args) {
        if (ObjectUtils.isEmpty(map)) {
            throw new HttpException(status, code, message, args);
        }
    }

    public static void notEmpty(Map<?, ?> map, int status, String code, Supplier<String> errorSupplier) {
        if (ObjectUtils.isEmpty(map)) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }

    public static void isEmpty(Map<?, ?> map, int status, String code, String message, Object... args) {
        if (ObjectUtils.isNotEmpty(map)) {
            throw new HttpException(status, code, message, args);
        }
    }

    public static void isEmpty(Map<?, ?> map, int status, String code, Supplier<String> errorSupplier) {
        if (ObjectUtils.isNotEmpty(map)) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }

    public static void notEmpty(Collection<?> collection, int status, String code, String message, Object... args) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new HttpException(status, code, message, args);
        }
    }

    public static void notEmpty(Collection<?> collection, int status, String code, Supplier<String> errorSupplier) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }

    public static void isEmpty(Collection<?> collection, int status, String code, String message, Object... args) {
        if (!CollectionUtils.isEmpty(collection)) {
            throw new HttpException(status, code, message, args);
        }
    }

    public static void isEmpty(Collection<?> collection, int status, String code, Supplier<String> errorSupplier) {
        if (!CollectionUtils.isEmpty(collection)) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }

    public static void notEmpty(Object[] array, int status, String code, String message, Object... args) {
        if (ObjectUtils.isEmpty(array)) {
            throw new HttpException(status, code, message, args);
        }
    }

    public static void notEmpty(Object[] array, int status, String code, Supplier<String> errorSupplier) {
        if (ObjectUtils.isEmpty(array)) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }


    public static void isEmpty(Object[] array, int status, String code, String message, Object... args) {
        if (!ObjectUtils.isEmpty(array)) {
            throw new HttpException(status, code, message, args);
        }
    }

    public static void isEmpty(Object[] array, int status, String code, Supplier<String> errorSupplier) {
        if (!ObjectUtils.isEmpty(array)) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }
}
