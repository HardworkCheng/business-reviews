package com.businessreviews.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 笔记详情响应
 */
@Data
public class NoteDetailResponse {
    
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
    
    @com.fasterxml.jackson.annotation.JsonProperty("isLiked")
    @lombok.Getter(lombok.AccessLevel.NONE)
    private Boolean isLiked;
    
    @com.fasterxml.jackson.annotation.JsonProperty("isBookmarked")
    @lombok.Getter(lombok.AccessLevel.NONE)
    private Boolean isBookmarked;
    
    @com.fasterxml.jackson.annotation.JsonProperty("isFollowing")
    @lombok.Getter(lombok.AccessLevel.NONE)
    private Boolean isFollowing; // 是否关注作者
    
    @com.fasterxml.jackson.annotation.JsonProperty("isAuthor")
    @lombok.Getter(lombok.AccessLevel.NONE)
    private Boolean isAuthor; // 是否是作者本人
    
    // Custom getters to ensure proper JSON serialization
    public Boolean getIsLiked() {
        return isLiked;
    }
    
    public Boolean getIsBookmarked() {
        return isBookmarked;
    }
    
    public Boolean getIsFollowing() {
        return isFollowing;
    }
    
    public Boolean getIsAuthor() {
        return isAuthor;
    }
    
    private String location;
    
    /**
     * 纬度
     */
    private BigDecimal latitude;
    
    /**
     * 经度
     */
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
