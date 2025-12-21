package com.businessreviews.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 笔记详情VO
 */
@Data
public class NoteDetailVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    private Long id;
    
    private String image;
    
    private String coverImage;
    
    private List<String> images;
    
    private String title;
    
    private String content;
    
    private String author;
    
    private String authorName;
    
    private String authorAvatar;
    
    private Long authorId;
    
    private String publishTime;
    
    private List<String> tags;
    
    /**
     * 话题列表
     */
    private List<TopicInfo> topics;
    
    private Integer likeCount;
    
    private Integer commentCount;
    
    private Integer viewCount;
    
    private Integer favoriteCount;
    
    private Integer shareCount;
    
    private Integer status;
    
    private Integer syncStatus;
    
    private Integer noteType;
    
    /**
     * 是否已点赞
     */
    @com.fasterxml.jackson.annotation.JsonProperty("isLiked")
    private Boolean liked;
    
    /**
     * 是否已收藏
     */
    @com.fasterxml.jackson.annotation.JsonProperty("isBookmarked")
    private Boolean bookmarked;
    
    /**
     * 是否关注作者
     */
    @com.fasterxml.jackson.annotation.JsonProperty("isFollowing")
    private Boolean following;
    
    /**
     * 是否是作者本人
     */
    @com.fasterxml.jackson.annotation.JsonProperty("isAuthor")
    private Boolean selfAuthor;
    
    private String location;
    
    private BigDecimal latitude;
    
    private BigDecimal longitude;
    
    private Long shopId;
    
    private String shopName;
    
    private LocalDateTime createdAt;
    
    /**
     * 话题信息内部类
     */
    @Data
    public static class TopicInfo {
        private Long id;
        private String name;
    }
}
