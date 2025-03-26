/*
 * Copyright (c) 2017～2025 Cowave All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.commons.framework.access.security;

import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author shanhuiming
 *
 */
@NoArgsConstructor
@Data
public class AccessInfo {

    /**
     * 用户id
     */
    private Object accessUserId;

    /**
     * 用户编码
     */
    private Object accessUserCode;

    /**
     * 用户账号
     */
    private String accessUserAccount;

    /**
     * 用户名称
     */
    private String accessUserName;

    /**
     * 部门id
     */
    private Object accessDeptId;

    /**
     * 部门编码
     */
    private Object accessDeptCode;

    /**
     * 部门名称
     */
    private String accessDeptName;

    /**
     * 访问时间
     */
    private Date accessTime = new Date();

    public <T> T getAccessUserId(){
        return (T)accessUserId;
    }

    public <T> T getAccessUserCode(){
        return (T)accessUserCode;
    }

    public <T> T getAccessDeptId(){
        return (T)accessDeptId;
    }

    public <T> T getAccessDeptCode(){
        return (T)accessDeptCode;
    }

    public AccessInfo(AccessUserDetails userDetails){
        if(userDetails != null){
            this.accessUserId = userDetails.getUserId();
            this.accessUserCode = userDetails.getUserCode();
            this.accessUserAccount = userDetails.getUsername();
            this.accessUserName = userDetails.getUserNick();
            this.accessDeptId = userDetails.getDeptId();
            this.accessDeptCode = userDetails.getDeptCode();
            this.accessDeptName = userDetails.getDeptName();
        }
    }
}
