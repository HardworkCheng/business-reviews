package com.businessreviews.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

/**
 * 短信验证码发送请求传输对象
 * <p>
 * 封装验证码发送所需的手机号及业务类型（注册、登录、找回密码等）
 * </p>
 */
@Data
public class SendCodeDTO {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    private String phone;

    @NotNull(message = "验证码类型不能为空")
    @Range(min = 1, max = 5, message = "验证码类型错误")
    private Integer type;
}
