# AI内容审核系统文档

> ✅ **实现状态：已完成** - 2025-12-29

## 1. 功能概述

为了维护校园社区的纯净环境，用户发布的内容（笔记/评论）在写入数据库后，会经过 AI 的自动预审。

### 1.1 审核类型

| 违规类型 | 枚举值 | 说明 |
|---------|-------|------|
| 安全内容 | `SAFE` | 内容正常，无违规 |
| 广告引流 | `ADVERTISEMENT` | 包含微信号、二维码、兼职刷单等 |
| 攻击谩骂 | `ABUSE` | 针对个人的辱骂、人身攻击、网络暴力 |
| 敏感内容 | `SENSITIVE` | 色情低俗或敏感政治话题 |

### 1.2 校园场景特化规则

- ✅ **正常内容（不是违规）**: 校园卡转让、失物招领、二手书交易、拼车拼单、宿舍用品转让
- ❌ **严重违规**: 兼职刷单、代写论文、网赚推广

## 1.3 已实现功能

- ✅ 笔记发布时自动触发AI审核（异步）
- ✅ 评论发布时自动触发AI审核（异步）
- ✅ 用户可在个人主页查看自己所有状态的笔记
- ✅ 前端显示审核状态标签（审核中/已隐藏/违规）
- ✅ 使用DeepSeek模型进行内容审核
- ✅ 审核失败时的降级策略（允许发布，后续人工复查）

## 2. 技术架构

### 2.1 异步审核流程

```
用户点击发布
    ↓
保存到数据库（status = PENDING=3）
    ↓
发布事件（NoteCreatedEvent / CommentCreatedEvent）
    ↓
立即返回给用户 "发布成功（审核中）"
    ↓
异步监听器（AuditEventListener）消费事件
    ↓
调用 ContentAuditAgent（LangChain4j + DeepSeek）
    ↓
根据审核结果更新数据库状态
    ├── 审核通过 → status = NORMAL=1
    └── 审核不通过 → status = HIDDEN=2
```

### 2.2 核心组件

| 组件 | 路径 | 说明 |
|-----|------|------|
| `ViolationType` | `backend-business-reviews-common/.../enums/ViolationType.java` | 违规类型枚举 |
| `AuditResult` | `backend-business-reviews-entity/.../model/dto/ai/AuditResult.java` | 审核结果DTO |
| `ContentAuditAgent` | `backend-business-reviews-service/.../service/ai/ContentAuditAgent.java` | AI审核Agent接口 |
| `ContentSecurityService` | `backend-business-reviews-service/.../service/ai/ContentSecurityService.java` | 内容安全服务接口 |
| `ContentSecurityServiceImpl` | `backend-business-reviews-service/.../service/impl/ai/ContentSecurityServiceImpl.java` | 内容安全服务实现 |
| `NoteCreatedEvent` | `backend-business-reviews-common/.../event/NoteCreatedEvent.java` | 笔记创建事件 |
| `CommentCreatedEvent` | `backend-business-reviews-common/.../event/CommentCreatedEvent.java` | 评论创建事件 |
| `AuditEventListener` | `backend-business-reviews-service/.../listener/AuditEventListener.java` | 异步审核监听器 |

## 3. AI提示词设计

### 3.1 System Message（审核员人设）

```
你是由学校教务处和学生会共同指定的"网络风纪委员"，负责审核校园社区的用户发布内容。

【审核标准】严格但公平，宁可错杀，不可放过。

【违规类型识别规则】
1. ADVERTISEMENT（广告引流）：
   - ✅ 判定为违规：微信号、QQ号、手机号、二维码、外链、兼职刷单、代写论文、校外培训招生
   - ❌ 不是违规（正常内容）：校园卡转让、失物招领、二手书交易、拼车拼单、宿舍用品转让
   
2. ABUSE（攻击谩骂）：
   - ✅ 判定为违规：辱骂、人身攻击、网络暴力、恶意诋毁、歧视言论
   - ❌ 不是违规：正常吐槽、合理批评（如"食堂饭菜太贵了"）
   
3. SENSITIVE（敏感内容）：
   - ✅ 判定为违规：色情低俗、政治敏感、涉及国家安全
   - ❌ 不是违规：正常的时事讨论或学术话题
   
4. SAFE（安全内容）：不属于上述任何违规类型
```

