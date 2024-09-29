/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.helper.operation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.feign.codec.Response;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.ExtendedServletRequestDataBinder;

/**
 *
 * @author shanhuiming
 *
 */
@RequiredArgsConstructor
@Aspect
@Component
public class OperationAspect implements ApplicationContextAware {

	private static final ThreadLocal<OperationLog> OPERATION = new ThreadLocal<>();

	@Nullable
	private final OperationHandler operationHandler;

	private ApplicationContext applicationContext;

	private final ThreadPoolExecutor applicationExecutor;

	@Override
	public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Pointcut("@annotation(com.cowave.commons.framework.helper.operation.Operation)")
	public void oplog() {

	}

	@Before("oplog() && @annotation(operation)")
	public void doBefore(JoinPoint joinPoint, Operation operation) {
		if(operationHandler == null) {
			return;
		}

		Map<String, Object> map = new HashMap<>();
		MethodSignature signature = (MethodSignature)joinPoint.getSignature();
		String[] paramNames = signature.getParameterNames();
		Object[] args = joinPoint.getArgs();
		if(paramNames != null) {
			for (int i = 0; i < args.length; i++) {
				if(args[i] == null){
					map.put(paramNames[i], null);
				}else{
					Class<?> clazz = args[i].getClass();
					if (MultipartFile[].class.isAssignableFrom(clazz)
							|| MultipartFile.class.isAssignableFrom(clazz)
							|| HttpServletRequest.class.isAssignableFrom(clazz)
							|| HttpServletResponse.class.isAssignableFrom(clazz)
							|| BeanPropertyBindingResult.class.isAssignableFrom(clazz)
							|| ExtendedServletRequestDataBinder.class.isAssignableFrom(clazz)
					){
						continue;
					}
					map.put(paramNames[i], args[i]);
				}
			}
		}

		OperationLog oplog = OperationLogLoader.newLog();
		oplog.initialize();
		oplog.setTypeCode(operation.type());
		oplog.setActionCode(operation.action());
		oplog.setRequest(map);
		if(operation.content() == Operation.Content.REQ || operation.content() == Operation.Content.ALL) {
			if(map.size() == 1) {
				oplog.putContent("req", map.values().iterator().next());
			}else {
				oplog.putContent("req", map);
			}
		}

		Class<? extends OperationParser> handlerClass =  operation.contentHandler();
		if(!EmptyOperationParser.class.isAssignableFrom(handlerClass)) {
			OperationParser operationHandler = applicationContext.getBean(handlerClass);
			operationHandler.parseRequestContent(signature.getMethod(), map, oplog);
		}
		OPERATION.set(oplog);
	}

	@AfterReturning(pointcut = "oplog() && @annotation(oper)", returning = "resp")
	public void doAfter(JoinPoint joinPoint, Operation oper, Object resp) {
		if(operationHandler == null) {
			return;
		}

		OperationLog operationLog = OPERATION.get();
		OPERATION.remove();
		operationLog.setLogStatus(OperationLog.SUCCESS);
		operationLog.setLogDesc(parseDesc(joinPoint, oper, resp));

		setResponse(operationLog, resp);
		if(oper.content() == Operation.Content.RESP || oper.content() == Operation.Content.ALL) {
			operationLog.putContent("resp", operationLog.getResponse());
		}

		Class<? extends OperationParser> handlerClass = oper.contentHandler();
		if(!EmptyOperationParser.class.isAssignableFrom(handlerClass)) {
			MethodSignature signature = (MethodSignature)joinPoint.getSignature();
			OperationParser operationHandler = applicationContext.getBean(handlerClass);
			operationHandler.parseResponseContent(signature.getMethod(), resp, operationLog);
		}

		if(operationLog.isAsync()){
			applicationExecutor.execute(() -> operationHandler.handle(operationLog));
		}else{
			operationHandler.handle(operationLog);
		}
	}

	@AfterThrowing(pointcut = "oplog() && @annotation(operation)", throwing = "e")
	public void doThrow(JoinPoint joinPoint, Operation operation, Exception e) {
		if(operationHandler == null) {
			return;
		}

		OperationLog operationLog = OPERATION.get();
		OPERATION.remove();
		operationLog.setLogStatus(OperationLog.FAIL);
		operationLog.setLogDesc(parseDesc(joinPoint, operation, null));
		Class<? extends OperationParser> handlerClass =  operation.contentHandler();
		if(!EmptyOperationParser.class.isAssignableFrom(handlerClass)) {
			MethodSignature signature = (MethodSignature)joinPoint.getSignature();
			OperationParser operationHandler = applicationContext.getBean(handlerClass);
			operationHandler.parseExceptionContent(signature.getMethod(), e, operationLog);
		}

		if(operationLog.isAsync()){
			applicationExecutor.execute(() -> operationHandler.handle(operationLog));
		}else{
			operationHandler.handle(operationLog);
		}
	}

	private String parseDesc(JoinPoint point, Operation operation, Object resp) {
		String desc = operation.desc();
		List<String> expressions = getExpressions(desc);
		if(CollectionUtils.isEmpty(expressions)){
			return desc;
		}

		Object[] args = point.getArgs();
		if(ArrayUtils.isEmpty(args)){
			return desc;
		}

		// 请求参数
		StandardEvaluationContext evalContext = new StandardEvaluationContext();
		MethodSignature signature = (MethodSignature)point.getSignature();
		String[] paramNames = signature.getParameterNames();
		for (int i = 0; i < args.length; i++) {
			evalContext.setVariable(paramNames[i], args[i]);
		}

		// 响应结果
		if(resp != null){
			if(Response.class.isAssignableFrom(resp.getClass()) ){
				resp = ((Response<?>)resp).getData();
				evalContext.setVariable("resp", resp);
			}else{
				evalContext.setVariable("resp", resp);
			}
		}

		// 变量替换
		SpelExpressionParser spelParser = new SpelExpressionParser();
		for(String expression : expressions){
			String express = expression.substring(2, expression.indexOf("}"));
			if(resp != null || !express.startsWith("resp.")) {
				String value = spelParser.parseExpression("#" + express).getValue(evalContext, String.class);
				if(value != null){
					desc = desc.replace(expression, value);
				}
			}
		}
		return desc;
	}

	private List<String> getExpressions(String expression){
		List<String> list = new ArrayList<>();
		int begin;
		int end = 0;
		while((begin = expression.indexOf("#{", end)) != -1){
			end = expression.indexOf("}", begin);
			list.add(expression.substring(begin, end + 1));
		}
		return list;
	}

	private void setResponse(OperationLog operationLog, Object resp) {
		if (ObjectUtils.isEmpty(resp)) {
			return;
		}

		if(Response.class.isAssignableFrom(resp.getClass())) {
			Response<?> response = (Response<?>)resp;
			operationLog.setResponse(response.getData());
		}else {
			operationLog.setResponse(resp);
		}
	}
}
