package com.businessreviews.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录响应VO
 */
@Data
public class LoginVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    private String token;
    
    private UserInfoVO userInfo;
}
