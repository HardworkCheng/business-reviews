package com.businessreviews.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 短信工具类
 */
@Slf4j
@Component
public class SmsUtil {

    /**
     * 发送验证码
     * @param phone 手机号
     * @param code 验证码
     */
    public void sendCode(String phone, String code) {
        // 这里接入实际的短信服务商API
        // 如阿里云短信、腾讯云短信等
        log.info("发送验证码到手机 {}: {}", phone, code);
        
        // 模拟发送成功
        // 实际实现时替换为真实的短信发送逻辑
    }
}
