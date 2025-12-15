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

 Date: 15/12/2025 14:45:41
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
INSERT INTO `categories` VALUES (3, '丽人·美发', '💇', '#FF9E64', 3, 1, '2025-12-03 20:26:19', '2025-12-03 20:26:19');
INSERT INTO `categories` VALUES (4, '美睫·美甲', '💅', '#06D6A0', 4, 1, '2025-12-03 20:26:19', '2025-12-03 20:26:19');
INSERT INTO `categories` VALUES (5, '按摩·足疗', '💆', '#FFD166', 5, 1, '2025-12-03 20:26:19', '2025-12-03 20:26:19');
INSERT INTO `categories` VALUES (6, '美容SPA', '🛁', '#EF476F', 6, 1, '2025-12-03 20:26:19', '2025-12-03 20:26:19');
INSERT INTO `categories` VALUES (7, '亲子游乐', '👶', '#06D6A0', 7, 1, '2025-12-03 20:26:19', '2025-12-03 20:26:19');
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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '聊天消息表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '消息会话表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '会话表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of conversations
-- ----------------------------

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
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商家用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of merchant_users
-- ----------------------------
INSERT INTO `merchant_users` VALUES (1, 1, '16750152199', NULL, 'cmj123456', '管理员', NULL, NULL, 1, '2025-12-15 10:25:26', '2025-12-14 19:08:25', '2025-12-14 19:08:25');
INSERT INTO `merchant_users` VALUES (2, 2, '18354763214', NULL, '123456', '管理员', NULL, NULL, 1, '2025-12-14 22:01:08', '2025-12-14 22:01:07', '2025-12-14 22:01:07');

-- ----------------------------
-- Table structure for merchants
-- ----------------------------
DROP TABLE IF EXISTS `merchants`;
CREATE TABLE `merchants`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商家ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商家名称/企业名称',
  `logo` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '商家Logo',
  `license_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '营业执照号',
  `license_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '营业执照图片',
  `contact_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系人姓名',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系电话',
  `contact_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系邮箱',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1正常，2禁用，3待审核）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_contact_phone`(`contact_phone` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商家表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of merchants
-- ----------------------------
INSERT INTO `merchants` VALUES (1, '小品烧烤', NULL, NULL, NULL, NULL, '16750152199', NULL, 1, '2025-12-14 19:08:25', '2025-12-14 19:08:25');
INSERT INTO `merchants` VALUES (2, '张亮麻辣烫', NULL, NULL, NULL, NULL, '18354763214', NULL, 1, '2025-12-14 22:01:07', '2025-12-14 22:01:07');

-- ----------------------------
-- Table structure for messages
-- ----------------------------
DROP TABLE IF EXISTS `messages`;
CREATE TABLE `messages`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `receiver_id` bigint NOT NULL COMMENT '接收者ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
  `type` tinyint NULL DEFAULT 1 COMMENT '消息类型：1=文本，2=图片',
  `is_read` tinyint NULL DEFAULT 0 COMMENT '是否已读：0=未读，1=已读',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sender`(`sender_id` ASC) USING BTREE,
  INDEX `idx_receiver`(`receiver_id` ASC) USING BTREE,
  INDEX `idx_sender_receiver`(`sender_id` ASC, `receiver_id` ASC) USING BTREE,
  INDEX `idx_receiver_sender`(`receiver_id` ASC, `sender_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '私信消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of messages
