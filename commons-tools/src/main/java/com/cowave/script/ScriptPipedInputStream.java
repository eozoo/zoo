/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.script;

import java.io.PipedInputStream;

/**
 * @author jiangbo
 * @date 2024/5/31
 */
public class ScriptPipedInputStream extends PipedInputStream {
    private int bufferSize = 1024;
    private int maxBufferSize = bufferSize;
    public ScriptPipedInputStream(int size) {
        super();
        buffer=new byte[size];
        bufferSize = size;
        maxBufferSize = size;
    }
    public ScriptPipedInputStream(int size, int maxBufferSize){
        this(size);
        this.maxBufferSize = maxBufferSize;
    }

    private int freeSpace(){
        int size = 0;
        if(out < in) {
            size = buffer.length-in;
        } else if(in < out){
            if(in == -1) {
                size = buffer.length;
            } else {
                size = out - in;
            }
        }
        return size;
    }
    synchronized void checkSpace(int len) {
        int size = freeSpace();
        if(size<len){
            int datasize=buffer.length-size;
            int foo = buffer.length;
            while((foo - datasize) < len){
                foo*=2;
            }

            if(foo > maxBufferSize){
                foo = maxBufferSize;
            }
            if((foo - datasize) < len) {
                return;
            }

            byte[] tmp = new byte[foo];
            if(out < in) {
                System.arraycopy(buffer, 0, tmp, 0, buffer.length);
            } else if(in < out){
                if(in != -1) {
                    System.arraycopy(buffer, 0, tmp, 0, in);
                    System.arraycopy(buffer, out,
                            tmp, tmp.length-(buffer.length-out),
                            (buffer.length-out));
                    out = tmp.length-(buffer.length-out);
                }
            } else{
                System.arraycopy(buffer, 0, tmp, 0, buffer.length);
                in=buffer.length;
            }
            buffer=tmp;
        } else if(buffer.length == size && size > bufferSize) {
            int  i = size/2;
            if(i< bufferSize) {
                i = bufferSize;
            }
            buffer= new byte[i];
        }
    }
}
