-- 为shops表添加merchant_id字段
-- 执行此脚本前请确保已备份数据

-- 添加merchant_id字段
ALTER TABLE `shops` ADD COLUMN `merchant_id` BIGINT DEFAULT NULL COMMENT '所属商家ID' AFTER `id`;

-- 添加索引
ALTER TABLE `shops` ADD KEY `idx_merchant_id` (`merchant_id`);

-- 完成
SELECT '字段添加成功' AS result;