-- ----------------------------
INSERT INTO `messages` VALUES (1, 22, 27, '是', 1, 0, '2025-12-14 23:28:17');
INSERT INTO `messages` VALUES (2, 22, 27, '你好', 1, 0, '2025-12-14 23:28:30');
INSERT INTO `messages` VALUES (3, 27, 22, '你好', 1, 0, '2025-12-14 23:29:54');
INSERT INTO `messages` VALUES (4, 22, 27, '你好', 1, 0, '2025-12-14 23:30:33');
INSERT INTO `messages` VALUES (5, 27, 22, '你好', 1, 0, '2025-12-14 23:30:42');
INSERT INTO `messages` VALUES (6, 22, 27, '你好', 1, 0, '2025-12-14 23:30:47');
INSERT INTO `messages` VALUES (7, 22, 27, '你好', 1, 0, '2025-12-14 23:37:38');
INSERT INTO `messages` VALUES (8, 27, 22, '你好', 1, 0, '2025-12-14 23:37:49');
INSERT INTO `messages` VALUES (9, 22, 27, '你喜欢吃什么', 1, 0, '2025-12-14 23:37:59');
INSERT INTO `messages` VALUES (10, 22, 27, '我喜欢吃鱼', 1, 0, '2025-12-14 23:44:52');
INSERT INTO `messages` VALUES (11, 24, 22, '你好', 1, 1, '2025-12-15 10:06:45');
INSERT INTO `messages` VALUES (12, 22, 24, '你好呀', 1, 1, '2025-12-15 10:06:58');
INSERT INTO `messages` VALUES (13, 24, 22, '你叫什么名字', 1, 1, '2025-12-15 10:07:21');
INSERT INTO `messages` VALUES (14, 22, 24, '我是小松鼠', 1, 1, '2025-12-15 10:31:08');
INSERT INTO `messages` VALUES (15, 22, 24, '你呢', 1, 1, '2025-12-15 10:31:12');
INSERT INTO `messages` VALUES (16, 24, 22, '我是小兔子', 1, 1, '2025-12-15 10:31:24');
INSERT INTO `messages` VALUES (17, 24, 22, '1', 1, 1, '2025-12-15 10:37:41');
INSERT INTO `messages` VALUES (18, 22, 24, '1', 1, 1, '2025-12-15 10:40:47');
INSERT INTO `messages` VALUES (19, 24, 22, '1', 1, 1, '2025-12-15 10:45:42');
INSERT INTO `messages` VALUES (20, 22, 24, 'nihao', 1, 1, '2025-12-15 10:46:10');
INSERT INTO `messages` VALUES (21, 24, 22, '1', 1, 1, '2025-12-15 10:46:32');
INSERT INTO `messages` VALUES (22, 24, 22, '1', 1, 1, '2025-12-15 10:50:13');
INSERT INTO `messages` VALUES (23, 22, 24, '1', 1, 1, '2025-12-15 10:50:18');
INSERT INTO `messages` VALUES (24, 24, 22, '你好小松鼠', 1, 1, '2025-12-15 10:50:46');

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
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '笔记评论表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '笔记标签表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '笔记话题关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of note_topics
-- ----------------------------

