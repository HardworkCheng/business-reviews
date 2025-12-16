package com.businessreviews.service.mobile;

import com.businessreviews.dto.request.LoginByCodeRequest;
import com.businessreviews.dto.request.OAuthLoginRequest;
import com.businessreviews.dto.request.SendCodeRequest;
import com.businessreviews.dto.response.LoginResponse;

/**
 * 移动端用户认证服务接口
 * 提供UniApp移动端的用户登录、注册、验证码等功能
 */
public interface AuthService {
    
    /**
     * 发送验证码
     */
    void sendCode(SendCodeRequest request);
    
    /**
     * 验证码登录
     */
    LoginResponse loginByCode(LoginByCodeRequest request);
    
    /**
     * 第三方登录
     */
    LoginResponse oauthLogin(OAuthLoginRequest request);
    
    /**
     * 退出登录
     */
    void logout(String token);
}
