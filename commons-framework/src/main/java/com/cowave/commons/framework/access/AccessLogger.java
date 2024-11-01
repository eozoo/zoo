/*
 * Copyright (c) 2017～2024 Cowave All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.commons.framework.access;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cowave.commons.framework.access.filter.AccessIdGenerator;
import com.cowave.commons.response.HttpResponse;
import com.cowave.commons.response.Response;
import com.cowave.commons.tools.ServletUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.ExtendedServletRequestDataBinder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.cowave.commons.response.HttpResponseCode.SUCCESS;

/**
 *
 * @author shanhuiming
 *
 */
@Aspect
@RequiredArgsConstructor
@Component
public class AccessLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessLogger.class);

    private final ObjectMapper objectMapper;

    private final AccessIdGenerator accessIdGenerator;

    @Nullable
    private final AccessUserParser accessUserParser;

    @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.GetMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.PostMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.PutMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public void request() {

    }

    @Before("request()")
    public void logRequest(JoinPoint point) throws JsonProcessingException {
        Access access = Access.get();
        if(access == null){
            // 请求未经过AccessFilter
            HttpServletRequest httpServletRequest = Access.httpRequest();
            assert httpServletRequest != null;
            if (httpServletRequest.getRequestURI().equals(httpServletRequest.getContextPath() + "/error")) {
                return; // error路径直接跳过
            }

            MethodSignature signature = (MethodSignature)point.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] args = point.getArgs();
            Map<String, Object> map = new HashMap<>();
            if(paramNames != null) {
                for (int i = 0; i < args.length; i++) {
                    if (args[i] == null) {
                        map.put(paramNames[i], null);
                    } else {
                        Class<?> clazz = args[i].getClass();
                        if (MultipartFile.class.isAssignableFrom(clazz) || MultipartFile[].class.isAssignableFrom(clazz)
                                || HttpServletRequest.class.isAssignableFrom(clazz) || HttpServletResponse.class.isAssignableFrom(clazz)
                                || BeanPropertyBindingResult.class.isAssignableFrom(clazz)
                                || ExtendedServletRequestDataBinder.class.isAssignableFrom(clazz)) {
                            continue;
                        }

                        if (accessUserParser != null) {
                            accessUserParser.parse(clazz, args[i]);
                        }
                        map.put(paramNames[i], args[i]);
                    }
                }
            }

            String accessIp = ServletUtils.getRequestIp(httpServletRequest);
            String accessUrl = httpServletRequest.getRequestURI();
            access = new Access(accessIdGenerator.newAccessId(), accessIp, accessUrl, System.currentTimeMillis());
            access.setRequestParam(map);
            Access.set(access);

            String httpMethod = httpServletRequest.getMethod();
            String contentType = httpServletRequest.getContentType();
            StringBuilder builder = new StringBuilder();
            builder.append(">> ").append(httpMethod).append(" ").append(accessUrl).append(" [").append(accessIp);
            if(StringUtils.isNotBlank(contentType)){
                builder.append(" ").append(contentType).append("]");
            }else{
                builder.append("]");
            }
            builder.append(" args=").append(objectMapper.writeValueAsString(map));

            LOGGER.info(builder.toString());
        }else{
            // 请求经过AccessFilter
            if (accessUserParser != null) {
                MethodSignature signature = (MethodSignature) point.getSignature();
                String[] paramNames = signature.getParameterNames();
                if (paramNames != null) {
                    for (Object arg : point.getArgs()) {
                        if (arg != null) {
                            accessUserParser.parse(arg.getClass(), arg);
                        }
                    }
                }
            }
        }
    }

    @AfterReturning(pointcut = "request()", returning = "resp")
    public void logResponse(Object resp) throws JsonProcessingException {
        HttpServletResponse servletResponse = Access.httpResponse();
        if(servletResponse == null){
            return;
        }

        Access access = Access.get();
        if(access == null){
            return;
        }

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
                if(Objects.equals(code, SUCCESS.getCode())){
                    LOGGER.info("<< {} {}ms {code={}, msg={}}", status, cost, code, msg);
                }else{
                    if(!LOGGER.isInfoEnabled()){
                        LOGGER.warn("<< {} {}ms {code={}, msg={}} {} {}", status, cost, code, msg, access.getAccessUrl(), objectMapper.writeValueAsString(access.getRequestParam()));
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
                        LOGGER.warn("<< {} {}ms {} {} {}", status, cost, msg, access.getAccessUrl(), objectMapper.writeValueAsString(access.getRequestParam()));
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
                        LOGGER.warn("<< {} {}ms {} {}", status, cost, access.getAccessUrl(), objectMapper.writeValueAsString(access.getRequestParam()));
                    }else{
                        LOGGER.info("<< {} {}ms", status, cost);
                    }
                }
            }
        }else{
            if(response != null) {
                LOGGER.debug("<< {} {}ms {code={}, msg={}, data={}}", status, cost, code, msg, objectMapper.writeValueAsString(data));
            }else if(httpResponse != null){
                LOGGER.debug("<< {} {}ms {}", status, cost, objectMapper.writeValueAsString(data));
            }else{
                LOGGER.debug("<< {} {}ms {}", status, cost, objectMapper.writeValueAsString(resp));
            }
        }
    }

    public static boolean isInfoEnabled(){
        return LOGGER.isInfoEnabled();
    }

	public static void info(String format, Object... arguments){
        LOGGER.info(format, arguments);
	}

    public static void warn(String format, Object... arguments){
        LOGGER.warn(format, arguments);
	}

    public static void error(String format, Object... arguments){
        LOGGER.error(format, arguments);
	}
}
