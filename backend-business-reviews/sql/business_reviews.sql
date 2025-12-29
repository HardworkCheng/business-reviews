/*
 Navicat Premium Dump SQL

 Source Server         : WebTest
 Source Server Type    : MySQL
 Source Server Version : 80036 (8.0.36)
 Source Host           : localhost:3306
 Source Schema         : business_reviews

 Target Server Type    : MySQL
 Target Server Version : 80036 (8.0.36)
 File Encoding         : 65001

 Date: 29/12/2025 10:52:44
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for categories
-- ----------------------------
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图标',
  `color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '主题颜色',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序顺序',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1启用，2禁用）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status_sort`(`status` ASC, `sort_order` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商家分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of categories
-- ----------------------------
INSERT INTO `categories` VALUES (1, '美食', '🍜', '#FFD166', 1, 1, '2025-12-03 20:26:19', '2025-12-03 20:26:19');
INSERT INTO `categories` VALUES (2, 'KTV', '🎤', '#EF476F', 2, 1, '2025-12-03 20:26:19', '2025-12-03 20:26:19');
INSERT INTO `categories` VALUES (3, '美发', '💇', '#FF9E64', 3, 1, '2025-12-03 20:26:19', '2025-12-20 22:25:50');
INSERT INTO `categories` VALUES (4, '美甲', '💅', '#06D6A0', 4, 1, '2025-12-03 20:26:19', '2025-12-20 22:26:00');
INSERT INTO `categories` VALUES (5, '足疗', '💆', '#FFD166', 5, 1, '2025-12-03 20:26:19', '2025-12-20 22:26:04');
INSERT INTO `categories` VALUES (6, '美容', '🛁', '#EF476F', 6, 1, '2025-12-03 20:26:19', '2025-12-20 22:26:11');
INSERT INTO `categories` VALUES (7, '游乐', '👶', '#06D6A0', 7, 1, '2025-12-03 20:26:19', '2025-12-20 22:26:20');
INSERT INTO `categories` VALUES (8, '酒吧', '🍷', '#FF9E64', 8, 1, '2025-12-03 20:26:19', '2025-12-03 20:26:19');

-- ----------------------------
-- Table structure for chat_messages
-- ----------------------------
DROP TABLE IF EXISTS `chat_messages`;
CREATE TABLE `chat_messages`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `session_id` bigint NOT NULL COMMENT '会话ID',
  `from_user_id` bigint NOT NULL COMMENT '发送者ID',
  `to_user_id` bigint NOT NULL COMMENT '接收者ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容',
  `message_type` tinyint NOT NULL DEFAULT 1 COMMENT '消息类型（1文本，2图片，3语音）',
  `is_read` tinyint NOT NULL DEFAULT 0 COMMENT '是否已读（0未读，1已读）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_session_id`(`session_id` ASC) USING BTREE,
  INDEX `idx_to_user_is_read`(`to_user_id` ASC, `is_read` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '聊天消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_messages
-- ----------------------------

-- ----------------------------
-- Table structure for chat_sessions
-- ----------------------------
DROP TABLE IF EXISTS `chat_sessions`;
CREATE TABLE `chat_sessions`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `other_user_id` bigint NOT NULL COMMENT '对方用户ID',
  `last_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '最后一条消息',
  `last_message_time` datetime NULL DEFAULT NULL COMMENT '最后消息时间',
  `unread_count` int NOT NULL DEFAULT 0 COMMENT '未读数量',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_other`(`user_id` ASC, `other_user_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_last_message_time`(`last_message_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '消息会话表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_sessions
-- ----------------------------

-- ----------------------------
-- Table structure for conversations
-- ----------------------------
DROP TABLE IF EXISTS `conversations`;
CREATE TABLE `conversations`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `user1_id` bigint NOT NULL COMMENT '用户1的ID（较小的ID）',
  `user2_id` bigint NOT NULL COMMENT '用户2的ID（较大的ID）',
  `last_message_id` bigint NULL DEFAULT NULL COMMENT '最后一条消息ID',
  `last_message_content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后一条消息内容',
  `last_message_time` datetime NULL DEFAULT NULL COMMENT '最后一条消息时间',
  `user1_unread_count` int NULL DEFAULT 0 COMMENT '用户1的未读消息数',
  `user2_unread_count` int NULL DEFAULT 0 COMMENT '用户2的未读消息数',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_users`(`user1_id` ASC, `user2_id` ASC) USING BTREE,
  INDEX `idx_user1`(`user1_id` ASC) USING BTREE,
  INDEX `idx_user2`(`user2_id` ASC) USING BTREE,
  INDEX `idx_updated_at`(`updated_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '会话表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of conversations
-- ----------------------------

-- ----------------------------
-- Table structure for coupon_search_logs
-- ----------------------------
DROP TABLE IF EXISTS `coupon_search_logs`;
CREATE TABLE `coupon_search_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID（未登录为NULL）',
  `keyword` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '搜索关键词',
  `result_count` int NOT NULL DEFAULT 0 COMMENT '搜索结果数量',
  `search_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '搜索时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_keyword`(`keyword` ASC) USING BTREE,
  INDEX `idx_search_time`(`search_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '优惠券搜索记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of coupon_search_logs
-- ----------------------------

-- ----------------------------
-- Table structure for coupon_statistics
-- ----------------------------
DROP TABLE IF EXISTS `coupon_statistics`;
CREATE TABLE `coupon_statistics`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `coupon_id` bigint NOT NULL COMMENT '优惠券ID',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `view_count` int NOT NULL DEFAULT 0 COMMENT '浏览次数',
  `view_user_count` int NOT NULL DEFAULT 0 COMMENT '浏览用户数',
  `claim_count` int NOT NULL DEFAULT 0 COMMENT '领取次数',
  `claim_user_count` int NOT NULL DEFAULT 0 COMMENT '领取用户数',
  `use_count` int NOT NULL DEFAULT 0 COMMENT '使用次数',
  `use_user_count` int NOT NULL DEFAULT 0 COMMENT '使用用户数',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_coupon_date`(`coupon_id` ASC, `stat_date` ASC) USING BTREE,
  INDEX `idx_stat_date`(`stat_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '优惠券统计表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of coupon_statistics
-- ----------------------------

-- ----------------------------
-- Table structure for coupon_view_logs
-- ----------------------------
DROP TABLE IF EXISTS `coupon_view_logs`;
CREATE TABLE `coupon_view_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `coupon_id` bigint NOT NULL COMMENT '优惠券ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID（未登录为NULL）',
  `device_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备ID',
  `source` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '来源（center/detail/share）',
  `view_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_coupon_id`(`coupon_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_view_time`(`view_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '优惠券浏览记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of coupon_view_logs
-- ----------------------------

-- ----------------------------
-- Table structure for coupons
-- ----------------------------
DROP TABLE IF EXISTS `coupons`;
CREATE TABLE `coupons`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '优惠券ID',
  `merchant_id` bigint NOT NULL COMMENT '所属商家ID',
  `shop_id` bigint NULL DEFAULT NULL COMMENT '适用店铺ID（NULL表示全部店铺）',
  `type` tinyint NOT NULL COMMENT '类型（1满减券，2折扣券，3代金券）',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '优惠券标题',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '使用说明',
  `amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '优惠金额（满减券、代金券）',
  `discount` decimal(5, 2) NULL DEFAULT NULL COMMENT '折扣（折扣券，如0.8表示8折）',
  `min_amount` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '最低消费金额',
  `total_count` int NOT NULL COMMENT '发行总量',
  `remain_count` int NOT NULL COMMENT '剩余数量',
  `per_user_limit` int NOT NULL DEFAULT 1 COMMENT '每人限领数量',
  `start_time` datetime NOT NULL COMMENT '有效期开始时间',
  `end_time` datetime NOT NULL COMMENT '有效期结束时间',
  `stackable` tinyint NOT NULL DEFAULT 0 COMMENT '是否可叠加使用（0否，1是）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（0下架，1上架）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_merchant_id`(`merchant_id` ASC) USING BTREE,
  INDEX `idx_shop_id`(`shop_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_start_time`(`start_time` ASC) USING BTREE,
  INDEX `idx_end_time`(`end_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '优惠券表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of coupons
-- ----------------------------
INSERT INTO `coupons` VALUES (1, 8, 6, 3, '200元代金券', '200元代金券,快来抢吧', 200.00, NULL, 0.00, 100, 99, 1, '2025-12-22 00:00:00', '2025-12-25 00:00:00', 1, 1, '2025-12-22 11:07:52', '2025-12-22 15:07:40');
INSERT INTO `coupons` VALUES (2, 8, 6, 2, '天生祥购物八折券', '天生祥购物八折券,快来看看吧', NULL, 0.80, 200.00, 50, 49, 1, '2025-12-21 00:00:00', '2026-01-01 00:00:00', 0, 1, '2025-12-22 11:25:29', '2025-12-22 11:25:29');
INSERT INTO `coupons` VALUES (3, 3, 5, 3, '100元代金券', '海底捞火锅100元代金券', 200.00, NULL, 0.00, 100, 99, 1, '2025-12-24 00:00:00', '2025-12-26 00:00:00', 1, 1, '2025-12-24 15:49:17', '2025-12-24 15:49:17');

-- ----------------------------
-- Table structure for merchant_users
-- ----------------------------
DROP TABLE IF EXISTS `merchant_users`;
CREATE TABLE `merchant_users`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `merchant_id` bigint NOT NULL COMMENT '所属商家ID',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（加密）',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '姓名',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像',
  `role_id` bigint NULL DEFAULT NULL COMMENT '角色ID',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1正常，2禁用）',
  `last_login_at` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_merchant_id`(`merchant_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商家用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of merchant_users
-- ----------------------------
INSERT INTO `merchant_users` VALUES (1, 1, '16750152199', NULL, 'cmj123456', '管理员', NULL, NULL, 1, '2025-12-15 10:25:26', '2025-12-14 19:08:25', '2025-12-14 19:08:25');
INSERT INTO `merchant_users` VALUES (2, 2, '18354763214', NULL, '123456', '管理员', NULL, NULL, 1, '2025-12-14 22:01:08', '2025-12-14 22:01:07', '2025-12-14 22:01:07');
INSERT INTO `merchant_users` VALUES (3, 3, '15216091650', NULL, '123456', '管理员', NULL, NULL, 1, '2025-12-18 19:53:25', '2025-12-16 19:01:17', '2025-12-16 19:01:17');

-- ----------------------------
-- Table structure for merchants
-- ----------------------------
DROP TABLE IF EXISTS `merchants`;
CREATE TABLE `merchants`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商家ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商家名称/企业名称',
  `merchant_owner_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '商家负责人姓名',
  `logo` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '商家Logo',
  `license_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '营业执照号',
  `license_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '营业执照图片',
  `contact_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系人姓名',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系电话',
  `contact_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系邮箱',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '登录密码（加密）',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '商家头像',
  `role_id` bigint NULL DEFAULT NULL COMMENT '角色ID',
  `last_login_at` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1正常，2禁用，3待审核）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_contact_phone`(`contact_phone` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_contact_phone`(`contact_phone` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商家表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of merchants
-- ----------------------------
INSERT INTO `merchants` VALUES (1, '小品烧烤', NULL, NULL, NULL, NULL, NULL, '16750152199', NULL, 'cmj123456', NULL, NULL, '2025-12-22 16:34:08', 1, '2025-12-14 19:08:25', '2025-12-18 20:22:11');
INSERT INTO `merchants` VALUES (2, '张亮麻辣烫', NULL, NULL, NULL, NULL, NULL, '18354763214', NULL, '123456', NULL, NULL, '2025-12-18 23:23:33', 1, '2025-12-14 22:01:07', '2025-12-18 20:22:11');
INSERT INTO `merchants` VALUES (3, '蜜雪冰城', NULL, NULL, NULL, NULL, NULL, '15216091650', NULL, '123456', NULL, NULL, '2025-12-24 15:51:40', 1, '2025-12-16 19:01:17', '2025-12-18 20:22:11');
INSERT INTO `merchants` VALUES (8, '天生祥', NULL, 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/logo/2025/12/18/c2c77baa7be446f6911672ba2373fc41.png', NULL, 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/license/2025/12/18/622d6b843a02481eb3ee345257b2f926.png', '张飞', '18987934526', '3406685262@qq.com', '123456', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/avatar/2025/12/18/663481a49ee04f64926fa26f457047ed.png', NULL, '2025-12-27 22:42:58', 1, '2025-12-18 21:11:49', '2025-12-18 21:11:49');
INSERT INTO `merchants` VALUES (9, '爱你羊肉', NULL, 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/logo/2025/12/25/15bd258911184284a11e040a9def83b8.png', NULL, 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/license/2025/12/25/0cf9dd5a07164c608cdc4bef85182f27.png', '张翼德', '18379634597', '3406685262@qq.com', '123456', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/avatar/2025/12/25/235cc26f38ed4ec49ecc093ed1301d00.png', NULL, '2025-12-28 11:53:01', 1, '2025-12-25 20:01:26', '2025-12-25 20:01:26');
INSERT INTO `merchants` VALUES (10, '南阳大师傅面包店', '程明杰', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/logo/2025/12/28/999d28a6fdb14acfb7f658c083294ad5.png', '123456', '', NULL, '19879634521', 'chengmingjie925@gmail.com', 'OsNJxwxvY32T4VXwrwjVSOrf5gYHgb2l5CxjOJhRsZV07GrS1vHsOVaYqE6OfCBw', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/avatar/2025/12/28/5e2a2015198e45b3ab2b2fd11ecc21d4.png', NULL, '2025-12-29 10:51:39', 1, '2025-12-28 13:03:53', '2025-12-28 15:41:33');

-- ----------------------------
-- Table structure for messages
-- ----------------------------
DROP TABLE IF EXISTS `messages`;
CREATE TABLE `messages`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `receiver_id` bigint NOT NULL COMMENT '接收者ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
  `type` int NULL DEFAULT 1 COMMENT '消息类型：1=文本，2=图片，3=语音，4=笔记分享',
  `note_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '笔记数据（JSON格式，用于笔记分享消息）',
  `is_read` tinyint NULL DEFAULT 0 COMMENT '是否已读：0=未读，1=已读',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sender`(`sender_id` ASC) USING BTREE,
  INDEX `idx_receiver`(`receiver_id` ASC) USING BTREE,
  INDEX `idx_sender_receiver`(`sender_id` ASC, `receiver_id` ASC) USING BTREE,
  INDEX `idx_receiver_sender`(`receiver_id` ASC, `sender_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 55 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '私信消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of messages
-- ----------------------------
INSERT INTO `messages` VALUES (1, 22, 27, '是', 1, NULL, 0, '2025-12-14 23:28:17');
INSERT INTO `messages` VALUES (2, 22, 27, '你好', 1, NULL, 0, '2025-12-14 23:28:30');
INSERT INTO `messages` VALUES (3, 27, 22, '你好', 1, NULL, 1, '2025-12-14 23:29:54');
INSERT INTO `messages` VALUES (4, 22, 27, '你好', 1, NULL, 0, '2025-12-14 23:30:33');
INSERT INTO `messages` VALUES (5, 27, 22, '你好', 1, NULL, 1, '2025-12-14 23:30:42');
INSERT INTO `messages` VALUES (6, 22, 27, '你好', 1, NULL, 0, '2025-12-14 23:30:47');
INSERT INTO `messages` VALUES (7, 22, 27, '你好', 1, NULL, 0, '2025-12-14 23:37:38');
INSERT INTO `messages` VALUES (8, 27, 22, '你好', 1, NULL, 1, '2025-12-14 23:37:49');
INSERT INTO `messages` VALUES (9, 22, 27, '你喜欢吃什么', 1, NULL, 0, '2025-12-14 23:37:59');
INSERT INTO `messages` VALUES (10, 22, 27, '我喜欢吃鱼', 1, NULL, 0, '2025-12-14 23:44:52');
INSERT INTO `messages` VALUES (11, 24, 22, '你好', 1, NULL, 1, '2025-12-15 10:06:45');
INSERT INTO `messages` VALUES (12, 22, 24, '你好呀', 1, NULL, 1, '2025-12-15 10:06:58');
INSERT INTO `messages` VALUES (13, 24, 22, '你叫什么名字', 1, NULL, 1, '2025-12-15 10:07:21');
INSERT INTO `messages` VALUES (14, 22, 24, '我是小松鼠', 1, NULL, 1, '2025-12-15 10:31:08');
INSERT INTO `messages` VALUES (15, 22, 24, '你呢', 1, NULL, 1, '2025-12-15 10:31:12');
INSERT INTO `messages` VALUES (16, 24, 22, '我是小兔子', 1, NULL, 1, '2025-12-15 10:31:24');
INSERT INTO `messages` VALUES (17, 24, 22, '1', 1, NULL, 1, '2025-12-15 10:37:41');
INSERT INTO `messages` VALUES (18, 22, 24, '1', 1, NULL, 1, '2025-12-15 10:40:47');
INSERT INTO `messages` VALUES (19, 24, 22, '1', 1, NULL, 1, '2025-12-15 10:45:42');
INSERT INTO `messages` VALUES (20, 22, 24, 'nihao', 1, NULL, 1, '2025-12-15 10:46:10');
INSERT INTO `messages` VALUES (21, 24, 22, '1', 1, NULL, 1, '2025-12-15 10:46:32');
INSERT INTO `messages` VALUES (22, 24, 22, '1', 1, NULL, 1, '2025-12-15 10:50:13');
INSERT INTO `messages` VALUES (23, 22, 24, '1', 1, NULL, 1, '2025-12-15 10:50:18');
INSERT INTO `messages` VALUES (24, 24, 22, '你好小松鼠', 1, NULL, 1, '2025-12-15 10:50:46');
INSERT INTO `messages` VALUES (25, 22, 24, '你在干嘛呀', 1, NULL, 1, '2025-12-15 14:56:39');
INSERT INTO `messages` VALUES (26, 24, 22, '在打游戏', 1, NULL, 1, '2025-12-15 14:56:47');
INSERT INTO `messages` VALUES (27, 22, 24, '你有没有吃到什么好吃的,可以推荐一下给我吗', 1, NULL, 1, '2025-12-15 14:57:09');
INSERT INTO `messages` VALUES (28, 22, 24, '看见没', 1, NULL, 1, '2025-12-15 16:43:33');
INSERT INTO `messages` VALUES (29, 24, 22, '看见咯', 1, NULL, 1, '2025-12-15 16:43:44');
INSERT INTO `messages` VALUES (30, 22, 24, '有没有什么想吃的', 1, NULL, 1, '2025-12-15 16:43:57');
INSERT INTO `messages` VALUES (31, 24, 22, '在吗', 1, NULL, 1, '2025-12-17 13:13:57');
INSERT INTO `messages` VALUES (32, 22, 24, '在的', 1, NULL, 1, '2025-12-17 13:14:03');
INSERT INTO `messages` VALUES (33, 24, 22, '在不', 1, NULL, 1, '2025-12-20 11:52:53');
INSERT INTO `messages` VALUES (34, 22, 24, '在的', 1, NULL, 1, '2025-12-20 11:53:09');
INSERT INTO `messages` VALUES (35, 24, 22, '你在干什么呢', 1, NULL, 1, '2025-12-20 11:53:18');
INSERT INTO `messages` VALUES (36, 22, 24, '我吗', 1, NULL, 1, '2025-12-20 11:53:32');
INSERT INTO `messages` VALUES (37, 24, 22, '对呀', 1, NULL, 1, '2025-12-20 11:54:00');
INSERT INTO `messages` VALUES (38, 22, 24, '吃饭了没', 1, NULL, 1, '2025-12-20 11:58:00');
INSERT INTO `messages` VALUES (39, 24, 22, '吃了呀', 1, NULL, 1, '2025-12-20 11:58:08');
INSERT INTO `messages` VALUES (40, 24, 22, '在干嘛', 1, NULL, 1, '2025-12-20 12:16:30');
INSERT INTO `messages` VALUES (41, 24, 22, '在打cf', 1, NULL, 1, '2025-12-20 12:16:51');
INSERT INTO `messages` VALUES (42, 22, 24, '在干嘛', 1, NULL, 1, '2025-12-20 12:26:56');
INSERT INTO `messages` VALUES (43, 24, 22, '在吃饭2', 1, NULL, 1, '2025-12-20 12:27:02');
INSERT INTO `messages` VALUES (44, 42, 22, '分享了一篇笔记', 4, '{\"noteId\":24,\"title\":\"天生祥招人啦\",\"coverImage\":\"https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/18/bd814ecc9b0d403880099fe58ade567f.png\",\"content\":\"在景东的小伙伴注意啦\n景东服务岗\n天生祥超市收银员\n袁老四服务员（包吃住）\n瑞幸咖啡（店员2名）\n职\"}', 1, '2025-12-25 23:11:34');
INSERT INTO `messages` VALUES (45, 42, 22, '分享了一篇笔记', 4, '{\"noteId\":24,\"title\":\"天生祥招人啦\",\"coverImage\":\"https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/18/bd814ecc9b0d403880099fe58ade567f.png\",\"content\":\"在景东的小伙伴注意啦\n景东服务岗\n天生祥超市收银员\n袁老四服务员（包吃住）\n瑞幸咖啡（店员2名）\n职\"}', 1, '2025-12-25 23:16:47');
INSERT INTO `messages` VALUES (46, 42, 22, '分享了一篇笔记', 4, '{\"coverImage\":\"https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/6f15b04f8efa4fa0a5f77271d33a9c47.png\",\"noteId\":6,\"title\":\"可爱的小猪头像\",\"content\":\"非常可爱的小猪头像,很值得相信的一次旅行\"}', 1, '2025-12-25 23:22:10');
INSERT INTO `messages` VALUES (47, 22, 42, '这是什么', 1, NULL, 0, '2025-12-26 10:04:58');
INSERT INTO `messages` VALUES (48, 22, 42, '分享了一篇笔记', 4, '{\"coverImage\":\"https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/18/bd814ecc9b0d403880099fe58ade567f.png\",\"noteId\":24,\"title\":\"天生祥招人啦\",\"content\":\"在景东的小伙伴注意啦\\n景东服务岗\\n天生祥超市收银员\\n袁老四服务员（包吃住）\\n瑞幸咖啡（店员2名）\\n职\"}', 0, '2025-12-26 10:05:20');
INSERT INTO `messages` VALUES (49, 22, 24, '分享了一篇笔记', 4, '{\"coverImage\":\"https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/18/bd814ecc9b0d403880099fe58ade567f.png\",\"noteId\":24,\"title\":\"天生祥招人啦\",\"content\":\"在景东的小伙伴注意啦\\n景东服务岗\\n天生祥超市收银员\\n袁老四服务员（包吃住）\\n瑞幸咖啡（店员2名）\\n职\"}', 0, '2025-12-26 10:05:20');
INSERT INTO `messages` VALUES (50, 22, 8, '分享了一篇笔记', 4, '{\"coverImage\":\"https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/18/bd814ecc9b0d403880099fe58ade567f.png\",\"noteId\":24,\"title\":\"天生祥招人啦\",\"content\":\"在景东的小伙伴注意啦\\n景东服务岗\\n天生祥超市收银员\\n袁老四服务员（包吃住）\\n瑞幸咖啡（店员2名）\\n职\"}', 0, '2025-12-26 10:20:28');
INSERT INTO `messages` VALUES (51, 22, 3, '分享了一篇笔记', 4, '{\"coverImage\":\"https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/18/bd814ecc9b0d403880099fe58ade567f.png\",\"noteId\":24,\"title\":\"天生祥招人啦\",\"content\":\"在景东的小伙伴注意啦\\n景东服务岗\\n天生祥超市收银员\\n袁老四服务员（包吃住）\\n瑞幸咖啡（店员2名）\\n职\"}', 0, '2025-12-26 10:20:28');
INSERT INTO `messages` VALUES (52, 22, 3, '分享了一篇笔记', 4, '{\"coverImage\":\"https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/05/3988b2b969ad4805a794f6b8cfffe1bb.png\",\"noteId\":9,\"title\":\"很美好的一次旅程\",\"content\":\"今天看到一个非常好看的头像很想分享给大家一起来观看这个头像\"}', 0, '2025-12-26 10:28:29');
INSERT INTO `messages` VALUES (53, 22, 3, '分享了一篇笔记', 4, '{\"coverImage\":\"https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/66e7fc5bc0e14762ac21f9b6cc8f5d7a.png\",\"noteId\":4,\"title\":\"小猫咪\",\"content\":\"可爱的小猫咪,值得大家相信\"}', 0, '2025-12-26 10:28:36');
INSERT INTO `messages` VALUES (54, 22, 24, '分享了一篇笔记', 4, '{\"coverImage\":\"https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/18/bd814ecc9b0d403880099fe58ade567f.png\",\"noteId\":24,\"title\":\"天生祥招人啦\",\"content\":\"在景东的小伙伴注意啦\\n景东服务岗\\n天生祥超市收银员\\n袁老四服务员（包吃住）\\n瑞幸咖啡（店员2名）\\n职\"}', 0, '2025-12-26 10:31:58');

-- ----------------------------
-- Table structure for note_comments
-- ----------------------------
DROP TABLE IF EXISTS `note_comments`;
CREATE TABLE `note_comments`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `note_id` bigint NOT NULL COMMENT '笔记ID',
  `user_id` bigint NOT NULL COMMENT '评论用户ID',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父评论ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论内容',
  `like_count` int NOT NULL DEFAULT 0 COMMENT '点赞数',
  `reply_count` int NOT NULL DEFAULT 0 COMMENT '回复数量',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1正常，2隐藏）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_note_id`(`note_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 26 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '笔记评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of note_comments
-- ----------------------------
INSERT INTO `note_comments` VALUES (2, 6, 22, NULL, '好可爱的小猪', 0, 0, 1, '2025-12-05 13:39:07', '2025-12-05 13:39:07');
INSERT INTO `note_comments` VALUES (3, 8, 22, NULL, '好可爱的小猪', 0, 0, 1, '2025-12-05 13:39:37', '2025-12-05 13:39:37');
INSERT INTO `note_comments` VALUES (4, 8, 22, NULL, '小猪真有意思', 0, 0, 1, '2025-12-05 14:11:16', '2025-12-05 14:11:16');
INSERT INTO `note_comments` VALUES (5, 2, 22, NULL, '好可爱的小熊', 0, 0, 1, '2025-12-05 14:34:29', '2025-12-05 14:34:29');
INSERT INTO `note_comments` VALUES (6, 6, 32, NULL, '很有意思的小猪', 0, 0, 1, '2025-12-05 15:43:30', '2025-12-05 15:43:30');
INSERT INTO `note_comments` VALUES (7, 9, 32, NULL, '赞成', 0, 0, 1, '2025-12-05 16:11:21', '2025-12-05 16:11:21');
INSERT INTO `note_comments` VALUES (8, 4, 33, NULL, '好酷的小猫咪', 0, 0, 1, '2025-12-05 18:28:06', '2025-12-05 18:28:06');
INSERT INTO `note_comments` VALUES (9, 1, 33, NULL, '这一组头像都好好看', 0, 0, 1, '2025-12-05 18:29:24', '2025-12-05 18:29:24');
INSERT INTO `note_comments` VALUES (10, 11, 22, NULL, '非常可爱', 0, 0, 1, '2025-12-05 18:37:28', '2025-12-05 18:37:28');
INSERT INTO `note_comments` VALUES (11, 12, 24, NULL, '确实非常可爱', 0, 0, 1, '2025-12-15 10:29:41', '2025-12-15 10:29:41');
INSERT INTO `note_comments` VALUES (12, 10, 24, NULL, '111', 0, 0, 1, '2025-12-17 21:55:00', '2025-12-17 21:55:00');
INSERT INTO `note_comments` VALUES (13, 21, 22, NULL, '真的好好吃的海底捞火锅', 0, 0, 1, '2025-12-18 17:40:06', '2025-12-18 17:40:06');
INSERT INTO `note_comments` VALUES (14, 22, 22, NULL, '感觉好好的海底捞火锅', 0, 0, 1, '2025-12-18 18:13:48', '2025-12-18 18:13:48');
INSERT INTO `note_comments` VALUES (15, 22, 3, NULL, '很好', 0, 0, 1, '2025-12-18 18:17:28', '2025-12-18 18:17:28');
INSERT INTO `note_comments` VALUES (16, 22, 3, NULL, '1', 0, 0, 1, '2025-12-18 18:17:41', '2025-12-18 18:17:41');
INSERT INTO `note_comments` VALUES (17, 22, 3, NULL, '海底捞还是很不错的', 0, 0, 1, '2025-12-18 18:49:17', '2025-12-18 18:49:17');
INSERT INTO `note_comments` VALUES (18, 24, 8, NULL, '我要去', 0, 0, 1, '2025-12-18 21:17:34', '2025-12-18 21:17:34');
INSERT INTO `note_comments` VALUES (19, 25, 8, NULL, '天生祥非常值得大家前来购物', 0, 0, 1, '2025-12-19 21:02:48', '2025-12-19 21:02:48');
INSERT INTO `note_comments` VALUES (20, 26, 22, NULL, '111', 0, 0, 1, '2025-12-20 12:04:01', '2025-12-20 12:04:01');
INSERT INTO `note_comments` VALUES (21, 27, 1, NULL, '一起看看', 0, 0, 1, '2025-12-20 21:39:29', '2025-12-20 21:39:29');
INSERT INTO `note_comments` VALUES (22, 27, 1, 21, '很值得去看看', 0, 0, 1, '2025-12-21 13:03:03', '2025-12-21 13:03:03');
INSERT INTO `note_comments` VALUES (23, 29, 42, NULL, '欢迎欢迎呀', 0, 0, 1, '2025-12-25 20:29:46', '2025-12-25 20:29:46');
INSERT INTO `note_comments` VALUES (24, 29, 42, NULL, '新店开业啦,欢迎大家', 0, 0, 1, '2025-12-25 20:30:06', '2025-12-25 20:30:06');
INSERT INTO `note_comments` VALUES (25, 30, 10, NULL, '走过路过不要错过,新用户特别划算的哦', 0, 0, 1, '2025-12-28 13:07:34', '2025-12-28 13:07:34');

-- ----------------------------
-- Table structure for note_tags
-- ----------------------------
DROP TABLE IF EXISTS `note_tags`;
CREATE TABLE `note_tags`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `note_id` bigint NOT NULL COMMENT '笔记ID',
  `tag_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签名称',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_note_id`(`note_id` ASC) USING BTREE,
  INDEX `idx_tag_name`(`tag_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '笔记标签表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of note_tags
-- ----------------------------

-- ----------------------------
-- Table structure for note_topics
-- ----------------------------
DROP TABLE IF EXISTS `note_topics`;
CREATE TABLE `note_topics`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `note_id` bigint NOT NULL COMMENT '笔记ID',
  `topic_id` bigint NOT NULL COMMENT '话题ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_note_topic`(`note_id` ASC, `topic_id` ASC) USING BTREE,
  INDEX `idx_topic_id`(`topic_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '笔记话题关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of note_topics
-- ----------------------------
INSERT INTO `note_topics` VALUES (2, 29, 5, '2025-12-25 22:25:55');
INSERT INTO `note_topics` VALUES (3, 29, 21, '2025-12-25 22:25:55');
INSERT INTO `note_topics` VALUES (4, 28, 22, '2025-12-25 23:23:51');
INSERT INTO `note_topics` VALUES (5, 30, 5, '2025-12-28 13:06:50');

-- ----------------------------
-- Table structure for notes
-- ----------------------------
DROP TABLE IF EXISTS `notes`;
CREATE TABLE `notes`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '笔记ID',
  `user_id` bigint NOT NULL COMMENT '作者用户ID',
  `note_type` tinyint NOT NULL DEFAULT 1 COMMENT '笔记类型（1用户笔记，2商家笔记）',
  `merchant_id` bigint NULL DEFAULT NULL COMMENT '商家ID（商家笔记专用）',
  `shop_id` bigint NULL DEFAULT NULL COMMENT '关联商家ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '笔记标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '笔记内容',
  `cover_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面图',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '图片集合（JSON）',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '位置信息',
  `latitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '纬度',
  `longitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '经度',
  `like_count` int NOT NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` int NOT NULL DEFAULT 0 COMMENT '评论数',
  `view_count` int NOT NULL DEFAULT 0 COMMENT '浏览数',
  `favorite_count` int NOT NULL DEFAULT 0 COMMENT '收藏数',
  `share_count` int NOT NULL DEFAULT 0 COMMENT '分享数',
  `tag_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签类型（hot/discount/new）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1正常，2隐藏，3审核中）',
  `is_recommend` tinyint NOT NULL DEFAULT 0 COMMENT '是否推荐（0否，1是）',
  `sync_status` tinyint NOT NULL DEFAULT 1 COMMENT '同步状态（0未同步，1已同步）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_shop_id`(`shop_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_is_recommend`(`is_recommend` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_like_count`(`like_count` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '笔记表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notes
-- ----------------------------
INSERT INTO `notes` VALUES (1, 22, 1, NULL, NULL, '可爱头像', '可爱的头像,非常好的一次旅行', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/744ce88af0f046c281268fa29aa387e6.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/744ce88af0f046c281268fa29aa387e6.png,https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/5558e3b38eaf44dc9e4701e229dedb1b.png,https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/d41f3e7fffa345e58d86723c7f529bad.png,https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/cba00da586054dc1bff418062ed91066.png,https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/cf2fbfa47286472aa3c458117371463b.png,https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/72ce2a8b89864dc490dec4607d8776b5.png', NULL, NULL, NULL, 0, 1, 41, 1, 0, NULL, 1, 0, 1, '2025-12-04 23:01:51', '2025-12-26 22:53:14');
INSERT INTO `notes` VALUES (2, 22, 1, NULL, NULL, '小熊', '非常可爱的小熊,值得大家拥有', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/eedcf9db924d4d4d8724aa3ae551cad4.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/eedcf9db924d4d4d8724aa3ae551cad4.png', NULL, NULL, NULL, 1, 1, 19, 1, 0, NULL, 1, 0, 1, '2025-12-04 23:08:44', '2025-12-20 12:27:36');
INSERT INTO `notes` VALUES (3, 27, 1, NULL, NULL, '可爱小狗头像', '非常可爱的小狗值得大家拥有', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/17543e07356c43f8aa33b952c4263a6c.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/17543e07356c43f8aa33b952c4263a6c.png', NULL, NULL, NULL, 1, 0, 16, 0, 0, NULL, 1, 0, 1, '2025-12-04 23:12:38', '2025-12-26 22:53:25');
INSERT INTO `notes` VALUES (4, 28, 1, NULL, NULL, '小猫咪', '可爱的小猫咪,值得大家相信', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/66e7fc5bc0e14762ac21f9b6cc8f5d7a.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/66e7fc5bc0e14762ac21f9b6cc8f5d7a.png', NULL, NULL, NULL, 3, 1, 84, 3, 0, NULL, 1, 0, 1, '2025-12-04 23:23:01', '2025-12-26 22:58:41');
INSERT INTO `notes` VALUES (5, 29, 1, NULL, NULL, '可爱小狗', '可爱的小狗值得大家相信', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/6e409a8067fb4500b6398fab6cd36ee0.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/6e409a8067fb4500b6398fab6cd36ee0.png', NULL, NULL, NULL, 2, 0, 19, 1, 0, NULL, 1, 0, 1, '2025-12-04 23:24:54', '2025-12-21 15:24:07');
INSERT INTO `notes` VALUES (6, 29, 1, NULL, NULL, '可爱的小猪头像', '非常可爱的小猪头像,很值得相信的一次旅行', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/6f15b04f8efa4fa0a5f77271d33a9c47.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/6f15b04f8efa4fa0a5f77271d33a9c47.png', NULL, NULL, NULL, 2, 2, 85, 2, 0, NULL, 1, 0, 1, '2025-12-04 23:42:40', '2025-12-26 22:58:48');
INSERT INTO `notes` VALUES (7, 27, 1, NULL, NULL, '你好我高兴', '非常真实的一次体验值得大家相信', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/05/8f1050bbab1f4685921694575e0198d6.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/05/8f1050bbab1f4685921694575e0198d6.png', NULL, NULL, NULL, 0, 0, 17, 0, 0, NULL, 1, 0, 1, '2025-12-05 00:16:39', '2025-12-26 22:58:34');
INSERT INTO `notes` VALUES (8, 22, 1, NULL, NULL, '我喜欢吃肉', '这家四川味道的肉真的很好吃,欢迎大家前来品尝', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/05/7f9ec7fea6fc4a4b9e6627cadc6c043e.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/05/7f9ec7fea6fc4a4b9e6627cadc6c043e.png', NULL, NULL, NULL, 0, 2, 31, 0, 0, NULL, 1, 0, 1, '2025-12-05 12:11:02', '2025-12-26 22:58:50');
INSERT INTO `notes` VALUES (9, 32, 1, NULL, NULL, '很美好的一次旅程', '今天看到一个非常好看的头像很想分享给大家一起来观看这个头像', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/05/3988b2b969ad4805a794f6b8cfffe1bb.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/05/3988b2b969ad4805a794f6b8cfffe1bb.png', NULL, NULL, NULL, 2, 1, 14, 1, 0, NULL, 1, 0, 1, '2025-12-05 15:47:33', '2025-12-26 10:28:19');
INSERT INTO `notes` VALUES (10, 22, 1, NULL, NULL, '很好', '非常好的一次背景,值得大家换上', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/05/58310d4b04b844bea24a35d9c70b2f48.jpg', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/05/58310d4b04b844bea24a35d9c70b2f48.jpg', NULL, NULL, NULL, 4, 1, 42, 2, 0, NULL, 1, 0, 1, '2025-12-05 16:17:26', '2025-12-27 12:15:53');
INSERT INTO `notes` VALUES (11, 22, 1, NULL, NULL, '可爱的小熊', '非常可爱的小熊,值得你拥有', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/05/34d40cd5a8e647d8addc654247983124.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/05/34d40cd5a8e647d8addc654247983124.png', NULL, NULL, NULL, 0, 1, 5, 0, 0, NULL, 1, 0, 1, '2025-12-05 18:37:15', '2025-12-15 15:26:48');
INSERT INTO `notes` VALUES (12, 24, 1, NULL, NULL, '非常可爱的小动物', '非常可爱的小动物,大家可以一起来看看', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/12/eecb59b646a64b8aa9b92ff0ee5e4ae5.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/12/eecb59b646a64b8aa9b92ff0ee5e4ae5.png,https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/12/4eebef401bcb48edbf6220b6a2150dcf.png', NULL, NULL, NULL, 0, 1, 18, 0, 0, NULL, 1, 0, 1, '2025-12-12 18:58:37', '2025-12-26 22:53:38');
INSERT INTO `notes` VALUES (17, 24, 1, NULL, NULL, '可爱的小动物头像', '非常可爱的小动物头像', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/14/c797b754ac7c464ca42cf50891f73810.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/14/c797b754ac7c464ca42cf50891f73810.png,https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/14/c73ab287fb82476689b7c45ef9e4aa11.png,https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/14/6e1d2855a3034d99af0ff49a6b24002c.png,https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/14/6392f5f3d29d4a4da7c3cacc3818afd2.png,https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/14/291f544bddd84503a925d1bab5f2c41d.png,https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/14/2f57372657744ec88a5fabb0a94abdb8.png,https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/14/ad8c102c6b464c9bae356b61f2aefec1.png,https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/14/ffaa7b1cd33c4759bf6cd09cbdfae333.png,https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/14/3a4059027b3b4406ba21c2df72e2088e.png', NULL, NULL, NULL, 0, 0, 0, 0, 0, NULL, 2, 0, 1, '2025-12-14 22:28:44', '2025-12-14 22:28:44');
INSERT INTO `notes` VALUES (18, 24, 1, NULL, NULL, '可爱的小狗', '非常可爱的小狗,快来一起看看', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/14/c55b325858484a839482c4f70395f1e5.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/14/c55b325858484a839482c4f70395f1e5.png', NULL, NULL, NULL, 0, 0, 6, 0, 0, NULL, 1, 0, 1, '2025-12-14 22:30:23', '2025-12-26 22:58:31');
INSERT INTO `notes` VALUES (19, 22, 1, NULL, NULL, '可爱的小兔子', '非常可爱的小兔子,一起来看看', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/15/4a30e307fa444f398dd6950f15d84cec.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/15/4a30e307fa444f398dd6950f15d84cec.png', '魔方集市中通快递', 22.7694250, 100.9992750, 0, 0, 11, 0, 0, NULL, 1, 0, 1, '2025-12-15 15:26:23', '2025-12-26 22:58:43');
INSERT INTO `notes` VALUES (21, 3, 2, 3, 5, '很好吃的海底捞火锅', '非常好吃的海底捞火锅,希望大家喜欢', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/18/d00933cc599f40a980b1e96af986baf9.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/18/d00933cc599f40a980b1e96af986baf9.png', '普洱学院西区食堂', 22.7726950, 100.9979380, 0, 1, 17, 0, 0, NULL, 1, 0, 1, '2025-12-18 17:37:37', '2025-12-21 15:24:22');
INSERT INTO `notes` VALUES (22, 3, 2, 3, 5, '品牌案例｜海底捞，川味火锅跨省直营品牌', '品牌案例｜海底捞，川味火锅跨省直营品牌\n👋🏻相信大家对海底捞再熟悉不过啦，但海底捞是如何经营品牌？可能就没有太系统的了解\n今天安妮带你一分钟走进海底捞品牌😎\n.\n【品牌策略】\n1️⃣品牌定位\n✅核心定位：服务最好的火锅店，以“极致服务体验\"为核心竞争力\n✅品牌口号：2024年升级为「一吃海底捞，马上没烦恼」	强调情绪价值与愉悦感\n✅品牌个性：热情、可靠、有温度\n.\n2⃣️品牌形象与识别\n✅视觉识别：标志性的红色主视觉，传递热情、活力与食欲\n✅超级角色： 推出“小捞捞”作为品牌全球代言人，结合熊猫与川剧变脸元素，成为品牌文化符号\n（川剧变脸➕红黄帽子➕红斗篷➕手持变脸折扇）\n✅品牌歌曲：《哈哈歌》，让“快乐”与“海底捞”不断产生母体关联\n.\n3⃣️品牌传播与文化自信\n🔥文化输出：小捞捞作为“首席外交官”，将拉面、变脸、吐火等传统技艺传播至全球\n🔥话语体系：坚持中国文化自信，推广“Huǒguō”一词，强化品牌文化标签\n.\n【产品策略	】\n1️⃣核心产品：安全、标准化的火锅体验\n✅食品安全：建立透明厨房与蜀海供应链体系，确保食材安全可追溯\n.\n2⃣️增值产品：围绕体验的无限延伸\n✅等位产品：免费美甲、擦鞋、小吃、棋牌等\n✅用餐辅助产品：围裙、手机防水袋、皮筋、眼镜布等\n✅娱乐化产品：捞面表演、生日歌、变脸等，增强用餐仪式感与记忆点\n.\n3⃣️创新与跨界融合\n✅智慧餐厅：引入机械臂、传菜机器人等科技元素，提升效率与体验感地域化锅底创新:如贵州雷山酸汤锅、浓浓浓菌汤锅、猪肚鸡锅等\n✅场景拓展：推出露营火锅、火锅音乐节等跨界场景，打破传统餐饮边界\n.\n【营销策略	】\n1️⃣IP联名与话题营销\n◾春季联名：与樱桃小丸子合作，推出春季新品与周边，结合“翻卡牌挑战”增强互动\n◾夏季地域主题：推出“雷山酸汤锅”，结合贵州文化表演(如高山流水敬酒)吸引眼球\n◾冬季新品：2024年推出“浓浓浓菌汤”，强化健康滋补概念\n2⃣️数字化与私域运营\n◾自有App\n◾会员体系\n3⃣️事件营销与跨界合作\n◾画牛得牛大赛:吸引绘画爱好者参与\n.\n🔥🔥海底捞的成功密码——情绪价值\n🖥️在忙碌的生活里，每个人内心深处其实都渴望一点点的“被看见”和“被照顾”。海底捞恰恰就提供了这样一个“情绪避难所”\n.\n🍊其他干货都在图里啦，有需要的宝子可戳图了解~', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/18/b6a0dd9780804a85919185bbb7ac770c.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/18/b6a0dd9780804a85919185bbb7ac770c.png,https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/18/6450528768354d75a723f77247c5b33e.png,https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/18/9b62bd2892af40a3a267a67847e91bdd.png,https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/18/56af203b202a4ed880e4288f3291cbcd.png', '普洱学院西区食堂', NULL, NULL, 1, 4, 12, 1, 0, NULL, 1, 0, 1, '2025-12-18 17:54:05', '2025-12-18 22:01:05');
INSERT INTO `notes` VALUES (23, 39, 1, NULL, 6, '天生祥的美味兔子', '天生祥的美味兔子,值得大家框框', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/18/690700badb424a5aa5916c411bd660f4.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/18/690700badb424a5aa5916c411bd660f4.png', NULL, NULL, NULL, 0, 0, 6, 0, 0, NULL, 1, 0, 1, '2025-12-18 21:14:36', '2025-12-26 22:53:01');
INSERT INTO `notes` VALUES (24, 8, 2, 8, 6, '天生祥招人啦', '在景东的小伙伴注意啦\n景东服务岗\n天生祥超市收银员\n袁老四服务员（包吃住）\n瑞幸咖啡（店员2名）\n职中食堂售卖四餐\n银生学校食堂卖四餐', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/18/bd814ecc9b0d403880099fe58ade567f.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/18/bd814ecc9b0d403880099fe58ade567f.png', '', NULL, NULL, 0, 1, 74, 1, 0, NULL, 1, 1, 1, '2025-12-18 21:16:17', '2025-12-26 22:53:44');
INSERT INTO `notes` VALUES (25, 39, 2, 8, 6, '普洱天生祥超市东西好全啊', '感觉比北京的大超市东西还全，而且有适合懒人的免洗免切的配菜盒。我没拍太多照片，我看超市里还有很多好像是自家品牌的商品，我以为这是云南的大连锁呢，一搜居然只是普洱的连锁超市，做成这样真的很牛很厉害了👍', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/18/4514a99897954368a583ba8525746f00.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/18/4514a99897954368a583ba8525746f00.png', NULL, NULL, NULL, 1, 1, 3, 1, 0, NULL, 1, 0, 1, '2025-12-18 21:58:56', '2025-12-19 21:02:48');
INSERT INTO `notes` VALUES (26, 22, 2, 1, NULL, '好看的小动物值得分享', '非常可爱的小熊,大家快来一起看看吧', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/20/9ea4ef6f710d4a1f837234e7b0c52362.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/20/9ea4ef6f710d4a1f837234e7b0c52362.png,https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/20/1288f0dbdaa44f50998d7f9a13ddcfa0.png,https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/20/6cc924f3346545bb94c09527408f4fcf.png,https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/20/a01554bb2cbe4a8aa2209cf325a554cb.png', NULL, NULL, NULL, 0, 1, 12, 0, 0, NULL, 1, 0, 1, '2025-12-20 12:03:08', '2025-12-20 12:28:19');
INSERT INTO `notes` VALUES (27, 22, 2, 1, 6, '冬季小猫咪', '是冬日限定皮肤哦～谁家猫咪这么可爱呀', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/20/2b940a4942874e99889e9e565a48f8db.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/20/2b940a4942874e99889e9e565a48f8db.png,https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/20/30d08609d6534219a962bf396df927f3.png,https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/20/494f43f9fabb4d66b9bd84197ab5ab31.png', '魔方集市中通快递', 22.7694250, 100.9992750, 0, 2, 6, 0, 0, NULL, 1, 0, 1, '2025-12-20 21:37:22', '2025-12-25 23:36:15');
INSERT INTO `notes` VALUES (28, 22, 2, 1, NULL, '小猫咪', '很可爱的小猫咪,大家一起看看', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/21/cce751b6e6be4bc0b09a6a46b7a8c360.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/21/cce751b6e6be4bc0b09a6a46b7a8c360.png', NULL, NULL, NULL, 0, 0, 35, 0, 0, NULL, 1, 0, 1, '2025-12-21 15:12:21', '2025-12-26 22:53:47');
INSERT INTO `notes` VALUES (29, 42, 2, 9, 7, '爱你牛肉开业啦', '爱你羊肉新开业啦,大家快来一起看看', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/25/9d3a07debdce440485ab4751b9ca48ca.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/25/9d3a07debdce440485ab4751b9ca48ca.png', '西二燕军美食城', 22.7693830, 100.9994630, 0, 2, 51, 0, 0, NULL, 1, 1, 1, '2025-12-25 20:28:30', '2025-12-28 15:02:06');
INSERT INTO `notes` VALUES (30, 43, 2, 10, 8, '南阳大师傅店铺开业啦', '大家快来一起体验一下吧,新店开业打骨折', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/28/c54b0029f3654d20aabda8a3d8dc33a6.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/28/c54b0029f3654d20aabda8a3d8dc33a6.png', '黎杨炸货铺', 22.7712710, 100.9989020, 1, 1, 3, 1, 0, NULL, 1, 0, 1, '2025-12-28 13:06:50', '2025-12-28 15:02:21');

-- ----------------------------
-- Table structure for notifications
-- ----------------------------
DROP TABLE IF EXISTS `notifications`;
CREATE TABLE `notifications`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` bigint NOT NULL COMMENT '接收通知的用户ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '通知标题',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '通知内容',
  `type` tinyint NOT NULL COMMENT '通知类型（1点赞，2评论，3关注，4系统）',
  `related_id` bigint NULL DEFAULT NULL COMMENT '关联ID（笔记ID、评论ID等）',
  `is_read` tinyint NOT NULL DEFAULT 0 COMMENT '是否已读（0未读，1已读）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_is_read`(`user_id` ASC, `is_read` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 78 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notifications
-- ----------------------------
INSERT INTO `notifications` VALUES (1, 29, '收到点赞', '用户2199 赞了你的笔记', 1, 6, 0, '2025-12-05 12:41:18');
INSERT INTO `notifications` VALUES (2, 29, '新粉丝', '用户2199 关注了你', 3, 22, 0, '2025-12-05 12:41:23');
INSERT INTO `notifications` VALUES (3, 28, '收到点赞', '用户2199 赞了你的笔记', 1, 4, 0, '2025-12-05 12:41:46');
INSERT INTO `notifications` VALUES (4, 28, '新粉丝', '用户2199 关注了你', 3, 22, 0, '2025-12-05 12:41:53');
INSERT INTO `notifications` VALUES (5, 27, '收到点赞', '用户2199 赞了你的笔记', 1, 7, 0, '2025-12-05 12:42:16');
INSERT INTO `notifications` VALUES (6, 27, '新粉丝', '用户2199 关注了你', 3, 22, 0, '2025-12-05 12:42:18');
INSERT INTO `notifications` VALUES (8, 29, '收到评论', '用户2199 评论了你的笔记', 2, 6, 0, '2025-12-05 13:39:07');
INSERT INTO `notifications` VALUES (9, 29, '收到点赞', '用户3697 赞了你的笔记', 1, 6, 0, '2025-12-05 15:43:02');
INSERT INTO `notifications` VALUES (10, 29, '新粉丝', '用户3697 关注了你', 3, 32, 0, '2025-12-05 15:43:07');
INSERT INTO `notifications` VALUES (11, 29, '收到评论', '用户3697 评论了你的笔记', 2, 6, 0, '2025-12-05 15:43:30');
INSERT INTO `notifications` VALUES (12, 28, '收到点赞', '用户3697 赞了你的笔记', 1, 4, 0, '2025-12-05 15:47:47');
INSERT INTO `notifications` VALUES (13, 27, '新粉丝', '用户3697 关注了你', 3, 32, 0, '2025-12-05 15:57:26');
INSERT INTO `notifications` VALUES (14, 27, '新粉丝', '用户3697 关注了你', 3, 32, 0, '2025-12-05 15:57:31');
INSERT INTO `notifications` VALUES (15, 27, '新粉丝', '用户3697 关注了你', 3, 32, 0, '2025-12-05 15:57:35');
INSERT INTO `notifications` VALUES (16, 28, '新粉丝', '用户3697 关注了你', 3, 32, 0, '2025-12-05 15:57:41');
INSERT INTO `notifications` VALUES (17, 22, '新粉丝', '用户3697 关注了你', 3, 32, 0, '2025-12-05 15:57:44');
INSERT INTO `notifications` VALUES (18, 27, '收到点赞', '用户3697 赞了你的笔记', 1, 3, 0, '2025-12-05 15:58:00');
INSERT INTO `notifications` VALUES (19, 29, '收到点赞', '用户3697 赞了你的笔记', 1, 5, 0, '2025-12-05 15:58:04');
INSERT INTO `notifications` VALUES (20, 22, '收到点赞', '用户3697 赞了你的笔记', 1, 2, 0, '2025-12-05 16:08:49');
INSERT INTO `notifications` VALUES (21, 22, '收到点赞', '用户5321 赞了你的笔记', 1, 10, 0, '2025-12-05 18:27:11');
INSERT INTO `notifications` VALUES (22, 22, '收到点赞', '用户5321 赞了你的笔记', 1, 10, 0, '2025-12-05 18:27:14');
INSERT INTO `notifications` VALUES (23, 22, '收到点赞', '用户5321 赞了你的笔记', 1, 10, 0, '2025-12-05 18:27:15');
INSERT INTO `notifications` VALUES (24, 28, '收到评论', '用户5321 评论了你的笔记', 2, 4, 0, '2025-12-05 18:28:06');
INSERT INTO `notifications` VALUES (25, 22, '收到评论', '用户5321 评论了你的笔记', 2, 1, 0, '2025-12-05 18:29:24');
INSERT INTO `notifications` VALUES (26, 29, '收到点赞', '7798 赞了你的笔记', 1, 6, 0, '2025-12-12 18:22:48');
INSERT INTO `notifications` VALUES (27, 29, '收到点赞', '7798 赞了你的笔记', 1, 6, 0, '2025-12-12 18:22:50');
INSERT INTO `notifications` VALUES (28, 29, '收到点赞', '7798 赞了你的笔记', 1, 6, 0, '2025-12-12 18:22:52');
INSERT INTO `notifications` VALUES (29, 29, '收到点赞', '7798 赞了你的笔记', 1, 6, 0, '2025-12-12 18:29:41');
INSERT INTO `notifications` VALUES (30, 29, '收到点赞', '7798 赞了你的笔记', 1, 6, 0, '2025-12-12 18:29:47');
INSERT INTO `notifications` VALUES (31, 28, '收到点赞', '7798 赞了你的笔记', 1, 4, 0, '2025-12-12 18:29:53');
INSERT INTO `notifications` VALUES (32, 28, '收到点赞', '7798 赞了你的笔记', 1, 4, 0, '2025-12-12 18:29:56');
INSERT INTO `notifications` VALUES (33, 28, '收到点赞', '7798 赞了你的笔记', 1, 4, 0, '2025-12-12 18:31:55');
INSERT INTO `notifications` VALUES (34, 28, '收到点赞', '7798 赞了你的笔记', 1, 4, 0, '2025-12-12 18:32:14');
INSERT INTO `notifications` VALUES (35, 29, '收到点赞', '7798 赞了你的笔记', 1, 6, 0, '2025-12-12 18:40:19');
INSERT INTO `notifications` VALUES (36, 29, '收到点赞', '7798 赞了你的笔记', 1, 6, 0, '2025-12-12 18:40:28');
INSERT INTO `notifications` VALUES (37, 29, '收到点赞', '7798 赞了你的笔记', 1, 6, 0, '2025-12-12 18:40:29');
INSERT INTO `notifications` VALUES (38, 29, '收到点赞', '7798 赞了你的笔记', 1, 6, 0, '2025-12-12 18:40:37');
INSERT INTO `notifications` VALUES (39, 29, '收到点赞', '7798 赞了你的笔记', 1, 6, 0, '2025-12-12 18:40:48');
INSERT INTO `notifications` VALUES (40, 28, '收到点赞', '7798 赞了你的笔记', 1, 4, 0, '2025-12-12 18:41:29');
INSERT INTO `notifications` VALUES (41, 28, '收到点赞', '7798 赞了你的笔记', 1, 4, 0, '2025-12-12 18:41:34');
INSERT INTO `notifications` VALUES (42, 29, '收到点赞', '7798 赞了你的笔记', 1, 6, 0, '2025-12-12 18:42:31');
INSERT INTO `notifications` VALUES (43, 29, '收到点赞', '7798 赞了你的笔记', 1, 6, 0, '2025-12-12 18:42:33');
INSERT INTO `notifications` VALUES (44, 29, '收到点赞', '7798 赞了你的笔记', 1, 6, 0, '2025-12-12 18:53:10');
INSERT INTO `notifications` VALUES (45, 29, '收到点赞', '7798 赞了你的笔记', 1, 6, 0, '2025-12-12 18:53:42');
INSERT INTO `notifications` VALUES (46, 28, '收到点赞', '7798 赞了你的笔记', 1, 4, 0, '2025-12-12 18:55:35');
INSERT INTO `notifications` VALUES (47, 28, '收到点赞', '7798 赞了你的笔记', 1, 4, 0, '2025-12-12 18:55:39');
INSERT INTO `notifications` VALUES (48, 28, '收到点赞', '7798 赞了你的笔记', 1, 4, 0, '2025-12-12 18:55:41');
INSERT INTO `notifications` VALUES (49, 29, '新粉丝', '7798 关注了你', 3, 24, 0, '2025-12-12 18:56:09');
INSERT INTO `notifications` VALUES (50, 32, '收到点赞', '7798 赞了你的笔记', 1, 9, 0, '2025-12-12 19:43:33');
INSERT INTO `notifications` VALUES (51, 29, '收到点赞', '用户2199 赞了你的笔记', 1, 6, 0, '2025-12-12 19:59:45');
INSERT INTO `notifications` VALUES (52, 29, '收到点赞', '用户2199 赞了你的笔记', 1, 6, 0, '2025-12-12 19:59:52');
INSERT INTO `notifications` VALUES (53, 29, '收到点赞', '用户2199 赞了你的笔记', 1, 6, 0, '2025-12-12 19:59:54');
INSERT INTO `notifications` VALUES (54, 29, '收到点赞', '用户2199 赞了你的笔记', 1, 6, 0, '2025-12-12 19:59:55');
INSERT INTO `notifications` VALUES (55, 32, '新粉丝', '用户2199 关注了你', 3, 22, 0, '2025-12-14 21:24:32');
INSERT INTO `notifications` VALUES (56, 27, '收到点赞', '用户2199 赞了你的笔记', 1, 3, 0, '2025-12-14 22:13:06');
INSERT INTO `notifications` VALUES (57, 24, '新粉丝', '用户2199 关注了你', 3, 22, 0, '2025-12-14 22:13:15');
INSERT INTO `notifications` VALUES (58, 32, '新粉丝', '用户2199 关注了你', 3, 22, 0, '2025-12-14 22:13:53');
INSERT INTO `notifications` VALUES (59, 24, '新粉丝', '用户2199 关注了你', 3, 22, 0, '2025-12-14 22:14:21');
INSERT INTO `notifications` VALUES (60, 24, '新粉丝', '用户2199 关注了你', 3, 22, 0, '2025-12-14 22:14:23');
INSERT INTO `notifications` VALUES (61, 29, '新粉丝', '用户2199 关注了你', 3, 22, 0, '2025-12-14 22:16:07');
INSERT INTO `notifications` VALUES (62, 29, '收到点赞', '用户2199 赞了你的笔记', 1, 6, 0, '2025-12-14 22:16:11');
INSERT INTO `notifications` VALUES (63, 29, '收到点赞', '用户2199 赞了你的笔记', 1, 6, 0, '2025-12-14 22:16:13');
INSERT INTO `notifications` VALUES (64, 29, '收到点赞', '用户2199 赞了你的笔记', 1, 6, 0, '2025-12-14 22:16:16');
INSERT INTO `notifications` VALUES (65, 32, '新粉丝', '用户2199 关注了你', 3, 22, 0, '2025-12-14 22:17:50');
INSERT INTO `notifications` VALUES (66, 27, '新粉丝', '用户2199 关注了你', 3, 22, 0, '2025-12-14 22:20:29');
INSERT INTO `notifications` VALUES (67, 32, '新粉丝', '7798 关注了你', 3, 24, 0, '2025-12-14 22:25:51');
INSERT INTO `notifications` VALUES (68, 32, '新粉丝', '7798 关注了你', 3, 24, 0, '2025-12-14 22:45:57');
INSERT INTO `notifications` VALUES (69, 28, '新粉丝', '7798 关注了你', 3, 24, 0, '2025-12-14 22:46:43');
INSERT INTO `notifications` VALUES (70, 28, '新粉丝', '7798 关注了你', 3, 24, 0, '2025-12-14 22:51:55');
INSERT INTO `notifications` VALUES (71, 24, '新粉丝', '用户2199 关注了你', 3, 22, 0, '2025-12-14 23:23:41');
INSERT INTO `notifications` VALUES (72, 28, '新粉丝', '用户2199 关注了你', 3, 22, 0, '2025-12-14 23:23:52');
INSERT INTO `notifications` VALUES (73, 29, '新粉丝', '用户2199 关注了你', 3, 22, 0, '2025-12-14 23:23:54');
INSERT INTO `notifications` VALUES (74, 24, '新粉丝', '用户2199 关注了你', 3, 22, 0, '2025-12-14 23:24:09');
INSERT INTO `notifications` VALUES (75, 27, '新粉丝', '用户2199 关注了你', 3, 22, 0, '2025-12-14 23:24:11');
INSERT INTO `notifications` VALUES (76, 27, '新粉丝', '用户2199 关注了你', 3, 22, 0, '2025-12-14 23:28:13');
INSERT INTO `notifications` VALUES (77, 22, '新粉丝', '用户7596 关注了你', 3, 27, 0, '2025-12-14 23:29:42');

-- ----------------------------
-- Table structure for private_messages
-- ----------------------------
DROP TABLE IF EXISTS `private_messages`;
CREATE TABLE `private_messages`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `conversation_id` bigint NOT NULL COMMENT '会话ID',
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `receiver_id` bigint NOT NULL COMMENT '接收者ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
  `message_type` tinyint NULL DEFAULT 1 COMMENT '消息类型：1=文本，2=图片，3=语音',
  `is_read` tinyint NULL DEFAULT 0 COMMENT '是否已读：0=未读，1=已读',
  `read_at` datetime NULL DEFAULT NULL COMMENT '已读时间',
  `is_deleted_by_sender` tinyint NULL DEFAULT 0 COMMENT '发送者是否删除：0=否，1=是',
  `is_deleted_by_receiver` tinyint NULL DEFAULT 0 COMMENT '接收者是否删除：0=否，1=是',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_conversation`(`conversation_id` ASC) USING BTREE,
  INDEX `idx_sender`(`sender_id` ASC) USING BTREE,
  INDEX `idx_receiver`(`receiver_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '私信消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of private_messages
-- ----------------------------

-- ----------------------------
-- Table structure for seckill_activities
-- ----------------------------
DROP TABLE IF EXISTS `seckill_activities`;
CREATE TABLE `seckill_activities`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '秒杀活动ID',
  `merchant_id` bigint NOT NULL COMMENT '所属商家ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '活动标题',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '活动描述',
  `start_time` datetime NOT NULL COMMENT '活动开始时间',
  `end_time` datetime NOT NULL COMMENT '活动结束时间',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1未开始，2进行中，3已结束，4已取消）',
  `display_order` int NOT NULL DEFAULT 0 COMMENT '显示顺序',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_merchant_id`(`merchant_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_start_time`(`start_time` ASC) USING BTREE,
  INDEX `idx_end_time`(`end_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '秒杀活动表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of seckill_activities
-- ----------------------------
INSERT INTO `seckill_activities` VALUES (1, 1, '限时秒杀', '每日限时秒杀，抢到就是赚到！', '2025-12-21 00:00:00', '2025-12-22 00:00:00', 2, 100, '2025-12-21 23:40:08', '2025-12-21 23:40:08');

-- ----------------------------
-- Table structure for seckill_coupons
-- ----------------------------
DROP TABLE IF EXISTS `seckill_coupons`;
CREATE TABLE `seckill_coupons`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `seckill_id` bigint NOT NULL COMMENT '秒杀活动ID',
  `coupon_id` bigint NOT NULL COMMENT '优惠券ID',
  `seckill_price` decimal(10, 2) NOT NULL COMMENT '秒杀价格',
  `original_price` decimal(10, 2) NOT NULL COMMENT '原价',
  `seckill_stock` int NOT NULL COMMENT '秒杀库存',
  `remain_stock` int NOT NULL COMMENT '剩余库存',
  `seckill_limit` int NOT NULL DEFAULT 1 COMMENT '每人限购数量',
  `display_order` int NOT NULL DEFAULT 0 COMMENT '显示顺序',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_seckill_coupon`(`seckill_id` ASC, `coupon_id` ASC) USING BTREE,
  INDEX `idx_coupon_id`(`coupon_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '秒杀券关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of seckill_coupons
-- ----------------------------

-- ----------------------------
-- Table structure for shop_reviews
-- ----------------------------
DROP TABLE IF EXISTS `shop_reviews`;
CREATE TABLE `shop_reviews`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `shop_id` bigint NOT NULL COMMENT '商家ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `rating` decimal(3, 2) NOT NULL COMMENT '综合评分（1-5）',
  `taste_score` decimal(3, 2) NULL DEFAULT NULL COMMENT '口味评分',
  `environment_score` decimal(3, 2) NULL DEFAULT NULL COMMENT '环境评分',
  `service_score` decimal(3, 2) NULL DEFAULT NULL COMMENT '服务评分',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '评价内容',
  `reply` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '商家回复内容',
  `reply_time` datetime NULL DEFAULT NULL COMMENT '回复时间',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '评价图片（JSON）',
  `like_count` int NOT NULL DEFAULT 0 COMMENT '点赞数',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1正常，2隐藏）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_shop_id`(`shop_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 50 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商家评价表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shop_reviews
-- ----------------------------
INSERT INTO `shop_reviews` VALUES (1, 2, 22, 5.00, 5.00, 5.00, 5.00, '非常好的商家', NULL, NULL, NULL, 0, 1, '2025-12-22 16:31:33', '2025-12-22 16:31:33');
INSERT INTO `shop_reviews` VALUES (2, 2, 22, 4.00, 3.00, 4.00, 3.00, '商家的米线很好吃\n值得大家再次来吃', NULL, NULL, NULL, 0, 1, '2025-12-22 16:32:11', '2025-12-22 16:32:11');
INSERT INTO `shop_reviews` VALUES (3, 2, 22, 1.00, 1.00, 2.00, 3.00, '店家很不好吃,真的不好吃,大家不要再去了\n避雷避雷!!!', NULL, NULL, NULL, 0, 1, '2025-12-22 16:36:15', '2025-12-22 16:36:15');
INSERT INTO `shop_reviews` VALUES (4, 6, 22, 4.00, 4.00, 3.00, 4.00, '很不错的商家,还送了小饮料', NULL, NULL, NULL, 0, 1, '2025-12-25 10:41:23', '2025-12-25 10:41:23');
INSERT INTO `shop_reviews` VALUES (5, 6, 22, 1.00, 1.00, 1.00, 1.00, '口味不好,环境很差,服务还不好', NULL, NULL, NULL, 0, 1, '2025-12-25 11:15:07', '2025-12-25 11:15:07');
INSERT INTO `shop_reviews` VALUES (6, 8, 43, 5.00, 5.00, 5.00, 5.00, '很不错的店铺', '欢迎下次光临,感谢陪伴', '2025-12-28 16:05:01', NULL, 0, 1, '2025-12-28 15:10:30', '2025-12-28 16:05:01');
INSERT INTO `shop_reviews` VALUES (7, 8, 24, 3.00, 3.00, 3.00, 5.00, '口味一般,环境一般,但是服务很可以', '亲，真的非常抱歉😭 听到您说口味和环境都没达到预期，我们心里特别难受。\n\n完全理解您的感受，花了钱却没获得满意的体验，换作是我们也会失望的。您提到的服务不错，是对我们小伙伴最大的鼓励，感谢！\n\n针对您反馈的问题，我们已经和后厨、清洁团队紧急开会，会立刻着手优化餐品配方和用餐环境，争取下次能让您眼前一亮✨。\n\n为了表达我们真诚的歉意，特意为您准备了一张5元无门槛优惠券，希望您能再给我们一次机会。\n\n再次为这次不完美的体验说声对不起！我们真的非常期待您的再次光临，让我们用行动证明改变🙏。', '2025-12-28 23:25:58', NULL, 0, 1, '2025-12-28 16:09:44', '2025-12-28 23:25:58');
INSERT INTO `shop_reviews` VALUES (8, 8, 24, 2.00, 1.00, 1.00, 2.00, '感觉很一般的店铺', '非常抱歉给了你很不好的体验\n', '2025-12-28 19:31:10', NULL, 0, 2, '2025-12-28 16:10:18', '2025-12-28 21:31:31');
INSERT INTO `shop_reviews` VALUES (9, 8, 24, 1.00, 1.00, 2.00, 1.00, '一般般的', NULL, NULL, NULL, 0, 2, '2025-12-28 16:10:32', '2025-12-28 21:31:25');
INSERT INTO `shop_reviews` VALUES (10, 8, 26, 5.00, 5.00, 5.00, 5.00, '可颂刚出炉的时候太香了！外皮酥脆，内里柔软，黄油味浓郁，绝对是本市最好吃的可颂！', NULL, NULL, NULL, 0, 1, '2025-12-27 08:45:12', '2025-12-27 08:45:12');
INSERT INTO `shop_reviews` VALUES (11, 8, 31, 4.00, 4.00, 4.00, 5.00, '全麦核桃面包很有嚼劲，健康又饱腹。店员还主动帮我加热，服务很贴心！', NULL, NULL, NULL, 0, 1, '2025-12-26 11:20:33', '2025-12-26 11:20:33');
INSERT INTO `shop_reviews` VALUES (12, 8, 29, 5.00, 5.00, 4.00, 5.00, '生日定制的草莓奶油蛋糕惊艳全场！奶油轻盈不腻，水果新鲜，孩子超开心！', NULL, NULL, NULL, 0, 1, '2025-12-25 15:10:45', '2025-12-25 15:10:45');
INSERT INTO `shop_reviews` VALUES (13, 8, 24, 3.00, 3.00, 2.00, 4.00, '面包味道不错，但周末排队太久，店里也没地方坐，体验一般。', NULL, NULL, NULL, 0, 1, '2025-12-24 10:35:22', '2025-12-24 10:35:22');
INSERT INTO `shop_reviews` VALUES (14, 8, 33, 5.00, 5.00, 5.00, 5.00, '碱水结咸香有嚼劲，配上黑咖啡是绝配！每天早上必买两个当早餐。', NULL, NULL, NULL, 0, 1, '2025-12-23 09:15:18', '2025-12-23 09:15:18');
INSERT INTO `shop_reviews` VALUES (15, 8, 27, 2.00, 2.00, 3.00, 2.00, '今天买的牛角包是冷的，而且有点干，感觉不是现烤的，失望。', '亲，看到您的反馈我们真的很抱歉😔。牛角包应该是热乎乎、酥脆可口的，给您带来这样的体验我们非常理解您的失望。我们已经和厨房强调了现烤和保温的问题，会立刻改进。为了表达我们的歉意，下次您到店可以凭此记录免费领取一杯饮料，希望能有机会为您提供一次满意的体验。再次向您说声对不起，期待您的再次光临！🙏', '2025-12-29 00:03:33', NULL, 0, 1, '2025-12-22 14:40:09', '2025-12-29 00:03:33');
INSERT INTO `shop_reviews` VALUES (16, 8, 30, 4.00, 5.00, 3.00, 4.00, '肉松小贝超级松软，肉松量足！就是店面太小，高峰期挤不进去。', NULL, NULL, NULL, 0, 1, '2025-12-21 16:55:37', '2025-12-21 16:55:37');
INSERT INTO `shop_reviews` VALUES (17, 8, 25, 5.00, 5.00, 5.00, 5.00, '无糖南瓜欧包太适合我这种控糖人士了！口感湿润，香气自然，强烈推荐！', NULL, NULL, NULL, 0, 1, '2025-12-20 08:30:14', '2025-12-20 08:30:14');
INSERT INTO `shop_reviews` VALUES (18, 8, 34, 4.00, 4.00, 4.00, 4.00, '抹茶丹麦酥层次分明，茶香清新不苦，甜度刚刚好，吃完还想再来！', '亲，看到您这么喜欢我们的抹茶丹麦酥，真是太开心啦！🥰 您提到的“层次分明”、“茶香清新不甜腻”正是我们最想呈现的口感，能得到您的认可我们特别有成就感！听到您说“吃完还想再来”就是对我们最大的鼓励！期待您下次光临，也欢迎带上同学一起来品尝呀！😊', '2025-12-29 00:01:47', NULL, 0, 1, '2025-12-28 10:05:29', '2025-12-29 00:01:47');
INSERT INTO `shop_reviews` VALUES (19, 8, 28, 5.00, 5.00, 5.00, 5.00, '店员小姐姐超温柔，还会根据我的口味推荐新品，每次来都像回家一样温暖！', NULL, NULL, NULL, 0, 1, '2025-12-27 13:25:41', '2025-12-27 13:25:41');
INSERT INTO `shop_reviews` VALUES (20, 8, 32, 4.00, 4.00, 5.00, 4.00, '店面干净整洁，面包陈列得像艺术品，看着就有食欲！', NULL, NULL, NULL, 0, 1, '2025-12-26 09:50:17', '2025-12-26 09:50:17');
INSERT INTO `shop_reviews` VALUES (21, 8, 26, 5.00, 5.00, 4.00, 5.00, '法棍外脆内韧，撕开还有拉丝感，配红酒太享受了！', NULL, NULL, NULL, 0, 1, '2025-12-25 18:40:33', '2025-12-25 18:40:33');
INSERT INTO `shop_reviews` VALUES (22, 8, 31, 3.00, 3.00, 3.00, 3.00, '价格偏高，一个可颂要18元，分量也不大，性价比一般。', NULL, NULL, NULL, 0, 1, '2025-12-24 12:15:27', '2025-12-24 12:15:27');
INSERT INTO `shop_reviews` VALUES (23, 8, 29, 5.00, 5.00, 5.00, 5.00, '巧克力流心可颂热乎乎的，切开瞬间爆浆，幸福感拉满！', NULL, NULL, NULL, 0, 1, '2025-12-23 11:05:19', '2025-12-23 11:05:19');
INSERT INTO `shop_reviews` VALUES (24, 8, 24, 4.00, 4.00, 4.00, 5.00, '提前预订了圣诞限定礼盒，包装精美，送客户很有面子！', NULL, NULL, NULL, 0, 1, '2025-12-22 17:30:08', '2025-12-22 17:30:08');
INSERT INTO `shop_reviews` VALUES (25, 8, 33, 1.00, 1.00, 1.00, 2.00, '买到发霉的吐司！联系客服三天没回复，食品安全太差了！', '亲，真的非常抱歉😭 让您买到发霉的吐司，还等了三天都没联系上我们，这体验太糟糕了，换作是我也会非常生气和失望。\n\n我们已紧急排查了这批产品，并加强了品控流程。对于这次糟糕的服务，我们深感愧疚，希望能为您补送两份精选小菜作为小小的心意🙏。\n\n真的对不起，让您有如此不愉快的经历。恳请您再给我们一次机会，我们一定会努力改进，不辜负您的信任。期待能再次为您服务！', '2025-12-28 23:27:44', NULL, 0, 1, '2025-12-21 09:20:44', '2025-12-28 23:27:44');
INSERT INTO `shop_reviews` VALUES (26, 8, 27, 4.00, 5.00, 3.00, 4.00, '芝士贝果拉丝超长，咸香十足！就是希望店里能多放几张凳子。', NULL, NULL, NULL, 0, 1, '2025-12-20 14:55:37', '2025-12-20 14:55:37');
INSERT INTO `shop_reviews` VALUES (27, 8, 30, 5.00, 5.00, 5.00, 5.00, '榴莲千层酥居然是隐藏菜单？求正式上架！榴莲味浓，酥皮掉渣，绝了！', NULL, NULL, NULL, 0, 1, '2025-12-28 09:10:55', '2025-12-28 09:10:55');
INSERT INTO `shop_reviews` VALUES (28, 8, 25, 4.00, 4.00, 4.00, 4.00, '南瓜乳酪包低糖健康，老人吃了都说好，还会回购！', NULL, NULL, NULL, 0, 1, '2025-12-27 16:40:18', '2025-12-27 16:40:18');
INSERT INTO `shop_reviews` VALUES (29, 8, 34, 5.00, 5.00, 5.00, 5.00, '肉桂卷香气扑鼻，甜而不腻，配上热牛奶是冬日最佳早餐！', NULL, NULL, NULL, 0, 1, '2025-12-26 08:25:22', '2025-12-26 08:25:22');
INSERT INTO `shop_reviews` VALUES (30, 8, 28, 5.00, 5.00, 4.00, 5.00, '每天早上都来买他们的日式生吐司，切片超薄，入口即化，配果酱绝了！', NULL, NULL, NULL, 0, 1, '2025-12-28 07:50:11', '2025-12-28 07:50:11');
INSERT INTO `shop_reviews` VALUES (31, 8, 32, 4.00, 4.00, 3.00, 5.00, '店员记得我常买的面包，还会主动提醒新品试吃，服务真的用心！', NULL, NULL, NULL, 0, 1, '2025-12-27 10:15:33', '2025-12-27 10:15:33');
INSERT INTO `shop_reviews` VALUES (32, 8, 26, 3.00, 3.00, 4.00, 3.00, '面包味道还行，但今天去晚了，很多热门款都卖光了，建议早点去。', NULL, NULL, NULL, 0, 1, '2025-12-26 13:40:27', '2025-12-26 13:40:27');
INSERT INTO `shop_reviews` VALUES (33, 8, 31, 5.00, 5.00, 5.00, 5.00, '圣诞限定树根蛋糕太惊艳了！巧克力浓郁，装饰精致，全家都爱吃！', NULL, NULL, NULL, 0, 1, '2025-12-25 19:20:45', '2025-12-25 19:20:45');
INSERT INTO `shop_reviews` VALUES (34, 8, 24, 2.00, 2.00, 2.00, 3.00, '牛角包放了一小时就变软了，不够酥脆，感觉用的是植物黄油？', NULL, NULL, NULL, 0, 1, '2025-12-24 11:05:18', '2025-12-24 11:05:18');
INSERT INTO `shop_reviews` VALUES (35, 8, 29, 5.00, 5.00, 5.00, 5.00, '无乳糖全麦面包救星！乳糖不耐也能安心吃，口感居然还不干！', NULL, NULL, NULL, 0, 1, '2025-12-23 08:35:29', '2025-12-23 08:35:29');
INSERT INTO `shop_reviews` VALUES (36, 8, 33, 4.00, 5.00, 3.00, 4.00, '蓝莓乳酪包酸甜适中，乳酪很新鲜！就是包装太简单，容易压扁。', NULL, NULL, NULL, 0, 1, '2025-12-22 15:50:37', '2025-12-22 15:50:37');
INSERT INTO `shop_reviews` VALUES (37, 8, 27, 5.00, 5.00, 4.00, 5.00, '碱水棒配黑啤是隐藏吃法！咸香有嚼劲，越嚼越香，强烈推荐！', NULL, NULL, NULL, 0, 1, '2025-12-21 12:25:14', '2025-12-21 12:25:14');
INSERT INTO `shop_reviews` VALUES (38, 8, 30, 4.00, 4.00, 4.00, 4.00, '店面虽小但干净明亮，面包陈列整齐，看着就很放心。', NULL, NULL, NULL, 0, 1, '2025-12-20 09:40:22', '2025-12-20 09:40:22');
INSERT INTO `shop_reviews` VALUES (39, 8, 25, 1.00, 1.00, 2.00, 1.00, '买到一只蟑螂在包装袋里！食品安全严重失职，已投诉！', '亲，真的非常非常抱歉！😭 看到您的反馈，我们完全理解您此刻的震惊和愤怒，在食品包装里发现异物，这确实是我们的严重失职，让您有了这么糟糕的体验。\n\n我们已第一时间联系门店进行彻查和整改，并会加强所有环节的清洁与品控，坚决杜绝此类事件再次发生。\n\n为了表达我们最诚挚的歉意，我们为您准备了一份心意：下次您到店时，可以免费领取一杯任意饮品。🙏\n\n再次为这次不愉快的经历向您郑重道歉，我们真心希望能有机会用更好的产品和服务，重新赢得您的信任。期待您的再次光临！', '2025-12-28 23:26:42', NULL, 0, 1, '2025-12-19 17:10:05', '2025-12-28 23:26:42');
INSERT INTO `shop_reviews` VALUES (40, 8, 34, 5.00, 5.00, 5.00, 5.00, '肉松海苔卷是我每周必囤的！肉松厚实，海苔香脆，孩子早餐最爱！', '亲，看到您每周都来囤我们的肉松海苔卷，还说是孩子早餐最爱，我们真的超级开心！🥰 能得到您和宝贝的长期喜爱，是对我们最大的认可。我们一定会继续坚持用厚实的肉松和香脆的海苔，把这份美味和用心一直做下去。期待下周再见到您来囤货哦！', '2025-12-29 10:21:38', NULL, 0, 1, '2025-12-28 14:30:41', '2025-12-29 10:21:38');
INSERT INTO `shop_reviews` VALUES (41, 8, 28, 4.00, 4.00, 5.00, 4.00, '店内播放轻音乐，氛围很放松，适合周末慢慢挑面包。', NULL, NULL, NULL, 0, 1, '2025-12-27 16:55:18', '2025-12-27 16:55:18');
INSERT INTO `shop_reviews` VALUES (42, 8, 32, 5.00, 5.00, 4.00, 5.00, '巧克力杏仁可颂坚果香浓，巧克力微苦不腻，吃完还想打包两个！', NULL, NULL, NULL, 0, 1, '2025-12-26 10:20:33', '2025-12-26 10:20:33');
INSERT INTO `shop_reviews` VALUES (43, 8, 26, 3.00, 3.00, 3.00, 4.00, '新品抹茶麻薯包太甜了，麻薯也偏硬，希望改进配方。', NULL, NULL, NULL, 0, 1, '2025-12-25 11:45:27', '2025-12-25 11:45:27');
INSERT INTO `shop_reviews` VALUES (44, 8, 31, 5.00, 5.00, 5.00, 5.00, '提前一周预订了婚礼小面包礼盒，造型可爱，宾客都问在哪买的！', NULL, NULL, NULL, 0, 1, '2025-12-24 18:10:19', '2025-12-24 18:10:19');
INSERT INTO `shop_reviews` VALUES (45, 8, 24, 4.00, 5.00, 3.00, 4.00, '蒜香法棍外皮超脆，蒜味浓郁，就是店里没地方坐，只能打包走。', NULL, NULL, NULL, 0, 1, '2025-12-23 13:35:08', '2025-12-23 13:35:08');
INSERT INTO `shop_reviews` VALUES (46, 8, 29, 5.00, 5.00, 5.00, 5.00, '店员主动教我怎么复热可颂，回家后依然酥脆，太专业了！', NULL, NULL, NULL, 0, 1, '2025-12-22 09:20:44', '2025-12-22 09:20:44');
INSERT INTO `shop_reviews` VALUES (47, 8, 33, 4.00, 4.00, 4.00, 5.00, '下雨天店员还送了我一次性雨衣，细节服务太暖心了！', NULL, NULL, NULL, 0, 1, '2025-12-21 15:55:37', '2025-12-21 15:55:37');
INSERT INTO `shop_reviews` VALUES (48, 8, 27, 5.00, 5.00, 4.00, 5.00, '玫瑰荔枝贝果颜值高，花香清新不抢味，女生一定会喜欢！', NULL, NULL, NULL, 0, 1, '2025-12-20 12:40:22', '2025-12-20 12:40:22');
INSERT INTO `shop_reviews` VALUES (49, 8, 30, 4.00, 4.00, 4.00, 4.00, '南瓜芝士欧包低糖高纤维，健身人士的福音，饱腹感强又好吃！', NULL, NULL, NULL, 0, 1, '2025-12-19 10:15:11', '2025-12-19 10:15:11');

-- ----------------------------
-- Table structure for shop_tags
-- ----------------------------
DROP TABLE IF EXISTS `shop_tags`;
CREATE TABLE `shop_tags`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `shop_id` bigint NOT NULL COMMENT '商家ID',
  `tag_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签名称',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_shop_id`(`shop_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商家标签表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shop_tags
-- ----------------------------

-- ----------------------------
-- Table structure for shops
-- ----------------------------
DROP TABLE IF EXISTS `shops`;
CREATE TABLE `shops`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商家ID',
  `merchant_id` bigint NULL DEFAULT NULL COMMENT '所属商家ID',
  `category_id` int NOT NULL COMMENT '分类ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商家名称',
  `header_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面图URL',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '商家图片集合（JSON）',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '商家描述',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '详细地址',
  `latitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '纬度',
  `longitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '经度',
  `business_hours` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '营业时间',
  `average_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '人均消费',
  `rating` decimal(3, 2) NOT NULL DEFAULT 0.00 COMMENT '综合评分',
  `taste_score` decimal(3, 2) NOT NULL DEFAULT 0.00 COMMENT '口味评分',
  `environment_score` decimal(3, 2) NOT NULL DEFAULT 0.00 COMMENT '环境评分',
  `service_score` decimal(3, 2) NOT NULL DEFAULT 0.00 COMMENT '服务评分',
  `review_count` int NOT NULL DEFAULT 0 COMMENT '评价数量',
  `popularity` int NOT NULL DEFAULT 0 COMMENT '人气值',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1营业中，2休息中，3已关闭）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category`(`category_id` ASC) USING BTREE,
  INDEX `idx_rating`(`rating` ASC) USING BTREE,
  INDEX `idx_popularity`(`popularity` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_location`(`latitude` ASC, `longitude` ASC) USING BTREE,
  INDEX `idx_merchant_id`(`merchant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商家信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shops
-- ----------------------------
INSERT INTO `shops` VALUES (2, 1, 1, '米线王子', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head1.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head1.png', '米线王子很好吃的', '16750152199', '普洱学院1735美食城', NULL, NULL, '09.00-22.00', 15.00, 3.33, 3.00, 3.67, 3.67, 3, 0, 1, '2025-12-14 20:43:46', '2025-12-21 20:17:33');
INSERT INTO `shops` VALUES (3, 2, 1, '张亮麻辣烫', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/14/1284665d69df4671b9ef565a077cd4bc.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/14/1f672828558f4de28535eda00e830b48.png', '', '18890746321', '普洱学院张亮麻辣烫', NULL, NULL, '9.00-22.00', 200.00, 5.00, 5.00, 5.00, 5.00, 0, 0, 1, '2025-12-14 22:04:08', '2025-12-14 22:04:08');
INSERT INTO `shops` VALUES (4, 2, 1, '张亮麻辣烫', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/14/892c53a62a2346d2bea437e7351d3092.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/14/fc29f438b4cd432eac95ac17657b35e2.png', '张亮麻辣烫', '18897963214', '张亮麻辣烫', NULL, NULL, '09.00-22.00', 200.00, 5.00, 5.00, 5.00, 5.00, 0, 0, 1, '2025-12-14 22:12:47', '2025-12-14 22:19:35');
INSERT INTO `shops` VALUES (5, 3, 1, '海底捞火锅', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/18/ac2f76999158465f87ee661c21e33c60.png', '[\"https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/18/1c86690a63704d26bd6ff9e53450edb8.png\"]', '非常好吃的海底捞火锅', '15216091650', '普洱学院西区食堂 (南屏镇学苑路大荒地)', 22.7693650, 100.9992760, '每天九点到凌晨六点', 200.00, 5.00, 5.00, 5.00, 5.00, 0, 0, 1, '2025-12-17 13:05:25', '2025-12-18 17:28:40');
INSERT INTO `shops` VALUES (6, 8, 2, '天生祥', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/logo/2025/12/18/c2c77baa7be446f6911672ba2373fc41.png', '[\"https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/22/69e5d5ff7ab34ababa036c93d51dc83c.png\"]', '天生祥 欢迎您的光临', '18987934526', '西二燕军美食城 (学苑路与景东路交叉口东南260米)', 22.7693830, 100.9994630, '09:00-22:00', NULL, 2.50, 2.50, 2.00, 2.50, 2, 0, 1, '2025-12-18 21:11:50', '2025-12-25 09:27:46');
INSERT INTO `shops` VALUES (7, 9, 1, '爱你羊肉', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/logo/2025/12/25/15bd258911184284a11e040a9def83b8.png', '[\"https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/28/049a22a740ce454288fb0a0d869213d6.png\",\"https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/28/1bf72f092f9340c6b76e528df06fde8f.png\",\"https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/28/410bcd9ccc9542538b6f21b4b3d6728c.png\",\"https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/28/b34b768a7c704d128a3ab85d60fd3f26.png\",\"https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/28/bbbf2485c6e3481492113a31b5ceeca3.png\",\"https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/28/07225e9bfe4d4d8bbdfc75e79ae7561a.png\",\"https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/28/9fc79db1c0b943e3aff5d4a5dfc165a9.png\"]', '爱你羊肉 欢迎您的光临', '18379634597', '魔方集市中通快递 (学苑路与景东路交叉口东南240米)', 22.7694250, 100.9992750, '09:00-22:00', 200.00, 5.00, 5.00, 5.00, 5.00, 0, 0, 1, '2025-12-25 20:01:26', '2025-12-28 11:55:05');
INSERT INTO `shops` VALUES (8, 10, 1, '南阳大师傅面包店', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/logo/2025/12/28/999d28a6fdb14acfb7f658c083294ad5.png', '', '南阳大师傅 欢迎您的光临', '19879634521', '待完善', NULL, NULL, '09:00-22:00', NULL, 4.00, 4.00, 4.00, 5.00, 2, 0, 1, '2025-12-28 13:03:53', '2025-12-28 21:31:31');

-- ----------------------------
-- Table structure for system_notices
-- ----------------------------
DROP TABLE IF EXISTS `system_notices`;
CREATE TABLE `system_notices`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` bigint NOT NULL COMMENT '接收通知的用户ID',
  `from_user_id` bigint NOT NULL COMMENT '触发通知的用户ID',
  `notice_type` tinyint NOT NULL COMMENT '通知类型（1点赞笔记，2评论笔记，3关注，4点赞评论）',
  `target_id` bigint NULL DEFAULT NULL COMMENT '目标ID',
  `content` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '通知内容',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关联图片URL',
  `is_read` tinyint NOT NULL DEFAULT 0 COMMENT '是否已读（0未读，1已读）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_is_read`(`user_id` ASC, `is_read` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 41 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of system_notices
-- ----------------------------
INSERT INTO `system_notices` VALUES (1, 22, 24, 1, 10, '7798 赞了你的笔记', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/05/58310d4b04b844bea24a35d9c70b2f48.jpg', 0, '2025-12-15 10:19:26');
INSERT INTO `system_notices` VALUES (2, 24, 22, 1, 12, '用户2199 赞了你的笔记', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/12/eecb59b646a64b8aa9b92ff0ee5e4ae5.png', 0, '2025-12-15 10:29:16');
INSERT INTO `system_notices` VALUES (3, 32, 22, 3, 22, '用户2199 关注了你', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/avatars/2025/12/14/46580897d95f49cebe12dfbb9f663cf1.png', 0, '2025-12-15 10:51:04');
INSERT INTO `system_notices` VALUES (4, 28, 22, 1, 4, '用户2199 赞了你的笔记', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/66e7fc5bc0e14762ac21f9b6cc8f5d7a.png', 0, '2025-12-15 15:48:19');
INSERT INTO `system_notices` VALUES (5, 28, 22, 1, 4, '用户2199 赞了你的笔记', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/66e7fc5bc0e14762ac21f9b6cc8f5d7a.png', 0, '2025-12-15 15:48:20');
INSERT INTO `system_notices` VALUES (6, 28, 22, 1, 4, '用户2199 赞了你的笔记', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/66e7fc5bc0e14762ac21f9b6cc8f5d7a.png', 0, '2025-12-15 15:48:23');
INSERT INTO `system_notices` VALUES (7, 28, 22, 1, 4, '用户2199 赞了你的笔记', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/66e7fc5bc0e14762ac21f9b6cc8f5d7a.png', 0, '2025-12-15 15:48:34');
INSERT INTO `system_notices` VALUES (8, 28, 22, 1, 4, '用户2199 赞了你的笔记', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/66e7fc5bc0e14762ac21f9b6cc8f5d7a.png', 0, '2025-12-15 15:48:36');
INSERT INTO `system_notices` VALUES (9, 28, 22, 1, 4, '用户2199 赞了你的笔记', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/66e7fc5bc0e14762ac21f9b6cc8f5d7a.png', 0, '2025-12-15 15:50:39');
INSERT INTO `system_notices` VALUES (10, 22, 24, 2, 10, '7798 评论了你的笔记', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/05/58310d4b04b844bea24a35d9c70b2f48.jpg', 0, '2025-12-17 21:55:00');
INSERT INTO `system_notices` VALUES (11, 32, 22, 3, 22, '用户2199 关注了你', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/avatars/2025/12/14/46580897d95f49cebe12dfbb9f663cf1.png', 0, '2025-12-18 14:36:42');
INSERT INTO `system_notices` VALUES (12, 27, 22, 3, 22, '用户2199 关注了你', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/avatars/2025/12/14/46580897d95f49cebe12dfbb9f663cf1.png', 0, '2025-12-18 14:36:44');
INSERT INTO `system_notices` VALUES (13, 3, 22, 3, 22, '用户2199 关注了你', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/avatars/2025/12/14/46580897d95f49cebe12dfbb9f663cf1.png', 0, '2025-12-18 17:38:24');
INSERT INTO `system_notices` VALUES (14, 3, 22, 1, 21, '用户2199 赞了你的笔记', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/18/d00933cc599f40a980b1e96af986baf9.png', 0, '2025-12-18 17:39:23');
INSERT INTO `system_notices` VALUES (15, 3, 22, 2, 21, '用户2199 评论了你的笔记', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/18/d00933cc599f40a980b1e96af986baf9.png', 0, '2025-12-18 17:40:06');
INSERT INTO `system_notices` VALUES (16, 3, 22, 2, 22, '用户2199 评论了你的笔记', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/18/b6a0dd9780804a85919185bbb7ac770c.png', 0, '2025-12-18 18:13:48');
INSERT INTO `system_notices` VALUES (17, 8, 39, 3, 39, '天生祥 关注了你', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/avatar/2025/12/18/663481a49ee04f64926fa26f457047ed.png', 0, '2025-12-18 21:16:58');
INSERT INTO `system_notices` VALUES (18, 8, 39, 3, 39, '天生祥 关注了你', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/avatar/2025/12/18/663481a49ee04f64926fa26f457047ed.png', 0, '2025-12-18 21:17:00');
INSERT INTO `system_notices` VALUES (19, 8, 39, 3, 39, '天生祥 关注了你', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/avatar/2025/12/18/663481a49ee04f64926fa26f457047ed.png', 0, '2025-12-18 21:17:01');
INSERT INTO `system_notices` VALUES (20, 8, 39, 3, 39, '天生祥 关注了你', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/avatar/2025/12/18/663481a49ee04f64926fa26f457047ed.png', 0, '2025-12-18 21:17:02');
INSERT INTO `system_notices` VALUES (21, 8, 39, 1, 24, '天生祥 赞了你的笔记', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/18/bd814ecc9b0d403880099fe58ade567f.png', 0, '2025-12-18 21:17:17');
INSERT INTO `system_notices` VALUES (22, 8, 39, 1, 24, '天生祥 赞了你的笔记', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/18/bd814ecc9b0d403880099fe58ade567f.png', 0, '2025-12-18 21:17:20');
INSERT INTO `system_notices` VALUES (23, 22, 39, 1, 10, '天生祥 赞了你的笔记', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/05/58310d4b04b844bea24a35d9c70b2f48.jpg', 0, '2025-12-18 23:20:16');
INSERT INTO `system_notices` VALUES (24, 3, 22, 3, 22, '用户2199 关注了你', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/avatars/2025/12/14/46580897d95f49cebe12dfbb9f663cf1.png', 0, '2025-12-20 12:27:29');
INSERT INTO `system_notices` VALUES (25, 24, 22, 1, 12, '用户2199 赞了你的笔记', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/12/eecb59b646a64b8aa9b92ff0ee5e4ae5.png', 0, '2025-12-20 12:27:43');
INSERT INTO `system_notices` VALUES (26, 22, 24, 3, 24, '7798 关注了你', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head1.png', 0, '2025-12-20 12:28:13');
INSERT INTO `system_notices` VALUES (27, 8, 22, 1, 24, '用户2199 赞了你的笔记', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/18/bd814ecc9b0d403880099fe58ade567f.png', 0, '2025-12-20 21:40:15');
INSERT INTO `system_notices` VALUES (28, 8, 22, 1, 24, '用户2199 赞了你的笔记', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/18/bd814ecc9b0d403880099fe58ade567f.png', 0, '2025-12-20 21:40:17');
INSERT INTO `system_notices` VALUES (29, 8, 22, 1, 24, '用户2199 赞了你的笔记', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/18/bd814ecc9b0d403880099fe58ade567f.png', 0, '2025-12-21 15:36:27');
INSERT INTO `system_notices` VALUES (30, 8, 22, 1, 24, '用户2199 赞了你的笔记', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/18/bd814ecc9b0d403880099fe58ade567f.png', 0, '2025-12-21 15:36:30');
INSERT INTO `system_notices` VALUES (31, 8, 22, 3, 22, '用户2199 关注了你', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/avatars/2025/12/14/46580897d95f49cebe12dfbb9f663cf1.png', 0, '2025-12-21 15:40:57');
INSERT INTO `system_notices` VALUES (32, 8, 22, 1, 24, '用户2199 赞了你的笔记', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/18/bd814ecc9b0d403880099fe58ade567f.png', 0, '2025-12-21 15:40:59');
INSERT INTO `system_notices` VALUES (33, 28, 22, 1, 4, '用户2199 赞了你的笔记', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/66e7fc5bc0e14762ac21f9b6cc8f5d7a.png', 0, '2025-12-21 15:46:50');
INSERT INTO `system_notices` VALUES (34, 24, 22, 3, 22, '用户2199 关注了你', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/avatars/2025/12/14/46580897d95f49cebe12dfbb9f663cf1.png', 0, '2025-12-21 15:48:08');
INSERT INTO `system_notices` VALUES (35, 24, 22, 1, 18, '用户2199 赞了你的笔记', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/14/c55b325858484a839482c4f70395f1e5.png', 0, '2025-12-21 15:48:09');
INSERT INTO `system_notices` VALUES (36, 39, 22, 1, 23, '用户2199 赞了你的笔记', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/18/690700badb424a5aa5916c411bd660f4.png', 0, '2025-12-21 19:30:04');
INSERT INTO `system_notices` VALUES (37, 8, 42, 3, 42, '爱你羊肉 关注了你', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/avatar/2025/12/25/235cc26f38ed4ec49ecc093ed1301d00.png', 0, '2025-12-25 22:50:31');
INSERT INTO `system_notices` VALUES (38, 22, 42, 3, 42, '爱你羊肉 关注了你', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/avatar/2025/12/25/235cc26f38ed4ec49ecc093ed1301d00.png', 0, '2025-12-25 23:03:07');
INSERT INTO `system_notices` VALUES (39, 22, 42, 1, 10, '爱你羊肉 赞了你的笔记', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/05/58310d4b04b844bea24a35d9c70b2f48.jpg', 0, '2025-12-25 23:03:08');
INSERT INTO `system_notices` VALUES (40, 42, 22, 3, 22, '用户2199 关注了你', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/avatars/2025/12/14/46580897d95f49cebe12dfbb9f663cf1.png', 0, '2025-12-25 23:04:20');

-- ----------------------------
-- Table structure for topics
-- ----------------------------
DROP TABLE IF EXISTS `topics`;
CREATE TABLE `topics`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '话题ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '话题名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '话题描述',
  `cover_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面图',
  `note_count` int NOT NULL DEFAULT 0 COMMENT '笔记数量',
  `view_count` int NOT NULL DEFAULT 0 COMMENT '浏览量',
  `is_hot` tinyint NOT NULL DEFAULT 0 COMMENT '是否热门',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1启用，2禁用）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_name`(`name` ASC) USING BTREE,
  INDEX `idx_is_hot`(`is_hot` ASC) USING BTREE,
  INDEX `idx_note_count`(`note_count` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '话题表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of topics
-- ----------------------------
INSERT INTO `topics` VALUES (1, '美食探店', '分享你的美食体验', NULL, 0, 0, 1, 1, '2025-12-25 22:15:53', '2025-12-25 22:15:53');
INSERT INTO `topics` VALUES (2, '周末好去处', '周末休闲娱乐推荐', NULL, 0, 0, 1, 1, '2025-12-25 22:15:53', '2025-12-25 22:15:53');
INSERT INTO `topics` VALUES (3, '打卡圣地', '网红打卡地点分享', NULL, 0, 0, 1, 1, '2025-12-25 22:15:53', '2025-12-25 22:15:53');
INSERT INTO `topics` VALUES (4, '优惠活动', '商家优惠信息分享', NULL, 0, 0, 1, 1, '2025-12-25 22:15:53', '2025-12-25 22:15:53');
INSERT INTO `topics` VALUES (5, '新店开业', '新开业商家推荐', NULL, 3, 0, 1, 1, '2025-12-25 22:15:53', '2025-12-28 13:06:49');
INSERT INTO `topics` VALUES (6, '环境优美', '环境好的店铺推荐', NULL, 0, 0, 1, 1, '2025-12-25 22:15:53', '2025-12-25 22:15:53');
INSERT INTO `topics` VALUES (7, '服务贴心', '服务好的商家分享', NULL, 0, 0, 1, 1, '2025-12-25 22:15:53', '2025-12-25 22:15:53');
INSERT INTO `topics` VALUES (8, '性价比高', '高性价比商家推荐', NULL, 0, 0, 1, 1, '2025-12-25 22:15:53', '2025-12-25 22:15:53');
INSERT INTO `topics` VALUES (9, '约会圣地', '适合约会的地方', NULL, 0, 0, 1, 1, '2025-12-25 22:15:53', '2025-12-25 22:15:53');
INSERT INTO `topics` VALUES (10, '亲子好去处', '适合带孩子的地方', NULL, 0, 0, 1, 1, '2025-12-25 22:15:53', '2025-12-25 22:15:53');
INSERT INTO `topics` VALUES (11, '下午茶', '下午茶推荐', NULL, 0, 0, 1, 1, '2025-12-25 22:15:53', '2025-12-25 22:15:53');
INSERT INTO `topics` VALUES (12, '夜宵好去处', '夜宵美食推荐', NULL, 0, 0, 1, 1, '2025-12-25 22:15:53', '2025-12-25 22:15:53');
INSERT INTO `topics` VALUES (13, '健康养生', '健康养生类商家', NULL, 0, 0, 1, 1, '2025-12-25 22:15:53', '2025-12-25 22:15:53');
INSERT INTO `topics` VALUES (14, '美容美发', '美容美发推荐', NULL, 0, 0, 1, 1, '2025-12-25 22:15:53', '2025-12-25 22:15:53');
INSERT INTO `topics` VALUES (15, '休闲娱乐', '休闲娱乐场所', NULL, 0, 0, 1, 1, '2025-12-25 22:15:53', '2025-12-25 22:15:53');
INSERT INTO `topics` VALUES (16, '运动健身', '运动健身场所', NULL, 0, 0, 1, 1, '2025-12-25 22:15:53', '2025-12-25 22:15:53');
INSERT INTO `topics` VALUES (17, '文艺小资', '文艺范儿的店铺', NULL, 0, 0, 1, 1, '2025-12-25 22:15:53', '2025-12-25 22:15:53');
INSERT INTO `topics` VALUES (18, '网红店', '网红店铺推荐', NULL, 0, 0, 1, 1, '2025-12-25 22:15:53', '2025-12-25 22:15:53');
INSERT INTO `topics` VALUES (19, '老字号', '传统老字号推荐', NULL, 0, 0, 1, 1, '2025-12-25 22:15:53', '2025-12-25 22:15:53');
INSERT INTO `topics` VALUES (20, '特色小吃', '特色小吃推荐', NULL, 0, 0, 1, 1, '2025-12-25 22:15:53', '2025-12-25 22:15:53');
INSERT INTO `topics` VALUES (21, '探店专属', NULL, NULL, 1, 0, 0, 1, '2025-12-25 22:25:55', '2025-12-25 22:25:54');
INSERT INTO `topics` VALUES (22, '小动物', NULL, NULL, 1, 0, 0, 1, '2025-12-25 23:23:51', '2025-12-25 23:23:51');

-- ----------------------------
-- Table structure for user_browse_history
-- ----------------------------
DROP TABLE IF EXISTS `user_browse_history`;
CREATE TABLE `user_browse_history`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `target_type` tinyint NOT NULL COMMENT '目标类型（1笔记，2商家）',
  `target_id` bigint NOT NULL COMMENT '目标ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户浏览历史表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_browse_history
-- ----------------------------
INSERT INTO `user_browse_history` VALUES (1, 22, 1, 28, '2025-12-26 22:53:46');
INSERT INTO `user_browse_history` VALUES (2, 22, 1, 19, '2025-12-26 22:58:43');
INSERT INTO `user_browse_history` VALUES (3, 22, 1, 8, '2025-12-26 22:58:51');
INSERT INTO `user_browse_history` VALUES (4, 22, 1, 24, '2025-12-26 22:53:44');
INSERT INTO `user_browse_history` VALUES (5, 22, 1, 4, '2025-12-26 22:58:40');
INSERT INTO `user_browse_history` VALUES (6, 22, 1, 6, '2025-12-26 22:58:46');
INSERT INTO `user_browse_history` VALUES (7, 22, 1, 10, '2025-12-27 12:15:53');
INSERT INTO `user_browse_history` VALUES (8, 22, 1, 18, '2025-12-26 22:58:30');
INSERT INTO `user_browse_history` VALUES (9, 22, 1, 12, '2025-12-26 22:53:37');
INSERT INTO `user_browse_history` VALUES (10, 22, 1, 23, '2025-12-26 22:53:01');
INSERT INTO `user_browse_history` VALUES (11, 22, 1, 9, '2025-12-26 10:28:20');
INSERT INTO `user_browse_history` VALUES (12, 42, 1, 29, '2025-12-25 23:03:00');
INSERT INTO `user_browse_history` VALUES (13, 42, 1, 24, '2025-12-25 23:22:04');
INSERT INTO `user_browse_history` VALUES (14, 42, 1, 4, '2025-12-25 23:03:22');
INSERT INTO `user_browse_history` VALUES (15, 42, 1, 10, '2025-12-25 23:03:12');
INSERT INTO `user_browse_history` VALUES (16, 42, 1, 6, '2025-12-25 23:22:08');
INSERT INTO `user_browse_history` VALUES (17, 22, 1, 27, '2025-12-25 23:36:15');
INSERT INTO `user_browse_history` VALUES (18, 22, 1, 7, '2025-12-26 22:58:34');
INSERT INTO `user_browse_history` VALUES (19, 22, 1, 1, '2025-12-26 22:53:13');
INSERT INTO `user_browse_history` VALUES (20, 22, 1, 3, '2025-12-26 22:53:25');
INSERT INTO `user_browse_history` VALUES (21, 43, 1, 30, '2025-12-28 15:02:19');
INSERT INTO `user_browse_history` VALUES (22, 43, 1, 29, '2025-12-28 15:02:06');

-- ----------------------------
-- Table structure for user_comment_likes
-- ----------------------------
DROP TABLE IF EXISTS `user_comment_likes`;
CREATE TABLE `user_comment_likes`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `comment_id` bigint NOT NULL COMMENT '评论ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_comment`(`user_id` ASC, `comment_id` ASC) USING BTREE,
  INDEX `idx_comment_id`(`comment_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户点赞评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_comment_likes
-- ----------------------------

-- ----------------------------
-- Table structure for user_coupons
-- ----------------------------
DROP TABLE IF EXISTS `user_coupons`;
CREATE TABLE `user_coupons`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `coupon_id` bigint NOT NULL COMMENT '优惠券ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '优惠券码',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1未使用，2已使用，3已过期）',
  `receive_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
  `use_time` datetime NULL DEFAULT NULL COMMENT '使用时间',
  `use_shop_id` bigint NULL DEFAULT NULL COMMENT '使用店铺ID',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '核销操作员ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_code`(`code` ASC) USING BTREE,
  INDEX `idx_coupon_id`(`coupon_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_receive_time`(`receive_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户优惠券表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_coupons
-- ----------------------------
INSERT INTO `user_coupons` VALUES (1, 2, 22, '55DA68BB200A', 1, '2025-12-22 11:25:40', NULL, NULL, NULL);
INSERT INTO `user_coupons` VALUES (2, 1, 22, 'EF5D33059BCB', 1, '2025-12-22 15:07:44', NULL, NULL, NULL);
INSERT INTO `user_coupons` VALUES (3, 3, 22, '032462E7F721', 1, '2025-12-24 15:49:23', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for user_favorites
-- ----------------------------
DROP TABLE IF EXISTS `user_favorites`;
CREATE TABLE `user_favorites`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `type` tinyint NOT NULL COMMENT '收藏类型（1=笔记，2=商家）',
  `target_id` bigint NOT NULL COMMENT '目标ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_type_target`(`user_id` ASC, `type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_target_id`(`target_id` ASC) USING BTREE,
  INDEX `idx_type`(`type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 82 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_favorites
-- ----------------------------
INSERT INTO `user_favorites` VALUES (2, 29, 1, 5, '2025-12-05 00:01:08');
INSERT INTO `user_favorites` VALUES (3, 28, 1, 4, '2025-12-05 00:01:32');
INSERT INTO `user_favorites` VALUES (4, 28, 1, 1, '2025-12-05 00:03:40');
INSERT INTO `user_favorites` VALUES (12, 32, 1, 6, '2025-12-05 15:43:05');
INSERT INTO `user_favorites` VALUES (13, 32, 1, 9, '2025-12-05 15:47:43');
INSERT INTO `user_favorites` VALUES (14, 32, 1, 4, '2025-12-05 15:47:48');
INSERT INTO `user_favorites` VALUES (24, 32, 1, 2, '2025-12-05 16:09:27');
INSERT INTO `user_favorites` VALUES (44, 24, 1, 6, '2025-12-12 18:53:11');
INSERT INTO `user_favorites` VALUES (45, 24, 1, 10, '2025-12-12 18:53:49');
INSERT INTO `user_favorites` VALUES (47, 24, 1, 4, '2025-12-12 18:55:39');
INSERT INTO `user_favorites` VALUES (61, 3, 1, 22, '2025-12-18 18:48:51');
INSERT INTO `user_favorites` VALUES (63, 39, 1, 24, '2025-12-18 21:17:23');
INSERT INTO `user_favorites` VALUES (64, 39, 1, 25, '2025-12-18 22:01:33');
INSERT INTO `user_favorites` VALUES (76, 42, 1, 10, '2025-12-25 23:03:09');
INSERT INTO `user_favorites` VALUES (78, 43, 1, 30, '2025-12-28 15:02:22');

-- ----------------------------
-- Table structure for user_follows
-- ----------------------------
DROP TABLE IF EXISTS `user_follows`;
CREATE TABLE `user_follows`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '关注者ID',
  `follow_user_id` bigint NOT NULL COMMENT '被关注者ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_follow`(`user_id` ASC, `follow_user_id` ASC) USING BTREE,
  INDEX `idx_follow_user_id`(`follow_user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 45 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '关注关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_follows
-- ----------------------------
INSERT INTO `user_follows` VALUES (4, 32, 29, '2025-12-05 15:43:07');
INSERT INTO `user_follows` VALUES (7, 32, 27, '2025-12-05 15:57:34');
INSERT INTO `user_follows` VALUES (8, 32, 28, '2025-12-05 15:57:41');
INSERT INTO `user_follows` VALUES (9, 32, 22, '2025-12-05 15:57:44');
INSERT INTO `user_follows` VALUES (10, 24, 29, '2025-12-12 18:56:09');
INSERT INTO `user_follows` VALUES (20, 24, 32, '2025-12-14 22:45:57');
INSERT INTO `user_follows` VALUES (22, 24, 28, '2025-12-14 22:51:55');
INSERT INTO `user_follows` VALUES (24, 22, 28, '2025-12-14 23:23:52');
INSERT INTO `user_follows` VALUES (25, 22, 29, '2025-12-14 23:23:54');
INSERT INTO `user_follows` VALUES (29, 27, 22, '2025-12-14 23:29:42');
INSERT INTO `user_follows` VALUES (31, 22, 32, '2025-12-18 14:36:42');
INSERT INTO `user_follows` VALUES (32, 22, 27, '2025-12-18 14:36:44');
INSERT INTO `user_follows` VALUES (37, 39, 8, '2025-12-18 21:17:02');
INSERT INTO `user_follows` VALUES (38, 22, 3, '2025-12-20 12:27:29');
INSERT INTO `user_follows` VALUES (39, 24, 22, '2025-12-20 12:28:13');
INSERT INTO `user_follows` VALUES (40, 22, 8, '2025-12-21 15:40:57');
INSERT INTO `user_follows` VALUES (41, 22, 24, '2025-12-21 15:48:08');
INSERT INTO `user_follows` VALUES (42, 42, 8, '2025-12-25 22:50:31');
INSERT INTO `user_follows` VALUES (43, 42, 22, '2025-12-25 23:03:07');
INSERT INTO `user_follows` VALUES (44, 22, 42, '2025-12-25 23:04:20');

-- ----------------------------
-- Table structure for user_note_bookmarks
-- ----------------------------
DROP TABLE IF EXISTS `user_note_bookmarks`;
CREATE TABLE `user_note_bookmarks`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `note_id` bigint NOT NULL COMMENT '笔记ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_note`(`user_id` ASC, `note_id` ASC) USING BTREE,
  INDEX `idx_note_id`(`note_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户收藏笔记表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_note_bookmarks
-- ----------------------------

-- ----------------------------
-- Table structure for user_note_likes
-- ----------------------------
DROP TABLE IF EXISTS `user_note_likes`;
CREATE TABLE `user_note_likes`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `note_id` bigint NOT NULL COMMENT '笔记ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_note`(`user_id` ASC, `note_id` ASC) USING BTREE,
  INDEX `idx_note_id`(`note_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 125 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户点赞笔记表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_note_likes
-- ----------------------------
INSERT INTO `user_note_likes` VALUES (2, 29, 6, '2025-12-05 00:01:00');
INSERT INTO `user_note_likes` VALUES (4, 29, 5, '2025-12-05 00:01:09');
INSERT INTO `user_note_likes` VALUES (10, 28, 4, '2025-12-05 00:01:31');
INSERT INTO `user_note_likes` VALUES (27, 32, 6, '2025-12-05 15:43:02');
INSERT INTO `user_note_likes` VALUES (28, 32, 9, '2025-12-05 15:47:43');
INSERT INTO `user_note_likes` VALUES (29, 32, 4, '2025-12-05 15:47:47');
INSERT INTO `user_note_likes` VALUES (30, 32, 3, '2025-12-05 15:58:00');
INSERT INTO `user_note_likes` VALUES (31, 32, 5, '2025-12-05 15:58:04');
INSERT INTO `user_note_likes` VALUES (32, 32, 2, '2025-12-05 16:08:49');
INSERT INTO `user_note_likes` VALUES (49, 33, 10, '2025-12-05 18:27:15');
INSERT INTO `user_note_likes` VALUES (73, 24, 4, '2025-12-12 18:55:41');
INSERT INTO `user_note_likes` VALUES (74, 24, 9, '2025-12-12 19:43:33');
INSERT INTO `user_note_likes` VALUES (86, 24, 10, '2025-12-15 10:19:26');
INSERT INTO `user_note_likes` VALUES (104, 3, 22, '2025-12-18 18:48:51');
INSERT INTO `user_note_likes` VALUES (107, 39, 25, '2025-12-18 22:01:33');
INSERT INTO `user_note_likes` VALUES (108, 39, 10, '2025-12-18 23:20:16');
INSERT INTO `user_note_likes` VALUES (123, 42, 10, '2025-12-25 23:03:08');
INSERT INTO `user_note_likes` VALUES (124, 43, 30, '2025-12-28 15:02:21');

-- ----------------------------
-- Table structure for user_online_status
-- ----------------------------
DROP TABLE IF EXISTS `user_online_status`;
CREATE TABLE `user_online_status`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `is_online` tinyint NULL DEFAULT 0 COMMENT '是否在线：0=离线，1=在线',
  `last_online_time` datetime NULL DEFAULT NULL COMMENT '最后在线时间',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户在线状态表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_online_status
-- ----------------------------

-- ----------------------------
-- Table structure for user_seckill_records
-- ----------------------------
DROP TABLE IF EXISTS `user_seckill_records`;
CREATE TABLE `user_seckill_records`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `seckill_id` bigint NOT NULL COMMENT '秒杀活动ID',
  `coupon_id` bigint NOT NULL COMMENT '优惠券ID',
  `user_coupon_id` bigint NOT NULL COMMENT '用户优惠券ID',
  `seckill_price` decimal(10, 2) NOT NULL COMMENT '秒杀价格',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '抢购时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_seckill_id`(`seckill_id` ASC) USING BTREE,
  INDEX `idx_coupon_id`(`coupon_id` ASC) USING BTREE,
  INDEX `idx_user_coupon_id`(`user_coupon_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户秒杀记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_seckill_records
-- ----------------------------

-- ----------------------------
-- Table structure for user_shop_favorites
-- ----------------------------
DROP TABLE IF EXISTS `user_shop_favorites`;
CREATE TABLE `user_shop_favorites`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `shop_id` bigint NOT NULL COMMENT '商家ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_shop`(`user_id` ASC, `shop_id` ASC) USING BTREE,
  INDEX `idx_shop_id`(`shop_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户收藏商家表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_shop_favorites
-- ----------------------------

-- ----------------------------
-- Table structure for user_stats
-- ----------------------------
DROP TABLE IF EXISTS `user_stats`;
CREATE TABLE `user_stats`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `following_count` int NOT NULL DEFAULT 0 COMMENT '关注数',
  `follower_count` int NOT NULL DEFAULT 0 COMMENT '粉丝数',
  `like_count` int NOT NULL DEFAULT 0 COMMENT '获赞总数',
  `favorite_count` int NOT NULL DEFAULT 0 COMMENT '收藏总数',
  `note_count` int NOT NULL DEFAULT 0 COMMENT '笔记总数',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 37 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户统计表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_stats
-- ----------------------------
INSERT INTO `user_stats` VALUES (8, 8, 0, 3, 0, 0, 0, '2025-12-03 23:22:47', '2025-12-26 22:53:44');
INSERT INTO `user_stats` VALUES (9, 9, 0, 0, 0, 0, 0, '2025-12-03 23:25:11', '2025-12-03 23:25:11');
INSERT INTO `user_stats` VALUES (10, 10, 0, 0, 0, 0, 0, '2025-12-03 23:43:40', '2025-12-03 23:43:40');
INSERT INTO `user_stats` VALUES (11, 11, 0, 0, 0, 0, 0, '2025-12-03 23:53:19', '2025-12-03 23:53:19');
INSERT INTO `user_stats` VALUES (12, 12, 0, 0, 0, 0, 0, '2025-12-04 00:02:05', '2025-12-04 00:02:05');
INSERT INTO `user_stats` VALUES (13, 13, 0, 0, 0, 0, 0, '2025-12-04 00:02:20', '2025-12-04 00:02:20');
INSERT INTO `user_stats` VALUES (14, 14, 0, 0, 0, 0, 0, '2025-12-04 09:23:09', '2025-12-04 09:23:09');
INSERT INTO `user_stats` VALUES (15, 15, 0, 0, 0, 0, 0, '2025-12-04 11:29:03', '2025-12-04 11:29:03');
INSERT INTO `user_stats` VALUES (16, 16, 0, 0, 0, 0, 0, '2025-12-04 12:52:02', '2025-12-04 12:52:02');
INSERT INTO `user_stats` VALUES (17, 17, 0, 0, 0, 0, 0, '2025-12-04 13:35:27', '2025-12-04 13:35:27');
INSERT INTO `user_stats` VALUES (18, 18, 0, 0, 0, 0, 0, '2025-12-04 13:40:54', '2025-12-04 13:40:54');
INSERT INTO `user_stats` VALUES (19, 19, 0, 0, 0, 0, 0, '2025-12-04 17:20:28', '2025-12-04 17:20:28');
INSERT INTO `user_stats` VALUES (20, 20, 0, 0, 0, 0, 0, '2025-12-04 19:07:55', '2025-12-04 19:07:55');
INSERT INTO `user_stats` VALUES (21, 21, 0, 0, 0, 0, 0, '2025-12-04 19:10:46', '2025-12-04 19:10:46');
INSERT INTO `user_stats` VALUES (22, 22, 8, 4, 5, 0, 10, '2025-12-04 19:15:52', '2025-12-26 22:58:56');
INSERT INTO `user_stats` VALUES (23, 23, 0, 0, 0, 0, 0, '2025-12-04 19:28:37', '2025-12-04 19:28:37');
INSERT INTO `user_stats` VALUES (24, 24, 4, 1, 0, 3, 3, '2025-12-04 19:31:26', '2025-12-26 22:53:38');
INSERT INTO `user_stats` VALUES (25, 25, 0, 0, 0, 0, 0, '2025-12-04 20:23:52', '2025-12-04 20:23:52');
INSERT INTO `user_stats` VALUES (26, 26, 0, 0, 0, 0, 0, '2025-12-04 21:38:22', '2025-12-04 21:38:22');
INSERT INTO `user_stats` VALUES (27, 27, 1, 2, 1, 0, 2, '2025-12-04 23:11:58', '2025-12-26 22:53:22');
INSERT INTO `user_stats` VALUES (28, 28, 0, 3, 3, 2, 1, '2025-12-04 23:21:45', '2025-12-26 22:53:49');
INSERT INTO `user_stats` VALUES (29, 29, 0, 3, 4, 1, 2, '2025-12-04 23:24:06', '2025-12-21 15:46:53');
INSERT INTO `user_stats` VALUES (30, 30, 0, 0, 0, 0, 0, '2025-12-05 00:05:47', '2025-12-05 00:05:47');
INSERT INTO `user_stats` VALUES (31, 31, 0, 0, 0, 0, 0, '2025-12-05 00:13:32', '2025-12-05 00:13:32');
INSERT INTO `user_stats` VALUES (32, 32, 4, 2, 2, 4, 1, '2025-12-05 15:42:43', '2025-12-18 14:36:41');
INSERT INTO `user_stats` VALUES (33, 33, 0, 0, 0, 0, 0, '2025-12-05 18:26:51', '2025-12-05 18:26:51');
INSERT INTO `user_stats` VALUES (34, 34, 0, 0, 0, 0, 0, '2025-12-12 12:01:13', '2025-12-12 12:01:13');
INSERT INTO `user_stats` VALUES (35, 40, 0, 0, 0, 0, 0, '2025-12-18 22:04:10', '2025-12-18 22:04:10');
INSERT INTO `user_stats` VALUES (36, 41, 0, 0, 0, 0, 0, '2025-12-22 15:33:30', '2025-12-22 15:33:30');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '手机号',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `bio` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '个人简介',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码（加密）',
  `wechat_openid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '微信OpenID',
  `qq_openid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'QQ OpenID',
  `weibo_uid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '微博UID',
  `gender` tinyint NULL DEFAULT 0 COMMENT '性别（0未知，1男，2女）',
  `birthday` date NULL DEFAULT NULL COMMENT '生日',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1正常，2禁用）',
  `last_login_at` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 44 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, '16750152199_1', '小品烧烤官方', 'https://via.placeholder.com/100', '小品烧烤的官方账号', '123456', NULL, NULL, NULL, 0, NULL, 1, NULL, '2025-12-14 21:57:33', '2025-12-20 21:38:47');
INSERT INTO `users` VALUES (2, '18354763214', '张亮麻辣烫官方', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/avatars/2025/12/18/57850de5a4ae4f5483264c12fc324507.png', '张亮麻辣烫的官方账号', NULL, NULL, NULL, NULL, 0, NULL, 1, '2025-12-18 23:21:41', '2025-12-14 22:04:49', '2025-12-14 22:04:49');
INSERT INTO `users` VALUES (3, '15216091650', '蜜雪冰城官方', 'https://via.placeholder.com/100', '蜜雪冰城的官方账号', NULL, NULL, NULL, NULL, 0, NULL, 1, '2025-12-18 18:48:39', '2025-12-18 17:37:37', '2025-12-18 17:37:37');
INSERT INTO `users` VALUES (8, '18987934526_8', '天生祥官方', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/logo/2025/12/18/c2c77baa7be446f6911672ba2373fc41.png', '天生祥的官方账号', NULL, NULL, NULL, NULL, 0, NULL, 1, NULL, '2025-12-18 21:16:17', '2025-12-18 21:16:17');
INSERT INTO `users` VALUES (10, '19879634521_10', '南阳大师傅官方', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/logo/2025/12/28/999d28a6fdb14acfb7f658c083294ad5.png', '南阳大师傅的官方账号', NULL, NULL, NULL, NULL, 0, NULL, 1, NULL, '2025-12-28 13:07:34', '2025-12-28 13:07:34');
INSERT INTO `users` VALUES (22, '16750152199', '用户2199', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/avatars/2025/12/14/46580897d95f49cebe12dfbb9f663cf1.png', '我喜欢吃', '123456', '', '', '', 0, '2003-09-24', 1, '2025-12-29 10:23:37', '2025-12-04 19:15:52', '2025-12-04 19:15:52');
INSERT INTO `users` VALUES (23, '18975635432', '用户5432', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head1.png', '大家好,想吃好吃的跟着我', '123456', '', '', '', 1, '2004-06-01', 1, '2025-12-04 20:19:23', '2025-12-04 19:28:37', '2025-12-04 19:28:37');
INSERT INTO `users` VALUES (24, '17090097798', '7798', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head1.png', '大家好我喜欢吃肉', '17090097798', '', '', '', 1, '2003-07-01', 1, '2025-12-28 16:09:17', '2025-12-04 19:31:26', '2025-12-04 19:31:26');
INSERT INTO `users` VALUES (25, '17459871478', '用户1478', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head6.png', NULL, '17459871478', NULL, NULL, NULL, 0, NULL, 1, '2025-12-04 20:27:48', '2025-12-04 20:23:52', '2025-12-04 20:23:52');
INSERT INTO `users` VALUES (26, '18765431245', '爱吃鱼', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head1.png', '大家好我喜欢吃鱼', '123456', '', '', '', 1, '1999-01-01', 1, '2025-12-11 18:12:19', '2025-12-04 21:38:22', '2025-12-04 21:57:55');
INSERT INTO `users` VALUES (27, '17863247596', '用户7596', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head10.png', NULL, '17863247596', NULL, NULL, NULL, 0, NULL, 1, '2025-12-14 23:29:23', '2025-12-04 23:11:58', '2025-12-04 23:11:58');
INSERT INTO `users` VALUES (28, '15374681234', '用户1234', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head9.png', NULL, '15374681234', NULL, NULL, NULL, 0, NULL, 1, '2025-12-04 23:21:45', '2025-12-04 23:21:45', '2025-12-04 23:21:45');
INSERT INTO `users` VALUES (29, '17584327894', '用户7894', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head2.png', NULL, '17584327894', NULL, NULL, NULL, 0, NULL, 1, '2025-12-04 23:24:06', '2025-12-04 23:24:06', '2025-12-04 23:24:06');
INSERT INTO `users` VALUES (30, '17895412134', '用户2134', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head1.png', NULL, '17895412134', NULL, NULL, NULL, 0, NULL, 1, '2025-12-05 00:05:47', '2025-12-05 00:05:47', '2025-12-05 00:05:47');
INSERT INTO `users` VALUES (31, '18963217896', '用户7896', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head1.png', NULL, '18963217896', NULL, NULL, NULL, 0, NULL, 1, '2025-12-05 00:13:32', '2025-12-05 00:13:32', '2025-12-05 00:13:32');
INSERT INTO `users` VALUES (32, '18847523697', '用户3697', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head7.png', '大家好我喜欢唱歌', '18847523697', '', '', '', 1, '2010-12-05', 1, '2025-12-05 16:10:30', '2025-12-05 15:42:43', '2025-12-05 15:42:43');
INSERT INTO `users` VALUES (33, '18978645321', '用户5321', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head2.png', NULL, '18978645321', NULL, NULL, NULL, 0, NULL, 1, '2025-12-05 18:26:51', '2025-12-05 18:26:51', '2025-12-05 18:26:51');
INSERT INTO `users` VALUES (34, '19877985431', '用户5431', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head1.png', NULL, '19877985431', NULL, NULL, NULL, 0, NULL, 1, '2025-12-12 12:01:13', '2025-12-12 12:01:13', '2025-12-12 12:01:13');
INSERT INTO `users` VALUES (39, '18987934526', '天生祥', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/avatar/2025/12/18/663481a49ee04f64926fa26f457047ed.png', '天生祥 官方账号', '123456', NULL, NULL, NULL, 0, NULL, 1, '2025-12-18 23:19:10', '2025-12-18 21:11:50', '2025-12-18 21:11:50');
INSERT INTO `users` VALUES (40, '13878945321', '用户5321', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head3.png', NULL, '123456', NULL, NULL, NULL, 0, NULL, 1, '2025-12-19 20:50:23', '2025-12-18 22:04:10', '2025-12-18 22:04:10');
INSERT INTO `users` VALUES (41, '18875342143', '用户2143', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head7.png', NULL, '18875342143', NULL, NULL, NULL, 0, NULL, 1, '2025-12-22 15:33:30', '2025-12-22 15:33:30', '2025-12-22 15:33:30');
INSERT INTO `users` VALUES (42, '18379634597', '爱你羊肉', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/avatar/2025/12/25/235cc26f38ed4ec49ecc093ed1301d00.png', '爱你羊肉 官方账号', '123456', NULL, NULL, NULL, 0, NULL, 1, '2025-12-25 20:02:24', '2025-12-25 20:01:26', '2025-12-25 20:01:26');
INSERT INTO `users` VALUES (43, '19879634521', '南阳大师傅', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/avatar/2025/12/28/5e2a2015198e45b3ab2b2fd11ecc21d4.png', '南阳大师傅 官方账号', '123456', NULL, NULL, NULL, 0, NULL, 1, '2025-12-28 13:05:30', '2025-12-28 13:03:53', '2025-12-28 13:03:53');

-- ----------------------------
-- Table structure for users_copy1
-- ----------------------------
DROP TABLE IF EXISTS `users_copy1`;
CREATE TABLE `users_copy1`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '手机号',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `bio` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '个人简介',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码（加密）',
  `wechat_openid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '微信OpenID',
  `qq_openid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'QQ OpenID',
  `weibo_uid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '微博UID',
  `gender` tinyint NULL DEFAULT 0 COMMENT '性别（0未知，1男，2女）',
  `birthday` date NULL DEFAULT NULL COMMENT '生日',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1正常，2禁用）',
  `last_login_at` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users_copy1
-- ----------------------------
INSERT INTO `users_copy1` VALUES (8, '16750152199', '用户7798', 'https://example.com/default-avatar.png', '大家', NULL, '', '', '', 1, '1990-01-01', 1, '2025-12-04 13:14:39', '2025-12-03 23:22:47', '2025-12-03 23:22:47');
INSERT INTO `users_copy1` VALUES (9, '13670985432', '用户5432', 'https://example.com/default-avatar.png', NULL, NULL, NULL, NULL, NULL, 0, NULL, 1, '2025-12-03 23:25:11', '2025-12-03 23:25:11', '2025-12-03 23:25:11');
INSERT INTO `users_copy1` VALUES (10, '16891234567', '用户4567', 'https://example.com/default-avatar.png', NULL, NULL, NULL, NULL, NULL, 0, NULL, 1, '2025-12-04 14:18:33', '2025-12-03 23:43:40', '2025-12-03 23:43:40');
INSERT INTO `users_copy1` VALUES (11, '16787451233', '用户1233', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/avatars/2025/12/04/fdbd65f861f148679a3cf3fe8a8e2a1d.png', NULL, NULL, NULL, NULL, NULL, 0, NULL, 1, '2025-12-04 17:42:14', '2025-12-03 23:53:19', '2025-12-03 23:53:19');
INSERT INTO `users_copy1` VALUES (12, '16750152197', '用户2197', 'https://example.com/default-avatar.png', NULL, NULL, NULL, NULL, NULL, 0, NULL, 1, '2025-12-04 00:02:05', '2025-12-04 00:02:05', '2025-12-04 00:02:05');
INSERT INTO `users_copy1` VALUES (13, '16750152198', '用户2198', 'https://example.com/default-avatar.png', NULL, NULL, NULL, NULL, NULL, 0, NULL, 1, '2025-12-04 00:02:20', '2025-12-04 00:02:20', '2025-12-04 00:02:20');
INSERT INTO `users_copy1` VALUES (14, '18899997777', '用户7777', 'https://example.com/default-avatar.png', NULL, NULL, NULL, NULL, NULL, 0, NULL, 1, '2025-12-04 14:12:09', '2025-12-04 09:23:09', '2025-12-04 09:23:09');
INSERT INTO `users_copy1` VALUES (15, '13896456321', '用户6321', 'https://example.com/default-avatar.png', '大家好呀\n', NULL, '', '', '', 1, '2003-01-01', 1, '2025-12-04 12:13:44', '2025-12-04 11:29:03', '2025-12-04 11:29:03');
INSERT INTO `users_copy1` VALUES (16, '17090097798', '用户7798', 'https://example.com/default-avatar.png', '喜欢吃', '123456', '', '', '', 1, '1990-01-01', 1, '2025-12-04 13:32:58', '2025-12-04 12:52:02', '2025-12-04 13:33:59');
INSERT INTO `users_copy1` VALUES (17, '18098987878', '用户', 'https://example.com/default-avatar.png', '喜欢吃', '123456', '', '', '', 1, '1990-01-01', 1, '2025-12-04 13:35:27', '2025-12-04 13:35:27', '2025-12-04 13:37:28');
INSERT INTO `users_copy1` VALUES (18, '17890097789', '用户12', '/static/avatars/2025/12/04/3895fc00abd740898d0a231cab06d4a1.png', '大家好我喜欢吃鱼', '12345678', '', '', '', 0, '2003-01-01', 1, '2025-12-04 14:19:41', '2025-12-04 13:40:54', '2025-12-04 17:41:55');
INSERT INTO `users_copy1` VALUES (19, '19878964735', '用户4735', '/static/avatars/2025/12/04/937195d25d1b441f8550c36d89bdb7e8.png', '大家好跟我一起分享美食', '19878964735', '', '', '', 1, '1990-02-02', 1, '2025-12-04 17:23:52', '2025-12-04 17:20:28', '2025-12-04 17:20:28');
INSERT INTO `users_copy1` VALUES (20, '15789687432', '用户7432', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head7.png', NULL, '15789687432', NULL, NULL, NULL, 0, NULL, 1, '2025-12-04 19:07:55', '2025-12-04 19:07:55', '2025-12-04 19:07:55');
INSERT INTO `users_copy1` VALUES (21, '17898745632', '用户5632', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head3.png', NULL, '17898745632', NULL, NULL, NULL, 0, NULL, 1, '2025-12-04 19:10:46', '2025-12-04 19:10:46', '2025-12-04 19:10:46');

-- ----------------------------
-- Table structure for users_copy2
-- ----------------------------
DROP TABLE IF EXISTS `users_copy2`;
CREATE TABLE `users_copy2`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '手机号',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `bio` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '个人简介',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码（加密）',
  `wechat_openid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '微信OpenID',
  `qq_openid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'QQ OpenID',
  `weibo_uid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '微博UID',
  `gender` tinyint NULL DEFAULT 0 COMMENT '性别（0未知，1男，2女）',
  `birthday` date NULL DEFAULT NULL COMMENT '生日',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1正常，2禁用）',
  `last_login_at` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users_copy2
-- ----------------------------
INSERT INTO `users_copy2` VALUES (8, '16750152199', '用户7798', 'https://example.com/default-avatar.png', '大家', NULL, '', '', '', 1, '1990-01-01', 1, '2025-12-04 13:14:39', '2025-12-03 23:22:47', '2025-12-03 23:22:47');
INSERT INTO `users_copy2` VALUES (9, '13670985432', '用户5432', 'https://example.com/default-avatar.png', NULL, NULL, NULL, NULL, NULL, 0, NULL, 1, '2025-12-03 23:25:11', '2025-12-03 23:25:11', '2025-12-03 23:25:11');
INSERT INTO `users_copy2` VALUES (10, '16891234567', '用户4567', 'https://example.com/default-avatar.png', NULL, NULL, NULL, NULL, NULL, 0, NULL, 1, '2025-12-04 14:18:33', '2025-12-03 23:43:40', '2025-12-03 23:43:40');
INSERT INTO `users_copy2` VALUES (11, '16787451233', '用户1233', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/avatars/2025/12/04/fdbd65f861f148679a3cf3fe8a8e2a1d.png', NULL, NULL, NULL, NULL, NULL, 0, NULL, 1, '2025-12-04 17:42:14', '2025-12-03 23:53:19', '2025-12-03 23:53:19');
INSERT INTO `users_copy2` VALUES (12, '16750152197', '用户2197', 'https://example.com/default-avatar.png', NULL, NULL, NULL, NULL, NULL, 0, NULL, 1, '2025-12-04 00:02:05', '2025-12-04 00:02:05', '2025-12-04 00:02:05');
INSERT INTO `users_copy2` VALUES (13, '16750152198', '用户2198', 'https://example.com/default-avatar.png', NULL, NULL, NULL, NULL, NULL, 0, NULL, 1, '2025-12-04 00:02:20', '2025-12-04 00:02:20', '2025-12-04 00:02:20');
INSERT INTO `users_copy2` VALUES (14, '18899997777', '用户7777', 'https://example.com/default-avatar.png', NULL, NULL, NULL, NULL, NULL, 0, NULL, 1, '2025-12-04 14:12:09', '2025-12-04 09:23:09', '2025-12-04 09:23:09');
INSERT INTO `users_copy2` VALUES (15, '13896456321', '用户6321', 'https://example.com/default-avatar.png', '大家好呀\n', NULL, '', '', '', 1, '2003-01-01', 1, '2025-12-04 12:13:44', '2025-12-04 11:29:03', '2025-12-04 11:29:03');
INSERT INTO `users_copy2` VALUES (16, '17090097798', '用户7798', 'https://example.com/default-avatar.png', '喜欢吃', '123456', '', '', '', 1, '1990-01-01', 1, '2025-12-04 13:32:58', '2025-12-04 12:52:02', '2025-12-04 13:33:59');
INSERT INTO `users_copy2` VALUES (17, '18098987878', '用户', 'https://example.com/default-avatar.png', '喜欢吃', '123456', '', '', '', 1, '1990-01-01', 1, '2025-12-04 13:35:27', '2025-12-04 13:35:27', '2025-12-04 13:37:28');
INSERT INTO `users_copy2` VALUES (18, '17890097789', '用户12', '/static/avatars/2025/12/04/3895fc00abd740898d0a231cab06d4a1.png', '大家好我喜欢吃鱼', '12345678', '', '', '', 0, '2003-01-01', 1, '2025-12-04 14:19:41', '2025-12-04 13:40:54', '2025-12-04 17:41:55');
INSERT INTO `users_copy2` VALUES (19, '19878964735', '用户4735', '/static/avatars/2025/12/04/937195d25d1b441f8550c36d89bdb7e8.png', '大家好跟我一起分享美食', '19878964735', '', '', '', 1, '1990-02-02', 1, '2025-12-04 17:23:52', '2025-12-04 17:20:28', '2025-12-04 17:20:28');
INSERT INTO `users_copy2` VALUES (20, '15789687432', '用户7432', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head7.png', NULL, '15789687432', NULL, NULL, NULL, 0, NULL, 1, '2025-12-04 19:07:55', '2025-12-04 19:07:55', '2025-12-04 19:07:55');
INSERT INTO `users_copy2` VALUES (21, '17898745632', '用户5632', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head3.png', NULL, '17898745632', NULL, NULL, NULL, 0, NULL, 1, '2025-12-04 19:10:46', '2025-12-04 19:10:46', '2025-12-04 19:10:46');

-- ----------------------------
-- Table structure for users_copy3
-- ----------------------------
DROP TABLE IF EXISTS `users_copy3`;
CREATE TABLE `users_copy3`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '手机号',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `bio` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '个人简介',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码（加密）',
  `wechat_openid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '微信OpenID',
  `qq_openid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'QQ OpenID',
  `weibo_uid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '微博UID',
  `gender` tinyint NULL DEFAULT 0 COMMENT '性别（0未知，1男，2女）',
  `birthday` date NULL DEFAULT NULL COMMENT '生日',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1正常，2禁用）',
  `last_login_at` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users_copy3
-- ----------------------------

-- ----------------------------
-- Table structure for verification_codes
-- ----------------------------
DROP TABLE IF EXISTS `verification_codes`;
CREATE TABLE `verification_codes`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '手机号',
  `code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '验证码',
  `code_type` tinyint NOT NULL DEFAULT 1 COMMENT '类型（1登录，2注册，3重置密码）',
  `is_used` tinyint NOT NULL DEFAULT 0 COMMENT '是否已使用',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_phone_code`(`phone` ASC, `code` ASC) USING BTREE,
  INDEX `idx_expire_time`(`expire_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 186 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '验证码表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of verification_codes
-- ----------------------------
INSERT INTO `verification_codes` VALUES (1, '18897854122', '275789', 1, 0, '2025-12-03 23:01:29', '2025-12-03 22:56:29');
INSERT INTO `verification_codes` VALUES (2, '18897854122', '432577', 1, 0, '2025-12-03 23:05:39', '2025-12-03 23:00:39');
INSERT INTO `verification_codes` VALUES (3, '17895213122', '763554', 1, 0, '2025-12-03 23:09:35', '2025-12-03 23:04:35');
INSERT INTO `verification_codes` VALUES (4, '16750152199', '738536', 1, 0, '2025-12-03 23:27:12', '2025-12-03 23:22:12');
INSERT INTO `verification_codes` VALUES (5, '13670985432', '870590', 1, 0, '2025-12-03 23:29:50', '2025-12-03 23:24:50');
INSERT INTO `verification_codes` VALUES (6, '16750152199', '778134', 1, 0, '2025-12-03 23:35:12', '2025-12-03 23:30:12');
INSERT INTO `verification_codes` VALUES (7, '16750152199', '502133', 1, 0, '2025-12-03 23:43:57', '2025-12-03 23:38:57');
INSERT INTO `verification_codes` VALUES (8, '16891234567', '491601', 1, 0, '2025-12-03 23:47:57', '2025-12-03 23:42:57');
INSERT INTO `verification_codes` VALUES (9, '16787451233', '010827', 1, 0, '2025-12-03 23:57:44', '2025-12-03 23:52:44');
INSERT INTO `verification_codes` VALUES (10, '16750152199', '942007', 1, 0, '2025-12-04 00:03:47', '2025-12-03 23:58:47');
INSERT INTO `verification_codes` VALUES (11, '16750152199', '447360', 1, 0, '2025-12-04 00:06:29', '2025-12-04 00:01:29');
INSERT INTO `verification_codes` VALUES (12, '16750152198', '471546', 1, 0, '2025-12-04 00:06:30', '2025-12-04 00:01:30');
INSERT INTO `verification_codes` VALUES (13, '16750152197', '809652', 1, 0, '2025-12-04 00:06:33', '2025-12-04 00:01:33');
INSERT INTO `verification_codes` VALUES (14, '16750152199', '893674', 1, 0, '2025-12-04 00:07:31', '2025-12-04 00:02:31');
INSERT INTO `verification_codes` VALUES (15, '18899997777', '720652', 1, 0, '2025-12-04 09:27:44', '2025-12-04 09:22:44');
INSERT INTO `verification_codes` VALUES (16, '16750152199', '685845', 1, 0, '2025-12-04 11:12:53', '2025-12-04 11:07:53');
INSERT INTO `verification_codes` VALUES (17, '13896456321', '782937', 1, 0, '2025-12-04 11:33:15', '2025-12-04 11:28:15');
INSERT INTO `verification_codes` VALUES (18, '16750152199', '116119', 1, 0, '2025-12-04 11:34:45', '2025-12-04 11:29:45');
INSERT INTO `verification_codes` VALUES (19, '16750152199', '096603', 1, 0, '2025-12-04 11:55:35', '2025-12-04 11:50:35');
INSERT INTO `verification_codes` VALUES (20, '16750152199', '764556', 1, 0, '2025-12-04 12:03:39', '2025-12-04 11:58:39');
INSERT INTO `verification_codes` VALUES (21, '16750152199', '986523', 1, 0, '2025-12-04 12:09:47', '2025-12-04 12:04:47');
INSERT INTO `verification_codes` VALUES (22, '16750152199', '610161', 1, 0, '2025-12-04 12:16:10', '2025-12-04 12:11:10');
INSERT INTO `verification_codes` VALUES (23, '13896456321', '420823', 1, 0, '2025-12-04 12:18:27', '2025-12-04 12:13:27');
INSERT INTO `verification_codes` VALUES (24, '17090097798', '638720', 1, 0, '2025-12-04 12:56:24', '2025-12-04 12:51:25');
INSERT INTO `verification_codes` VALUES (25, '17090097798', '814733', 1, 0, '2025-12-04 13:04:30', '2025-12-04 12:59:30');
INSERT INTO `verification_codes` VALUES (26, '17090097798', '896779', 1, 0, '2025-12-04 13:06:25', '2025-12-04 13:01:25');
INSERT INTO `verification_codes` VALUES (27, '17090097798', '518349', 1, 0, '2025-12-04 13:07:40', '2025-12-04 13:02:40');
INSERT INTO `verification_codes` VALUES (28, '16750152199', '466353', 1, 0, '2025-12-04 13:08:19', '2025-12-04 13:03:19');
INSERT INTO `verification_codes` VALUES (29, '17090097798', '296488', 1, 0, '2025-12-04 13:17:15', '2025-12-04 13:12:15');
INSERT INTO `verification_codes` VALUES (30, '16750152199', '209101', 1, 0, '2025-12-04 13:19:23', '2025-12-04 13:14:23');
INSERT INTO `verification_codes` VALUES (31, '17090097798', '772807', 1, 0, '2025-12-04 13:21:31', '2025-12-04 13:16:31');
INSERT INTO `verification_codes` VALUES (32, '17090097798', '515653', 1, 0, '2025-12-04 13:22:12', '2025-12-04 13:17:12');
INSERT INTO `verification_codes` VALUES (33, '17090097798', '942646', 1, 0, '2025-12-04 13:26:54', '2025-12-04 13:21:54');
INSERT INTO `verification_codes` VALUES (34, '17090097798', '375079', 1, 0, '2025-12-04 13:31:30', '2025-12-04 13:26:30');
INSERT INTO `verification_codes` VALUES (35, '17090097798', '025296', 1, 0, '2025-12-04 13:32:06', '2025-12-04 13:27:06');
INSERT INTO `verification_codes` VALUES (36, '17090097798', '357827', 1, 0, '2025-12-04 13:37:48', '2025-12-04 13:32:48');
INSERT INTO `verification_codes` VALUES (37, '17090097798', '452350', 1, 0, '2025-12-04 13:38:12', '2025-12-04 13:33:12');
INSERT INTO `verification_codes` VALUES (38, '18098987878', '671817', 1, 0, '2025-12-04 13:40:19', '2025-12-04 13:35:19');
INSERT INTO `verification_codes` VALUES (39, '18098987878', '659397', 1, 0, '2025-12-04 13:41:25', '2025-12-04 13:36:25');
INSERT INTO `verification_codes` VALUES (40, '17890097789', '129240', 1, 0, '2025-12-04 13:45:42', '2025-12-04 13:40:42');
INSERT INTO `verification_codes` VALUES (41, '17890097789', '873387', 1, 0, '2025-12-04 13:55:16', '2025-12-04 13:50:16');
INSERT INTO `verification_codes` VALUES (42, '17890097789', '780115', 1, 0, '2025-12-04 13:56:36', '2025-12-04 13:51:36');
INSERT INTO `verification_codes` VALUES (43, '17890097789', '062696', 1, 0, '2025-12-04 14:03:41', '2025-12-04 13:58:41');
INSERT INTO `verification_codes` VALUES (44, '16787451233', '061554', 1, 0, '2025-12-04 14:06:54', '2025-12-04 14:01:54');
INSERT INTO `verification_codes` VALUES (45, '18899997777', '424284', 1, 0, '2025-12-04 14:16:57', '2025-12-04 14:11:57');
INSERT INTO `verification_codes` VALUES (46, '16891234567', '812932', 1, 0, '2025-12-04 14:23:15', '2025-12-04 14:18:15');
INSERT INTO `verification_codes` VALUES (47, '17890097789', '646784', 1, 0, '2025-12-04 14:24:30', '2025-12-04 14:19:30');
INSERT INTO `verification_codes` VALUES (48, '19878964735', '302722', 1, 0, '2025-12-04 17:25:15', '2025-12-04 17:20:15');
INSERT INTO `verification_codes` VALUES (49, '19878964735', '433318', 1, 0, '2025-12-04 17:28:41', '2025-12-04 17:23:41');
INSERT INTO `verification_codes` VALUES (50, '16787451233', '863319', 1, 0, '2025-12-04 17:47:00', '2025-12-04 17:42:00');
INSERT INTO `verification_codes` VALUES (51, '15789687432', '648929', 1, 0, '2025-12-04 19:12:44', '2025-12-04 19:07:44');
INSERT INTO `verification_codes` VALUES (52, '17898745632', '969653', 1, 0, '2025-12-04 19:15:27', '2025-12-04 19:10:27');
INSERT INTO `verification_codes` VALUES (53, '16750152199', '213116', 1, 0, '2025-12-04 19:20:44', '2025-12-04 19:15:44');
INSERT INTO `verification_codes` VALUES (54, '16750152199', '925161', 1, 0, '2025-12-04 19:22:05', '2025-12-04 19:17:05');
INSERT INTO `verification_codes` VALUES (55, '16750152199', '343904', 1, 0, '2025-12-04 19:23:08', '2025-12-04 19:18:08');
INSERT INTO `verification_codes` VALUES (56, '16750152199', '395894', 1, 0, '2025-12-04 19:25:10', '2025-12-04 19:20:10');
INSERT INTO `verification_codes` VALUES (57, '18975635432', '904941', 1, 0, '2025-12-04 19:31:20', '2025-12-04 19:26:20');
INSERT INTO `verification_codes` VALUES (58, '18975635432', '462444', 1, 0, '2025-12-04 19:33:24', '2025-12-04 19:28:24');
INSERT INTO `verification_codes` VALUES (59, '18975635432', '808643', 1, 0, '2025-12-04 19:34:11', '2025-12-04 19:29:11');
INSERT INTO `verification_codes` VALUES (60, '17090097798', '074347', 1, 0, '2025-12-04 19:36:16', '2025-12-04 19:31:16');
INSERT INTO `verification_codes` VALUES (61, '17090097798', '730185', 1, 0, '2025-12-04 19:48:23', '2025-12-04 19:43:23');
INSERT INTO `verification_codes` VALUES (62, '17090097798', '973175', 1, 0, '2025-12-04 19:49:16', '2025-12-04 19:44:16');
INSERT INTO `verification_codes` VALUES (63, '18975635432', '076989', 1, 0, '2025-12-04 20:23:59', '2025-12-04 20:18:59');
INSERT INTO `verification_codes` VALUES (64, '16750152199', '508657', 1, 0, '2025-12-04 20:24:44', '2025-12-04 20:19:44');
INSERT INTO `verification_codes` VALUES (65, '16750152199', '092968', 1, 0, '2025-12-04 20:25:48', '2025-12-04 20:20:48');
INSERT INTO `verification_codes` VALUES (66, '16750152199', '767369', 1, 0, '2025-12-04 20:26:49', '2025-12-04 20:21:49');
INSERT INTO `verification_codes` VALUES (67, '17090097798', '927761', 1, 0, '2025-12-04 20:27:32', '2025-12-04 20:22:32');
INSERT INTO `verification_codes` VALUES (68, '17459871478', '667909', 1, 0, '2025-12-04 20:28:39', '2025-12-04 20:23:39');
INSERT INTO `verification_codes` VALUES (69, '17459871478', '853518', 1, 0, '2025-12-04 20:32:35', '2025-12-04 20:27:35');
INSERT INTO `verification_codes` VALUES (70, '16750152199', '585931', 1, 0, '2025-12-04 20:33:27', '2025-12-04 20:28:27');
INSERT INTO `verification_codes` VALUES (71, '17090097798', '015913', 1, 0, '2025-12-04 20:35:11', '2025-12-04 20:30:11');
INSERT INTO `verification_codes` VALUES (72, '17090097798', '477075', 1, 0, '2025-12-04 20:39:43', '2025-12-04 20:34:43');
INSERT INTO `verification_codes` VALUES (73, '16750152199', '105496', 1, 0, '2025-12-04 20:40:09', '2025-12-04 20:35:09');
INSERT INTO `verification_codes` VALUES (74, '19879854213', '971164', 1, 0, '2025-12-04 21:43:10', '2025-12-04 21:38:10');
INSERT INTO `verification_codes` VALUES (75, '16750152199', '172030', 1, 0, '2025-12-04 21:43:43', '2025-12-04 21:38:43');
INSERT INTO `verification_codes` VALUES (76, '18396745432', '942642', 1, 0, '2025-12-04 21:45:32', '2025-12-04 21:40:32');
INSERT INTO `verification_codes` VALUES (77, '19879854213', '714678', 4, 0, '2025-12-04 22:02:28', '2025-12-04 21:57:28');
INSERT INTO `verification_codes` VALUES (78, '18765431245', '843829', 5, 0, '2025-12-04 22:02:47', '2025-12-04 21:57:47');
INSERT INTO `verification_codes` VALUES (79, '18765431245', '195398', 1, 0, '2025-12-04 22:03:35', '2025-12-04 21:58:35');
INSERT INTO `verification_codes` VALUES (80, '18765431245', '964367', 1, 0, '2025-12-04 22:05:03', '2025-12-04 22:00:03');
INSERT INTO `verification_codes` VALUES (81, '16750152199', '507675', 1, 0, '2025-12-04 22:59:35', '2025-12-04 22:54:36');
INSERT INTO `verification_codes` VALUES (82, '17863247596', '026296', 1, 0, '2025-12-04 23:16:46', '2025-12-04 23:11:46');
INSERT INTO `verification_codes` VALUES (83, '15374681234', '736117', 1, 0, '2025-12-04 23:26:37', '2025-12-04 23:21:37');
INSERT INTO `verification_codes` VALUES (84, '17584327894', '163306', 1, 0, '2025-12-04 23:28:58', '2025-12-04 23:23:58');
INSERT INTO `verification_codes` VALUES (85, '16750152199', '984458', 1, 0, '2025-12-05 00:05:18', '2025-12-05 00:00:18');
INSERT INTO `verification_codes` VALUES (86, '18765431245', '793866', 1, 0, '2025-12-05 00:07:07', '2025-12-05 00:02:07');
INSERT INTO `verification_codes` VALUES (87, '17895412134', '822779', 1, 0, '2025-12-05 00:10:38', '2025-12-05 00:05:38');
INSERT INTO `verification_codes` VALUES (88, '16750152199', '516026', 1, 0, '2025-12-05 00:16:28', '2025-12-05 00:11:28');
INSERT INTO `verification_codes` VALUES (89, '17863247596', '638217', 1, 0, '2025-12-05 00:17:33', '2025-12-05 00:12:33');
INSERT INTO `verification_codes` VALUES (90, '18963217896', '772999', 1, 0, '2025-12-05 00:18:26', '2025-12-05 00:13:26');
INSERT INTO `verification_codes` VALUES (91, '16750152199', '051983', 1, 0, '2025-12-05 00:20:28', '2025-12-05 00:15:28');
INSERT INTO `verification_codes` VALUES (92, '16750152199', '451978', 1, 0, '2025-12-05 11:46:20', '2025-12-05 11:41:20');
INSERT INTO `verification_codes` VALUES (93, '16750152199', '229729', 1, 0, '2025-12-05 11:49:12', '2025-12-05 11:44:12');
INSERT INTO `verification_codes` VALUES (94, '16750152199', '988813', 1, 0, '2025-12-05 11:54:39', '2025-12-05 11:49:39');
INSERT INTO `verification_codes` VALUES (95, '17090097798', '801105', 1, 0, '2025-12-05 12:03:41', '2025-12-05 11:58:41');
INSERT INTO `verification_codes` VALUES (96, '17090097798', '197960', 1, 0, '2025-12-05 12:04:53', '2025-12-05 11:59:53');
INSERT INTO `verification_codes` VALUES (97, '17090097798', '802094', 1, 0, '2025-12-05 12:06:47', '2025-12-05 12:01:47');
INSERT INTO `verification_codes` VALUES (98, '17090097798', '435960', 1, 0, '2025-12-05 12:13:25', '2025-12-05 12:08:25');
INSERT INTO `verification_codes` VALUES (99, '16750152199', '635478', 1, 0, '2025-12-05 12:13:54', '2025-12-05 12:08:54');
INSERT INTO `verification_codes` VALUES (100, '16750152199', '345046', 1, 0, '2025-12-05 12:30:41', '2025-12-05 12:25:41');
INSERT INTO `verification_codes` VALUES (101, '16750152199', '693607', 1, 0, '2025-12-05 12:42:33', '2025-12-05 12:37:33');
INSERT INTO `verification_codes` VALUES (102, '16750152199', '010893', 1, 0, '2025-12-05 13:10:46', '2025-12-05 13:05:46');
INSERT INTO `verification_codes` VALUES (103, '16750152199', '224488', 1, 0, '2025-12-05 13:11:11', '2025-12-05 13:06:11');
INSERT INTO `verification_codes` VALUES (104, '16750152199', '224717', 1, 0, '2025-12-05 13:11:39', '2025-12-05 13:06:39');
INSERT INTO `verification_codes` VALUES (105, '16750152199', '939335', 1, 0, '2025-12-05 13:17:21', '2025-12-05 13:12:21');
INSERT INTO `verification_codes` VALUES (106, '16750152199', '457548', 1, 0, '2025-12-05 13:24:36', '2025-12-05 13:19:36');
INSERT INTO `verification_codes` VALUES (107, '16750152199', '369474', 1, 0, '2025-12-05 13:30:24', '2025-12-05 13:25:24');
INSERT INTO `verification_codes` VALUES (108, '16750152199', '046650', 1, 0, '2025-12-05 13:45:36', '2025-12-05 13:40:36');
INSERT INTO `verification_codes` VALUES (109, '16750152199', '013602', 1, 0, '2025-12-05 14:09:46', '2025-12-05 14:04:46');
INSERT INTO `verification_codes` VALUES (110, '18847523697', '666415', 1, 0, '2025-12-05 15:47:33', '2025-12-05 15:42:33');
INSERT INTO `verification_codes` VALUES (111, '18847523697', '940012', 4, 0, '2025-12-05 15:48:55', '2025-12-05 15:43:55');
INSERT INTO `verification_codes` VALUES (112, '16750152199', '202987', 5, 0, '2025-12-05 15:49:26', '2025-12-05 15:44:26');
INSERT INTO `verification_codes` VALUES (113, '18847523697', '791459', 1, 0, '2025-12-05 16:07:08', '2025-12-05 16:02:08');
INSERT INTO `verification_codes` VALUES (114, '18847523697', '771423', 1, 0, '2025-12-05 16:13:24', '2025-12-05 16:08:24');
INSERT INTO `verification_codes` VALUES (115, '18847523697', '244084', 1, 0, '2025-12-05 16:15:20', '2025-12-05 16:10:20');
INSERT INTO `verification_codes` VALUES (116, '16750152199', '796351', 1, 0, '2025-12-05 16:20:43', '2025-12-05 16:15:43');
INSERT INTO `verification_codes` VALUES (117, '18978645321', '638853', 1, 0, '2025-12-05 18:31:14', '2025-12-05 18:26:14');
INSERT INTO `verification_codes` VALUES (118, '16750152199', '377257', 1, 0, '2025-12-05 18:35:12', '2025-12-05 18:30:12');
INSERT INTO `verification_codes` VALUES (119, '16750152199', '118076', 1, 0, '2025-12-05 18:35:23', '2025-12-05 18:30:23');
INSERT INTO `verification_codes` VALUES (120, '16750152199', '322428', 1, 0, '2025-12-05 18:37:15', '2025-12-05 18:32:15');
INSERT INTO `verification_codes` VALUES (121, '16750152199', '352174', 1, 0, '2025-12-05 18:38:44', '2025-12-05 18:33:44');
INSERT INTO `verification_codes` VALUES (122, '16750152199', '991364', 1, 0, '2025-12-05 18:40:54', '2025-12-05 18:35:54');
INSERT INTO `verification_codes` VALUES (123, '16750152199', '532151', 1, 0, '2025-12-05 18:41:05', '2025-12-05 18:36:05');
INSERT INTO `verification_codes` VALUES (124, '16750152199', '754479', 1, 0, '2025-12-11 17:54:30', '2025-12-11 17:49:30');
INSERT INTO `verification_codes` VALUES (125, '17090097798', '128902', 1, 0, '2025-12-11 18:15:23', '2025-12-11 18:10:23');
INSERT INTO `verification_codes` VALUES (126, '18765431245', '854422', 1, 0, '2025-12-11 18:17:04', '2025-12-11 18:12:04');
INSERT INTO `verification_codes` VALUES (127, '16750152199', '781865', 1, 0, '2025-12-11 18:40:28', '2025-12-11 18:35:28');
INSERT INTO `verification_codes` VALUES (128, '17090097798', '628243', 1, 0, '2025-12-11 18:54:39', '2025-12-11 18:49:39');
INSERT INTO `verification_codes` VALUES (129, '17090097798', '098827', 1, 0, '2025-12-11 21:58:29', '2025-12-11 21:53:29');
INSERT INTO `verification_codes` VALUES (130, '17090097798', '960594', 1, 0, '2025-12-11 22:09:55', '2025-12-11 22:04:55');
INSERT INTO `verification_codes` VALUES (131, '17090097798', '096358', 1, 0, '2025-12-11 22:17:20', '2025-12-11 22:12:20');
INSERT INTO `verification_codes` VALUES (132, '17090097798', '481283', 1, 0, '2025-12-11 23:05:57', '2025-12-11 23:00:57');
INSERT INTO `verification_codes` VALUES (133, '17090097798', '155403', 1, 0, '2025-12-11 23:20:08', '2025-12-11 23:15:08');
INSERT INTO `verification_codes` VALUES (134, '17090097798', '166421', 1, 0, '2025-12-11 23:36:37', '2025-12-11 23:31:37');
INSERT INTO `verification_codes` VALUES (135, '16750152199', '212549', 1, 0, '2025-12-12 10:09:10', '2025-12-12 10:04:10');
INSERT INTO `verification_codes` VALUES (136, '17090097798', '551480', 1, 0, '2025-12-12 10:11:50', '2025-12-12 10:06:50');
INSERT INTO `verification_codes` VALUES (137, '16750152199', '849528', 1, 0, '2025-12-12 10:20:34', '2025-12-12 10:15:34');
INSERT INTO `verification_codes` VALUES (138, '16750152199', '767318', 1, 0, '2025-12-12 10:29:50', '2025-12-12 10:24:50');
INSERT INTO `verification_codes` VALUES (139, '16750152199', '204553', 1, 0, '2025-12-12 10:30:25', '2025-12-12 10:25:25');
INSERT INTO `verification_codes` VALUES (140, '16750152199', '777925', 1, 0, '2025-12-12 10:32:07', '2025-12-12 10:27:07');
INSERT INTO `verification_codes` VALUES (141, '16750152199', '028657', 1, 0, '2025-12-12 11:11:40', '2025-12-12 11:06:40');
INSERT INTO `verification_codes` VALUES (142, '17090097798', '922269', 1, 0, '2025-12-12 11:52:21', '2025-12-12 11:47:21');
INSERT INTO `verification_codes` VALUES (143, '17090097798', '789165', 1, 0, '2025-12-12 11:54:08', '2025-12-12 11:49:08');
INSERT INTO `verification_codes` VALUES (144, '17090097798', '330352', 1, 0, '2025-12-12 12:04:00', '2025-12-12 11:59:00');
INSERT INTO `verification_codes` VALUES (145, '19877985431', '775788', 1, 0, '2025-12-12 12:06:02', '2025-12-12 12:01:02');
INSERT INTO `verification_codes` VALUES (146, '17090097798', '570490', 1, 0, '2025-12-12 12:14:28', '2025-12-12 12:09:28');
INSERT INTO `verification_codes` VALUES (147, '17090097798', '334602', 1, 0, '2025-12-12 12:15:28', '2025-12-12 12:10:28');
INSERT INTO `verification_codes` VALUES (148, '17090097798', '889580', 1, 0, '2025-12-12 19:00:15', '2025-12-12 18:55:15');
INSERT INTO `verification_codes` VALUES (149, '16750152199', '723413', 1, 0, '2025-12-12 19:57:15', '2025-12-12 19:52:15');
INSERT INTO `verification_codes` VALUES (150, '16750152199', '320491', 1, 0, '2025-12-14 12:54:57', '2025-12-14 12:49:57');
INSERT INTO `verification_codes` VALUES (151, '16750152199', '864207', 1, 0, '2025-12-14 19:46:42', '2025-12-14 19:41:42');
INSERT INTO `verification_codes` VALUES (152, '16750152199', '022796', 1, 0, '2025-12-14 19:58:52', '2025-12-14 19:53:52');
INSERT INTO `verification_codes` VALUES (153, '16750152199', '609041', 1, 0, '2025-12-14 20:38:55', '2025-12-14 20:33:55');
INSERT INTO `verification_codes` VALUES (154, '16750152199', '809419', 1, 0, '2025-12-14 20:45:27', '2025-12-14 20:40:27');
INSERT INTO `verification_codes` VALUES (155, '16750152199', '695902', 1, 0, '2025-12-14 22:03:16', '2025-12-14 21:58:16');
INSERT INTO `verification_codes` VALUES (156, '17090097798', '387627', 1, 0, '2025-12-14 22:30:19', '2025-12-14 22:25:19');
INSERT INTO `verification_codes` VALUES (157, '17090097798', '234790', 1, 0, '2025-12-14 22:51:19', '2025-12-14 22:46:19');
INSERT INTO `verification_codes` VALUES (158, '17090097798', '507828', 1, 0, '2025-12-14 22:58:07', '2025-12-14 22:53:07');
INSERT INTO `verification_codes` VALUES (159, '16750152199', '394551', 1, 0, '2025-12-14 22:58:47', '2025-12-14 22:53:47');
INSERT INTO `verification_codes` VALUES (160, '17090097798', '415272', 1, 0, '2025-12-14 23:19:55', '2025-12-14 23:14:55');
INSERT INTO `verification_codes` VALUES (161, '17090097798', '601247', 1, 0, '2025-12-14 23:24:57', '2025-12-14 23:19:57');
INSERT INTO `verification_codes` VALUES (162, '16750152199', '615290', 1, 0, '2025-12-14 23:28:13', '2025-12-14 23:23:13');
INSERT INTO `verification_codes` VALUES (163, '17863247596', '769481', 1, 0, '2025-12-14 23:34:15', '2025-12-14 23:29:15');
INSERT INTO `verification_codes` VALUES (164, '16750152199', '767127', 1, 0, '2025-12-15 10:09:57', '2025-12-15 10:04:57');
INSERT INTO `verification_codes` VALUES (165, '17090097798', '396794', 1, 0, '2025-12-15 10:11:09', '2025-12-15 10:06:09');
INSERT INTO `verification_codes` VALUES (166, '16750152199', '578529', 1, 0, '2025-12-16 17:45:12', '2025-12-16 17:40:13');
INSERT INTO `verification_codes` VALUES (167, '16750152199', '745843', 1, 0, '2025-12-16 18:02:20', '2025-12-16 17:57:20');
INSERT INTO `verification_codes` VALUES (168, '16750152199', '808181', 1, 0, '2025-12-17 13:05:17', '2025-12-17 13:00:17');
INSERT INTO `verification_codes` VALUES (169, '17090097798', '803095', 1, 0, '2025-12-17 13:17:59', '2025-12-17 13:12:59');
INSERT INTO `verification_codes` VALUES (170, '16750152199', '124800', 1, 0, '2025-12-18 14:14:03', '2025-12-18 14:09:03');
INSERT INTO `verification_codes` VALUES (171, '16750152199', '876341', 1, 0, '2025-12-18 14:14:03', '2025-12-18 14:09:03');
INSERT INTO `verification_codes` VALUES (172, '16750152199', '479614', 1, 0, '2025-12-18 14:16:54', '2025-12-18 14:11:54');
INSERT INTO `verification_codes` VALUES (173, '16750152199', '478040', 1, 0, '2025-12-18 17:43:01', '2025-12-18 17:38:01');
INSERT INTO `verification_codes` VALUES (174, '15216091650', '425840', 1, 0, '2025-12-18 18:53:21', '2025-12-18 18:48:21');
INSERT INTO `verification_codes` VALUES (175, '18987934526', '733843', 1, 0, '2025-12-18 21:17:48', '2025-12-18 21:12:48');
INSERT INTO `verification_codes` VALUES (176, '18987934526', '078945', 1, 0, '2025-12-18 21:55:03', '2025-12-18 21:50:03');
INSERT INTO `verification_codes` VALUES (177, '13878945321', '622884', 1, 0, '2025-12-18 22:08:56', '2025-12-18 22:03:56');
INSERT INTO `verification_codes` VALUES (178, '13878945321', '684031', 1, 0, '2025-12-18 22:09:36', '2025-12-18 22:04:36');
INSERT INTO `verification_codes` VALUES (179, '18354763214', '077804', 1, 0, '2025-12-18 23:26:24', '2025-12-18 23:21:24');
INSERT INTO `verification_codes` VALUES (180, '16750152199', '993091', 1, 0, '2025-12-20 11:56:29', '2025-12-20 11:51:29');
INSERT INTO `verification_codes` VALUES (181, '16750152199', '796208', 1, 0, '2025-12-20 21:33:35', '2025-12-20 21:28:35');
INSERT INTO `verification_codes` VALUES (182, '16750152199', '510234', 1, 0, '2025-12-21 21:40:36', '2025-12-21 21:35:36');
INSERT INTO `verification_codes` VALUES (183, '16750152199', '022948', 1, 0, '2025-12-22 09:54:53', '2025-12-22 09:49:53');
INSERT INTO `verification_codes` VALUES (184, '16750152199', '922170', 1, 0, '2025-12-22 09:59:18', '2025-12-22 09:54:18');
INSERT INTO `verification_codes` VALUES (185, '18875342143', '008041', 1, 0, '2025-12-22 15:38:23', '2025-12-22 15:33:23');

-- ----------------------------
-- View structure for v_available_coupons
-- ----------------------------
DROP VIEW IF EXISTS `v_available_coupons`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_available_coupons` AS select `c`.`id` AS `id`,`c`.`merchant_id` AS `merchant_id`,`c`.`shop_id` AS `shop_id`,`c`.`type` AS `type`,`c`.`title` AS `title`,`c`.`description` AS `description`,`c`.`amount` AS `amount`,`c`.`discount` AS `discount`,`c`.`min_amount` AS `min_amount`,`c`.`total_count` AS `total_count`,`c`.`remain_count` AS `remain_count`,`c`.`per_user_limit` AS `per_user_limit`,`c`.`start_time` AS `start_time`,`c`.`end_time` AS `end_time`,`c`.`stackable` AS `stackable`,`c`.`status` AS `status`,`c`.`created_at` AS `created_at`,`c`.`updated_at` AS `updated_at`,(case when (`c`.`remain_count` <= 0) then '已抢光' when (now() < `c`.`start_time`) then '未开始' when (now() > `c`.`end_time`) then '已结束' when (`c`.`status` <> 1) then '已下架' else '可领取' end) AS `availability_status` from `coupons` `c` where ((`c`.`status` = 1) and (now() >= `c`.`start_time`) and (now() <= `c`.`end_time`) and (`c`.`remain_count` > 0));

-- ----------------------------
-- View structure for v_user_coupon_details
-- ----------------------------
DROP VIEW IF EXISTS `v_user_coupon_details`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_user_coupon_details` AS select `uc`.`id` AS `user_coupon_id`,`uc`.`user_id` AS `user_id`,`uc`.`code` AS `code`,`uc`.`status` AS `user_coupon_status`,`uc`.`receive_time` AS `receive_time`,`uc`.`use_time` AS `use_time`,`uc`.`use_shop_id` AS `use_shop_id`,`c`.`id` AS `coupon_id`,`c`.`merchant_id` AS `merchant_id`,`c`.`shop_id` AS `shop_id`,`c`.`type` AS `type`,`c`.`title` AS `title`,`c`.`description` AS `description`,`c`.`amount` AS `amount`,`c`.`discount` AS `discount`,`c`.`min_amount` AS `min_amount`,`c`.`start_time` AS `start_time`,`c`.`end_time` AS `end_time`,`c`.`stackable` AS `stackable`,`c`.`status` AS `coupon_status`,(case when (`uc`.`status` = 2) then '已使用' when (`uc`.`status` = 3) then '已过期' when (now() > `c`.`end_time`) then '已过期' else '未使用' end) AS `status_text` from (`user_coupons` `uc` join `coupons` `c` on((`uc`.`coupon_id` = `c`.`id`)));

-- ----------------------------
-- Procedure structure for sp_claim_coupon
-- ----------------------------
DROP PROCEDURE IF EXISTS `sp_claim_coupon`;
delimiter ;;
CREATE PROCEDURE `sp_claim_coupon`(IN p_user_id BIGINT,
  IN p_coupon_id BIGINT,
  OUT p_result INT,
  OUT p_message VARCHAR(200),
  OUT p_code VARCHAR(32))
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
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
