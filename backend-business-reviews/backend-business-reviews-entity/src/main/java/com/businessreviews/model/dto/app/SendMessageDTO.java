package com.businessreviews.model.dto.app;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发送私信请求传输对象
 * <p>
 * 用于用户之间发送点对点消息（支持文本、图片、语音类型）。
 * </p>
 * 
 * @author businessreviews
 */
@Data
public class SendMessageDTO {

    /** 接收者ID */
    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;

    /** 消息内容 */
    @NotBlank(message = "消息内容不能为空")
    private String content;

    /** 消息类型：1=文本，2=图片，3=语音 */
    private Integer messageType = 1;
}
