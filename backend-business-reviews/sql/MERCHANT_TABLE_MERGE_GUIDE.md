# 商家表整合指南

## 概述

将 `merchant_users` 表整合到 `merchants` 表中，实现商家信息的统一管理。

## 表结构对比

### 原 merchant_users 表字段
| 字段 | 类型 | 说明 | 整合后处理 |
|------|------|------|-----------|
| id | bigint | 用户ID | 不需要（使用merchants.id） |
| merchant_id | bigint | 所属商家ID | 不需要（整合后相同） |
| phone | varchar(20) | 手机号 | 与contact_phone重复 |
| email | varchar(100) | 邮箱 | 与contact_email重复 |
| password | varchar(100) | 密码 | **新增到merchants** |
| name | varchar(50) | 姓名 | 与contact_name重复 |
| avatar | varchar(500) | 头像 | **新增到merchants** |
| role_id | bigint | 角色ID | **新增到merchants** |
| status | tinyint | 状态 | 与merchants.status重复 |
| last_login_at | datetime | 最后登录时间 | **新增到merchants** |
| created_at | datetime | 创建时间 | 已有 |
| updated_at | datetime | 更新时间 | 已有 |

### 需要在 merchants 表新增的字段

```sql
ALTER TABLE `merchants` 
ADD COLUMN `password` varchar(100) COMMENT '登录密码（加密）',
ADD COLUMN `avatar` varchar(500) COMMENT '商家头像',
ADD COLUMN `role_id` bigint COMMENT '角色ID',
ADD COLUMN `last_login_at` datetime COMMENT '最后登录时间';
```

## 商家入驻时需要填写的字段

### 必填字段
1. **手机号** (phone/contact_phone) - 用于登录
2. **验证码** (code) - 验证手机号
3. **密码** (password) - 登录密码
4. **商家名称** (merchantName/name) - 企业/店铺名称
5. **联系人姓名** (contactName/contact_name) - 负责人姓名

### 选填字段
1. **商家Logo** (logo) - 品牌标识
2. **联系邮箱** (contactEmail/contact_email) - 业务联系邮箱
3. **营业执照号** (licenseNo/license_no) - 企业资质
4. **营业执照图片** (licenseImage/license_image) - 资质证明

### 系统自动生成的字段（不需要填写）
1. **创建时间** (created_at)
2. **更新时间** (updated_at)
3. **最后登录时间** (last_login_at)
4. **状态** (status) - 默认为1（正常）

## 执行步骤

### 1. 执行数据库迁移脚本
```bash
mysql -u root -p business_reviews < merge_merchant_tables.sql
```

### 2. 验证数据迁移
```sql
SELECT id, name, contact_phone, password, avatar, last_login_at 
FROM merchants;
```

### 3. 确认无误后删除旧表
```sql
DROP TABLE IF EXISTS merchant_users;
```

### 4. 重新编译后端
```bash
cd backend-business-reviews
mvn clean compile
```

### 5. 重启后端服务

## 代码修改清单

### 后端修改
1. ✅ `Merchant.java` - 添加password, avatar, roleId, lastLoginAt字段
2. ✅ `MerchantMapper.java` - 添加selectByPhone, countByPhone方法
3. ✅ `MerchantAuthService.java` - 更新register方法签名
4. ✅ `MerchantAuthServiceImpl.java` - 移除MerchantUserMapper依赖，使用MerchantMapper
5. ✅ `MerchantAuthInterceptor.java` - 使用Merchant替代MerchantUser
6. ✅ `MerchantRegisterRequest.java` - 添加完整的入驻字段（包括avatar）
7. ✅ `MerchantAuthController.java` - 更新register方法

### 前端修改
1. ✅ `login/index.vue` - 更新入驻表单为两列布局，添加图片上传功能
2. ✅ `api/auth.ts` - 更新merchantRegister接口参数（添加avatar）

## 商家入驻自动化功能

### 自动创建UniApp用户账号
商家入驻成功后，系统会自动在`users`表中创建对应的UniApp用户账号：
- `users.phone` = `merchants.contact_phone`
- `users.username` = `merchants.name`
- `users.avatar` = `merchants.avatar` 或 `merchants.logo`
- `users.password` = `merchants.password`

### 自动创建默认门店
商家入驻成功后，系统会自动在`shops`表中创建默认门店：
- `shops.name` = `merchants.name`（商家名称）
- `shops.header_image` = `merchants.logo`（商家Logo作为封面图）
- `shops.phone` = `merchants.contact_phone`（联系电话）
- `shops.merchant_id` = `merchants.id`（关联商家ID）

## 注意事项

1. **数据备份**：执行迁移前务必备份数据库
2. **密码加密**：生产环境应使用BCrypt加密密码
3. **唯一索引**：contact_phone需要添加唯一索引用于登录
4. **Token兼容**：整合后Token中存储的是merchantId
5. **图片上传**：商家Logo、头像、执照图片均通过阿里云OSS上传
