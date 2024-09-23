/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.helper.operation;

import java.lang.reflect.Method;
import java.util.Map;

import com.cowave.commons.framework.helper.operation.OperationHandler;
import com.cowave.commons.framework.helper.operation.OperationLog;
import org.springframework.stereotype.Component;

/**
 *
 * @author shanhuiming
 *
 */
@Component
public class EmptyOperationHandler implements OperationHandler {

	@Override
	public void pareseRequestContent(Method method, Map<String, Object> args, OperationLog log) {

	}

	@Override
	public void pareseResponseContent(Method method, Object resp, OperationLog log) {

	}

	@Override
	public void pareseExceptionContent(Method method, Exception e, OperationLog log) {

	}
}
