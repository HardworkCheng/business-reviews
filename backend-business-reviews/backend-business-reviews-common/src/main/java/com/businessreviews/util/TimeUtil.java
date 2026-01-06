package com.businessreviews.util;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 时间格式化工具类
 * <p>
 * 提供人性化的时间显示格式（如：刚刚、几分钟前、几天前）
 * </p>
 */
public class TimeUtil {

    /**
     * 格式化为相对时间
     */
    public static String formatRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);

        if (minutes < 1) {
            return "刚刚";
        } else if (minutes < 60) {
            return minutes + "分钟前";
        } else if (minutes < 1440) { // 24小时
            return (minutes / 60) + "小时前";
        } else if (minutes < 10080) { // 7天
            return (minutes / 1440) + "天前";
        } else if (minutes < 43200) { // 30天
            return (minutes / 10080) + "周前";
        } else if (minutes < 525600) { // 365天
            return (minutes / 43200) + "个月前";
        } else {
            return (minutes / 525600) + "年前";
        }
    }
}
