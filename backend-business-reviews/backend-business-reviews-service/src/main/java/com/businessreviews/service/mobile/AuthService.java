package com.businessreviews.service.mobile;

import com.businessreviews.model.dto.LoginByCodeDTO;
import com.businessreviews.model.dto.OAuthLoginDTO;
import com.businessreviews.model.dto.SendCodeDTO;
import com.businessreviews.model.vo.LoginVO;

/**
 * 移动端用户认证服务接口
 * 提供UniApp移动端的用户登录、注册、验证码等功能
 */
public interface AuthService {
    
    /**
     * 发送验证码
     */
    void sendCode(SendCodeDTO request);
    
    /**
     * 验证码登录
     */
    LoginVO loginByCode(LoginByCodeDTO request);
    
    /**
     * 第三方登录
     */
    LoginVO oauthLogin(OAuthLoginDTO request);
    
    /**
     * 退出登录
     */
    void logout(String token);
}
