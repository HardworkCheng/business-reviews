# 前端性能与架构优化清单 (Frontend Optimization Plan)

经过对**移动端 (`front-business-reviews-Mobile`)** 和 **Web端 (`front-business-reviews-Web`)** 项目的深度审计，我制定了以下全方位的优化清单。

这不仅是代码层面的重构，更是对**工程化、体验和可维护性**的全面升级。

---

## 📱 移动端 (UniApp) 核心优化

### 1. 架构解耦：拆分 "巨石组件" (P0)
**现状**: `publish.vue` 文件长度超过 1400 行，混合了 UI、表单逻辑、图片上传、AI 生成、地图定位等所有逻辑。
- **问题**: 极难维护，修改任何一个小功能都需要在 1000 多行代码中跳转。AI 生成逻辑和 UI 强耦合，无法复用。
**优化方案**:
- **提取 Composables (Hooks)**:
  - `useImageUpload.js`: 封装图片选择、压缩、上传 OSS 逻辑。
  - `useAiNote.js`: 封装 AI 轮询、文案生成状态机逻辑。
  - `useLocation.js`: 封装定位权限申请、地图选点回调逻辑。
- **组件拆分**:
  - `<TopicSelector />`: 独立话题选择弹窗和逻辑。
  - `<ShopSelector />`: 独立商户搜索和列表组件。

### 2. 网络层重构 (P1)
**现状**: `request.js` 中包含大量 UI 逻辑（`uni.showToast`, `uni.reLaunch`），并且手动拼接 URL 参数。
- **问题**: 
  - 无法在后台静默请求（比如在此页面时预加载下一页数据），因为一旦出错就会强制弹窗。
  - 缺乏统一的拦截器模式，扩展性差。
**优化方案**:
- **分离 UI 与 网络**: `request` 函数只负责数据传输。错误处理应通过 `Promise.reject` 抛出，由业务层决定是弹窗还是静默处理（或提供配置项 `showError: false`）。
- **增强拦截器**: 实现 request/response 拦截器模式，统一处理 token 注入和 401 过期刷新。

### 3. CSS 架构治理 (P2)
**现状**: 页面中使用大量 `scss` 嵌套，未充分利用全局变量。
**优化方案**:
- 建立 `uni.scss` 变量库，统一定义主题色（如 `#ffca28`）、圆角、字号。
- 引入原子化 CSS类 (或使用 UnoCSS/Tailwind)，减少手写 `flex`, `margin` 等重复样式。

---

## 💻 Web端 (Vue Admin) 核心优化

### 4. 工程化构建优化 (P0)
**现状**: `vite.config.ts` 配置非常基础，缺少现代 Vue 开发的标准插件。
- **问题**: 
  - 手动引入 Vue API (`ref`, `computed`) 和组件，开发效率低且代码冗余。
  - build 只有 `tsc` 检查，缺乏压缩和分包策略。
**优化方案**:
- **引入 `unplugin-auto-import`**: 自动导入 Vue, Vue Router, Pinia API。
- **引入 `unplugin-vue-components`**: 自动按需导入 Element Plus 组件（支持 Tree-shaking，减小包体积）。
- **Gzip 压缩**: 引入 `vite-plugin-compression`，对静态资源进行 Gzip 压缩，显著提升加载速度。

### 5. 路由与权限控制增强 (P1)
**现状**: 
- `request.ts` 使用 `window.location.href = '/login'` 进行跳转，这会导致整个页面重载，破坏 SPA 体验。
- `router/index.ts` 中手写了 JWT 格式校验 (`split('.')`)，这很脆弱。
**优化方案**:
- **使用 Router 实例跳转**: 在 interceptor 中引入 router 实例，使用 `router.push('/login')`。
- **动态路由**: 敏感路由（如 `ShopEdit`）应根据用户信息动态挂载，而不是写死在 `routes` 数组里然后用 `beforeEach` 拦截。

### 6. 类型安全 (TypeScript) (P2)
**现状**: 项目中存在大量 `any` 类型（如 `const RETRY_CONFIG` 中的 `error: any`）。
**优化方案**:
- 定义全局 API 响应接口 `interface ApiResponse<T> { code: number; data: T; msg: string }`。
- 对核心业务实体（如 `Shop`, `Note`）定义完善的 Interface，杜绝 `any` 满天飞。

---

## 🎨 通用体验与 UI 优化

### 7. 图片与静态资源加载 (P1)
**现状**: 移动端商家列表直接渲染图片，Web端也没有明显的懒加载策略。
**优化方案**:
- **OSS 处理**: 利用阿里云 OSS 的 URL 参数（`?x-oss-process=image/resize,w_200`），在列表页只请求缩略图，在详情页请求大图。**这是节省流量和提升速度最有效的手段**。
- **骨架屏 (Skeleton)**: 在数据加载期间展示灰色骨架，而不是白色空屏或简单的 Loading 转圈。

### 8. AI 交互体验升级 (P2)
**现状**: 移动端使用"轮询 + 假文案" (`setInterval`) 来模拟 AI 进度，不够真实。
**优化方案**:
- **接入后端 SSE**: 配合后端 P3 级优化，使用 EventSource 接收真实的 AI 思考过程（"正在搜索商户...", "正在根据图片色调生成形容词..."），让用户感觉更智能。

### 9. 异常监控 (P3)
**现状**: 前端报错（JS Error 或 API 错误）只在控制台打印 (`console.error`)。
**优化方案**:
- 生产环境无法看到用户控制台。建议接入轻量级监控（如 Sentry 或自建简单的日志上报接口），当发生未捕获异常时，自动将堆栈信息发送给后端。

---

## 📋 执行路线图 (Roadmap)

1.  **本周 (Quick Wins)**:
    - Web端: 配置 `vite` 自动导入插件，优化 `request.ts` 跳转逻辑。
    - Mobile端: 为列表页图片添加 OSS 缩略图参数。
2.  **下周 (Refactoring)**:
    - Mobile端: 拆分 `publish.vue`，提取图片上传 Hook。
    - Web端: 补全 TS 类型定义。
3.  **长期 (Architecture)**:
    - 引入 SSE 实现流式 AI 体验。
    - 建立前端日志监控体系。
