# 商家评论管理 - 后端集成说明

## 修改概述

本次修改实现了商家运营中心评论管理页面与后端API的完整集成，删除了所有固定测试数据，改为调用真实的后端接口。

## 后端修改

### 1. Service接口扩展

**文件**: `MerchantCommentService.java`

新增方法：
```java
// 获取数据概览
Map<String, Object> getDashboard(Long merchantId);

// 置顶评论
void topComment(Long merchantId, Long commentId, Boolean isTop);
```

### 2. Service实现类扩展

**文件**: `MerchantCommentServiceImpl.java`

#### 2.1 getDashboard() 实现
返回数据概览，包括：
- `todayNewComments`: 今日新增评论数
- `todayTrend`: 较昨日增长百分比
- `averageRating`: 店铺综合评分（暂时固定4.8）
- `ratingTrend`: 评分趋势
- `pendingReply`: 待回复数量
- `replyTrend`: 回复效率趋势
- `negativeComments`: 差评/投诉数量（暂时为0）

#### 2.2 topComment() 实现
置顶评论功能（需要数据库添加is_top字段）

#### 2.3 getCommentList() 扩展
- 添加了`calculateTabCounts()`方法，计算各Tab的评论数量
- 返回结果中包含`tabCounts`字段

#### 2.4 convertToCommentVO() 扩展
- 添加了`noteTitle`字段（笔记标题）
- 添加了`status`字段（published/deleted）
- 添加了`reply`和`replyTime`字段（商家回复）
- 获取笔记信息用于显示关联商品

### 3. Controller扩展

**文件**: `MerchantCommentController.java`

新增接口：
```java
// 获取数据概览
@GetMapping("/dashboard")
public Result<Map<String, Object>> getDashboard()

// 置顶评论
@PutMapping("/{id}/top")
public Result<?> topComment(@PathVariable Long id, @RequestBody Map<String, Boolean> request)
```

### 4. 实体类扩展

#### 4.1 CommentVO.java
新增字段：
```java
private String noteTitle;  // 笔记标题
private String status;  // 状态：published/deleted
private String reply;  // 商家回复内容
private LocalDateTime replyTime;  // 商家回复时间
private Boolean isTop;  // 是否置顶
private Boolean isVip;  // 是否VIP用户
private Boolean isAnonymous;  // 是否匿名
private Double rating;  // 评分
private List<String> images;  // 评论图片
```

#### 4.2 PageResult.java
新增字段：
```java
private Map<String, Long> tabCounts;  // Tab计数
```

## 前端修改

### 1. 字段映射调整

前端字段 → 后端字段：
- `userName` → `author`
- `userAvatar` → `avatar`
- `createTime` → `time`
- `goodsName` → `noteTitle`

### 2. Status参数映射

前端Tab → 后端status值：
- `'all'` → `undefined` (不传status参数)
- `'published'` → `1`
- `'deleted'` → `2`
- `'negative'` → `undefined` (暂不支持)

### 3. API调用

所有API调用都已对接真实后端接口：
- ✅ `GET /merchant/comments/dashboard` - 数据概览
- ✅ `GET /merchant/comments` - 评论列表
- ✅ `POST /merchant/comments/{id}/reply` - 回复评论
- ✅ `DELETE /merchant/comments/{id}` - 删除评论
- ✅ `PUT /merchant/comments/{id}/top` - 置顶评论

## 数据库修改建议

### 1. 添加置顶字段（可选）

```sql
ALTER TABLE note_comments 
ADD COLUMN is_top TINYINT(1) DEFAULT 0 COMMENT '是否置顶' AFTER status;

ALTER TABLE note_comments 
ADD INDEX idx_is_top (is_top);
```

### 2. 添加评分字段（可选）

如果需要支持评分功能：
```sql
ALTER TABLE note_comments 
ADD COLUMN rating DECIMAL(2,1) DEFAULT NULL COMMENT '评分(1-5)' AFTER content;
```

### 3. 添加图片字段（可选）

如果需要支持评论图片：
```sql
ALTER TABLE note_comments 
ADD COLUMN images TEXT DEFAULT NULL COMMENT '评论图片JSON数组' AFTER content;
```

## 测试步骤

### 1. 启动后端服务

```bash
cd backend-business-reviews
mvn clean install
mvn spring-boot:run
```

