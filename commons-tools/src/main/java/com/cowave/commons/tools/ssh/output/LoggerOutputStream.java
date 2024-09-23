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
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author jiangbo
 */
@Slf4j
public class LoggerOutputStream extends TerminalPipedOutputStream {

    public LoggerOutputStream(Charset charsetName) throws IOException {
        super(charsetName);
    }

    @Override
    public void handlerLine(String line) {
        log.info(line);
    }

    public static LoggerOutputStream apply() throws IOException {
        return apply(StandardCharsets.UTF_8);
    }

    public static LoggerOutputStream apply(Charset charsetName) throws IOException {
        return new LoggerOutputStream(charsetName);
    }
}
