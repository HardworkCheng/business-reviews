你是一位精通 Java Spring Boot 和 LangChain4j 的高级后端工程师。请帮我实现一个“商家评论口碑周报”的智能分析功能。

### 1. 技术栈与环境
- **框架**: Spring Boot 3.x(基于当前项目而定)
- **AI 框架**: LangChain4j (查找能够下载的相关版本)
    - 依赖: `langchain4j-spring-boot-starter-ai-services`
    - 依赖: `langchain4j-open-ai-spring-boot-starter` (连接 DeepSeek/OpenAI)
- **工具**: FastJSON2 (用于序列化), Lombok
- **数据库**: 使用现有的相关数据库来实现

### 2. 业务需求
我需要生成一份“商家每周口碑报告”。
- **输入**: 给定一个商家 ID (shopId)，从数据库拉取过去 7 天的所有评论文本。
- **处理**: 使用 LLM 对这些评论进行汇总分析。考虑到评论可能较多，需要对文本进行适当拼接或截断处理，防止超出 Token 限制。
- **输出**: 返回一个结构化的 Java 对象 (DTO)，包含：情感评分、总体摘要、3个优点、3个缺点、给商家的建议。

### 3. 代码生成要求
请按照以下步骤生成完整的 Java 代码：

**Step 1: 定义数据模型 (DTO)**
创建一个 `WeeklyReportDTO` 类，字段包含：

- `sentimentScore` (Integer, 0-10分)
- `summary` (String, 50字以内)
- `pros` (List<String>)
- `cons` (List<String>)
- `advice` (String)

**Step 2: 定义 AI 服务接口 (AiService)**
创建一个接口 `ReviewAnalysisAgent`，使用 `@AiService` 注解。
- 使用 `@SystemMessage` 设定角色：专业的餐饮商圈数据分析师。
- 使用 `@UserMessage` 传入商家名称和评论列表字符串。
- 要求 LLM 严格按照 JSON 格式输出，以便框架自动映射到 DTO。

**Step 3: 实现业务逻辑 (Service)**
创建一个 `ReviewAnalysisService` 类：
- 注入 `ReviewAnalysisAgent` 和 `ReviewMapper`。
- 实现方法 `generateReport(Long shopId)`。
- **关键逻辑**: 从数据库获取评论 List -> 过滤空评论 -> 拼接成一个长字符串 (限制最大长度，例如截取前 100 条或限制 5000 字符，防止 Token 溢出) -> 调用 Agent -> 返回 DTO。

**Step 4: 补充配置 (Optional)**
提供 `application.yml` 中 LangChain4j 连接 DeepSeek/OpenAI 的基础配置模板。

请保持代码整洁，添加必要的注释。