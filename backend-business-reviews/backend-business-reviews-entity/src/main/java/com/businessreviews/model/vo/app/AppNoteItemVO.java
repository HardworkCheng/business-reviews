package com.businessreviews.model.vo.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户端笔记列表项VO
 */
@Data
public class AppNoteItemVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String title;
    private String content;
    private String coverImage;
    private List<String> images;
    private Integer likeCount;
    private Integer commentCount;
    private Integer viewCount;
    private Long userId;
    private String userNickname;
    private String userAvatar;
    
    @JsonProperty("isLiked")
    private Boolean liked;
    
    private LocalDateTime createTime;
}
