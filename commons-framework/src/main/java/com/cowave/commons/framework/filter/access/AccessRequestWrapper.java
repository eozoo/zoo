/*
 * Copyright (c) 2017～2024 Cowave All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.commons.framework.filter.access;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.PropertyFilter;
import com.cowave.commons.framework.access.Access;
import com.cowave.commons.tools.Converts;
import com.cowave.commons.tools.ServletUtils;
import com.github.pagehelper.page.PageMethod;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 *
 * @author shanhuiming
 *
 */
@Slf4j
public class AccessRequestWrapper extends HttpServletRequestWrapper {
    private static final String PAGE = "page";
    private static final String PAGE_NUM = "pageNum";
    private static final String PAGE_INDEX = "pageIndex";
    private static final String PAGE_SIZE = "pageSize";
    private static final String IS_ASC = "isAsc";
    private static final String ORDER_COLUMN = "orderColumn";

    private String body = "";

    private final String methodName;

    private final String contentType;

    public AccessRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        this.methodName = getMethod();
        this.contentType = getContentType();
        setCharacterEncoding("UTF-8");
        setCharacterEncoding("UTF-8");
        if (StringUtils.startsWithIgnoreCase(contentType, MediaType.APPLICATION_JSON_VALUE)) {
            body = ServletUtils.getRequestBody(request);
        }
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        return new ServletInputStream() {
            @Override
            public int read() {
                return bais.read();
            }

            @Override
            public int available() {
                return bytes.length;
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }
        };
    }

    @SuppressWarnings("unchecked")
    public void recordAccessParams() {
        String url = getRequestURI();
        String remote = getRemoteAddr();

        // params
        Map<String, String> paramMap = new HashMap<>();
        Enumeration<String> parameterNames = getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String[] paramValues = getParameterValues(paramName);
            if(paramValues.length > 1){
                paramMap.put(paramName, String.join(", ", paramValues));
            }else if(paramValues.length == 1){
                paramMap.put(paramName, paramValues[0]);
            }
        }

        // body Object/Array
        Map<String, Object> bodyMap = new HashMap<>();
        List<Map> bodyArray = new ArrayList<>();
        if (StringUtils.isNotBlank(body)) {
            if(body.startsWith("[")){
                bodyArray = JSON.parseArray(body, Map.class);
            }else{
                bodyMap = JSON.parseObject(body, Map.class);
            }
        }

        Map<String, Object> requestParams = new HashMap<>();

        // 请求日志
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append(">> ").append(methodName).append(" ").append(url).append(" [").append(remote);
        if(StringUtils.isNotBlank(contentType)){
            logBuilder.append(" ").append(contentType).append("]");
        }else{
            logBuilder.append("]");
        }
        if(!paramMap.isEmpty()){
            requestParams.put("params", paramMap);
            logBuilder.append(" params=").append(JSON.toJSONString(paramMap, new PasswordFilter()));
        }
        if(!bodyMap.isEmpty()){
            requestParams.put("body", bodyMap);
            logBuilder.append(" body=").append(JSON.toJSONString(bodyMap, new PasswordFilter()));
        }
        if(!bodyArray.isEmpty()){
            requestParams.put("body", bodyArray);
            logBuilder.append(" body=").append(JSON.toJSONString(bodyArray, new PasswordFilter()));
        }
        log.info(logBuilder.toString());

        // 记录请求参数
        Access access = Access.get();
        access.setRequestParam(requestParams);

        // 尝试获取分页参数
        Object index = getPageIndex(paramMap);
        if(index == null){
            index = getPageIndex(bodyMap);
        }
        Object size = getPageSize(paramMap);
        if(size == null){
            size = getPageSize(bodyMap);
        }

        // 记录分页参数
        Integer pageIndex = Converts.toInt(index, null);
        Integer pageSize = Converts.toInt(size, null);
        access.setPageIndex(pageIndex);
        access.setPageSize(pageSize);

        // 避免被线程复用，先清除下设置
        PageMethod.clearPage();
        // 设置PageHelper，如果引入依赖的话这里检测到分页参数就默认设置
        if(index != null && size != null){
            String orderByColumn = paramMap.get(ORDER_COLUMN);
            String isAsc = paramMap.get(IS_ASC);
            String orderBy = getOrderBy(orderByColumn, isAsc);
            PageMethod.startPage(pageIndex, pageSize, orderBy).setReasonable(true);
        }
    }

    private Object getPageIndex(Map<String, ?> paramMap){
        Object page = paramMap.get(PAGE);
        if(page == null){
            page = paramMap.get(PAGE_NUM);
        }
        if(page == null){
            page = paramMap.get(PAGE_INDEX);
        }
        return page;
    }

    private Object getPageSize(Map<String, ?> paramMap){
        return paramMap.get(PAGE_SIZE);
    }

    private String getOrderBy(String orderByColumn, String isAsc) {
        if (ObjectUtils.isEmpty(orderByColumn)) {
            return "";
        }
        return StrUtil.toUnderlineCase(orderByColumn) + " " + getIsAsc(isAsc);
    }

    private String getIsAsc(String isAsc) {
        if (ObjectUtils.isNotEmpty(isAsc)) {
            if ("ascending".equals(isAsc)) { // 兼容前端排序类型
                return "asc";
            } else if ("descending".equals(isAsc)) {
                return "desc";
            }
            return isAsc;
        }
        return "";
    }

    public static class PasswordFilter implements PropertyFilter {
        @Override
        public boolean apply(Object object, String name, Object value) {
            return !"password".equalsIgnoreCase(name) && !"passwd".equalsIgnoreCase(name);
        }
    }
}

