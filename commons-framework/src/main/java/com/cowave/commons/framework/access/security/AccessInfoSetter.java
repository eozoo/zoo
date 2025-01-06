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

import com.cowave.commons.framework.access.Access;

import java.util.Date;

/**
 * @author shanhuiming
 */
public interface AccessInfoSetter {

    default void setAccessInfo() {
        AccessInfo accessInfo = Access.accessInfo();
        this.setCreateBy(accessInfo.getAccessUserCode());
        this.setCreateUser(accessInfo.getAccessUserId());
        this.setCreateDept(accessInfo.getAccessDeptId());
        this.setCreateTime(accessInfo.getAccessTime());
        this.setUpdateBy(accessInfo.getAccessUserCode());
        this.setUpdateUser(accessInfo.getAccessUserId());
        this.setUpdateDept(accessInfo.getAccessDeptId());
        this.setUpdateTime(accessInfo.getAccessTime());
    }

    default void setCreateBy(String userCode){

    }

    default void setCreateUser(Integer userId){

    }

    default void setCreateDept(Integer deptId){

    }

    default void setCreateTime(Date createTime){

    }

    default void setUpdateBy(String userCode){

    }

    default void setUpdateUser(Integer userId){

    }

    default void setUpdateDept(Integer deptId){

    }

    default void setUpdateTime(Date updateTime){

    }
}
