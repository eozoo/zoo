/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
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
