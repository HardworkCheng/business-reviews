-- 为shop_reviews表添加商家回复字段
-- 用于支持商家运营中心的回复功能

USE business_reviews;

-- 添加reply字段（商家回复内容）
ALTER TABLE shop_reviews 
ADD COLUMN `reply` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '商家回复内容' AFTER `content`;

-- 添加reply_time字段（回复时间）
ALTER TABLE shop_reviews 
ADD COLUMN `reply_time` datetime NULL COMMENT '回复时间' AFTER `reply`;

-- 验证字段是否添加成功
DESC shop_reviews;
