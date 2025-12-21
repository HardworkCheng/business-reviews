package com.businessreviews.model.dto.app;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户端关注用户请求DTO
 */
@Data
public class FollowUserDTO {
    
    @NotNull(message = "用户ID不能为空")
    private Long userId;
}
