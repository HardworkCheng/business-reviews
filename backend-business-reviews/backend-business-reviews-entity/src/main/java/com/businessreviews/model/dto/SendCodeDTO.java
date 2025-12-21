package com.businessreviews.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

/**
 * 发送验证码请求DTO
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
