package com.cowave.commons.framework.filter.repeat;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

/**
 *
 * @author shanhuiming
 *
 */
public class RepeatLimitFilter implements Filter{

    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException{
        ServletRequest requestWrapper = null;
        if (request instanceof HttpServletRequest
                && StringUtils.startsWithIgnoreCase(request.getContentType(), MediaType.APPLICATION_JSON_VALUE)){
            requestWrapper = new RepeatRequestWrapper((HttpServletRequest) request, response);
        }
        if (null == requestWrapper){
            chain.doFilter(request, response);
        }else{
            chain.doFilter(requestWrapper, response);
        }
    }

    @Override
    public void destroy(){

    }
}
