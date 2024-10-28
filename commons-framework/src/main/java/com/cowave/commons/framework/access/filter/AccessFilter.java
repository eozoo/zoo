/*
 * Copyright (c) 2017～2024 Cowave All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.commons.framework.access.filter;

import com.alibaba.fastjson.JSON;
import com.cowave.commons.framework.access.Access;
import com.cowave.commons.framework.access.AccessProperties;
import com.cowave.commons.tools.Messages;
import com.cowave.commons.tools.ServletUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.MimeHeaders;
import org.slf4j.MDC;
import org.springframework.feign.codec.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;

import static org.springframework.feign.codec.ResponseCode.BAD_REQUEST;
import static org.springframework.feign.codec.ResponseCode.SUCCESS;

/**
 *
 * @author shanhuiming
 *
 */
@Slf4j
@RequiredArgsConstructor
public class AccessFilter implements Filter {

    private final TransactionIdSetter transactionIdSetter;

    private final AccessIdGenerator accessIdGenerator;

    private final AccessProperties accessProperties;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        // 获取accessId
        String accessId = httpServletRequest.getHeader("Access-Id");
        if (StringUtils.isBlank(accessId)) {
            accessId = accessIdGenerator.newAccessId();
        }
        // 获取国际化
        String language = httpServletRequest.getHeader("Accept-Language");
        // 获取Seata事务id
        String xid = httpServletRequest.getHeader("xid");

        // 设置响应头 Access-Id
        httpServletResponse.setHeader("Access-Id", accessId);
        // 设置响应头 Content-Security-Policy
        if(StringUtils.isNotBlank(accessProperties.getContentSecurityPolicy())){
            httpServletResponse.setHeader("Content-Security-Policy", accessProperties.getContentSecurityPolicy());
        }
        // 设置响应头 Access-Control
        AccessProperties.CrossControl crossControl = accessProperties.getControl();
        httpServletResponse.setHeader("Access-Control-Allow-Origin", crossControl.getAllowOrigin());
        httpServletResponse.setHeader("Access-Control-Allow-Methods", crossControl.getAllowMethods());
        httpServletResponse.setHeader("Access-Control-Allow-Headers", crossControl.getAllowHeaders());
        httpServletResponse.setHeader("Access-Control-Allow-Credentials", String.valueOf(crossControl.isAllowCredentials()));

        // 设置MDC.accessId
        MDC.put("accessId", accessId);
        // 设置国际化
        Messages.setLanguage(language);
        // 设置Seata事务id
        if(transactionIdSetter != null && xid != null){
            transactionIdSetter.setXid(xid);
        }
        // 设置Access
        String accessIp = ServletUtils.getRequestIp(httpServletRequest);
        String accessUrl = httpServletRequest.getRequestURI();
        Access.set(new Access(accessId, accessIp, accessUrl, System.currentTimeMillis()));

        // 记录请求日志，顺便获取设置下分页参数
        AccessRequestWrapper accessRequestWrapper = new AccessRequestWrapper(httpServletRequest);
        try{
            accessRequestWrapper.recordAccessParams();
        }catch (Exception e){
            int httpStatus = BAD_REQUEST.getStatus();
            if(accessProperties.isAlwaysSuccess()){
                httpStatus = SUCCESS.getStatus();
            }
            HttpServletResponse httpResponse = (HttpServletResponse)response;
            httpResponse.setCharacterEncoding("UTF-8");
            httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            httpResponse.setStatus(httpStatus);
            response.getWriter().write(JSON.toJSONString(
                    Response.msg(BAD_REQUEST, Messages.msg("frame.advice.httpMessageConversionException"))));
            return;
        }

        // servlet处理
        chain.doFilter(accessRequestWrapper, response);

        // 拦截打印响应（AccessLogger中没有拦截到的）
        Access access = Access.get();
        if(!access.isResponseLogged()){
            int status = httpServletResponse.getStatus();
            long cost = System.currentTimeMillis() - access.getAccessTime();
            if(status == HttpStatus.OK.value()){
                log.info("<< {} {}ms", status, cost);
            }else{
                log.warn("<< {} {}ms", status, cost);
            }
        }

        // 清除access
        Access.remove();
        MDC.remove("accessId");
    }

    public void headerAccessId(HttpServletRequest request, String value) {
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
            log.error("", e);
        }
    }
}
