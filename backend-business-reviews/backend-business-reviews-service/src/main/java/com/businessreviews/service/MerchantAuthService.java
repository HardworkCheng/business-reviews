package com.businessreviews.service;

import com.businessreviews.dto.request.MerchantLoginRequest;
import com.businessreviews.dto.response.MerchantLoginResponse;
import com.businessreviews.dto.response.MerchantUserInfoResponse;
import com.businessreviews.entity.MerchantUser;

/**
 * 商家认证服务接口
 */
public interface MerchantAuthService {
    
    /**
     * 发送验证码
     */
    void sendCode(String phone);
    
    /**
     * 验证码登录
     */
    MerchantLoginResponse loginByCode(String phone, String code);
    
    /**
     * 密码登录
     */
    MerchantLoginResponse loginByPassword(MerchantLoginRequest request);
    
    /**
     * 获取当前登录用户信息
     */
    MerchantUserInfoResponse getCurrentUserInfo(Long userId);
    
    /**
     * 退出登录
     */
    void logout(String token);
    
    /**
     * 商家入驻注册
     */
    MerchantLoginResponse register(String phone, String code, String password, String merchantName);
}
