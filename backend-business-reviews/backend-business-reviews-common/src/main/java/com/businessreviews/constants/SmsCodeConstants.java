package com.businessreviews.constants;

/**
 * 短信验证码业务配置常量
 * <p>
 * 定义验证码长度、有效期、发送间隔等核心参数
 * </p>
 */
public final class SmsCodeConstants {

    /** 私有构造函数，防止实例化 */
    private SmsCodeConstants() {
        // 私有构造函数，防止实例化
    }

    /** 验证码长度 (默认6位) */
    public static final int CODE_LENGTH = 6;

    /** 验证码有效期（秒）：5分钟 */
    public static final long EXPIRE_TIME = 300;
    /** @deprecated 兼容旧代码，请使用 EXPIRE_TIME */
    public static final long CODE_EXPIRE_SECONDS = 300;

    /** 发送频率限制（秒）：60秒 (防止接口被刷) */
    public static final long LIMIT_TIME = 60;

    /** 发送间隔限制（秒）：60秒 */
    public static final long SEND_INTERVAL_SECONDS = 60;
}
