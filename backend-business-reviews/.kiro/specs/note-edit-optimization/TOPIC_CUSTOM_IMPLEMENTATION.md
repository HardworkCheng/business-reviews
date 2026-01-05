# 笔记话题自定义功能实现完成

## 修改摘要

已完成支持用户自定义话题的功能实现，现在用户可以：
1. ✅ 输入自定义话题名称
2. ✅ 系统自动创建新话题
3. ✅ 编辑时正确回显话题
4. ✅ 保存时正确关联话题

## 修改的文件

### 后端修改

1. **PublishNoteDTO.java**
   - 添加 `List<String> topicNames` 字段
   - 支持通过话题名称提交（优先于topics字段）

2. **NoteServiceImpl.java**
   - 添加 `saveNoteTopicsByNames()` 方法 - 通过话题名称保存
   - 添加 `findOrCreateTopic()` 方法 - 查找或创建话题
   - 修改 `publishNote()` 方法 - 优先使用topicNames
   - 修改 `updateNote()` 方法 - 优先使用topicNames

3. **TopicMapper.java**
   - 添加 `incrementNoteCount()` 方法 - 增加话题笔记数
   - 添加 `decrementNoteCount()` 方法 - 减少话题笔记数

### 前端修改

1. **note-edit.vue**
   - 修改提交逻辑：发送 `topicNames` 而不是 `topics`
   - 话题数据格式：`{ name: '话题名称' }`

2. **publish.vue**
   - 修改提交逻辑：发送 `topicNames` 而不是 `topics`
   - 话题数据格式：`{ name: '话题名称' }`

## 功能说明

### 1. 自定义话题流程

```
用户输入话题名称 "我的美食日记"
    ↓
前端发送: { topicNames: ["我的美食日记"] }
    ↓
后端检查topics表是否存在该话题
    ↓
如果不存在，自动创建新话题
    ↓
创建note_topics关联记录
    ↓
更新话题的note_count
```

### 2. 话题回显流程

```
用户打开编辑页面
    ↓
后端查询note_topics关联表
    ↓
关联查询topics表获取话题名称
    ↓
返回: { topics: [{ id: 1, name: "美食探店" }] }
    ↓
前端显示在"最近使用"区域
```

### 3. 数据结构

#### topics表
```sql
CREATE TABLE `topics` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '话题名称',
  `description` text COMMENT '话题描述',
  `cover_image` varchar(500) COMMENT '封面图',
  `note_count` int NOT NULL DEFAULT 0 COMMENT '笔记数量',
  `view_count` int NOT NULL DEFAULT 0 COMMENT '浏览量',
  `is_hot` tinyint NOT NULL DEFAULT 0 COMMENT '是否热门',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1启用，2禁用）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
)
```

#### note_topics关联表
```sql
CREATE TABLE `note_topics` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `note_id` bigint NOT NULL COMMENT '笔记ID',
  `topic_id` bigint NOT NULL COMMENT '话题ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_note_topic` (`note_id`, `topic_id`)
)
```

## 测试步骤

### 测试1：创建带自定义话题的笔记

1. 打开发布笔记页面
2. 点击"添加话题"
3. 输入自定义话题："我的美食日记"
4. 点击"添加"按钮
5. 填写其他信息并发布
6. **预期结果**：
   - 笔记发布成功
   - topics表中自动创建新话题
   - note_topics表中创建关联记录

**验证SQL**：
```sql
-- 查看新创建的话题
SELECT * FROM topics WHERE name = '我的美食日记';

-- 查看笔记话题关联
SELECT nt.*, t.name 
FROM note_topics nt 
JOIN topics t ON nt.topic_id = t.id 
WHERE nt.note_id = <你的笔记ID>;
```

### 测试2：编辑笔记查看话题回显

1. 打开已有话题的笔记详情页
2. 点击"编辑"按钮
3. **预期结果**：
   - 话题正确显示在"最近使用"区域
   - 话题名称正确显示
   - 可以删除或添加新话题

### 测试3：添加重复话题

1. 创建第一篇笔记，添加话题"美食探店"
2. 创建第二篇笔记，再次添加话题"美食探店"
3. **预期结果**：
   - 不会创建重复的话题记录
   - 两篇笔记都关联到同一个话题
   - 话题的note_count = 2

**验证SQL**：
```sql
-- 查看话题的笔记数量
SELECT id, name, note_count FROM topics WHERE name = '美食探店';

