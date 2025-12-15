package com.businessreviews.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发送消息请求
 */
@Data
public class SendMessageRequest {
    
    /** 接收者ID */
    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;
    
    /** 消息内容 */
    @NotBlank(message = "消息内容不能为空")
    private String content;
    
    /** 消息类型：1=文本，2=图片，3=语音 */
    private Integer messageType = 1;
}
