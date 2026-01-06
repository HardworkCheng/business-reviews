package com.businessreviews.util;

import java.math.BigDecimal;

/**
 * 地理位置距离计算工具类
 * <p>
 * 基于Haversine公式计算球面两点间的距离，用于附近商家搜索等功能。
 * </p>
 */
public class DistanceUtil {

    /**
     * 地球半径（公里）
     */
    private static final double EARTH_RADIUS = 6371.0;

    /**
     * 计算两点之间的距离（Haversine公式）
     *
     * @param lat1 纬度1
     * @param lon1 经度1
     * @param lat2 纬度2
     * @param lon2 经度2
     * @return 距离（公里）
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    /**
     * 计算两点之间的距离
     */
    public static double calculateDistance(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return 0;
        }
        return calculateDistance(lat1.doubleValue(), lon1.doubleValue(), lat2.doubleValue(), lon2.doubleValue());
    }

    /**
     * 格式化距离显示
     */
    public static String formatDistance(double distanceInKm) {
        if (distanceInKm < 0.1) {
            return Math.round(distanceInKm * 1000) + "m";
        } else if (distanceInKm < 1) {
            return Math.round(distanceInKm * 1000) + "m";
        } else if (distanceInKm < 10) {
            return String.format("%.1fkm", distanceInKm);
        } else {
            return Math.round(distanceInKm) + "km";
        }
    }

    /**
     * 计算并格式化距离
     */
    public static String getFormattedDistance(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        double distance = calculateDistance(lat1, lon1, lat2, lon2);
        return formatDistance(distance);
    }
}
