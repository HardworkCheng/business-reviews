package com.businessreviews.model.vo.app;

import com.businessreviews.model.annotation.Sensitive;
import com.businessreviews.model.annotation.SensitiveType;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户端个人信息展示对象
 * <p>
 * 包含用户详细资料及社交关系统计
 * </p>
 * 
 * @author businessreviews
 */
@Data
public class AppUserInfoVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String nickname;
    private String avatar;

    @Sensitive(type = SensitiveType.PHONE)
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
