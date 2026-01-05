# Uniapp 前端项目深度优化指南

基于对 `front-business-reviews-Mobile` 项目的源码分析，以下是针对性的优化建议与改进方案。

## 1. 🛡️ 安全与配置管理 (最高优先级)

### 1.1 移除硬编码密钥
在 `src/pages/index/index.vue` 中发现硬编码的高德地图 API Key：
```javascript
const key = '1521141ae4ee08e1a0e37b59d2fd2438' // 🚫 危险！
```
**改进方案**：
*   **短期**：将其提取到 `src/common/config.js` 或 `src/manifest.json` 的自定义字段中。
*   **长期**：**强烈建议**将涉及密钥的 API 调用（如 IP 定位、逆地理编码）迁移到后端 Java 服务。前端只负责传递经纬度给后端，由后端调用高德 API。这样可以彻底隐藏 Key，防止被恶意盗用导致配额耗尽。

### 1.2 环境变量管理
目前 API 地址硬编码在 `request.js` 中。
**改进方案**：
建立 `.env` 体系：
```bash
# .env.development
VITE_API_BASE_URL=http://localhost:8080/api

# .env.production
VITE_API_BASE_URL=https://api.yourdomain.com/api
```
并在代码中使用 `import.meta.env.VITE_API_BASE_URL`。

---

## 2. 🧩 组件化与代码复用

### 2.1 提取公共组件
分析发现 `shop-detail.vue` 和 `index.vue` 中存在大量可复用的 UI 逻辑：
*   **评分组件 (`Rate`)**: `shop-detail.vue` 中手动通过 `★` 字符拼接实现评分展示。建议封装为 `<StartRate :score="4.5" />` 组件，支持半星显示。
*   **笔记卡片 (`NoteCard`)**: `index.vue` 中的瀑布流卡片逻辑复杂，建议提取为独立的组件，方便在"我的收藏"、"用户主页"等页面复用。

### 2.2 统一工具函数
*   **时间格式化**: `index.vue` (`formatTime`) 和 `shop-detail.vue` (`formatReviewDate`) 各自实现了一套类似的逻辑。
    **建议**：引入 `dayjs` (轻量级) 或在 `src/utils/date.js` 中统一封装 `relativeTime` 函数。
*   **图片处理**: 图片加载失败的处理 (`handleImageError`) 和 URL 拼接逻辑散落在各处。
    **建议**：封装全局指令 `v-img-fallback` 或组件 `<AppImage src="..." />`，统一处理默认图和 OSS 缩略图参数。

---

## 3. 💾 状态管理与数据流

### 3.1 引入 Pinia
目前通过 `uni.getStorageSync('token')` 和 `uni.getStorageSync('userInfo')` 在各个页面频繁读取本地存储。
**缺点**：IO 操作慢，且无法响应式更新（例如修改头像后，首页不会自动刷新）。
**改进方案**：
1.  安装 Pinia。
2.  创建 `useUserStore`，在内存中维护用户信息。
3.  仅在 Store 初始化和更新时读写 Storage。

### 3.2 页面通信优化
`index.vue` 使用 `onShow` 强制刷新数据 (`loadData`) 来确保存换城市或登录状态变化后数据同步。
**缺点**：造成不必要的 API 请求，用户体验有"闪烁"感。
**改进**：配合 Pinia，仅在 `city` 或 `user` 发生特定变化时触发刷新，或者使用 `uni.$on` / `uni.$emit` 进行跨页面事件通知。

---

## 4. ⚡ 性能体验优化

### 4.1 图片加载策略
*   **现状**：直接加载原图。
*   **优化**：
    *   **CDN 裁剪**：利用阿里云 OSS 的能力，列表页添加后缀 `?x-oss-process=image/resize,w_300`，详情页使用 `w_800`。
    *   **懒加载**：对于长列表（如笔记流、评论流），确保 `<image>` 标签开启 `lazy-load` 属性。

### 4.2 骨架屏 (Skeleton)
目前数据加载时处于空白或 loading 弹窗状态。
**建议**：使用 `uv-ui` 或手动编写 CSS 骨架屏，在 API 返回前展示灰色的卡片轮廓，提升感官速度。

### 4.3 长列表优化
`shop-detail.vue` 中的评论列表如果在数据量大时（>100条）可能会导致卡顿。
**建议**：
*   分页加载逻辑已存在，很好。
*   如果列表极长，考虑引入 `z-paging` 等虚拟滚动组件，仅渲染可视区域内的 dom 节点。

---

## 5. 🎨 UI/UX 细节打磨

### 5.1 移除无效资源
`via.placeholder.com` 是国外服务，国内访问极慢甚至超时。
**必须修改**：将所有 `via.placeholder.com` 的引用替换为本地静态图片 `/static/images/default-avatar.png` 或 `/static/images/default-cover.png`。

### 5.2 交互反馈
*   **点击态**: 按钮和卡片添加 `:active` 伪类样式（如透明度变化或缩放），让 App 更有"原生感"。
*   **Loading 细节**: 这里的 AI 生成回复、加载更多等操作，建议使用统一的 Loading 动画组件，而不是简单的 `uni.showLoading()`（它会阻断用户操作）。

---

## 6. 📝 总结
这个 Uniapp 项目基础结构良好，采用了 Vue 3 setup 语法，代码逻辑清晰。
**当前最大的风险点**是 **API Key 泄露** 和 **硬编码**。
**最大的提升点**是 **组件化封装** 和 **引入 Pinia 状态管理**。

按此指南优化后，项目的健壮性、维护性和用户体验都将提升一个档次。
