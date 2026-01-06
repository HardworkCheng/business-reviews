package com.businessreviews.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 笔记视图对象（VO）
 * <p>
 * 用于Web层返回给前端展示，封装了笔记的完整业务数据
 * </p>
 * 
 * @author businessreviews
 */
@Data
public class NoteVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 笔记ID（字符串类型，避免前端精度丢失）
     */
    private String noteId;

    /**
     * 笔记标题
     */
    private String title;

    /**
     * 笔记内容
     */
    private String content;

    /**
     * 封面图
     */
    private String coverImage;

    /**
     * 图片列表
     */
    private List<String> images;

    /**
     * 位置信息
     */
    private String location;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 浏览数
     */
    private Integer viewCount;

    /**
     * 收藏数
     */
    private Integer favoriteCount;

    /**
     * 分享数
     */
    private Integer shareCount;

    /**
     * 标签类型（hot/discount/new）
     */
    private String tagType;

    /**
     * 是否推荐
     */
    private Boolean recommend;

    /**
     * 创建时间（格式化字符串）
     */
    private String createdAt;

    /**
     * 作者信息
     */
    private UserVO author;

    /**
     * 关联商家信息
     */
    private ShopVO shop;

    /**
     * 是否已点赞
     */
    private Boolean liked;

    /**
     * 是否已收藏
     */
    private Boolean favorited;
}
