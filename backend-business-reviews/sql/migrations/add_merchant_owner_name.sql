-- 为 merchants 表添加 merchant_owner_name 字段
-- 用于存储商家负责人姓名

ALTER TABLE `merchants` 
ADD COLUMN `merchant_owner_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '商家负责人姓名' 
AFTER `name`;
