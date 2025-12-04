package com.businessreviews.dto.response;

import lombok.Data;

@Data
public class UserProfileResponse {
    private String userId;
    private String username;
    private String avatar;
    private String bio;
    private Integer followingCount;
    private Integer followerCount;
    private Integer likeCount;
    private Integer noteCount;
    private Boolean isFollowing;
}
