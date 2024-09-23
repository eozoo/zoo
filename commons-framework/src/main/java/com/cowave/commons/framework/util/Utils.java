/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.cowave.commons.framework.access.AccessLogger;

/**
 *
 * @author shanhuiming
 *
 */
public class Utils {

	public static String getRequestBody(ServletRequest request) {
        StringBuilder builder = new StringBuilder();
        try (InputStream inputStream = request.getInputStream();
        		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (Exception e) {
        	AccessLogger.error("", e);
        }
        return builder.toString();
    }

	public static String getRequestIp(HttpServletRequest request){
		if (request == null){
			return "unknown";
		}

		String ip = request.getHeader("X-Real-IP");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
			ip = request.getHeader("x-forwarded-for");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
			ip = request.getHeader("X-Forwarded-For");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
			ip = request.getRemoteAddr();
		}
		return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : getMultistageReverseProxyIp(ip);
	}

	// 从多级反向代理，获取第一个非unknown IP地址
	private static String getMultistageReverseProxyIp(String ip){
		if (ip != null && ip.indexOf(",") > -1){ // 多级反向代理检测
			final String[] ips = ip.trim().split(",");
			for (String subIp : ips){
				if (!isUnknown(subIp)){
					ip = subIp;
					break;
				}
			}
		}
		return ip;
	}

	private static boolean isUnknown(String checkString){
		return StringUtils.isBlank(checkString) || "unknown".equalsIgnoreCase(checkString);
	}

	public static String capitalize(String str) {
		int strLen = length(str);
		if (strLen == 0) {
			return str;
		} else {
			int firstCodepoint = str.codePointAt(0);
			int newCodePoint = Character.toTitleCase(firstCodepoint);
			if (firstCodepoint == newCodePoint) {
				return str;
			} else {
				int[] newCodePoints = new int[strLen];
				int outOffset = 0;
				newCodePoints[outOffset++] = newCodePoint;

				int codepoint;
				for(int inOffset = Character.charCount(firstCodepoint); inOffset < strLen; inOffset += Character.charCount(codepoint)) {
					codepoint = str.codePointAt(inOffset);
					newCodePoints[outOffset++] = codepoint;
				}

				return new String(newCodePoints, 0, outOffset);
			}
		}
	}

	private static int length(CharSequence cs) {
		return cs == null ? 0 : cs.length();
	}

	public static boolean ishttp(String link) {
		return StringUtils.startsWithAny(link, "http://", "https://");
	}
}
