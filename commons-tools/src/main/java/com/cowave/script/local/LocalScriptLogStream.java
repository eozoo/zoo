/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.script.local;

import lombok.extern.slf4j.Slf4j;

/**
 * @author jiangbo
 * @date 2024/5/31
 */
@Slf4j
public class LocalScriptLogStream extends BaseLocalScriptOutputStream {

    public static LocalScriptLogStream apply() {
        return new LocalScriptLogStream();
    }

    @Override
    public void handlerLine(String line) {
        log.info(line);
    }
}
