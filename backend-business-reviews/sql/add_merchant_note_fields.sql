-- 添加商家笔记管理相关字段
-- 执行时间: 2025-12-18

-- 1. 添加笔记类型字段，区分用户笔记和商家笔记
ALTER TABLE `notes` ADD COLUMN `note_type` TINYINT NOT NULL DEFAULT 1 COMMENT '笔记类型（1用户笔记，2商家笔记）' AFTER `user_id`;

-- 2. 添加商家ID字段，用于标识商家笔记的发布者
ALTER TABLE `notes` ADD COLUMN `merchant_id` BIGINT NULL DEFAULT NULL COMMENT '商家ID（商家笔记专用）' AFTER `note_type`;

-- 3. 添加同步状态字段，标识是否已同步到UniApp
ALTER TABLE `notes` ADD COLUMN `sync_status` TINYINT NOT NULL DEFAULT 1 COMMENT '同步状态（0未同步，1已同步）' AFTER `is_recommend`;

-- 4. 添加商家笔记相关索引
ALTER TABLE `notes` ADD INDEX `idx_note_type`(`note_type` ASC) USING BTREE;
ALTER TABLE `notes` ADD INDEX `idx_merchant_id`(`merchant_id` ASC) USING BTREE;
ALTER TABLE `notes` ADD INDEX `idx_sync_status`(`sync_status` ASC) USING BTREE;

-- 5. 更新现有数据：将关联了shop_id的笔记标记为商家笔记
UPDATE `notes` SET 
    `note_type` = 2,
    `merchant_id` = (SELECT `merchant_id` FROM `shops` WHERE `shops`.`id` = `notes`.`shop_id` LIMIT 1)
WHERE `shop_id` IS NOT NULL;

-- 6. 添加外键约束（可选，根据需要启用）
-- ALTER TABLE `notes` ADD CONSTRAINT `fk_notes_merchant_id` FOREIGN KEY (`merchant_id`) REFERENCES `merchants` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

-- 7. 创建商家笔记统计视图
CREATE OR REPLACE VIEW `merchant_note_stats` AS
SELECT 
    m.id AS merchant_id,
    m.name AS merchant_name,
    COUNT(n.id) AS total_notes,
    SUM(CASE WHEN n.status = 1 THEN 1 ELSE 0 END) AS published_notes,
    SUM(CASE WHEN n.status = 0 THEN 1 ELSE 0 END) AS draft_notes,
    SUM(CASE WHEN n.status = 3 THEN 1 ELSE 0 END) AS pending_notes,
    SUM(n.like_count) AS total_likes,
    SUM(n.comment_count) AS total_comments,
    SUM(n.view_count) AS total_views,
    SUM(n.favorite_count) AS total_favorites
FROM `merchants` m
LEFT JOIN `notes` n ON m.id = n.merchant_id AND n.note_type = 2
GROUP BY m.id, m.name;

-- 8. 创建笔记同步状态更新触发器
DELIMITER $$
CREATE TRIGGER `update_note_sync_status` 
AFTER UPDATE ON `notes`
FOR EACH ROW
BEGIN
    -- 当笔记状态变为已发布时，标记为需要同步
    IF NEW.status = 1 AND OLD.status != 1 THEN
        UPDATE `notes` SET `sync_status` = 0 WHERE `id` = NEW.id;
    END IF;
END$$
DELIMITER ;

-- 验证数据更新
SELECT 
    note_type,
    COUNT(*) as count,
    COUNT(CASE WHEN merchant_id IS NOT NULL THEN 1 END) as with_merchant_id,
    COUNT(CASE WHEN shop_id IS NOT NULL THEN 1 END) as with_shop_id
FROM notes 
GROUP BY note_type;

-- 显示商家笔记统计
SELECT * FROM merchant_note_stats WHERE total_notes > 0;