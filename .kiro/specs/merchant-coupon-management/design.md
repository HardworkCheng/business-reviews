# Design Document - 商家优惠券管理系统完善

## Overview

本设计文档描述了商家运营中心优惠券发放功能的完善方案。核心目标是确保Web端商家运营中心的优惠券创建表单与数据库设计（`coupon_redesign_tables.sql`）保持一致，并实现商家发放的优惠券能够在移动端用户的优惠券页面正确展示。

主要改动包括：
1. **Web端表单重构**：调整优惠券类型和字段以匹配数据库设计
2. **字段映射统一**：确保前后端字段名称和数据类型一致
3. **移动端展示优化**：根据优惠券类型正确显示优惠信息

## Architecture

### 系统架构图

```
┌─────────────────────────────────────────────────────────────┐
│                   Web端商家运营中心 (Vue 3)                    │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              优惠券创建/编辑表单 (create.vue)           │  │
│  │  - 优惠券类型选择 (满减券/折扣券/代金券)                  │  │
│  │  - 动态字段显示 (根据类型显示不同字段)                    │  │
│  │  - 数据验证和格式化                                     │  │
│  └──────────────────────────────────────────────────────┘  │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP POST /merchant/coupons
┌──────────────────────────▼──────────────────────────────────┐
│                    后端服务 (Spring Boot)                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │MerchantCoupon│  │MerchantCoupon│  │  CouponMapper │      │
│  │  Controller  │→ │   Service    │→ │              │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                      数据库 (MySQL)                           │
│  ┌──────────────────────────────────────────────────────┐  │
│  │                    coupons 表                          │  │
│  │  id, merchant_id, shop_id, type, title, description   │  │
│  │  amount, discount, min_amount, total_count,           │  │
│  │  remain_count, per_user_limit, start_time, end_time,  │  │
│  │  stackable, status, created_at, updated_at            │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                   移动端 uniapp (Vue 3)                       │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              优惠券展示页面 (discount.vue)              │  │
│  │  - 领券中心：展示可领取优惠券                            │  │
│  │  - 我的卡包：展示已领取优惠券                            │  │
│  │  - 根据类型显示不同格式的优惠信息                        │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### 数据流

```
商家创建优惠券:
Web表单 → API请求 → 后端验证 → 数据库存储 → 返回结果

