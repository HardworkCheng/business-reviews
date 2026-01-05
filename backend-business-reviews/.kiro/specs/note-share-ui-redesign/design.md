# Design Document

## Overview

本设计文档描述了笔记分享页面从全屏长列表布局到 Bottom Sheet 弹窗模式的重构方案。设计采用现代化的交互模式，参考抖音等主流应用的分享界面，通过横向滚动的头像列表、清晰的选中反馈和流畅的动画效果，提升用户体验。

核心设计理念：
- **轻量化交互**：使用 Bottom Sheet 保持上下文连续性
- **简化信息架构**：合并列表减少认知负担
- **视觉优先**：头像为主的横向布局更直观
- **即时反馈**：清晰的选中状态和操作结果提示

## Architecture

### 组件层次结构

```
note-share.vue (Bottom Sheet Container)
├── Overlay Mask (半透明遮罩)
├── Sheet Content (弹窗内容)
│   ├── Sheet Header (顶部栏)
│   │   ├── Close Button
│   │   └── Title ("分享给")
│   ├── Note Preview Card (笔记预览)
│   │   ├── Cover Image
│   │   └── Note Info (标题+内容)
│   ├── Search Bar (搜索框)
│   ├── User Horizontal Scroll (横向用户列表)
│   │   └── User Avatar Card (用户卡片) × N
│   │       ├── Avatar Image
│   │       ├── Online Status Dot
│   │       ├── Selection Checkmark
│   │       └── Username Label
│   └── Footer Action Bar (底部操作栏)
│       ├── Selected Count
│       └── Share Button
```

### 状态管理

```javascript
// 核心状态
const isSheetVisible = ref(false)        // 弹窗显示状态
const shareUserList = ref([])            // 合并去重后的用户列表
const selectedUserIds = ref([])          // 已选中的用户ID数组
const searchKeyword = ref('')            // 搜索关键词
const isSharing = ref(false)             // 分享进行中状态
const noteData = ref({})                 // 笔记数据

// 计算属性
const filteredUserList = computed(() => {
  // 根据搜索关键词过滤用户列表
})

const hasSelection = computed(() => {
  return selectedUserIds.value.length > 0
})
```

## Components and Interfaces

### 1. Bottom Sheet Container

**职责**: 管理弹窗的显示/隐藏、动画效果和遮罩交互

**Props**:
- `visible`: Boolean - 控制弹窗显示
- `maskClosable`: Boolean - 点击遮罩是否关闭（默认 true）

**Events**:
- `@close`: 弹窗关闭事件
- `@open`: 弹窗打开事件

**动画实现**:
```css
/* 使用 CSS Transform 实现滑入/滑出 */
.sheet-enter-active, .sheet-leave-active {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.sheet-enter-from, .sheet-leave-to {
  transform: translateY(100%);
}
```

### 2. User Avatar Card Component

**职责**: 展示单个用户的头像、昵称、在线状态和选中状态

**Props**:
```typescript
interface UserAvatarCardProps {
  user: {
    userId: number
    username: string
    avatar: string
    isOnline?: boolean
  }
  selected: boolean
}
```

**Layout**:
```
┌─────────────┐
│  ┌───────┐  │
│  │ Avatar│  │ <- 圆形头像 (80rpx)
│  │   ✓   │  │ <- 选中对勾（覆盖在头像上）
│  └───────┘  │
│      🟢     │ <- 在线状态点（右下角）
│   Username  │ <- 昵称（单行省略）
└─────────────┘
```

