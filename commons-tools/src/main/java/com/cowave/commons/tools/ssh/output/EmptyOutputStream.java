/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.tools.ssh.output;

import com.cowave.commons.tools.ssh.TerminalPipedOutputStream;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author jiangbo
 */
public class EmptyOutputStream extends TerminalPipedOutputStream {

    public EmptyOutputStream(Charset charsetName) throws IOException {
        super(charsetName);
    }

    public void handlerLine(String line){

    }
}
