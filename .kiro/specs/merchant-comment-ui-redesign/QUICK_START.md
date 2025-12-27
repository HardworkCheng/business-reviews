# 快速启动指南

## 前端启动

### 1. 确保依赖已安装
```bash
cd front-business-reviews-Web
npm install
```

### 2. 启动开发服务器
```bash
npm run dev
```

### 3. 访问评论管理页面
打开浏览器访问：`http://localhost:5173/comment/list`

## 后端需要实现的接口

### 优先级1：必须实现（页面才能正常工作）

#### 1. 获取数据概览
```java
@GetMapping("/merchant/comments/dashboard")
public Result<CommentDashboard> getDashboard() {
    // 返回数据概览
    return Result.success(commentService.getDashboard());
}
```

返回数据结构：
```json
{
  "todayNewComments": 128,
  "todayTrend": 12,
  "averageRating": 4.8,
  "ratingTrend": 0,
  "pendingReply": 9,
  "replyTrend": -5,
  "negativeComments": 2
}
```

#### 2. 扩展评论列表接口
需要在现有的 `/merchant/comments` 接口中添加 `tabCounts` 字段：

```json
{
  "list": [...],
  "total": 128,
  "tabCounts": {
    "all": 128,
    "published": 120,
    "negative": 8,
    "deleted": 0
  }
}
```

### 优先级2：增强功能

#### 3. 置顶评论
```java
@PutMapping("/merchant/comments/{id}/top")
public Result<Void> topComment(@PathVariable Long id, @RequestBody TopRequest request) {
    commentService.topComment(id, request.getIsTop());
    return Result.success();
}
```

#### 4. 导出评论数据
```java
@GetMapping("/merchant/comments/export")
public void exportComments(
    @RequestParam(required = false) String status,
    @RequestParam(required = false) String keyword,
    HttpServletResponse response
) {
    // 生成Excel文件并返回
    commentService.exportComments(status, keyword, response);
}
```

## 数据库修改

### 添加置顶字段（如果还没有）
```sql
ALTER TABLE comments ADD COLUMN is_top TINYINT(1) DEFAULT 0 COMMENT '是否置顶';
ALTER TABLE comments ADD INDEX idx_is_top (is_top);
```

## 测试数据

### 创建测试评论
可以使用以下SQL创建一些测试数据：

```sql
-- 插入测试评论
INSERT INTO comments (user_id, user_name, user_avatar, content, rating, status, create_time) VALUES
(1, '测试用户1', 'https://api.dicebear.com/7.x/avataaars/svg?seed=user1', '这是一条测试评论', 5, 'published', NOW()),
(2, '测试用户2', 'https://api.dicebear.com/7.x/avataaars/svg?seed=user2', '服务很好，推荐！', 4, 'published', NOW()),
(3, '测试用户3', 'https://api.dicebear.com/7.x/avataaars/svg?seed=user3', '不太满意', 2, 'published', NOW());
```

## 常见问题

### Q1: 页面显示空白
A: 检查后端API是否正常返回数据，打开浏览器控制台查看错误信息。

### Q2: 样式不正确
A: 确保 `comment-management.scss` 文件已正确引入，检查SCSS编译是否正常。

### Q3: 图标不显示
A: 确保Element Plus图标库已安装：
```bash
npm install @element-plus/icons-vue
```

### Q4: API请求失败
A: 检查 `src/api/request.ts` 中的baseURL配置是否正确。

## 功能演示

### 1. 数据概览
- 页面顶部显示4个数据卡片
- 悬停卡片会有上浮动画
- 趋势指标用颜色区分

### 2. Tab筛选
- 点击不同Tab查看不同状态的评论
- Tab标签显示对应数量

### 3. 搜索
- 输入关键词搜索评论
- 支持搜索内容、用户名、订单号
- 500ms防抖

### 4. 操作
- 点击回复按钮回复评论
- 点击置顶按钮置顶评论
- 点击删除按钮删除评论（需确认）

### 5. 导出
- 点击导出按钮导出当前筛选的评论数据
- 生成Excel文件下载

## 联系方式

如有问题，请联系开发团队。