### 3.2 结构化输出

LangChain4j 会自动将 `AuditResult` 类的结构传递给大模型，模型按此结构输出 JSON，框架自动完成反序列化。

**无需手动解析 JSON！**

```java
AuditResult result = contentAuditAgent.auditContent(text);
if (!result.isSafe()) {
    throw new BusinessException("内容违规: " + result.getReason());
}
```

## 4. 状态说明

### 4.1 笔记状态（NoteStatus）

| 状态码 | 枚举值 | 说明 |
|-------|-------|------|
| 1 | `NORMAL` | 正常/已发布 - 审核通过，公开可见 |
| 2 | `HIDDEN` | 隐藏/已拒绝 - 审核不通过或用户隐藏 |
| 3 | `PENDING` | 审核中 - 新发布内容，等待AI审核 |
| 4 | `REJECTED` | 违规 - 内容违反社区规范 |

### 4.2 评论状态

| 状态码 | 说明 |
|-------|------|
| 1 | 正常 |
| 2 | 隐藏/审核不通过 |

## 5. 前端配合指南

### 5.1 发布后的状态

用户发完贴后，列表页获取的数据 `status` 是 `3 (审核中)`。

### 5.2 展示逻辑

- **别人的帖子**: 只显示 `status = 1` 的
- **自己的帖子**:
  - 如果 `status = 3`，显示灰色的"审核中"标签
  - 如果 `status = 2` 或 `status = 4`，显示红色的"违规已屏蔽"标签

### 5.3 发布失败时的提示

当用户发布被拦截时，前端弹窗提示：

```
发布失败，系统检测到您的内容包含微信号引流，建议移除联系方式后重试。
```

## 6. 生产环境建议

### 6.1 模型温度

在 `application.yml` 中，审核类任务的 `temperature` 应该设置得**非常低**（例如 `0.1` 或 `0.0`）。
因为审核需要的是**稳定、标准统一**，不需要 AI 发挥创造力。

### 6.2 降级策略

当 AI 审核服务异常时，当前实现采用**宽松策略**：
- 允许内容发布
- 记录日志，后续人工复查

如需采用**严格策略**（审核失败则拒绝发布），可修改 `ContentSecurityServiceImpl.auditContent()` 方法中的 catch 块。

### 6.3 线程池配置

`AsyncConfig` 已配置线程池参数：
- 核心线程数: 5
- 最大线程数: 20
- 队列容量: 200

避免高并发下服务器资源耗尽。

## 7. 扩展建议

### 7.1 记录审核日志

可以创建 `audit_logs` 表，记录每次审核的：
- 内容ID、类型（笔记/评论）
- 审核结果、违规类型
- 审核耗时
- AI模型版本

### 7.2 增加敏感词库

可以在 AI 审核前，先进行本地敏感词库快速过滤：
- 减少 AI 调用次数
- 降低成本
- 提高响应速度

### 7.3 通知用户

**✅ 已实现！** 审核不通过时，系统会自动向用户发送一条来自"AI审核助手"的通知：

**通知内容格式**：
```
【内容审核通知】

您发布的笔记「标题」因违反社区规范已被屏蔽。

📌 违规类型：广告引流 - 检测到微信号引流信息

💡 整改建议：请移除微信号后重试

如有疑问，请联系客服申诉。
```

**技术实现**：
- **发送者**: AI审核助手（用户ID=0，虚拟用户）
- **接收者**: 被屏蔽内容的发布者
- **通知类型**: 系统通知（type=5，存储在system_notices表）
- **实时推送**: 通过WebSocket实时推送

**前端显示**：
- 在消息页面的**"通知"Tab**中显示"AI审核助手"通知
- 头像使用 `/static/icons/ai-assistant.png`（与用户头像同等大小）
- 通知标题显示"AI审核助手"
- 通知内容显示完整的审核结果和整改建议
