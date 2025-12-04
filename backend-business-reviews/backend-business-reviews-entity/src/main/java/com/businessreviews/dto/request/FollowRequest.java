package com.businessreviews.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FollowRequest {
    
    @NotBlank(message = "用户ID不能为空")
    private String userId;
}
