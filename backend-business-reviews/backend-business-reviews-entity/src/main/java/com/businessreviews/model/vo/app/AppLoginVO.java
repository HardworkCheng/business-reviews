package com.businessreviews.model.vo.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户端登录响应对象
 * <p>
 * 包含用户Token、基础信息及新用户标识
 * </p>
 * 
 * @author businessreviews
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppLoginVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String token;
    private String nickname;
    private String avatar;

    @JsonProperty("isNew")
    private Boolean newUser;
}
