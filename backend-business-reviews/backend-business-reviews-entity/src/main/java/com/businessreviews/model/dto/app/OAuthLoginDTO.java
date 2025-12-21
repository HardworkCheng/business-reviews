package com.businessreviews.model.dto.app;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户端第三方登录请求DTO
 */
@Data
public class OAuthLoginDTO {
    
    @NotBlank(message = "登录类型不能为空")
    private String type;  // wechat/qq/weibo
    
    @NotBlank(message = "授权码不能为空")
    private String code;
}
