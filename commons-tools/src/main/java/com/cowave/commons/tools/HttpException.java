/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.tools;

import lombok.Getter;
import org.springframework.feign.codec.HttpCode;

/**
 *
 * @author shanhuiming
 *
 */
@Getter
public class HttpException extends RuntimeException {

    private final int status;

    private final String code;

    public HttpException(HttpCode httpCode) {
        super(httpCode.getMsg());
        this.code = httpCode.getCode();
        this.status = httpCode.getStatus();
    }

    public HttpException(HttpCode httpCode, String message, Object... args) {
        super(Messages.translateIfNeed(message, args));
        this.code = httpCode.getCode();
        this.status = httpCode.getStatus();
    }

    public HttpException(int status, String code, String message, Object... args) {
        super(Messages.translateIfNeed(message, args));
        this.code = code;
        this.status = status;
    }

    // 异常放在前面，避免方法签名混淆
    public HttpException(Throwable cause, int status, String code, String message, Object... args) {
        super(Messages.translateIfNeed(message, args), cause);
        this.code = code;
        this.status = status;
    }
}
