-- ========================================
-- 商家运营中心数据库扩展脚本
-- 基于 business_reviews 数据库
-- ========================================

USE `business_reviews`;

-- ========================================
-- 1. 商家表
-- ========================================
DROP TABLE IF EXISTS `merchants`;
CREATE TABLE `merchants` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商家ID',
  `name` VARCHAR(100) NOT NULL COMMENT '商家名称/企业名称',
  `logo` VARCHAR(500) DEFAULT NULL COMMENT '商家Logo',
  `license_no` VARCHAR(50) DEFAULT NULL COMMENT '营业执照号',
  `license_image` VARCHAR(500) DEFAULT NULL COMMENT '营业执照图片',
  `contact_name` VARCHAR(50) DEFAULT NULL COMMENT '联系人姓名',
  `contact_phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
  `contact_email` VARCHAR(100) DEFAULT NULL COMMENT '联系邮箱',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1正常，2禁用，3待审核）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_contact_phone` (`contact_phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家表';

-- ========================================
-- 2. 商家用户表（运营人员）
-- ========================================
DROP TABLE IF EXISTS `merchant_users`;
CREATE TABLE `merchant_users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `merchant_id` BIGINT NOT NULL COMMENT '所属商家ID',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `password` VARCHAR(100) NOT NULL COMMENT '密码（加密）',
  `name` VARCHAR(50) DEFAULT NULL COMMENT '姓名',
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像',
  `role_id` BIGINT DEFAULT NULL COMMENT '角色ID',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1正常，2禁用）',
  `last_login_at` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家用户表';

-- ========================================
-- 3. 商家角色表
-- ========================================
DROP TABLE IF EXISTS `merchant_roles`;
CREATE TABLE `merchant_roles` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `merchant_id` BIGINT NOT NULL COMMENT '所属商家ID',
  `name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `permissions` JSON DEFAULT NULL COMMENT '权限列表（JSON）',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '角色描述',
  `is_system` TINYINT NOT NULL DEFAULT 0 COMMENT '是否系统角色（0否，1是）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家角色表';

-- ========================================
-- 4. 商家门店关联表（扩展shops表）
-- ========================================
ALTER TABLE `shops` ADD COLUMN `merchant_id` BIGINT DEFAULT NULL COMMENT '所属商家ID' AFTER `id`;
ALTER TABLE `shops` ADD KEY `idx_merchant_id` (`merchant_id`);

-- ========================================
-- 5. 优惠券表
-- ========================================
DROP TABLE IF EXISTS `coupons`;
CREATE TABLE `coupons` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '优惠券ID',
  `merchant_id` BIGINT NOT NULL COMMENT '所属商家ID',
  `shop_id` BIGINT DEFAULT NULL COMMENT '适用门店ID（空表示全部门店）',
  `type` TINYINT NOT NULL COMMENT '券类型（1现金券，2折扣券，3专属券，4新人券）',
  `title` VARCHAR(100) NOT NULL COMMENT '券标题',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '券描述',
  `amount` DECIMAL(10,2) DEFAULT NULL COMMENT '优惠金额（现金券）',
  `discount` DECIMAL(3,2) DEFAULT NULL COMMENT '折扣率（折扣券，如0.85表示85折）',
  `min_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '最低消费金额',
  `total_count` INT NOT NULL DEFAULT 0 COMMENT '发放总量',
  `remain_count` INT NOT NULL DEFAULT 0 COMMENT '剩余数量',
  `daily_limit` INT DEFAULT NULL COMMENT '每日领取限制',
  `per_user_limit` INT NOT NULL DEFAULT 1 COMMENT '每人限领次数',
  `start_time` DATETIME NOT NULL COMMENT '生效时间',
  `end_time` DATETIME NOT NULL COMMENT '失效时间',
  `use_start_time` DATETIME DEFAULT NULL COMMENT '可使用开始时间',
  `use_end_time` DATETIME DEFAULT NULL COMMENT '可使用结束时间',
  `applicable_categories` JSON DEFAULT NULL COMMENT '适用品类（JSON）',
  `stackable` TINYINT NOT NULL DEFAULT 0 COMMENT '是否可叠加（0否，1是）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1启用，2停用，3已结束）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`),
  KEY `idx_end_time` (`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠券表';

-- ========================================
-- 6. 用户领券记录表
-- ========================================
DROP TABLE IF EXISTS `user_coupons`;
CREATE TABLE `user_coupons` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `coupon_id` BIGINT NOT NULL COMMENT '优惠券ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `code` VARCHAR(32) NOT NULL COMMENT '券码（唯一）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1未使用，2已使用，3已过期）',
  `receive_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
  `use_time` DATETIME DEFAULT NULL COMMENT '使用时间',
  `use_shop_id` BIGINT DEFAULT NULL COMMENT '使用门店ID',
  `operator_id` BIGINT DEFAULT NULL COMMENT '核销操作员ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_coupon_id` (`coupon_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户领券记录表';

-- ========================================
-- 7. 优惠券渠道统计表
-- ========================================
DROP TABLE IF EXISTS `coupon_channels`;
CREATE TABLE `coupon_channels` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `coupon_id` BIGINT NOT NULL COMMENT '优惠券ID',
  `channel_type` VARCHAR(20) NOT NULL COMMENT '渠道类型（app/detail/scan）',
  `pv` INT NOT NULL DEFAULT 0 COMMENT '浏览量',
  `uv` INT NOT NULL DEFAULT 0 COMMENT '独立访客数',
  `claim_count` INT NOT NULL DEFAULT 0 COMMENT '领取数量',
  `stat_date` DATE NOT NULL COMMENT '统计日期',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_coupon_channel_date` (`coupon_id`, `channel_type`, `stat_date`),
  KEY `idx_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠券渠道统计表';

-- ========================================
-- 8. 笔记审核表
-- ========================================
DROP TABLE IF EXISTS `note_audits`;
CREATE TABLE `note_audits` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '审核记录ID',
  `note_id` BIGINT NOT NULL COMMENT '笔记ID',
  `status` TINYINT NOT NULL COMMENT '审核状态（1待审核，2通过，3驳回）',
  `reason` VARCHAR(500) DEFAULT NULL COMMENT '驳回原因',
  `suggestion` VARCHAR(500) DEFAULT NULL COMMENT '修改建议',
  `auditor_id` BIGINT DEFAULT NULL COMMENT '审核员ID',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_note_id` (`note_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记审核表';

-- ========================================
-- 9. 扩展笔记表字段
-- ========================================
ALTER TABLE `notes` ADD COLUMN `merchant_id` BIGINT DEFAULT NULL COMMENT '商家ID（商家发布的笔记）' AFTER `user_id`;
ALTER TABLE `notes` ADD COLUMN `publish_time` DATETIME DEFAULT NULL COMMENT '定时发布时间' AFTER `status`;
ALTER TABLE `notes` ADD COLUMN `audit_status` TINYINT DEFAULT 1 COMMENT '审核状态（1草稿，2待审核，3已通过，4已驳回）' AFTER `publish_time`;
ALTER TABLE `notes` ADD KEY `idx_merchant_id` (`merchant_id`);
ALTER TABLE `notes` ADD KEY `idx_audit_status` (`audit_status`);

-- ========================================
-- 10. 商家操作日志表
-- ========================================
DROP TABLE IF EXISTS `merchant_operation_logs`;
CREATE TABLE `merchant_operation_logs` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `operator_id` BIGINT NOT NULL COMMENT '操作员ID',
  `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作员姓名',
  `module` VARCHAR(50) NOT NULL COMMENT '模块（shop/note/coupon/member）',
  `action` VARCHAR(50) NOT NULL COMMENT '操作类型（create/update/delete/publish/offline/redeem）',
  `target_id` BIGINT DEFAULT NULL COMMENT '目标ID',
  `content` TEXT DEFAULT NULL COMMENT '操作内容（JSON）',
  `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_module` (`module`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家操作日志表';

-- ========================================
-- 11. 商家数据统计表（每日汇总）
-- ========================================
DROP TABLE IF EXISTS `merchant_daily_stats`;
CREATE TABLE `merchant_daily_stats` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `shop_id` BIGINT DEFAULT NULL COMMENT '门店ID（空表示商家汇总）',
  `stat_date` DATE NOT NULL COMMENT '统计日期',
  `note_views` INT NOT NULL DEFAULT 0 COMMENT '笔记浏览量',
  `note_likes` INT NOT NULL DEFAULT 0 COMMENT '笔记点赞数',
  `note_comments` INT NOT NULL DEFAULT 0 COMMENT '笔记评论数',
  `note_favorites` INT NOT NULL DEFAULT 0 COMMENT '笔记收藏数',
  `new_followers` INT NOT NULL DEFAULT 0 COMMENT '新增粉丝数',
  `coupon_claims` INT NOT NULL DEFAULT 0 COMMENT '优惠券领取数',
  `coupon_redeems` INT NOT NULL DEFAULT 0 COMMENT '优惠券核销数',
  `shop_views` INT NOT NULL DEFAULT 0 COMMENT '门店浏览量',
  `shop_favorites` INT NOT NULL DEFAULT 0 COMMENT '门店收藏数',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_merchant_shop_date` (`merchant_id`, `shop_id`, `stat_date`),
  KEY `idx_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家数据统计表';

-- ========================================
-- 12. 快捷回复库表
-- ========================================
DROP TABLE IF EXISTS `merchant_quick_replies`;
CREATE TABLE `merchant_quick_replies` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
  `title` VARCHAR(50) NOT NULL COMMENT '快捷回复标题',
  `content` VARCHAR(500) NOT NULL COMMENT '回复内容',
  `use_count` INT NOT NULL DEFAULT 0 COMMENT '使用次数',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序顺序',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='快捷回复库表';

-- ========================================
-- 初始化默认角色
-- ========================================
INSERT INTO `merchant_roles` (`merchant_id`, `name`, `permissions`, `description`, `is_system`) VALUES
(0, '店主/管理员', '["shop:*","note:*","coupon:*","comment:*","member:*","dashboard:*","settings:*"]', '全部权限', 1),
(0, '运营', '["note:create","note:update","note:list","coupon:create","coupon:update","coupon:list","dashboard:view"]', '笔记与优惠券管理', 1),
(0, '客服', '["comment:list","comment:reply","comment:delete","message:*"]', '评论与消息管理', 1),
(0, '审核', '["note:audit","comment:audit"]', '内容审核', 1);

-- ========================================
-- 完成
-- ========================================
