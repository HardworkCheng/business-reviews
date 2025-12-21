package com.businessreviews.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户资料VO
 * 注意：Boolean字段使用@JsonProperty保持API兼容性
 */
@Data
public class UserProfileVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String userId;
    private String username;
    private String avatar;
    private String bio;
    private Integer followingCount;
    private Integer followerCount;
    private Integer likeCount;
    private Integer noteCount;
    
    /**
     * 是否已关注
     * 注意：按照阿里巴巴规范，Boolean字段不使用is前缀
     * 使用@JsonProperty保持API兼容性
     */
    @JsonProperty("isFollowing")
    private Boolean following;
}
