# Implementation Plan

## 1. Backend - 新增密码登录功能

- [x] 1.1 创建LoginByPasswordRequest DTO
  - 在backend-business-reviews-entity模块创建请求DTO
  - 包含phone和password字段，添加验证注解
  - _Requirements: 2.4, 5.1_

- [x] 1.2 扩展AuthService接口
  - 在AuthService接口中添加loginByPassword方法
  - _Requirements: 2.4_

- [x] 1.3 实现AuthServiceImpl的loginByPassword方法
  - 验证手机号格式
  - 查询用户是否存在
  - 比对密码（注意：当前密码未加密，直接比对）
  - 生成JWT Token
  - 更新最后登录时间
  - 返回统一的LoginResponse
  - _Requirements: 2.4, 2.5, 5.1, 5.2, 5.3, 5.4_

- [x] 1.4 在AuthController中添加密码登录接口
  - 添加POST /auth/login-by-password端点
  - 调用authService.loginByPassword方法
  - _Requirements: 2.4_

- [ ] 1.5 编写密码登录属性测试
  - **Property 4: Password Authentication Success**
  - **Property 5: Password Authentication Failure**
  - **Property 6: Phone Number Format Validation**
  - **Property 7: Last Login Timestamp Update**
  - **Validates: Requirements 2.4, 2.5, 5.1, 5.2, 5.3, 5.4**

## 2. Frontend - 重构登录页面

- [x] 2.1 修改login.vue - 移除第三方登录相关代码
  - 删除微信、QQ、微博登录按钮
  - 删除wechatLogin、qqLogin、weiboLogin方法
  - 删除分隔线和第三方登录区域
  - 删除相关样式代码
  - _Requirements: 3.1_

- [x] 2.2 修改login.vue - 添加登录模式切换功能
  - 添加loginMode状态变量（'sms' | 'password'）
  - 添加模式切换Tab组件
  - 根据模式显示不同的输入字段
  - _Requirements: 2.1, 2.2, 2.3, 4.1, 4.2_

- [x] 2.3 修改login.vue - 实现密码登录表单
  - 添加password状态变量
  - 添加showPassword状态变量用于密码可见性切换
  - 添加密码输入框和可见性切换按钮
  - _Requirements: 2.3, 4.4_

- [x] 2.4 修改login.vue - 实现密码登录逻辑
  - 添加handlePasswordLogin方法
  - 调用loginByPassword API
  - 处理登录成功和失败情况
  - _Requirements: 2.4, 2.5_

## 3. Frontend - 更新API模块

- [x] 3.1 修改auth.js - 添加密码登录API
  - 添加loginByPassword方法
  - 删除oauthLogin方法（第三方登录）
  - _Requirements: 2.4, 3.3_

## 4. Frontend - 修改设置页面

- [x] 4.1 修改settings.vue - 移除第三方账号绑定区域
  - 删除"第三方账号"section
  - 删除微信、QQ、微博绑定相关代码
  - 删除editWechat、editQQ、editWeibo方法
  - 删除handleSave中的第三方账号字段
  - _Requirements: 3.2_

## 5. Checkpoint - 确保所有修改正确

- [x] 5. Checkpoint - Make sure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## 6. Frontend - 样式优化

- [x] 6.1 优化登录页面样式
  - 调整Tab切换组件样式
  - 优化密码输入框样式
  - 确保整体UI一致性
  - _Requirements: 4.1, 4.2, 4.3, 4.4_

## 7. Final Checkpoint

- [x] 7. Final Checkpoint - Make sure all tests pass
  - Ensure all tests pass, ask the user if questions arise.
