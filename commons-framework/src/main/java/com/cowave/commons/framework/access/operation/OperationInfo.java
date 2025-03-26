/*
 * Copyright (c) 2017～2025 Cowave All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.commons.framework.access.operation;

import lombok.Data;

/**
 * 操作属性
 *
 * @author shanhuiming
 */
@Data
public class OperationInfo {

    /**
     * 操作模块
     */
    private String opModule;

    /**
     * 操作类型
     */
    private String opType;

    /**
     * 操作动作
     */
    private String opAction;

    /**
     * 操作内容
     */
    private Object opContent;

    /**
     * 操作耗时
     */
    private long opCost;

    /**
     * 操作处理标识
     */
    private String opFlag;

    /**
     * 操作描述
     */
    private String desc;

    /**
     * 操作是否成功
     */
    private boolean success;
}
