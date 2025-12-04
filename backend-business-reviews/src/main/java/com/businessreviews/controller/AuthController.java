package com.businessreviews.controller;

import com.businessreviews.common.Result;
import com.businessreviews.dto.request.LoginByCodeRequest;
import com.businessreviews.dto.request.OAuthLoginRequest;
import com.businessreviews.dto.request.SendCodeRequest;
import com.businessreviews.dto.response.LoginResponse;
import com.businessreviews.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 发送验证码
     */
    @PostMapping("/send-code")
    public Result<?> sendCode(@RequestBody @Valid SendCodeRequest request) {
        authService.sendCode(request);
        return Result.success("验证码已发送");
    }

    /**
     * 验证码登录
     */
    @PostMapping("/login-by-code")
    public Result<LoginResponse> loginByCode(@RequestBody @Valid LoginByCodeRequest request) {
        LoginResponse response = authService.loginByCode(request);
        return Result.success("登录成功", response);
    }

    /**
     * 第三方登录
     */
    @PostMapping("/oauth-login")
    public Result<LoginResponse> oauthLogin(@RequestBody @Valid OAuthLoginRequest request) {
        LoginResponse response = authService.oauthLogin(request);
        return Result.success("登录成功", response);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result<?> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.logout(authorization);
        return Result.success("退出成功");
    }
}
