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
import java.util.concurrent.ThreadPoolExecutor;

import com.cowave.commons.framework.access.Access;
import com.cowave.commons.framework.access.AccessLogger;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationContext;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

/**
 *
 * @author shanhuiming
 *
 */
@RequiredArgsConstructor
@Aspect
@Component
public class OperationAspect {

	private final ThreadLocal<OperationHolder> localOperationHolder = new ThreadLocal<>();

	private final ExpressionParser exprParser = new SpelExpressionParser();

	private final ApplicationContext applicationContext;

	private final ThreadPoolExecutor applicationExecutor;

	@Pointcut("@annotation(com.cowave.commons.framework.helper.operation.Operation)")
	public void oplog() {

	}

	@Before("oplog() && @annotation(operation)")
	public void doBefore(JoinPoint joinPoint, Operation operation) {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();
		// 方法参数
		Object[] args = joinPoint.getArgs();
		// 方法参数名
		String[] paramNames = signature.getParameterNames();
		// 设置EvaluationContext
		EvaluationContext context = new StandardEvaluationContext();
		if(paramNames != null) {
			for (int i = 0; i < args.length; i++) {
				context.setVariable(paramNames[i], args[i]);
			}
		}
		// 日志属性
		OperationInfo opInfo = new OperationInfo();
		opInfo.setAccessTime(Access.accessTime());
		opInfo.setAccessIp(Access.accessIp());
		opInfo.setAccessUrl(Access.accessUrl());
		opInfo.setUserId(Access.userId());
		opInfo.setUserCode(Access.userCode());
		opInfo.setDeptId(Access.deptId());
		opInfo.setDeptCode(Access.deptCode());
		opInfo.setOpType(operation.type());
		opInfo.setOpAction(operation.action());
		localOperationHolder.set(new OperationHolder(opInfo, context));
	}

	@AfterReturning(pointcut = "oplog() && @annotation(operation)", returning = "resp")
	public void doAfter(JoinPoint joinPoint, Operation operation, Object resp) {
		OperationHolder operationHolder = localOperationHolder.get();
		localOperationHolder.remove();
		OperationInfo operationInfo = operationHolder.opInfo;
		operationInfo.setSuccess(true);
		// 日志处理时设置三个约定参数，opHandler、opInfo、resp、exception
		EvaluationContext context = operationHolder.evContext;
		context.setVariable("opHandler", applicationContext.getBean(operation.handlerClass()));
		context.setVariable("opInfo", operationInfo);
		context.setVariable("resp", resp);
		context.setVariable("exception", null);
		// 处理日志
		handleOperation(operation, context);
	}

	@AfterThrowing(pointcut = "oplog() && @annotation(operation)", throwing = "e")
	public void doThrow(JoinPoint joinPoint, Operation operation, Exception e) {
		OperationHolder operationHolder = localOperationHolder.get();
		localOperationHolder.remove();
		OperationInfo operationInfo = operationHolder.opInfo;
		operationInfo.setSuccess(false);
		// 日志处理时设置三个约定参数，opHandler、opInfo、resp、exception
		EvaluationContext context = operationHolder.evContext;
		context.setVariable("opHandler", applicationContext.getBean(operation.handlerClass()));
		context.setVariable("opInfo", operationInfo);
		context.setVariable("resp", null);
		context.setVariable("exception", e);
		// 处理日志
		handleOperation(operation, context);
	}

	private void handleOperation(Operation operation, EvaluationContext context){
		Expression expression = exprParser.parseExpression(operation.opExpr());
		if(operation.isAsync()){
			applicationExecutor.execute(() -> expression.getValue(context));
		}else{
			try{
				expression.getValue(context);
			}catch (Exception ex){
				AccessLogger.error("", ex);
			}
		}
	}

	record OperationHolder(OperationInfo opInfo, EvaluationContext evContext) {

	}
}