**样式规范**:
- 头像尺寸: 80rpx × 80rpx
- 在线状态点: 16rpx × 16rpx，绿色 (#52c41a)
- 选中对勾: 白色，半透明黑色背景
- 昵称字体: 24rpx，最大宽度 100rpx

### 3. Horizontal Scroll View

**职责**: 横向滚动展示用户列表

**实现**:
```vue
<scroll-view 
  class="user-scroll" 
  scroll-x 
  :show-scrollbar="false"
  :enable-flex="true"
>
  <view class="user-scroll-content">
    <user-avatar-card 
      v-for="user in filteredUserList"
      :key="user.userId"
      :user="user"
      :selected="selectedUserIds.includes(user.userId)"
      @click="toggleSelection(user.userId)"
    />
  </view>
</scroll-view>
```

**样式要点**:
- 使用 `display: flex` 实现横向布局
- 设置 `white-space: nowrap` 防止换行
- 卡片间距: 20rpx
- 左右内边距: 30rpx

## Data Models

### User Model

```typescript
interface User {
  userId: number           // 用户唯一标识
  username: string         // 用户昵称
  avatar: string          // 头像URL
  bio?: string            // 个人简介
  isOnline?: boolean      // 在线状态（可选）
}
```

### Note Data Model

```typescript
interface NoteData {
  noteId: number          // 笔记ID
  title: string           // 笔记标题
  content: string         // 笔记内容
  coverImage: string      // 封面图URL
}
```

### Share Request Model

```typescript
interface ShareRequest {
  noteId: number          // 要分享的笔记ID
  userIds: number[]       // 目标用户ID数组
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: User List Deduplication

*For any* combination of FollowingList and FansList, when merged into ShareUserList, each unique userId should appear exactly once in the final list.

**Validates: Requirements 2.2, 2.3**

**Test Strategy**: 
- Generate random following and fans lists with overlapping users
- Verify merged list contains no duplicate userIds
- Verify all unique users from both lists are present

### Property 2: Selection State Consistency

*For any* user in ShareUserList, the visual selection state (checkmark/border) should always match the presence of that userId in the selectedUserIds array.

**Validates: Requirements 4.1, 4.2, 4.3**

**Test Strategy**:
- Toggle selection for random users
- Verify selectedUserIds array updates correctly
- Verify UI reflects the selection state

### Property 3: Search Filter Correctness

*For any* search keyword, all users in filteredUserList should have either username or bio containing the keyword (case-insensitive).

**Validates: Requirements 5.2, 5.4**

**Test Strategy**:
- Input random search keywords
- Verify all filtered results match the keyword
- Verify no matching users are excluded

### Property 4: Share Button State

*For any* state of selectedUserIds, the share button should be disabled if and only if selectedUserIds is empty.

**Validates: Requirements 6.1**

**Test Strategy**:
- Test with empty selection
- Test with one or more selections
- Verify button disabled state matches selection state

### Property 5: Animation Completion

*For any* Bottom Sheet open/close operation, the animation should complete before user interaction is re-enabled.

**Validates: Requirements 1.4**

**Test Strategy**:
- Trigger open/close operations
- Verify animation duration matches CSS transition
- Verify interaction is blocked during animation

## Error Handling

### 1. Network Errors

**Scenario**: API 请求失败（获取用户列表、分享操作）

**Handling**:
```javascript
try {
  const result = await fetchUserList()
} catch (error) {
  uni.showToast({
    title: '加载失败，请重试',
    icon: 'none'
  })
  // 保持现有数据，允许用户重试
}
```

### 2. Empty State Handling

**Scenario**: 用户没有关注者和粉丝

**Handling**:
- 显示友好的空状态插图和提示文案
- 提供"去关注用户"的引导按钮

### 3. Image Loading Failures

**Scenario**: 用户头像或笔记封面加载失败

**Handling**:
```vue
<image 
  :src="user.avatar" 
  @error="handleImageError"
  mode="aspectFill"
/>

// 使用默认占位图
const handleImageError = (e) => {
  e.target.src = '/static/default-avatar.png'
}
```

### 4. Duplicate Share Prevention

**Scenario**: 用户快速多次点击分享按钮

**Handling**:
```javascript
const handleShare = async () => {
  if (isSharing.value) return  // 防止重复提交
  
  isSharing.value = true
  try {
    await shareNoteToUsers(noteId, selectedUserIds)
  } finally {
    isSharing.value = false
  }
}
```

## Testing Strategy

### Unit Tests

1. **User List Merging Logic**
   - Test merging with no overlap
   - Test merging with complete overlap
   - Test merging with partial overlap
   - Test empty list scenarios

2. **Search Filter Function**
   - Test exact match
   - Test partial match
   - Test case-insensitive matching
   - Test special characters

3. **Selection Toggle Logic**
   - Test adding selection
   - Test removing selection
   - Test toggling same user multiple times

### Property-Based Tests

使用 **jqwik** (Java) 或 **fast-check** (JavaScript) 进行属性测试。

**配置**: 每个属性测试运行最少 100 次迭代。

**标注格式**: 
```javascript
// **Feature: note-share-ui-redesign, Property 1: User List Deduplication**
test('merged user list has no duplicates', () => {
  fc.assert(
    fc.property(
      fc.array(userArbitrary),  // following list
      fc.array(userArbitrary),  // fans list
      (following, fans) => {
        const merged = mergeAndDeduplicateUsers(following, fans)
        const userIds = merged.map(u => u.userId)
        const uniqueIds = [...new Set(userIds)]
        return userIds.length === uniqueIds.length
      }
    ),
    { numRuns: 100 }
  )
})
```

### Integration Tests

1. **Complete Share Flow**
   - Open bottom sheet
   - Search for user
   - Select multiple users
   - Complete share operation
   - Verify success feedback

2. **Animation and Interaction**
   - Verify sheet opens with animation
   - Verify mask click closes sheet
   - Verify close button works
   - Verify scroll behavior

### Visual Regression Tests

使用截图对比验证：
- Bottom sheet 布局正确性
- 横向滚动列表样式
- 选中状态视觉效果
- 空状态展示

## Implementation Notes

### 1. 使用 uni-popup 还是自定义实现？

**推荐**: 自定义实现

**理由**:
- uni-popup 可能不完全符合设计需求
- 自定义实现可以更好地控制动画和交互
- 代码量不大，维护成本可控

### 2. 横向滚动性能优化

```vue
<!-- 使用 scroll-view 的性能优化属性 -->
<scroll-view
  scroll-x
  :enable-flex="true"
  :show-scrollbar="false"
  :scroll-with-animation="true"
  :enhanced="true"
  :bounces="false"
>
```

### 3. 在线状态实现

如果后端不提供在线状态，可以：
- 暂时隐藏在线状态点
- 或使用随机状态作为 UI 演示

### 4. 响应式设计

确保在不同屏幕尺寸下都能正常显示：
- 使用 rpx 单位
- 弹窗最大高度: 80vh
- 最小高度: 400rpx

### 5. 无障碍支持

- 为关闭按钮添加 aria-label
- 确保键盘可以操作（H5 端）
- 提供足够的点击区域（最小 44rpx × 44rpx）
