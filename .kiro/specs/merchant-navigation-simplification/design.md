# Design Document

## Overview

本设计文档描述商家运营中心Web端导航栏简化功能的技术实现方案。该功能将移除数据看板和UniApp对接相关模块,保留核心业务功能(门店管理、笔记管理、优惠券、评论管理、系统设置),使界面更加简洁、聚焦于核心商家运营功能。

## Architecture

### Component Structure

```
MainLayout.vue (主布局组件)
├── Sidebar (侧边栏导航)
│   ├── Logo Section
│   └── Menu Items
│       ├── Shop Management (门店管理)
│       ├── Note Management (笔记管理)
│       ├── Coupon Management (优惠券)
│       ├── Comment Management (评论管理)
│       └── System Settings (系统设置)
└── Header (顶部导航栏)
    ├── Collapse Button
    ├── Breadcrumb
    └── User Dropdown
```

### Router Structure

```
/login (登录页)
/ (主布局)
├── /shops (门店列表)
├── /shops/create (创建门店)
├── /shops/edit/:id (编辑门店)
├── /notes (笔记列表)
├── /notes/create (创建笔记)
├── /notes/edit/:id (编辑笔记)
├── /coupons (优惠券列表)
├── /coupons/create (创建优惠券)
├── /coupons/edit/:id (编辑优惠券)
├── /comments (评论列表)
└── /settings (系统设置)
```

## Components and Interfaces

### MainLayout.vue

**修改内容:**
1. 移除侧边栏中的"数据看板"菜单项
2. 移除侧边栏中的"UniApp对接"子菜单及其所有子项
3. 移除顶部导航栏中的"UniApp同步状态"显示
4. 调整默认路由重定向到门店管理页面

**保留内容:**
- Logo区域
- 折叠按钮
- 面包屑导航
- 通知徽章
- 用户下拉菜单
- 核心业务菜单项

### Router Configuration (router/index.ts)

**修改内容:**
1. 移除Dashboard路由及组件导入
2. 移除UniApp相关路由(users, messages, analytics)
3. 修改根路径默认重定向到/shops
4. 添加路由守卫处理已删除路由的访问

**保留内容:**
- 登录路由
- 核心业务路由(shops, notes, coupons, comments, settings)
- 路由守卫逻辑

## Data Models

### Menu Item Structure

```typescript
interface MenuItem {
  index: string        // 路由路径
  icon: Component      // 图标组件
  label: string        // 显示文本
  visible: boolean     // 是否显示
}
```

### Route Configuration

```typescript
interface RouteConfig {
  path: string
  name: string
  component: Component
  meta?: {
    requiresAuth?: boolean
    activeMenu?: string
  }
  children?: RouteConfig[]
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Navigation Menu Exclusion

*For any* render of the Navigation Sidebar, the menu items SHALL NOT include Dashboard or UniApp Integration items
**Validates: Requirements 1.1, 2.1, 2.2**

### Property 2: Core Menu Items Presence

*For any* render of the Navigation Sidebar, all five core business menu items (Shop Management, Note Management, Coupon Management, Comment Management, System Settings) SHALL be present and visible
**Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5**

### Property 3: Menu Item Ordering

*For any* render of the Navigation Sidebar, the menu items SHALL appear in the specified order: Shop Management, Note Management, Coupon Management, Comment Management, System Settings
**Validates: Requirements 4.1**

### Property 4: Header Sync Status Exclusion

*For any* render of the Header component, the UniApp sync status indicator SHALL NOT be present in the DOM
**Validates: Requirements 5.1**

### Property 5: Route Redirect Behavior

*For any* navigation attempt to a removed route (/, /users, /messages, /analytics), the router SHALL redirect to /shops
**Validates: Requirements 1.2, 2.3, 6.2**

### Property 6: Route Configuration Consistency

*For any* route defined in the router configuration, there SHALL exist a corresponding menu item in the Navigation Sidebar OR the route SHALL be a sub-route (create/edit)
**Validates: Requirements 6.1**

## Error Handling

### Route Not Found

当用户尝试访问已删除的路由时:
1. 路由守卫捕获导航请求
2. 检查目标路由是否在已删除列表中
3. 执行重定向到/shops
4. 不显示错误消息(静默重定向)

### Component Loading Errors

如果核心业务组件加载失败:
1. 显示友好的错误提示
2. 提供重试按钮
3. 记录错误日志

### Menu Rendering Errors

如果菜单渲染失败:
1. 使用降级UI显示基本导航
2. 确保至少可以访问系统设置页面
3. 记录错误日志

## Testing Strategy

### Unit Testing

使用Vitest进行单元测试,覆盖以下场景:

1. **MainLayout Component Tests**
   - 验证侧边栏不包含Dashboard菜单项
   - 验证侧边栏不包含UniApp对接子菜单
   - 验证所有核心业务菜单项存在
   - 验证菜单项顺序正确
   - 验证Header不包含UniApp同步状态

2. **Router Configuration Tests**
   - 验证Dashboard路由不存在
   - 验证UniApp相关路由不存在
   - 验证核心业务路由存在且配置正确
   - 验证路由重定向逻辑

3. **Navigation Guard Tests**
   - 验证访问已删除路由时重定向到/shops
   - 验证正常路由导航不受影响

### Property-Based Testing

使用fast-check进行属性测试,配置每个测试运行至少100次迭代:

1. **Property Test for Menu Rendering**
   - 生成随机组件状态
   - 验证渲染结果始终不包含已删除菜单项
   - 验证核心菜单项始终存在且顺序正确

2. **Property Test for Route Handling**
   - 生成随机路由路径
   - 验证已删除路由始终重定向
   - 验证有效路由正常导航

### Integration Testing

使用Cypress进行端到端测试:

1. **Navigation Flow Tests**
   - 测试用户登录后默认进入门店管理页面
   - 测试点击各个菜单项正常跳转
   - 测试直接访问已删除路由URL被重定向

2. **UI Consistency Tests**
   - 测试侧边栏展开/折叠功能
   - 测试面包屑导航显示正确
   - 测试响应式布局

## Implementation Notes

### File Changes

需要修改的文件:
1. `src/layouts/MainLayout.vue` - 移除菜单项和同步状态
2. `src/router/index.ts` - 移除路由配置和添加重定向
3. 可选删除的文件:
   - `src/views/dashboard/index.vue`
   - `src/views/user/list.vue`
   - `src/views/message/list.vue`
   - `src/views/analytics/index.vue`
   - `src/api/dashboard.ts` (如果存在)

### Backward Compatibility

考虑到可能存在的书签或外部链接:
1. 保留路由重定向逻辑,不直接删除路由定义
2. 在路由守卫中添加重定向处理
3. 考虑添加过渡期的提示消息(可选)

### Performance Considerations

1. 移除Dashboard组件后,减少首页加载的图表库依赖
2. 减少菜单项数量,提升侧边栏渲染性能
3. 移除UniApp同步状态轮询,减少不必要的网络请求

## Migration Path

### Phase 1: Remove UI Elements
1. 修改MainLayout.vue移除菜单项
2. 移除Header中的同步状态显示
3. 测试UI渲染正常

### Phase 2: Update Router
1. 修改router/index.ts移除路由
2. 添加重定向逻辑
3. 测试路由导航正常

### Phase 3: Cleanup (Optional)
1. 删除不再使用的组件文件
2. 删除不再使用的API文件
3. 更新相关文档

### Phase 4: Testing & Deployment
1. 运行所有测试
2. 进行手动测试
3. 部署到生产环境
