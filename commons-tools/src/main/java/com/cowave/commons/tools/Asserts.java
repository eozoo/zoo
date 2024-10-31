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

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author shanhuiming
 *
 */
public class Asserts {

    public static void isTrue(boolean expression, String message, Object... args) {
        if (!expression) {
            throw new AssertsException(message, args);
        }
    }

    public static void isTrue(boolean expression, Supplier<String> errorSupplier) {
        if (!expression) {
            throw new AssertsException(errorSupplier.get());
        }
    }

    public static void isFalse(boolean expression, String message, Object... args) {
        if (expression) {
            throw new AssertsException(message, args);
        }
    }

    public static void isFalse(boolean expression, Supplier<String> errorSupplier) {
        if (expression) {
            throw new AssertsException(errorSupplier.get());
        }
    }

    public static void notEquals(Object a, Object b, String message, Object... args) {
        if (Objects.equals(a, b)) {
            throw new AssertsException(message, args);
        }
    }

    public static void notEquals(Object a, Object b, Supplier<String> errorSupplier) {
        if (Objects.equals(a, b)) {
            throw new AssertsException(errorSupplier.get());
        }
    }

    public static void equals(Object a, Object b, String message, Object... args) {
        if (!Objects.equals(a, b)) {
            throw new AssertsException(message, args);
        }
    }

    public static void equals(Object a, Object b, Supplier<String> errorSupplier) {
        if (!Objects.equals(a, b)) {
            throw new AssertsException(errorSupplier.get());
        }
    }

    public static void notBlank(String text, String message, Object... args) {
        if (StringUtils.isBlank(text)) {
            throw new AssertsException(message, args);
        }
    }

    public static void notBlank(String text, Supplier<String> errorSupplier) {
        if (StringUtils.isBlank(text)) {
            throw new AssertsException(errorSupplier.get());
        }
    }

    public static void isBlank(String text, String message, Object... args) {
        if (StringUtils.isBlank(text)) {
            throw new AssertsException(message, args);
        }
    }

    public static void isBlank(String text, Supplier<String> errorSupplier) {
        if (StringUtils.isBlank(text)) {
            throw new AssertsException(errorSupplier.get());
        }
    }

    public static void notNull(Object object, String message, Object... args) {
        if (object == null) {
            throw new AssertsException(message, args);
        }
    }

    public static void notNull(Object object, Supplier<String> errorSupplier) {
        if (object == null) {
            throw new AssertsException(errorSupplier.get());
        }
    }

    public static void isNull(Object object, String message, Object... args) {
        if (object != null) {
            throw new AssertsException(message, args);
        }
    }

    public static void isNull(Object object, Supplier<String> errorSupplier) {
        if (object != null) {
            throw new AssertsException(errorSupplier.get());
        }
    }

    public static void notEmpty(Map<?, ?> map, String message, Object... args) {
        if (ObjectUtils.isEmpty(map)) {
            throw new AssertsException(message, args);
        }
    }

    public static void notEmpty(Map<?, ?> map, Supplier<String> errorSupplier) {
        if (ObjectUtils.isEmpty(map)) {
            throw new AssertsException(errorSupplier.get());
        }
    }

    public static void isEmpty(Map<?, ?> map, String message, Object... args) {
        if (ObjectUtils.isNotEmpty(map)) {
            throw new AssertsException(message, args);
        }
    }

    public static void isEmpty(Map<?, ?> map, Supplier<String> errorSupplier) {
        if (ObjectUtils.isNotEmpty(map)) {
            throw new AssertsException(errorSupplier.get());
        }
    }

    public static void notEmpty(Collection<?> collection, String message, Object... args) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new AssertsException(message, args);
        }
    }

    public static void notEmpty(Collection<?> collection, Supplier<String> errorSupplier) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new AssertsException(errorSupplier.get());
        }
    }

    public static void isEmpty(Collection<?> collection, String message, Object... args) {
        if (!CollectionUtils.isEmpty(collection)) {
            throw new AssertsException(message, args);
        }
    }

    public static void isEmpty(Collection<?> collection, Supplier<String> errorSupplier) {
        if (!CollectionUtils.isEmpty(collection)) {
            throw new AssertsException(errorSupplier.get());
        }
    }

    public static void notEmpty(Object[] array, String message, Object... args) {
        if (ObjectUtils.isEmpty(array)) {
            throw new AssertsException(message, args);
        }
    }

    public static void notEmpty(Object[] array, Supplier<String> errorSupplier) {
        if (ObjectUtils.isEmpty(array)) {
            throw new AssertsException(errorSupplier.get());
        }
    }


    public static void isEmpty(Object[] array, String message, Object... args) {
        if (!ObjectUtils.isEmpty(array)) {
            throw new AssertsException(message, args);
        }
    }

    public static void isEmpty(Object[] array, Supplier<String> errorSupplier) {
        if (!ObjectUtils.isEmpty(array)) {
            throw new AssertsException(errorSupplier.get());
        }
    }
}
