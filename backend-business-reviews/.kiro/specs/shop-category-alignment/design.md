# Design Document

## Overview

本设计旨在统一商家运营中心与UniApp移动端的经营类目数据，通过以下方式实现：
1. 统一数据库categories表中的类目名称，使其与UniApp首页展示一致
2. 修改商家运营中心前端，从后端API动态获取类目列表，替代硬编码
3. 确保前后端使用相同的类目ID进行数据关联

## Architecture

### 系统架构
```
┌─────────────────┐
│  UniApp 首页    │
│  (8个类目图标)  │
└────────┬────────┘
         │
         │ 读取类目
         ▼
┌─────────────────────────────┐
│   Categories 表 (数据库)    │
│  - id (1-8)                 │
│  - name (统一名称)          │
│  - icon                     │
│  - sort_order               │
└────────┬────────────────────┘
         │
         │ 读取类目
         ▼
┌─────────────────┐
│ 商家运营中心    │
│ (类目下拉框)    │
└─────────────────┘
```

### 数据流
1. 商家运营中心页面加载 → 调用后端API获取类目列表
2. 后端从categories表查询status=1的类目，按sort_order排序
3. 前端接收类目数据，填充下拉框选项
4. 商家选择类目并保存 → 提交categoryId到后端
5. 后端验证categoryId有效性并更新shops表

## Components and Interfaces

### 1. 数据库层

#### Categories表结构（已存在）
```sql
CREATE TABLE `categories` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `icon` varchar(100),
  `color` varchar(20),
  `sort_order` int NOT NULL DEFAULT 0,
  `status` tinyint NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`)
);
```

#### 数据修正SQL
需要更新categories表中的name字段，使其与UniApp首页一致：
```sql
UPDATE categories SET name = '美发' WHERE id = 3;
UPDATE categories SET name = '美甲' WHERE id = 4;
UPDATE categories SET name = '足疗' WHERE id = 5;
UPDATE categories SET name = '美容' WHERE id = 6;
UPDATE categories SET name = '游乐' WHERE id = 7;
```

### 2. 后端API层

#### 新增接口：获取类目列表
```java
// Controller
@GetMapping("/api/common/categories")
public Result<List<CategoryVO>> getCategories() {
    List<CategoryVO> categories = commonService.getCategories();
    return Result.success(categories);
}

// Service
public List<CategoryVO> getCategories() {
    LambdaQueryWrapper<CategoryDO> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(CategoryDO::getStatus, 1)
           .orderByAsc(CategoryDO::getSortOrder);
    List<CategoryDO> list = categoryMapper.selectList(wrapper);
    return list.stream()
               .map(this::convertToVO)
               .collect(Collectors.toList());
}
```

#### CategoryVO数据结构
```java
public class CategoryVO {
    private Integer id;
    private String name;
    private String icon;
    private String color;
    private Integer sortOrder;
}
```

### 3. 前端层

#### 商家运营中心 (Vue3 + TypeScript)

##### API调用
```typescript
// src/api/shop.ts
export const getCategories = () => {
  return request.get<CategoryVO[]>('/api/common/categories')
}

interface CategoryVO {
  id: number
  name: string
  icon?: string
  color?: string
  sortOrder?: number
}
```

##### 组件修改
```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getCategories } from '@/api/shop'

// 从硬编码改为动态加载
const categories = ref<CategoryVO[]>([])

const loadCategories = async () => {
  try {
    categories.value = await getCategories()
  } catch (error) {
    console.error('加载类目失败:', error)
    // 降级方案：使用默认类目
    categories.value = [
      { id: 1, name: '美食' },
      { id: 2, name: 'KTV' },
      { id: 3, name: '美发' },
      { id: 4, name: '美甲' },
      { id: 5, name: '足疗' },
      { id: 6, name: '美容' },
      { id: 7, name: '游乐' },
      { id: 8, name: '酒吧' }
    ]
  }
}

onMounted(() => {
  loadCategories()
})
</script>

<template>
  <el-select v-model="shopForm.categoryId" placeholder="请选择经营类目">
    <el-option 
      v-for="cat in categories" 
      :key="cat.id" 
      :label="cat.name" 
      :value="cat.id" 
    />
  </el-select>
