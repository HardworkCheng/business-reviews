package com.businessreviews.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论VO
 */
@Data
public class CommentVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
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
    
    private List<CommentVO> replies;
}
