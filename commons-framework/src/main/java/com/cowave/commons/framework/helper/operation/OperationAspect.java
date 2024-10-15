/*
 * Copyright (c) 2017～2024 Cowave All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.commons.framework.helper.operation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import com.cowave.commons.framework.access.Access;
import com.cowave.commons.framework.access.AccessLogger;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationContext;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.ExtendedServletRequestDataBinder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author shanhuiming
 *
 */
@RequiredArgsConstructor
@Aspect
@Component
public class OperationAspect {

	private final ExpressionParser exprParser = new SpelExpressionParser();

	private final ApplicationContext applicationContext;

	private final ThreadPoolExecutor applicationExecutor;

	@Pointcut("@annotation(com.cowave.commons.framework.helper.operation.Operation)")
	public void oplog() {

	}

	@AfterReturning(pointcut = "oplog() && @annotation(operation)", returning = "resp")
	public void doAfter(JoinPoint joinPoint, Operation operation, Object resp) {
		OperationInfo operationInfo = new OperationInfo();
		EvaluationContext context = new StandardEvaluationContext();
		prepareOperation(joinPoint, operation, operationInfo, context);
		// Evaluation参数
		context.setVariable("resp", resp);
		context.setVariable("exception", null);
		// 操作信息
		operationInfo.setSuccess(true);
		operationInfo.setSummary(parseSummary(operation, context));
		context.setVariable("opInfo", operationInfo);
		// 处理日志
		handleOperation(operation, context);
	}

	@AfterThrowing(pointcut = "oplog() && @annotation(operation)", throwing = "e")
	public void doThrow(JoinPoint joinPoint, Operation operation, Exception e) {
		OperationInfo operationInfo = new OperationInfo();
		EvaluationContext context = new StandardEvaluationContext();
		prepareOperation(joinPoint, operation, operationInfo, context);
		// Evaluation参数
		context.setVariable("resp", null);
		context.setVariable("exception", e);
		// 操作信息
		operationInfo.setSuccess(false);
		operationInfo.setSummary(parseSummary(operation, context));
		context.setVariable("opInfo", operationInfo);
		// 处理日志
		handleOperation(operation, context);
	}

	private void prepareOperation(JoinPoint joinPoint, Operation operation, OperationInfo opInfo, EvaluationContext context){
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		// 方法参数
		Object[] args = joinPoint.getArgs();
		// 方法参数名
		String[] paramNames = signature.getParameterNames();
		// 设置EvaluationContext
		Map<String, Object> argMap = new HashMap<>();
		if(paramNames != null) {
			for (int i = 0; i < args.length; i++) {
				context.setVariable(paramNames[i], args[i]);
				if (args[i] != null) {
					// 去掉一些不能序列化的参数，避免后面一些对argMap的操作失败
					Class<?> clazz = args[i].getClass();
					if (MultipartFile.class.isAssignableFrom(clazz)
							|| MultipartFile[].class.isAssignableFrom(clazz)
							|| HttpServletRequest.class.isAssignableFrom(clazz)
							|| HttpServletResponse.class.isAssignableFrom(clazz)
							|| BeanPropertyBindingResult.class.isAssignableFrom(clazz)
							|| ExtendedServletRequestDataBinder.class.isAssignableFrom(clazz)) {
						continue;
					}
				}
				argMap.put(paramNames[i], args[i]);
			}
		}
		context.setVariable("opHandler", applicationContext.getBean(operation.handler()));
		// 设置OperationInfo
		opInfo.setAccessTime(Access.accessTime());
		opInfo.setAccessIp(Access.accessIp());
		opInfo.setAccessUrl(Access.accessUrl());
		opInfo.setUserId(Access.userId());
		opInfo.setUserCode(Access.userCode());
		opInfo.setDeptId(Access.deptId());
		opInfo.setDeptCode(Access.deptCode());
		opInfo.setOpType(operation.type());
		opInfo.setOpAction(operation.action());
		opInfo.setOpArgs(argMap);
		opInfo.setOpCost(System.currentTimeMillis() - Access.accessTime().getTime());
	}

	private String parseSummary(Operation operation, EvaluationContext context){
		String summarySpel = operation.summary();
		if(StringUtils.isBlank(summarySpel)){
			return "";
		}

		try{
			return exprParser.parseExpression(summarySpel, new TemplateParserContext()).getValue(context, String.class);
		}catch(Exception e){
			AccessLogger.error("", e);
			return "";
		}
	}

	private void handleOperation(Operation operation, EvaluationContext context){
		if(operation.isAsync()){
			applicationExecutor.execute(() -> exprParser.parseExpression(operation.expr()).getValue(context));
		}else{
			try{
				exprParser.parseExpression(operation.expr()).getValue(context);
			}catch (Exception ex){
				AccessLogger.error("", ex);
			}
		}
	}
}
