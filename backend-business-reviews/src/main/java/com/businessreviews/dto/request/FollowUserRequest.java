package com.businessreviews.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 关注用户请求
 */
@Data
public class FollowUserRequest {
    
    @NotNull(message = "用户ID不能为空")
    private Long userId;
}
