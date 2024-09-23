/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.tools.geospatial;

import lombok.Data;

/**
 * 屏幕像素坐标，以左上角为原点(0,0)
 *
 * @author shanhuiming
 */
@Data
class Pixel {

    /**
     * 横向像素
     */
    long x;

    /**
     * 纵向像素
     */
    long y;

    public Pixel(long x, long y){
        this.x=x;
        this.y=y;
    }
}
