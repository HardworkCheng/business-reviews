-- ========================================
-- 私信功能数据库表创建脚本
-- 执行此脚本以启用私信功能
-- ========================================

-- 1. 消息表（私信消息）
CREATE TABLE IF NOT EXISTS `messages` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
  `receiver_id` BIGINT NOT NULL COMMENT '接收者ID',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `type` TINYINT DEFAULT 1 COMMENT '消息类型：1=文本，2=图片',
  `is_read` TINYINT DEFAULT 0 COMMENT '是否已读：0=未读，1=已读',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_sender` (`sender_id`),
  KEY `idx_receiver` (`receiver_id`),
  KEY `idx_sender_receiver` (`sender_id`, `receiver_id`),
  KEY `idx_receiver_sender` (`receiver_id`, `sender_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='私信消息表';

-- 2. 通知表（系统通知、点赞、评论、关注等）
CREATE TABLE IF NOT EXISTS `notifications` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` BIGINT NOT NULL COMMENT '接收通知的用户ID',
  `title` VARCHAR(200) DEFAULT NULL COMMENT '通知标题',
  `content` TEXT COMMENT '通知内容',
  `type` TINYINT DEFAULT 4 COMMENT '通知类型：1=点赞，2=评论，3=关注，4=系统',
  `related_id` BIGINT DEFAULT NULL COMMENT '关联ID（如笔记ID、评论ID等）',
  `is_read` TINYINT DEFAULT 0 COMMENT '是否已读：0=未读，1=已读',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type` (`type`),
  KEY `idx_user_type` (`user_id`, `type`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- 3. 用户在线状态表（用于WebSocket连接管理，可选）
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

-- ========================================
-- 验证表是否创建成功
-- ========================================
SELECT 'messages表创建成功' AS status;
SELECT 'notifications表创建成功' AS status;
SELECT 'user_online_status表创建成功' AS status;

-- ========================================
-- 查看表结构（可选）
-- ========================================
-- DESCRIBE messages;
-- DESCRIBE notifications;
-- DESCRIBE user_online_status;
