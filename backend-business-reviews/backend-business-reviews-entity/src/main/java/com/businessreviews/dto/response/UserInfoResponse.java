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
    
    // 脱敏后的手机号(用于显示)
    private String phone;
    
    // 完整手机号(用于发送验证码等敏感操作)
    private String fullPhone;
    
    private Integer gender;
    
    private LocalDate birthday;
    
    private String wechatOpenid;
    
    private String qqOpenid;
    
    private String weiboUid;
    
    private Integer followingCount;
    
    private Integer followerCount;
    
    private Integer likeCount;
    
    private Integer favoriteCount;
    
    private Integer noteCount;
}
