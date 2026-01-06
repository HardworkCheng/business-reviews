package com.businessreviews.web.merchant;

import com.businessreviews.common.Result;
import com.businessreviews.model.dto.merchant.MerchantLoginDTO;
import com.businessreviews.model.dto.merchant.MerchantRegisterDTO;
import com.businessreviews.model.dto.SendCodeDTO;
import com.businessreviews.model.vo.merchant.MerchantLoginVO;
import com.businessreviews.model.vo.merchant.MerchantUserInfoVO;
import com.businessreviews.merchant.context.MerchantContext;
import com.businessreviews.service.merchant.MerchantAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 商家运营中心 - 认证控制器 (Web端)
 * <p>
 * 提供商家运营中心的认证相关API：
 * - POST /merchant/auth/send-code - 发送验证码
 * - POST /merchant/auth/login - 登录（支持密码和验证码）
 * - POST /merchant/auth/register - 商家入驻注册
 * - GET /merchant/auth/profile - 获取当前用户信息
 * - POST /merchant/auth/logout - 退出登录
 * </p>
 *
 * @author businessreviews
 * @see com.businessreviews.service.merchant.MerchantAuthService
 */
@RestController
@RequestMapping("/merchant/auth")
@RequiredArgsConstructor
public class MerchantAuthController {

    private final MerchantAuthService merchantAuthService;

    /**
     * 发送验证码
     *
     * @param request 请求参数
     * @return 成功结果
     */
    @PostMapping("/send-code")
    public Result<?> sendCode(@RequestBody @Valid SendCodeDTO request) {
        merchantAuthService.sendCode(request.getPhone());
        return Result.success("验证码已发送");
    }

    /**
     * 登录（支持密码和验证码）
     *
     * @param request 登录请求参数
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result<MerchantLoginVO> login(@RequestBody @Valid MerchantLoginDTO request) {
        MerchantLoginVO response;
        if ("code".equals(request.getLoginType())) {
            response = merchantAuthService.loginByCode(request.getPhone(), request.getCode());
        } else {
            response = merchantAuthService.loginByPassword(request);
        }
        return Result.success("登录成功", response);
    }

    /**
     * 商家入驻注册
     *
     * @param request 注册请求参数
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result<MerchantLoginVO> register(@RequestBody @Valid MerchantRegisterDTO request) {
        MerchantLoginVO response = merchantAuthService.register(request);
        return Result.success("入驻成功", response);
    }

    /**
     * 获取当前用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/profile")
    public Result<MerchantUserInfoVO> getProfile() {
        Long userId = MerchantContext.requireUserId();
        MerchantUserInfoVO response = merchantAuthService.getCurrentUserInfo(userId);
        return Result.success(response);
    }

    /**
     * 更新当前用户信息
     *
     * @param request 更新请求参数
     * @return 成功结果
     */
    @PutMapping("/profile")
    public com.businessreviews.common.Result<?> updateProfile(
            @RequestBody com.businessreviews.model.dto.merchant.MerchantUpdateDTO request) {
        Long userId = MerchantContext.requireUserId();
        merchantAuthService.updateProfile(userId, request);
        return com.businessreviews.common.Result.success("更新成功");
    }

    /**
     * 退出登录
     *
     * @param authorization Token
     * @return 成功结果
     */
    @PostMapping("/logout")
    public Result<?> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        merchantAuthService.logout(authorization);
        return Result.success("退出成功");
    }
}
