/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.access;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import cn.hutool.core.net.NetUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cowave.commons.framework.filter.security.AccessToken;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author shanhuiming
 */
@Data
public class Access {

    private static final ThreadLocal<Access> LOCAL = new TransmittableThreadLocal<>();

    private final String accessId;

    private final String accessIp;

    private final String accessUrl;

    private final Long accessTime;

    private AccessToken accessToken;

    private Locale locale;

    private Object requestParam;

    private Integer pageIndex;

    private Integer pageSize;

    private boolean responseLogged;

    public Access(String accessId, String accessIp, String accessUrl, Long accessTime, String language){
        this.accessId = accessId;
        this.accessIp = accessIp;
        this.accessUrl = accessUrl;
        this.accessTime = accessTime;
        this.locale = Locale.getDefault();
        if(StringUtils.isNotBlank(language)) {
            if(language.toLowerCase().contains("en")) {
                this.locale = new Locale("en");
            }
            if(language.toLowerCase().contains("zh")) {
                this.locale = new Locale("zh");
            }
        }
    }

    public static Access get(){
        return LOCAL.get();
    }

    public static void set(Access access){
        LOCAL.set(access);
    }

    public static void remove(){
        LOCAL.remove();
    }

    public static String accessId() {
        Access access;
        if((access = get()) == null) {
            return null;
        }
        return access.accessId;
    }

    public static String accessIp() {
        Access access;
        if((access = get()) == null) {
            return NetUtil.getLocalhostStr();
        }
        return access.accessIp;
    }

    public static String accessUrl() {
        Access access;
        if((access = get()) == null) {
            return null;
        }
        return access.accessUrl;
    }

    public static Date accessTime() {
        Access access;
        if((access = get()) == null) {
            return new Date();
        }
        return new Date(access.accessTime);
    }

    public static Locale accessLanguage() {
        Access access;
        if((access = get()) == null) {
            return Locale.getDefault();
        }
        return access.locale;
    }

    public static <T> Page<T> page(){
        Access access;
        if((access = get()) == null) {
            return new Page<>();
        }
        int pageIndex = access.pageIndex != null ? access.pageIndex : 1;
        int pageSize = access.pageSize != null ? access.pageSize : 10;
        return new Page<>(pageIndex, pageSize);
    }

    public static <T> Page<T> page(int defaultSize){
        Access access;
        if((access = get()) == null) {
            return new Page<>();
        }
        int pageIndex = access.pageIndex != null ? access.pageIndex : 1;
        int pageSize = access.pageSize != null ? access.pageSize : defaultSize;
        return new Page<>(pageIndex, pageSize);
    }

    public static int pageIndex() {
        Access access;
        if((access = get()) == null) {
            return 1;
        }
        return access.pageIndex != null ? access.pageIndex : 1;
    }

    public static int pageSize() {
        Access access;
        if((access = get()) == null) {
            return 10;
        }
        return access.pageSize != null ? access.pageSize : 10;
    }

    public static int pageSize(int defaultSize) {
        Access access;
        if((access = get()) == null) {
            return defaultSize;
        }
        return access.pageSize != null ? access.pageSize : defaultSize;
    }

    public static int pageOffset() {
        Access access;
        if((access = get()) == null) {
            return 0;
        }
        int pageIndex = access.pageIndex != null ? access.pageIndex : 1;
        int pageSize = access.pageSize != null ? access.pageSize : 10;
        if(pageIndex <= 0 || pageSize <= 0){
            return 0;
        }
        return (pageIndex - 1) * pageSize;
    }

    public static int pageOffset(int defaultSize) {
        Access access;
        if((access = get()) == null) {
            return 0;
        }
        int pageIndex = access.pageIndex != null ? access.pageIndex : 1;
        int pageSize = access.pageSize != null ? access.pageSize : defaultSize;
        if(pageIndex <= 0 || pageSize <= 0){
            return 0;
        }
        return (pageIndex - 1) * pageSize;
    }

    public static AccessToken accessToken() {
        Access access = get();
        if(access == null) {
            return null;
        }
        return access.accessToken;
    }

    public static String tokenType() {
        AccessToken token = accessToken();
        if(token == null) {
            return null;
        }
        return token.getType();
    }

    public static String tokenAccessValue() {
        AccessToken token = accessToken();
        if(token == null) {
            return null;
        }
        return token.getAccessToken();
    }

    public static String tokenRefreshValue() {
        AccessToken token = accessToken();
        if(token == null) {
            return null;
        }
        return token.getRefreshToken();
    }

