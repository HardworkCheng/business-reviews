你是一位精通 Java Spring Boot 和 LangChain4j 的全栈架构师。
我现在要从零开发一个uniapp用户端核心功能：“智能探店笔记生成器（支持图生文）”。
请帮我完成从 DTO 到 Controller 再到 Service 的完整代码实现。

### 1. 业务需求
这是一个辅助大学生快速发布探店笔记的工具。
- **输入**:
    1. `shopName`: 商家名称。
    2. `imageUrls`: 用户上传的图片 URL 列表（可能 1 张，也可能多张）。
    3. `tags`: 用户选填的标签列表（如“好吃”、“量大”），**可能为空**。
- **逻辑**:
    - **场景 A (有图+无标签)**: AI 充当“眼睛”，识别图片中的食物色泽、分量、环境氛围，自动发挥生成笔记。
    - **场景 B (有图+有标签)**: AI 将视觉信息与用户标签结合。例如用户标签是“太辣了”，图片中全是红油，AI 应描述“看着这满碗红油就知道辣得过瘾”。
- **输出**:
    - 一篇“小红书风格”的短文（Emoji 丰富、语气年轻活泼、分段清晰、200字左右）。

### 2. 技术栈
- **框架**: Spring Boot (基于当前项目)
- **AI 框架**: LangChain4j (基于当前项目)
    - 核心类: `ChatLanguageModel` (用于手动发送消息), `UserMessage`, `ImageContent`, `TextContent`。
- **工具**: Lombok, FastJSON2

### 3. 代码生成详细要求

请一步步生成以下文件，并给出必要的解释：

**Step 1: 定义数据传输对象 (DTO)**
创建 `NoteGenerateRequest` 类，包含商家名、图片链接列表、标签列表。

**Step 2: 实现核心服务 (VisionNoteService)**
这是最关键的部分。由于图片数量是不确定的，**请不要**使用简单的 `@UserMessage` 字符串模板注解。
请演示如何**手动构建**一个多模态的 `UserMessage`：
1.  注入 `ChatLanguageModel`。
2.  构建 SystemMessage：设定人设为“眼光毒辣、语气夸张的大学生探店博主”。
3.  构建 UserMessage：
    - 先添加一段 `TextContent`，包含商家名和标签信息（如果标签存在）。告诉 AI：“如果标签为空，请完全基于图片内容发挥”。
    - 然后遍历 `imageUrls`，为每一张图片创建一个 `ImageContent` 并加入到 Message 中。
    - **注意**: 如果你使用的是 `ImageContent.from(url)`，请确保代码逻辑正确。
4.  调用模型并返回生成的文本。

**Step 3: 实现控制器 (NoteController)**
提供一个 RESTful 接口 `/api/note/generate`，接收 POST 请求并调用 Service。

### 4. System Prompt (内置在代码中)
请在 Service 代码的 SystemMessage 中包含以下策略：
"你具备视觉识别能力。请仔细观察图片细节（食物光泽、摆盘、环境灯光）。
写作风格要求：

1. 标题要吸睛（使用 '‼️', '✨' 等符号）。
2. 正文多用 Emoji，语气活泼（如'绝绝子', '暴风吸入', '按头安利'）。
3. 如果用户给了标签，必须在文中体现；如果没有标签，请根据图片通过想象力补充口感描述。
4. 结尾给出推荐指数（⭐⭐⭐⭐⭐）。"

请生成完整、可运行的 Java 代码。

### 💡 给开发者的重要“避坑”指南

在 AI 生成代码后，你在运行前需要注意以下 2 点，这决定了功能能不能跑通：

#### 1. 模型必须选对（非常重要！）

这个功能依赖 **Vision (视觉)** 能力。你不能用普通的聊天模型。 在你的 `application.yml` 中，你需要配置支持视觉的模型。

**推荐配置 (如果你用 阿里通义千问 Qwen-VL，性价比高且支持图片)：**

langchain4j:
  open-ai:
    chat-model:
      base-url: https://dashscope.aliyuncs.com/compatible-mode/v1 # 阿里云兼容 OpenAI 接口
      api-key: ${ALI_API_KEY}
      model-name: qwen-vl-max # 或者 qwen-vl-plus，这是支持看图的模型
      timeout: 60s

#### 2. 图片必须是公网 URL(我的项目中已有调用阿里云OSS上传图片方法)

LangChain4j 发送给大模型的是图片的 **URL 地址**。

- **本地测试时**：你不能传 `C:/Users/Images/1.jpg`。
- **正确做法**：图片必须已经上传到了你的阿里云 OSS / 七牛云 / 腾讯云 COS，得到一个 `http://.../xxx.jpg` 的链接，然后把这个链接传给后端。
- 注意我的项目中已有相关阿里云上传OSS方法,请检查