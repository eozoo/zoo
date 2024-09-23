/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.util;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 *
 * @author shanhuiming
 *
 */
public class Asserts {

	public static void isTrue(boolean expression, String message, Object... args) {
		if (!expression) {
			throw new AssertsException(message).args(args);
		}
	}

	public static void isTrue(boolean expression, Supplier<String> errorSupplier) {
		if (!expression) {
			throw new AssertsException(errorSupplier.get());
		}
	}

	public static void isFalse(boolean expression, String message, Object... args) {
		if (expression) {
			throw new AssertsException(message).args(args);
		}
	}

	public static void isFalse(boolean expression, Supplier<String> errorSupplier) {
		if (expression) {
			throw new AssertsException(errorSupplier.get());
		}
	}

	public static void notEquals(Object a, Object b, String message, Object... args) {
		if (Objects.equals(a, b)) {
			throw new AssertsException(message).args(args);
		}
	}

	public static void notEquals(Object a, Object b, Supplier<String> errorSupplier) {
		if (Objects.equals(a, b)) {
			throw new AssertsException(errorSupplier.get());
		}
	}

	public static void equals(Object a, Object b, String message, Object... args) {
		if (!Objects.equals(a, b)) {
			throw new AssertsException(message).args(args);
		}
	}

	public static void equals(Object a, Object b, Supplier<String> errorSupplier) {
		if (!Objects.equals(a, b)) {
			throw new AssertsException(errorSupplier.get());
		}
	}

	public static void notBlank(@Nullable String text, String message, Object... args) {
		if (!StringUtils.hasText(text)) {
			throw new AssertsException(message).args(args);
		}
	}

	public static void notBlank(@Nullable String text, Supplier<String> errorSupplier) {
		if (!StringUtils.hasText(text)) {
			throw new AssertsException(errorSupplier.get());
		}
	}

	public static void isBlank(@Nullable String text, String message, Object... args) {
		if (StringUtils.hasText(text)) {
			throw new AssertsException(message).args(args);
		}
	}

	public static void isBlank(@Nullable String text, Supplier<String> errorSupplier) {
		if (StringUtils.hasText(text)) {
			throw new AssertsException(errorSupplier.get());
		}
	}

	public static void notNull(@Nullable Object object, String message, Object... args) {
		if (object == null) {
			throw new AssertsException(message).args(args);
		}
	}

	public static void notNull(@Nullable Object object, Supplier<String> errorSupplier) {
		if (object == null) {
			throw new AssertsException(errorSupplier.get());
		}
	}

	public static void isNull(@Nullable Object object, String message, Object... args) {
		if (object != null) {
			throw new AssertsException(message).args(args);
		}
	}

	public static void isNull(@Nullable Object object, Supplier<String> errorSupplier) {
		if (object != null) {
			throw new AssertsException(errorSupplier.get());
		}
	}

	public static void notEmpty(@Nullable Map<?, ?> map, String message, Object... args) {
		if (ObjectUtils.isEmpty(map)) {
			throw new AssertsException(message).args(args);
		}
	}

	public static void notEmpty(@Nullable Map<?, ?> map, Supplier<String> errorSupplier) {
		if (ObjectUtils.isEmpty(map)) {
			throw new AssertsException(errorSupplier.get());
		}
	}

	public static void isEmpty(@Nullable Map<?, ?> map, String message, Object... args) {
		if (ObjectUtils.isNotEmpty(map)) {
			throw new AssertsException(message).args(args);
		}
	}

	public static void isEmpty(@Nullable Map<?, ?> map, Supplier<String> errorSupplier) {
		if (ObjectUtils.isNotEmpty(map)) {
			throw new AssertsException(errorSupplier.get());
		}
	}

	public static void notEmpty(@Nullable Collection<?> collection, String message, Object... args) {
		if (CollectionUtils.isEmpty(collection)) {
			throw new AssertsException(message).args(args);
		}
	}

	public static void notEmpty(@Nullable Collection<?> collection, Supplier<String> errorSupplier) {
		if (CollectionUtils.isEmpty(collection)) {
			throw new AssertsException(errorSupplier.get());
		}
	}

	public static void isEmpty(@Nullable Collection<?> collection, String message, Object... args) {
		if (!CollectionUtils.isEmpty(collection)) {
			throw new AssertsException(message).args(args);
		}
	}

	public static void isEmpty(@Nullable Collection<?> collection, Supplier<String> errorSupplier) {
		if (!CollectionUtils.isEmpty(collection)) {
			throw new AssertsException(errorSupplier.get());
		}
	}

	public static void notEmpty(@Nullable Object[] array, String message, Object... args) {
		if (org.springframework.util.ObjectUtils.isEmpty(array)) {
			throw new AssertsException(message).args(args);
		}
	}

	public static void notEmpty(@Nullable Object[] array, Supplier<String> errorSupplier) {
		if (org.springframework.util.ObjectUtils.isEmpty(array)) {
			throw new AssertsException(errorSupplier.get());
		}
	}


	public static void isEmpty(@Nullable Object[] array, String message, Object... args) {
		if (!org.springframework.util.ObjectUtils.isEmpty(array)) {
			throw new AssertsException(message).args(args);
		}
	}

	public static void isEmpty(@Nullable Object[] array, Supplier<String> errorSupplier) {
		if (!org.springframework.util.ObjectUtils.isEmpty(array)) {
			throw new AssertsException(errorSupplier.get());
		}
	}
}
