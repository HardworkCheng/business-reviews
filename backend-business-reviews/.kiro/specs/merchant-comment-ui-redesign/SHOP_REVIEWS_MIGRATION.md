# 商家评论管理 - 从笔记评论迁移到商家评论

## 业务需求变更

**原需求（错误）**: 展示笔记评论（note_comments表）
**新需求（正确）**: 展示商家评论（shop_reviews表）

用户在UniApp中访问商家详情页时，可以对商家进行评价，这些评价存储在`shop_reviews`表中。商家运营中心的评论管理应该展示和管理这些商家评论，而不是笔记评论。

---

## 数据表对比

### shop_reviews 表（商家评论）
```sql
CREATE TABLE `shop_reviews` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `shop_id` bigint NOT NULL COMMENT '商家ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `rating` decimal(3, 2) NOT NULL COMMENT '综合评分（1-5）',
  `taste_score` decimal(3, 2) NULL COMMENT '口味评分',
  `environment_score` decimal(3, 2) NULL COMMENT '环境评分',
  `service_score` decimal(3, 2) NULL COMMENT '服务评分',
  `content` text NULL COMMENT '评价内容',
  `images` text NULL COMMENT '评价图片（JSON）',
  `like_count` int NOT NULL DEFAULT 0 COMMENT '点赞数',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1正常，2隐藏）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_shop_id` (`shop_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_created_at` (`created_at`)
)
```

### note_comments 表（笔记评论 - 不再使用）
```sql
CREATE TABLE `note_comments` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `note_id` bigint NOT NULL COMMENT '笔记ID',
  `user_id` bigint NOT NULL COMMENT '评论用户ID',
  `parent_id` bigint NULL COMMENT '父评论ID',
  `content` text NOT NULL COMMENT '评论内容',
  `like_count` int NOT NULL DEFAULT 0 COMMENT '点赞数',
  `reply_count` int NOT NULL DEFAULT 0 COMMENT '回复数量',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1正常，2隐藏）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_note_id` (`note_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_parent_id` (`parent_id`),
  INDEX `idx_created_at` (`created_at`)
)
```

---

## 字段映射关系

| shop_reviews | note_comments | 说明 |
|-------------|---------------|------|
| id | id | 评论ID |
| shop_id | note_id | 关联对象ID（商家 vs 笔记） |
| user_id | user_id | 用户ID |
| rating | - | 综合评分（shop_reviews独有） |
| taste_score | - | 口味评分（shop_reviews独有） |
| environment_score | - | 环境评分（shop_reviews独有） |
| service_score | - | 服务评分（shop_reviews独有） |
| content | content | 评论内容 |
| images | - | 评价图片（shop_reviews独有） |
| like_count | like_count | 点赞数 |
| status | status | 状态 |
| created_at | created_at | 创建时间 |
| updated_at | updated_at | 更新时间 |
| - | parent_id | 父评论ID（note_comments独有，用于回复） |
| - | reply_count | 回复数量（note_comments独有） |

---

## 核心差异

### 1. 数据关系
- **shop_reviews**: 用户 → 商家（一对多）
- **note_comments**: 用户 → 笔记 → 商家（多对多，需要通过笔记关联）

### 2. 评分系统
- **shop_reviews**: 有综合评分和三个子评分（口味、环境、服务）
- **note_comments**: 没有评分系统

### 3. 回复机制
- **shop_reviews**: 没有parent_id，不支持嵌套回复
- **note_comments**: 有parent_id，支持嵌套回复

### 4. 图片存储
- **shop_reviews**: 直接存储在images字段（JSON格式）
- **note_comments**: 没有图片字段

---

## 需要修改的文件

### 后端

#### 1. Service接口
**文件**: `MerchantCommentService.java`
- 方法签名保持不变
- 内部逻辑从note_comments改为shop_reviews

#### 2. Service实现
**文件**: `MerchantCommentServiceImpl.java`
- 注入`ShopReviewMapper`替代`NoteCommentMapper`
- 移除`NoteMapper`依赖（不再需要通过笔记关联）
- 修改所有查询逻辑，直接查询shop_reviews表
- 修改`convertToCommentVO`方法，适配shop_reviews字段

