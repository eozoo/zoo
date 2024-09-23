/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.script;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author jiangbo
 * @date 2024/5/31
 */
public class DefaultScriptOutputStream extends BaseScriptOutputStream {

    public DefaultScriptOutputStream(Charset charsetName) throws IOException {
        super(charsetName);
    }

    /**
     * 逐行处理输出
     * @param line 结果
     */
    public void handlerLine(String line){}

}
