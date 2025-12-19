-- 快速修复notes表缺失的字段
-- 执行时间: 2025-12-18
-- 问题: 商家发布笔记时报错 "Unknown column 'note_type' in 'field list'"
-- 
-- 使用方法: 在MySQL客户端中执行此脚本
-- 注意: 如果字段已存在会报错，可以忽略

-- 1. 添加 note_type 字段（笔记类型：1用户笔记，2商家笔记）
ALTER TABLE `notes` ADD COLUMN `note_type` TINYINT NOT NULL DEFAULT 1 COMMENT '笔记类型（1用户笔记，2商家笔记）' AFTER `user_id`;

-- 2. 添加 merchant_id 字段（商家ID）
ALTER TABLE `notes` ADD COLUMN `merchant_id` BIGINT NULL DEFAULT NULL COMMENT '商家ID（商家笔记专用）' AFTER `note_type`;

-- 3. 添加 sync_status 字段（同步状态）
ALTER TABLE `notes` ADD COLUMN `sync_status` TINYINT NOT NULL DEFAULT 1 COMMENT '同步状态（0未同步，1已同步）' AFTER `is_recommend`;

-- 4. 添加索引
ALTER TABLE `notes` ADD INDEX `idx_note_type`(`note_type`);
ALTER TABLE `notes` ADD INDEX `idx_merchant_id`(`merchant_id`);
ALTER TABLE `notes` ADD INDEX `idx_sync_status`(`sync_status`);

-- 5. 验证
DESCRIBE notes;
