# Requirements Document - 商家优惠券管理系统完善

## Introduction

本需求文档描述了商家运营中心优惠券发放功能的完善方案。核心目标是确保Web端商家运营中心的优惠券创建表单与数据库设计保持一致，并实现商家发放的优惠券能够在移动端用户的优惠券页面正确展示。

系统涉及三个主要组件：
1. **Web端商家运营中心**：商家创建和管理优惠券的界面
2. **后端服务**：处理优惠券的创建、存储和查询
3. **移动端uniapp**：用户查看和领取优惠券的界面

## Glossary

- **Coupon（优惠券）**：商家发放的促销凭证，用户领取后可在消费时抵扣
- **Merchant（商家）**：在平台注册的商户，可以发放优惠券
- **User（用户）**：移动端App用户，可以领取和使用优惠券
- **CouponType（优惠券类型）**：满减券(1)、折扣券(2)、代金券(3)
- **MinAmount（最低消费金额）**：使用优惠券的最低消费门槛
- **Amount（优惠金额）**：满减券和代金券的抵扣金额
- **Discount（折扣）**：折扣券的折扣比例，如0.8表示8折
- **TotalCount（发行总量）**：优惠券的发放总数
- **RemainCount（剩余数量）**：优惠券的剩余可领取数量
- **PerUserLimit（每人限领）**：每个用户可领取该优惠券的最大数量
- **Stackable（可叠加）**：优惠券是否可与其他优惠券同时使用

## Requirements

### Requirement 1

**User Story:** As a merchant, I want to create coupons with all necessary fields matching the database schema, so that the coupon data is stored correctly and consistently.

#### Acceptance Criteria

1. WHEN a merchant opens the coupon creation form THEN the system SHALL display input fields for: title, type, amount, discount, minAmount, totalCount, remainCount, perUserLimit, startTime, endTime, stackable, and description
2. WHEN a merchant selects coupon type as "满减券"(1) THEN the system SHALL display amount and minAmount fields and hide the discount field
3. WHEN a merchant selects coupon type as "折扣券"(2) THEN the system SHALL display discount and minAmount fields and hide the amount field
4. WHEN a merchant selects coupon type as "代金券"(3) THEN the system SHALL display amount field and hide the discount and minAmount fields
5. WHEN a merchant submits the coupon form THEN the system SHALL validate that all required fields are filled according to the selected coupon type
6. WHEN a merchant creates a coupon THEN the system SHALL set remainCount equal to totalCount initially

### Requirement 2

**User Story:** As a merchant, I want the coupon form to use the correct data types and formats, so that the data is compatible with the database schema.

#### Acceptance Criteria

1. WHEN a merchant enters the amount field THEN the system SHALL accept decimal values with up to 2 decimal places (DECIMAL 10,2)
2. WHEN a merchant enters the discount field THEN the system SHALL accept decimal values between 0.01 and 0.99 representing the discount rate
3. WHEN a merchant enters the minAmount field THEN the system SHALL accept decimal values with up to 2 decimal places with a default of 0.00
4. WHEN a merchant enters the totalCount field THEN the system SHALL accept positive integer values
5. WHEN a merchant enters the perUserLimit field THEN the system SHALL accept positive integer values with a default of 1
6. WHEN a merchant selects the validity period THEN the system SHALL capture both startTime and endTime as datetime values

### Requirement 3

**User Story:** As a merchant, I want to optionally specify which shop the coupon applies to, so that I can create shop-specific or merchant-wide coupons.

#### Acceptance Criteria

1. WHEN a merchant creates a coupon THEN the system SHALL provide an optional shop selection dropdown
2. WHEN a merchant does not select a shop THEN the system SHALL set shopId to NULL indicating the coupon applies to all shops
3. WHEN a merchant selects a specific shop THEN the system SHALL associate the coupon with that shop's ID

### Requirement 4

**User Story:** As a user, I want to see all available coupons from merchants in the coupon center, so that I can discover and claim coupons.

#### Acceptance Criteria

1. WHEN a user opens the coupon center THEN the system SHALL display all coupons where status equals 1 (active) and current time is between startTime and endTime and remainCount is greater than 0
2. WHEN displaying a coupon THEN the system SHALL show the coupon title, type indicator, amount or discount value, minAmount condition, and merchant/shop information
3. WHEN a coupon has remainCount equal to 0 THEN the system SHALL display "已抢光" status on the coupon card
4. WHEN a user has already claimed a coupon THEN the system SHALL display "已领取" status on that coupon card

### Requirement 5

**User Story:** As a user, I want to see the correct coupon value display based on coupon type, so that I understand the discount I will receive.

#### Acceptance Criteria

1. WHEN displaying a 满减券(type=1) THEN the system SHALL show "满{minAmount}减{amount}" format
2. WHEN displaying a 折扣券(type=2) THEN the system SHALL show "{discount*10}折" format (e.g., 0.8 displays as "8折")
3. WHEN displaying a 代金券(type=3) THEN the system SHALL show "¥{amount}" format
4. WHEN displaying coupon validity THEN the system SHALL show the date range from startTime to endTime

### Requirement 6

**User Story:** As a developer, I want the frontend form fields to map correctly to the backend API and database schema, so that data flows correctly through the system.

#### Acceptance Criteria

1. WHEN the frontend sends a create coupon request THEN the request body SHALL include fields: shopId, type, title, description, amount, discount, minAmount, totalCount, perUserLimit, startTime, endTime, stackable
2. WHEN the backend receives a create coupon request THEN the system SHALL map the request fields to the CouponDO entity fields
3. WHEN the backend stores a coupon THEN the system SHALL set remainCount equal to totalCount and set createdAt and updatedAt timestamps automatically
4. WHEN the backend returns coupon data THEN the response SHALL include all fields needed for frontend display

