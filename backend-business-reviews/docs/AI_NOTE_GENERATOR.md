# 探店笔记AI智能生成功能文档

## 📋 功能概述

本功能允许用户在发布探店笔记时，通过点击"AI写笔记"魔法按钮，利用视觉AI模型自动识别上传的图片内容，结合用户选择的标签，生成小红书风格的探店笔记。

### 核心特性
- **图片识别**：AI自动识别图片中的食物色泽、分量、摆盘、环境氛围
- **标签融合**：将用户选择的标签（如"好吃"、"量大"）自然融入笔记内容
- **小红书风格**：生成Emoji丰富、语气活泼、分段清晰的年轻化文案
- **智能兜底**：AI调用失败时自动返回默认模板

## 🏗️ 技术架构

### 后端（Java Spring Boot + LangChain4j）

```
com.businessreviews
├── model.dto.ai
│   └── NoteGenerateRequest.java      # 请求DTO
├── model.vo.ai
│   └── NoteGenerateVO.java           # 响应VO
├── service.ai
│   └── VisionNoteService.java        # 服务接口
├── service.impl.ai
│   └── VisionNoteServiceImpl.java    # 服务实现（核心）
└── web.app
    └── NoteAIController.java         # REST控制器
```

### 前端（UniApp Vue3）

```
front-business-reviews-Mobile/src
├── api
│   └── note.js                       # 新增 generateNoteByAI()
└── pages/publish
    └── publish.vue                   # 新增魔法按钮
```

## 🔧 API接口

### POST /api/note/generate

根据图片和标签生成探店笔记。

**请求参数：**

```json
{
  "shopName": "老王烧烤",          // 可选，商家名称
  "imageUrls": [                   // 必填，图片URL数组（OSS公网链接）
    "https://xxx.oss-cn-beijing.aliyuncs.com/1.jpg",
    "https://xxx.oss-cn-beijing.aliyuncs.com/2.jpg"
  ],
  "tags": ["好吃", "量大", "环境好"]  // 可选，用户标签
}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "title": "✨ 老王烧烤绝绝子！量大管饱必吃 ‼️",
    "content": "今天来探店老王烧烤！🔥\n\n一进门就被这满桌的烤肉惊艳到了～\n肉质超级鲜嫩，油脂滋滋作响 💯\n分量真的太足了，两个人根本吃不完！\n\n环境也很不错，适合朋友聚会 🎉\n服务态度超好，推荐推荐！\n\n推荐指数：⭐⭐⭐⭐⭐"
  }
}
```

## ⚙️ 配置说明

### 多模型架构

本项目配置了两个不同的AI模型，用于不同的业务场景：

| 模型 | Bean名称 | 用途 | 特点 |
|------|----------|------|------|
| DeepSeek | `deepSeekChatModel` (@Primary) | 商家端评论分析、智能回复 | 文本对话，性价比高 |
| 通义千问 Qwen-VL | `visionChatModel` | 用户端探店笔记AI生成 | 支持图片识别（Vision） |

### 配置文件说明

在 `application.yml` 中配置两个模型：

```yaml
ai:
  # DeepSeek 模型配置（商家端）
  deepseek:
    api-key: ${DEEPSEEK_API_KEY:sk-xxx}
    base-url: https://api.deepseek.com
    model-name: deepseek-chat
    temperature: 0.3
    max-tokens: 2000
    timeout: 60

  # 通义千问 Qwen-VL 模型配置（用户端）
  qwen-vision:
    api-key: ${ALI_API_KEY:sk-xxx}
    base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
    model-name: qwen-vl-max
    temperature: 0.7
    max-tokens: 2000
    timeout: 60
```

### 环境变量

```bash
# DeepSeek API Key
export DEEPSEEK_API_KEY=sk-xxxxxxxxxxxxx

# 阿里云通义千问 API Key
export ALI_API_KEY=sk-xxxxxxxxxxxxx
```

### 代码架构

- **`AiModelConfig.java`**：配置类，创建两个 `ChatLanguageModel` Bean
  - `deepSeekChatModel()`：`@Primary`，被 `@AiService` 注解的类自动使用
  - `visionChatModel()`：需要通过 `@Qualifier("visionChatModel")` 显式注入

- **`@AiService` 代理类**（使用 DeepSeek）：
  - `SmartReplyAgent` - 智能回复
  - `ReviewReplyAgent` - 差评回复
  - `ReviewAnalysisAgent` - 评论分析

- **手动注入的服务**（使用通义千问）：
  - `VisionNoteServiceImpl` - 通过 `@Qualifier("visionChatModel")` 注入

## 💡 使用指南

1. **进入发布页面**：点击底部"发布"Tab
2. **上传图片**：至少上传1张探店照片
3. **选择标签（可选）**：点击"添加话题"选择或输入标签
4. **点击魔法按钮**：点击"✨ AI写笔记"按钮
5. **等待生成**：约3-10秒完成（取决于图片数量和网络）
6. **编辑完善**：可以在AI生成的基础上进行调整
7. **发布笔记**：点击"发布"按钮完成发布

## ⚠️ 注意事项

1. **图片必须是公网URL**：图片需先上传到阿里云OSS获取公网链接，本地路径无法使用
2. **模型选择正确**：普通的DeepSeek/GPT-3.5不支持图片识别，必须使用Vision模型
3. **网络稳定性**：建议在良好网络环境下使用
4. **生成时间**：多图片情况下生成时间可能较长，请耐心等待

## 📊 场景处理

| 场景 | 图片 | 标签 | AI行为 |
|------|------|------|--------|
| 场景A | 有 | 无 | AI充当"眼睛"，完全基于图片发挥 |
| 场景B | 有 | 有 | AI结合图片+标签，生成更贴合用户感受的笔记 |
| 无效 | 无 | - | 提示"请先上传图片" |

## 🔍 相关文件

### 后端
- `NoteGenerateRequest.java` - 请求DTO
- `NoteGenerateVO.java` - 响应VO  
- `VisionNoteService.java` - 服务接口
- `VisionNoteServiceImpl.java` - 核心实现（使用视觉模型）
- `NoteAIController.java` - REST控制器
- `AiModelConfig.java` - **多模型配置类**

### 前端
- `note.js` - API方法 `generateNoteByAI()`
- `publish.vue` - 魔法按钮UI和逻辑

### 配置
- `application.yml` - AI模型配置（DeepSeek + Qwen-VL）

