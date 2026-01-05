# 商家运营中心评论管理页面UI重设计 - 设计文档

## 概述

本设计文档描述了商家运营中心评论管理页面的UI重设计方案。该设计基于现代化的UI/UX原则，参考提供的HTML设计稿，实现数据可视化、交互友好的评论管理界面。设计采用Vue 3 + TypeScript + Element Plus技术栈，确保代码质量和可维护性。

## 架构

### 技术栈
- **前端框架**: Vue 3 (Composition API)
- **UI组件库**: Element Plus
- **语言**: TypeScript
- **样式**: SCSS
- **状态管理**: Pinia (如需要)
- **HTTP客户端**: Axios

### 页面结构
```
CommentList.vue
├── DashboardCards (数据概览看板)
│   ├── StatCard (今日新增评论)
│   ├── StatCard (店铺综合评分)
│   ├── StatCard (待回复内容)
│   └── StatCard (差评/投诉待处理)
├── MainPanel (主内容区)
│   ├── Toolbar (工具栏)
│   │   ├── TabFilter (Tab筛选)
│   │   ├── SearchInput (搜索框)
│   │   └── ExportButton (导出按钮)
│   ├── CommentTable (评论表格)
│   │   ├── UserInfoColumn (用户信息列)
│   │   ├── CommentDetailColumn (评论详情列)
│   │   ├── StatusColumn (状态列)
│   │   └── ActionColumn (操作列)
│   └── Pagination (分页组件)
└── ReplyDialog (回复对话框)
```

## 组件和接口

### 1. DashboardCards 组件

**职责**: 展示评论管理的关键数据指标

**Props**:
```typescript
interface DashboardData {
  todayNewComments: number;      // 今日新增评论
  todayTrend: number;            // 较昨日增长百分比
  averageRating: number;         // 店铺综合评分
  ratingTrend: number;           // 评分趋势
  pendingReply: number;          // 待回复数量
  replyTrend: number;            // 回复效率趋势
  negativeComments: number;      // 差评/投诉数量
}
```

**样式特性**:
- 卡片悬停上浮效果 (transform: translateY(-4px))
- 渐变色图标背景
- 趋势指标颜色编码 (绿色上升/红色下降/灰色持平)
- 响应式布局 (el-row + el-col)

### 2. CommentTable 组件

**职责**: 展示评论列表和详细信息

**数据模型**:
```typescript
interface Comment {
  id: number;
  userId: number;
  userName: string;
  userAvatar: string;
  isVip: boolean;
  isAnonymous: boolean;
  rating: number;                // 1-5星评分
  content: string;
  images: string[];              // 评论图片URL数组
  goodsName: string;             // 关联商品名称
  orderId?: string;              // 订单号
  createTime: string;
  status: 'published' | 'deleted';
  reply?: string;                // 商家回复内容
  replyTime?: string;
  isTop: boolean;                // 是否置顶
}
```

**列定义**:
1. **用户信息列** (width: 220px)
   - 圆形头像 (44px, 带阴影)
   - 用户名
   - VIP/匿名标签

2. **评论详情列** (min-width: 400px)
   - 评分星级 + 评论时间 + 商品标签
   - 评论文本内容
   - 评论图片网格 (60x60px, 支持预览)
   - 商家回复区域 (橙色背景, 左侧边框)

3. **状态列** (width: 120px, center)
   - 状态点 + 状态文字
   - 颜色编码 (绿色正常/灰色已删除)

4. **操作列** (width: 180px, center, fixed right)
   - 回复按钮 (蓝色悬停)
   - 置顶按钮 (橙色悬停)
   - 删除按钮 (红色悬停)

### 3. TabFilter 组件

**职责**: 提供评论状态分类筛选

**Tab定义**:
```typescript
interface TabItem {
  name: string;                  // 'all' | 'published' | 'negative' | 'deleted'
  label: string;                 // 显示文本
  count: number;                 // 该分类评论数量
}
```

**交互**:
- 点击Tab切换评论列表
- 保持搜索条件
- 更新URL参数 (可选)

### 4. ReplyDialog 组件

**职责**: 商家回复评论的对话框

**表单模型**:
```typescript
interface ReplyForm {
  commentId: number;
  content: string;               // 回复内容
}
```

**验证规则**:
- content: 必填, 最小长度5, 最大长度500

## 数据模型

### API接口

#### 1. 获取数据概览
```typescript
GET /api/merchant/comments/dashboard
Response: {
  todayNewComments: number;
  todayTrend: number;
  averageRating: number;
  ratingTrend: number;
  pendingReply: number;
  replyTrend: number;
  negativeComments: number;
}
```

#### 2. 获取评论列表
```typescript
GET /api/merchant/comments/list
Params: {
  pageNum: number;
  pageSize: number;
  status?: 'all' | 'published' | 'negative' | 'deleted';
  keyword?: string;              // 搜索关键词
}
Response: {
  list: Comment[];
  total: number;
}
```

#### 3. 回复评论
```typescript
POST /api/merchant/comments/{commentId}/reply
Body: {
  content: string;
}
Response: {
  success: boolean;
  message: string;
}
```

#### 4. 置顶评论
```typescript
PUT /api/merchant/comments/{commentId}/top
Body: {
  isTop: boolean;
}
Response: {
  success: boolean;
}
```

#### 5. 删除评论
```typescript
DELETE /api/merchant/comments/{commentId}
Response: {
  success: boolean;
  message: string;
}
```

