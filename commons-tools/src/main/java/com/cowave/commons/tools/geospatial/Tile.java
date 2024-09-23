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
 *
 * 瓦片坐标，以左上角为原点(0, 0)，到右下角(2 ^ 图像级别 - 1, 2 ^ 图像级别 - 1)为止
 *
 * @author shanhuiming
 */
@Data
class Tile {

    /**
     * 横向瓦片数
     */
    long x;

    /**
     * 纵向瓦片数
     */
    long y;

    /**
     * 级别
     */
    int z;

    public Tile(long x, long y){
        this.x=x;
        this.y=y;
    }

    public Tile(long x, long y, int z){
        this.x=x;
        this.y=y;
        this.z=z;
    }
}