用户查看优惠券:
移动端请求 → 后端查询 → 数据格式化 → 返回列表 → 前端渲染
```

## Components and Interfaces

### Web端组件 (front-business-reviews-Web)

**优惠券创建表单 (src/views/coupon/create.vue)**

| 字段 | 类型 | 说明 | 数据库字段 |
|------|------|------|-----------|
| title | String | 优惠券标题 | title |
| type | Integer | 类型(1满减券,2折扣券,3代金券) | type |
| amount | Decimal | 优惠金额 | amount |
| discount | Decimal | 折扣(0.01-0.99) | discount |
| minAmount | Decimal | 最低消费金额 | min_amount |
| totalCount | Integer | 发行总量 | total_count |
| perUserLimit | Integer | 每人限领 | per_user_limit |
| startTime | DateTime | 开始时间 | start_time |
| endTime | DateTime | 结束时间 | end_time |
| stackable | Boolean | 是否可叠加 | stackable |
| shopId | Long | 适用店铺(可选) | shop_id |
| description | String | 使用说明 | description |

### 后端API接口

**创建优惠券**
```
POST /merchant/coupons
Request Body: CreateCouponDTO
Response: { code: 200, data: { couponId: Long } }
```

**获取可领取优惠券列表**
```
GET /app/coupons/available
Query: pageNum, pageSize, keyword, type
Response: { code: 200, data: [CouponVO] }
```

### 移动端组件 (front-business-reviews-Mobile)

**优惠券展示 (src/pages/discount/discount.vue)**
- 根据 type 字段显示不同格式的优惠信息
- 根据 remainCount 和用户领取状态显示按钮状态

## Data Models

### 数据库表结构 (coupons)

```sql
CREATE TABLE `coupons` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
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
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
);
```

### 优惠券类型定义

| type值 | 名称 | 必填字段 | 显示格式 |
|--------|------|----------|----------|
| 1 | 满减券 | amount, minAmount | 满{minAmount}减{amount} |
| 2 | 折扣券 | discount, minAmount | {discount*10}折 |
| 3 | 代金券 | amount | ¥{amount} |

### 前端表单字段映射

| Web端字段 | 后端DTO字段 | 数据库字段 | 说明 |
|-----------|-------------|------------|------|
| title | title | title | 直接映射 |
| type | type | type | 1/2/3 |
| amount | amount | amount | Decimal |
| discount | discount | discount | 0.01-0.99 |
| minAmount | minAmount | min_amount | Decimal |
| totalCount | totalCount | total_count | Integer |
| perUserLimit | perUserLimit | per_user_limit | Integer |
| validityPeriod[0] | startTime | start_time | DateTime |
| validityPeriod[1] | endTime | end_time | DateTime |
| stackable | stackable | stackable | Boolean→Integer |
| shopId | shopId | shop_id | Long/null |
| description | description | description | String |

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: 字段可见性与优惠券类型一致性
*For any* 优惠券类型值(1/2/3)，表单显示的字段应与该类型的必填字段定义一致：类型1显示amount和minAmount，类型2显示discount和minAmount，类型3仅显示amount
**Validates: Requirements 1.2, 1.3, 1.4**

### Property 2: 表单验证与类型必填字段一致性
*For any* 优惠券表单提交，验证逻辑应根据选择的类型检查对应的必填字段：类型1需要amount>0和minAmount>=0，类型2需要0<discount<1和minAmount>=0，类型3需要amount>0
**Validates: Requirements 1.5, 2.1, 2.2, 2.3**

### Property 3: 剩余数量初始化正确性
*For any* 新创建的优惠券，remainCount应等于totalCount
**Validates: Requirements 1.6, 6.3**

### Property 4: 可领取优惠券过滤正确性
*For any* 优惠券列表查询，返回的优惠券应满足：status=1 AND 当前时间在startTime和endTime之间 AND remainCount>0
**Validates: Requirements 4.1**

### Property 5: 优惠券状态显示正确性
*For any* 优惠券，当remainCount=0时显示"已抢光"，当用户已领取时显示"已领取"，否则显示"领取"
**Validates: Requirements 4.3, 4.4**

### Property 6: 优惠券值显示格式正确性
*For any* 优惠券，根据type显示正确格式：type=1显示"满{minAmount}减{amount}"，type=2显示"{discount*10}折"，type=3显示"¥{amount}"
**Validates: Requirements 5.1, 5.2, 5.3**

### Property 7: 店铺关联正确性
*For any* 优惠券创建，当未选择店铺时shopId为null，当选择店铺时shopId为对应店铺ID
**Validates: Requirements 3.2, 3.3**

### Property 8: 数值范围验证正确性
*For any* 输入值，amount和minAmount应为非负数且最多2位小数，discount应在(0,1)区间，totalCount和perUserLimit应为正整数
**Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5**

## Error Handling

### 前端错误处理

| 错误场景 | 处理方式 |
|---------|---------|
| 必填字段为空 | 表单验证提示"请输入xxx" |
| 数值格式错误 | 表单验证提示"请输入有效的数值" |
| 折扣范围错误 | 表单验证提示"折扣应在0.01-0.99之间" |
| 时间范围错误 | 表单验证提示"结束时间应晚于开始时间" |
| 网络请求失败 | Toast提示"网络异常，请重试" |
| 创建失败 | Toast提示具体错误信息 |

### 后端错误码

| 错误码 | 描述 |
|-------|------|
| 400 | 参数验证失败 |
| 401 | 未登录或登录过期 |
| 403 | 无权限操作 |
| 404 | 优惠券不存在 |
| 500 | 服务器内部错误 |

## Testing Strategy

### 单元测试

**前端测试 (Vitest)**
- 表单字段可见性逻辑测试
- 表单验证逻辑测试
- 优惠券值格式化函数测试

**后端测试 (JUnit + jqwik)**
- CreateCouponDTO验证测试
- CouponService业务逻辑测试
- 数据映射正确性测试

### 属性测试

使用 jqwik (Java) 进行属性测试，验证核心正确性属性：

**测试配置：**
- 每个属性测试运行最少 100 次迭代
- 使用智能生成器约束输入空间

**属性测试标注格式：**
```java
// **Feature: merchant-coupon-management, Property 1: 字段可见性与优惠券类型一致性**
// **Validates: Requirements 1.2, 1.3, 1.4**
```

### 集成测试

- API接口测试：验证创建优惠券接口的请求/响应
- 前后端联调测试：验证Web端创建的优惠券在移动端正确显示

