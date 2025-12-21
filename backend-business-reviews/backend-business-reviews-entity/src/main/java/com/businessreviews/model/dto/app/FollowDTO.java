package com.businessreviews.model.dto.app;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户端关注请求DTO
 */
@Data
public class FollowDTO {
    
    @NotBlank(message = "用户ID不能为空")
    private String userId;
}
