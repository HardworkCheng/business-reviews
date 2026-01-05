# Requirements Document

## Introduction

本文档定义了笔记分享页面 UI/UX 重构的需求。该重构旨在将现有的全屏长列表布局改造为更现代的底部弹窗（Bottom Sheet）交互模式，参考抖音等主流应用的分享界面设计，提升用户体验和视觉美感。

## Glossary

- **Bottom Sheet**: 从屏幕底部向上滑出的半屏弹窗组件，常用于选择、分享等场景
- **ShareUserList**: 合并后的可分享用户列表，包含关注者和粉丝（去重后）
- **FollowingList**: 当前用户关注的用户列表
- **FansList**: 当前用户的粉丝列表
- **Deduplication**: 去重处理，确保同一用户在列表中只出现一次
- **Horizontal Scroll View**: 横向滚动视图，用于展示用户头像列表
- **Selection State**: 用户选中状态，通过视觉反馈（如对勾、边框）表示

## Requirements

### Requirement 1

**User Story:** 作为用户，我希望分享界面以底部弹窗形式展示，而不是全屏页面，这样可以保持上下文连续性，提升操作流畅度。

#### Acceptance Criteria

1. WHEN 用户触发分享操作 THEN 系统 SHALL 从屏幕底部弹出半屏弹窗，而非跳转到全屏页面
2. WHEN 弹窗展示时 THEN 系统 SHALL 显示白色圆角背景，顶部包含关闭按钮和"分享给"标题
3. WHEN 用户点击关闭按钮或背景遮罩 THEN 系统 SHALL 关闭弹窗并返回原页面
4. WHEN 弹窗打开或关闭时 THEN 系统 SHALL 播放平滑的滑入/滑出动画
5. WHEN 弹窗展示时 THEN 系统 SHALL 在弹窗后方显示半透明遮罩层

### Requirement 2

**User Story:** 作为用户，我希望看到一个统一的可分享用户列表，而不是分成"关注"和"粉丝"两个区域，这样可以更快速地找到目标用户。

#### Acceptance Criteria

1. WHEN 系统加载用户列表 THEN 系统 SHALL 合并 FollowingList 和 FansList 为单一的 ShareUserList
2. WHEN 合并用户列表时 THEN 系统 SHALL 根据 userId 进行去重处理
3. IF 某用户既是关注者又是粉丝 THEN 系统 SHALL 确保该用户在 ShareUserList 中仅出现一次
4. WHEN 用户列表为空 THEN 系统 SHALL 显示友好的空状态提示
5. WHEN 用户搜索时 THEN 系统 SHALL 在合并后的 ShareUserList 中进行过滤

### Requirement 3

**User Story:** 作为用户，我希望用户列表以横向滚动的头像形式展示，这样可以一次看到更多用户，并且交互更加直观。

#### Acceptance Criteria

1. WHEN 用户列表展示时 THEN 系统 SHALL 使用横向滚动视图（scroll-view scroll-x）
2. WHEN 展示单个用户项时 THEN 系统 SHALL 在上方显示圆形头像，下方显示用户昵称
3. WHEN 用户昵称过长时 THEN 系统 SHALL 单行显示并使用省略号截断
4. WHEN 用户在线时 THEN 系统 SHALL 在头像右下角显示绿色在线状态指示点
5. WHEN 用户头像加载失败时 THEN 系统 SHALL 显示默认占位头像

### Requirement 4

**User Story:** 作为用户，我希望选中用户时有清晰的视觉反馈，这样我可以明确知道哪些用户已被选中。

#### Acceptance Criteria

1. WHEN 用户点击头像 THEN 系统 SHALL 切换该用户的选中状态
2. WHEN 用户被选中时 THEN 系统 SHALL 在头像上显示对勾图标或改变边框颜色
3. WHEN 用户取消选中时 THEN 系统 SHALL 移除选中状态的视觉标识
4. WHEN 选中状态改变时 THEN 系统 SHALL 播放平滑的过渡动画
5. WHEN 底部操作栏展示时 THEN 系统 SHALL 实时显示已选中用户数量

### Requirement 5

**User Story:** 作为用户，我希望保留笔记预览和搜索功能，这样我可以确认分享内容并快速找到目标用户。

#### Acceptance Criteria

1. WHEN 弹窗展示时 THEN 系统 SHALL 在顶部显示笔记预览卡片（封面图和标题）
2. WHEN 用户输入搜索关键词 THEN 系统 SHALL 实时过滤用户列表
3. WHEN 搜索结果为空 THEN 系统 SHALL 显示"未找到匹配用户"提示
4. WHEN 用户清空搜索框 THEN 系统 SHALL 恢复显示完整用户列表
5. WHEN 笔记数据加载失败时 THEN 系统 SHALL 显示默认占位信息

### Requirement 6

**User Story:** 作为用户，我希望分享操作简单快捷，并能获得明确的成功或失败反馈。

#### Acceptance Criteria

1. WHEN 用户未选择任何用户时 THEN 系统 SHALL 禁用分享按钮并显示灰色状态
2. WHEN 用户点击分享按钮 THEN 系统 SHALL 显示加载状态并禁止重复点击
3. WHEN 分享成功时 THEN 系统 SHALL 显示成功提示并自动关闭弹窗
4. WHEN 分享失败时 THEN 系统 SHALL 显示错误信息并保持弹窗打开
5. WHEN 分享过程中 THEN 系统 SHALL 在底部操作栏显示"分享中..."状态
