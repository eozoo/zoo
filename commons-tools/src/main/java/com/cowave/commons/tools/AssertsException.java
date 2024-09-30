/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.tools;

/**
 *
 * @author shanhuiming
 *
 */
public class AssertsException extends RuntimeException {

	public AssertsException(String message, Object... args) {
        super(Messages.translateIfNeed(message, args));
    }

    public AssertsException(Throwable cause, String message, Object... args) {
        super(Messages.translateIfNeed(message, args), cause);
    }
}
