/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.access;

import java.util.Collection;

import com.cowave.commons.framework.filter.security.AccessToken;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.stereotype.Component;

/**
 *
 * @author shanhuiming
 *
 */
@ConditionalOnClass(WebSecurityConfigurerAdapter.class)
@Component
public class AccessUserParser {

    @SuppressWarnings("rawtypes")
    public void parse(Class<?> clazz, Object arg) {
        Access access = Access.get();
        AccessToken accessToken = access.getAccessToken();
        if(accessToken == null){
            return;
        }
        if(AccessUser.class.isAssignableFrom(clazz)) {
            AccessUser accessUser = (AccessUser)arg;
            accessUser.setAccessUserId(accessToken.getUserId());
            accessUser.setAccessUserCode(accessToken.getUserCode());
            accessUser.setAccessUserAccount(accessToken.getUsername());
            accessUser.setAccessUserName(accessToken.getUserNick());
            accessUser.setAccessDeptId(accessToken.getDeptId());
            accessUser.setAccessDeptCode(accessToken.getDeptCode());
            accessUser.setAccessDeptName(accessToken.getDeptName());
        }else if(Collection.class.isAssignableFrom(clazz)) {
            Collection col = (Collection)arg;
            for(Object o : col) {
                if(!AccessUser.class.isAssignableFrom(o.getClass())) {
                    break;
                }
                AccessUser accessUser = (AccessUser)arg;
                accessUser.setAccessUserId(accessToken.getUserId());
                accessUser.setAccessUserCode(accessToken.getUserCode());
                accessUser.setAccessUserAccount(accessToken.getUsername());
                accessUser.setAccessUserName(accessToken.getUserNick());
                accessUser.setAccessDeptId(accessToken.getDeptId());
                accessUser.setAccessDeptCode(accessToken.getDeptCode());
                accessUser.setAccessDeptName(accessToken.getDeptName());
            }
        }
    }
}
