/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.zoo.framework.access.security;

import com.cowave.zoo.framework.access.Access;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * @author shanhuiming
 */
public interface AccessInfoSetter {

    default void setAccessInfo() {
        AccessInfo accessInfo = Access.accessInfo();
        String tenantId = this.getTenantId();
        if(StringUtils.isBlank(tenantId)){
            this.setTenantId(accessInfo.getAccessTenantId());
        }
        this.setCreateBy(accessInfo.getAccessUserCode());
        this.setUpdateBy(accessInfo.getAccessUserCode());
        this.setCreateTime(accessInfo.getAccessTime());
        this.setUpdateTime(accessInfo.getAccessTime());
    }

    default String getTenantId(){
        return null;
    }

    default void setTenantId(String tenantId){

    }

    default void setCreateBy(String userCode){

    }

    default void setUpdateBy(String userCode){

    }

    default void setCreateTime(Date createTime){

    }

    default void setUpdateTime(Date updateTime){

    }
}
