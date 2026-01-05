# Implementation Plan - 优惠券页面重新设计

- [x] 1. 创建优惠券页面基础结构和导航组件

  - [x] 1.1 重构 discount.vue 页面结构，添加顶部切换导航栏


    - 创建 segment-control 分段控制器组件
    - 实现"领券中心"和"我的卡包"切换功能
    - 添加滑块动画效果
    - _Requirements: 1.1, 1.2_
  - [x] 1.2 创建页面视图切换逻辑


    - 实现 currentView 状态管理
    - 添加视图切换动画（fadeIn）
    - _Requirements: 1.2, 10.1_

- [x] 2. 实现领券中心视图

  - [x] 2.1 创建搜索框组件

    - 实现搜索框 UI 样式
    - 添加搜索输入和清空功能
    - _Requirements: 1.3, 7.1, 7.4_
  - [x] 2.2 实现秒杀专区组件

    - 创建秒杀专区容器和标题
    - 实现倒计时显示和更新逻辑
    - 创建横向滚动的秒杀卡片列表
    - _Requirements: 2.1, 2.2, 2.3_
  - [ ]* 2.3 编写属性测试：倒计时计算正确性
    - **Property 3: 倒计时计算正确性**
    - **Validates: Requirements 2.3**
  - [x] 2.4 实现秒杀卡片组件

    - 显示秒杀价格、商品名称、进度条
    - 根据库存状态显示不同按钮（立即抢/仅剩X张/已抢光）
    - _Requirements: 2.4, 2.5, 2.6_
  - [ ]* 2.5 编写属性测试：秒杀券库存状态映射
    - **Property 2: 秒杀券库存状态映射**
    - **Validates: Requirements 2.4, 2.5, 2.6**
  - [x] 2.6 实现普通优惠券列表

    - 创建"好店神券"列表标题
    - 实现优惠券卡片组件（商家logo、名称、券标题、条件、金额）
    - 根据状态显示领取/已领取/已抢光按钮
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_
  - [ ]* 2.7 编写属性测试：优惠券状态渲染一致性
    - **Property 1: 优惠券状态渲染一致性**
    - **Validates: Requirements 3.3, 3.4, 3.5**

- [x] 3. 实现优惠券领取功能

  - [x] 3.1 实现领取前登录检查

    - 检查用户登录状态
    - 未登录时显示提示并跳转登录页
    - _Requirements: 4.1, 4.2_
  - [ ]* 3.2 编写属性测试：领取操作登录检查
    - **Property 5: 领取操作登录检查**
    - **Validates: Requirements 4.1, 4.2**
  - [x] 3.3 实现优惠券领取 API 调用

    - 调用后端领取接口
    - 处理成功和失败响应
    - 显示 Toast 提示
    - _Requirements: 4.3, 4.4, 4.5_
  - [x] 3.4 实现领取后状态更新

    - 更新优惠券状态为"已领取"
    - 更新秒杀券进度条和剩余数量
    - _Requirements: 4.4, 4.6_

- [x] 4. Checkpoint - 确保所有测试通过

  - Ensure all tests pass, ask the user if questions arise.

- [x] 5. 实现我的卡包视图

  - [x] 5.1 创建状态筛选标签组件

    - 实现全部/未使用/已使用/已过期标签
    - 添加标签切换动画
    - _Requirements: 5.1_
  - [x] 5.2 实现优惠券筛选逻辑

    - 根据选中状态过滤优惠券列表
    - _Requirements: 5.3_
  - [ ]* 5.3 编写属性测试：优惠券筛选正确性
    - **Property 4: 优惠券筛选正确性**
    - **Validates: Requirements 5.3**
  - [x] 5.4 实现我的优惠券卡片组件

    - 显示优惠金额、券名称、商家名称、有效期
    - 根据状态显示"去使用"按钮或"已核销"/"已过期"标记
    - 已使用/已过期卡片降低透明度
    - _Requirements: 5.4, 5.5, 5.6, 5.7_
  - [x] 5.5 实现空状态组件

    - 显示空状态图标和提示文字
    - 添加"去领券中心"按钮
    - _Requirements: 5.8_

- [x] 6. 实现优惠券使用功能

  - [x] 6.1 实现"去使用"按钮点击逻辑

    - 跳转到对应商家详情页
    - 全部商家适用时显示提示
    - _Requirements: 6.1, 6.2_
  - [x] 6.2 实现券码展示功能

    - 显示可供商家扫描或输入的券码
    - _Requirements: 6.3_

- [x] 7. 实现搜索功能

  - [x] 7.1 实现搜索过滤逻辑

    - 根据关键词过滤优惠券列表
    - 搜索结果为空时显示提示
    - _Requirements: 7.2, 7.3_
  - [ ]* 7.2 编写属性测试：搜索过滤正确性
    - **Property 9: 搜索过滤正确性**
    - **Validates: Requirements 7.2**

- [x] 8. Checkpoint - 确保所有测试通过

  - Ensure all tests pass, ask the user if questions arise.

- [x] 9. 创建移动端 API 接口

  - [x] 9.1 创建优惠券 API 模块


    - 实现 getAvailableCoupons 接口
    - 实现 getSeckillActivities 接口
    - 实现 claimCoupon 接口
    - 实现 getMyCoupons 接口
    - _Requirements: 1.4, 2.1, 4.3, 5.2_

- [x] 10. 后端接口开发（如需要）

  - [x] 10.1 创建秒杀活动相关接口


    - 实现获取进行中秒杀活动列表
    - 实现秒杀券领取逻辑（含库存扣减）
    - _Requirements: 2.1, 4.6, 8.3_
  - [ ]* 10.2 编写属性测试：领取后库存扣减
    - **Property 6: 领取后库存扣减**
    - **Validates: Requirements 9.1**
  - [ ]* 10.3 编写属性测试：券码唯一性
    - **Property 7: 券码唯一性**
    - **Validates: Requirements 9.5**
  - [ ]* 10.4 编写属性测试：领取上限检查
    - **Property 8: 领取上限检查**
    - **Validates: Requirements 9.4**
  - [x] 10.5 实现优惠券核销接口


    - 验证券码有效性
    - 更新优惠券状态
    - 记录使用时间和门店
    - _Requirements: 6.4_
  - [ ]* 10.6 编写属性测试：核销状态更新
    - **Property 10: 核销状态更新**
    - **Validates: Requirements 6.4**

- [x] 11. UI 样式优化

  - [x] 11.1 实现优惠券卡片样式

    - 添加阴影和圆角效果
    - 实现点击缩放反馈
    - _Requirements: 10.3, 10.6_
  - [x] 11.2 实现 Toast 提示组件

    - 创建统一的 Toast 样式
    - 支持成功/失败/信息类型
    - _Requirements: 10.4_

- [x] 12. Final Checkpoint - 确保所有测试通过


  - Ensure all tests pass, ask the user if questions arise.
