# 笔记分享JSON解析错误修复

## 问题描述

分享笔记后，前端在解析 `noteData` 时出现 JSON 解析错误：
```
SyntaxError: Bad control character in string literal in JSON at position 167
```

## 根本原因

在 `MessageServiceImpl.shareNoteToUsers()` 方法中，使用了手动字符串拼接来构建 JSON：

```java
String noteDataJson = String.format(
    "{\"noteId\":%d,\"title\":\"%s\",\"coverImage\":\"%s\",\"content\":\"%s\"}",
    note.getId(),
    note.getTitle().replace("\"", "\\\""),
    note.getCoverImage(),
    note.getContent().substring(0, 50).replace("\"", "\\\"")
);
```

这种方式存在严重问题：
1. **只转义了双引号**，没有处理其他控制字符（换行符 `\n`、制表符 `\t`、回车符 `\r` 等）
2. **没有处理反斜杠** `\` 本身的转义
3. 笔记内容中的任何特殊字符都会导致 JSON 格式错误

## 解决方案

使用 Jackson 的 `ObjectMapper` 来正确序列化 JSON，它会自动处理所有特殊字符的转义：

```java
// 注入 ObjectMapper
private final ObjectMapper objectMapper;

// 使用 ObjectMapper 序列化
Map<String, Object> noteDataMap = new HashMap<>();
noteDataMap.put("noteId", note.getId());
noteDataMap.put("title", note.getTitle() != null ? note.getTitle() : "");
noteDataMap.put("coverImage", note.getCoverImage() != null ? note.getCoverImage() : "");

String content = note.getContent() != null ? note.getContent() : "";
if (content.length() > 50) {
    content = content.substring(0, 50);
}
noteDataMap.put("content", content);

noteDataJson = objectMapper.writeValueAsString(noteDataMap);
```

## 修改的文件

- `backend-business-reviews/backend-business-reviews-service/src/main/java/com/businessreviews/service/impl/app/MessageServiceImpl.java`
  - 添加 `ObjectMapper` 依赖注入
  - 添加 Jackson 导入：`import com.fasterxml.jackson.databind.ObjectMapper;`
  - 重写 `shareNoteToUsers()` 方法中的 JSON 序列化逻辑

## 测试验证

修复后需要测试以下场景：

1. **包含换行符的笔记标题/内容**
   - 创建包含多行文本的笔记
   - 分享该笔记
   - 验证接收方能正确显示笔记卡片

2. **包含特殊字符的笔记**
   - 标题包含引号、反斜杠等特殊字符
   - 内容包含制表符、emoji 等
   - 验证 JSON 解析成功

3. **空字段处理**
   - 笔记没有封面图
   - 笔记标题或内容为空
   - 验证不会出现 null 值导致的错误

## 预期效果

- ✅ 前端能正确解析 `noteData` JSON
- ✅ 笔记卡片正常显示标题、封面和内容预览
- ✅ 点击笔记卡片能跳转到笔记详情页
- ✅ 不再出现 "Bad control character" 错误

## 相关问题

这个修复同时解决了：
- WebSocket 推送的笔记分享消息解析问题
- 轮询获取的历史笔记分享消息解析问题
- 数据库中存储的 `note_data` 字段格式问题

## 最佳实践

**永远不要手动拼接 JSON 字符串！**

正确做法：
- ✅ 使用 `ObjectMapper.writeValueAsString()`
- ✅ 使用 `@JsonProperty` 注解的 DTO 类
- ✅ 让 Spring MVC 自动序列化响应对象

错误做法：
- ❌ 使用 `String.format()` 拼接 JSON
- ❌ 手动转义特殊字符
- ❌ 使用字符串替换处理 JSON

## 修复日期

2025-12-25
