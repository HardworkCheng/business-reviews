# 差评智能回复助手 - 功能文档

## 功能概述

本功能使用 LangChain4j 框架对接 DeepSeek 大语言模型，帮助商家自动生成针对差评的高情商回复文案。商家可以选择或输入自定义的补偿策略，AI 会结合用户差评内容生成温暖、专业、有安抚性的回复。

## 技术栈

- **框架**: Spring Boot 3.x
- **AI 框架**: LangChain4j 0.36.2
- **LLM 提供商**: DeepSeek (使用 OpenAI 兼容 API)

## API 接口

### 生成差评回复

**请求路径**: `POST /merchant/reply/generate`

**请求头**:
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**请求体**:
```json
{
    "reviewText": "空调太热了，根本吃不下去饭！",
    "strategy": "送两张冰粉券"
}
```

**参数说明**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| reviewText | String | 是 | 用户的差评内容 |
| strategy | String | 否 | 商家的补偿策略，如空则 AI 不会承诺任何优惠 |

**响应示例**:
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "reply": "亲爱的同学，真的非常抱歉给您带来了这么糟糕的用餐体验！😭 看到您反馈空调太热影响了吃饭的心情，我们心里也很过意不去。我们已经立刻联系物业检查空调设备了。为了表达我们的歉意，特意为您准备了两张冰粉券，希望能为您消消暑。真的希望您能再给我们一次机会！",
        "generatedAt": "2024-12-28 23:15:00"
    }
}
```

## AI Prompt 设计

### 人设 (System Message)
- 角色: 校园商圈的金牌客服经理
- 特点: 擅长危机公关和情绪安抚
- 语气: 诚恳、温暖、专业
- 可使用 emoji 表情使回复更亲切

### 5步回复法
1. **真诚致歉** → 开头先表达诚挚的歉意
2. **共情用户** → 复述用户的问题，表示理解他们的不满
3. **提出改进** → 说明会采取什么具体措施改进
4. **补偿方案** → 如果有补偿策略则自然带出，没有则跳过
5. **期待回归** → 再次致歉并邀请顾客再次光临

### 重要规则
- 回复字数控制在 100-200 字
- 使用亲切称呼（亲、同学）
- 如果补偿策略为空，**绝不编造**任何优惠承诺

## 代码结构

```
backend-business-reviews/
├── backend-business-reviews-entity/
│   └── src/main/java/com/businessreviews/model/
│       ├── dto/ai/
│       │   └── GenerateReplyDTO.java       # 请求 DTO
│       └── vo/ai/
│           └── GenerateReplyVO.java        # 响应 VO
│
├── backend-business-reviews-service/
│   └── src/main/java/com/businessreviews/service/
│       ├── ai/
│       │   ├── ReviewReplyAgent.java       # AI 服务接口 (@AiService)
│       │   └── ReviewReplyService.java     # 业务服务接口
│       └── impl/ai/
│           └── ReviewReplyServiceImpl.java # 业务服务实现
│
└── backend-business-reviews-web/
    └── src/main/java/com/businessreviews/web/merchant/
        └── MerchantReplyController.java    # API 控制器
```

## 前端使用

### 回复弹窗界面

在评论管理页面，点击"回复"按钮后弹出的对话框包含：

1. **用户评论展示区** - 显示原始差评内容和评分
2. **AI 智能回复区**
   - 补偿策略下拉选择框（预设选项）
   - 自定义补偿策略输入框
   - "AI 生成回复"按钮
3. **回复内容编辑区** - 显示 AI 生成的回复，商家可二次编辑
4. **提示信息** - 提醒商家 AI 生成内容仅供参考

### 预设补偿策略
- 仅诚恳道歉（无补偿）
- 送 5 元无门槛券
- 送 8 折折扣券
- 下次到店送饮料
- 送两份小菜
- 自定义输入

## 注意事项

1. **Token 限制**: 评论内容限制在 1000 字以内
2. **补偿策略**: 限制在 200 字以内
3. **兜底方案**: AI 调用失败时返回预设的默认回复
4. **安全性**: 需要商家登录后才能调用该接口
5. **二次确认**: AI 生成的回复需要商家确认后才会发送

## 依赖配置

确保 `application.yml` 中已配置 LangChain4j：

```yaml
langchain4j:
  open-ai:
    chat-model:
      api-key: ${DEEPSEEK_API_KEY:your-api-key}
      base-url: https://api.deepseek.com
      model-name: deepseek-chat
      temperature: 0.7
      max-tokens: 500
      timeout: 60s
```
