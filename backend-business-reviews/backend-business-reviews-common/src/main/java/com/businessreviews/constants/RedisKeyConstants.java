package com.businessreviews.constants;

/**
 * Redis Key键名常量类
 * <p>
 * 统一管理系统中使用的Redis Key前缀，避免Key冲突和拼写错误。
 * 命名规范：业务模块:功能:子项:
 * </p>
 */
public final class RedisKeyConstants {

    /**
     * 私有构造函数，防止实例化
     */
    private RedisKeyConstants() {
        // 私有构造函数，防止实例化
    }

    /** 短信验证码 Key前缀 (后面拼接手机号) */
    public static final String SMS_CODE = "sms:code:";

    /** 短信发送频率限制 Key前缀 */
    public static final String SMS_LIMIT = "sms:limit:";

    /** 商家端-短信验证码 Key前缀 */
    public static final String MERCHANT_SMS_CODE = "merchant:sms:code:";

    /** 商家端-短信发送频率限制 Key前缀 */
    public static final String MERCHANT_SMS_LIMIT = "merchant:sms:limit:";

    /** 修改手机号次数限制(24小时内) */
    public static final String CHANGE_PHONE_LIMIT = "user:change:phone:limit:";

    /** Token黑名单 */
    public static final String TOKEN_BLACKLIST = "token:blacklist:";

    /** 用户信息缓存 */
    public static final String USER_INFO = "user:info:";

    /** 用户点赞笔记集合 */
    public static final String USER_NOTE_LIKES = "user:note:likes:";

    /** 用户收藏笔记集合 */
    public static final String USER_NOTE_BOOKMARKS = "user:note:bookmarks:";

    /** 用户点赞评论集合 */
    public static final String USER_COMMENT_LIKES = "user:comment:likes:";

    /** 商家信息缓存 */
    public static final String SHOP_INFO = "shop:info:";

    /** 分类列表缓存 */
    public static final String CATEGORIES = "categories:all";

    /** 推荐笔记缓存 */
    public static final String NOTES_RECOMMENDED = "notes:recommended:page:";

    /** 热门搜索词 */
    public static final String HOT_SEARCHES = "search:hot";

    /** 热门话题 */
    public static final String HOT_TOPICS = "topics:hot";

    /** 商家地理位置 (Redis GEO) */
    public static final String SHOP_GEO = "shop:geo";
}
