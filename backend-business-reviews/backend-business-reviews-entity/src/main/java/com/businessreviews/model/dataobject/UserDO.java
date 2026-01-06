package com.businessreviews.model.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户基础信息数据映射对象
 * <p>
 * 对应数据库表 users，存储用户的账号、资料及认证信息
 * </p>
 * 
 * @author businessreviews
 */
@Data
@TableName("users")
public class UserDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 用户名
     */
    private String username;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 密码（加密）
     */
    private String password;

    /**
     * 微信OpenID
     */
    private String wechatOpenid;

    /**
     * QQ OpenID
     */
    private String qqOpenid;

    /**
     * 微博UID
     */
    private String weiboUid;

    /**
     * 性别（0未知，1男，2女）
     */
    private Integer gender;

    /**
     * 生日
     */
    private LocalDate birthday;

    /**
     * 状态（1正常，2禁用）
     */
    private Integer status;

    /**
     * 最后登录时间
     */
    @TableField("last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
