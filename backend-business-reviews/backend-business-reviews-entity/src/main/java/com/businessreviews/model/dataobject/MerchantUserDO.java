package com.businessreviews.model.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商家用户（运营人员）数据对象（DO）
 * 与数据库表merchant_users一一对应
 * 
 * @author businessreviews
 */
@Data
@TableName("merchant_users")
public class MerchantUserDO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 所属商家ID
     */
    private Long merchantId;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 密码（加密）
     */
    private String password;
    
    /**
     * 姓名
     */
    private String name;
    
    /**
     * 头像
     */
    private String avatar;
    
    /**
     * 角色ID
     */
    private Long roleId;
    
    /**
     * 状态（1正常，2禁用）
     */
    private Integer status;
    
    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginAt;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
