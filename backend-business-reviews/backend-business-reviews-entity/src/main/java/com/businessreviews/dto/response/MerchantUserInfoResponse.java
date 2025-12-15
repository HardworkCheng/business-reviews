package com.businessreviews.dto.response;

import lombok.Data;
import java.util.List;

/**
 * 商家用户信息响应
 */
@Data
public class MerchantUserInfoResponse {
    
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
