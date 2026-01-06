package com.businessreviews.model.dto.merchant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 商家端登录请求传输对象
 * <p>
 * 支持两种登录模式：
 * 1. 密码登录 (loginType=password)
 * 2. 验证码登录 (loginType=code)
 * </p>
 * 
 * @author businessreviews
 */
@Data
public class MerchantLoginDTO {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 密码（密码登录时必填） */
    private String password;

    /** 验证码（验证码登录时必填） */
    private String code;

    /** 登录方式：password/code */
    private String loginType;
}
