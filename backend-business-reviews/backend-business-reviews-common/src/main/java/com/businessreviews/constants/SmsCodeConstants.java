package com.businessreviews.constants;

/**
 * 短信验证码配置常量
 */
public final class SmsCodeConstants {
    
    private SmsCodeConstants() {
        // 私有构造函数，防止实例化
    }
    
    /** 验证码有效期（秒）：5分钟 */
    public static final long EXPIRE_TIME = 300;
    
    /** 发送频率限制（秒）：10秒 (开发环境临时调低) */
    public static final long LIMIT_TIME = 10;
}
