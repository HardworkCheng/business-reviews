package com.businessreviews.model.dto.app;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 第三方登录请求传输对象
 * <p>
 * 包含第三方平台类型及授权码(Code)
 * </p>
 * 
 * @author businessreviews
 */
@Data
public class OAuthLoginDTO {

    @NotBlank(message = "登录类型不能为空")
    private String type; // wechat/qq/weibo

    @NotBlank(message = "授权码不能为空")
    private String code;
}
