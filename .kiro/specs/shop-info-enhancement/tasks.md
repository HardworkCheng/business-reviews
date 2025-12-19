# Implementation Plan

- [x] 1. 修复路由守卫登录重定向问题

  - [x] 1.1 检查并修复路由守卫逻辑，确保未登录用户访问根路径时重定向到登录页


    - 修改 `router/index.ts` 中的 beforeEach 守卫
    - 确保 `requiresAuth` 检查正确执行
    - _Requirements: 4.1, 4.2, 4.3_

- [x] 2. 优化店铺信息页面结构
  - [x] 2.1 移除店铺图片相册区域
    - 删除 `list.vue` 中的店铺图片卡片组件
    - 删除相关的图片上传逻辑和状态
    - _Requirements: 1.1_
  - [x] 2.2 移除调试信息区域
    - 删除调试信息卡片及相关代码
    - _Requirements: 1.2_
  - [x] 2.3 在页面头部添加修改按钮
    - 在标题右侧添加编辑按钮
    - 实现编辑模式状态管理
    - _Requirements: 2.1, 2.2, 2.3_

- [x] 3. 优化地址输入功能
  - [x] 3.1 将省份/城市/区县字段改为可编辑
    - 移除 readonly 属性
    - 添加手动输入支持
    - _Requirements: 3.2, 3.3_
  - [x] 3.2 实现地址自动解析功能
    - 使用高德地图 Geocoder API
    - 根据详细地址自动填充省市区
    - _Requirements: 3.1_
  - [x] 3.3 实现经纬度自动计算

    - 根据完整地址调用地理编码服务
    - 自动填充经纬度字段
    - _Requirements: 3.4, 3.5_

- [x] 4. Checkpoint - 确保所有功能正常工作
  - Ensure all tests pass, ask the user if questions arise.
