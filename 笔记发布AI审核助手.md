你是一位精通 Java Spring Boot 和 LangChain4j 的后端架构师。
请帮我实现一个**“校园社区内容自动审核系统”**。

### 1. 业务目标
为了维护校园社区的纯净环境，用户发布的内容（笔记/评论）在写入数据库前，需要经过 AI 的自动预审。
AI 需要识别文本中是否包含以下违规内容：

1.  **广告引流** (Ads): 包含微信号、二维码、兼职刷单等。
2.  **攻击谩骂** (Abuse): 针对个人的辱骂、人身攻击、网络暴力。
3.  **涉黄涉政** (Sensitive): 色情低俗或敏感政治话题。
4.  **安全** (Safe): 内容正常。

### 2. 技术栈
- **框架**: Spring Boot 3.x(基于当前项目)
- **AI 框架**: LangChain4j (基于当前项目)
    - 核心特性: 利用 LangChain4j 的**结构化对象映射**能力（直接返回 Java Bean/Enum），而非手动解析 JSON 字符串。
- **工具**: Lombok

### 3. 代码生成要求

请按照以下步骤生成完整的 Java 代码：

**Step 1: 定义违规类型枚举 (ViolationType)**
创建一个 Enum `ViolationType`，包含：
- `SAFE` (安全)
- `ADVERTISEMENT` (广告引流)
- `ABUSE` (攻击谩骂)
- `SENSITIVE` (敏感内容)

**Step 2: 定义审核结果对象 (AuditResult)**
创建一个类 `AuditResult`：

- `boolean isSafe`: 是否安全。
- `ViolationType type`: 违规类型。
- `String reason`: 违规的具体原因说明（20字以内）。
- `String suggestion`: 给用户的整改建议（例如：“请移除微信号”）。

**Step 3: 定义 AI 服务接口 (ContentAuditAgent)**
使用 `@AiService` 注解。
- **关键点**: 方法的返回值直接定义为 `AuditResult` 对象。LangChain4j 会自动引导 LLM 输出匹配该对象的 JSON 结构。
- **System Message**:
    - 设定人设：你是由学校教务处和学生会共同指定的“网络风纪委员”，审核标准严格但公平。
    - 审核规则：
        - 宁可错杀，不可放过（对于疑似广告要严格判定）。
        - 校园卡转让、失物招领属于正常内容，**不是**广告。
        - 兼职刷单、代写论文属于**严重违规广告**。
- **User Message**: 传入待审核文本。

**Step 4: 实现业务服务 (ContentSecurityService)**
创建一个 Service 类，提供 `public AuditResult auditContent(String text)` 方法。
- 调用 Agent 获取结果。
- 打印日志记录审核过程。

### 4. 期望的调用示例
在代码注释中展示如何根据 `AuditResult` 进行业务处理：
if (!result.isSafe()) {
    // 拦截发布，抛出异常或返回错误信息给前端
    throw new BusinessException("内容违规: " + result.getReason());
}

请生成包含 import 的完整代码。

### 💡 核心设计思路解读（开发者必读）

这个提示词里包含了我为你设计的几个**“小心机”**，能让你的审核系统更智能：

#### 1. 结构化映射（Killer Feature）

注意 **Step 3**。普通的做法是让 AI 返回字符串 "YES/NO"，然后你再写 `if (str.equals("YES"))`。 **LangChain4j 的做法**是：你直接定义一个 Java 类 `AuditResult`，框架会自动把这个类的结构（Schema）喂给大模型，并强制大模型按这个结构吐出数据。你的代码里**不需要**出现任何 JSON 解析库（如 Jackson/FastJson）的调用，代码会异常整洁。

#### 2. 校园场景的“特化”规则

在 **System Message** 中，我特意加了这两条规则：

- *“校园卡转让、失物招领属于正常内容，不是广告。”* —— 这一点**至关重要**！否则通用的 AI 会把同学出的闲置二手书、丢卡找人全部当成广告拦截掉，导致用户体验极差。
- *“兼职刷单、代写论文属于严重违规。”* —— 这是校园网最常见的黑产，需要重点打击。

#### 3. 建议字段 (`suggestion`)

我还让 AI 返回了一个 `suggestion` 字段。

- **用途**：当用户发布失败时，前端直接弹窗提示：“发布失败，系统检测到您的内容包含微信号引流，**建议移除联系方式后重试**。”
- 这比冷冰冰的“内容违规”要友好得多，能指导用户如何修改。

### ⚠️ 生产环境建议

1. **模型温度 (Temperature)**： 在 `application.yml` 中，审核类任务的 `temperature` 应该设置得**非常低**（例如 `0.1` 或 `0.0`）。因为审核需要的是**稳定、标准统一**，不需要 AI 发挥创造力。
2. **异步处理**： 如果审核很慢（需要 2-3 秒），建议不要阻塞用户发帖接口。
   - **策略**：用户点发布 -> 状态存为 `PENDING` (审核中) -> 提示“发布成功，审核通过后显示” -> 后台异步跑 AI 审核 -> 更新状态为 `PUBLISHED` 或 `REJECTED`。

