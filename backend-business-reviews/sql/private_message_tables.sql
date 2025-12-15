-- 私信功能相关数据库表

-- 1. 会话表（用于管理两个用户之间的聊天会话）
CREATE TABLE IF NOT EXISTS `conversations` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `user1_id` BIGINT NOT NULL COMMENT '用户1的ID（较小的ID）',
  `user2_id` BIGINT NOT NULL COMMENT '用户2的ID（较大的ID）',
  `last_message_id` BIGINT DEFAULT NULL COMMENT '最后一条消息ID',
  `last_message_content` VARCHAR(500) DEFAULT NULL COMMENT '最后一条消息内容',
  `last_message_time` DATETIME DEFAULT NULL COMMENT '最后一条消息时间',
  `user1_unread_count` INT DEFAULT 0 COMMENT '用户1的未读消息数',
  `user2_unread_count` INT DEFAULT 0 COMMENT '用户2的未读消息数',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users` (`user1_id`, `user2_id`),
  KEY `idx_user1` (`user1_id`),
  KEY `idx_user2` (`user2_id`),
  KEY `idx_updated_at` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话表';

-- 2. 私信消息表
CREATE TABLE IF NOT EXISTS `private_messages` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `conversation_id` BIGINT NOT NULL COMMENT '会话ID',
  `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
  `receiver_id` BIGINT NOT NULL COMMENT '接收者ID',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `message_type` TINYINT DEFAULT 1 COMMENT '消息类型：1=文本，2=图片，3=语音',
  `is_read` TINYINT DEFAULT 0 COMMENT '是否已读：0=未读，1=已读',
  `read_at` DATETIME DEFAULT NULL COMMENT '已读时间',
  `is_deleted_by_sender` TINYINT DEFAULT 0 COMMENT '发送者是否删除：0=否，1=是',
  `is_deleted_by_receiver` TINYINT DEFAULT 0 COMMENT '接收者是否删除：0=否，1=是',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_conversation` (`conversation_id`),
  KEY `idx_sender` (`sender_id`),
  KEY `idx_receiver` (`receiver_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='私信消息表';

-- 3. 在线状态表（用于WebSocket连接管理）
CREATE TABLE IF NOT EXISTS `user_online_status` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `is_online` TINYINT DEFAULT 0 COMMENT '是否在线：0=离线，1=在线',
  `last_online_time` DATETIME DEFAULT NULL COMMENT '最后在线时间',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户在线状态表';
