package com.businessreviews.model.vo;

import com.businessreviews.model.annotation.Sensitive;
import com.businessreviews.model.annotation.SensitiveType;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 用户详细信息展示对象
 * <p>
 * 包含用户基础资料及社交统计数据（关注、粉丝、获赞等）
 * </p>
 * 
 * @author businessreviews
 */
@Data
public class UserInfoVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String userId;

    private String username;

    private String avatar;

    private String bio;

    // 脱敏后的手机号(用于显示)
    @Sensitive(type = SensitiveType.PHONE)
    private String phone;

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
