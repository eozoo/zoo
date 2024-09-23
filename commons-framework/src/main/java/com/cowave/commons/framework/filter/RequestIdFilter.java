package com.cowave.commons.framework.filter;

import java.io.IOException;
import java.lang.reflect.Field;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

import com.cowave.commons.framework.access.Access;
import com.cowave.commons.framework.access.AccessLogger;
import com.cowave.commons.framework.util.IdGenerator;
import com.cowave.commons.framework.util.Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.MimeHeaders;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 *
 * @author shanhuiming
 *
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@WebFilter(filterName = "requestIdFilter", urlPatterns = "/*")
public class RequestIdFilter implements Filter {

    private final IdGenerator idGenerator = new IdGenerator();

    @Value("${info.cluster.id:}${server.port:8080}")
    private String prefix;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String requestId = req.getHeader("requestId");
        if (StringUtils.isBlank(requestId)) {
            requestId = getRequestId();
            setRequestId(req, requestId);
        }
        Thread.currentThread().setName(requestId);
        MDC.put("tid", String.valueOf(Thread.currentThread().getId()));

        String language = req.getHeader("Accept-Language");
        String requestIp = Utils.getRequestIp(req);
        String requestUrl = req.getRequestURI();
        Access.set(new Access(language, requestIp, requestId, requestUrl, System.currentTimeMillis()));
        chain.doFilter(request, response);
    }

    private void setRequestId(HttpServletRequest request, String value) {
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
            mine.addValue("requestId").setString(value);
        } catch (Exception e) {
            // never will happened
            AccessLogger.error("", e);
        }
    }

    private String getRequestId() {
        return idGenerator.generateIdWithDate(prefix, "", "yyyyMMddHHmmss", 1000);
    }
}
