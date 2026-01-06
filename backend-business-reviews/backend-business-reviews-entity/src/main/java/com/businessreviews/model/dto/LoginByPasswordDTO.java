package com.businessreviews.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 账号密码登录请求传输对象
 * <p>
 * 封装前端提交的手机号和密码，用于执行传统密码登录流程
 * </p>
 */
@Data
public class LoginByPasswordDTO {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    private String phone;

    @NotBlank(message = "密码不能为空")
    private String password;
}
