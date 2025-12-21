package com.businessreviews.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 笔记列表项VO
 */
@Data
public class NoteListItemVO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
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
