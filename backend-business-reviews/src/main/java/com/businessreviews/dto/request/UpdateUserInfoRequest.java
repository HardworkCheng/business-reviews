package com.businessreviews.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import java.time.LocalDate;

/**
 * 更新用户信息请求
 */
@Data
public class UpdateUserInfoRequest {
    
    @Size(min = 2, max = 20, message = "用户名长度应在2-20之间")
    private String username;
    
    private String avatar;
    
    @Size(max = 100, message = "个人简介最多100个字符")
    private String bio;
    
    @Range(min = 0, max = 2, message = "性别参数错误")
    private Integer gender;
    
    private LocalDate birthday;
}
