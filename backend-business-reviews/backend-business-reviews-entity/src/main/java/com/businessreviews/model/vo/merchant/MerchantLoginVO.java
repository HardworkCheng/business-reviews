package com.businessreviews.model.vo.merchant;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 商家端登录VO
 * 迁移自 dto.response.MerchantLoginResponse
 */
@Data
public class MerchantLoginVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    /** JWT Token */
    private String token;
    
    /** 用户信息 */
    private MerchantUserInfoVO userInfo;
}
