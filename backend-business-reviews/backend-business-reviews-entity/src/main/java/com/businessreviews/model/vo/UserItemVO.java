package com.businessreviews.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户列表项展示对象
 * <p>
 * 用于用户列表（如粉丝列表、关注列表）展示
 * </p>
 * 注意：Boolean字段使用@JsonProperty保持API兼容性
 * 
 * @author businessreviews
 */
@Data
public class UserItemVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String userId;
    private String username;
    private String avatar;
    private String bio;

    /**
     * 是否已关注
     * 注意：按照阿里巴巴规范，Boolean字段不使用is前缀
     * 使用@JsonProperty保持API兼容性
     */
    @JsonProperty("isFollowing")
    private Boolean following;
}