    public static AccessUser accessUser(){
        AccessUser accessUser = new AccessUser();
        accessUser.setAccessUserId(userId());
        accessUser.setAccessUserAccount(userAccount());
        accessUser.setAccessUserName(userName());
        accessUser.setAccessDeptId(deptId());
        accessUser.setAccessDeptName(deptName());
        return accessUser;
    }

    public static Long userId() {
        AccessToken token = accessToken();
        if(token == null) {
            return null;
        }
        return token.getUserId();
    }

    public static String userCode() {
        AccessToken token = accessToken();
        if(token == null) {
            return null;
        }
        return token.getUserCode();
    }

    public static String userName() {
        AccessToken token = accessToken();
        if(token == null) {
            return null;
        }
        return token.getUserNick();
    }

    public static String userAccount() {
        AccessToken token = accessToken();
        if(token == null) {
            return null;
        }
        return token.getUsername();
    }

    public static List<String> userRoles() {
        Access access;
        AccessToken accessToken;
        if((access = get()) == null || (accessToken = access.accessToken) == null) {
            return null;
        }
        return accessToken.getRoles();
    }

    public static List<String> userPermissions() {
        Access access;
        AccessToken accessToken;
        if((access = get()) == null || (accessToken = access.accessToken) == null) {
            return null;
        }
        return accessToken.getPermissions();
    }

    public static boolean userIsAdmin(){
        List<String> roles = userRoles();
        if(CollectionUtils.isEmpty(roles)){
            return false;
        }
        return roles.contains("sysAdmin");
    }

    public static Long deptId() {
        Access access;
        AccessToken accessToken;
        if((access = get()) == null || (accessToken = access.accessToken) == null) {
            return null;
        }
        return accessToken.getDeptId();
    }

    public static String deptCode() {
        Access access;
        AccessToken accessToken;
        if((access = get()) == null || (accessToken = access.accessToken) == null) {
            return null;
        }
        return accessToken.getDeptCode();
    }

    public static String deptName() {
        Access access;
        AccessToken accessToken;
        if((access = get()) == null || (accessToken = access.accessToken) == null) {
            return null;
        }
        return accessToken.getDeptName();
    }

    public static Integer clusterId() {
        Access access;
        AccessToken accessToken;
        if((access = get()) == null || (accessToken = access.accessToken) == null) {
            return null;
        }
        return accessToken.getClusterId();
    }

    public static Integer clusterLevel() {
        Access access;
        AccessToken accessToken;
        if((access = get()) == null || (accessToken = access.accessToken) == null) {
            return null;
        }
        return accessToken.getClusterLevel();
    }

    public static String clusterName() {
        Access access;
        AccessToken accessToken;
        if((access = get()) == null || (accessToken = access.accessToken) == null) {
            return null;
        }
        return accessToken.getClusterName();
    }

    public static HttpServletRequest httpRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(attributes != null){
            return attributes.getRequest();
        }
        return null;
    }

    public static HttpServletResponse httpResponse() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(attributes != null){
            return attributes.getResponse();
        }
        return null;
    }

    public static HttpSession httpSession() {
        HttpServletRequest httpRequest = httpRequest();
        if(httpRequest != null){
            return httpRequest.getSession();
        }
        return null;
    }

    public static Cookie[] httpCookies(){
        HttpServletRequest httpRequest = httpRequest();
        if(httpRequest != null){
            return httpRequest.getCookies();
        }
        return null;
    }

    public static String getCookie(String name) {
        Cookie[] cookies = httpCookies();
        if(cookies != null){
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
                }
            }
        }
        return null;
    }

    public static void setCookie(String name, String value) {
        setCookie(name, value, "/");
    }

    public static void setCookie(String name, String value, String path) {
        setCookie(name, value, path, 60 * 60 * 24);
    }

    public static void setCookie(String name, String value, String path, int age) {
        HttpServletResponse httpResponse = httpResponse();
        if(httpResponse == null){
            return;
        }
        Cookie cookie = new Cookie(name, URLEncoder.encode(value, StandardCharsets.UTF_8));
        cookie.setPath(path);
        cookie.setMaxAge(age);
        httpResponse.addCookie(cookie);
    }

    public static void removeCookie(String name) {
        Cookie[] cookies = httpCookies();
        if(cookies != null){
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    cookie.setMaxAge(0);
                    Objects.requireNonNull(httpResponse()).addCookie(cookie);
                }
            }
        }
    }

    public static void setResponseStatus(HttpStatus httpStatus){
        HttpServletResponse response = httpResponse();
        if(response != null){
            response.setStatus(httpStatus.value());
        }
    }

    public static void setResponseHeader(String name, String value){
        HttpServletResponse response = httpResponse();
        if(response != null){
            response.setHeader(name, value);
        }
    }
}
