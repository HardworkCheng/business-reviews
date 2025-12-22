# PNG 图标替换指南

## 当前状态
✅ 所有配置已恢复为使用 PNG 图标文件
✅ 你现在可以直接在此目录替换同名 PNG 文件

## 需要准备的 PNG 图标文件

### 底部导航栏图标（必需 - 10个文件）
1. **home.png** - 首页图标（普通状态，灰色 #999999）
2. **home-active.png** - 首页图标（激活状态，橙色 #FF9E64）
3. **map.png** - 地图/优惠图标（普通状态，灰色 #999999）
4. **map-active.png** - 地图/优惠图标（激活状态，橙色 #FF9E64）
5. **add.png** - 发布图标（普通状态，橙色圆形）
6. **add-active.png** - 发布图标（激活状态，更大的橙色圆形）
7. **message.png** - 消息图标（普通状态，灰色 #999999）
8. **message-active.png** - 消息图标（激活状态，橙色 #FF9E64）
9. **profile.png** - 个人资料图标（普通状态，灰色 #999999）
10. **profile-active.png** - 个人资料图标（激活状态，橙色 #FF9E64）

### 页面功能图标（必需 - 2个文件）
11. **arrow-down.png** - 下拉箭头图标（用于城市选择，灰色）
12. **search.png** - 搜索图标（用于搜索框，灰色）

### 分类图标（已存在，可选择替换 - 8个文件）
13. **food.png** - 美食分类图标 ✅
14. **ktv.png** - KTV分类图标 ✅
15. **beauty.png** - 美发分类图标 ✅
16. **nail.png** - 美甲分类图标 ✅
17. **massage.png** - 足疗分类图标 ✅
18. **spa.png** - 美容分类图标 ✅
19. **entertainment.png** - 游乐分类图标 ✅
20. **bar.png** - 酒吧分类图标 ✅

## 图标规格要求
- **尺寸**：48x48 像素或 64x64 像素（推荐 64x64）
- **格式**：PNG 格式，支持透明背景
- **文件大小**：建议每个文件小于 50KB
- **颜色**：
  - 普通状态：#999999（灰色）
  - 激活状态：#FF9E64（橙色）
  - add 图标特殊：始终使用橙色，激活状态更大更粗

## 当前配置文件

### pages.json（底部导航栏配置）
```json
{
  "pagePath": "pages/index/index",
  "iconPath": "static/icons/home.png",
  "selectedIconPath": "static/icons/home-active.png",
  "text": "首页"
}
```

### pages/index/index.vue（首页图标）
```html
<image src="/static/icons/arrow-down.png" class="arrow-icon-img" mode="aspectFit"></image>
<image src="/static/icons/search.png" class="search-icon-img" mode="aspectFit"></image>
```

### 分类图标配置
```javascript
{ name: '美食', icon: '/static/icons/food.png' }
```

## 替换步骤
1. **准备 PNG 图标文件**：确保文件名与上述列表完全一致
2. **复制到目录**：将文件放入 `src/static/icons/` 目录
3. **覆盖同名文件**：直接替换现有文件（如果存在）
4. **重新编译**：重启开发服务器或重新编译项目
5. **测试效果**：检查图标是否正常显示

## 获取图标的方法

### 方法1：在线转换 SVG 到 PNG
- 网站：https://convertio.co/svg-png/
- 上传现有的 SVG 文件（在同目录下）
- 设置输出尺寸为 64x64
- 下载 PNG 文件

### 方法2：使用设计软件
- Photoshop、Figma、Sketch 等
- 创建 64x64 画布
- 设计图标并导出为 PNG

### 方法3：下载免费图标
- 网站：iconfont.cn、iconify.design
- 搜索对应功能的图标
- 下载 PNG 格式

## 注意事项
- ⚠️ 文件名必须完全匹配（区分大小写）
- ⚠️ 必须是真正的 PNG 图片文件，不能是文本文件
- ⚠️ uniapp tabBar 不支持 SVG 格式
- ⚠️ 建议保持透明背景，避免白色背景
- ⚠️ 图标尺寸要统一，避免显示大小不一致

## 完成后的效果
替换完成后，你的应用将显示：
- 底部导航栏：自定义的 PNG 图标
- 首页搜索框：自定义的搜索和箭头图标
- 分类区域：自定义的分类图标