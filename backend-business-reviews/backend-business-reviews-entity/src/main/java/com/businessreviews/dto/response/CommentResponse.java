package com.businessreviews.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论响应
 */
@Data
public class CommentResponse {
    
    private Long id;
    
    private String author;
    
    private Long authorId;
    
    private String avatar;
    
    private String content;
    
    private LocalDateTime time;
    
    private String timeAgo;
    
    private Integer likes;
    
    private Boolean liked;
    
    private Integer replyCount;
    
    private String replyToUser;
    
    private List<CommentResponse> replies;
}
