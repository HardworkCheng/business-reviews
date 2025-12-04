-- ========================================
-- 美食点评系统数据库初始化脚本
-- Database: business_reviews
-- ========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `business_reviews` 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE `business_reviews`;

-- ========================================
-- 1. 用户表
-- ========================================
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
  `bio` VARCHAR(200) DEFAULT NULL COMMENT '个人简介',
  `password` VARCHAR(100) DEFAULT NULL COMMENT '密码（加密）',
  `wechat_openid` VARCHAR(100) DEFAULT NULL COMMENT '微信OpenID',
  `qq_openid` VARCHAR(100) DEFAULT NULL COMMENT 'QQ OpenID',
  `weibo_uid` VARCHAR(100) DEFAULT NULL COMMENT '微博UID',
  `gender` TINYINT DEFAULT 0 COMMENT '性别（0未知，1男，2女）',
  `birthday` DATE DEFAULT NULL COMMENT '生日',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1正常，2禁用）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_username` (`username`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ========================================
-- 2. 用户统计表
-- ========================================
DROP TABLE IF EXISTS `user_stats`;
CREATE TABLE `user_stats` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `following_count` INT NOT NULL DEFAULT 0 COMMENT '关注数',
  `follower_count` INT NOT NULL DEFAULT 0 COMMENT '粉丝数',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '获赞总数',
  `favorite_count` INT NOT NULL DEFAULT 0 COMMENT '收藏总数',
  `note_count` INT NOT NULL DEFAULT 0 COMMENT '笔记总数',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户统计表';

-- ========================================
-- 3. 关注关系表
-- ========================================
DROP TABLE IF EXISTS `user_follows`;
CREATE TABLE `user_follows` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '关注者ID',
  `follow_user_id` BIGINT NOT NULL COMMENT '被关注者ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_follow` (`user_id`, `follow_user_id`),
  KEY `idx_follow_user_id` (`follow_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关注关系表';

-- ========================================
-- 4. 商家分类表
-- ========================================
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `icon` VARCHAR(100) DEFAULT NULL COMMENT '图标',
  `color` VARCHAR(20) DEFAULT NULL COMMENT '主题颜色',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序顺序',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1启用，2禁用）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status_sort` (`status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家分类表';

-- ========================================
-- 5. 商家信息表
-- ========================================
DROP TABLE IF EXISTS `shops`;
CREATE TABLE `shops` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商家ID',
  `category_id` INT NOT NULL COMMENT '分类ID',
  `name` VARCHAR(100) NOT NULL COMMENT '商家名称',
  `header_image` VARCHAR(500) DEFAULT NULL COMMENT '封面图URL',
  `images` TEXT DEFAULT NULL COMMENT '商家图片集合（JSON）',
  `description` TEXT DEFAULT NULL COMMENT '商家描述',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `address` VARCHAR(200) NOT NULL COMMENT '详细地址',
  `province` VARCHAR(50) DEFAULT NULL COMMENT '省份',
  `city` VARCHAR(50) DEFAULT NULL COMMENT '城市',
  `district` VARCHAR(50) DEFAULT NULL COMMENT '区县',
  `latitude` DECIMAL(10,7) DEFAULT NULL COMMENT '纬度',
  `longitude` DECIMAL(10,7) DEFAULT NULL COMMENT '经度',
  `business_hours` VARCHAR(100) DEFAULT NULL COMMENT '营业时间',
  `average_price` DECIMAL(10,2) DEFAULT NULL COMMENT '人均消费',
  `rating` DECIMAL(3,2) NOT NULL DEFAULT 0.00 COMMENT '综合评分',
  `taste_score` DECIMAL(3,2) NOT NULL DEFAULT 0.00 COMMENT '口味评分',
  `environment_score` DECIMAL(3,2) NOT NULL DEFAULT 0.00 COMMENT '环境评分',
  `service_score` DECIMAL(3,2) NOT NULL DEFAULT 0.00 COMMENT '服务评分',
  `review_count` INT NOT NULL DEFAULT 0 COMMENT '评价数量',
  `popularity` INT NOT NULL DEFAULT 0 COMMENT '人气值',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1营业中，2休息中，3已关闭）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`),
  KEY `idx_city` (`city`),
  KEY `idx_rating` (`rating`),
  KEY `idx_popularity` (`popularity`),
  KEY `idx_status` (`status`),
  KEY `idx_location` (`latitude`, `longitude`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家信息表';

-- ========================================
-- 6. 商家标签表
-- ========================================
DROP TABLE IF EXISTS `shop_tags`;
CREATE TABLE `shop_tags` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `shop_id` BIGINT NOT NULL COMMENT '商家ID',
  `tag_name` VARCHAR(50) NOT NULL COMMENT '标签名称',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家标签表';

-- ========================================
-- 7. 商家评价表
-- ========================================
DROP TABLE IF EXISTS `shop_reviews`;
CREATE TABLE `shop_reviews` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `shop_id` BIGINT NOT NULL COMMENT '商家ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `rating` DECIMAL(3,2) NOT NULL COMMENT '综合评分（1-5）',
  `taste_score` DECIMAL(3,2) DEFAULT NULL COMMENT '口味评分',
  `environment_score` DECIMAL(3,2) DEFAULT NULL COMMENT '环境评分',
  `service_score` DECIMAL(3,2) DEFAULT NULL COMMENT '服务评分',
  `content` TEXT DEFAULT NULL COMMENT '评价内容',
  `images` TEXT DEFAULT NULL COMMENT '评价图片（JSON）',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1正常，2隐藏）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家评价表';

-- ========================================
-- 8. 用户收藏商家表
-- ========================================
DROP TABLE IF EXISTS `user_shop_favorites`;
CREATE TABLE `user_shop_favorites` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `shop_id` BIGINT NOT NULL COMMENT '商家ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_shop` (`user_id`, `shop_id`),
  KEY `idx_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏商家表';

-- ========================================
-- 9. 笔记表
-- ========================================
DROP TABLE IF EXISTS `notes`;
CREATE TABLE `notes` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '笔记ID',
  `user_id` BIGINT NOT NULL COMMENT '作者用户ID',
  `shop_id` BIGINT DEFAULT NULL COMMENT '关联商家ID',
  `title` VARCHAR(200) NOT NULL COMMENT '笔记标题',
  `content` TEXT NOT NULL COMMENT '笔记内容',
  `cover_image` VARCHAR(500) DEFAULT NULL COMMENT '封面图',
  `images` TEXT DEFAULT NULL COMMENT '图片集合（JSON）',
  `location` VARCHAR(100) DEFAULT NULL COMMENT '位置信息',
  `latitude` DECIMAL(10,7) DEFAULT NULL COMMENT '纬度',
  `longitude` DECIMAL(10,7) DEFAULT NULL COMMENT '经度',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` INT NOT NULL DEFAULT 0 COMMENT '评论数',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览数',
  `favorite_count` INT NOT NULL DEFAULT 0 COMMENT '收藏数',
  `share_count` INT NOT NULL DEFAULT 0 COMMENT '分享数',
  `tag_type` VARCHAR(20) DEFAULT NULL COMMENT '标签类型（hot/discount/new）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1正常，2隐藏，3审核中）',
  `is_recommend` TINYINT NOT NULL DEFAULT 0 COMMENT '是否推荐（0否，1是）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_status` (`status`),
  KEY `idx_is_recommend` (`is_recommend`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_like_count` (`like_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记表';

-- ========================================
-- 10. 笔记标签表
-- ========================================
DROP TABLE IF EXISTS `note_tags`;
CREATE TABLE `note_tags` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `note_id` BIGINT NOT NULL COMMENT '笔记ID',
  `tag_name` VARCHAR(50) NOT NULL COMMENT '标签名称',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_note_id` (`note_id`),
  KEY `idx_tag_name` (`tag_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记标签表';

-- ========================================
-- 11. 笔记评论表
-- ========================================
DROP TABLE IF EXISTS `note_comments`;
CREATE TABLE `note_comments` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `note_id` BIGINT NOT NULL COMMENT '笔记ID',
  `user_id` BIGINT NOT NULL COMMENT '评论用户ID',
  `parent_id` BIGINT DEFAULT NULL COMMENT '父评论ID',
  `content` TEXT NOT NULL COMMENT '评论内容',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1正常，2隐藏）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_note_id` (`note_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记评论表';

-- ========================================
-- 12. 用户点赞笔记表
-- ========================================
DROP TABLE IF EXISTS `user_note_likes`;
CREATE TABLE `user_note_likes` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `note_id` BIGINT NOT NULL COMMENT '笔记ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_note` (`user_id`, `note_id`),
  KEY `idx_note_id` (`note_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户点赞笔记表';

-- ========================================
-- 13. 用户收藏笔记表
-- ========================================
DROP TABLE IF EXISTS `user_note_bookmarks`;
CREATE TABLE `user_note_bookmarks` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `note_id` BIGINT NOT NULL COMMENT '笔记ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_note` (`user_id`, `note_id`),
  KEY `idx_note_id` (`note_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏笔记表';

-- ========================================
-- 14. 用户点赞评论表
-- ========================================
DROP TABLE IF EXISTS `user_comment_likes`;
CREATE TABLE `user_comment_likes` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `comment_id` BIGINT NOT NULL COMMENT '评论ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_comment` (`user_id`, `comment_id`),
  KEY `idx_comment_id` (`comment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户点赞评论表';

-- ========================================
-- 15. 消息会话表
-- ========================================
DROP TABLE IF EXISTS `chat_sessions`;
CREATE TABLE `chat_sessions` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `other_user_id` BIGINT NOT NULL COMMENT '对方用户ID',
  `last_message` VARCHAR(500) DEFAULT NULL COMMENT '最后一条消息',
  `last_message_time` DATETIME DEFAULT NULL COMMENT '最后消息时间',
  `unread_count` INT NOT NULL DEFAULT 0 COMMENT '未读数量',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_other` (`user_id`, `other_user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_last_message_time` (`last_message_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息会话表';

-- ========================================
-- 16. 聊天消息表
-- ========================================
DROP TABLE IF EXISTS `chat_messages`;
CREATE TABLE `chat_messages` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `session_id` BIGINT NOT NULL COMMENT '会话ID',
  `from_user_id` BIGINT NOT NULL COMMENT '发送者ID',
  `to_user_id` BIGINT NOT NULL COMMENT '接收者ID',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `message_type` TINYINT NOT NULL DEFAULT 1 COMMENT '消息类型（1文本，2图片，3语音）',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读（0未读，1已读）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_to_user_is_read` (`to_user_id`, `is_read`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

-- ========================================
-- 17. 系统通知表
-- ========================================
DROP TABLE IF EXISTS `system_notices`;
CREATE TABLE `system_notices` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` BIGINT NOT NULL COMMENT '接收通知的用户ID',
  `from_user_id` BIGINT NOT NULL COMMENT '触发通知的用户ID',
  `notice_type` TINYINT NOT NULL COMMENT '通知类型（1点赞笔记，2评论笔记，3关注，4点赞评论）',
  `target_id` BIGINT DEFAULT NULL COMMENT '目标ID',
  `content` VARCHAR(200) DEFAULT NULL COMMENT '通知内容',
  `image_url` VARCHAR(500) DEFAULT NULL COMMENT '关联图片URL',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读（0未读，1已读）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_is_read` (`user_id`, `is_read`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统通知表';

-- ========================================
-- 18. 用户浏览历史表
-- ========================================
DROP TABLE IF EXISTS `user_browse_history`;
CREATE TABLE `user_browse_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `target_type` TINYINT NOT NULL COMMENT '目标类型（1笔记，2商家）',
  `target_id` BIGINT NOT NULL COMMENT '目标ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户浏览历史表';

-- ========================================
-- 19. 验证码表
-- ========================================
DROP TABLE IF EXISTS `verification_codes`;
CREATE TABLE `verification_codes` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `code` VARCHAR(10) NOT NULL COMMENT '验证码',
  `code_type` TINYINT NOT NULL DEFAULT 1 COMMENT '类型（1登录，2注册，3重置密码）',
  `is_used` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已使用',
  `expire_time` DATETIME NOT NULL COMMENT '过期时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_phone_code` (`phone`, `code`),
  KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='验证码表';

-- ========================================
-- 20. 话题表
-- ========================================
DROP TABLE IF EXISTS `topics`;
CREATE TABLE `topics` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '话题ID',
  `name` VARCHAR(100) NOT NULL COMMENT '话题名称',
  `description` TEXT DEFAULT NULL COMMENT '话题描述',
  `cover_image` VARCHAR(500) DEFAULT NULL COMMENT '封面图',
  `note_count` INT NOT NULL DEFAULT 0 COMMENT '笔记数量',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览量',
  `is_hot` TINYINT NOT NULL DEFAULT 0 COMMENT '是否热门',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1启用，2禁用）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`),
  KEY `idx_is_hot` (`is_hot`),
  KEY `idx_note_count` (`note_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='话题表';

-- ========================================
-- 21. 笔记话题关联表
-- ========================================
DROP TABLE IF EXISTS `note_topics`;
CREATE TABLE `note_topics` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `note_id` BIGINT NOT NULL COMMENT '笔记ID',
  `topic_id` BIGINT NOT NULL COMMENT '话题ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_note_topic` (`note_id`, `topic_id`),
  KEY `idx_topic_id` (`topic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记话题关联表';

-- ========================================
-- 初始化分类数据
-- ========================================
INSERT INTO `categories` (`name`, `icon`, `color`, `sort_order`) VALUES
('美食', '🍜', '#FFD166', 1),
('KTV', '🎤', '#EF476F', 2),
('丽人·美发', '💇', '#FF9E64', 3),
('美睫·美甲', '💅', '#06D6A0', 4),
('按摩·足疗', '💆', '#FFD166', 5),
('美容SPA', '🛁', '#EF476F', 6),
('亲子游乐', '👶', '#06D6A0', 7),
('酒吧', '🍷', '#FF9E64', 8);

-- ========================================
-- 完成
-- ========================================
