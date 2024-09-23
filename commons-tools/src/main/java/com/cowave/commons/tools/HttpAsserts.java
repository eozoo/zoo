/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
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

    public static void isTrue(boolean expression, int status, String code, String message) {
        if (!expression) {
            throw new HttpException(status, code, message);
        }
    }

    public static void isTrue(boolean expression, int status, String code, Supplier<String> errorSupplier) {
        if (!expression) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }

    public static void isFalse(boolean expression, int status, String code, String message) {
        if (expression) {
            throw new HttpException(status, code, message);
        }
    }

    public static void isFalse(boolean expression, int status, String code, Supplier<String> errorSupplier) {
        if (expression) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }

    public static void notEquals(Object a, Object b, int status, String code, String message) {
        if (Objects.equals(a, b)) {
            throw new HttpException(status, code, message);
        }
    }

    public static void notEquals(Object a, Object b, int status, String code, Supplier<String> errorSupplier) {
        if (Objects.equals(a, b)) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }

    public static void equals(Object a, Object b, int status, String code, String message) {
        if (!Objects.equals(a, b)) {
            throw new HttpException(status, code, message);
        }
    }

    public static void equals(Object a, Object b, int status, String code, Supplier<String> errorSupplier) {
        if (!Objects.equals(a, b)) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }

    public static void notBlank(String text, int status, String code, String message) {
        if (StringUtils.isBlank(text)) {
            throw new HttpException(status, code, message);
        }
    }

    public static void notBlank(String text, int status, String code, Supplier<String> errorSupplier) {
        if (StringUtils.isBlank(text)) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }

    public static void isBlank(String text, int status, String code, String message) {
        if (StringUtils.isBlank(text)) {
            throw new HttpException(status, code, message);
        }
    }

    public static void isBlank(String text, int status, String code, Supplier<String> errorSupplier) {
        if (StringUtils.isBlank(text)) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }

    public static void notNull(Object object, int status, String code, String message) {
        if (object == null) {
            throw new HttpException(status, code, message);
        }
    }

    public static void notNull(Object object, int status, String code, Supplier<String> errorSupplier) {
        if (object == null) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }

    public static void isNull(Object object, int status, String code, String message) {
        if (object != null) {
            throw new HttpException(status, code, message);
        }
    }

    public static void isNull(Object object, int status, String code, Supplier<String> errorSupplier) {
        if (object != null) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }

    public static void notEmpty(Map<?, ?> map, int status, String code, String message) {
        if (ObjectUtils.isEmpty(map)) {
            throw new HttpException(status, code, message);
        }
    }

    public static void notEmpty(Map<?, ?> map, int status, String code, Supplier<String> errorSupplier) {
        if (ObjectUtils.isEmpty(map)) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }

    public static void isEmpty(Map<?, ?> map, int status, String code, String message) {
        if (ObjectUtils.isNotEmpty(map)) {
            throw new HttpException(status, code, message);
        }
    }

    public static void isEmpty(Map<?, ?> map, int status, String code, Supplier<String> errorSupplier) {
        if (ObjectUtils.isNotEmpty(map)) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }

    public static void notEmpty(Collection<?> collection, int status, String code, String message) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new HttpException(status, code, message);
        }
    }

    public static void notEmpty(Collection<?> collection, int status, String code, Supplier<String> errorSupplier) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }

    public static void isEmpty(Collection<?> collection, int status, String code, String message) {
        if (!CollectionUtils.isEmpty(collection)) {
            throw new HttpException(status, code, message);
        }
    }

    public static void isEmpty(Collection<?> collection, int status, String code, Supplier<String> errorSupplier) {
        if (!CollectionUtils.isEmpty(collection)) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }

    public static void notEmpty(Object[] array, int status, String code, String message) {
        if (ObjectUtils.isEmpty(array)) {
            throw new HttpException(status, code, message);
        }
    }

    public static void notEmpty(Object[] array, int status, String code, Supplier<String> errorSupplier) {
        if (ObjectUtils.isEmpty(array)) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }


    public static void isEmpty(Object[] array, int status, String code, String message) {
        if (!ObjectUtils.isEmpty(array)) {
            throw new HttpException(status, code, message);
        }
    }

    public static void isEmpty(Object[] array, int status, String code, Supplier<String> errorSupplier) {
        if (!ObjectUtils.isEmpty(array)) {
            throw new HttpException(status, code, errorSupplier.get());
        }
    }
}
