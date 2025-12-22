# Implementation Plan - 商家优惠券管理系统完善

- [x] 1. 重构Web端优惠券创建表单
  - [x] 1.1 更新优惠券类型选项
    - 将类型选项从（现金券/折扣券/专属券/新人券）改为（满减券/折扣券/代金券）
    - 更新type值映射：1=满减券，2=折扣券，3=代金券
    - _Requirements: 1.1_
  - [x] 1.2 实现动态字段显示逻辑
    - 满减券(type=1)：显示amount和minAmount字段
    - 折扣券(type=2)：显示discount和minAmount字段
    - 代金券(type=3)：仅显示amount字段
    - 移除maxDiscount和applicableShops字段
    - _Requirements: 1.2, 1.3, 1.4_
  - [ ]* 1.3 编写属性测试：字段可见性与优惠券类型一致性
    - **Property 1: 字段可见性与优惠券类型一致性**
    - **Validates: Requirements 1.2, 1.3, 1.4**
  - [x] 1.4 更新表单字段名称和数据类型
    - 将threshold字段改为minAmount
    - 将discountRate字段改为discount（使用小数格式0.01-0.99）
    - 添加discount输入提示（如：输入0.8表示8折）
    - _Requirements: 2.1, 2.2, 2.3_
  - [x] 1.5 更新表单验证规则
    - 根据优惠券类型验证必填字段
    - 验证amount和minAmount为非负数且最多2位小数
    - 验证discount在0.01-0.99范围内
    - 验证totalCount和perUserLimit为正整数
    - _Requirements: 1.5, 2.4, 2.5_
  - [ ]* 1.6 编写属性测试：表单验证与类型必填字段一致性
    - **Property 2: 表单验证与类型必填字段一致性**
    - **Validates: Requirements 1.5, 2.1, 2.2, 2.3**

- [x] 2. 更新Web端API调用
  - [x] 2.1 更新createCoupon请求数据格式
    - 确保请求体包含正确的字段名：shopId, type, title, description, amount, discount, minAmount, totalCount, perUserLimit, startTime, endTime, stackable
    - 移除不需要的字段：threshold, discountRate, maxDiscount, applicableShops
    - _Requirements: 6.1_
  - [x] 2.2 更新表单提交数据处理
    - 将validityPeriod数组拆分为startTime和endTime
    - 将stackable布尔值转换为整数（0/1）
    - _Requirements: 6.1, 2.6_

- [x] 3. Checkpoint - 确保Web端表单功能正常
  - Ensure all tests pass, ask the user if questions arise.

- [x] 4. 更新后端DTO和服务
  - [x] 4.1 清理CreateCouponDTO中的冗余字段
    - 移除threshold字段（使用minAmount）
    - 移除discountRate字段（使用discount）
    - 移除maxDiscount字段
    - 移除applicableShops字段
    - _Requirements: 6.2_
  - [x] 4.2 更新MerchantCouponService创建逻辑
    - 确保remainCount初始化为totalCount
    - 确保createdAt和updatedAt自动设置
    - _Requirements: 1.6, 6.3_
  - [ ]* 4.3 编写属性测试：剩余数量初始化正确性
    - **Property 3: 剩余数量初始化正确性**
    - **Validates: Requirements 1.6, 6.3**

- [x] 5. 更新移动端优惠券展示
  - [x] 5.1 更新优惠券值显示格式
    - 满减券(type=1)：显示"满{minAmount}减{amount}"
    - 折扣券(type=2)：显示"{discount*10}折"
    - 代金券(type=3)：显示"¥{amount}"
    - _Requirements: 5.1, 5.2, 5.3_
  - [ ]* 5.2 编写属性测试：优惠券值显示格式正确性
    - **Property 6: 优惠券值显示格式正确性**
    - **Validates: Requirements 5.1, 5.2, 5.3**
  - [x] 5.3 更新优惠券卡片条件显示
    - 根据minAmount显示使用条件（如"满100可用"）
    - 当minAmount为0时显示"无门槛"
    - _Requirements: 4.2_
  - [x] 5.4 更新优惠券状态显示逻辑
    - remainCount=0时显示"已抢光"
    - 用户已领取时显示"已领取"
    - 其他情况显示"领取"
    - _Requirements: 4.3, 4.4_
  - [ ]* 5.5 编写属性测试：优惠券状态显示正确性
    - **Property 5: 优惠券状态显示正确性**
    - **Validates: Requirements 4.3, 4.4**

- [x] 6. 更新后端优惠券查询接口
  - [x] 6.1 更新可领取优惠券查询逻辑
    - 过滤条件：status=1 AND 当前时间在startTime和endTime之间 AND remainCount>0
    - 返回字段包含：id, merchantId, shopId, type, title, description, amount, discount, minAmount, totalCount, remainCount, perUserLimit, startTime, endTime, stackable
    - _Requirements: 4.1, 6.4_
  - [ ]* 6.2 编写属性测试：可领取优惠券过滤正确性
    - **Property 4: 可领取优惠券过滤正确性**
    - **Validates: Requirements 4.1**

- [x] 7. Checkpoint - 确保前后端联调正常
  - Ensure all tests pass, ask the user if questions arise.

- [x] 8. Final Checkpoint - 确保所有测试通过

  - Ensure all tests pass, ask the user if questions arise.
