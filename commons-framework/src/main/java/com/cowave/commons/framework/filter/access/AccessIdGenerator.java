/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.filter.access;

import com.cowave.commons.tools.ids.IdGenerator;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author shanhuiming
 *
 */
@RequiredArgsConstructor
public class AccessIdGenerator {

    private final IdGenerator idGenerator = new IdGenerator();

    private final String idPrefix;

    public String newAccessId() {
        return idGenerator.generateIdWithDate(idPrefix, "", "yyyyMMddHHmmss", 1000);
    }
}
