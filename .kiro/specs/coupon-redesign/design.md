# Design Document - 优惠券页面重新设计

## Overview

本设计文档描述了基于参考 HTML 设计重新实现 uniapp 优惠券页面的技术方案。该系统采用前后端分离架构，移动端使用 Vue 3 + uniapp 框架，后端使用 Spring Boot + MyBatis Plus，数据库使用 MySQL。

核心功能包括：
1. **领券中心**：展示可领取的优惠券，包括限时秒杀和普通商家券
2. **我的卡包**：管理用户已领取的优惠券，支持状态筛选
3. **优惠券领取**：用户领取优惠券并生成唯一券码
4. **优惠券使用**：用户在商家处核销优惠券
5. **搜索功能**：快速查找优惠券

## Architecture

### 系统架构图

```
┌─────────────────────────────────────────────────────────────┐
│                        移动端 (uniapp)                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  领券中心页面  │  │  我的卡包页面  │  │   搜索组件    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP/HTTPS
┌──────────────────────────▼──────────────────────────────────┐
│                    后端服务 (Spring Boot)                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Controller 层 │  │  Service 层   │  │  Mapper 层   │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                      数据库 (MySQL)                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  coupons 表   │  │user_coupons表 │  │seckill表系列 │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
```

### 技术栈

**前端（移动端）**
- 框架：Vue 3 + uniapp
- 状态管理：Composition API (ref, computed)
- HTTP 客户端：uni.request

**后端**
- 框架：Spring Boot 2.x
- ORM：MyBatis Plus
- 数据库：MySQL 8.0

## Components and Interfaces

### 前端组件结构

```
discount.vue（优惠券主页面）
├── CustomNavbar（自定义导航栏 - 领券中心/我的卡包切换）
├── CouponCenter（领券中心视图）
│   ├── SearchBox（搜索框）
│   ├── SeckillZone（秒杀专区）
│   │   └── SeckillCard（秒杀卡片）
│   └── CouponList（优惠券列表）
│       └── CouponCard（优惠券卡片）
└── MyWallet（我的卡包视图）
    ├── WalletTabs（状态筛选标签）
    ├── MyCouponList（我的优惠券列表）
    │   └── MyCouponCard（我的优惠券卡片）
    └── EmptyState（空状态）
```

### 后端 API 接口

| 接口 | 方法 | 描述 |
|------|------|------|
| `/app/coupons/available` | GET | 获取可领取优惠券列表 |
| `/app/seckill/activities` | GET | 获取秒杀活动列表 |
| `/app/coupons/{id}/claim` | POST | 领取优惠券 |
| `/app/coupons/my` | GET | 获取我的优惠券列表 |
| `/merchant/coupons/redeem` | POST | 核销优惠券 |

## Data Models

### 核心数据模型

**Coupon（优惠券）**
- id, merchantId, shopId, type, title, description
- amount, discount, minAmount
- totalCount, remainCount, perUserLimit
- startTime, endTime, status

**UserCoupon（用户优惠券）**
- id, couponId, userId, code, status
- receiveTime, useTime, useShopId

**SeckillActivity（秒杀活动）**
- id, merchantId, title, startTime, endTime, status

**SeckillCoupon（秒杀券关联）**
- id, seckillId, couponId, seckillPrice, originalPrice, seckillStock



## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

基于需求文档的验收标准分析，以下是核心的正确性属性：

### Property 1: 优惠券状态渲染一致性
*For any* 优惠券数据，渲染后的卡片状态（可领取/已领取/已抢光）应与优惠券的 remainCount 和用户领取记录保持一致
**Validates: Requirements 3.3, 3.4, 3.5**

### Property 2: 秒杀券库存状态映射
*For any* 秒杀券，当 remainStock > 阈值时显示"立即抢"，当 0 < remainStock <= 阈值时显示剩余数量，当 remainStock = 0 时显示"已抢光"
**Validates: Requirements 2.4, 2.5, 2.6**

### Property 3: 倒计时计算正确性
*For any* 秒杀活动的结束时间，倒计时显示的时分秒应等于 endTime - currentTime 的正确转换
**Validates: Requirements 2.3**

### Property 4: 优惠券筛选正确性
*For any* 用户优惠券列表和筛选状态，筛选后的列表应只包含匹配该状态的优惠券
**Validates: Requirements 5.3**

### Property 5: 领取操作登录检查
*For any* 领取优惠券操作，如果用户未登录，系统应阻止领取并提示登录
**Validates: Requirements 4.1, 4.2**

### Property 6: 领取后库存扣减
*For any* 成功的优惠券领取操作，优惠券的 remainCount 应减少 1
**Validates: Requirements 9.1**

### Property 7: 券码唯一性
*For any* 两次不同的优惠券领取操作，生成的券码应不相同
**Validates: Requirements 9.5**

### Property 8: 领取上限检查
*For any* 用户对同一优惠券的领取操作，如果已领取次数 >= perUserLimit，系统应拒绝领取
**Validates: Requirements 9.4**

### Property 9: 搜索过滤正确性
*For any* 搜索关键词，返回的优惠券列表中每个优惠券的标题或商家名称应包含该关键词
**Validates: Requirements 7.2**

### Property 10: 核销状态更新
*For any* 成功的核销操作，用户优惠券的状态应从"未使用"变为"已使用"，且记录使用时间和门店
**Validates: Requirements 6.4**

## Error Handling

### 前端错误处理

| 错误场景 | 处理方式 |
|---------|---------|
| 网络请求失败 | 显示 Toast 提示"网络异常，请重试" |
| 未登录领取 | 显示 Toast 提示"请先登录"，跳转登录页 |
| 领取失败（已达上限） | 显示 Toast 提示"已达领取上限" |
| 领取失败（已抢光） | 显示 Toast 提示"优惠券已抢光" |
| 数据加载失败 | 显示空状态或重试按钮 |

### 后端错误码

| 错误码 | 描述 |
|-------|------|
| 400001 | 优惠券不存在 |
| 400002 | 优惠券已下架 |
| 400003 | 优惠券已抢光 |
| 400004 | 已达领取上限 |
| 400005 | 优惠券已过期 |
| 400006 | 券码无效 |
| 400007 | 优惠券已使用 |

## Testing Strategy

### 单元测试

使用 Vitest（前端）和 JUnit（后端）进行单元测试：

**前端测试重点：**
- 优惠券状态计算函数
- 倒计时计算函数
- 筛选逻辑函数
- 数据格式化函数

**后端测试重点：**
- 优惠券领取服务
- 库存扣减逻辑
- 券码生成逻辑
- 核销服务

### 属性测试

使用 fast-check（前端）进行属性测试，验证核心正确性属性：

**测试配置：**
- 每个属性测试运行最少 100 次迭代
- 使用智能生成器约束输入空间

**属性测试标注格式：**
```javascript
// **Feature: coupon-redesign, Property 1: 优惠券状态渲染一致性**
// **Validates: Requirements 3.3, 3.4, 3.5**
```

### 集成测试

- API 接口测试
- 前后端联调测试
- 秒杀并发测试