#### 6. 导出评论数据
```typescript
GET /api/merchant/comments/export
Params: {
  status?: string;
  keyword?: string;
}
Response: Blob (Excel文件)
```

## 正确性属性

*属性是一个特征或行为，应该在系统的所有有效执行中保持为真。属性作为人类可读规范和机器可验证正确性保证之间的桥梁。*

### 属性 1: 数据卡片数值一致性
*对于任何* 评论数据集，数据概览看板中显示的各项统计数值应与实际评论数据计算结果一致
**验证: 需求 1.2**

### 属性 2: Tab筛选结果正确性
*对于任何* Tab筛选条件，返回的评论列表应只包含符合该状态的评论
**验证: 需求 2.3**

### 属性 3: 搜索结果包含性
*对于任何* 搜索关键词，返回的评论列表中每条评论的内容、用户名或订单号应包含该关键词
**验证: 需求 5.2**

### 属性 4: 分页数据完整性
*对于任何* 分页参数，返回的评论数据总数应等于所有页面评论数量之和
**验证: 需求 7.3**

### 属性 5: 回复操作幂等性
*对于任何* 评论，多次提交相同的回复内容应只创建一条回复记录
**验证: 需求 4.3**

### 属性 6: 删除操作不可逆性
*对于任何* 已删除的评论，其状态应永久标记为已删除，不应出现在"正常显示"Tab中
**验证: 需求 4.5**

### 属性 7: 状态显示一致性
*对于任何* 评论，其在列表中显示的状态应与数据库中存储的状态一致
**验证: 需求 3.5**

## 错误处理

### 1. API请求失败
- 显示友好的错误提示消息
- 保持当前页面状态
- 提供重试选项

### 2. 数据加载失败
- 显示空状态占位符
- 提供刷新按钮
- 记录错误日志

### 3. 表单验证失败
- 高亮显示错误字段
- 显示具体错误信息
- 阻止表单提交

### 4. 网络超时
- 显示超时提示
- 自动重试机制 (最多3次)
- 提供手动刷新选项

### 5. 权限不足
- 显示权限不足提示
- 隐藏无权限操作按钮
- 引导用户联系管理员

## 测试策略

### 单元测试
- 测试数据计算逻辑 (趋势百分比、评分平均值)
- 测试状态筛选逻辑
- 测试搜索关键词匹配逻辑
- 测试表单验证规则

### 组件测试
- 测试DashboardCards组件渲染
- 测试CommentTable组件交互
- 测试TabFilter切换功能
- 测试ReplyDialog表单提交

### 集成测试
- 测试完整的评论列表加载流程
- 测试回复评论的端到端流程
- 测试删除评论的端到端流程
- 测试导出数据功能

### 视觉回归测试
- 测试数据卡片悬停效果
- 测试表格行悬停效果
- 测试按钮悬停颜色变化
- 测试响应式布局

### 性能测试
- 测试大量评论数据的渲染性能
- 测试图片懒加载效果
- 测试分页切换速度
- 测试搜索响应时间

## 样式规范

### 颜色系统
```scss
// 主色调
$primary-color: #1890ff;
$success-color: #52c41a;
$warning-color: #fa8c16;
$danger-color: #f5222d;
$info-color: #8c9eae;

// 背景色
$bg-primary: #ffffff;
$bg-secondary: #f4f7fc;
$bg-hover: #fcfcfd;
$bg-card: #fafafa;

// 文字颜色
$text-primary: #1a1a1a;
$text-secondary: #595959;
$text-tertiary: #8c8c8c;
$text-disabled: #bfbfbf;

// 边框颜色
$border-light: #f0f0f0;
$border-base: #e6eaf0;
```

### 间距系统
```scss
$spacing-xs: 4px;
$spacing-sm: 8px;
$spacing-md: 12px;
$spacing-lg: 16px;
$spacing-xl: 24px;
```

### 圆角系统
```scss
$radius-sm: 4px;
$radius-md: 8px;
$radius-lg: 12px;
$radius-xl: 16px;
$radius-circle: 50%;
```

### 阴影系统
```scss
$shadow-sm: 0 2px 6px rgba(0, 0, 0, 0.05);
$shadow-md: 0 4px 12px rgba(0, 0, 0, 0.08);
$shadow-lg: 0 12px 24px rgba(0, 0, 0, 0.12);
```

### 动画系统
```scss
$transition-fast: 0.2s cubic-bezier(0.25, 0.8, 0.25, 1);
$transition-base: 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
$transition-slow: 0.5s cubic-bezier(0.25, 0.8, 0.25, 1);
```

## 实现注意事项

### 1. 性能优化
- 使用虚拟滚动处理大量评论数据
- 图片懒加载和缩略图优化
- 防抖搜索输入
- 缓存数据概览数据

### 2. 可访问性
- 为图标按钮添加aria-label
- 确保键盘导航支持
- 提供足够的颜色对比度
- 支持屏幕阅读器

### 3. 响应式设计
- 移动端适配 (可选)
- 平板端布局优化
- 大屏幕最大宽度限制 (1600px)

### 4. 国际化
- 使用i18n支持多语言
- 日期时间格式本地化
- 数字格式本地化

### 5. 安全性
- XSS防护 (评论内容转义)
- CSRF令牌验证
- 权限验证
- 敏感操作二次确认
