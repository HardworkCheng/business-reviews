package com.businessreviews.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录成功响应对象
 * <p>
 * 包含鉴权Token及通过鉴权获取的用户基础信息
 * </p>
 * 
 * @author businessreviews
 */
@Data
public class LoginVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String token;

    private UserInfoVO userInfo;
}
