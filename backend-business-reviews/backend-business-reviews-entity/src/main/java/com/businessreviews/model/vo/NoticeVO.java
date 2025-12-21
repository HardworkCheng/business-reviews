package com.businessreviews.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统通知VO
 */
@Data
public class NoticeVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    private Long id;
    
    private String user;
    
    private Long userId;
    
    private String avatar;
    
    private String action;
    
    private String time;
    
    private String icon;
    
    private String type;  // like/comment/follow
    
    private String image;
    
    private Long targetId;
    
    /**
     * 是否已读
     */
    @JsonProperty("isRead")
    private Boolean readStatus;
}
