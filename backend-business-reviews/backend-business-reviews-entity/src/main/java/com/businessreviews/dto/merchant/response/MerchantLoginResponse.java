package com.businessreviews.dto.merchant.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商家端登录响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantLoginResponse {
    
    private Long userId;
    private Long merchantId;
    private String token;
    private String merchantName;
    private String phone;
    private Integer role;
}
