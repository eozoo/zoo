/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.server;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * @author jiangbo
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServletUtils {

    private static final String UN_KNOWN = "unknown";
    private static final String[] HEADER_KEYS = {"X-Real-IP", "x-forwarded-for", "Proxy-Client-IP", "X-Forwarded-For", "WL-Proxy-Client-IP"};

    public static String getRequestIp(HttpServletRequest request) {
        if (request == null) {
            return UN_KNOWN;
        }
        String ip = "";
        for (String headerKey : HEADER_KEYS) {
            ip = request.getHeader(headerKey);
            if (StringUtils.isNotBlank(ip) && !UN_KNOWN.equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        ip = request.getRemoteAddr();
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : getMultistageReverseProxyIp(ip);
    }

    /**
     * 从多级反向代理，获取第一个非unknown IP地址
     */
    private static String getMultistageReverseProxyIp(String ip) {
        if (ip != null && ip.contains(",")) {
            String[] ips = ip.trim().split(",");
            for (String subIp : ips) {
                if (!isUnknown(subIp)) {
                    ip = subIp;
                    break;
                }
            }
        }
        return ip;
    }

    private static boolean isUnknown(String checkString) {
        return StringUtils.isBlank(checkString) || UN_KNOWN.equalsIgnoreCase(checkString);
    }
}
