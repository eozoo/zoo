/*
 * Copyright (c) 2017～2024 Cowave All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.commons.response.exception;

import com.cowave.commons.response.ResponseCode;
import lombok.Getter;

/**
 * 异常日志只打印e.message
 *
 * @author shanhuiming
 */
@Getter
public class HttpWarnException extends HttpException {

    public HttpWarnException(String message, Object... args) {
        super(message, args);
    }

    public HttpWarnException(Throwable cause, String message, Object... args) {
        super(cause, message, args);
    }

    public HttpWarnException(ResponseCode responseCode) {
        super(responseCode);
    }

    public HttpWarnException(ResponseCode responseCode, String message, Object... args) {
       super(responseCode, message, args);
    }

    public HttpWarnException(ResponseCode responseCode, Throwable cause, String message, Object... args) {
        super(responseCode, cause, message, args);
    }

    public HttpWarnException(int status, String code) {
        super(status, code);
    }

    public HttpWarnException(int status, String code, String message, Object... args) {
        super(status, code, message, args);
    }

    public HttpWarnException(int status, String code, Throwable cause, String message, Object... args) {
        super(status, code, cause, message, args);
    }
}
