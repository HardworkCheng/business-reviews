package com.businessreviews.dto.merchant.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 商家端登录请求
 */
@Data
public class MerchantLoginRequest {
    
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
