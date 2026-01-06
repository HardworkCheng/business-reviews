package com.businessreviews.model.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息通知数据映射对象
 * <p>
 * 对应数据库表 notifications，整合点赞、评论、关注及系统通知
 * </p>
 * 
 * @author businessreviews
 */
@Data
@TableName("notifications")
public class NotificationDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String content;

    private Integer type; // 1=点赞, 2=评论, 3=关注, 4=系统

    private Long relatedId;

    /**
     * 是否已读
     */
    @TableField("is_read")
    private Boolean readStatus;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
