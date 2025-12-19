-- 修复notes表缺失的字段
-- 执行时间: 2025-12-18
-- 问题: 商家发布笔记时报错 "Unknown column 'note_type' in 'field list'"

-- 使用存储过程安全添加字段（如果不存在则添加）
DELIMITER //

-- 添加 note_type 字段
DROP PROCEDURE IF EXISTS add_note_type_column//
CREATE PROCEDURE add_note_type_column()
BEGIN
    IF NOT EXISTS (
        SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'notes' 
        AND COLUMN_NAME = 'note_type'
    ) THEN
        ALTER TABLE `notes` ADD COLUMN `note_type` TINYINT NOT NULL DEFAULT 1 COMMENT '笔记类型（1用户笔记，2商家笔记）' AFTER `user_id`;
        SELECT 'Added note_type column' AS result;
    ELSE
        SELECT 'note_type column already exists' AS result;
    END IF;
END//

-- 添加 merchant_id 字段
DROP PROCEDURE IF EXISTS add_merchant_id_column//
CREATE PROCEDURE add_merchant_id_column()
BEGIN
    IF NOT EXISTS (
        SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'notes' 
        AND COLUMN_NAME = 'merchant_id'
    ) THEN
        ALTER TABLE `notes` ADD COLUMN `merchant_id` BIGINT NULL DEFAULT NULL COMMENT '商家ID（商家笔记专用）' AFTER `note_type`;
        SELECT 'Added merchant_id column' AS result;
    ELSE
        SELECT 'merchant_id column already exists' AS result;
    END IF;
END//

-- 添加 sync_status 字段
DROP PROCEDURE IF EXISTS add_sync_status_column//
CREATE PROCEDURE add_sync_status_column()
BEGIN
    IF NOT EXISTS (
        SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'notes' 
        AND COLUMN_NAME = 'sync_status'
    ) THEN
        ALTER TABLE `notes` ADD COLUMN `sync_status` TINYINT NOT NULL DEFAULT 1 COMMENT '同步状态（0未同步，1已同步）' AFTER `is_recommend`;
        SELECT 'Added sync_status column' AS result;
    ELSE
        SELECT 'sync_status column already exists' AS result;
    END IF;
END//

DELIMITER ;

-- 执行存储过程
CALL add_note_type_column();
CALL add_merchant_id_column();
CALL add_sync_status_column();

-- 清理存储过程
DROP PROCEDURE IF EXISTS add_note_type_column;
DROP PROCEDURE IF EXISTS add_merchant_id_column;
DROP PROCEDURE IF EXISTS add_sync_status_column;

-- 添加索引（如果不存在）
-- 注意：MySQL不支持 IF NOT EXISTS 语法添加索引，所以使用忽略错误的方式
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'notes' AND INDEX_NAME = 'idx_note_type') = 0,
    'ALTER TABLE `notes` ADD INDEX `idx_note_type`(`note_type`)',
    'SELECT "idx_note_type already exists"'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'notes' AND INDEX_NAME = 'idx_merchant_id') = 0,
    'ALTER TABLE `notes` ADD INDEX `idx_merchant_id`(`merchant_id`)',
    'SELECT "idx_merchant_id already exists"'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'notes' AND INDEX_NAME = 'idx_sync_status') = 0,
    'ALTER TABLE `notes` ADD INDEX `idx_sync_status`(`sync_status`)',
    'SELECT "idx_sync_status already exists"'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 验证字段是否添加成功
SELECT COLUMN_NAME, DATA_TYPE, COLUMN_DEFAULT, IS_NULLABLE, COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'notes'
AND COLUMN_NAME IN ('note_type', 'merchant_id', 'sync_status')
ORDER BY ORDINAL_POSITION;

-- 显示表结构
DESCRIBE notes;
