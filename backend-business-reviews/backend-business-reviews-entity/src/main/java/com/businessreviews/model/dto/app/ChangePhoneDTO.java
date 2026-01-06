package com.businessreviews.model.dto.app;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 更换绑定手机号请求传输对象
 * <p>
 * 流程：验证旧手机号验证码 -> 验证新手机号验证码 -> 完成更换
 * </p>
 * 
 * @author businessreviews
 */
@Data
public class ChangePhoneDTO {

    /**
     * 原手机号验证码
     */
    @NotBlank(message = "原手机号验证码不能为空")
    private String oldPhoneCode;

    /**
     * 新手机号
     */
    @NotBlank(message = "新手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    private String newPhone;

    /**
     * 新手机号验证码
     */
    @NotBlank(message = "新手机号验证码不能为空")
    private String newPhoneCode;
}
