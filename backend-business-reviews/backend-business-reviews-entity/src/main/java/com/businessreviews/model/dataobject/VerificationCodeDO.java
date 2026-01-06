package com.businessreviews.model.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 验证码数据映射对象
 * <p>
 * 对应数据库表 verification_codes，存储短信/邮件验证码及其状态
 * </p>
 * 
 * @author businessreviews
 */
@Data
@TableName("verification_codes")
public class VerificationCodeDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String phone;

    private String code;

    /**
     * 类型（1登录，2注册，3重置密码）
     */
    @TableField("code_type")
    private Integer type;

    /**
     * 是否已使用
     */
    @TableField("is_used")
    private Boolean used;

    @TableField("expire_time")
    private LocalDateTime expireAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
