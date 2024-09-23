/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.entry;

import lombok.Data;

/**
 * @author jiangbo
 * @date 2024/1/2
 */
@Data
public class Result<T> {

    private boolean success;

    private String msg;

    private T data;

    public static <T> Result<T> success(T t) {
        Result<T> result = new Result<>();
        result.setSuccess(true);
        result.setData(t);
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.setSuccess(false);
        result.setMsg(msg);
        return result;
    }

    public static <T> Result<T> apply(boolean success, T data) {
        Result<T> result = new Result<>();
        result.setSuccess(success);
        result.setData(data);
        return result;
    }

    public boolean isFailed() {
        return !success;
    }
}
