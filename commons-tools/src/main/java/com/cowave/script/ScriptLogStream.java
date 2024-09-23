/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.script;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author jiangbo
 * @date 2024/5/31
 */
@Slf4j
public class ScriptLogStream extends BaseScriptOutputStream {

    public static ScriptLogStream apply(Charset charsetName) throws IOException {
        return new ScriptLogStream(charsetName);
    }

    public static ScriptLogStream apply() throws IOException {
        return apply(StandardCharsets.UTF_8);
    }

    public ScriptLogStream(Charset charsetName) throws IOException {
        super(charsetName);
    }

    @Override
    public void handlerLine(String line) {
        log.info(line);
    }
}
