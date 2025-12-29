# 后端 AI 功能实现分析（4 个功能）

本文档基于当前后端代码实现，逐一说明四个 AI 功能的实现方式、调用流程、关键类与关键配置，力求**通俗易懂且细节充分**。

## 总览：四个 AI 功能

1. 商家评论智能回复（好评/差评自动识别 + 生成回复）
2. 探店笔记 AI 智能生成（多模态：图片 + 标签）
3. AI 内容审核（笔记/评论发布后的异步审核）
4. 商家评论口碑周报 AI 分析（7 天评论总结）

## 统一技术栈与模型配置

### AI 框架与模型

- **AI 框架**：LangChain4j 0.36.2
- **模型接入方式**：OpenAI 兼容 API（DeepSeek / 通义千问 Qwen-VL）
- **模型配置入口**：`backend-business-reviews-web/src/main/java/com/businessreviews/config/AiModelConfig.java`

该配置类创建了两个模型：

- `deepSeekChatModel`（@Primary）：用于文本类 AI 功能
  - 功能覆盖：评论智能回复、评论周报分析、内容审核
- `visionChatModel`（@Qualifier("visionChatModel")）：用于视觉多模态
  - 功能覆盖：探店笔记 AI 生成（图片识别）

### 统一配置文件

- `backend-business-reviews-web/src/main/resources/application.yml`
  - `ai.deepseek.*`：DeepSeek 模型参数
  - `ai.qwen-vision.*`：Qwen-VL 视觉模型参数

### 统一接入方式

- **文本类 AI 功能** 使用 `@AiService` 自动生成代理：
  - 例如：`SmartReplyAgent`、`ReviewAnalysisAgent`、`ContentAuditAgent`
- **视觉多模态 AI 功能** 直接注入 `ChatLanguageModel` 手动构建多模态消息
  - 例如：`VisionNoteServiceImpl`

---

## 1. 商家评论智能回复

### 功能目标

商家在后台一键生成评论回复，AI 自动判断是好评还是差评，使用不同语气和策略输出回复文案。

### API 入口

- **路径**：`POST /api/merchant/reply/generate`
- **控制器**：`backend-business-reviews-web/src/main/java/com/businessreviews/web/merchant/MerchantReplyController.java`
- **请求 DTO**：`backend-business-reviews-entity/src/main/java/com/businessreviews/model/dto/ai/GenerateReplyDTO.java`

请求体关键字段：

- `reviewText`：评论内容（必填，≤1000 字）
- `strategy`：商家赠礼/补偿策略（可选，≤200 字）

### 处理流程（简化版）

1. `MerchantReplyController` 接收请求并做参数校验（`@Valid`）。
2. 调用 `ReviewReplyServiceImpl.generateReply()`。
3. 服务内部调用 `SmartReplyAgent.generateSmartReply()`。
4. AI 返回文本回复。
5. 若 AI 调用失败，返回默认兜底回复。

### 核心实现类

- AI 代理接口：`backend-business-reviews-service/src/main/java/com/businessreviews/service/ai/SmartReplyAgent.java`
- 业务服务实现：`backend-business-reviews-service/src/main/java/com/businessreviews/service/impl/ai/ReviewReplyServiceImpl.java`

### Prompt 设计关键点（通俗解释）

`SmartReplyAgent` 使用 `@SystemMessage` 和 `@UserMessage` 明确规定：

- **AI 先判断评论情感**（好评/差评/中性）
- **好评** → 开心、感激、俏皮
- **差评** → 诚恳、安抚、专业
- **赠礼策略语境必须正确**
  - 好评：是“惊喜回馈”
  - 差评：是“补偿/表达歉意”
- **字数控制 100-150 字**
- **不能乱编优惠**（如果没传策略）

### 兜底策略

当 AI 接口异常时：

- `ReviewReplyServiceImpl.generateDefaultReply()` 返回一个中性回复模板
- 不区分好评/差评，避免误导

### 安全与权限

- `/merchant/**` 路径由 `MerchantAuthInterceptor` 拦截，需要商家登录
- 拦截器类：`backend-business-reviews-web/src/main/java/com/businessreviews/merchant/interceptor/MerchantAuthInterceptor.java`

---

## 2. 探店笔记 AI 智能生成

### 功能目标

用户发布探店笔记时点击“AI 写笔记”，系统根据**图片 + 标签**生成小红书风格文案。

### API 入口

