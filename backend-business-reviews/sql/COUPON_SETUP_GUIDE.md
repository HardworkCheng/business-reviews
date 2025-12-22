# 优惠券系统数据库设置指南

## 问题说明

之前执行 `coupon_redesign_tables.sql` 时出现错误：
```
1146 - Table 'business_reviews.user_coupons' doesn't exist
```

**原因**：SQL文件中的视图和存储过程依赖 `user_coupons` 表，但该表在执行时可能还不存在。

## 解决方案

已修复 `coupon_redesign_tables.sql` 文件，现在可以独立执行。

### 修复内容

1. ✅ 添加了基础表的创建（使用 `CREATE TABLE IF NOT EXISTS`）
   - `coupons` 表
   - `user_coupons` 表

2. ✅ 添加了存储过程的删除语句（`DROP PROCEDURE IF EXISTS`）

3. ✅ 添加了详细的执行说明注释

## 执行步骤

### 方法1：直接执行完整文件（推荐）

```sql
-- 在MySQL客户端或Navicat中执行
SOURCE /path/to/coupon_redesign_tables.sql;
```

或者在Navicat中：
1. 打开 `coupon_redesign_tables.sql` 文件
2. 点击"运行"按钮
3. 等待执行完成

### 方法2：分步执行

如果仍然遇到问题，可以按以下顺序执行：

```sql
-- 1. 确保数据库存在
USE business_reviews;

-- 2. 创建基础表（如果不存在）
-- 执行文件中的 "0. 确保基础表存在" 部分

-- 3. 创建新增表
-- 执行文件中的 1-6 部分（秒杀活动表、统计表等）

-- 4. 创建视图
-- 执行文件中的 "视图创建" 部分

-- 5. 创建存储过程
-- 执行文件中的 "存储过程" 部分
```

## 验证安装

执行测试脚本验证所有表是否创建成功：

```sql
SOURCE /path/to/test_coupon_tables.sql;
```

或者手动检查：

```sql
-- 查看所有优惠券相关表
SHOW TABLES LIKE '%coupon%';
SHOW TABLES LIKE '%seckill%';

-- 查看表结构
DESC coupons;
DESC user_coupons;
DESC seckill_activities;
DESC seckill_coupons;

-- 查看视图
SHOW FULL TABLES WHERE table_type = 'VIEW';

-- 查看存储过程
SHOW PROCEDURE STATUS WHERE db = 'business_reviews';
```

## 创建的数据库对象

### 表（Tables）

1. **coupons** - 优惠券表（基础表）
2. **user_coupons** - 用户优惠券表（基础表）
3. **seckill_activities** - 秒杀活动表（新增）
4. **seckill_coupons** - 秒杀券关联表（新增）
5. **user_seckill_records** - 用户秒杀记录表（新增）
6. **coupon_view_logs** - 优惠券浏览记录表（新增）
7. **coupon_search_logs** - 优惠券搜索记录表（新增）
8. **coupon_statistics** - 优惠券统计表（新增）

### 视图（Views）

1. **v_user_coupon_details** - 用户优惠券详情视图
2. **v_available_coupons** - 可领取优惠券视图

### 存储过程（Procedures）

1. **sp_claim_coupon** - 用户领取优惠券存储过程

## 示例数据

执行完成后，会自动插入一条秒杀活动示例数据：

```sql
SELECT * FROM seckill_activities;
```

## 常见问题

### Q1: 执行时提示某个表已存在

**A**: 这是正常的。使用 `CREATE TABLE IF NOT EXISTS` 不会覆盖已存在的表。

### Q2: 视图创建失败

**A**: 确保 `coupons` 和 `user_coupons` 表已经存在。可以先执行基础表创建部分。

### Q3: 存储过程创建失败

**A**: 检查是否有权限创建存储过程。需要 `CREATE ROUTINE` 权限。

```sql
-- 授予权限（需要管理员执行）
GRANT CREATE ROUTINE ON business_reviews.* TO 'your_user'@'localhost';
FLUSH PRIVILEGES;
```

## 回滚（如需要）

如果需要删除所有创建的对象：

```sql
-- 删除新增的表
DROP TABLE IF EXISTS coupon_statistics;
DROP TABLE IF EXISTS coupon_search_logs;
DROP TABLE IF EXISTS coupon_view_logs;
DROP TABLE IF EXISTS user_seckill_records;
DROP TABLE IF EXISTS seckill_coupons;
DROP TABLE IF EXISTS seckill_activities;

-- 删除视图
DROP VIEW IF EXISTS v_available_coupons;
DROP VIEW IF EXISTS v_user_coupon_details;

-- 删除存储过程
DROP PROCEDURE IF EXISTS sp_claim_coupon;
```

## 下一步

数据库设置完成后，可以：

1. 启动后端服务
2. 测试API接口
3. 在前端页面查看优惠券功能

## 技术支持

如果仍然遇到问题，请检查：

1. MySQL版本（建议 5.7+）
2. 数据库字符集（utf8mb4）
3. 用户权限
4. SQL执行日志
