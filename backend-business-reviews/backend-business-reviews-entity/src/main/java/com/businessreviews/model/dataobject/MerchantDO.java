package com.businessreviews.model.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商家数据对象（DO）
 * 与数据库表merchants一一对应
 * 
 * @author businessreviews
 */
@Data
@TableName("merchants")
public class MerchantDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商家名称/企业名称
     */
    private String name;

    /**
     * 商家负责人姓名
     */
    private String merchantOwnerName;

    /**
     * 商家Logo
     */
    private String logo;

    /**
     * 营业执照号
     */
    private String licenseNo;

    /**
     * 营业执照图片
     */
    private String licenseImage;

    /**
     * 联系电话（用于登录）
     */
    private String contactPhone;

    /**
     * 联系邮箱
     */
    private String contactEmail;

    /**
     * 登录密码（加密）
     */
    private String password;

    /**
     * 商家头像
     */
    private String avatar;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginAt;

    /**
     * 状态（1正常，2禁用，3待审核）
     */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