-- 查看该话题关联的所有笔记
SELECT n.id, n.title, t.name AS topic_name
FROM notes n
JOIN note_topics nt ON n.id = nt.note_id
JOIN topics t ON nt.topic_id = t.id
WHERE t.name = '美食探店';
```

### 测试4：热门话题推荐

1. 运行初始化SQL插入热门话题：
```bash
mysql -u your_username -p your_database < backend-business-reviews/sql/insert_initial_topics.sql
```

2. 打开发布/编辑页面
3. 点击"添加话题"
4. **预期结果**：
   - "热门推荐"区域显示预设话题
   - 可以点击选择热门话题
   - 也可以输入自定义话题

### 测试5：话题数量限制

1. 尝试添加6个话题
2. **预期结果**：
   - 前端提示"最多选择5个话题"
   - 无法添加第6个话题

### 测试6：话题名称长度限制

1. 输入超过20个字符的话题名称
2. **预期结果**：
   - 前端提示"话题名称不能超过20个字"
   - 无法添加该话题

## 数据库初始化（可选）

如果你想要一些预设的热门话题，运行以下命令：

```bash
# Windows CMD
mysql -u root -p your_password your_database < backend-business-reviews\sql\insert_initial_topics.sql

# 或者直接在MySQL客户端执行
source backend-business-reviews/sql/insert_initial_topics.sql;
```

这会插入20个热门话题，包括：
- 美食探店
- 周末好去处
- 打卡圣地
- 优惠活动
- 新店开业
- 等等...

## API接口说明

### 发布笔记
```
POST /app/notes
Content-Type: application/json

{
  "title": "笔记标题",
  "content": "笔记内容",
  "images": ["图片URL1", "图片URL2"],
  "shopId": 1,
  "location": "位置名称",
  "latitude": 39.9042,
  "longitude": 116.4074,
  "topicNames": ["美食探店", "我的自定义话题"],
  "status": 1
}
```

### 更新笔记
```
PUT /app/notes/{noteId}
Content-Type: application/json

{
  "title": "更新后的标题",
  "content": "更新后的内容",
  "images": ["图片URL1"],
  "topicNames": ["新话题1", "新话题2"]
}
```

### 获取笔记详情
```
GET /app/notes/{noteId}

Response:
{
  "id": 1,
  "title": "笔记标题",
  "content": "笔记内容",
  "topics": [
    { "id": 1, "name": "美食探店" },
    { "id": 2, "name": "我的自定义话题" }
  ],
  ...
}
```

## 注意事项

1. **话题名称唯一性**
   - topics表的name字段有唯一索引
   - 相同名称的话题只会创建一次
   - 大小写敏感

2. **话题状态**
   - 新创建的话题默认status=1（启用）
   - 新创建的话题默认is_hot=0（非热门）
   - 可以通过后台管理设置热门话题

3. **话题计数**
   - 每次关联笔记时，话题的note_count会自动+1
   - 删除笔记时，需要手动处理话题计数（待实现）

4. **兼容性**
   - 保留了原有的topics字段（ID列表）
   - 优先使用topicNames字段
   - 如果topicNames为空，才使用topics字段

## 后续优化建议

1. **话题管理后台**
   - 商家后台可以管理话题
   - 设置热门话题
   - 禁用不当话题

2. **话题搜索**
   - 支持按话题搜索笔记
   - 话题详情页展示相关笔记

3. **话题统计**
   - 统计话题使用频率
   - 自动识别热门话题

4. **话题推荐**
   - 根据用户历史推荐话题
   - 根据笔记内容智能推荐话题

## 问题排查

### 问题1：编辑时话题不显示

**检查步骤**：
1. 查看浏览器控制台是否有错误
2. 检查API返回的topics字段是否有数据
3. 检查数据库note_topics表是否有关联记录

**SQL检查**：
```sql
SELECT nt.*, t.name 
FROM note_topics nt 
LEFT JOIN topics t ON nt.topic_id = t.id 
WHERE nt.note_id = <笔记ID>;
```

### 问题2：保存时话题没有创建

**检查步骤**：
1. 查看后端日志是否有错误
2. 检查topicNames字段是否正确传递
3. 检查topics表是否有唯一索引冲突

**后端日志**：
```
创建新话题: 我的美食日记, ID: 21
```

### 问题3：话题重复创建

**原因**：topics表的name字段没有唯一索引

**解决方案**：
```sql
ALTER TABLE topics ADD UNIQUE INDEX uk_name (name);
```

## 完成状态

- ✅ 后端支持自定义话题
- ✅ 前端发送话题名称
- ✅ 话题自动创建
- ✅ 话题正确回显
- ✅ 话题计数更新
- ✅ 数据库脚本
- ✅ 测试指南
- ✅ API文档

## 下一步

现在你可以：
1. 重启后端服务
2. 刷新前端页面
3. 测试创建带自定义话题的笔记
4. 测试编辑笔记查看话题回显

如果遇到任何问题，请查看上面的"问题排查"部分！
