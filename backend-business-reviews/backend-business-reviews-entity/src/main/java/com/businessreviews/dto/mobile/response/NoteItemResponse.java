package com.businessreviews.dto.mobile.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 移动端笔记列表项响应
 */
@Data
public class NoteItemResponse {
    
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
    private Boolean isLiked;
    private LocalDateTime createTime;
}
