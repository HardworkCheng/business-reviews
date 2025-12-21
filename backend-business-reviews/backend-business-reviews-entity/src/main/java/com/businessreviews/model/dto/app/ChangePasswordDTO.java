package com.businessreviews.model.dto.app;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户端修改密码请求DTO
 */
@Data
public class ChangePasswordDTO {
    
    /**
     * 验证码
     */
    @NotBlank(message = "验证码不能为空")
    private String code;
    
    /**
     * 旧密码
     */
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;
    
    /**
     * 新密码
     */
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
    
    /**
     * 确认新密码
     */
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}
