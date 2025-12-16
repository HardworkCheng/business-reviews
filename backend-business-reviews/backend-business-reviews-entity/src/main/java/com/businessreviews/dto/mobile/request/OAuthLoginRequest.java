package com.businessreviews.dto.mobile.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 移动端第三方登录请求
 */
@Data
public class OAuthLoginRequest {
    
    @NotBlank(message = "登录类型不能为空")
    private String type;  // wechat/qq/weibo
    
    @NotBlank(message = "授权码不能为空")
    private String code;
}
