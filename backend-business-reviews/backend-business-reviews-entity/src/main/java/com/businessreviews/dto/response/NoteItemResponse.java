package com.businessreviews.dto.response;

import lombok.Data;

@Data
public class NoteItemResponse {
    private String id;
    private String image;
    private String coverImage;  // 封面图片
    private String title;
    private String content;     // 笔记内容
    private String author;
    private String authorAvatar;
    private String authorId;
    private Integer likes;
    private Integer views;
    private Integer comments;   // 评论数
    private String tag;
    private String tagClass;
    private String createdAt;
    private Integer status;     // 状态
    private Integer syncStatus; // 同步状态
    private Boolean syncedToApp; // 是否已同步到App
    
    // 商家笔记相关字段
    private String shopId;
    private String shopName;
    private Integer noteType; // 1用户笔记，2商家笔记
}
