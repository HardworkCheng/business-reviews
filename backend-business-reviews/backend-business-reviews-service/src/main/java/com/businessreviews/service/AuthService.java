package com.businessreviews.service;

import com.businessreviews.model.dto.LoginByCodeDTO;
import com.businessreviews.model.dto.LoginByPasswordDTO;
import com.businessreviews.model.dto.OAuthLoginDTO;
import com.businessreviews.model.dto.SendCodeDTO;
import com.businessreviews.model.vo.LoginVO;

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
     * 密码登录
     */
    LoginVO loginByPassword(LoginByPasswordDTO request);
    
    /**
     * 第三方登录
     */
    LoginVO oauthLogin(OAuthLoginDTO request);
    
    /**
     * 退出登录
     */
    void logout(String token);
}
