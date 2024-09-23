/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.script.local;

import java.io.*;

/**
 * @author jiangbo
 * @date 2024/9/11
 */
public class BaseLocalScriptOutputStream implements Closeable {

    private BufferedReader bufferedReader;

    public void wrap(InputStream inputStream, String charset) throws IOException {
        bufferedReader = new BufferedReader(new InputStreamReader(inputStream, charset));
        String line;
        // 实时读取输出
        while ((line = bufferedReader.readLine()) != null) {
            handlerLine(line);
        }
    }

    /**
     * 逐行处理输出
     *
     * @param line 结果
     */
    public void handlerLine(String line) {
    }

    @Override
    public void close() throws IOException {
        bufferedReader.close();
    }
}
