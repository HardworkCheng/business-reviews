package com.businessreviews.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 笔记详情响应
 */
@Data
public class NoteDetailResponse {
    
    private Long id;
    
    private String image;
    
    private List<String> images;
    
    private String title;
    
    private String content;
    
    private String author;
    
    private String authorAvatar;
    
    private Long authorId;
    
    private String publishTime;
    
    private List<String> tags;
    
    private Integer likeCount;
    
    private Integer commentCount;
    
    private Integer viewCount;
    
    private Integer favoriteCount;
    
    private Boolean isLiked;
    
    private Boolean isBookmarked;
    
    private String location;
    
    private Long shopId;
    
    private String shopName;
    
    private LocalDateTime createdAt;
}
