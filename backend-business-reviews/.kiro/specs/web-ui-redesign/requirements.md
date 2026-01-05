# Requirements Document

## Introduction

本需求文档描述了将 business-reviews Web 前端 UI 根据 dianp 设计原型图进行改造的功能需求。目标是使 Web 前端页面与设计原型图保持一致的视觉风格、颜色色调和交互体验。

## Glossary

- **Web前端系统**: 位于 `front-business-reviews-Web` 目录下的 Vue3 + Element Plus 商家运营中心前端应用
- **设计原型图**: 位于 `dianp` 目录下的 HTML/CSS 页面设计原型
- **主色调**: 橙色 (#FF7D00)，用于主要按钮、高亮元素和品牌标识
- **辅助色**: 绿色 (#4CAF50)，用于成功状态和次要操作
- **中性色系**: 从 #F5F5F5 到 #171717 的灰度色阶，用于文本和背景
- **渐变背景**: 从黄色调 (yellow-50) 到绿色调 (green-50) 的渐变背景
- **卡片组件**: 带有圆角、阴影和悬停效果的内容容器

## Requirements

### Requirement 1: 登录页面UI改造

**User Story:** 作为商家用户，我希望看到一个现代化、美观的登录页面，以便获得良好的第一印象和使用体验。

#### Acceptance Criteria

1. WHEN 用户访问登录页面 THEN Web前端系统 SHALL 显示渐变背景（从黄色调到绿色调）
2. WHEN 登录页面加载完成 THEN Web前端系统 SHALL 显示带有橙色顶部装饰条的白色登录卡片
3. WHEN 用户查看登录表单 THEN Web前端系统 SHALL 显示带有橙色图标的输入框
4. WHEN 用户悬停在登录按钮上 THEN Web前端系统 SHALL 显示橙色按钮的悬停效果（颜色加深、轻微放大）
5. WHEN 登录页面显示 THEN Web前端系统 SHALL 在底部显示"还没有账号？立即入驻"的提示

### Requirement 2: 主布局框架改造

**User Story:** 作为商家用户，我希望看到统一风格的导航和布局，以便在不同页面间获得一致的使用体验。

#### Acceptance Criteria

1. WHEN 用户登录成功后 THEN Web前端系统 SHALL 显示白色背景的顶部导航栏，包含平台Logo和用户信息
2. WHEN 用户查看侧边栏 THEN Web前端系统 SHALL 显示白色背景的侧边菜单，宽度为 64 像素（展开时 256 像素）
3. WHEN 用户点击菜单项 THEN Web前端系统 SHALL 显示橙色左边框和橙色背景色的激活状态
4. WHEN 用户悬停在菜单项上 THEN Web前端系统 SHALL 显示浅灰色背景的悬停效果
5. WHEN 主内容区域加载 THEN Web前端系统 SHALL 显示渐变背景（从黄色调到绿色调）

### Requirement 3: 数据看板页面改造

**User Story:** 作为商家用户，我希望看到清晰美观的数据看板，以便快速了解店铺运营状况。

#### Acceptance Criteria

1. WHEN 用户访问数据看板 THEN Web前端系统 SHALL 显示带有圆角和阴影的统计卡片
2. WHEN 统计卡片显示 THEN Web前端系统 SHALL 在右上角显示对应颜色的圆形图标背景
3. WHEN 用户悬停在统计卡片上 THEN Web前端系统 SHALL 显示卡片上浮和阴影加深的效果
4. WHEN 数据增长为正 THEN Web前端系统 SHALL 显示绿色向上箭头和百分比
5. WHEN 数据增长为负 THEN Web前端系统 SHALL 显示红色向下箭头和百分比
6. WHEN 快捷操作区域显示 THEN Web前端系统 SHALL 显示橙色主按钮和白色边框次要按钮

### Requirement 4: 笔记管理页面改造

**User Story:** 作为商家用户，我希望看到清晰的笔记列表和操作界面，以便高效管理我的笔记内容。

#### Acceptance Criteria

1. WHEN 用户访问笔记管理页面 THEN Web前端系统 SHALL 显示带有搜索框和筛选按钮的操作栏
2. WHEN 笔记列表显示 THEN Web前端系统 SHALL 显示带有封面图、标题、分类、状态、阅读量和创建时间的列表项
3. WHEN 笔记状态为"已通过" THEN Web前端系统 SHALL 显示绿色背景的状态标签
4. WHEN 笔记状态为"待审核" THEN Web前端系统 SHALL 显示黄色背景的状态标签
5. WHEN 笔记状态为"已驳回" THEN Web前端系统 SHALL 显示红色背景的状态标签
6. WHEN 用户悬停在列表项上 THEN Web前端系统 SHALL 显示浅灰色背景的悬停效果
7. WHEN 分页组件显示 THEN Web前端系统 SHALL 显示橙色背景的当前页码按钮

### Requirement 5: 优惠券管理页面改造

**User Story:** 作为商家用户，我希望看到直观的优惠券管理界面，以便轻松创建和管理优惠券活动。

#### Acceptance Criteria

1. WHEN 用户访问优惠券管理页面 THEN Web前端系统 SHALL 显示数据概览卡片（总数、已发放、已使用、核销率）
2. WHEN 优惠券卡片显示 THEN Web前端系统 SHALL 显示带有类型标签、状态标签、面额和有效期的卡片
3. WHEN 优惠券状态为"进行中" THEN Web前端系统 SHALL 显示绿色圆点和绿色背景的状态标签
4. WHEN 优惠券状态为"已过期" THEN Web前端系统 SHALL 显示红色圆点和红色背景的状态标签
5. WHEN 优惠券状态为"未开始" THEN Web前端系统 SHALL 显示黄色圆点和黄色背景的状态标签
6. WHEN 用户悬停在优惠券卡片上 THEN Web前端系统 SHALL 显示卡片上浮和顶部彩色装饰条

### Requirement 6: 店铺信息管理页面改造

**User Story:** 作为商家用户，我希望看到清晰的店铺信息编辑界面，以便方便地管理店铺基本信息。

#### Acceptance Criteria

1. WHEN 用户访问店铺信息页面 THEN Web前端系统 SHALL 显示标签页导航（店铺信息、店铺套餐）
2. WHEN 标签页激活 THEN Web前端系统 SHALL 显示橙色下边框和橙色文字
3. WHEN 表单区域显示 THEN Web前端系统 SHALL 显示带有圆角边框的白色卡片容器
4. WHEN 输入框获得焦点 THEN Web前端系统 SHALL 显示橙色边框和橙色阴影
5. WHEN 操作按钮显示 THEN Web前端系统 SHALL 显示橙色主按钮和灰色边框取消按钮

### Requirement 7: 全局样式和主题配置

**User Story:** 作为开发者，我希望有统一的主题配置，以便在整个应用中保持一致的视觉风格。

#### Acceptance Criteria

1. WHEN 应用加载 THEN Web前端系统 SHALL 使用 #FF7D00 作为主色调
2. WHEN 应用加载 THEN Web前端系统 SHALL 使用 #4CAF50 作为辅助色
3. WHEN 应用加载 THEN Web前端系统 SHALL 使用 Inter 字体族作为主要字体
4. WHEN 卡片组件渲染 THEN Web前端系统 SHALL 应用统一的圆角（12px）和阴影样式
5. WHEN 按钮组件渲染 THEN Web前端系统 SHALL 应用统一的圆角（8px）和过渡动画