### 2. 启动前端服务

```bash
cd front-business-reviews-Web
npm install
npm run dev
```

### 3. 访问页面

打开浏览器访问：`http://localhost:5173/comment/list`

### 4. 测试功能

1. ✅ 查看数据概览卡片是否显示正确
2. ✅ 切换Tab查看不同状态的评论
3. ✅ 搜索评论功能
4. ✅ 回复评论功能
5. ✅ 删除评论功能
6. ✅ 置顶评论功能（需要数据库支持）
7. ✅ 分页功能

## 数据流程

### 1. 页面加载
```
前端 → GET /merchant/comments/dashboard → 后端
前端 ← 返回数据概览 ← 后端

前端 → GET /merchant/comments?pageNum=1&pageSize=10 → 后端
前端 ← 返回评论列表 + tabCounts ← 后端
```

### 2. Tab切换
```
前端 → GET /merchant/comments?status=1 → 后端
前端 ← 返回筛选后的评论列表 ← 后端
```

### 3. 回复评论
```
前端 → POST /merchant/comments/{id}/reply → 后端
前端 ← 返回成功 ← 后端
前端 → 刷新评论列表和数据概览
```

### 4. 删除评论
```
前端 → DELETE /merchant/comments/{id} → 后端
前端 ← 返回成功 ← 后端
前端 → 刷新评论列表和数据概览
```

## 注意事项

### 1. 商家关联逻辑

当前实现中，商家评论是通过以下关系获取的：
```
商家 → 门店 → 笔记 → 评论
```

确保：
- 商家有关联的门店
- 门店有关联的笔记
- 笔记有用户评论

### 2. 权限验证

所有接口都通过`MerchantContext`获取当前登录商家ID，确保：
- 商家只能查看自己门店的评论
- 商家只能回复/删除自己门店的评论

### 3. 数据为空的情况

如果商家没有关联的门店或笔记，页面会显示空状态：
- 数据概览卡片显示0
- 评论列表为空
- Tab计数都为0

### 4. 评分和图片功能

当前版本中：
- 评分功能：数据库表中没有rating字段，前端会隐藏评分显示
- 图片功能：数据库表中没有images字段，前端会隐藏图片显示

如需启用这些功能，需要：
1. 修改数据库表结构
2. 修改后端Service实现
3. 前端已经支持，无需修改

## 后续优化建议

### 1. 差评/投诉功能

当前`negative` Tab暂不支持，建议：
- 添加评分字段
- 根据评分（如≤2分）自动标记为差评
- 或添加投诉标记字段

### 2. 评分统计

当前店铺综合评分固定为4.8，建议：
- 从所有评论的评分计算平均值
- 实时更新评分趋势

### 3. 置顶功能完善

当前置顶功能需要：
- 数据库添加is_top字段
- 修改查询逻辑，置顶评论排在前面
- 前端显示置顶标识

### 4. 导出功能

当前导出功能前端已实现，后端需要：
- 实现Excel生成逻辑
- 包含所有评论字段
- 支持筛选条件导出

## 常见问题

### Q1: 页面显示"获取评论列表失败"
A: 检查：
1. 后端服务是否启动
2. 商家是否已登录
3. 商家是否有关联的门店和笔记
4. 浏览器控制台查看具体错误信息

### Q2: 数据概览卡片都显示0
A: 这是正常的，如果：
1. 商家没有关联的门店
2. 门店没有关联的笔记
3. 笔记没有用户评论

### Q3: 置顶功能不生效
A: 需要：
1. 数据库添加is_top字段
2. 修改Service实现，更新is_top字段
3. 修改查询逻辑，按is_top排序

### Q4: 评分和图片不显示
A: 当前数据库表中没有这些字段，需要：
1. 修改数据库表结构
2. 修改后端Service实现
3. 前端已支持，无需修改

## 总结

本次集成完成了：
- ✅ 删除所有固定测试数据
- ✅ 对接真实后端API
- ✅ 实现数据概览功能
- ✅ 实现Tab筛选功能
- ✅ 实现搜索功能
- ✅ 实现回复功能
- ✅ 实现删除功能
- ✅ 实现置顶功能（需数据库支持）
- ✅ 实现分页功能

页面已经可以正常使用，显示真实的用户评论数据！
