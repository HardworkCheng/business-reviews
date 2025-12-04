# TabBar 图标说明

## 📁 图标位置
所有 TabBar 图标位于：`static/tabbar/` 目录

## 🎨 图标设计

本项目的 TabBar 图标参考了 **dianping 项目**中使用的 **FontAwesome 图标**设计，全部为 SVG 格式。

### 图标列表

| 功能 | 图标名称 | 参考来源 | 颜色 |
|------|---------|---------|------|
| 首页 | home.svg / home-active.svg | FontAwesome fa-home | 灰色/主色 |
| 地图 | map.svg / map-active.svg | FontAwesome fa-map | 灰色/主色 |
| 发布 | add.svg / add-active.svg | FontAwesome fa-plus | 主色 |
| 消息 | message.svg / message-active.svg | FontAwesome fa-comment | 灰色/主色 |
| 我的 | profile.svg / profile-active.svg | FontAwesome fa-user | 灰色/主色 |

### 图标详情

#### 1. 🏠 首页图标（Home）
- **设计**：简洁的房屋轮廓
- **普通状态**：灰色填充 (#999999)
- **选中状态**：主色填充 (#FF9E64)
- **文件**：`home.svg`, `home-active.svg`

#### 2. 🗺️ 地图图标（Map）
- **设计**：地图折叠效果
- **普通状态**：灰色填充 (#999999)
- **选中状态**：主色填充 (#FF9E64)
- **文件**：`map.svg`, `map-active.svg`

#### 3. ➕ 发布图标（Add）
- **设计**：圆形背景 + 加号，突出显示
- **普通状态**：主色圆形 + 白色加号
- **选中状态**：更大的主色圆形 + 白色加号
- **文件**：`add.svg`, `add-active.svg`
- **特点**：始终为主色调，突出发布功能

#### 4. 💬 消息图标（Message）
- **设计**：聊天气泡带三个小圆点
- **普通状态**：灰色填充 (#999999)
- **选中状态**：主色填充 (#FF9E64)
- **文件**：`message.svg`, `message-active.svg`

#### 5. 👤 我的图标（Profile）
- **设计**：用户头像剪影
- **普通状态**：灰色填充 (#999999)
- **选中状态**：主色填充 (#FF9E64)
- **文件**：`profile.svg`, `profile-active.svg`

## 🎨 颜色规范

### 普通状态
- 颜色：`#999999` (中灰色)
- 用途：未选中的 TabBar 图标

### 选中状态
- 颜色：`#FF9E64` (粘土橙 - 主色)
- 用途：当前激活的 TabBar 图标

### 特殊说明
- **发布按钮**：始终使用主色调 (#FF9E64)，以突出其重要性
- 发布按钮的选中状态会显得更大更醒目

## 📐 图标尺寸

- SVG 尺寸：48x48px
- 实际显示：由 uni-app 自动调整
- 建议：保持 SVG 的视口为 48x48，确保跨平台一致性

## 🔄 如何替换图标

### 方案 1：替换 SVG 文件
1. 准备新的 SVG 图标（48x48px）
2. 确保包含普通版和选中版（颜色不同）
3. 替换 `static/tabbar/` 目录下的对应文件
4. 保持文件名不变

### 方案 2：转换为 PNG 格式
如果 HBuilderX 不支持 SVG，可以转换为 PNG：

1. 使用在线工具转换 SVG 为 PNG：
   - https://cloudconvert.com/svg-to-png
   - https://www.aconvert.com/cn/image/svg-to-png/

2. 推荐尺寸：
   - @1x: 40x40px
   - @2x: 80x80px (推荐)
   - @3x: 120x120px

3. 修改 `pages.json` 中的图标路径：
   ```json
   "iconPath": "static/tabbar/home.png",
   "selectedIconPath": "static/tabbar/home-active.png"
   ```

### 方案 3：使用 Iconfont
1. 到 iconfont.cn 下载图标
2. 转换为 PNG 或继续使用 SVG
3. 替换项目中的图标文件

## 📝 配置说明

图标在 `pages.json` 中配置：

```json
"tabBar": {
  "color": "#999999",
  "selectedColor": "#FF9E64",
  "backgroundColor": "#ffffff",
  "borderStyle": "black",
  "list": [
    {
      "pagePath": "pages/index/index",
      "iconPath": "static/tabbar/home.svg",
      "selectedIconPath": "static/tabbar/home-active.svg",
      "text": "首页"
    }
    // ... 其他配置
  ]
}
```

## ⚠️ 注意事项

1. **文件格式**：
   - SVG：矢量图，无损缩放，文件小
   - PNG：位图，需要准备不同分辨率
   - 推荐使用 SVG，如不支持则使用 PNG

2. **命名规范**：
   - 普通状态：`功能名.svg` 或 `功能名.png`
   - 选中状态：`功能名-active.svg` 或 `功能名-active.png`

3. **颜色一致性**：
   - 确保选中状态使用项目主色 #FF9E64
   - 普通状态使用中灰色 #999999

4. **平台兼容性**：
   - H5：完全支持 SVG
   - 微信小程序：支持 SVG（基础库 2.3.0+）
   - App：建议使用 PNG 获得最佳兼容性

## 🚀 最佳实践

1. **保持一致性**：所有图标风格应保持一致
2. **适当留白**：确保图标在小尺寸下清晰可见
3. **测试显示**：在不同设备上测试图标显示效果
4. **优化大小**：压缩 SVG/PNG 文件以提升性能

## 📚 参考资源

- FontAwesome 官网：https://fontawesome.com/
- Iconfont：https://www.iconfont.cn/
- SVG 优化工具：https://jakearchibald.github.io/svgomg/
- PNG 压缩工具：https://tinypng.com/

---

**图标设计完成！现在您的 TabBar 拥有完整且美观的图标系统！** 🎉
