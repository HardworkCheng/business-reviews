package com.businessreviews.manager;

import com.businessreviews.exception.BusinessException;
import com.businessreviews.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * 短信服务管理器
 * 负责短信验证码的发送和验证
 * 
 * @author businessreviews
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SmsManager {
    
    private final RedisUtil redisUtil;
    
    /**
     * 验证码长度
     */
    private static final int CODE_LENGTH = 6;
    
    /**
     * 验证码有效期（秒）
     */
    private static final long CODE_EXPIRE_SECONDS = 300;
    
    /**
     * 发送间隔限制（秒）
     */
    private static final long SEND_INTERVAL_SECONDS = 60;
    
    /**
     * Redis key前缀
     */
    private static final String SMS_CODE_PREFIX = "sms:code:";
    private static final String SMS_LIMIT_PREFIX = "sms:limit:";
    
    private static final Random RANDOM = new Random();
    
    /**
     * 发送验证码
     *
     * @param phone 手机号
     * @return 验证码（开发环境返回，生产环境返回null）
     */
    public String sendCode(String phone) {
        // 检查发送频率限制
        String limitKey = SMS_LIMIT_PREFIX + phone;
        if (redisUtil.hasKey(limitKey)) {
            throw new BusinessException(40001, "验证码发送过于频繁，请稍后再试");
        }
        
        // 生成验证码
        String code = generateCode();
        
        // 发送短信（实际调用短信服务商API）
        boolean success = doSendSms(phone, code);
        if (!success) {
            throw new BusinessException(50000, "短信发送失败，请稍后重试");
        }
        
        // 保存验证码到Redis
        String codeKey = SMS_CODE_PREFIX + phone;
        redisUtil.set(codeKey, code, CODE_EXPIRE_SECONDS);
        
        // 设置发送频率限制
        redisUtil.set(limitKey, "1", SEND_INTERVAL_SECONDS);
        
        log.info("验证码已发送到手机 {}", maskPhone(phone));
        
        // 开发环境返回验证码，生产环境返回null
        return code;
    }
    
    /**
     * 验证验证码
     *
     * @param phone 手机号
     * @param code  验证码
     * @return 是否验证成功
     */
    public boolean verifyCode(String phone, String code) {
        if (phone == null || code == null) {
            return false;
        }
        
        String codeKey = SMS_CODE_PREFIX + phone;
        String cachedCode = redisUtil.get(codeKey);
        
        if (cachedCode == null) {
            return false;
        }
        
        boolean valid = cachedCode.equals(code);
        if (valid) {
            // 验证成功后删除验证码
            redisUtil.delete(codeKey);
        }
        
        return valid;
    }
    
    /**
     * 获取缓存的验证码（用于验证）
     *
     * @param phone 手机号
     * @return 验证码
     */
    public String getCachedCode(String phone) {
        String codeKey = SMS_CODE_PREFIX + phone;
        return redisUtil.get(codeKey);
    }
    
    /**
     * 删除验证码
     *
     * @param phone 手机号
     */
    public void removeCode(String phone) {
        String codeKey = SMS_CODE_PREFIX + phone;
        redisUtil.delete(codeKey);
    }
    
    /**
     * 实际发送短信
     * 这里接入实际的短信服务商API（如阿里云短信、腾讯云短信等）
     */
    private boolean doSendSms(String phone, String code) {
        // TODO: 接入实际的短信服务商API
        // 目前为模拟发送
        log.info("发送验证码到手机 {}: {}", phone, code);
        return true;
    }
    
    /**
     * 生成随机验证码
     */
    private String generateCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }
    
    /**
     * 手机号脱敏
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}
