/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.script;

import lombok.Getter;

import java.io.IOException;
import java.io.PipedOutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * @author jiangbo
 * @date 2024/5/31
 */
@Getter
public abstract class BaseScriptOutputStream extends PipedOutputStream {

    private final ScriptPipedInputStream inputStream;

    protected final Charset charsetName;


    public BaseScriptOutputStream(Charset charsetName) throws IOException{
        this(new ScriptPipedInputStream(32*1024), charsetName);
    }

    public BaseScriptOutputStream(ScriptPipedInputStream in, Charset charsetName) throws IOException{
        super(in);
        inputStream = in;
        this.charsetName = charsetName;
    }

    protected byte[] buf = new byte[1024];

    protected int count;

    @Override
    public void write(int b) throws IOException {
        if (b == '\n') {
            writeLog();
        } else {
            if (count == buf.length - 1) {
                byte[] newArray = new byte[buf.length * 2];
                System.arraycopy(buf, 0, newArray, 0, buf.length);
                buf = newArray;
            }
            buf[count++] = (byte) b;
        }
        if(inputStream != null) {
            inputStream.checkSpace(1);
        }
        super.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        for (int i = 0; i < len; i++) {
            write(b[off + i]);
        }
    }

    @Override
    public void close() throws IOException {
        if (count > 0) {
            writeLog();
        }
        super.close();
    }

    protected synchronized void writeLog() {
        byte[] real = Arrays.copyOf(buf, count);
        handlerLine(new String(real, charsetName));
        buf = new byte[1024];
        count = 0;
    }

    /**
     * 逐行处理输出
     * @param line 结果
     */
    public abstract void handlerLine(String line);

}
