package com.cowave.commons.framework.filter.repeat;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.springframework.feign.codec.Response;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.alibaba.fastjson.JSON;

/**
 *
 * @author shanhuiming
 *
 */
public abstract class AbstractRepeatLimitInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request,
                             @NotNull HttpServletResponse httpResponse, @NotNull Object handler) throws IOException {
        if (handler instanceof HandlerMethod handlerMethod) {
            Method method = handlerMethod.getMethod();
            RepeatLimit annotation = method.getAnnotation(RepeatLimit.class);
            if (annotation != null && this.isRepeatSubmit(request, annotation)) {
                Response<Void> response = Response.error(annotation.message());
                httpResponse.setStatus(200);
                httpResponse.setContentType("application/json");
                httpResponse.setCharacterEncoding("utf-8");
                httpResponse.getWriter().print(JSON.toJSONString(response));
                return false;
            }
        }
        return true;
    }

    public abstract boolean isRepeatSubmit(HttpServletRequest request, RepeatLimit annotation);
}
