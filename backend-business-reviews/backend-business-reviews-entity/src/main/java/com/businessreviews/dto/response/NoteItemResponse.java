package com.businessreviews.dto.response;

import lombok.Data;

@Data
public class NoteItemResponse {
    private String id;
    private String image;
    private String title;
    private String author;
    private String authorAvatar;
    private String authorId;
    private Integer likes;
    private Integer views;
    private String tag;
    private String tagClass;
    private String createdAt;
}
