package com.businessreviews.constants;

/**
 * Redis缓存过期时间配置常量
 * <p>
 * 单位：秒
 * 统一管理系统各模块缓存的生命周期
 * </p>
 */
public final class CacheExpireConstants {

    private CacheExpireConstants() {
        // 私有构造函数，防止实例化
    }

    /** 短信验证码有效期：5分钟 */
    public static final long SMS_CODE = 300;

    /** 短信发送频率限制：10秒 (开发环境临时调低) */
    public static final long SMS_LIMIT = 10;

    /** 用户信息缓存：30分钟 */
    public static final long USER_INFO = 1800;

    /** 商家信息缓存：1小时 */
    public static final long SHOP_INFO = 3600;

    /** 分类列表缓存：1天 */
    public static final long CATEGORIES = 86400;

    /** 推荐笔记缓存：5分钟 */
    public static final long NOTES_RECOMMENDED = 300;

    /** 热门搜索词缓存：1小时 */
    public static final long HOT_SEARCH = 3600;
}
