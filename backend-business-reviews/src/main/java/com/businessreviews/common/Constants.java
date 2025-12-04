package com.businessreviews.common;

/**
 * 系统常量类
 */
public class Constants {
    
    /**
     * 验证码类型
     */
    public static class CodeType {
        /** 登录 */
        public static final int LOGIN = 1;
        /** 注册 */
        public static final int REGISTER = 2;
        /** 重置密码 */
        public static final int RESET_PASSWORD = 3;
    }
    
    /**
     * 用户状态
     */
    public static class UserStatus {
        /** 正常 */
        public static final int NORMAL = 1;
        /** 禁用 */
        public static final int DISABLED = 2;
    }
    
    /**
     * 性别
     */
    public static class Gender {
        /** 未知 */
        public static final int UNKNOWN = 0;
        /** 男 */
        public static final int MALE = 1;
        /** 女 */
        public static final int FEMALE = 2;
    }
    
    /**
     * 商家状态
     */
    public static class ShopStatus {
        /** 营业中 */
        public static final int OPEN = 1;
        /** 休息中 */
        public static final int REST = 2;
        /** 已关闭 */
        public static final int CLOSED = 3;
    }
    
    /**
     * 笔记状态
     */
    public static class NoteStatus {
        /** 正常 */
        public static final int NORMAL = 1;
        /** 隐藏 */
        public static final int HIDDEN = 2;
        /** 审核中 */
        public static final int PENDING = 3;
    }
    
    /**
     * 评论状态
     */
    public static class CommentStatus {
        /** 正常 */
        public static final int NORMAL = 1;
        /** 隐藏 */
        public static final int HIDDEN = 2;
    }
    
    /**
     * 消息类型
     */
    public static class MessageType {
        /** 文本 */
        public static final int TEXT = 1;
        /** 图片 */
        public static final int IMAGE = 2;
        /** 语音 */
        public static final int VOICE = 3;
    }
    
    /**
     * 通知类型
     */
    public static class NoticeType {
        /** 点赞笔记 */
        public static final int LIKE_NOTE = 1;
        /** 评论笔记 */
        public static final int COMMENT_NOTE = 2;
        /** 关注 */
        public static final int FOLLOW = 3;
        /** 点赞评论 */
        public static final int LIKE_COMMENT = 4;
    }
    
    /**
     * 浏览目标类型
     */
    public static class BrowseTargetType {
        /** 笔记 */
        public static final int NOTE = 1;
        /** 商家 */
        public static final int SHOP = 2;
    }
    
    /**
     * 收藏类型
     */
    public static class FavoriteType {
        /** 笔记 */
        public static final int NOTE = 1;
        /** 商家 */
        public static final int SHOP = 2;
    }
    
    /**
     * 短信验证码配置
     */
    public static class SmsCode {
        /** 验证码有效期（秒）：5分钟 */
        public static final long EXPIRE_TIME = 300;
        /** 发送频率限制（秒）：10秒 (开发环境临时调低) */
        public static final long LIMIT_TIME = 10;
    }
    
    /**
     * Redis Key前缀
     */
    public static class RedisKey {
        /** 短信验证码 */
        public static final String SMS_CODE = "sms:code:";
        /** 短信发送频率限制 */
        public static final String SMS_LIMIT = "sms:limit:";
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
    
    /**
     * 缓存过期时间（秒）
     */
    public static class CacheExpire {
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
    
    /**
     * 错误码
     */
    public static class ErrorCode {
        /** 手机号格式错误 */
        public static final int PHONE_FORMAT_ERROR = 40001;
        /** 验证码错误或已过期 */
        public static final int CODE_ERROR = 40002;
        /** 用户不存在 */
        public static final int USER_NOT_FOUND = 40003;
        /** 参数缺失 */
        public static final int PARAM_MISSING = 40004;
        /** 操作过于频繁 */
        public static final int TOO_FREQUENT = 40005;
        /** 未登录 */
        public static final int UNAUTHORIZED = 40101;
        /** Token已过期 */
        public static final int TOKEN_EXPIRED = 40102;
        /** Token无效 */
        public static final int TOKEN_INVALID = 40103;
        /** 无权限操作 */
        public static final int FORBIDDEN = 40301;
        /** 资源不存在 */
        public static final int NOT_FOUND = 40401;
        /** 笔记不存在 */
        public static final int NOTE_NOT_FOUND = 40402;
        /** 商家不存在 */
        public static final int SHOP_NOT_FOUND = 40403;
        /** 服务器内部错误 */
        public static final int SERVER_ERROR = 50001;
        /** 数据库错误 */
        public static final int DB_ERROR = 50002;
    }
    
    /**
     * 分页默认值
     */
    public static class Page {
        /** 默认页码 */
        public static final int DEFAULT_PAGE_NUM = 1;
        /** 默认每页数量 */
        public static final int DEFAULT_PAGE_SIZE = 10;
        /** 最大每页数量 */
        public static final int MAX_PAGE_SIZE = 50;
    }
}
