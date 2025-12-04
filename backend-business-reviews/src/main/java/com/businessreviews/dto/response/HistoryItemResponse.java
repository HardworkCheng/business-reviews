package com.businessreviews.dto.response;

import lombok.Data;

@Data
public class HistoryItemResponse {
    private String id;
    private Integer type;  // 1=笔记, 2=商家
    private String targetId;
    private String image;
    private String title;
    private String createdAt;
}
