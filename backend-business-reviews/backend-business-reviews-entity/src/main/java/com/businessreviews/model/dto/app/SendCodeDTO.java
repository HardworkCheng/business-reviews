package com.businessreviews.model.dto.app;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

/**
 * 短信验证码发送请求传输对象
 * <p>
 * 请求发送特定业务类型的验证码（如登录、注册、修改密码等）。
 * </p>
 * 
 * @author businessreviews
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