- **路径**：`POST /api/note/generate`
- **控制器**：`backend-business-reviews-web/src/main/java/com/businessreviews/web/app/NoteAIController.java`
- **请求 DTO**：`backend-business-reviews-entity/src/main/java/com/businessreviews/model/dto/ai/NoteGenerateRequest.java`

请求体关键字段：

- `shopName`：商家名（可选）
- `imageUrls`：图片 URL 列表（必填，至少 1 张）
- `tags`：用户标签（可选）

### 处理流程（简化版）

1. `NoteAIController` 接收请求。
2. `VisionNoteServiceImpl.generateNote()` 校验至少有 1 张图片。
3. 构建 **系统提示词** + **多模态用户消息**（文本 + 图片 URL）。
4. 调用视觉模型 `visionChatModel`（Qwen-VL）。
5. 从 AI 返回的文本中解析“标题 + 正文”。
6. 若 AI 调用失败，返回默认模板。

### 多模态消息构建（通俗解释）

`VisionNoteServiceImpl` 把“文字描述”和“图片链接”一起传给模型：

- 文字部分说明：商家名、用户标签、写作要求
- 图片部分：每张 URL 都会作为 `ImageContent` 发送

这相当于告诉 AI：“先看这些图，再按我的要求写文案”。

### 核心实现类

- 服务接口：`backend-business-reviews-service/src/main/java/com/businessreviews/service/ai/VisionNoteService.java`
- 服务实现：`backend-business-reviews-service/src/main/java/com/businessreviews/service/impl/ai/VisionNoteServiceImpl.java`

### Prompt 设计关键点

系统提示词要求：

- 标题吸睛（20 字以内，带 emoji）
- 正文 150-250 字，多段落
- 必须自然融合标签
- 结尾附推荐指数（例如：?????）
- 输出格式必须包含 `---` 分割标题和正文

### 解析逻辑与兜底

- 优先使用 `---` 分割标题/正文
- 其次使用双换行分割
- 再否则取第一行做标题
- AI 失败：`generateDefaultNote()` 返回固定模板

### 注意点

- 图片 URL 必须是公网可访问（例如 OSS）
- 没有图片会直接抛 `IllegalArgumentException`

---

## 3. AI 内容审核（笔记/评论）

### 功能目标

用户发布笔记或评论后，系统自动异步审核内容是否违规。

### 触发入口（事件驱动）

- **笔记发布事件**：`NoteServiceImpl.publishNote()` 中发布 `NoteCreatedEvent`
- **评论发布事件**：`CommentServiceImpl.addComment()` 中发布 `CommentCreatedEvent`

关键类：

- 事件类：
  - `backend-business-reviews-common/src/main/java/com/businessreviews/event/NoteCreatedEvent.java`
  - `backend-business-reviews-common/src/main/java/com/businessreviews/event/CommentCreatedEvent.java`

- 发布入口：
  - `backend-business-reviews-service/src/main/java/com/businessreviews/service/impl/app/NoteServiceImpl.java`
  - `backend-business-reviews-service/src/main/java/com/businessreviews/service/impl/app/CommentServiceImpl.java`

### 异步审核流程（详细）

1. 用户发布笔记或评论。
2. 系统先写入数据库：
   - 笔记状态设为 `PENDING=3`（审核中）
   - 评论默认状态 `1`（正常）
3. 发布事件：`NoteCreatedEvent` / `CommentCreatedEvent`
4. `AuditEventListener` 异步监听事件，开始调用 AI 审核
5. `ContentSecurityServiceImpl` 调用 `ContentAuditAgent`
6. 根据结果更新状态：
   - 笔记：通过 → `1`；违规 → `2`
   - 评论：违规 → `2`
7. 违规时发送“AI审核助手”通知给用户

### 核心实现类

- AI 审核代理：`backend-business-reviews-service/src/main/java/com/businessreviews/service/ai/ContentAuditAgent.java`
- 审核服务实现：`backend-business-reviews-service/src/main/java/com/businessreviews/service/impl/ai/ContentSecurityServiceImpl.java`
- 异步监听器：`backend-business-reviews-service/src/main/java/com/businessreviews/listener/AuditEventListener.java`

### 审核模型输出（结构化）

`ContentAuditAgent` 直接返回 `AuditResult` 对象：

- `isSafe`：是否安全
- `type`：违规类型（SAFE / ADVERTISEMENT / ABUSE / SENSITIVE）
- `reason`：违规原因
- `suggestion`：整改建议

