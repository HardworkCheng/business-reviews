package com.businessreviews.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Email;
import lombok.Data;

/**
 * 商家入驻注册请求
 * 整合了原merchant_users表和merchants表的所有必要字段
 */
@Data
public class MerchantRegisterRequest {
    
    // ========== 登录信息 ==========
    
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    private String phone;
    
    @NotBlank(message = "验证码不能为空")
    private String code;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    // ========== 商家基本信息 ==========
    
    @NotBlank(message = "商家名称不能为空")
    private String merchantName;
    
    /** 商家Logo */
    private String logo;
    
    /** 商家头像（用于UniApp用户账号） */
    private String avatar;
    
    // ========== 联系人信息 ==========
    
    @NotBlank(message = "联系人姓名不能为空")
    private String contactName;
    
    /** 联系邮箱 */
    @Email(message = "邮箱格式错误")
    private String contactEmail;
    
    // ========== 营业资质信息 ==========
    
    /** 营业执照号 */
    private String licenseNo;
    
    /** 营业执照图片 */
    private String licenseImage;
}
