# 需要的 PNG 图标列表

## 问题说明
uniapp 的 tabBar 不支持 SVG 格式图标，只支持 PNG 和 JPG 格式。因此需要将以下 SVG 图标转换为 PNG 格式。

## 需要创建的 PNG 图标

### 底部导航栏图标（必需）
1. **home.png** - 首页图标（普通状态，灰色 #999999）
2. **home-active.png** - 首页图标（激活状态，橙色 #FF9E64）
3. **map.png** - 地图图标（普通状态，灰色 #999999）
4. **map-active.png** - 地图图标（激活状态，橙色 #FF9E64）
5. **add.png** - 发布图标（普通状态，橙色圆形）
6. **add-active.png** - 发布图标（激活状态，更大的橙色圆形）
7. **message.png** - 消息图标（普通状态，灰色 #999999）
8. **message-active.png** - 消息图标（激活状态，橙色 #FF9E64）
9. **profile.png** - 个人资料图标（普通状态，灰色 #999999）
10. **profile-active.png** - 个人资料图标（激活状态，橙色 #FF9E64）

### 页面功能图标（建议）
11. **arrow-down.png** - 下拉箭头图标（用于城市选择）
12. **search.png** - 搜索图标（用于搜索框）

## 图标规格要求
- **尺寸**：建议 48x48 像素或 64x64 像素
- **格式**：PNG 格式，支持透明背景
- **颜色**：
  - 普通状态：#999999（灰色）
  - 激活状态：#FF9E64（橙色）
  - add 图标特殊：始终使用橙色，激活状态更大更粗

## 当前状态
- ✅ 已更新 pages.json 配置文件
- ✅ 已更新页面中的图标引用路径
- ❌ 需要你手动创建上述 PNG 图标文件

## 操作建议
1. 使用设计软件（如 Photoshop、Figma、Sketch）创建这些图标
2. 或者使用在线 SVG 转 PNG 工具转换现有的 SVG 文件
3. 将创建好的 PNG 文件放入 `src/static/icons/` 目录
4. 确保文件名与上述列表完全一致

## 参考现有 SVG 文件
你可以参考 `src/static/icons/` 目录下的 SVG 文件来创建对应的 PNG 版本：
- home.svg → home.png
- home-active.svg → home-active.png
- 等等...