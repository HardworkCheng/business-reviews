package com.businessreviews.model.vo.merchant;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 商家端用户信息VO
 * 迁移自 dto.response.MerchantUserInfoResponse
 */
@Data
public class MerchantUserInfoVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    /** 用户ID */
    private String userId;
    
    /** 商家ID */
    private String merchantId;
    
    /** 商家名称 */
    private String merchantName;
    
    /** 商家Logo */
    private String merchantLogo;
    
    /** 用户姓名 */
    private String name;
    
    /** 手机号 */
    private String phone;
    
    /** 头像 */
    private String avatar;
    
    /** 角色名称 */
    private String roleName;
    
    /** 权限列表 */
    private List<String> permissions;
}