#### 3. Controller
**文件**: `MerchantCommentController.java`
- 无需修改（接口保持不变）

#### 4. VO对象
**文件**: `CommentVO.java`
- 可能需要添加评分相关字段

### 前端

#### 1. 页面组件
**文件**: `comment/list.vue`
- 可能需要调整评分显示逻辑
- 其他UI保持不变

---

## 修改步骤

### Step 1: 修改Service实现类

**修改内容**:
1. 注入ShopReviewMapper
2. 移除NoteMapper和NoteCommentMapper
3. 修改getCommentList方法
   - 直接查询shop_reviews表
   - 根据shopId或merchantId筛选
4. 修改getDashboard方法
   - 统计shop_reviews数据
5. 修改replyComment方法
   - shop_reviews不支持回复，需要新的实现方式
6. 修改deleteComment方法
   - 更新shop_reviews的status
7. 修改convertToCommentVO方法
   - 适配shop_reviews字段

### Step 2: 处理回复功能

**问题**: shop_reviews表没有parent_id字段，不支持嵌套回复

**解决方案**:
1. **方案A**: 创建新表shop_review_replies存储商家回复
2. **方案B**: 在shop_reviews表添加reply字段存储商家回复
3. **方案C**: 暂时禁用回复功能

**推荐**: 方案B - 添加reply字段

```sql
ALTER TABLE shop_reviews 
ADD COLUMN `reply` text NULL COMMENT '商家回复内容' AFTER `content`,
ADD COLUMN `reply_time` datetime NULL COMMENT '回复时间' AFTER `reply`;
```

### Step 3: 更新CommentVO

添加评分相关字段：
```java
private BigDecimal rating;  // 综合评分
private BigDecimal tasteScore;  // 口味评分
private BigDecimal environmentScore;  // 环境评分
private BigDecimal serviceScore;  // 服务评分
```

### Step 4: 更新前端显示

- 显示综合评分和子评分
- 显示评价图片
- 调整回复逻辑

---

## 数据流程（修改后）

### 查看所有门店评论
```
用户不选择门店
  ↓
前端不传shopId参数
  ↓
后端获取商家所有门店ID
  ↓
查询这些门店的shop_reviews
  ↓
返回所有商家评论
```

### 查看特定门店评论
```
用户选择门店A
  ↓
前端传递shopId=A
  ↓
后端验证门店A属于该商家
  ↓
只查询门店A的shop_reviews
  ↓
返回门店A的商家评论
```

---

## 实施计划

### Phase 1: 数据库准备
- [ ] 检查shop_reviews表是否存在
- [ ] 添加reply和reply_time字段（如果需要）
- [ ] 确认测试数据

### Phase 2: 后端修改
- [ ] 修改MerchantCommentServiceImpl
- [ ] 更新CommentVO
- [ ] 测试所有API接口

### Phase 3: 前端调整
- [ ] 更新评分显示
- [ ] 测试所有功能

### Phase 4: 测试验证
- [ ] 功能测试
- [ ] 边界测试
- [ ] 性能测试

---

## 注意事项

### 1. 回复功能
shop_reviews表原本不支持回复，需要添加字段或创建新表。

### 2. 评分显示
shop_reviews有多个评分字段，前端需要适配显示。

### 3. 数据迁移
如果已有note_comments数据，不需要迁移（两个是不同的业务）。

### 4. 门店归属验证
shops表有merchant_id字段，可以直接验证门店归属。

---

## 预期效果

修改完成后，商家运营中心的评论管理将：
- ✅ 展示用户对商家的评价（shop_reviews）
- ✅ 支持按门店筛选
- ✅ 显示综合评分和子评分
- ✅ 显示评价图片
- ✅ 支持商家回复（需要添加字段）
- ✅ 支持删除/隐藏评价
- ✅ 数据统计和概览

---

**创建时间**: 2025-12-25
**状态**: 待实施
