package com.businessreviews.model.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息数据对象
 */
@Data
@TableName("messages")
public class MessageDO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long senderId;
    
    private Long receiverId;
    
    private String content;
    
    private Integer type; // 1=文本, 2=图片
    
    /**
     * 是否已读
     */
    @TableField("is_read")
    private Boolean readStatus;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
