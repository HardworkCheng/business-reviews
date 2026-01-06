package com.businessreviews.web.app;

import com.businessreviews.common.Result;
import com.businessreviews.model.dto.LoginByCodeDTO;
import com.businessreviews.model.dto.LoginByPasswordDTO;
import com.businessreviews.model.dto.app.OAuthLoginDTO;
import com.businessreviews.model.dto.SendCodeDTO;
import com.businessreviews.model.vo.LoginVO;
import com.businessreviews.service.app.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 移动端用户认证控制器 (UniApp)
 * <p>
 * 提供移动端用户的认证相关API：
 * - POST /auth/send-code - 发送验证码
 * - POST /auth/login-by-code - 验证码登录
 * - POST /auth/oauth-login - 第三方登录（微信/QQ/微博）
 * - POST /auth/logout - 退出登录
 * </p>
 * 
 * @author businessreviews
 * @see com.businessreviews.service.app.AuthService
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 发送验证码
     *
     * @param request 请求参数（手机号）
     * @return 成功结果
     */
    @PostMapping("/send-code")
    public Result<?> sendCode(@RequestBody @Valid SendCodeDTO request) {
        authService.sendCode(request);
        return Result.success("验证码已发送");
    }

    /**
     * 验证码登录
     *
     * @param request 登录参数（手机号、验证码）
     * @return 登录结果（Token及用户信息）
     */
    @PostMapping("/login-by-code")
    public Result<LoginVO> loginByCode(@RequestBody @Valid LoginByCodeDTO request) {
        LoginVO response = authService.loginByCode(request);
        return Result.success("登录成功", response);
    }

    /**
     * 密码登录
     *
     * @param request 登录参数（手机号、密码）
     * @return 登录结果（Token及用户信息）
     */
    @PostMapping("/login-by-password")
    public Result<LoginVO> loginByPassword(@RequestBody @Valid LoginByPasswordDTO request) {
        LoginVO response = authService.loginByPassword(request);
        return Result.success("登录成功", response);
    }

    /**
     * 第三方登录（已禁用）
     *
     * @param request OAuth登录参数
     * @return 登录结果
     */
    @PostMapping("/oauth-login")
    public Result<LoginVO> oauthLogin(@RequestBody @Valid OAuthLoginDTO request) {
        LoginVO response = authService.oauthLogin(request);
        return Result.success("登录成功", response);
    }

    /**
     * 退出登录
     *
     * @param authorization JWT Token
     * @return 成功结果
     */
    @PostMapping("/logout")
    public Result<?> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.logout(authorization);
        return Result.success("退出成功");
    }

}
