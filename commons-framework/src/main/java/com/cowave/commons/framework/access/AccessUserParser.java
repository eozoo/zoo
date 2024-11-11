/*
 * Copyright (c) 2017～2024 Cowave All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.commons.framework.access;

import java.util.Collection;

import com.cowave.commons.framework.access.security.AccessToken;
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
        if(access == null){
            return;
        }

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
