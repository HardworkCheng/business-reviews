package com.businessreviews.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户基础信息VO
 * <p>
 * 用于缓存的轻量级用户信息，仅包含高频访问的字段。
 * 适用于笔记列表、评论列表等场景下的用户信息展示。
 * </p>
 *
 * @author businessreviews
 */
@Data
public class UserBasicVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long id;

    /** 用户名/昵称 */
    private String username;

    /** 头像URL */
    private String avatar;
}
