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
package com.cowave.zoo.framework.access.filter;

import com.cowave.zoo.framework.access.annotation.Sensitive;
import com.cowave.zoo.framework.access.annotation.SensitiveSerializerModifier;
import com.cowave.zoo.http.client.asserts.I18Messages;
import com.cowave.zoo.http.client.response.Response;
import com.cowave.zoo.framework.access.Access;
import com.cowave.zoo.framework.access.AccessLogger;
import com.cowave.zoo.framework.access.AccessProperties;
import com.cowave.zoo.framework.access.security.AccessUserDetails;
import com.cowave.zoo.tools.ServletUtils;
import com.cowave.zoo.tools.SpringContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.MimeHeaders;
import org.slf4j.MDC;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.cowave.zoo.framework.access.security.BearerTokenDelegate.*;
import static com.cowave.zoo.http.client.constants.HttpCode.BAD_REQUEST;
import static com.cowave.zoo.http.client.constants.HttpCode.SUCCESS;
import static com.cowave.zoo.http.client.constants.HttpHeader.*;

/**
 * @author shanhuiming
 */
@Slf4j
public class AccessFilter implements Filter {

    private final TransactionIdSetter transactionIdSetter;

    private final AccessIdGenerator accessIdGenerator;

    private final AccessProperties accessProperties;

    private final ObjectMapper objectMapper;

    private final ObjectWriter sensitiveWriter;

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final Map<String, Map<String, Set<String>>> sensitiveParamMap = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Class<?>>> sensitiveBodyMap = new ConcurrentHashMap<>();

    public AccessFilter(TransactionIdSetter transactionIdSetter, AccessIdGenerator accessIdGenerator,
                        AccessProperties accessProperties, ObjectMapper objectMapper) {
        this.transactionIdSetter = transactionIdSetter;
        this.accessIdGenerator = accessIdGenerator;
        this.accessProperties = accessProperties;
        this.objectMapper = objectMapper;

        ObjectMapper sensitiveMapper = objectMapper.copy();
        SimpleModule module = new SimpleModule();
        module.setSerializerModifier(new SensitiveSerializerModifier());
        sensitiveMapper.registerModule(module);
        this.sensitiveWriter = sensitiveMapper.writer();

        DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();
        RequestMappingHandlerMapping requestMappingHandlerMapping = SpringContext.getBean("requestMappingHandlerMapping");
        Map<RequestMappingInfo, HandlerMethod> handlerMethodMap = requestMappingHandlerMapping.getHandlerMethods();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethodMap.entrySet()) {
            RequestMappingInfo requestMappingInfo = entry.getKey();
            HandlerMethod handlerMethod = entry.getValue();
            // param参数，保存<url，参数名称集合>
            Set<String> paramFields = new HashSet<>();
            // body参数，保存<url, body对象class>
            Class<?> bodyClass = null;
            Method method = handlerMethod.getMethod();
            String[] names = discoverer.getParameterNames(method);
            Parameter[] parameters = method.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                Class<?> paramType = parameter.getType();
                if (parameter.isAnnotationPresent(RequestParam.class) && parameter.isAnnotationPresent(Sensitive.class)) {
                    // @RequestParam参数，可能被重命名，要取实际的参数名
                    RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
                    String paramName = requestParam.name();
                    if (StringUtils.isBlank(paramName)) {
                        paramName = requestParam.value();
                    }
                    if (StringUtils.isBlank(paramName)) {
                        paramName = names[i];
                    }
                    paramFields.add(paramName);
                } else if (parameter.isAnnotationPresent(RequestBody.class)) {
                    // @RequestBody参数，扫描其中字段，只要存在有@Sensitive的字段，就记录这个paramType
                    if (hasSensitiveFields(paramType)) {
                        bodyClass = paramType;
                    }
                } else if (parameter.isAnnotationPresent(Sensitive.class) && isPrimitiveOrWrapper(paramType)) {
                    // 值参数，有@Sensitive，当成Param处理
                    paramFields.add(names[i]);
                } else if (!isPrimitiveOrWrapper(paramType)) {
                    // 对象参数，没有注解，查找标记了@Sensitive的字段，当成Param处理
                    paramFields.addAll(findSensitiveFields(paramType));
                }
            }

            if (paramFields.isEmpty() && bodyClass == null) {
                continue;
            }

