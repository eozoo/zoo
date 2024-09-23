/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.filter.repeat;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import com.cowave.commons.framework.access.Access;
import com.cowave.commons.framework.filter.page.PageRequestWrapper;
import com.cowave.commons.framework.helper.MessageHelper;
import com.cowave.commons.framework.support.redis.RedisHelper;
import com.cowave.commons.framework.util.Utils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import org.springframework.feign.codec.Response;
import org.springframework.feign.codec.ResponseCode;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 *
 * @author shanhuiming
 *
 */
@RequiredArgsConstructor
public class RepeatInterceptor implements HandlerInterceptor {

    public static final String REPEAT_KEY = "repeat:";

    public static final String REPEAT_PARAMS = "params";

    public static final String REPEAT_TIME = "time";

    private final RedisHelper redisHelper;

    private final MessageHelper messageHelper;

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse httpResponse, @NotNull Object handler) throws IOException {
        if (handler instanceof HandlerMethod handlerMethod) {
            Method method = handlerMethod.getMethod();
            Repeat repeat = method.getAnnotation(Repeat.class);
            if (repeat != null && this.isRepeatSubmit(request, repeat)) {
                Response<Void> response =
                        messageHelper.translateErrorResponse(ResponseCode.BAD_REQUEST, repeat.message(), "请求过快，请稍后重试～");
                httpResponse.setStatus(ResponseCode.OK.getCode());
                httpResponse.setHeader("Retry-After", String.valueOf(repeat.interval() / 1000.0));
                httpResponse.setContentType("application/json");
                httpResponse.setCharacterEncoding("utf-8");
                httpResponse.getWriter().print(JSON.toJSONString(response));
                return false;
            }
        }
        return true;
    }

    public boolean isRepeatSubmit(HttpServletRequest request, Repeat repeat) {
        String nowParams = "";
        if (request instanceof PageRequestWrapper repeatedlyRequest) {
            nowParams = Utils.getRequestBody(repeatedlyRequest);
        }

        // body参数为空，获取Parameter的数据
        if (StringUtils.isEmpty(nowParams)) {
            nowParams = JSON.toJSONString(request.getParameterMap());
        }
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put(REPEAT_PARAMS, nowParams);
        requestMap.put(REPEAT_TIME, System.currentTimeMillis());

        // 请求资源标识
        String url = request.getRequestURI();

        // 访问身份标识
        String accessKey = StringUtils.trimToEmpty(request.getHeader("Authorization"));
        if(StringUtils.isEmpty(accessKey)){
            accessKey = Access.ip();
        }

        String repeatKey = REPEAT_KEY + url + ":" + accessKey;
        Map<String, Object> oldRequestMap = redisHelper.getValue(repeatKey);
        if (oldRequestMap != null
                && compareParams(requestMap, oldRequestMap)
                && compareTime(requestMap, oldRequestMap, repeat.interval())) {
                return true;
        }
        redisHelper.putExpireValue(repeatKey, requestMap, repeat.interval(), TimeUnit.MILLISECONDS);
        return false;
    }

    private boolean compareParams(Map<String, Object> nowMap, Map<String, Object> preMap) {
        String nowParams = (String) nowMap.get(REPEAT_PARAMS);
        String preParams = (String) preMap.get(REPEAT_PARAMS);
        return nowParams.equals(preParams);
    }

    private boolean compareTime(Map<String, Object> nowMap, Map<String, Object> preMap, int interval) {
        long time1 = (Long) nowMap.get(REPEAT_TIME);
        long time2 = (Long) preMap.get(REPEAT_TIME);
        return (time1 - time2) < interval;
    }
}
