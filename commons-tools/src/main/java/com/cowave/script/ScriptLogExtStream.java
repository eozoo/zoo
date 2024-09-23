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
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author jiangbo
 * @date 2024/5/31
 */
@Slf4j
public class ScriptLogExtStream extends BaseScriptOutputStream {

    public static ScriptLogExtStream apply(Charset charsetName) throws IOException {
        return new ScriptLogExtStream(charsetName);
    }

    public static ScriptLogExtStream apply() throws IOException {
        return apply(StandardCharsets.UTF_8);
    }

    public ScriptLogExtStream(Charset charsetName) throws IOException {
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
}
