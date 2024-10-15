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
