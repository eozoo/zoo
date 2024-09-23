package com.cowave.commons.framework.access;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import cn.hutool.core.net.NetUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import com.cowave.commons.framework.filter.security.AccessToken;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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

    private final String requestIp;

    private final String requestId;

    private final String requestUrl;

    private final Long requestTime;

    private Locale locale;

    private AccessToken accessToken;

    private TreeMap<String, Object> requestParam;

    private Integer pageIndex;

    private Integer pageSize;

    private Integer pageOffset;

    public Access(String language, String requestIp, String requestId, String requestUrl, Long requestTime){
        this.requestIp = requestIp;
        this.requestId = requestId;
        this.requestUrl = requestUrl;
        this.requestTime = requestTime;
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

    public static Integer pageIndex() {
        Access access;
        if((access = get()) == null) {
            return null;
        }
        return access.pageIndex;
    }

    public static Integer pageSize() {
        Access access;
        if((access = get()) == null) {
            return null;
        }
        return access.pageSize;
    }

    public static Integer pageOffset() {
        Access access;
        if((access = get()) == null) {
            return null;
        }
        return access.pageOffset;
    }

    public static Locale language() {
        Access access;
        if((access = get()) == null) {
            return Locale.getDefault();
        }
        return access.locale;
    }

    public static String ip() {
        Access access;
        if((access = get()) == null) {
            return NetUtil.getLocalhostStr();
        }
        return access.requestIp;
    }

    public static String id() {
        Access access;
        if((access = get()) == null) {
            return null;
        }
        return access.requestId;
    }

    public static String url() {
        Access access;
        if((access = get()) == null) {
            return null;
        }
        return access.requestUrl;
    }

    public static Date time() {
        Access access;
        if((access = get()) == null) {
            return new Date();
        }
        return new Date(access.requestTime);
    }

    public static AccessToken accessToken() {
        Access access = get();
        if(access == null) {
            return null;
        }
        return access.accessToken;
    }

    public static String token() {
        Access access;
        AccessToken accessToken;
        if((access = get()) == null || (accessToken = access.accessToken) == null) {
            return null;
        }
        return accessToken.getToken();
    }

    public static String tokenType() {
        Access access;
        AccessToken accessToken;
        if((access = get()) == null || (accessToken = access.accessToken) == null) {
            return null;
        }
        return accessToken.getType();
    }

    public static AccessUser user(){
        AccessUser accessUser = new AccessUser();
        accessUser.setAccessUserId(userId());
        accessUser.setAccessUserAccount(userAccount());
        accessUser.setAccessUserName(userName());
        accessUser.setAccessDeptId(deptId());
        accessUser.setAccessDeptName(deptName());
        return accessUser;
    }

    public static Long userId() {
        Access access;
        AccessToken accessToken;
        if((access = get()) == null || (accessToken = access.accessToken) == null) {
            return null;
        }
        return accessToken.getUserId();
    }

    public static String userCode() {
        Access access;
        AccessToken accessToken;
        if((access = get()) == null || (accessToken = access.accessToken) == null) {
            return null;
        }
        return accessToken.getUserCode();
    }

    public static String userAccount() {
        Access access;
        AccessToken accessToken;
        if((access = get()) == null || (accessToken = access.accessToken) == null) {
            return null;
        }
        return accessToken.getUsername();
    }

    public static String userName() {
        Access access;
        AccessToken accessToken;
        if((access = get()) == null || (accessToken = access.accessToken) == null) {
            return null;
        }
        return accessToken.getUserNick();
    }

    public static boolean isAdmin(){
        List<String> roles = userRoles();
        if(CollectionUtils.isEmpty(roles)){
            return false;
        }
        return roles.contains("sysAdmin");
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
}
