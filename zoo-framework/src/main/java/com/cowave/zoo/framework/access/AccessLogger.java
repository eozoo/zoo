/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.zoo.framework.access;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cowave.zoo.http.client.response.HttpResponse;
import com.cowave.zoo.http.client.response.Response;
import com.cowave.zoo.framework.access.filter.AccessIdGenerator;
import com.cowave.zoo.framework.access.security.AccessInfoSetter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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

import java.util.Objects;

import static com.cowave.zoo.http.client.constants.HttpCode.SUCCESS;

/**
 * @author shanhuiming
 */
@Aspect
@RequiredArgsConstructor
@Component
public class AccessLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessLogger.class);

    private final AccessIdGenerator accessIdGenerator;

    private final ObjectMapper objectMapper;

    @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.GetMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.PostMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.PatchMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.PutMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public void request() {

    }

    @Before("request()")
    public void logRequest(JoinPoint point) {
        HttpServletRequest httpServletRequest = Access.httpRequest();

        // 非Servlet忽略
        if (httpServletRequest == null) {
            return;
        }

        // error路径跳过
        if (httpServletRequest.getRequestURI().equals(httpServletRequest.getContextPath() + "/error")) {
            return;
        }

        // 补一下Access
        Access access = Access.get();
        if (access == null) {
            access = Access.newAccess(accessIdGenerator);
            Access.set(access);
        }

        // 尝试设置下参数对象AccessInfoSetter
        MethodSignature signature = (MethodSignature) point.getSignature();
        String[] paramNames = signature.getParameterNames();
        if (paramNames != null) {
            Object[] args = point.getArgs();
            for (Object arg : args) {
                if (arg == null) {
                    continue;
                }
                Class<?> clazz = arg.getClass();
                if (AccessInfoSetter.class.isAssignableFrom(clazz)) {
                    AccessInfoSetter infoSetter = (AccessInfoSetter) arg;
                    infoSetter.setAccessInfo();
                }
            }
        }
    }

    @AfterReturning(pointcut = "request()", returning = "resp")
    public void logResponse(Object resp) throws JsonProcessingException {
        HttpServletResponse servletResponse = Access.httpResponse();

        // 非Servlet忽略
        if (servletResponse == null) {
            return;
        }

        // 非http请求忽略
        Access access = Access.get();
        if (access == null) {
            return;
        }

        // 未经过AccessFilter忽略
        if (!access.isAccessFiltered()) {
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
            if (Response.class.isAssignableFrom(resp.getClass())) {
                response = (Response<?>) resp;
                data = response.getData();
                code = response.getCode();
                msg = response.getMsg() != null ? response.getMsg() : "";
            } else if (HttpResponse.class.isAssignableFrom(resp.getClass())) {
                httpResponse = (HttpResponse<?>) resp;
                data = httpResponse.getBody();
                status = httpResponse.getStatusCodeValue();
                msg = httpResponse.getMessage() != null ? httpResponse.getMessage() : "";
            }
        }

        if (resp == null || !LOGGER.isDebugEnabled()) {
            if (response != null) {
                // Response
                if (Objects.equals(code, SUCCESS.getCode())) {
                    LOGGER.info("<< {} {}ms {code={}, msg={}}", status, cost, code, msg);
                } else {
                    if (!LOGGER.isInfoEnabled()) {
                        LOGGER.warn("<< {} {}ms {code={}, msg={}} {} {}", status, cost, code, msg, access.getAccessUrl(), objectMapper.writeValueAsString(access.getRequestParam()));
                    } else {
                        LOGGER.warn("<< {} {}ms {code={}, msg={}}", status, cost, code, msg);
                    }
                }
            } else if (httpResponse != null) {
                // HttpResponse
                if (status == HttpStatus.OK.value()) {
                    LOGGER.info("<< {} {}ms {}", status, cost, msg);
                } else {
                    if (!LOGGER.isInfoEnabled()) {
                        LOGGER.warn("<< {} {}ms {} {} {}", status, cost, msg, access.getAccessUrl(), objectMapper.writeValueAsString(access.getRequestParam()));
                    } else {
                        LOGGER.warn("<< {} {}ms {}", status, cost, msg);
                    }
                }
            } else {
                // Others
                if (status == HttpStatus.OK.value()) {
                    LOGGER.info("<< {} {}ms", status, cost);
                } else {
                    if (!LOGGER.isInfoEnabled()) {
                        LOGGER.warn("<< {} {}ms {} {}", status, cost, access.getAccessUrl(), objectMapper.writeValueAsString(access.getRequestParam()));
                    } else {
                        LOGGER.info("<< {} {}ms", status, cost);
                    }
                }
            }
        } else {
            if (response != null) {
                LOGGER.debug("<< {} {}ms {code={}, msg={}, data={}}", status, cost, code, msg, objectMapper.writeValueAsString(data));
            } else if (httpResponse != null) {
                LOGGER.debug("<< {} {}ms {}", status, cost, objectMapper.writeValueAsString(data));
            } else {
                LOGGER.debug("<< {} {}ms {}", status, cost, objectMapper.writeValueAsString(resp));
            }
        }
    }

    public static boolean isInfoEnabled() {
        return LOGGER.isInfoEnabled();
    }

    public static void info(String format, Object... arguments) {
        LOGGER.info(format, arguments);
    }

    public static void warn(String format, Object... arguments) {
        LOGGER.warn(format, arguments);
    }

    public static void error(String format, Object... arguments) {
        LOGGER.error(format, arguments);
    }
}
