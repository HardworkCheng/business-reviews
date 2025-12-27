# 商家运营中心评论管理页面 - 任务列表（已更新）

## ✅ 已完成的任务（UI重设计）

- [x] 1-20. UI重设计相关任务（已全部完成）

---

## ✅ 新增任务：从笔记评论迁移到商家评论

### 任务 21: 业务逻辑修正 - 使用shop_reviews表

**问题**: 原实现错误地使用了note_comments表（笔记评论），应该使用shop_reviews表（商家评论）

**需求**: 商家运营中心的评论管理应该展示用户在UniApp商家详情页对商家的评价，而不是笔记的评论

**完成内容**:

- [x] 21.1 数据库修改
  - 创建SQL脚本添加reply和reply_time字段到shop_reviews表
  - 支持商家回复功能
  - _文件: `backend-business-reviews/sql/add_reply_fields_to_shop_reviews.sql`_

- [x] 21.2 更新ShopReviewDO实体类
  - 添加reply字段（商家回复内容）
  - 添加replyTime字段（回复时间）
  - _文件: `ShopReviewDO.java`_

- [x] 21.3 更新CommentVO
  - 添加tasteScore字段（口味评分）
  - 添加environmentScore字段（环境评分）
  - 添加serviceScore字段（服务评分）
  - _文件: `CommentVO.java`_

- [x] 21.4 重写MerchantCommentServiceImpl
  - 注入ShopReviewMapper替代NoteCommentMapper
  - 移除NoteMapper依赖
  - 修改getCommentList方法 - 直接查询shop_reviews表
  - 修改calculateTabCounts方法 - 统计shop_reviews数据
  - 修改replyComment方法 - 更新reply字段而不是创建新记录
  - 修改deleteComment方法 - 更新shop_reviews的status
  - 修改getCommentStats方法 - 统计shop_reviews数据
  - 修改getDashboard方法 - 计算真实的平均评分
  - 修改validateShopBelongsToMerchant方法 - 使用merchant_id验证
  - 修改getShopIdsByMerchant方法 - 使用merchant_id查询
  - 重写convertToCommentVO方法 - 适配shop_reviews字段
  - 移除getNoteIdsByMerchant和getNoteIdsByShop方法
  - _文件: `MerchantCommentServiceImpl.java`_

- [x] 21.5 验证编译
  - 检查所有修改的文件无编译错误
  - 确认依赖注入正确
  - _状态: ✅ 无编译错误_

- [x] 21.6 创建文档
  - 创建迁移说明文档
  - 创建实施完成文档
  - _文件: `SHOP_REVIEWS_MIGRATION.md`, `SHOP_REVIEWS_IMPLEMENTATION.md`_

---

## 核心变更说明

### 数据表变更
- **修改前**: 使用note_comments表（笔记评论）
- **修改后**: 使用shop_reviews表（商家评论）

### 数据关系变更
- **修改前**: 商家 → 门店 → 笔记 → 评论（多对多）
- **修改后**: 商家 → 门店 → 评论（一对多）

### 功能增强
- ✅ 支持综合评分和子评分（口味、环境、服务）
- ✅ 支持评价图片
- ✅ 支持商家回复（reply字段）
- ✅ 差评/投诉识别（评分<3分）
- ✅ 真实的平均评分计算
- ✅ 门店归属验证

### API接口
- ✅ 接口保持不变
- ✅ 前端无需修改
- ✅ 数据结构兼容

---

## 测试清单

### 数据库测试
- [ ] 执行SQL脚本添加reply和reply_time字段
- [ ] 验证字段添加成功
- [ ] 插入测试数据到shop_reviews表

### 功能测试
- [ ] 查看所有门店的评论
- [ ] 查看特定门店的评论
- [ ] 切换门店筛选
- [ ] 搜索评论
- [ ] 回复评论（验证reply字段更新）
- [ ] 删除评论（验证status更新）
- [ ] 查看数据概览（验证统计数据）
- [ ] 查看评分显示（综合评分和子评分）
- [ ] 查看评价图片
- [ ] 差评/投诉Tab（评分<3分）

### 边界测试
- [ ] 商家没有门店时的处理
- [ ] 门店没有评论时的处理
- [ ] 评论没有图片时的处理
- [ ] 评论没有回复时的处理
- [ ] 选择不存在的门店ID

### 权限测试
- [ ] 商家只能查看自己门店的评论
- [ ] 商家只能回复自己门店的评论
- [ ] 商家只能删除自己门店的评论
- [ ] 不能查看其他商家门店的评论

---

## 下一步操作

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
按照测试清单逐项测试

### 5. 前端调整（可选）
如果需要显示多个评分，可以调整前端UI

---

## 文档清单

### 技术文档
- `.kiro/specs/merchant-comment-ui-redesign/SHOP_REVIEWS_MIGRATION.md` - 迁移说明
- `.kiro/specs/merchant-comment-ui-redesign/SHOP_REVIEWS_IMPLEMENTATION.md` - 实施完成文档
- `.kiro/specs/merchant-comment-ui-redesign/tasks_updated.md` - 更新的任务列表

### SQL脚本
- `backend-business-reviews/sql/add_reply_fields_to_shop_reviews.sql` - 添加回复字段

---

**更新时间**: 2025-12-25
**状态**: ✅ 代码修改完成，待测试
**编译状态**: ✅ 无错误
