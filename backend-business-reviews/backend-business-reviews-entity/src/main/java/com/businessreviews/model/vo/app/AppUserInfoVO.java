package com.businessreviews.model.vo.app;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户端用户信息VO
 */
@Data
public class AppUserInfoVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
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
