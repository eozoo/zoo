/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.filter.access;

import com.alibaba.fastjson.JSON;
import com.cowave.commons.framework.access.Access;
import com.cowave.commons.framework.access.AccessLogger;
import com.cowave.commons.tools.Messages;
import com.cowave.commons.tools.ServletUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.MimeHeaders;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.feign.codec.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConversionException;

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
@RequiredArgsConstructor
public class AccessFilter implements Filter {

    private final TransactionIdSetter transactionIdSetter;

    private final AccessIdGenerator accessIdGenerator;

    @Value("${spring.application.response-always-success:false}")
    private boolean responseAlwaysSuccess;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        // 检查获取accessId，设置请求和响应Header
        String accessId = httpServletRequest.getHeader("Access-Id");
        if (StringUtils.isBlank(accessId)) {
            accessId = accessIdGenerator.newAccessId();
        }
        httpServletResponse.setHeader("Access-Id", accessId);
        // MDC设置accessId
        MDC.put("accessId", accessId);
        // Access设置
        String accessIp = ServletUtils.getRequestIp(httpServletRequest);
        String accessUrl = httpServletRequest.getRequestURI();
        Access.set(new Access(accessId, accessIp, accessUrl, System.currentTimeMillis()));
        // 国际化设置
        String language = httpServletRequest.getHeader("Accept-Language");
        Messages.setLanguage(language);
        // 拦截记录请求信息，顺便获取设置下分页参数
        AccessRequestWrapper accessRequestWrapper = new AccessRequestWrapper(httpServletRequest);
        try{
            accessRequestWrapper.recordAccessParams();
        }catch (HttpMessageConversionException e){
            int httpStatus = BAD_REQUEST.getStatus();
            if(responseAlwaysSuccess){
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
        // 尝试获取设置Seata事务id
        String xid = httpServletRequest.getHeader("xid");
        if(transactionIdSetter != null && xid != null){
            transactionIdSetter.setXid(xid);
        }
        // servlet处理
        chain.doFilter(accessRequestWrapper, response);
        // 拦截打印响应（AccessLogger中没有拦截到的）
        Access access = Access.get();
        if(!access.isResponseLogged()){
            int status = httpServletResponse.getStatus();
            long cost = System.currentTimeMillis() - access.getAccessTime();
            if(status == HttpStatus.OK.value()){
                AccessLogger.info("<< {} {}ms", status, cost);
            }else{
                AccessLogger.warn("<< {} {}ms", status, cost);
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
            AccessLogger.error("", e);
        }
    }
}
