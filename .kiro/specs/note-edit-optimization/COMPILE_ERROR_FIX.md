# 编译错误修复

## 问题

编译时出现错误：
```
java: 找不到符号
符号:   方法 setIsHot(int)
位置: 类型为com.businessreviews.model.dataobject.TopicDO的变量 topic
```

## 原因

`TopicDO` 类中的字段名是 `hot` 而不是 `isHot`：

```java
@TableField("is_hot")
private Integer hot;  // 字段名是 hot
```

因为使用了 Lombok 的 `@Data` 注解，自动生成的方法是：
- `setHot(Integer hot)` ✅
- `getHot()` ✅

而不是：
- `setIsHot(Integer isHot)` ❌
- `getIsHot()` ❌

## 解决方案

修改 `NoteServiceImpl.java` 第748行：

**修改前**：
```java
topic.setIsHot(0);  // ❌ 错误
```

**修改后**：
```java
topic.setHot(0);  // ✅ 正确
```

## 修改位置

文件：`backend-business-reviews/backend-business-reviews-service/src/main/java/com/businessreviews/service/impl/app/NoteServiceImpl.java`

方法：`findOrCreateTopic(String topicName)`

行号：748

## 验证

修改后重新编译：
```bash
cd backend-business-reviews
mvn clean compile
```

应该不再有编译错误。

## 注意事项

在使用 Lombok 的 `@Data` 注解时：
- 字段名 `hot` → 生成 `setHot()` / `getHot()`
- 字段名 `isHot` → 生成 `setIsHot()` / `getIsHot()`

对于布尔类型的字段，Lombok 会特殊处理：
- `boolean isHot` → 生成 `isHot()` / `setHot()`
- `Boolean isHot` → 生成 `getIsHot()` / `setIsHot()`

但对于 `Integer` 类型的字段，就是普通的 getter/setter 命名规则。
