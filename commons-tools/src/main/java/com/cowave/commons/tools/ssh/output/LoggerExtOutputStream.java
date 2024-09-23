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
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author jiangbo
 */
@Slf4j
public class LoggerExtOutputStream extends TerminalPipedOutputStream {

    public LoggerExtOutputStream(Charset charsetName) throws IOException {
        super(charsetName);
    }

    @Override
    public void handlerLine(String line) {
        if (StringUtils.startsWith(line, "+ ")) {
            log.info(line);
        } else {
            log.error(line);
        }
    }

    public static LoggerExtOutputStream apply() throws IOException {
        return apply(StandardCharsets.UTF_8);
    }

    public static LoggerExtOutputStream apply(Charset charsetName) throws IOException {
        return new LoggerExtOutputStream(charsetName);
    }
}
