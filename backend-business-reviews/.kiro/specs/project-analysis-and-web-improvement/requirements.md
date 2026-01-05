# 项目分析与Web前端完善需求文档

## 项目概述

本项目是一个商家点评系统，包含以下主要模块：
- **backend-business-reviews**: 后端服务（Spring Boot 3.2.0 + MyBatis-Plus）
- **front-business-reviews-Web**: 商家运营中心前端（Vue 3 + Element Plus）
- **front-business-reviews-Mobile**: 移动端前端（UniApp）

## 当前项目状态

### 后端服务状态

#### 已完成功能
- [x] 商家认证服务 (MerchantAuthService) - 登录、注册、验证码
- [x] JWT Token 认证机制
- [x] 数据库连接配置（MySQL + Redis）
- [x] MyBatis-Plus 集成
- [x] 阿里云 OSS 配置框架

#### 待完善功能
- [x] MerchantCouponService - 优惠券服务（已完成完整业务逻辑）
- [x] MerchantShopService - 门店服务（已完成完整业务逻辑）
- [x] MerchantDashboardService - 数据看板服务（已完成）
- [x] MerchantNoteService - 笔记管理服务（已完成）
- [x] MerchantCommentService - 评论管理服务（已完成）

### 前端项目状态

#### 已完成功能
- [x] 项目基础架构（Vue 3 + Vite + TypeScript）
- [x] Element Plus UI 框架集成
- [x] 路由配置（vue-router）
- [x] 状态管理（Pinia）
- [x] API 请求封装（axios）
- [x] 登录/注册页面
- [x] 主布局框架
- [x] 数据看板页面
- [x] 门店管理页面（列表、创建）
- [x] 优惠券管理页面（列表、创建）
- [x] 笔记管理页面（列表、创建）
- [x] 评论管理页面

#### 已修复问题
- [x] main.ts 中的 Vite 模板代码冲突
- [x] 路由缺少编辑页面路由
- [x] 视图组件中的函数命名冲突
- [x] 缺少 router 导入

---

## 用户故事

### US-001: 商家登录与认证
**作为** 商家用户
**我希望** 能够通过手机号和密码或验证码登录系统
**以便于** 管理我的门店和营销活动

**验收标准:**
- [ ] 支持密码登录
- [ ] 支持验证码登录
- [ ] 登录成功后跳转到数据看板
- [ ] Token 过期自动跳转登录页

### US-002: 数据看板
**作为** 商家用户
**我希望** 在首页看到今日运营数据概览
**以便于** 快速了解店铺运营状况

**验收标准:**
- [ ] 显示今日浏览量、互动数、领券数、核销量
- [ ] 显示数据环比增长趋势
- [ ] 显示近7日浏览趋势图表
- [ ] 显示热门内容TOP5

### US-003: 门店管理
**作为** 商家用户
**我希望** 能够管理我的门店信息
**以便于** 维护门店的基本信息和状态

**验收标准:**
- [ ] 查看门店列表（支持分页、搜索）
- [ ] 创建新门店
- [ ] 编辑门店信息
- [ ] 启用/停用门店

### US-004: 优惠券管理
**作为** 商家用户
**我希望** 能够创建和管理优惠券
**以便于** 开展营销活动吸引顾客

**验收标准:**
- [ ] 查看优惠券列表（支持分页、筛选）
- [ ] 创建不同类型优惠券（现金券、折扣券、专属券、新人券）
- [ ] 编辑优惠券信息
- [ ] 查看优惠券统计数据
- [ ] 核销优惠券

### US-005: 笔记管理
**作为** 商家用户
**我希望** 能够发布和管理店铺笔记
**以便于** 宣传店铺和产品

**验收标准:**
- [ ] 查看笔记列表（支持分页、筛选）
- [ ] 创建笔记（支持图片上传、标签）
- [ ] 编辑笔记
- [ ] 发布/下线笔记
- [ ] 查看笔记数据（浏览量、点赞、评论）

### US-006: 评论管理
**作为** 商家用户
**我希望** 能够查看和回复用户评论
**以便于** 与顾客互动并维护店铺口碑

**验收标准:**
- [ ] 查看评论列表（支持分页、筛选）
- [ ] 回复评论
- [ ] 删除不当评论

---

## 技术要求

### 后端技术栈
- Spring Boot 3.2.0
- MyBatis-Plus
- MySQL 8.0
- Redis
- JWT 认证
- 阿里云 OSS

### 前端技术栈
- Vue 3.5
- TypeScript
- Vite 7
- Element Plus 2.12
- Pinia 3
- Vue Router 4
- Axios
- ECharts 6

### API 接口规范
- 基础路径: `/api`
- 商家端端口: 8081
- 移动端端口: 8080
- 认证方式: Bearer Token

---

## 已完成工作

### 后端服务实现
- [x] MerchantShopServiceImpl - 门店管理完整业务逻辑
- [x] MerchantCouponServiceImpl - 优惠券管理完整业务逻辑
- [x] MerchantNoteServiceImpl - 笔记管理完整业务逻辑
- [x] MerchantCommentServiceImpl - 评论管理完整业务逻辑
- [x] MerchantDashboardServiceImpl - 数据看板完整业务逻辑

### 后端Controller实现
- [x] MerchantNoteController - 笔记管理接口
- [x] MerchantCommentController - 评论管理接口
- [x] MerchantDashboardController - 数据看板接口

### 前端修复
- [x] main.ts - 移除Vite模板代码冲突
- [x] index.html - 更新标题
- [x] router/index.ts - 添加编辑路由
- [x] 所有视图组件 - 修复函数命名和router导入
- [x] API文件 - 添加缺失的接口调用

## 下一步计划

1. **启动测试**
   - 启动后端 MerchantApplication (端口 8081)
   - 启动前端 `npm run dev` (front-business-reviews-Web)
   - 访问 http://localhost:3000 测试功能

2. **联调测试**
   - 前后端接口联调
   - 功能测试
   - 性能优化

## 最新修复 (2024-12-14)

1. **前端API参数修复**
   - 分页参数从 `page/size` 改为 `pageNum/pageSize`
   - 响应数据从 `res.records` 改为 `res.list || res.records`

2. **文件上传修复**
   - 上传URL从 `/api/upload` 改为 `/api/merchant/upload`
   - 添加 Authorization 请求头
