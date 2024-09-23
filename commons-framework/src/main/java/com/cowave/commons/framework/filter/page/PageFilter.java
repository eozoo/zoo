package com.cowave.commons.framework.filter.page;

import com.alibaba.fastjson.JSON;
import com.cowave.commons.framework.access.Access;
import com.cowave.commons.framework.helper.MessageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.feign.codec.Response;
import org.springframework.feign.codec.ResponseCode;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConversionException;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 * @author shanhuiming
 *
 */
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@WebFilter(filterName = "pageFilter", urlPatterns = "/*")
public class PageFilter implements Filter {

    private final MessageHelper messageHelper;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        PageRequestWrapper pageRequestWrapper = new PageRequestWrapper((HttpServletRequest) request, response);
        Access access = Access.get();
        try{
            pageRequestWrapper.checkAndSetPage();
        }catch (HttpMessageConversionException e){
            HttpServletResponse httpResponse = (HttpServletResponse)response;
            httpResponse.setCharacterEncoding("UTF-8");
            httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            httpResponse.setStatus(ResponseCode.BAD_REQUEST.getCode());
            response.getWriter().write(JSON.toJSONString(messageHelper.translateErrorResponse(
                    ResponseCode.BAD_REQUEST, "frame.advice.httpMessageConversionException", "请求参数转换失败")));
            return;
        }

        access.setPageIndex(pageRequestWrapper.getPageIndex());
        access.setPageSize(pageRequestWrapper.getPageSize());
        access.setPageOffset(pageRequestWrapper.getPageOffset());
        chain.doFilter(pageRequestWrapper, response);
    }
}
