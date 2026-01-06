package com.businessreviews.model.vo.merchant;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 商家端登录响应对象
 * <p>
 * 包含商家Token及管理员/员工信息
 * </p>
 * 
 * @author businessreviews
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
