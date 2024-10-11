package com.cowave.commons.framework.access;

import org.springframework.feign.codec.Response;

/**
 *
 * @author shanhuiming
 *
 */
public interface AccessExceptionHandler {

    void handler(Exception e, int status, Response<Void> response);
}
