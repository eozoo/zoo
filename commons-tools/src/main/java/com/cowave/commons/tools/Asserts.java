/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
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

	public static void notBlank(String text, String message, Object... args) {
		if (StringUtils.isBlank(text)) {
			throw new AssertsException(message).args(args);
		}
	}

	public static void notBlank(String text, Supplier<String> errorSupplier) {
		if (StringUtils.isBlank(text)) {
			throw new AssertsException(errorSupplier.get());
		}
	}

	public static void isBlank(String text, String message, Object... args) {
		if (StringUtils.isBlank(text)) {
			throw new AssertsException(message).args(args);
		}
	}

	public static void isBlank(String text, Supplier<String> errorSupplier) {
		if (StringUtils.isBlank(text)) {
			throw new AssertsException(errorSupplier.get());
		}
	}

	public static void notNull(Object object, String message, Object... args) {
		if (object == null) {
			throw new AssertsException(message).args(args);
		}
	}

	public static void notNull(Object object, Supplier<String> errorSupplier) {
		if (object == null) {
			throw new AssertsException(errorSupplier.get());
		}
	}

	public static void isNull(Object object, String message, Object... args) {
		if (object != null) {
			throw new AssertsException(message).args(args);
		}
	}

	public static void isNull(Object object, Supplier<String> errorSupplier) {
		if (object != null) {
			throw new AssertsException(errorSupplier.get());
		}
	}

	public static void notEmpty(Map<?, ?> map, String message, Object... args) {
		if (ObjectUtils.isEmpty(map)) {
			throw new AssertsException(message).args(args);
		}
	}

	public static void notEmpty(Map<?, ?> map, Supplier<String> errorSupplier) {
		if (ObjectUtils.isEmpty(map)) {
			throw new AssertsException(errorSupplier.get());
		}
	}

	public static void isEmpty(Map<?, ?> map, String message, Object... args) {
		if (ObjectUtils.isNotEmpty(map)) {
			throw new AssertsException(message).args(args);
		}
	}

	public static void isEmpty(Map<?, ?> map, Supplier<String> errorSupplier) {
		if (ObjectUtils.isNotEmpty(map)) {
			throw new AssertsException(errorSupplier.get());
		}
	}

	public static void notEmpty(Collection<?> collection, String message, Object... args) {
		if (CollectionUtils.isEmpty(collection)) {
			throw new AssertsException(message).args(args);
		}
	}

	public static void notEmpty(Collection<?> collection, Supplier<String> errorSupplier) {
		if (CollectionUtils.isEmpty(collection)) {
			throw new AssertsException(errorSupplier.get());
		}
	}

	public static void isEmpty(Collection<?> collection, String message, Object... args) {
		if (!CollectionUtils.isEmpty(collection)) {
			throw new AssertsException(message).args(args);
		}
	}

	public static void isEmpty(Collection<?> collection, Supplier<String> errorSupplier) {
		if (!CollectionUtils.isEmpty(collection)) {
			throw new AssertsException(errorSupplier.get());
		}
	}

	public static void notEmpty(Object[] array, String message, Object... args) {
		if (ObjectUtils.isEmpty(array)) {
			throw new AssertsException(message).args(args);
		}
	}

	public static void notEmpty(Object[] array, Supplier<String> errorSupplier) {
		if (ObjectUtils.isEmpty(array)) {
			throw new AssertsException(errorSupplier.get());
		}
	}


	public static void isEmpty(Object[] array, String message, Object... args) {
		if (!ObjectUtils.isEmpty(array)) {
			throw new AssertsException(message).args(args);
		}
	}

	public static void isEmpty(Object[] array, Supplier<String> errorSupplier) {
		if (!ObjectUtils.isEmpty(array)) {
			throw new AssertsException(errorSupplier.get());
		}
	}
}
