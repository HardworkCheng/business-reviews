package com.businessreviews.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 商家注册请求
 */
@Data
public class MerchantRegisterRequest {
    
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    private String phone;
    
    @NotBlank(message = "验证码不能为空")
    private String code;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    @NotBlank(message = "商家名称不能为空")
    private String merchantName;
    
    /** 联系人姓名（可选） */
    private String contactName;
}
