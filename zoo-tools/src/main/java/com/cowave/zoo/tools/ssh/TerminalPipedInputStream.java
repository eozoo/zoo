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
package com.cowave.zoo.tools.ssh;

import java.io.PipedInputStream;

/**
 * @author jiangbo
 */
public class TerminalPipedInputStream extends PipedInputStream {

    private int bufferSize = 1024;

    private int maxBufferSize = bufferSize;

    public TerminalPipedInputStream(int size) {
        super();
        buffer = new byte[size];
        bufferSize = size;
        maxBufferSize = size;
    }
    public TerminalPipedInputStream(int size, int maxBufferSize){
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
    public synchronized void checkSpace(int len) {
        int size = freeSpace();
        if(size < len){
            int datasize = buffer.length - size;
            int foo = buffer.length;
            while((foo - datasize) < len){
                foo *= 2;
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
                    System.arraycopy(buffer, out, tmp, tmp.length - (buffer.length - out), (buffer.length - out));
                    out = tmp.length - (buffer.length - out);
                }
            } else{
                System.arraycopy(buffer, 0, tmp, 0, buffer.length);
                in = buffer.length;
            }
            buffer = tmp;
        } else if(buffer.length == size && size > bufferSize) {
            int  i = size/2;
            if(i < bufferSize) {
                i = bufferSize;
            }
            buffer = new byte[i];
        }
    }
}
