/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
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
