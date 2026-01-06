package com.businessreviews.service.app;

import com.businessreviews.model.dto.LoginByCodeDTO;
import com.businessreviews.model.dto.LoginByPasswordDTO;
import com.businessreviews.model.dto.app.OAuthLoginDTO;
import com.businessreviews.model.dto.SendCodeDTO;
import com.businessreviews.model.vo.LoginVO;

/**
 * 用户端认证服务接口
 * <p>
 * 提供UniApp移动端的用户登录、注册、验证码等功能
 * </p>
 * 
 * @author businessreviews
 */
public interface AuthService {

    /**
     * 发送验证码
     * 
     * @param request 发送验证码请求对象
     */
    void sendCode(SendCodeDTO request);

    /**
     * 验证码登录
     * 
     * @param request 验证码登录请求对象
     * @return 登录成功响应（包含Token）
     */
    LoginVO loginByCode(LoginByCodeDTO request);

    /**
     * 密码登录
     * 
     * @param request 密码登录请求对象
     * @return 登录成功响应（包含Token）
     */
    LoginVO loginByPassword(LoginByPasswordDTO request);

    /**
     * 第三方登录
     * 
     * @param request OAuth登录请求对象
     * @return 登录成功响应（包含Token）
     */
    LoginVO oauthLogin(OAuthLoginDTO request);

    /**
     * 退出登录
     * 
     * @param token 用户Token
     */
    void logout(String token);
}
