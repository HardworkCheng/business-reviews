-- 删除 contact_name 字段，统一使用 merchant_owner_name
-- 先确保数据已迁移（如果需要保留旧数据）
-- 这里直接删除字段

ALTER TABLE `merchants` DROP COLUMN `contact_name`;
