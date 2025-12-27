# 商家评论管理 - shop_reviews表实现完成

## ✅ 修改完成

已成功将商家运营中心的评论管理从**笔记评论（note_comments）**迁移到**商家评论（shop_reviews）**。

---

## 修改内容

### 1. 数据库修改

#### 添加回复字段
**文件**: `backend-business-reviews/sql/add_reply_fields_to_shop_reviews.sql`

```sql
-- 添加reply字段（商家回复内容）
ALTER TABLE shop_reviews 
ADD COLUMN `reply` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '商家回复内容' AFTER `content`;

-- 添加reply_time字段（回复时间）
ALTER TABLE shop_reviews 
ADD COLUMN `reply_time` datetime NULL COMMENT '回复时间' AFTER `reply`;
```

**执行方式**:
```bash
mysql -u root -p business_reviews < backend-business-reviews/sql/add_reply_fields_to_shop_reviews.sql
```

---

### 2. 实体类修改

#### ShopReviewDO.java
**文件**: `backend-business-reviews/backend-business-reviews-entity/src/main/java/com/businessreviews/model/dataobject/ShopReviewDO.java`

**新增字段**:
```java
private String reply;  // 商家回复内容
private LocalDateTime replyTime;  // 回复时间
```

#### CommentVO.java
**文件**: `backend-business-reviews/backend-business-reviews-entity/src/main/java/com/businessreviews/model/vo/CommentVO.java`

**新增字段**:
```java
private Double tasteScore;  // 口味评分
private Double environmentScore;  // 环境评分
private Double serviceScore;  // 服务评分
```

---

### 3. Service实现修改

#### MerchantCommentServiceImpl.java
**文件**: `backend-business-reviews/backend-business-reviews-service/src/main/java/com/businessreviews/service/impl/merchant/MerchantCommentServiceImpl.java`

**核心变更**:

1. **依赖注入**:
   - ✅ 注入`ShopReviewMapper`（替代`NoteCommentMapper`）
   - ❌ 移除`NoteMapper`依赖
   - ❌ 移除`NoteCommentMapper`依赖

2. **getCommentList方法**:
   - 直接查询`shop_reviews`表
   - 根据`shopId`或`merchantId`筛选
   - 不再需要通过笔记关联

3. **calculateTabCounts方法**:
   - 统计`shop_reviews`数据
   - 差评/投诉：评分低于3分的评论

4. **replyComment方法**:
   - 更新`shop_reviews`的`reply`和`reply_time`字段
   - 不再创建新的回复记录

5. **deleteComment方法**:
   - 更新`shop_reviews`的`status`字段
   - 不再更新笔记评论数

6. **getCommentStats方法**:
   - 统计`shop_reviews`数据
   - 待回复：`reply`字段为NULL的评论

7. **getDashboard方法**:
   - 统计`shop_reviews`数据
   - 计算平均评分（从`rating`字段）
   - 差评/投诉：评分低于3分且未回复的评论

8. **validateShopBelongsToMerchant方法**:
   - 使用`shop.getMerchantId()`验证门店归属
   - 不再跳过验证

9. **getShopIdsByMerchant方法**:
   - 使用`shop.getMerchantId()`查询门店
   - 不再注释掉查询条件

10. **convertToCommentVO方法**:
    - 适配`ShopReviewDO`字段
    - 设置评分相关字段
    - 解析图片JSON
    - 设置商家回复

11. **移除方法**:
    - ❌ `getNoteIdsByMerchant`
    - ❌ `getNoteIdsByShop`

---

## 数据流程（修改后）

### 用户评价流程
```
用户在UniApp访问商家详情页
  ↓
用户对商家进行评价
  ↓
评价数据存入shop_reviews表
  ↓
商家运营中心查询显示
```

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

## 功能对比

### 修改前（note_comments）
- ❌ 展示笔记评论
- ❌ 需要通过笔记关联商家
- ❌ 没有评分系统
- ✅ 支持嵌套回复（parent_id）
- ❌ 没有图片字段

### 修改后（shop_reviews）
- ✅ 展示商家评论
- ✅ 直接关联商家
- ✅ 有综合评分和子评分
- ✅ 支持商家回复（reply字段）
- ✅ 有图片字段

---

## API接口（保持不变）