</template>
```

## Data Models

### CategoryDO (已存在)
```java
@TableName("categories")
public class CategoryDO {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String icon;
    private String color;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### CategoryVO
```java
public class CategoryVO {
    private Integer id;
    private String name;
    private String icon;
    private String color;
    private Integer sortOrder;
}
```

### ShopDO (已存在，无需修改)
```java
@TableName("shops")
public class ShopDO {
    private Long id;
    private Integer categoryId;  // 外键关联categories.id
    // ... 其他字段
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: 类目列表完整性
*For any* 类目列表查询请求，返回的类目数量应该等于数据库中status=1的类目数量，且每个类目都包含id和name字段
**Validates: Requirements 2.1, 2.3**

### Property 2: 类目排序一致性
*For any* 类目列表查询请求，返回的类目顺序应该严格按照sort_order字段升序排列
**Validates: Requirements 3.1**

### Property 3: 类目ID有效性验证
*For any* 店铺保存请求，如果包含categoryId字段，该ID必须在categories表中存在且status=1
**Validates: Requirements 2.4**

### Property 4: 类目名称一致性
*For any* 类目ID，在商家运营中心显示的类目名称应该与UniApp首页对应ID的类目名称完全一致
**Validates: Requirements 3.2**

### Property 5: 数据同步实时性
*For any* 类目数据更新操作，前端在下次请求时应该获取到最新的类目列表，无需重启应用
**Validates: Requirements 2.2**

## Error Handling

### 1. 类目列表加载失败
- **场景**: 后端API不可用或网络错误
- **处理**: 前端使用降级方案，显示默认的8个类目
- **用户提示**: "类目加载失败，使用默认类目"

### 2. 无效的类目ID
- **场景**: 提交的categoryId在数据库中不存在
- **处理**: 后端返回400错误，拒绝保存
- **用户提示**: "选择的类目无效，请重新选择"

### 3. 类目数据不一致
- **场景**: 数据库中的类目名称与预期不符
- **处理**: 执行数据修正SQL脚本
- **验证**: 运行测试确保修正后的数据正确

### 4. 类目数量异常
- **场景**: 数据库中status=1的类目不是8个
- **处理**: 记录警告日志，前端显示实际可用的类目
- **监控**: 添加数据一致性检查

## Testing Strategy

### Unit Tests
- 测试CommonService.getCategories()方法返回正确的类目列表
- 测试类目按sort_order正确排序
- 测试只返回status=1的类目
- 测试CategoryVO转换逻辑正确

### Integration Tests
- 测试/api/common/categories接口返回200状态码
- 测试返回的JSON格式正确
- 测试前端能够正确解析API响应
- 测试保存店铺时categoryId验证逻辑

### Property-Based Tests
使用JUnit 5 + jqwik进行属性测试：

```java
@Property
void categoryListShouldBeComplete(@ForAll @IntRange(min = 1, max = 20) int totalCategories) {
    // 准备：创建指定数量的类目
    // 执行：查询类目列表
    // 验证：返回的类目数量等于status=1的类目数量
}

@Property
void categoryListShouldBeSorted(@ForAll List<CategoryDO> categories) {
    // 准备：随机顺序的类目列表
    // 执行：查询类目列表
    // 验证：返回的列表按sort_order升序排列
}

@Property
void invalidCategoryIdShouldBeRejected(@ForAll @IntRange(min = 100, max = 1000) int invalidId) {
    // 准备：一个不存在的类目ID
    // 执行：尝试保存店铺
    // 验证：应该抛出异常或返回错误
}
```

### Manual Tests
- 在商家运营中心修改店铺类目，验证保存成功
- 在UniApp按类目筛选，验证能找到对应店铺
- 修改数据库类目名称，验证前端自动更新
- 测试网络断开时的降级方案

### End-to-End Tests
1. 商家登录运营中心
2. 进入店铺信息管理页面
3. 修改经营类目为"美食"
4. 保存并刷新页面
5. 验证类目显示为"美食"
6. 在UniApp搜索"美食"类目
7. 验证该店铺出现在搜索结果中

## Implementation Notes

### 数据迁移步骤
1. 备份categories表数据
2. 执行UPDATE语句修正类目名称
3. 验证修正后的数据
4. 重启后端服务（如有缓存）

### 兼容性考虑
- 保持categoryId字段不变，只修改name字段
- 已有店铺的categoryId关联不受影响
- API向后兼容，不影响UniApp现有功能

### 性能优化
- 类目列表数据量小（8条），无需分页
- 考虑在后端添加缓存（Redis），减少数据库查询
- 前端可以缓存类目列表，避免重复请求

### 监控和日志
- 记录类目列表查询次数
- 记录无效categoryId的提交尝试
- 监控类目数据一致性
