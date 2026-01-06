package com.businessreviews.model.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 私信消息数据映射对象
 * <p>
 * 对应数据库表 private_messages，存储用户间的即时通讯消息详情
 * </p>
 * 
 * @author businessreviews
 */
@Data
@TableName("private_messages")
public class PrivateMessageDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话ID */
    private Long conversationId;

    /** 发送者ID */
    private Long senderId;

    /** 接收者ID */
    private Long receiverId;

    /** 消息内容 */
    private String content;

    /** 消息类型：1=文本，2=图片，3=语音 */
    private Integer messageType;

    /** 是否已读：0=未读，1=已读 */
    @TableField("is_read")
    private Integer readStatus;

    /** 已读时间 */
    private LocalDateTime readAt;

    /** 发送者是否删除：0=否，1=是 */
    @TableField("is_deleted_by_sender")
    private Integer deletedBySender;

    /** 接收者是否删除：0=否，1=是 */
    @TableField("is_deleted_by_receiver")
    private Integer deletedByReceiver;

    private LocalDateTime createdAt;
}
