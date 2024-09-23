package com.cowave.commons.framework.access;

import java.util.List;
import java.util.TreeMap;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
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
import org.springframework.feign.codec.RemoteChain;
import org.springframework.feign.codec.Response;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.ExtendedServletRequestDataBinder;

import com.alibaba.fastjson.JSON;

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
			+ "&& !execution(public * de.codecentric.boot..*Controller.*(..)) "
			+ "&& !execution(public * com.cowave.commons.framework.access.AccessErrorController.*(..)) ")
	public void point() {

	}

	@Before("point()")
	public void logRequest(JoinPoint point) {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = attributes.getRequest();
		String url = request.getRequestURI();
		String remote = request.getRemoteAddr();
		String requestId = request.getHeader("requestId");
		Thread.currentThread().setName(requestId);

		TreeMap<String, Object> map = new TreeMap<>();
		MethodSignature signature = (MethodSignature)point.getSignature();
		String[] paramNames = signature.getParameterNames();
		Object[] args = point.getArgs();
		if(paramNames != null) {
			for (int i = 0; i < args.length; i++) {
				if(args[i] == null){
					map.put(paramNames[i], null);
				}else{
					Class<?> clazz = args[i].getClass();
					if ("requestId".equals(paramNames[i])
							|| MultipartFile[].class.isAssignableFrom(clazz)
							|| MultipartFile.class.isAssignableFrom(clazz)
							|| HttpServletRequest.class.isAssignableFrom(clazz)
							|| HttpServletResponse.class.isAssignableFrom(clazz)
							|| BeanPropertyBindingResult.class.isAssignableFrom(clazz)
							|| ExtendedServletRequestDataBinder.class.isAssignableFrom(clazz)
							){
						continue;
					}

					if(accessUserParser != null) {
						accessUserParser.parse(clazz, args[i]);
					}
					map.put(paramNames[i], args[i]);
				}
			}
		}
		Access access = Access.get();
		access.setRequestParam(map);
		LOGGER.info(">> request  {} {} {}", remote, url, JSON.toJSONString(map));
	}

	@AfterReturning(returning = "resp", pointcut = "point()")
	public void logResponse(Object resp) {
		Access access = Access.get();

		Integer code = null;
		String msg = null;
		Object data = null;
		List<RemoteChain> chains = null;
		if (resp != null && Response.class.isAssignableFrom(resp.getClass())) {
			Response<?> rsp = (Response<?>) resp;
			rsp.setRequestId(access.getRequestId());
			code = rsp.getCode();
			msg = rsp.getMsg();
			data = rsp.getData();
			chains = rsp.getChains();
		}

		String url = access.getRequestUrl();
		long cost = 0L;
		if(access.getRequestTime() != null){
			cost = System.currentTimeMillis() - access.getRequestTime();
		}

		if(resp == null || !LOGGER.isDebugEnabled()){
			if(chains != null){
				StringBuilder builder = new StringBuilder();
				RemoteChain.buildeTree("", chains, builder);
				LOGGER.info("<< response {} {}ms {} {}\n{}", code, cost, url, msg, builder);
			}else{
				if(code != null) {
					LOGGER.info("<< response {} {}ms {} {}", code, cost, url, msg);
				}else {
					LOGGER.info("<< response {}ms {}", cost, url);
				}
			}
		}else{
			if(chains != null){
				StringBuilder builder = new StringBuilder();
				RemoteChain.buildeTree("", chains, builder);
				LOGGER.debug("<< response {} {}ms {} {} {}\n{}", code, cost, url, msg, JSON.toJSONString(data), builder);
			}else{
				if(code != null) {
					LOGGER.debug("<< response {} {}ms {} {} {}", code, cost, url, msg, JSON.toJSONString(data));
				}else {
					LOGGER.debug("<< response {}ms {} {}", cost, url, JSON.toJSONString(resp));
				}
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