            Set<String> urlPatterns = requestMappingInfo.getPathPatternsCondition().getPatternValues();
            Set<RequestMethod> httpMethods = requestMappingInfo.getMethodsCondition().getMethods();
            if (httpMethods.isEmpty()) {
                for (String urlPattern : urlPatterns) {
                    if (!paramFields.isEmpty()) {
                        sensitiveParamMap.computeIfAbsent("ALL",
                                k -> new HashMap<>()).put(urlPattern, paramFields);
                    }
                    if (bodyClass != null) {
                        sensitiveBodyMap.computeIfAbsent("ALL",
                                k -> new HashMap<>()).put(urlPattern, bodyClass);
                    }
                }
            } else {
                for (RequestMethod httpMethod : httpMethods) {
                    for (String urlPattern : urlPatterns) {
                        if (!paramFields.isEmpty()) {
                            sensitiveParamMap.computeIfAbsent(httpMethod.name(),
                                    k -> new HashMap<>()).put(urlPattern, paramFields);
                        }
                        if (bodyClass != null) {
                            sensitiveBodyMap.computeIfAbsent(httpMethod.name(),
                                    k -> new HashMap<>()).put(urlPattern, bodyClass);
                        }
                    }
                }
            }
        }
    }

    private boolean hasSensitiveFields(Class<?> clazz) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(Sensitive.class)) {
                    return true;
                }
            }
            current = current.getSuperclass();
        }
        return false;
    }

    private Set<String> findSensitiveFields(Class<?> clazz) {
        Set<String> fields = new HashSet<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(Sensitive.class)) {
                    fields.add(field.getName());
                }
            }
            current = current.getSuperclass();
        }
        return fields;
    }

    private boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive() ||
                clazz.equals(String.class) ||
                clazz.equals(Integer.class) ||
                clazz.equals(Long.class) ||
                clazz.equals(Double.class) ||
                clazz.equals(Float.class) ||
                clazz.equals(Boolean.class) ||
                clazz.equals(Character.class) ||
                clazz.equals(Byte.class) ||
                clazz.equals(Short.class);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        // X-Request-ID
        String accessId = httpServletRequest.getHeader(X_Request_ID);
        if (StringUtils.isBlank(accessId)) {
            accessId = accessIdGenerator.newAccessId();
        }
        // Accept-Language
        String language = httpServletRequest.getHeader(Accept_Language);
        // 获取Seata事务id
        String xid = httpServletRequest.getHeader("xid");

        // 设置响应头
        httpServletResponse.setHeader(X_Request_ID, accessId);
        // 设置响应头 Content-Security-Policy
        if (StringUtils.isNotBlank(accessProperties.getContentSecurityPolicy())) {
            httpServletResponse.setHeader(Content_Security_Policy, accessProperties.getContentSecurityPolicy());
        }
        // 设置响应头 Access-Control
        AccessProperties.CrossControl crossControl = accessProperties.getCross();
        httpServletResponse.setHeader(Access_Control_Allow_Origin, crossControl.getAllowOrigin());
        httpServletResponse.setHeader(Access_Control_Allow_Methods, crossControl.getAllowMethods());
        httpServletResponse.setHeader(Access_Control_Allow_Headers, crossControl.getAllowHeaders());
        httpServletResponse.setHeader(Access_Control_Allow_Credentials, String.valueOf(crossControl.isAllowCredentials()));

        // 设置MDC.accessId
        MDC.put("accessId", accessId);
        // 设置国际化
        I18Messages.setLanguage(language);
        // 设置Seata事务id
        if (transactionIdSetter != null && xid != null) {
            transactionIdSetter.setXid(xid);
        }
        // 设置Access
        String accessIp = ServletUtils.getRequestIp(httpServletRequest);
        String accessUrl = httpServletRequest.getRequestURI();
        String method = httpServletRequest.getMethod().toLowerCase();

        Access access = new Access(true, accessId, accessIp, accessUrl, method, System.currentTimeMillis());
        parseAuthorizationIfNeed(access);
        Access.set(access);

        // 日志脱敏信息
        Set<String> sensitiveParamFields = findMatchingValue(sensitiveParamMap, httpServletRequest);
        Class<?> sensitiveBodyClass = findMatchingValue(sensitiveBodyMap, httpServletRequest);

        // 请求参数、日志
        AccessRequestWrapper accessRequestWrapper = new AccessRequestWrapper(
                httpServletRequest, objectMapper, sensitiveWriter, access, sensitiveParamFields, sensitiveBodyClass);
        try {
            accessRequestWrapper.recordAccessParams();
        } catch (Exception e) {
            AccessLogger.error("", e);
            int httpStatus = BAD_REQUEST.getStatus();
            if (accessProperties.isAlwaysSuccess()) {
                httpStatus = SUCCESS.getStatus();
            }
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setCharacterEncoding("UTF-8");
            httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            httpResponse.setStatus(httpStatus);
            response.getWriter().write(objectMapper.writeValueAsString(
                    Response.msg(BAD_REQUEST, I18Messages.msg("frame.advice.httpMessageConversionException"))));
            return;
        }

        // servlet处理
        chain.doFilter(accessRequestWrapper, response);

        // 拦截打印响应（如果AccessLogger中没有打印）
        if (!access.isResponseLogged()) {
            int status = httpServletResponse.getStatus();
            long cost = System.currentTimeMillis() - access.getAccessTime();
            if (status == SUCCESS.getStatus()) {
                AccessLogger.info("<< {} {}ms", status, cost);
            } else {
                if (!AccessLogger.isInfoEnabled()) {
                    AccessLogger.warn("<< {} {}ms {} {}", status, cost,
                            access.getAccessUrl(), sensitiveWriter.writeValueAsString(access.getAccessLogParams()));
                } else {
                    AccessLogger.warn("<< {} {}ms", status, cost);
                }
            }
        }

        // 清除access
        Access.remove();
        MDC.remove("accessId");
    }

    private <T> T findMatchingValue(Map<String, Map<String, T>> map, HttpServletRequest httpServletRequest) {
        String url = httpServletRequest.getRequestURI();
        Map<String, T> methodMap = map.get(httpServletRequest.getMethod());
        if (methodMap != null) {
            for (Map.Entry<String, T> entry : methodMap.entrySet()) {
                if (antPathMatcher.match(entry.getKey(), url)) {
                    return entry.getValue();
                }
            }
        }
        Map<String, T> allMap = map.get("ALL");
        if (allMap != null) {
            for (Map.Entry<String, T> entry : allMap.entrySet()) {
                if (antPathMatcher.match(entry.getKey(), url)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    private void parseAuthorizationIfNeed(Access access) {
        String jwt = Access.getRequestHeader(accessProperties.tokenKey());
        if (StringUtils.isBlank(jwt) || accessProperties.authEnable()) {
            return;
        }

        String[] parts = jwt.split("\\.");
        if (parts.length != 3) {
            return;
        }

        // 如果携带了访问令牌但是服务没有开启鉴权，这里解析一下认证信息
        String payload = parts[1];
        String json = new String(Base64.getUrlDecoder().decode(payload));
        try {
            Map<String, Object> claims = objectMapper.readValue(json, Map.class);
            AccessUserDetails userDetails = new AccessUserDetails();
            userDetails.setAuthType((String) claims.get(CLAIM_TYPE));
            userDetails.setAccessId((String) claims.get(CLAIM_ACCESS_ID));
            userDetails.setRefreshId((String) claims.get(CLAIM_REFRESH_ID));
            // tenant
            userDetails.setTenantId((String) claims.get(CLAIM_TENANT_ID));
            // user
            userDetails.setUserId(claims.get(CLAIM_USER_ID));
            userDetails.setUserCode(claims.get(CLAIM_USER_CODE));
            userDetails.setUsername((String) claims.get(CLAIM_USER_ACCOUNT));
            userDetails.setUserNick((String) claims.get(CLAIM_USER_NAME));
            userDetails.setUserProperties((Map<String, Object>) claims.get(CLAIM_USER_PROPERTIES));
            // dept
            userDetails.setDeptId(claims.get(CLAIM_DEPT_ID));
            userDetails.setDeptCode(claims.get(CLAIM_DEPT_CODE));
            userDetails.setDeptName((String) claims.get(CLAIM_DEPT_NAME));
            // cluster
            userDetails.setClusterId((Integer) claims.get(CLAIM_CLUSTER_ID));
            userDetails.setClusterLevel((Integer) claims.get(CLAIM_CLUSTER_LEVEL));
            userDetails.setClusterName((String) claims.get(CLAIM_CLUSTER_NAME));
            // roles
            userDetails.setRoles((List<String>) claims.get(CLAIM_USER_ROLE));
            // permits
            userDetails.setPermissions((List<String>) claims.get(CLAIM_USER_PERM));
            // 设置
            access.setUserDetails(userDetails);
        } catch (JsonProcessingException e) {
            log.error("", e);
        }
    }

    private void headerAccessId(HttpServletRequest request, String value) {
        Class<?> clazz = request.getClass();
        try {
            Field req = clazz.getDeclaredField("request");
            req.setAccessible(true);
            Object o = req.get(request);

            Field coyoteRequest = o.getClass().getDeclaredField("coyoteRequest");
            coyoteRequest.setAccessible(true);
            Object oo = coyoteRequest.get(o);

            Field headers = oo.getClass().getDeclaredField("headers");
            headers.setAccessible(true);
            MimeHeaders mine = (MimeHeaders) headers.get(oo);
            mine.addValue("Access-Id").setString(value);
        } catch (Exception e) {
            // never will happened
            AccessLogger.error("", e);
        }
    }
}