这个对象由 LangChain4j 自动将 JSON 反序列化，无需手工解析。

DTO：`backend-business-reviews-entity/src/main/java/com/businessreviews/model/dto/ai/AuditResult.java`
枚举：`backend-business-reviews-common/src/main/java/com/businessreviews/enums/ViolationType.java`

### 校园场景规则

Prompt 中特别强调：

- **允许内容**：校园卡转让、失物招领、二手交易等
- **严禁内容**：兼职刷单、代写论文、网赚推广

### 兜底策略

如果 AI 调用失败：

- `ContentSecurityServiceImpl` 直接返回 `safe()`
- 即：**先允许发布**，后续人工复查

### 审核通知

违规时发送系统通知：

- 通知类型：`NOTICE_TYPE_AUDIT = 5`
- 发送人：虚拟用户 `AI审核助手 (userId=0)`
- 通知内容包含违规原因和整改建议

通知发送实现：`backend-business-reviews-service/src/main/java/com/businessreviews/service/impl/app/MessageServiceImpl.java`

### 异步线程池

- 配置类：`backend-business-reviews-web/src/main/java/com/businessreviews/config/AsyncConfig.java`
- 线程池名称：`asyncExecutor`
- 用于避免审核阻塞用户请求

---

## 4. 商家评论口碑周报 AI 分析

### 功能目标

系统自动分析商家最近 7 天评论，生成结构化周报：情感评分、优缺点、建议等。

### API 入口

- **路径**：`GET /api/merchant/analytics/weekly-report/{shopId}`
- **控制器**：`backend-business-reviews-web/src/main/java/com/businessreviews/web/merchant/MerchantDashboardController.java`

### 处理流程（详细）

1. 校验商家身份（`MerchantContext.requireMerchantId()`）。
2. 查询店铺是否存在。
3. 拉取过去 7 天内的评论（只取 `status=1` 的正常评论）。
4. 最多处理 100 条评论，总文本长度限制 5000 字。
5. 组装评论文本（附评分）。
6. 调用 `ReviewAnalysisAgent` 生成 JSON 格式周报。
7. 使用 `fastjson2` 解析成 `WeeklyReportDTO`。
8. 返回给前端。

### 核心实现类

- AI 代理：`backend-business-reviews-service/src/main/java/com/businessreviews/service/ai/ReviewAnalysisAgent.java`
- 服务实现：`backend-business-reviews-service/src/main/java/com/businessreviews/service/impl/ai/ReviewAnalysisServiceImpl.java`
- DTO：`backend-business-reviews-entity/src/main/java/com/businessreviews/model/dto/ai/WeeklyReportDTO.java`

### Prompt 设计关键点

要求严格输出 JSON：

```json
{
  "sentimentScore": 8,
  "summary": "本周评价整体较好...",
  "pros": ["优点1", "优点2"],
  "cons": ["缺点1"],
  "advice": "建议..."
}
```

系统会尝试从 AI 响应中提取 JSON（即使返回了 ```json 代码块）。

### 兜底与异常处理

- **无评论** → 返回默认周报（提示本周暂无评论）
- **AI 调用失败** → 抛出 `BusinessException(500)` 提示“AI 服务不可用”
- **JSON 解析失败** → 返回默认结构（sentimentScore=5）

---

## 关键依赖与库

- `dev.langchain4j:langchain4j-spring-boot-starter`
- `dev.langchain4j:langchain4j-open-ai-spring-boot-starter`
- `com.alibaba.fastjson2:fastjson2`

定义位置：

- `backend-business-reviews/pom.xml`
- `backend-business-reviews/backend-business-reviews-service/pom.xml`

---

## 小结：四个 AI 功能的实现模式对比

| 功能 | 模型 | 调用方式 | 输出形式 | 兜底策略 |
|------|------|----------|----------|----------|
| 智能回复 | DeepSeek | @AiService 代理 | 文本 | 默认中性回复 |
| 探店笔记生成 | Qwen-VL | 手动构造多模态消息 | 标题+正文文本 | 默认笔记模板 |
| 内容审核 | DeepSeek | @AiService 代理 | 结构化对象 | 允许发布，人工复查 |
| 周报分析 | DeepSeek | @AiService 代理 | JSON → DTO | 空报告或默认值 |

---

如需进一步补充（比如具体数据库字段、前端调用路径、示例请求/响应），可以告诉我你希望更详细到哪一层。
