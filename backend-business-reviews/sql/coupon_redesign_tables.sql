-- ========================================
-- 优惠券系统重新设计 - 数据库表结构
-- 创建日期: 2025-12-21
-- 说明: 支持移动端优惠券领取和管理功能
-- 
-- 执行说明：
-- 1. 本文件可以独立执行，会自动创建所需的基础表
-- 2. 如果基础表已存在，使用 CREATE TABLE IF NOT EXISTS 不会覆盖
-- 3. 新增的表使用 DROP TABLE IF EXISTS 确保干净创建
-- ========================================

USE `business_reviews`;

-- ========================================
-- 0. 确保基础表存在
-- ========================================

-- 创建 coupons 表（如果不存在）
CREATE TABLE IF NOT EXISTS `coupons` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '优惠券ID',
  `merchant_id` BIGINT NOT NULL COMMENT '所属商家ID',
  `shop_id` BIGINT DEFAULT NULL COMMENT '适用店铺ID（NULL表示全部店铺）',
  `type` TINYINT NOT NULL COMMENT '类型（1满减券，2折扣券，3代金券）',
  `title` VARCHAR(100) NOT NULL COMMENT '优惠券标题',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '使用说明',
  `amount` DECIMAL(10,2) DEFAULT NULL COMMENT '优惠金额（满减券、代金券）',
  `discount` DECIMAL(5,2) DEFAULT NULL COMMENT '折扣（折扣券，如0.8表示8折）',
  `min_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '最低消费金额',
  `total_count` INT NOT NULL COMMENT '发行总量',
  `remain_count` INT NOT NULL COMMENT '剩余数量',
  `per_user_limit` INT NOT NULL DEFAULT 1 COMMENT '每人限领数量',
  `start_time` DATETIME NOT NULL COMMENT '有效期开始时间',
  `end_time` DATETIME NOT NULL COMMENT '有效期结束时间',
  `stackable` TINYINT NOT NULL DEFAULT 0 COMMENT '是否可叠加使用（0否，1是）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（0下架，1上架）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_status` (`status`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_end_time` (`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠券表';

-- 创建 user_coupons 表（如果不存在）
CREATE TABLE IF NOT EXISTS `user_coupons` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `coupon_id` BIGINT NOT NULL COMMENT '优惠券ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `code` VARCHAR(32) NOT NULL COMMENT '优惠券码',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1未使用，2已使用，3已过期）',
  `receive_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
  `use_time` DATETIME DEFAULT NULL COMMENT '使用时间',
  `use_shop_id` BIGINT DEFAULT NULL COMMENT '使用店铺ID',
  `operator_id` BIGINT DEFAULT NULL COMMENT '核销操作员ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_coupon_id` (`coupon_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_receive_time` (`receive_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户优惠券表';

-- ========================================
-- 1. 秒杀活动表（新增）
-- ========================================
DROP TABLE IF EXISTS `seckill_activities`;
CREATE TABLE `seckill_activities` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '秒杀活动ID',
  `merchant_id` BIGINT NOT NULL COMMENT '所属商家ID',
  `title` VARCHAR(100) NOT NULL COMMENT '活动标题',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '活动描述',
  `start_time` DATETIME NOT NULL COMMENT '活动开始时间',
  `end_time` DATETIME NOT NULL COMMENT '活动结束时间',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1未开始，2进行中，3已结束，4已取消）',
  `display_order` INT NOT NULL DEFAULT 0 COMMENT '显示顺序',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_status` (`status`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_end_time` (`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀活动表';

-- ========================================
-- 2. 秒杀券关联表（新增）
-- ========================================
DROP TABLE IF EXISTS `seckill_coupons`;
CREATE TABLE `seckill_coupons` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `seckill_id` BIGINT NOT NULL COMMENT '秒杀活动ID',
  `coupon_id` BIGINT NOT NULL COMMENT '优惠券ID',
  `seckill_price` DECIMAL(10,2) NOT NULL COMMENT '秒杀价格',
  `original_price` DECIMAL(10,2) NOT NULL COMMENT '原价',
  `seckill_stock` INT NOT NULL COMMENT '秒杀库存',
  `remain_stock` INT NOT NULL COMMENT '剩余库存',
  `seckill_limit` INT NOT NULL DEFAULT 1 COMMENT '每人限购数量',
  `display_order` INT NOT NULL DEFAULT 0 COMMENT '显示顺序',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_seckill_coupon` (`seckill_id`, `coupon_id`),
  KEY `idx_coupon_id` (`coupon_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀券关联表';

-- ========================================
-- 3. 用户秒杀记录表（新增）
-- ========================================
DROP TABLE IF EXISTS `user_seckill_records`;
CREATE TABLE `user_seckill_records` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `seckill_id` BIGINT NOT NULL COMMENT '秒杀活动ID',
  `coupon_id` BIGINT NOT NULL COMMENT '优惠券ID',
  `user_coupon_id` BIGINT NOT NULL COMMENT '用户优惠券ID',
  `seckill_price` DECIMAL(10,2) NOT NULL COMMENT '秒杀价格',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '抢购时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_seckill_id` (`seckill_id`),
  KEY `idx_coupon_id` (`coupon_id`),
  KEY `idx_user_coupon_id` (`user_coupon_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户秒杀记录表';

-- ========================================
-- 4. 优惠券浏览记录表（新增，用于统计）
-- ========================================
DROP TABLE IF EXISTS `coupon_view_logs`;
CREATE TABLE `coupon_view_logs` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `coupon_id` BIGINT NOT NULL COMMENT '优惠券ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '用户ID（未登录为NULL）',
  `device_id` VARCHAR(64) DEFAULT NULL COMMENT '设备ID',
  `source` VARCHAR(20) NOT NULL COMMENT '来源（center/detail/share）',
  `view_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
  PRIMARY KEY (`id`),
  KEY `idx_coupon_id` (`coupon_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_view_time` (`view_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠券浏览记录表';

-- ========================================
-- 5. 优惠券搜索记录表（新增，用于优化搜索）
-- ========================================
DROP TABLE IF EXISTS `coupon_search_logs`;
CREATE TABLE `coupon_search_logs` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '用户ID（未登录为NULL）',
  `keyword` VARCHAR(100) NOT NULL COMMENT '搜索关键词',
  `result_count` INT NOT NULL DEFAULT 0 COMMENT '搜索结果数量',
  `search_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '搜索时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_keyword` (`keyword`),
  KEY `idx_search_time` (`search_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠券搜索记录表';

-- ========================================
-- 6. 优惠券统计表（新增，用于商家查看数据）
-- ========================================
DROP TABLE IF EXISTS `coupon_statistics`;
CREATE TABLE `coupon_statistics` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `coupon_id` BIGINT NOT NULL COMMENT '优惠券ID',
  `stat_date` DATE NOT NULL COMMENT '统计日期',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
  `view_user_count` INT NOT NULL DEFAULT 0 COMMENT '浏览用户数',
  `claim_count` INT NOT NULL DEFAULT 0 COMMENT '领取次数',
  `claim_user_count` INT NOT NULL DEFAULT 0 COMMENT '领取用户数',
  `use_count` INT NOT NULL DEFAULT 0 COMMENT '使用次数',
  `use_user_count` INT NOT NULL DEFAULT 0 COMMENT '使用用户数',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_coupon_date` (`coupon_id`, `stat_date`),
  KEY `idx_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠券统计表';

-- ========================================
-- 7. 为现有 coupons 表添加秒杀相关字段（可选）
-- ========================================
-- 如果需要在优惠券表中直接标记秒杀券，可以执行以下语句：
-- ALTER TABLE `coupons` ADD COLUMN `is_seckill` TINYINT NOT NULL DEFAULT 0 COMMENT '是否为秒杀券（0否，1是）' AFTER `stackable`;
-- ALTER TABLE `coupons` ADD COLUMN `seckill_price` DECIMAL(10,2) DEFAULT NULL COMMENT '秒杀价格' AFTER `is_seckill`;
-- ALTER TABLE `coupons` ADD COLUMN `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价' AFTER `seckill_price`;

-- ========================================
-- 8. 为现有 user_coupons 表添加过期时间字段（可选）
-- ========================================
-- ALTER TABLE `user_coupons` ADD COLUMN `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间（冗余字段，便于查询）' AFTER `use_time`;
-- ALTER TABLE `user_coupons` ADD COLUMN `order_id` BIGINT DEFAULT NULL COMMENT '关联订单ID' AFTER `operator_id`;

-- ========================================
-- 数据初始化示例
-- ========================================

-- 插入示例秒杀活动
INSERT INTO `seckill_activities` (`merchant_id`, `title`, `description`, `start_time`, `end_time`, `status`, `display_order`) VALUES
(1, '限时秒杀', '每日限时秒杀，抢到就是赚到！', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 1 DAY), 2, 100);

-- ========================================
-- 视图创建（可选，用于简化查询）
-- ========================================

-- 创建用户优惠券详情视图
CREATE OR REPLACE VIEW `v_user_coupon_details` AS
SELECT 
  uc.id AS user_coupon_id,
  uc.user_id,
  uc.code,
  uc.status AS user_coupon_status,
  uc.receive_time,
  uc.use_time,
  uc.use_shop_id,
  c.id AS coupon_id,
  c.merchant_id,
  c.shop_id,
  c.type,
  c.title,
  c.description,
  c.amount,
  c.discount,
  c.min_amount,
  c.start_time,
  c.end_time,
  c.stackable,
  c.status AS coupon_status,
  CASE 
    WHEN uc.status = 2 THEN '已使用'
    WHEN uc.status = 3 THEN '已过期'
    WHEN NOW() > c.end_time THEN '已过期'
    ELSE '未使用'
  END AS status_text
FROM `user_coupons` uc
INNER JOIN `coupons` c ON uc.coupon_id = c.id;

-- 创建可领取优惠券视图
CREATE OR REPLACE VIEW `v_available_coupons` AS
SELECT 
  c.*,
  CASE 
    WHEN c.remain_count <= 0 THEN '已抢光'
    WHEN NOW() < c.start_time THEN '未开始'
    WHEN NOW() > c.end_time THEN '已结束'
    WHEN c.status != 1 THEN '已下架'
    ELSE '可领取'
  END AS availability_status
FROM `coupons` c
WHERE c.status = 1 
  AND NOW() >= c.start_time 
  AND NOW() <= c.end_time
  AND c.remain_count > 0;

-- ========================================
-- 存储过程：用户领取优惠券
-- ========================================
DROP PROCEDURE IF EXISTS `sp_claim_coupon`;

DELIMITER //
CREATE PROCEDURE `sp_claim_coupon`(
  IN p_user_id BIGINT,
  IN p_coupon_id BIGINT,
  OUT p_result INT,
  OUT p_message VARCHAR(200),
  OUT p_code VARCHAR(32)
)
BEGIN
  DECLARE v_remain_count INT;
  DECLARE v_per_user_limit INT;
  DECLARE v_user_claim_count INT;
  DECLARE v_coupon_code VARCHAR(32);
  
  -- 开始事务
  START TRANSACTION;
  
  -- 检查优惠券是否存在且可用
  SELECT remain_count, per_user_limit INTO v_remain_count, v_per_user_limit
  FROM coupons
  WHERE id = p_coupon_id 
    AND status = 1 
    AND NOW() >= start_time 
    AND NOW() <= end_time
  FOR UPDATE;
  
  IF v_remain_count IS NULL THEN
    SET p_result = -1;
    SET p_message = '优惠券不存在或已下架';
    ROLLBACK;
  ELSEIF v_remain_count <= 0 THEN
    SET p_result = -2;
    SET p_message = '优惠券已抢光';
    ROLLBACK;
  ELSE
    -- 检查用户领取次数
    SELECT COUNT(*) INTO v_user_claim_count
    FROM user_coupons
    WHERE user_id = p_user_id AND coupon_id = p_coupon_id;
    
    IF v_user_claim_count >= v_per_user_limit THEN
      SET p_result = -3;
      SET p_message = '已达领取上限';
      ROLLBACK;
    ELSE
      -- 生成优惠券码
      SET v_coupon_code = CONCAT('CPN', LPAD(p_coupon_id, 8, '0'), LPAD(p_user_id, 8, '0'), UNIX_TIMESTAMP());
      
      -- 插入用户优惠券记录
      INSERT INTO user_coupons (coupon_id, user_id, code, status, receive_time)
      VALUES (p_coupon_id, p_user_id, v_coupon_code, 1, NOW());
      
      -- 扣减库存
      UPDATE coupons SET remain_count = remain_count - 1 WHERE id = p_coupon_id;
      
      SET p_result = 0;
      SET p_message = '领取成功';
      SET p_code = v_coupon_code;
      COMMIT;
    END IF;
  END IF;
END //
DELIMITER ;

-- ========================================
-- 完成
-- ========================================
SELECT '优惠券系统表结构创建完成！' AS message;
