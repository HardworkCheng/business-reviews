package com.businessreviews.dto.merchant.response;

import lombok.Data;

/**
 * 商家端用户信息响应
 */
@Data
public class MerchantUserInfoResponse {
    
    private Long userId;
    private Long merchantId;
    private String merchantName;
    private String phone;
    private String contactName;
    private Integer role;
    private Integer status;
    private Integer shopCount;
    private Integer noteCount;
    private Integer couponCount;
}
