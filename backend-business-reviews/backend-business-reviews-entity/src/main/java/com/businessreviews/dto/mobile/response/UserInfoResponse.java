package com.businessreviews.dto.mobile.response;

import lombok.Data;

/**
 * 移动端用户信息响应
 */
@Data
public class UserInfoResponse {
    
    private Long id;
    private String nickname;
    private String avatar;
    private String phone;
    private Integer gender;
    private String bio;
    private String birthday;
    private String location;
    private Integer noteCount;
    private Integer followingCount;
    private Integer followerCount;
    private Integer likeCount;
}
