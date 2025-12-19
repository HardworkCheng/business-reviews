-- ========================================
-- 合并 merchant_users 表到 merchants 表
-- 执行此脚本前请先备份数据库
-- ========================================

-- 1. 为 merchants 表添加新字段（如果不存在）
-- 注意：执行前请检查字段是否已存在，避免重复添加

-- 添加密码字段
ALTER TABLE `merchants` 
ADD COLUMN IF NOT EXISTS `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '登录密码（加密）' AFTER `contact_email`;

-- 添加头像字段
ALTER TABLE `merchants` 
ADD COLUMN IF NOT EXISTS `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '商家头像' AFTER `password`;

-- 添加角色ID字段
ALTER TABLE `merchants` 
ADD COLUMN IF NOT EXISTS `role_id` bigint NULL DEFAULT NULL COMMENT '角色ID' AFTER `avatar`;

-- 添加最后登录时间字段
ALTER TABLE `merchants` 
ADD COLUMN IF NOT EXISTS `last_login_at` datetime NULL DEFAULT NULL COMMENT '最后登录时间' AFTER `role_id`;

-- 2. 从 merchant_users 表迁移数据到 merchants 表
UPDATE `merchants` m
INNER JOIN `merchant_users` mu ON m.id = mu.merchant_id
SET 
    m.password = mu.password,
    m.avatar = mu.avatar,
    m.role_id = mu.role_id,
    m.last_login_at = mu.last_login_at,
    m.contact_email = COALESCE(m.contact_email, mu.email);

-- 3. 为 contact_phone 添加唯一索引（用于登录）
-- 注意：如果已存在重复的 contact_phone，需要先处理
ALTER TABLE `merchants` ADD UNIQUE INDEX `uk_contact_phone`(`contact_phone` ASC);

-- 4. 验证数据迁移结果
SELECT 
    m.id,
    m.name,
    m.contact_phone,
    m.password,
    m.avatar,
    m.last_login_at
FROM `merchants` m;

-- 5. 确认数据迁移成功后，可以删除 merchant_users 表
-- DROP TABLE IF EXISTS `merchant_users`;

-- ========================================
-- 注意事项：
-- 1. 执行前请备份数据库
-- 2. 确认数据迁移成功后再删除 merchant_users 表
-- 3. 删除表后需要更新后端代码
-- ========================================
