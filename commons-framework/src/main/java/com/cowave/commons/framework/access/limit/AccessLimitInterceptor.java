/*
 * Copyright (c) 2017～2024 Cowave All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.commons.framework.access.limit;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import com.cowave.commons.framework.helper.redis.RedisHelper;
import lombok.RequiredArgsConstructor;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 *
 * @author shanhuiming
 *
 */
@RequiredArgsConstructor
public class AccessLimitInterceptor implements HandlerInterceptor {

    public static final String REPEAT_KEY = "repeat:";

    public static final String REPEAT_PARAMS = "params";

    public static final String REPEAT_TIME = "time";

    private final RedisHelper redisHelper;

    private final AccessLimitHelper limitHelper;

    private final boolean isAlwaysSuccess;

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse httpResponse, @NotNull Object handler) throws IOException {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        Method method = ((HandlerMethod) handler).getMethod();
        AccessLimit accessLimit = method.getAnnotation(AccessLimit.class);
        if(accessLimit == null){
            return true;
        }


        // 使用Redis 获取 limiter现在

//        // user api
//        if (handler instanceof HandlerMethod handlerMethod) {
//            Method method = handlerMethod.getMethod();
//            AccessLimit accessLimit = method.getAnnotation(AccessLimit.class);
//
//            if (accessLimit != null && this.isRepeatSubmit(request, accessLimit)) {
//                int httpStatus = TOO_MANY_REQUESTS.getStatus();
//                if(isAlwaysSuccess){
//                    httpStatus = SUCCESS.getStatus();
//                }
//                httpResponse.setStatus(httpStatus);
//                httpResponse.setCharacterEncoding("utf-8");
//                httpResponse.setContentType("application/json");
//                httpResponse.setHeader("Retry-After", String.valueOf(accessLimit.interval() / 1000.0));
//                httpResponse.getWriter().print(JSON.toJSONString(
//                        Response.msg(TOO_MANY_REQUESTS, Messages.translateIfNeed(accessLimit.message()))));
//                return false;
//            }
//        }
        return true;
    }

//    public boolean isRepeatSubmit(HttpServletRequest request, AccessLimit accessLimit) {
//        String nowParams = "";
//        if (request instanceof AccessRequestWrapper repeatedlyRequest) {
//            nowParams = ServletUtils.getRequestBody(repeatedlyRequest);
//        }
//
//        // body参数为空，获取Parameter的数据
//        if (StringUtils.isEmpty(nowParams)) {
//            nowParams = JSON.toJSONString(request.getParameterMap());
//        }
//        Map<String, Object> requestMap = new HashMap<>();
//        requestMap.put(REPEAT_PARAMS, nowParams);
//        requestMap.put(REPEAT_TIME, System.currentTimeMillis());
//
//        // 请求资源标识
//        String url = request.getRequestURI();
//
//        // 访问身份标识
//        String accessKey = StringUtils.trimToEmpty(request.getHeader("Authorization"));
//        if(StringUtils.isEmpty(accessKey)){
//            accessKey = Access.accessIp();
//        }
//
//        String repeatKey = REPEAT_KEY + url + ":" + accessKey;
//        Map<String, Object> oldRequestMap = redisHelper.getValue(repeatKey);
//        if (oldRequestMap != null
//                && compareParams(requestMap, oldRequestMap)
//                && compareTime(requestMap, oldRequestMap, accessLimit.interval())) {
//                return true;
//        }
//        redisHelper.putExpireValue(repeatKey, requestMap, accessLimit.interval(), TimeUnit.MILLISECONDS);
//        return false;
//    }
//
//    private boolean compareParams(Map<String, Object> nowMap, Map<String, Object> preMap) {
//        String nowParams = (String) nowMap.get(REPEAT_PARAMS);
//        String preParams = (String) preMap.get(REPEAT_PARAMS);
//        return nowParams.equals(preParams);
//    }
//
//    private boolean compareTime(Map<String, Object> nowMap, Map<String, Object> preMap, int interval) {
//        long time1 = (Long) nowMap.get(REPEAT_TIME);
//        long time2 = (Long) preMap.get(REPEAT_TIME);
//        return (time1 - time2) < interval;
//    }
}
