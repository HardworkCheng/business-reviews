# Design Document - Seckill Auto-scroll Feature

## Overview

本设计文档描述了为移动端优惠券页面的限时秒杀区域实现自动滚动功能的技术方案。该功能将使用Vue 3 Composition API和uni-app的scroll-view组件，通过定时器控制滚动位置，并响应用户交互来暂停和恢复自动滚动。

## Architecture

### Component Structure

```
discount.vue (现有组件)
├── Template
│   └── seckill-zone (秒杀区域)
│       └── scroll-view (滚动视图)
│           └── seckill-card[] (秒杀卡片列表)
└── Script
    ├── Auto-scroll State Management (自动滚动状态管理)
    ├── Timer Management (定时器管理)
    ├── User Interaction Handlers (用户交互处理)
    └── Lifecycle Hooks (生命周期钩子)
```

### Data Flow

1. 组件挂载 → 初始化自动滚动
2. 定时器触发 → 计算下一个滚动位置 → 更新scroll-left
3. 用户触摸 → 暂停自动滚动 → 启动恢复定时器
4. 恢复定时器到期 → 恢复自动滚动
5. 组件卸载 → 清理所有定时器

## Components and Interfaces

### State Variables

```typescript
// 自动滚动相关状态
const scrollLeft = ref(0)              // 当前滚动位置
const isAutoScrolling = ref(true)      // 是否正在自动滚动
const isTouching = ref(false)          // 用户是否正在触摸
const cardWidth = ref(244)             // 单个卡片宽度 (220rpx + 24rpx margin)
const scrollTimer = ref(null)          // 自动滚动定时器
const resumeTimer = ref(null)          // 恢复滚动定时器
```

### Configuration Constants

```typescript
const AUTO_SCROLL_INTERVAL = 3000      // 自动滚动间隔 (毫秒)
const RESUME_DELAY = 3000              // 恢复滚动延迟 (毫秒)
const SCROLL_DURATION = 500            // 滚动动画时长 (毫秒)
```

### Methods

#### startAutoScroll()
启动自动滚动定时器
- 清除现有定时器
- 创建新的定时器，每3秒触发一次
- 调用scrollToNext()移动到下一个卡片

#### stopAutoScroll()
停止自动滚动
- 清除滚动定时器
- 设置isAutoScrolling为false

#### scrollToNext()
滚动到下一个卡片
- 计算下一个滚动位置
- 如果到达末尾，重置到开始位置
- 更新scrollLeft触发滚动

#### handleTouchStart()
处理用户触摸开始事件
- 设置isTouching为true
- 停止自动滚动
- 清除恢复定时器

#### handleTouchEnd()
处理用户触摸结束事件
- 设置isTouching为false
- 启动恢复定时器（3秒后恢复自动滚动）

#### resumeAutoScroll()
恢复自动滚动
- 检查用户是否仍在触摸
- 如果未触摸，启动自动滚动

#### cleanup()
清理资源
- 清除所有定时器
- 重置状态变量

## Data Models

### Scroll State Model

```typescript
interface ScrollState {
  scrollLeft: number        // 当前滚动位置 (px)
  isAutoScrolling: boolean  // 是否自动滚动中
  isTouching: boolean       // 用户是否触摸中
  cardWidth: number         // 卡片宽度 (px)
  totalCards: number        // 卡片总数
  maxScrollLeft: number     // 最大滚动距离
}
```

### Timer References

```typescript
interface TimerRefs {
  scrollTimer: number | null   // 自动滚动定时器ID
  resumeTimer: number | null   // 恢复定时器ID
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Auto-scroll activation on load
*For any* seckill zone component mount, the automatic scrolling should start within 3 seconds of component initialization
**Validates: Requirements 1.1**

### Property 2: Seamless loop behavior
*For any* scroll position at the end of the card list, the next scroll action should reset the position to the beginning (position 0)
**Validates: Requirements 1.2**

### Property 3: Touch pause behavior
*For any* touch event on the scroll area, the automatic scrolling should immediately stop and remain stopped while the touch is active
**Validates: Requirements 1.3**

### Property 4: Resume after inactivity
*For any* touch end or interaction end event, if no new interaction occurs within 3 seconds, the automatic scrolling should resume from the current position
**Validates: Requirements 1.4, 3.3**

### Property 5: Cleanup on unmount
*For any* component unmount event, all active timers (scrollTimer and resumeTimer) and event listeners should be cleared
**Validates: Requirements 1.5, 4.2**

### Property 6: Scroll speed consistency
*For any* automatic scroll action, the time between consecutive scroll positions should be approximately 3000ms ± 100ms
**Validates: Requirements 2.1**

### Property 7: Animation duration
*For any* scroll transition, the animation duration should be 500ms
**Validates: Requirements 2.2**

### Property 8: Manual scroll interruption
*For any* manual scroll event initiated by the user, the automatic scrolling should stop immediately
**Validates: Requirements 3.1**

### Property 9: Card interaction pause
*For any* tap event on a seckill card, the automatic scrolling should pause
**Validates: Requirements 3.2**

## Error Handling

### Scroll Position Errors
- 如果计算的滚动位置超出范围，重置为0
- 如果卡片宽度未正确计算，使用默认值244rpx

### Timer Errors
- 在设置新定时器前，始终清除现有定时器
- 使用try-catch包裹定时器回调，防止错误中断自动滚动

### User Interaction Conflicts
- 使用防抖处理快速连续的触摸事件
- 确保触摸状态和定时器状态同步

## Testing Strategy

### Unit Tests

1. **Timer Management Tests**
   - 测试startAutoScroll正确创建定时器
   - 测试stopAutoScroll正确清除定时器
   - 测试cleanup清除所有定时器

2. **Scroll Position Tests**
   - 测试scrollToNext正确计算下一个位置
   - 测试循环逻辑在到达末尾时重置位置
   - 测试边界条件（空列表、单个卡片）

3. **User Interaction Tests**
   - 测试触摸事件正确暂停滚动
   - 测试恢复定时器在触摸结束后启动
   - 测试多次快速触摸的处理

### Property-Based Tests

使用jqwik (Java) 或 fast-check (JavaScript) 进行属性测试：

1. **Property 1: Auto-scroll timing**
   - 生成随机的卡片数量和宽度
   - 验证滚动间隔始终为3000ms ± 100ms

2. **Property 2: Loop consistency**
   - 生成随机的滚动位置序列
   - 验证到达末尾后总是重置为0

3. **Property 3: Touch interaction**
   - 生成随机的触摸事件序列
   - 验证每次触摸都暂停滚动，每次释放后3秒恢复

4. **Property 4: Timer cleanup**
   - 生成随机的组件生命周期事件
   - 验证卸载后所有定时器都被清除

### Integration Tests

1. 在真实的uni-app环境中测试scroll-view的滚动行为
2. 测试与现有秒杀券数据加载的集成
3. 测试在不同设备尺寸下的表现

## Implementation Notes

### uni-app Specific Considerations

1. **scroll-view组件**
   - 使用scroll-left属性控制滚动位置
   - 使用scroll-with-animation启用平滑滚动
   - 监听@touchstart和@touchend事件

2. **rpx to px转换**
   - 需要将rpx单位转换为px用于scroll-left
   - 使用uni.getSystemInfoSync()获取屏幕宽度进行转换

3. **性能优化**
   - 使用computed计算卡片总宽度
   - 避免在滚动过程中进行复杂计算
   - 使用防抖处理用户交互事件

### Browser Compatibility

- 确保定时器在微信小程序、H5、App环境中正常工作
- 测试不同平台的触摸事件行为差异