### 1. 获取评论列表
```
GET /merchant/comments
```
**参数**:
- `pageNum`: 页码
- `pageSize`: 每页数量
- `shopId`: 门店ID（可选）
- `status`: 状态（1=正常，2=隐藏）
- `keyword`: 搜索关键词

### 2. 获取数据概览
```
GET /merchant/comments/dashboard
```
**参数**:
- `shopId`: 门店ID（可选）

### 3. 回复评论
```
POST /merchant/comments/{commentId}/reply
```
**Body**:
```json
{
  "content": "回复内容"
}
```

### 4. 删除评论
```
DELETE /merchant/comments/{commentId}
```

### 5. 置顶评论
```
POST /merchant/comments/{commentId}/top
```
**参数**:
- `isTop`: 是否置顶

---

## 前端修改（无需修改）

前端代码无需修改，因为：
- API接口保持不变
- 返回的数据结构保持不变
- 字段映射保持一致

唯一的变化是：
- `noteTitle`字段现在显示的是**门店名称**而不是笔记标题
- 新增了评分字段（`tasteScore`, `environmentScore`, `serviceScore`）

---

## 测试步骤

### 1. 执行数据库脚本
```bash
mysql -u root -p business_reviews < backend-business-reviews/sql/add_reply_fields_to_shop_reviews.sql
```

### 2. 重新编译后端
```bash
cd backend-business-reviews
mvn clean install
```

### 3. 启动后端服务
```bash
mvn spring-boot:run
```

### 4. 测试功能
- [ ] 查看所有门店的评论
- [ ] 查看特定门店的评论
- [ ] 切换门店筛选
- [ ] 搜索评论
- [ ] 回复评论
- [ ] 删除评论
- [ ] 查看数据概览
- [ ] 查看评分显示

---

## 注意事项

### 1. 数据库字段
确保执行了SQL脚本，添加了`reply`和`reply_time`字段。

### 2. 测试数据
如果`shop_reviews`表中没有数据，需要：
- 在UniApp中对商家进行评价
- 或手动插入测试数据

### 3. 图片解析
当前图片解析使用逗号分隔，如果实际存储格式不同，需要调整`convertToCommentVO`方法中的解析逻辑。

### 4. 评分显示
前端可能需要调整UI来显示多个评分（综合评分、口味、环境、服务）。

### 5. 门店归属验证
现在使用`shop.getMerchantId()`进行验证，确保`shops`表有`merchant_id`字段。

---

## 数据表结构

### shop_reviews（修改后）
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
  `reply` text NULL COMMENT '商家回复内容',  -- 新增
  `reply_time` datetime NULL COMMENT '回复时间',  -- 新增
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

---

## 预期效果

修改完成后，商家运营中心的评论管理将：
- ✅ 展示用户对商家的评价（shop_reviews）
- ✅ 支持按门店筛选
- ✅ 显示综合评分和子评分（口味、环境、服务）
- ✅ 显示评价图片
- ✅ 支持商家回复
- ✅ 支持删除/隐藏评价
- ✅ 数据统计和概览
- ✅ 差评/投诉识别（评分<3分）
- ✅ 待回复统计（reply为NULL）

---

## 文件清单

### 修改的文件
1. `backend-business-reviews/backend-business-reviews-entity/src/main/java/com/businessreviews/model/dataobject/ShopReviewDO.java`
2. `backend-business-reviews/backend-business-reviews-entity/src/main/java/com/businessreviews/model/vo/CommentVO.java`
3. `backend-business-reviews/backend-business-reviews-service/src/main/java/com/businessreviews/service/impl/merchant/MerchantCommentServiceImpl.java`

### 新增的文件
1. `backend-business-reviews/sql/add_reply_fields_to_shop_reviews.sql`
2. `.kiro/specs/merchant-comment-ui-redesign/SHOP_REVIEWS_MIGRATION.md`
3. `.kiro/specs/merchant-comment-ui-redesign/SHOP_REVIEWS_IMPLEMENTATION.md`

### 无需修改的文件
- `MerchantCommentService.java` - 接口保持不变
- `MerchantCommentController.java` - Controller保持不变
- `front-business-reviews-Web/src/views/comment/list.vue` - 前端保持不变
- `front-business-reviews-Web/src/api/comment.ts` - API保持不变

---

**创建时间**: 2025-12-25
**状态**: ✅ 已完成
**编译状态**: ✅ 无错误
**下一步**: 执行数据库脚本并测试功能