-- ----------------------------
-- Table structure for notes
-- ----------------------------
DROP TABLE IF EXISTS `notes`;
CREATE TABLE `notes`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '笔记ID',
  `user_id` bigint NOT NULL COMMENT '作者用户ID',
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
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_shop_id`(`shop_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_is_recommend`(`is_recommend` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_like_count`(`like_count` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '笔记表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notes
-- ----------------------------
INSERT INTO `notes` VALUES (1, 22, NULL, '可爱头像', '可爱的头像,非常好的一次旅行', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/744ce88af0f046c281268fa29aa387e6.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/744ce88af0f046c281268fa29aa387e6.png,https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/5558e3b38eaf44dc9e4701e229dedb1b.png,https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/d41f3e7fffa345e58d86723c7f529bad.png,https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/cba00da586054dc1bff418062ed91066.png,https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/cf2fbfa47286472aa3c458117371463b.png,https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/72ce2a8b89864dc490dec4607d8776b5.png', NULL, NULL, NULL, 1, 1, 40, 1, 0, NULL, 1, 0, '2025-12-04 23:01:51', '2025-12-14 22:14:26');
INSERT INTO `notes` VALUES (2, 22, NULL, '小熊', '非常可爱的小熊,值得大家拥有', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/eedcf9db924d4d4d8724aa3ae551cad4.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/eedcf9db924d4d4d8724aa3ae551cad4.png', NULL, NULL, NULL, 1, 1, 17, 2, 0, NULL, 1, 0, '2025-12-04 23:08:44', '2025-12-14 20:47:24');
INSERT INTO `notes` VALUES (3, 27, NULL, '可爱小狗头像', '非常可爱的小狗值得大家拥有', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/17543e07356c43f8aa33b952c4263a6c.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/17543e07356c43f8aa33b952c4263a6c.png', NULL, NULL, NULL, 2, 0, 14, 0, 0, NULL, 1, 0, '2025-12-04 23:12:38', '2025-12-14 23:23:31');
INSERT INTO `notes` VALUES (4, 28, NULL, '小猫咪', '可爱的小猫咪,值得大家相信', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/66e7fc5bc0e14762ac21f9b6cc8f5d7a.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/66e7fc5bc0e14762ac21f9b6cc8f5d7a.png', NULL, NULL, NULL, 4, 1, 57, 4, 0, NULL, 1, 0, '2025-12-04 23:23:01', '2025-12-14 23:23:55');
INSERT INTO `notes` VALUES (5, 29, NULL, '可爱小狗', '可爱的小狗值得大家相信', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/6e409a8067fb4500b6398fab6cd36ee0.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/6e409a8067fb4500b6398fab6cd36ee0.png', NULL, NULL, NULL, 2, 0, 18, 1, 0, NULL, 1, 0, '2025-12-04 23:24:54', '2025-12-05 15:58:04');
INSERT INTO `notes` VALUES (6, 29, NULL, '可爱的小猪头像', '非常可爱的小猪头像,很值得相信的一次旅行', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/6f15b04f8efa4fa0a5f77271d33a9c47.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/04/6f15b04f8efa4fa0a5f77271d33a9c47.png', NULL, NULL, NULL, 3, 2, 70, 3, 0, NULL, 1, 0, '2025-12-04 23:42:40', '2025-12-14 23:23:53');
INSERT INTO `notes` VALUES (7, 27, NULL, '你好我高兴', '非常真实的一次体验值得大家相信', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/05/8f1050bbab1f4685921694575e0198d6.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/05/8f1050bbab1f4685921694575e0198d6.png', NULL, NULL, NULL, 1, 0, 13, 1, 0, NULL, 1, 0, '2025-12-05 00:16:39', '2025-12-12 12:05:58');
INSERT INTO `notes` VALUES (8, 22, NULL, '我喜欢吃肉', '这家四川味道的肉真的很好吃,欢迎大家前来品尝', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/05/7f9ec7fea6fc4a4b9e6627cadc6c043e.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/05/7f9ec7fea6fc4a4b9e6627cadc6c043e.png', NULL, NULL, NULL, 1, 2, 25, 1, 0, NULL, 1, 0, '2025-12-05 12:11:02', '2025-12-14 23:23:34');
INSERT INTO `notes` VALUES (9, 32, NULL, '很美好的一次旅程', '今天看到一个非常好看的头像很想分享给大家一起来观看这个头像', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/05/3988b2b969ad4805a794f6b8cfffe1bb.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/05/3988b2b969ad4805a794f6b8cfffe1bb.png', NULL, NULL, NULL, 2, 1, 7, 1, 0, NULL, 1, 0, '2025-12-05 15:47:33', '2025-12-14 22:29:26');
INSERT INTO `notes` VALUES (10, 22, NULL, '很好', '非常好的一次背景,值得大家换上', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/05/58310d4b04b844bea24a35d9c70b2f48.jpg', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/05/58310d4b04b844bea24a35d9c70b2f48.jpg', NULL, NULL, NULL, 3, 0, 12, 2, 0, NULL, 1, 0, '2025-12-05 16:17:26', '2025-12-15 10:22:46');
INSERT INTO `notes` VALUES (11, 22, NULL, '可爱的小熊', '非常可爱的小熊,值得你拥有', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/05/34d40cd5a8e647d8addc654247983124.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/05/34d40cd5a8e647d8addc654247983124.png', NULL, NULL, NULL, 0, 1, 4, 0, 0, NULL, 1, 0, '2025-12-05 18:37:15', '2025-12-14 23:23:45');
INSERT INTO `notes` VALUES (12, 24, NULL, '非常可爱的小动物', '非常可爱的小动物,大家可以一起来看看', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/12/eecb59b646a64b8aa9b92ff0ee5e4ae5.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/12/eecb59b646a64b8aa9b92ff0ee5e4ae5.png,https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/12/4eebef401bcb48edbf6220b6a2150dcf.png', NULL, NULL, NULL, 1, 1, 13, 0, 0, NULL, 1, 0, '2025-12-12 18:58:37', '2025-12-15 10:29:54');
INSERT INTO `notes` VALUES (13, 1, 2, '米线王子好好吃', '米线王子真的好好吃', NULL, '', NULL, NULL, NULL, 0, 0, 0, 0, 0, NULL, 0, 0, '2025-12-14 20:52:11', '2025-12-14 20:52:11');
INSERT INTO `notes` VALUES (14, 1, 2, '很好吃的米线店', '快来吃米线王子', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/14/baf31b081b3f460d89918d9137f8bbcc.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/14/baf31b081b3f460d89918d9137f8bbcc.png', NULL, NULL, NULL, 0, 0, 0, 0, 0, NULL, 0, 0, '2025-12-14 21:57:33', '2025-12-14 21:57:33');
INSERT INTO `notes` VALUES (15, 2, 3, '张亮麻辣烫,麻辣烫之王', '张亮麻辣烫,值得你选择', NULL, '', NULL, NULL, NULL, 0, 0, 0, 0, 0, NULL, 0, 0, '2025-12-14 22:04:49', '2025-12-14 22:04:49');
INSERT INTO `notes` VALUES (16, 2, 4, '张亮麻辣烫,麻辣烫之王', '张亮麻辣烫真好吃', NULL, '', NULL, NULL, NULL, 0, 0, 0, 0, 0, NULL, 0, 0, '2025-12-14 22:24:40', '2025-12-14 22:24:40');
INSERT INTO `notes` VALUES (17, 24, NULL, '可爱的小动物头像', '非常可爱的小动物头像', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/14/c797b754ac7c464ca42cf50891f73810.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/14/c797b754ac7c464ca42cf50891f73810.png,https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/14/c73ab287fb82476689b7c45ef9e4aa11.png,https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/14/6e1d2855a3034d99af0ff49a6b24002c.png,https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/14/6392f5f3d29d4a4da7c3cacc3818afd2.png,https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/14/291f544bddd84503a925d1bab5f2c41d.png,https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/14/2f57372657744ec88a5fabb0a94abdb8.png,https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/14/ad8c102c6b464c9bae356b61f2aefec1.png,https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/14/ffaa7b1cd33c4759bf6cd09cbdfae333.png,https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/14/3a4059027b3b4406ba21c2df72e2088e.png', NULL, NULL, NULL, 0, 0, 0, 0, 0, NULL, 2, 0, '2025-12-14 22:28:44', '2025-12-14 22:28:44');
INSERT INTO `notes` VALUES (18, 24, NULL, '可爱的小狗', '非常可爱的小狗,快来一起看看', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/14/c55b325858484a839482c4f70395f1e5.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/14/c55b325858484a839482c4f70395f1e5.png', NULL, NULL, NULL, 0, 0, 2, 0, 0, NULL, 1, 0, '2025-12-14 22:30:23', '2025-12-14 23:23:40');

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '私信消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of private_messages
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
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '评价图片（JSON）',
  `like_count` int NOT NULL DEFAULT 0 COMMENT '点赞数',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1正常，2隐藏）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_shop_id`(`shop_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商家评价表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shop_reviews
-- ----------------------------

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商家标签表' ROW_FORMAT = Dynamic;

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
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '省份',
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '城市',
  `district` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '区县',
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
  INDEX `idx_city`(`city` ASC) USING BTREE,
  INDEX `idx_rating`(`rating` ASC) USING BTREE,
  INDEX `idx_popularity`(`popularity` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_location`(`latitude` ASC, `longitude` ASC) USING BTREE,
  INDEX `idx_merchant_id`(`merchant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商家信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shops
-- ----------------------------
INSERT INTO `shops` VALUES (2, 1, 1, '米线王子', '', '', '米线王子很好吃的', '16750152199', '普洱学院1735美食城', NULL, NULL, NULL, NULL, NULL, '09.00-22.00', 15.00, 5.00, 5.00, 5.00, 5.00, 0, 0, 1, '2025-12-14 20:43:46', '2025-12-15 10:26:21');
INSERT INTO `shops` VALUES (3, 2, 1, '张亮麻辣烫', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/14/1284665d69df4671b9ef565a077cd4bc.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/14/1f672828558f4de28535eda00e830b48.png', '', '18890746321', '普洱学院张亮麻辣烫', NULL, NULL, NULL, NULL, NULL, '9.00-22.00', 200.00, 5.00, 5.00, 5.00, 5.00, 0, 0, 1, '2025-12-14 22:04:08', '2025-12-14 22:04:08');
INSERT INTO `shops` VALUES (4, 2, 1, '张亮麻辣烫', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/14/892c53a62a2346d2bea437e7351d3092.png', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/merchant/2025/12/14/fc29f438b4cd432eac95ac17657b35e2.png', '张亮麻辣烫', '18897963214', '张亮麻辣烫', NULL, NULL, NULL, NULL, NULL, '09.00-22.00', 200.00, 5.00, 5.00, 5.00, 5.00, 0, 0, 1, '2025-12-14 22:12:47', '2025-12-14 22:19:35');

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
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of system_notices
-- ----------------------------
INSERT INTO `system_notices` VALUES (1, 22, 24, 1, 10, '7798 赞了你的笔记', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/05/58310d4b04b844bea24a35d9c70b2f48.jpg', 0, '2025-12-15 10:19:26');
INSERT INTO `system_notices` VALUES (2, 24, 22, 1, 12, '用户2199 赞了你的笔记', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/images/2025/12/12/eecb59b646a64b8aa9b92ff0ee5e4ae5.png', 0, '2025-12-15 10:29:16');
INSERT INTO `system_notices` VALUES (3, 32, 22, 3, 22, '用户2199 关注了你', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/avatars/2025/12/14/46580897d95f49cebe12dfbb9f663cf1.png', 0, '2025-12-15 10:51:04');

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '话题表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of topics
-- ----------------------------

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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户浏览历史表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_browse_history
-- ----------------------------

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
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户点赞评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_comment_likes
-- ----------------------------

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
) ENGINE = InnoDB AUTO_INCREMENT = 53 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_favorites
-- ----------------------------
INSERT INTO `user_favorites` VALUES (2, 29, 1, 5, '2025-12-05 00:01:08');
INSERT INTO `user_favorites` VALUES (3, 28, 1, 4, '2025-12-05 00:01:32');
INSERT INTO `user_favorites` VALUES (4, 28, 1, 1, '2025-12-05 00:03:40');
INSERT INTO `user_favorites` VALUES (5, 22, 1, 2, '2025-12-05 11:32:00');
INSERT INTO `user_favorites` VALUES (9, 22, 1, 4, '2025-12-05 12:41:52');
INSERT INTO `user_favorites` VALUES (10, 22, 1, 7, '2025-12-05 12:42:17');
INSERT INTO `user_favorites` VALUES (11, 22, 1, 8, '2025-12-05 13:13:17');
INSERT INTO `user_favorites` VALUES (12, 32, 1, 6, '2025-12-05 15:43:05');
INSERT INTO `user_favorites` VALUES (13, 32, 1, 9, '2025-12-05 15:47:43');
INSERT INTO `user_favorites` VALUES (14, 32, 1, 4, '2025-12-05 15:47:48');
INSERT INTO `user_favorites` VALUES (24, 32, 1, 2, '2025-12-05 16:09:27');
INSERT INTO `user_favorites` VALUES (27, 22, 1, 10, '2025-12-05 16:18:21');
INSERT INTO `user_favorites` VALUES (44, 24, 1, 6, '2025-12-12 18:53:11');
INSERT INTO `user_favorites` VALUES (45, 24, 1, 10, '2025-12-12 18:53:49');
INSERT INTO `user_favorites` VALUES (47, 24, 1, 4, '2025-12-12 18:55:39');
INSERT INTO `user_favorites` VALUES (49, 22, 1, 6, '2025-12-14 22:16:16');

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
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '关注关系表' ROW_FORMAT = Dynamic;

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
INSERT INTO `user_follows` VALUES (26, 22, 24, '2025-12-14 23:24:09');
INSERT INTO `user_follows` VALUES (28, 22, 27, '2025-12-14 23:28:13');
INSERT INTO `user_follows` VALUES (29, 27, 22, '2025-12-14 23:29:42');
INSERT INTO `user_follows` VALUES (30, 22, 32, '2025-12-15 10:51:04');

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
) ENGINE = InnoDB AUTO_INCREMENT = 88 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户点赞笔记表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_note_likes
-- ----------------------------
INSERT INTO `user_note_likes` VALUES (2, 29, 6, '2025-12-05 00:01:00');
INSERT INTO `user_note_likes` VALUES (4, 29, 5, '2025-12-05 00:01:09');
INSERT INTO `user_note_likes` VALUES (10, 28, 4, '2025-12-05 00:01:31');
INSERT INTO `user_note_likes` VALUES (22, 22, 4, '2025-12-05 12:41:46');
INSERT INTO `user_note_likes` VALUES (23, 22, 7, '2025-12-05 12:42:16');
INSERT INTO `user_note_likes` VALUES (25, 22, 8, '2025-12-05 13:13:16');
INSERT INTO `user_note_likes` VALUES (26, 22, 1, '2025-12-05 14:05:30');
INSERT INTO `user_note_likes` VALUES (27, 32, 6, '2025-12-05 15:43:02');
INSERT INTO `user_note_likes` VALUES (28, 32, 9, '2025-12-05 15:47:43');
INSERT INTO `user_note_likes` VALUES (29, 32, 4, '2025-12-05 15:47:47');
INSERT INTO `user_note_likes` VALUES (30, 32, 3, '2025-12-05 15:58:00');
INSERT INTO `user_note_likes` VALUES (31, 32, 5, '2025-12-05 15:58:04');
INSERT INTO `user_note_likes` VALUES (32, 32, 2, '2025-12-05 16:08:49');
INSERT INTO `user_note_likes` VALUES (46, 22, 10, '2025-12-05 16:18:21');
INSERT INTO `user_note_likes` VALUES (49, 33, 10, '2025-12-05 18:27:15');
INSERT INTO `user_note_likes` VALUES (73, 24, 4, '2025-12-12 18:55:41');
INSERT INTO `user_note_likes` VALUES (74, 24, 9, '2025-12-12 19:43:33');
INSERT INTO `user_note_likes` VALUES (79, 22, 3, '2025-12-14 22:13:06');
INSERT INTO `user_note_likes` VALUES (82, 22, 6, '2025-12-14 22:16:16');
INSERT INTO `user_note_likes` VALUES (86, 24, 10, '2025-12-15 10:19:26');
INSERT INTO `user_note_likes` VALUES (87, 22, 12, '2025-12-15 10:29:16');

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户收藏商家表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 35 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户统计表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_stats
-- ----------------------------
INSERT INTO `user_stats` VALUES (8, 8, 0, 0, 0, 0, 0, '2025-12-03 23:22:47', '2025-12-03 23:22:47');
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
INSERT INTO `user_stats` VALUES (22, 22, 5, 2, 6, 6, 5, '2025-12-04 19:15:52', '2025-12-15 10:51:04');
INSERT INTO `user_stats` VALUES (23, 23, 0, 0, 0, 0, 0, '2025-12-04 19:28:37', '2025-12-04 19:28:37');
INSERT INTO `user_stats` VALUES (24, 24, 3, 1, 1, 3, 3, '2025-12-04 19:31:26', '2025-12-15 10:29:15');
INSERT INTO `user_stats` VALUES (25, 25, 0, 0, 0, 0, 0, '2025-12-04 20:23:52', '2025-12-04 20:23:52');
INSERT INTO `user_stats` VALUES (26, 26, 0, 0, 0, 0, 0, '2025-12-04 21:38:22', '2025-12-04 21:38:22');
INSERT INTO `user_stats` VALUES (27, 27, 1, 2, 3, 0, 2, '2025-12-04 23:11:58', '2025-12-14 23:29:42');
INSERT INTO `user_stats` VALUES (28, 28, 0, 3, 4, 2, 1, '2025-12-04 23:21:45', '2025-12-14 23:23:51');
INSERT INTO `user_stats` VALUES (29, 29, 0, 3, 5, 1, 2, '2025-12-04 23:24:06', '2025-12-14 23:23:54');
INSERT INTO `user_stats` VALUES (30, 30, 0, 0, 0, 0, 0, '2025-12-05 00:05:47', '2025-12-05 00:05:47');
INSERT INTO `user_stats` VALUES (31, 31, 0, 0, 0, 0, 0, '2025-12-05 00:13:32', '2025-12-05 00:13:32');
INSERT INTO `user_stats` VALUES (32, 32, 4, 2, 2, 4, 1, '2025-12-05 15:42:43', '2025-12-15 10:51:04');
INSERT INTO `user_stats` VALUES (33, 33, 0, 0, 0, 0, 0, '2025-12-05 18:26:51', '2025-12-05 18:26:51');
INSERT INTO `user_stats` VALUES (34, 34, 0, 0, 0, 0, 0, '2025-12-12 12:01:13', '2025-12-12 12:01:13');

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
) ENGINE = InnoDB AUTO_INCREMENT = 35 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, '16750152199_1', '小品烧烤官方', 'https://via.placeholder.com/100', '小品烧烤的官方账号', NULL, NULL, NULL, NULL, 0, NULL, 1, NULL, '2025-12-14 21:57:33', '2025-12-14 21:57:33');
INSERT INTO `users` VALUES (2, '18354763214', '张亮麻辣烫官方', 'https://via.placeholder.com/100', '张亮麻辣烫的官方账号', NULL, NULL, NULL, NULL, 0, NULL, 1, NULL, '2025-12-14 22:04:49', '2025-12-14 22:04:49');
INSERT INTO `users` VALUES (22, '16750152199', '用户2199', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/avatars/2025/12/14/46580897d95f49cebe12dfbb9f663cf1.png', '我喜欢吃饭', '123456', '', '', '', 0, '2003-09-24', 1, '2025-12-15 10:05:13', '2025-12-04 19:15:52', '2025-12-04 19:15:52');
INSERT INTO `users` VALUES (23, '18975635432', '用户5432', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head1.png', '大家好,想吃好吃的跟着我', '123456', '', '', '', 1, '2004-06-01', 1, '2025-12-04 20:19:23', '2025-12-04 19:28:37', '2025-12-04 19:28:37');
INSERT INTO `users` VALUES (24, '17090097798', '7798', 'https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head1.png', '大家好我喜欢吃肉', '17090097798', '', '', '', 1, '2003-07-01', 1, '2025-12-15 10:06:19', '2025-12-04 19:31:26', '2025-12-04 19:31:26');
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
) ENGINE = InnoDB AUTO_INCREMENT = 166 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '验证码表' ROW_FORMAT = Dynamic;

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

SET FOREIGN_KEY_CHECKS = 1;
