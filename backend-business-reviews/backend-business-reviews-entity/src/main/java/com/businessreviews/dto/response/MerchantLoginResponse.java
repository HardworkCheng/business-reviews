package com.businessreviews.dto.response;

import lombok.Data;

/**
 * 商家登录响应
 */
@Data
public class MerchantLoginResponse {
    
    /** JWT Token */
    private String token;
    
    /** 用户信息 */
    private MerchantUserInfoResponse userInfo;
}
