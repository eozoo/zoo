/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.helper.geospatial;

import java.util.List;

import lombok.Data;


/**
 *
 * @author shanhuiming
 *
 */
@Data
public class GeoArea {

    /** 
     * 圆形
     */
    public static final int SHAPE_CIRCLE = 0;

    /** 
     * 多边形
     */
    public static final int SHAPE_POLYGON = 1;

    /** 
     * 轨迹 
     */
    public static final int SHAPE_TRACK = 2;

    /** 
     * 区域形状 0：圆形 1:多边形 2：航迹 
     */
    private Integer type;

    /** 
     * 区域点集合 
     */
    private List<GeoPoint> points;
}
