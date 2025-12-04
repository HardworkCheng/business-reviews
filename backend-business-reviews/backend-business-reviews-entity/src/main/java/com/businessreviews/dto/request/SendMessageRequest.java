package com.businessreviews.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendMessageRequest {
    
    @NotBlank(message = "目标用户ID不能为空")
    private String targetUserId;
    
    @NotBlank(message = "消息内容不能为空")
    private String content;
    
    private Integer type = 1; // 1=文本, 2=图片
}
