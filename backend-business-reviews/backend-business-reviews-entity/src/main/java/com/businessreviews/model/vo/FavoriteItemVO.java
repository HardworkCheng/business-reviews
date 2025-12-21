package com.businessreviews.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 收藏项VO
 */
@Data
public class FavoriteItemVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String id;
    private Integer type;  // 1=笔记, 2=商家
    private String targetId;
    private String image;
    private String title;
    private Integer likes;  // 点赞数
    private String createdAt;
}
