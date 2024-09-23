/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.access;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.feign.codec.HttpResponse;
import org.springframework.feign.codec.Response;
import org.springframework.feign.codec.ResponseCode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;

import java.util.Objects;

/**
 *
 * @author shanhuiming
 *
 */
@RequiredArgsConstructor
@Aspect
@Component
public class AccessLogger {

	private static final Logger LOGGER = LoggerFactory.getLogger(AccessLogger.class);

	@Nullable
	private final AccessUserParser accessUserParser;

	@Pointcut("execution(public * *..*Controller.*(..)) "
			+ "&& !execution(public * org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController.*(..)) ")
	public void point() {

	}

	@Before("point()")
	public void logRequest(JoinPoint point) {
		if (accessUserParser != null) {
			MethodSignature signature = (MethodSignature)point.getSignature();
			String[] paramNames = signature.getParameterNames();
			if(paramNames != null) {
				for (Object arg : point.getArgs()) {
					if (arg != null) {
						accessUserParser.parse(arg.getClass(), arg);
					}
				}
			}
		}
	}

	@AfterReturning(returning = "resp", pointcut = "point()")
	public void logResponse(Object resp) {
		HttpServletResponse servletResponse = Access.httpResponse();
		if(servletResponse == null){
			return;
		}

		Access access = Access.get();
		access.setResponseLogged(true);
		int status = servletResponse.getStatus();
		long cost = System.currentTimeMillis() - access.getAccessTime();

		String code = null;
		String msg = null;
		Object data = null;
		Response<?> response = null;
		HttpResponse<?> httpResponse = null;
		if (resp != null) {
			if(Response.class.isAssignableFrom(resp.getClass())){
				response = (Response<?>) resp;
				data = response.getData();
				code = response.getCode();
				msg = response.getMsg() != null ? response.getMsg() : "";
			}else if(HttpResponse.class.isAssignableFrom(resp.getClass())){
				httpResponse = (HttpResponse<?>) resp;
				data = httpResponse.getBody();
				status = httpResponse.getStatusCodeValue();
				msg = httpResponse.getMessage() != null ? httpResponse.getMessage() : "";
			}
		}

		if(resp == null || !LOGGER.isDebugEnabled()){
			if(response != null) {
				// Response
				if(Objects.equals(code, ResponseCode.SUCCESS.getCode())){
					LOGGER.info("<< {} {}ms {code={}, msg={}}", status, cost, code, msg);
				}else{
					if(!LOGGER.isInfoEnabled()){
						LOGGER.warn("<< {} {}ms {code={}, msg={}} {} {}", status, cost, code, msg, access.getAccessUrl(), JSON.toJSONString(access.getRequestParam()));
					}else{
						LOGGER.warn("<< {} {}ms {code={}, msg={}}", status, cost, code, msg);
					}
				}
			}else if(httpResponse != null){
				// HttpResponse
				if(status == HttpStatus.OK.value()){
					LOGGER.info("<< {} {}ms {}", status, cost, msg);
				}else{
					if(!LOGGER.isInfoEnabled()){
						LOGGER.warn("<< {} {}ms {} {} {}", status, cost, msg, access.getAccessUrl(), JSON.toJSONString(access.getRequestParam()));
					}else{
						LOGGER.warn("<< {} {}ms {}", status, cost, msg);
					}
				}
			}else{
				// Others
				if(status == HttpStatus.OK.value()){
					LOGGER.info("<< {} {}ms", status, cost);
				}else{
					if(!LOGGER.isInfoEnabled()){
						LOGGER.warn("<< {} {}ms {} {}", status, cost, access.getAccessUrl(), JSON.toJSONString(access.getRequestParam()));
					}else{
						LOGGER.info("<< {} {}ms", status, cost);
					}
				}
			}
		}else{
			if(response != null) {
				LOGGER.debug("<< {} {}ms {code={}, msg={}, data={}}", status, cost, code, msg, JSON.toJSONString(data));
			}else if(httpResponse != null){
				LOGGER.debug("<< {} {}ms {}", status, cost, JSON.toJSONString(data));
			}else{
				LOGGER.debug("<< {} {}ms {}", status, cost, JSON.toJSONString(resp));
			}
		}
	}

	public static void info(String msg){
		LOGGER.info(msg);
	}

	public static void info(String format, Object... arguments){
		LOGGER.info(format, arguments);
	}

	public static void warn(String msg){
		LOGGER.warn(msg);
	}

	public static void warn(String format, Object... arguments){
		LOGGER.warn(format, arguments);
	}

	public static void error(String msg){
		LOGGER.error(msg);
	}

	public static void error(String format, Object... arguments){
		LOGGER.error(format, arguments);
	}
}
