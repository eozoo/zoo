package com.cowave.commons.framework.helper.geospatial;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 区域点信息
 *
 * @author shanhuiming
 */
@NoArgsConstructor
@Data
public class GeoPoint {
    
    /** 
     * 经度 
     */
    private double longitude;
    
    /** 
     * 纬度 
     */
    private double latitude;
    
    /** 
     * 半径 
     */
    private double radius;

    public GeoPoint(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }
}
