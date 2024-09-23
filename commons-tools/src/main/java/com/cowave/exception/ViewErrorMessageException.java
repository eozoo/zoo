/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.exception;

/**
 * 支持直接在页面展示错误信息的异常
 *
 * @author jiangbo
 * @date 2024/1/19
 */
public class ViewErrorMessageException extends RuntimeException {

    public ViewErrorMessageException() {
        super();
    }

    public ViewErrorMessageException(String message, Throwable t) {
        super(message, t);
    }

    public ViewErrorMessageException(String message) {
        super(message);
    }

    public ViewErrorMessageException(Throwable t) {
        super(t);
    }
}