**优化方案建议:**
请基于我们之前实现的 `ContentAuditAgent` (LangChain4j 审核服务)，帮我实现一套**“异步内容审核工作流”**。

### 1. 业务痛点与目标
目前的内容发布是**同步**的，用户点击发布后需要等待 AI 审核完成（约 2-3 秒）才能收到响应，体验极差。
我们需要重构为**异步非阻塞**流程：

1.  **用户侧**：点击发布 -> 立即返回“发布成功（审核中）” -> 页面显示状态为“审核中”。
2.  **系统侧**：后台异步触发 AI 审核 -> 审核通过则自动公开 -> 审核不通过则标记违规并隐藏。

### 2. 技术架构方案
请使用 **Spring Event (事件驱动) + @Async (异步执行)** 的组合来实现。
* **发布主线程**：只负责落库（状态为 PENDING），发布事件，然后立即返回。
* **事件监听器**：在独立线程中消费事件，调用 LangChain4j 进行审核，并更新数据库状态。

### 3. 代码生成详细步骤

请一步步生成以下代码，并修改现有的相关类：

**Step 1: 数据库模型升级 (Post Entity)**
修改 `Post` 实体类（或 DTO），增加审核状态字段：
- `status`: Integer/Enum (0: 审核中/PENDING, 1: 已发布/PUBLISHED, 2: 拒绝/REJECTED)。
- `rejectReason`: String (审核不通过的原因)。
*注意：新发布的笔记默认状态应设为 0 (PENDING)。*

**Step 2: 定义审核事件 (PostCreatedEvent)**
创建一个继承自 `ApplicationEvent` 的事件类，包含 `postId` 和 `content` 等必要信息。

**Step 3: 实现异步监听器 (AuditEventListener)**
创建一个组件 `AuditEventListener`：
- 使用 `@EventListener` 监听 `PostCreatedEvent`。
- 使用 `@Async` 注解确保该方法在单独线程池中执行（不阻塞主线程）。
- **核心逻辑**：
    1.  调用 `ContentAuditAgent.auditContent(content)` 获取 `AuditResult`。
    2.  根据 `AuditResult.isSafe()`：
        - **Safe**: 将数据库中该 postId 的状态更新为 1 (PUBLISHED)。
        - **Unsafe**: 将状态更新为 2 (REJECTED)，并保存 `reason` 和 `suggestion`。
    3.  (可选) 打印日志：`"笔记 [id] 审核完成，结果：..."`。

**Step 4: 重构发布服务 (PostService)**
修改 `createPost` 方法：
1.  保存笔记到数据库，强制设置 `status = PENDING`。
2.  使用 `ApplicationEventPublisher` 发布 `PostCreatedEvent`。
3.  **立即返回**，不等待审核结果。

**Step 5: 开启异步配置**
生成一个配置类 `AsyncConfig`，加上 `@EnableAsync` 注解，并配置一个简单的线程池（TaskExecutor），以防高并发下默认线程池资源耗尽。

### 4. 关键逻辑提示 (System Prompt for AI)
请在生成的代码注释中强调：
- 事务分离：发布笔记的事务在 Controller 返回前已提交，异步审核是新的事务。
- 竞态条件：确保异步线程查数据库时，主线程的数据已经落库（通常 Event 机制没问题，但要注意数据一致性）。

### 5. 期望代码结构
请生成完整的 `Post` 实体更新、`PostCreatedEvent`、`AuditEventListener`、`AsyncConfig` 以及重构后的 `PostService` 代码。



### 💡 架构师视角的解读（为什么要这样写？）

1. **为什么用 Spring Event 而不是直接调用 `@Async` Service 方法？**
   - **解耦**：如果以后你需要“审核不通过 -> 发送站内信通知”或者“审核通过 -> 增加用户积分”，你只需要再写一个 Listener 监听同一个事件即可，完全不用改动发布笔记的核心代码。这是**高内聚低耦合**的最佳实践。
   - **避免循环依赖**：在 Service 内部调用自身的 Async 方法往往会失效（因为绕过了 AOP 代理），用 Event 机制可以完美避开这个问题。
2. **为什么需要自定义线程池 (`AsyncConfig`)？**
   - Spring Boot 默认的 `@Async` 如果不配置线程池，可能会使用 `SimpleAsyncTaskExecutor`，它不重用线程，每次调用都新建线程。在校园高并发场景下（比如下课吃饭时间大家都在发帖），这会导致服务器内存爆炸。配置一个有界的线程池（比如最大 10 个线程）能保护你的服务器。
3. **前端配合指南**
   - 告诉你的前端同学：用户发完贴后，列表页获取的数据 `status` 是 `0 (审核中)`。
   - 前端展示逻辑：
     - 别人的帖子：只显示 `status = 1` 的。
     - **自己的帖子**：如果 `status = 0`，显示一个灰色的“审核中”标签；如果 `status = 2`，显示红色的“违规已屏蔽”标签，并展示 `rejectReason`。