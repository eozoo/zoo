package com.cowave.commons.framework.helper.geospatial;

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
