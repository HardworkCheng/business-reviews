package com.businessreviews.constants;

/**
 * Redis Key前缀常量
 */
public final class RedisKeyConstants {
    
    private RedisKeyConstants() {
        // 私有构造函数，防止实例化
    }
    
    /** 短信验证码 */
    public static final String SMS_CODE = "sms:code:";
    
    /** 短信发送频率限制 */
    public static final String SMS_LIMIT = "sms:limit:";
    
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
}
