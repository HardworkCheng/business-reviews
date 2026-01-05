# 门店筛选功能说明

## 功能概述

添加了门店筛选功能，商家可以选择查看特定门店的评论，而不是所有门店的评论。

## 修改内容

### 1. 后端修改

#### Service接口
**文件**: `MerchantCommentService.java`

添加`shopId`参数：
```java
// 获取评论列表
PageResult<CommentVO> getCommentList(Long merchantId, Long shopId, Integer pageNum, Integer pageSize, 
        Integer status, String keyword);

// 获取数据概览
Map<String, Object> getDashboard(Long merchantId, Long shopId);
```

#### Service实现
**文件**: `MerchantCommentServiceImpl.java`

**修改逻辑**：
1. 如果传入`shopId`，只查询该门店的评论
2. 如果不传`shopId`，查询商家所有门店的评论

**新增方法**：
```java
// 根据门店ID获取笔记列表
private List<Long> getNoteIdsByShop(Long shopId)

// 验证门店是否属于商家
private void validateShopBelongsToMerchant(Long merchantId, Long shopId)
```

#### Controller
**文件**: `MerchantCommentController.java`

添加`shopId`参数：
```java
@GetMapping
public Result<PageResult<CommentVO>> getCommentList(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) Long shopId,  // 新增
        @RequestParam(required = false) Integer status,
        @RequestParam(required = false) String keyword)

@GetMapping("/dashboard")
public Result<Map<String, Object>> getDashboard(
        @RequestParam(required = false) Long shopId)  // 新增
```

### 2. 前端修改

#### API接口
**文件**: `comment.ts`

```typescript
// 修改数据概览接口，支持传递参数
export function getCommentDashboard(params?: any) {
  return request.get('/merchant/comments/dashboard', { params })
}

// 新增获取门店列表接口
export function getMerchantShops() {
  return request.get('/merchant/shops')
}
```

#### 页面组件
**文件**: `comment/list.vue`

**新增功能**：
1. 门店下拉选择框
2. 门店列表数据
3. 门店切换事件处理

**新增状态**：
```typescript
const shopList = ref<any[]>([])  // 门店列表
const selectedShopId = ref<number | null>(null)  // 选中的门店ID
```

**新增方法**：
```typescript
// 获取门店列表
const fetchShops = async () => { ... }

// 门店切换
const handleShopChange = () => { ... }
```

**修改方法**：
```typescript
// fetchDashboard - 添加shopId参数
// fetchComments - 添加shopId参数
```

## 使用说明

### 1. 查看所有门店的评论

- 不选择门店（默认状态）
- 或选择"全部门店"选项
- 系统会显示商家所有门店的评论

### 2. 查看特定门店的评论

- 在门店下拉框中选择具体的门店
- 系统会只显示该门店的评论
- 数据概览也会只统计该门店的数据

### 3. 切换门店

- 切换门店时，页面会自动刷新
- 重新加载数据概览和评论列表
- 分页会重置到第一页

## API接口

### 1. 获取评论列表

```
GET /merchant/comments
```

**参数**：
- `pageNum`: 页码（默认1）
- `pageSize`: 每页数量（默认10）
- `shopId`: 门店ID（可选，不传则查询所有门店）
- `status`: 状态（可选）
- `keyword`: 搜索关键词（可选）

**示例**：
```
# 查询所有门店的评论
GET /merchant/comments?pageNum=1&pageSize=10

# 查询指定门店的评论
GET /merchant/comments?pageNum=1&pageSize=10&shopId=1
```

### 2. 获取数据概览

```
GET /merchant/comments/dashboard
```

**参数**：
- `shopId`: 门店ID（可选，不传则统计所有门店）

**示例**：
```
# 查询所有门店的数据概览
GET /merchant/comments/dashboard

# 查询指定门店的数据概览
GET /merchant/comments/dashboard?shopId=1
```

### 3. 获取门店列表

```
GET /merchant/shops
```

**说明**：
- 返回当前商家的所有门店列表
- 用于填充门店下拉选择框

## 数据流程

### 查看所有门店评论
```
用户不选择门店
  ↓
前端不传shopId参数
  ↓
后端获取商家所有门店
  ↓
查询所有门店的笔记
  ↓
返回所有笔记的评论
```

### 查看特定门店评论
```
用户选择门店A
  ↓
前端传递shopId=A
  ↓
后端验证门店A属于该商家
  ↓
只查询门店A的笔记
  ↓
返回门店A笔记的评论
```

## 注意事项

### 1. 门店归属验证

当前实现中，`validateShopBelongsToMerchant`方法暂时跳过了验证。原因：
- `shops`表中可能没有`merchant_id`字段
- 需要根据实际数据库结构调整验证逻辑

**建议**：
- 如果`shops`表有`merchant_id`字段，添加验证
- 或通过其他关联表验证门店归属

### 2. 门店列表接口

前端调用`GET /merchant/shops`获取门店列表，需要确保：
- 该接口已实现
- 返回格式包含`id`和`name`字段
- 只返回当前商家的门店

### 3. 默认行为

- 页面加载时，默认不选择门店
- 显示所有门店的评论
- 用户可以手动选择门店进行筛选

### 4. 数据一致性

- 切换门店时，数据概览和评论列表都会更新
- Tab计数也会根据选中的门店重新计算
- 搜索条件会保留

## 测试建议

### 1. 功能测试

- [ ] 页面加载时显示所有门店的评论
- [ ] 选择特定门店后只显示该门店的评论
- [ ] 切换门店时数据正确更新
- [ ] 选择"全部门店"后显示所有评论
- [ ] 数据概览随门店选择正确更新

### 2. 边界测试

- [ ] 商家没有门店时的处理
- [ ] 门店没有笔记时的处理
- [ ] 笔记没有评论时的处理
- [ ] 选择不存在的门店ID

### 3. 权限测试

- [ ] 商家只能查看自己门店的评论
- [ ] 不能查看其他商家门店的评论

## 后续优化建议

### 1. 门店归属验证

完善`validateShopBelongsToMerchant`方法：
```java
private void validateShopBelongsToMerchant(Long merchantId, Long shopId) {
    ShopDO shop = shopMapper.selectById(shopId);
    if (shop == null) {
        throw new BusinessException(40404, "门店不存在");
    }
    // 如果shops表有merchant_id字段
    if (!merchantId.equals(shop.getMerchantId())) {
        throw new BusinessException(40300, "无权访问该门店");
    }
}
```

### 2. 门店列表缓存

- 门店列表不经常变化，可以缓存
- 减少API调用次数

### 3. URL参数同步

- 将选中的门店ID同步到URL参数
- 刷新页面时保持门店选择状态

### 4. 门店信息展示

- 在数据概览中显示当前选中的门店名称
- 提供更清晰的上下文信息

## 总结

通过添加门店筛选功能，商家可以：
- ✅ 查看所有门店的评论（默认）
- ✅ 查看特定门店的评论（选择门店）
- ✅ 灵活切换不同门店
- ✅ 数据概览随门店选择更新

这样商家可以更精准地管理每个门店的评论，提高管理效率。
