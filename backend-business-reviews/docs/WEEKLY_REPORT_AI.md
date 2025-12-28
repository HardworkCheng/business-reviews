# 商家评论口碑周报 - AI 智能分析功能

## 功能概述

本功能使用 LangChain4j 框架对接 DeepSeek/OpenAI 大语言模型，对商家过去 7 天的用户评论进行智能分析，生成结构化的周报。

## 技术栈

- **框架**: Spring Boot 3.2.0
- **AI 框架**: LangChain4j 0.36.2
- **LLM 提供商**: DeepSeek (使用 OpenAI 兼容 API)
- **工具**: FastJSON2 (用于 JSON 解析), Lombok

## API 接口

### 生成商家周报

**请求路径**: `GET /api/merchant/analytics/weekly-report/{shopId}`

**请求头**:
```
Authorization: Bearer <jwt_token>
```

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| shopId | Long | 是 | 门店ID |

**响应示例**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "sentimentScore": 8,
        "summary": "本周评价整体较好，顾客对口味和服务满意度较高",
        "pros": [
            "菜品口味受到顾客一致好评",
            "服务态度热情周到",
            "环境整洁舒适"
        ],
        "cons": [
            "高峰期等位时间较长",
            "部分菜品偶有出餐慢的情况"
        ],
        "advice": "建议优化高峰期客流管理，增加接待能力，同时加强后厨出餐效率培训",
        "reviewCount": 42,
        "generatedAt": "2024-12-28 22:15:00",
        "period": "2024-12-21 ~ 2024-12-28"
    }
}
```

**响应字段说明**:
| 字段 | 类型 | 说明 |
|------|------|------|
| sentimentScore | Integer | 情感评分 (0-10)，0=极度负面，10=极度正面 |
| summary | String | 总体摘要 (50字以内) |
| pros | List<String> | 优点列表 (最多3个) |
| cons | List<String> | 缺点列表 (最多3个) |
| advice | String | 给商家的改进建议 |
| reviewCount | Integer | 分析的评论数量 |
| generatedAt | String | 报告生成时间 |
| period | String | 统计期间 |

## 配置说明

### application.yml 配置

```yaml
# LangChain4j AI 配置
langchain4j:
  open-ai:
    chat-model:
      # DeepSeek API 配置
      api-key: ${DEEPSEEK_API_KEY:your-api-key}
      base-url: https://api.deepseek.com
      model-name: deepseek-chat
      temperature: 0.3
      max-tokens: 2000
      timeout: 60s
```

### 环境变量

推荐通过环境变量配置 API Key:

```bash
# Windows (PowerShell)
$env:DEEPSEEK_API_KEY="sk-your-api-key"

# Linux/Mac
export DEEPSEEK_API_KEY="sk-your-api-key"
```

### 切换到 OpenAI

如需使用 OpenAI 原生 API，修改配置如下:

```yaml
langchain4j:
  open-ai:
    chat-model:
      api-key: ${OPENAI_API_KEY:sk-your-openai-key}
      base-url: https://api.openai.com/v1
      model-name: gpt-3.5-turbo
```

## 代码结构

```
backend-business-reviews/
├── backend-business-reviews-entity/
│   └── src/main/java/com/businessreviews/model/dto/ai/
│       └── WeeklyReportDTO.java          # 响应数据模型
│
├── backend-business-reviews-service/
│   └── src/main/java/com/businessreviews/service/
│       ├── ai/
│       │   ├── ReviewAnalysisAgent.java      # AI 服务接口 (@AiService)
│       │   └── ReviewAnalysisService.java    # 业务服务接口
│       └── impl/ai/
│           └── ReviewAnalysisServiceImpl.java # 业务服务实现
│
└── backend-business-reviews-web/
    └── src/main/java/com/businessreviews/web/merchant/
        └── MerchantDashboardController.java  # API 控制器
```

## 注意事项

1. **Token 限制**: 为防止超出 LLM 的 Token 限制，系统会自动:
   - 限制最多处理 100 条评论
   - 限制评论文本总长度不超过 5000 字符

2. **空评论处理**: 自动过滤空白评论

3. **错误处理**: 
   - 如果没有评论，返回默认报告
   - AI 服务异常时返回友好错误信息

4. **安全性**: 需要商家登录后才能调用该接口

## 依赖版本

```xml
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-spring-boot-starter</artifactId>
    <version>0.36.2</version>
</dependency>
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-open-ai-spring-boot-starter</artifactId>
    <version>0.36.2</version>
</dependency>
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2</artifactId>
    <version>2.0.43</version>
</dependency>
```
