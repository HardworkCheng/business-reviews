-- ========================================
-- 额外需要的数据库表
-- ========================================

-- 1. 用户收藏表（如果不存在）
DROP TABLE IF EXISTS `user_favorites`;
CREATE TABLE IF NOT EXISTS `user_favorites` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `type` TINYINT NOT NULL COMMENT '类型（1笔记，2商家）',
  `target_id` BIGINT NOT NULL COMMENT '目标ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_type_target` (`user_id`, `type`, `target_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏表';

-- 2. 用户浏览历史表（如果不存在）
CREATE TABLE IF NOT EXISTS `user_browse_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `target_type` TINYINT NOT NULL COMMENT '目标类型（1笔记，2商家）',
  `target_id` BIGINT NOT NULL COMMENT '目标ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户浏览历史表';

-- 3. 商家评价表（如果不存在）
CREATE TABLE IF NOT EXISTS `shop_reviews` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `shop_id` BIGINT NOT NULL COMMENT '商家ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `rating` DECIMAL(2,1) NOT NULL DEFAULT 5.0 COMMENT '总体评分',
  `taste_score` DECIMAL(2,1) DEFAULT 5.0 COMMENT '口味评分',
  `environment_score` DECIMAL(2,1) DEFAULT 5.0 COMMENT '环境评分',
  `service_score` DECIMAL(2,1) DEFAULT 5.0 COMMENT '服务评分',
  `content` TEXT COMMENT '评价内容',
  `images` TEXT COMMENT '评价图片（JSON数组）',
  `like_count` INT DEFAULT 0 COMMENT '点赞数',
  `status` TINYINT DEFAULT 1 COMMENT '状态（1正常，0隐藏）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家评价表';

-- 4. 为shops表添加merchant_id字段（如果不存在）
ALTER TABLE `shops` ADD COLUMN IF NOT EXISTS `merchant_id` BIGINT DEFAULT NULL COMMENT '所属商家ID' AFTER `id`;
ALTER TABLE `shops` ADD INDEX IF NOT EXISTS `idx_merchant_id` (`merchant_id`);

-- 5. 优惠券表（如果不存在）
CREATE TABLE IF NOT EXISTS `coupons` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '优惠券ID',
  `merchant_id` BIGINT NOT NULL COMMENT '所属商家ID',
  `shop_id` BIGINT DEFAULT NULL COMMENT '适用门店ID（空表示全部门店）',
  `type` TINYINT NOT NULL DEFAULT 1 COMMENT '券类型（1现金券，2折扣券，3专属券，4新人券）',
  `title` VARCHAR(100) NOT NULL COMMENT '券标题',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '券描述',
  `amount` DECIMAL(10,2) DEFAULT NULL COMMENT '优惠金额（现金券）',
  `discount` DECIMAL(3,2) DEFAULT NULL COMMENT '折扣率（折扣券，如0.85表示85折）',
  `min_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '最低消费金额',
  `total_count` INT NOT NULL DEFAULT 0 COMMENT '发放总量',
  `remain_count` INT NOT NULL DEFAULT 0 COMMENT '剩余数量',
  `daily_limit` INT DEFAULT NULL COMMENT '每日领取限制',
  `per_user_limit` INT DEFAULT 1 COMMENT '每人限领次数',
  `start_time` DATETIME NOT NULL COMMENT '生效时间',
  `end_time` DATETIME NOT NULL COMMENT '失效时间',
  `use_start_time` DATETIME DEFAULT NULL COMMENT '可使用开始时间',
  `use_end_time` DATETIME DEFAULT NULL COMMENT '可使用结束时间',
  `applicable_categories` VARCHAR(500) DEFAULT NULL COMMENT '适用品类（JSON）',
  `stackable` TINYINT DEFAULT 0 COMMENT '是否可叠加（0否，1是）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1启用，2停用，3已结束）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠券表';

-- 6. 用户优惠券表（如果不存在）
CREATE TABLE IF NOT EXISTS `user_coupons` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `coupon_id` BIGINT NOT NULL COMMENT '优惠券ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `code` VARCHAR(50) NOT NULL COMMENT '券码（唯一）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1未使用，2已使用，3已过期）',
  `receive_time` DATETIME NOT NULL COMMENT '领取时间',
  `use_time` DATETIME DEFAULT NULL COMMENT '使用时间',
  `use_shop_id` BIGINT DEFAULT NULL COMMENT '使用门店ID',
  `operator_id` BIGINT DEFAULT NULL COMMENT '核销操作员ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_coupon_id` (`coupon_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户优惠券表';

-- 7. 插入测试优惠券数据
INSERT INTO `coupons` (`merchant_id`, `shop_id`, `type`, `title`, `description`, `amount`, `discount`, `min_amount`, `total_count`, `remain_count`, `per_user_limit`, `start_time`, `end_time`, `status`) VALUES
(1, NULL, 1, '新人专享10元券', '新用户首单立减10元', 10.00, NULL, 50.00, 1000, 950, 1, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 1),
(1, NULL, 2, '全场8.8折', '全场商品享8.8折优惠', NULL, 0.88, 100.00, 500, 480, 1, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 1),
(1, 1, 1, '美食专享20元券', '指定门店满100减20', 20.00, NULL, 100.00, 200, 180, 2, NOW(), DATE_ADD(NOW(), INTERVAL 15 DAY), 1);
