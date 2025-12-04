package com.businessreviews.dto.response;

import lombok.Data;

/**
 * 登录响应
 */
@Data
public class LoginResponse {
    
    private String token;
    
    private UserInfoResponse userInfo;
}
