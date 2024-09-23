/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.support.logging;

import ch.qos.logback.core.util.FileSize;

/**
 * @author shanhuiming
 * @see ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy
 */
public class SizeAndTimeBasedRollingPolicy<E> extends TimeBasedRollingPolicy<E> {
    FileSize maxFileSize;

    public SizeAndTimeBasedRollingPolicy() {
    }

    public void start() {
        SizeAndTimeBasedFNATP<E> sizeAndTimeBasedFNATP = new SizeAndTimeBasedFNATP(SizeAndTimeBasedFNATP.Usage.EMBEDDED);
        if (this.maxFileSize == null) {
            this.addError("maxFileSize property is mandatory.");
        } else {
            this.addInfo("Archive files will be limited to [" + this.maxFileSize + "] each.");
            sizeAndTimeBasedFNATP.setMaxFileSize(this.maxFileSize);
            this.timeBasedFileNamingAndTriggeringPolicy = sizeAndTimeBasedFNATP;
            if (!this.isUnboundedTotalSizeCap() && this.totalSizeCap.getSize() < this.maxFileSize.getSize()) {
                this.addError("totalSizeCap of [" + this.totalSizeCap + "] is smaller than maxFileSize [" + this.maxFileSize + "] which is non-sensical");
            } else {
                super.start();
            }
        }
    }

    public void setMaxFileSize(FileSize aMaxFileSize) {
        this.maxFileSize = aMaxFileSize;
    }

    public String toString() {
        return "com.cowave.rolling.SizeAndTimeBasedRollingPolicy@" + this.hashCode();
    }
}
