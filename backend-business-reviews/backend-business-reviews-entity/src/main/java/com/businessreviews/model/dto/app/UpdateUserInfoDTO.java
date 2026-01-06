package com.businessreviews.model.dto.app;

import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import java.time.LocalDate;

/**
 * 用户个人资料更新请求传输对象
 * <p>
 * 修改头像、昵称、简介等公开资料。
 * </p>
 * 
 * @author businessreviews
 */
@Data
public class UpdateUserInfoDTO {

    @Size(min = 2, max = 20, message = "用户名长度应在2-20之间")
    private String username;

    private String avatar;

    @Size(max = 100, message = "个人简介最多100个字符")
    private String bio;

    @Range(min = 0, max = 2, message = "性别参数错误")
    private Integer gender;

    private LocalDate birthday;

    private String wechatOpenid;

    private String qqOpenid;

    private String weiboUid;
}
