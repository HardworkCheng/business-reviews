package com.businessreviews.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 笔记列表项响应
 */
@Data
public class NoteListItemResponse {
    
    private Long id;
    
    private String image;
    
    private String title;
    
    private String author;
    
    private String authorAvatar;
    
    private Long authorId;
    
    private Integer likes;
    
    private Integer views;
    
    private String tag;
    
    private String tagClass;
    
    private LocalDateTime createdAt;
}
