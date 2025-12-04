package com.businessreviews.dto.response;

import lombok.Data;

@Data
public class TopicResponse {
    private String id;
    private String name;
    private String description;
    private String coverImage;
    private Integer noteCount;
    private Integer viewCount;
    private Boolean isHot;
}
