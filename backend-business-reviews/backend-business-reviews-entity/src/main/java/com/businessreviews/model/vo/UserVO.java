package com.businessreviews.model.vo;

import com.businessreviews.model.annotation.Sensitive;
import com.businessreviews.model.annotation.SensitiveType;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户视图对象（VO）
 * <p>
 * 用于Web层返回给前端展示，封装用户核心信息，敏感字段已脱敏
 * </p>
 * 
 * @author businessreviews
 */
@Data
public class UserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID（字符串类型，避免前端精度丢失）
     */
    private String userId;

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
     * 手机号（脱敏：138****1234）
     */
    @Sensitive(type = SensitiveType.PHONE)
    private String phone;

    /**
     * 性别（0未知，1男，2女）
     */
    private Integer gender;

    /**
     * 生日（格式化字符串）
     */
    private String birthday;

    /**
     * 关注数
     */
    private Integer followingCount;

    /**
     * 粉丝数
     */
    private Integer followerCount;

    /**
     * 获赞数
     */
    private Integer likeCount;

    /**
     * 收藏数
     */
    private Integer favoriteCount;

    /**
     * 笔记数
     */
    private Integer noteCount;

    /**
     * 是否已关注（当前用户视角）
     */
    private Boolean following;
}
