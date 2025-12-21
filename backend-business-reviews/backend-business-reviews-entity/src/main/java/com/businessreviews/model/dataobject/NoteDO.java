package com.businessreviews.model.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 笔记数据对象（DO）
 * 与数据库表notes一一对应
 * 
 * @author businessreviews
 */
@Data
@TableName("notes")
public class NoteDO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 作者用户ID
     */
    private Long userId;
    
    /**
     * 笔记类型（1用户笔记，2商家笔记）
     */
    private Integer noteType;
    
    /**
     * 商家ID（商家笔记专用）
     */
    private Long merchantId;
    
    /**
     * 关联商家ID
     */
    private Long shopId;
    
    /**
     * 笔记标题
     */
    private String title;
    
    /**
     * 笔记内容
     */
    @TableField(typeHandler = org.apache.ibatis.type.StringTypeHandler.class)
    private String content;
    
    /**
     * 封面图
     */
    private String coverImage;
    
    /**
     * 图片集合（JSON）
     */
    @TableField(typeHandler = org.apache.ibatis.type.StringTypeHandler.class)
    private String images;
    
    /**
     * 位置信息
     */
    private String location;
    
    /**
     * 纬度
     */
    private BigDecimal latitude;
    
    /**
     * 经度
     */
    private BigDecimal longitude;
    
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
     * 状态（1正常，2隐藏，3审核中）
     */
    private Integer status;
    
    /**
     * 是否推荐（0否，1是）
     * 注意：按照阿里巴巴规范，Boolean字段不使用is前缀
     */
    @TableField("is_recommend")
    private Integer recommend;
    
    /**
     * 同步状态（0未同步，1已同步）
     */
    private Integer syncStatus;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
