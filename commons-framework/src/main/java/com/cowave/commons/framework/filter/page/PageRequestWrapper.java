/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.filter.page;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.cowave.commons.framework.util.Converts;
import com.cowave.commons.framework.util.Utils;
import com.github.pagehelper.page.PageMethod;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConversionException;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 *
 * @author shanhuiming
 *
 */
public class PageRequestWrapper extends HttpServletRequestWrapper {

    private static final String PAGE_INDEX = "page";

    private static final String PAGE_SIZE = "pageSize";

    private static final String IS_ASC = "isAsc";

    private static final String ORDER_COLUMN = "orderColumn";

    private int pageIndex;

    private int pageSize;

    private String body = "";

    public PageRequestWrapper(HttpServletRequest request, ServletResponse response) throws IOException {
        super(request);
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        if (StringUtils.startsWithIgnoreCase(this.getContentType(), MediaType.APPLICATION_JSON_VALUE)) {
            body = Utils.getRequestBody(request);
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
    public void checkAndSetPage() {
        PageMethod.clearPage(); // 线程池线程复用可能导致问题，所以要进行清除下
        if (StringUtils.isNotBlank(body) && !body.startsWith("[")) {
            Map<String, Object> paramMap;
            try{
                paramMap = JSON.parseObject(body, Map.class);
            }catch (Exception e){
                throw new HttpMessageConversionException(e.getMessage());
            }
            if (paramMap.containsKey(PAGE_INDEX) && paramMap.containsKey(PAGE_SIZE)) {
                Integer pageIndex = Converts.toInt(paramMap.get(PAGE_INDEX), 1);
                Integer pageSize = Converts.toInt(paramMap.get(PAGE_SIZE), 10);
                String orderByColumn = (String) paramMap.get(ORDER_COLUMN);
                String isAsc = (String) paramMap.get(IS_ASC);
                String orderBy = getOrderBy(orderByColumn, isAsc);
                PageMethod.startPage(pageIndex, pageSize, orderBy).setReasonable(true);
                this.pageIndex = pageIndex;
                this.pageSize = pageSize;
            }
        } else {
            if (getParameter(PAGE_INDEX) != null && getParameter(PAGE_SIZE) != null) {
                Integer pageIndex = Converts.toInt(getParameter(PAGE_INDEX), 1);
                Integer pageSize = Converts.toInt(getParameter(PAGE_SIZE), 10);
                String orderByColumn = getParameter(ORDER_COLUMN);
                String isAsc = getParameter(IS_ASC);
                String orderBy = getOrderBy(orderByColumn, isAsc);
                PageMethod.startPage(pageIndex, pageSize, orderBy).setReasonable(true);
                this.pageIndex = pageIndex;
                this.pageSize = pageSize;
            }
        }
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

    private String getOrderBy(String orderByColumn, String isAsc) {
        if (ObjectUtils.isEmpty(orderByColumn)) {
            return "";
        }
        return StrUtil.toUnderlineCase(orderByColumn) + " " + getIsAsc(isAsc);
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getPageOffset() {
        if(pageIndex <= 0 || pageSize <= 0){
            return 0;
        }
        return (this.pageIndex - 1) * this.pageSize;
    }
}

