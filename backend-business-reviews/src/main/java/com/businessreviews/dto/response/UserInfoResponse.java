package com.businessreviews.dto.response;

import lombok.Data;

import java.time.LocalDate;

/**
 * 用户信息响应
 */
@Data
public class UserInfoResponse {
    
    private String userId;
    
    private String username;
    
    private String avatar;
    
    private String bio;
    
    private String phone;
    
    private Integer gender;
    
    private LocalDate birthday;
    
    private Integer followingCount;
    
    private Integer followerCount;
    
    private Integer likeCount;
    
    private Integer favoriteCount;
    
    private Integer noteCount;
}
