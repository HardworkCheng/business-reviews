# SSE 流式 AI 响应技术方案

> 本文档详细说明了 Business Reviews 系统中 AI 内容生成的流式输出（SSE）实现方案，提供 ChatGPT 式的打字机效果体验。

## 📋 目录

1. [问题背景](#问题背景)
2. [技术分析](#技术分析)
3. [解决方案](#解决方案)
4. [后端配置详解](#后端配置详解)
5. [前端接入指南](#前端接入指南)
6. [SSE 事件协议](#sse-事件协议)
7. [最佳实践](#最佳实践)
8. [部署配置](#部署配置)
9. [常见问题](#常见问题)

---

## 问题背景

### 同步 AI 生成的体验问题

在引入流式响应之前，AI 内容生成采用同步模式：用户发起请求后，需要等待整个内容生成完成才能看到结果。

```
┌─────────────────────────────────────────────────────────────────────┐
│                    同步模式用户体验                                   │
└─────────────────────────────────────────────────────────────────────┘

用户操作                      系统响应                    用户感知
─────────                    ─────────                  ─────────
点击"AI生成" ───────────────► 后端调用 AI 模型          ┌──────────┐
                              等待生成...                │ 加载中... │
                              等待生成...                │   😓     │
                              等待生成...                │ 10秒过去 │
                              等待生成...（10-30秒）      │   😰     │
                              生成完成！        ◄─────── │ 20秒过去 │
              ◄───────────── 一次性返回全部内容         └──────────┘
显示完整内容

❌ 问题：
- 长时间白屏/加载，用户焦虑
- 无法提前预览内容
- 无法中途取消
- 用户不确定系统是否在工作
```

### 流式模式的优势

```
┌─────────────────────────────────────────────────────────────────────┐
│                    流式模式用户体验                                   │
└─────────────────────────────────────────────────────────────────────┘

用户操作                      系统响应                    用户感知
─────────                    ─────────                  ─────────
点击"AI生成" ───────────────► 后端调用 AI 模型          ┌──────────┐
              ◄───────────── SSE: "探"                  │ 探       │
              ◄───────────── SSE: "店"                  │ 探店     │
              ◄───────────── SSE: "笔"                  │ 探店笔   │
              ◄───────────── SSE: "记"                  │ 探店笔记 │
              ◄───────────── SSE: "：" ...              │  ...✨   │
              ◄───────────── SSE: [DONE]               └──────────┘
显示完整内容                  生成完成！

✅ 优势：
- 首字 1-2 秒显示，即时反馈
- 逐字输出，仿佛 AI 在"思考"
- 可随时取消
- 打字机效果，体验感好
```

### 问题影响

| 指标 | 同步模式 | 流式模式 | 提升 |
|------|----------|----------|------|
| 首字显示时间 | 10-30 秒 | 1-2 秒 | **↓ 90%+** |
| 用户焦虑感 | 高 | 低 | ↓ 大幅降低 |
| 中途取消 | ❌ 不支持 | ✅ 支持 | 新增 |
| 交互体验 | 一般 | 优秀 | ↑ 显著提升 |

---

## 技术分析

### SSE vs WebSocket vs 轮询

| 技术 | 适用场景 | 复杂度 | 本场景推荐度 |
|------|----------|--------|--------------|
| **SSE** | 服务端单向推送 | ⭐ 简单 | ⭐⭐⭐⭐⭐ |
| WebSocket | 双向实时通信 | ⭐⭐⭐ 较复杂 | ⭐⭐ |
| 轮询 | 兼容性要求极高 | ⭐⭐ 中等 | ⭐ |

**为什么选择 SSE？**

1. **单向推送足够**：AI 生成是服务端向客户端单向推送
2. **基于 HTTP**：无需额外协议，防火墙友好
3. **自动重连**：浏览器原生支持断线重连
4. **简单实现**：Spring 提供 `SseEmitter` 开箱即用

### LangChain4j 流式模型

```java
// 同步模型（等待全部生成）
ChatLanguageModel model;
String response = model.generate(prompt);  // 阻塞等待

// 流式模型（逐 token 回调）
StreamingChatLanguageModel streamingModel;
streamingModel.generate(prompt, new StreamingResponseHandler<>() {
    @Override
    public void onNext(String token) {
        // 每生成一个 token 触发
    }
    @Override
    public void onComplete(Response<AiMessage> response) {
        // 生成完成
    }
});
```

---

## 解决方案

### 架构总览

```
┌─────────────────────────────────────────────────────────────────────┐
│                     SSE 流式 AI 响应架构                             │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────┐    POST /generate/stream     ┌───────────────────────┐
│             │ ───────────────────────────► │                       │
│  前端应用    │                              │   Spring Controller   │
│  (UniApp)   │                              │   (SseEmitter)        │
│             │ ◄───── SSE Connection ────── │                       │
└─────────────┘                              └───────────┬───────────┘
      ▲                                                  │
      │                                                  ▼
      │ event: token                          ┌───────────────────────┐
      │ data: "探"                            │                       │
      │                                       │   VisionNoteStream    │
      │ event: token                          │   ServiceImpl         │
      │ data: "店"                            │                       │
      │                                       │   - StreamingChat     │
      │ event: token                          │     LanguageModel     │
      │ data: "笔"                            │   - StreamingResponse │
      │                                       │     Handler           │
      │ event: done                           │                       │
      │ data: [DONE]                          └───────────┬───────────┘
      │                                                   │
      │                                                   ▼
      │                                        ┌───────────────────────┐
      │                                        │                       │
      └─────────── 逐 token 显示 ◄───────────── │   通义千问 Qwen-VL    │
                                               │   (流式 API)          │
                                               │                       │
                                               └───────────────────────┘
```

### 核心组件

| 组件 | 职责 |
|------|------|
| `AiModelConfig` | 配置流式模型 Bean |
| `VisionNoteStreamService` | 流式笔记生成服务接口 |
| `VisionNoteStreamServiceImpl` | 流式生成实现（订阅 token 流） |
| `NoteAIController` | 提供 SSE 端点 |
| `note.js` (前端) | 流式 API 调用函数 |
| `publish.vue` (前端) | 打字机效果展示 |

---

## 后端配置详解

### AiModelConfig.java - 流式模型配置

```java
@Configuration
public class AiModelConfig {

    // ... 同步模型配置（略）...

    /**
     * DeepSeek 流式聊天模型 Bean
     * 用于商家端的流式 AI 功能
     */
    @Bean
    @Primary
    public StreamingChatLanguageModel deepSeekStreamingChatModel() {
        log.info("初始化 DeepSeek 流式聊天模型: {}", deepSeekModelName);

        return OpenAiStreamingChatModel.builder()
                .apiKey(deepSeekApiKey)
                .baseUrl(deepSeekBaseUrl)
                .modelName(deepSeekModelName)
                .temperature(deepSeekTemperature)
                .maxTokens(deepSeekMaxTokens)
                .timeout(Duration.ofSeconds(deepSeekTimeout))
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    /**
     * 通义千问 Qwen-VL 流式视觉模型 Bean
     * 用于用户端的探店笔记流式生成
     */
    @Bean("visionStreamingChatModel")
    public StreamingChatLanguageModel visionStreamingChatModel() {
        log.info("初始化 通义千问 Qwen-VL 流式视觉模型: {}", qwenVisionModelName);

        return OpenAiStreamingChatModel.builder()
                .apiKey(qwenVisionApiKey)
                .baseUrl(qwenVisionBaseUrl)
                .modelName(qwenVisionModelName)
                .temperature(qwenVisionTemperature)
                .maxTokens(qwenVisionMaxTokens)
                .timeout(Duration.ofSeconds(qwenVisionTimeout))
                .logRequests(true)
                .logResponses(true)
                .build();
    }
}
```

### VisionNoteStreamServiceImpl.java - 流式服务

```java
@Slf4j
@Service
public class VisionNoteStreamServiceImpl implements VisionNoteStreamService {

    private final StreamingChatLanguageModel visionStreamingChatModel;

    public VisionNoteStreamServiceImpl(
            @Qualifier("visionStreamingChatModel") 
            StreamingChatLanguageModel visionStreamingChatModel) {
        this.visionStreamingChatModel = visionStreamingChatModel;
    }

    @Override
    public void generateNoteStream(NoteGenerateRequest request, SseEmitter emitter) {
        // 1. 构建消息
        SystemMessage systemMessage = SystemMessage.from(SYSTEM_PROMPT);
        UserMessage userMessage = buildUserMessage(request);
        List<ChatMessage> messages = List.of(systemMessage, userMessage);

        // 2. 调用流式模型
        visionStreamingChatModel.generate(messages, new StreamingResponseHandler<>() {
            
            @Override
            public void onNext(String token) {
                try {
                    // 每个 token 作为 SSE 事件发送
                    emitter.send(SseEmitter.event()
                            .name("token")
                            .data(Objects.requireNonNull(token)));
                } catch (IOException e) {
                    log.error("SSE 发送失败", e);
                    emitter.completeWithError(e);
                }
            }

            @Override
            public void onComplete(Response<AiMessage> response) {
                try {
                    // 发送完成信号
                    emitter.send(SseEmitter.event()
                            .name("done")
                            .data("[DONE]"));
                    emitter.complete();
                    log.info("流式生成完成");
                } catch (IOException e) {
                    log.error("SSE 完成事件发送失败", e);
                }
            }

            @Override
            public void onError(Throwable error) {
                log.error("流式生成失败", error);
                sendError(emitter, error.getMessage());
                emitter.completeWithError(error);
            }
        });
    }
}
```

### NoteAIController.java - SSE 端点

```java
@Slf4j
@RestController
@RequestMapping("/note")
@RequiredArgsConstructor
public class NoteAIController {

    private final VisionNoteStreamService visionNoteStreamService;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * AI 智能生成探店笔记（SSE 流式版本）
     * 
     * @produces text/event-stream
     */
    @PostMapping(value = "/generate/stream", 
                 produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generateNoteStream(
            @RequestBody @Valid NoteGenerateRequest request) {
        
        log.info("收到 AI 流式生成请求");

        // 创建 SseEmitter，超时时间 3 分钟
        SseEmitter emitter = new SseEmitter(180000L);

        // 异步执行流式生成
        executor.execute(() -> 
            visionNoteStreamService.generateNoteStream(request, emitter));

        // 设置回调
        emitter.onTimeout(() -> {
            log.warn("SSE 连接超时");
            emitter.complete();
        });
        emitter.onCompletion(() -> log.info("SSE 连接关闭"));
        emitter.onError(e -> log.error("SSE 错误: {}", e.getMessage()));

        return emitter;
    }
}
```

---

## 前端接入指南

### API 函数 - 流式请求

```javascript
// api/note.js

/**
 * AI 智能生成探店笔记（流式版本）
 * @param {Object} data - 生成请求
 * @param {Function} onToken - token 回调
 * @param {Function} onComplete - 完成回调
 * @param {Function} onError - 错误回调
 */
export const generateNoteByAIStream = async (
  data, onToken, onComplete, onError) => {
  
  const baseUrl = getBaseUrl()
  const token = uni.getStorageSync('token')
  
  try {
    const response = await fetch(`${baseUrl}/note/generate/stream`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      },
      body: JSON.stringify(data)
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    // 读取流
    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let fullText = ''
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      
      // 解析 SSE 格式
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        if (line.startsWith('data:')) {
          const data = line.substring(5).trim()
          
          if (data === '[DONE]') {
            if (onComplete) onComplete(fullText)
            return
          }
          
          fullText += data
          if (onToken) onToken(data)
        }
      }
    }

    if (onComplete) onComplete(fullText)

  } catch (error) {
    console.error('SSE 流式生成失败:', error)
    if (onError) onError(error)
    throw error
  }
}
```

### 组件使用 - 打字机效果

```javascript
// publish.vue

const handleMagicGenerate = async () => {
  generating.value = true
  title.value = ''
  content.value = ''
  
  let fullText = ''
  let isParsingTitle = true
  
  await generateNoteByAIStream(
    generateRequest,
    
    // onToken: 每个 token 到达
    (token) => {
      fullText += token
      
      // 解析标题和正文（用 --- 分隔）
      if (isParsingTitle && fullText.includes('---')) {
        const parts = fullText.split('---')
        title.value = parts[0].trim()
        content.value = parts.slice(1).join('---').trim()
        isParsingTitle = false
      } else if (isParsingTitle) {
        title.value = fullText.trim()
      } else {
        const parts = fullText.split('---')
        content.value = parts.slice(1).join('---').trim()
      }
    },
    
    // onComplete: 生成完成
    (finalText) => {
      // 最终解析
      if (finalText.includes('---')) {
        const parts = finalText.split('---')
        title.value = parts[0].trim()
        content.value = parts.slice(1).join('---').trim()
      }
      generating.value = false
      uni.showToast({ title: 'AI创作完成！', icon: 'success' })
    },
    
    // onError: 错误处理
    (error) => {
      generating.value = false
      uni.showToast({ title: error.message, icon: 'none' })
    }
  )
}
```

---

## SSE 事件协议

### 事件类型定义

| 事件名 | 数据格式 | 触发时机 | 说明 |
|--------|----------|----------|------|
| `token` | 字符串 | 每个 token 生成时 | AI 生成的内容片段 |
| `done` | `[DONE]` | 全部生成完成 | 客户端应关闭连接 |
| `error` | 错误信息 | 发生异常 | 客户端应显示错误 |

### SSE 数据格式

```
event: token
data: 探

event: token
data: 店

event: token
data: 笔

event: token
data: 记

...

event: done
data: [DONE]
```

### 错误事件

```
event: error
data: AI 生成失败: 模型超时

(连接关闭)
```

---

## 最佳实践

### ✅ 后端推荐

```java
// 1. 设置合理的超时时间
SseEmitter emitter = new SseEmitter(180000L);  // 3分钟

// 2. 异步执行，不阻塞 Tomcat 线程
executor.execute(() -> streamService.generate(request, emitter));

// 3. 完善的回调处理
emitter.onTimeout(() -> emitter.complete());
emitter.onCompletion(() -> log.info("连接关闭"));
emitter.onError(e -> log.error("错误", e));

// 4. 使用 Objects.requireNonNull 处理 null
emitter.send(SseEmitter.event()
        .name("token")
        .data(Objects.requireNonNull(token)));
```

### ✅ 前端推荐

```javascript
// 1. 提供取消功能
const abortController = new AbortController()
fetch(url, { signal: abortController.signal })

// 取消生成
onCancel() {
  abortController.abort()
}

// 2. 显示生成状态
<view v-if="generating" class="generating-indicator">
  <text>AI 正在创作...</text>
</view>

// 3. 优雅的错误处理
onError: (error) => {
  if (error.name === 'AbortError') {
    // 用户主动取消，不显示错误
    return
  }
  uni.showToast({ title: 'AI 生成失败', icon: 'none' })
}
```

### ❌ 避免的做法

```java
// 1. 不要在主线程同步执行
@PostMapping("/generate/stream")
public SseEmitter generate() {
    SseEmitter emitter = new SseEmitter();
    streamService.generate(emitter);  // ❌ 阻塞
    return emitter;
}

// 2. 不要忘记处理超时
SseEmitter emitter = new SseEmitter();  // ❌ 默认 30 秒超时，AI 可能更久
```

---

## 部署配置

### Nginx 反向代理配置

SSE 需要禁用响应缓冲，否则会导致内容一次性发送：

```nginx
location /api/note/generate/stream {
    proxy_pass http://backend;
    
    # SSE 关键配置
    proxy_http_version 1.1;
    proxy_set_header Connection "";
    
    # 禁用缓冲
    proxy_buffering off;
    proxy_cache off;
    
    # 分块传输
    chunked_transfer_encoding on;
    
    # 超时设置（与后端一致）
    proxy_read_timeout 180s;
    proxy_send_timeout 180s;
}

location /api/merchant/reply/generate/stream {
    # 同上配置
}
```

### Spring Boot 配置

```yaml
# application.yml
server:
  # 异步请求超时（毫秒）
  servlet:
    async:
      timeout: 180000  # 3分钟

spring:
  mvc:
    async:
      request-timeout: 180000
```

---

## 常见问题

### 1. 前端收不到流式数据？

```
检查清单：
□ Nginx 是否禁用了 proxy_buffering？
□ CDN 是否启用了响应缓冲？
□ 后端 SseEmitter 超时设置是否足够？
□ Content-Type 是否为 text/event-stream？
```

### 2. 连接频繁超时？

```java
// 增加超时时间
SseEmitter emitter = new SseEmitter(300000L);  // 5分钟

// 检查 AI 模型 timeout 配置
OpenAiStreamingChatModel.builder()
    .timeout(Duration.ofSeconds(120))  // 增加
    .build();
```

### 3. UniApp 小程序不支持 fetch？

```javascript
// 小程序环境使用 uni.request 模拟（不推荐）
// 建议：小程序环境降级使用同步接口

// #ifdef MP
// 小程序使用同步接口
const result = await generateNoteByAI(request)
// #endif

// #ifdef H5
// H5 使用流式接口
await generateNoteByAIStream(request, onToken, onComplete, onError)
// #endif
```

### 4. 如何实现进度百分比？

```javascript
// AI 生成长度不确定，无法精确计算百分比
// 可以使用模糊进度

let charCount = 0
onToken: (token) => {
  charCount += token.length
  // 假设预期长度为 300 字
  const progress = Math.min(charCount / 300 * 100, 95)
  progressBar.value = progress
}

onComplete: () => {
  progressBar.value = 100
}
```

---

## 总结

通过实现 SSE 流式 AI 响应，我们显著提升了用户体验：

| 目标 | 实现效果 |
|------|----------|
| 即时反馈 | 首字 1-2 秒显示 |
| 交互体验 | 打字机效果，仿佛 AI 在"思考" |
| 可控性 | 支持中途取消 |
| 技术实现 | 基于 Spring SseEmitter + LangChain4j |

### 接口清单

| 功能 | 同步接口 | 流式接口 |
|------|----------|----------|
| 用户笔记生成 | `POST /note/generate` | `POST /note/generate/stream` |
| 商家智能回复 | `POST /merchant/reply/generate` | `POST /merchant/reply/generate/stream` |

这套方案使 AI 内容生成的用户体验提升到与主流 AI 产品（如 ChatGPT）同等水平。
