package com.cowave.commons.framework.helper.operation;

import java.lang.reflect.Method;
import java.util.Map;

/**
 *
 * @author shanhuiming
 *
 */
public interface OperationHandler {

	void pareseRequestContent(Method method, Map<String, Object> args, OperationLog log);

	void pareseResponseContent(Method method, Object resp, OperationLog log);

	void pareseExceptionContent(Method method, Exception e, OperationLog log);
}
