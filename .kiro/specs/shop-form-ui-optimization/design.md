# Design Document: Shop Form UI Optimization

## Overview

本设计文档描述了商家运营中心店铺信息管理页面的表单优化方案，包括：
1. 修复输入框在编辑模式下无法输入的问题
2. 将省份/城市/区县三个输入框合并为一行布局
3. 在地址行右侧添加经纬度显示和定位按钮
4. 将店铺简介缩小为一半宽度，右侧添加商家相册上传功能
5. 在 UniApp 移动端商家详情页展示商家相册

## Architecture

### 前端架构

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    Shop Info Page (list.vue)                             │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │                         基本信息区域                                 │ │
│  │  ┌──────────┐  ┌─────────────────────────────────────────────────┐ │ │
│  │  │  Logo    │  │  店铺名称 | 经营类目                              │ │ │
│  │  │  上传    │  ├─────────────────────┬───────────────────────────┤ │ │
│  │  │          │  │  店铺简介 (50%)     │  商家相册 (50%)            │ │ │
│  │  └──────────┘  │  [textarea]         │  [图片上传组件]            │ │ │
│  │                └─────────────────────┴───────────────────────────┘ │ │
│  │                │  人均消费 | 店铺状态                              │ │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │                      联系方式与地址区域                              │ │
│  │  │  联系电话 | 营业时间                                            │ │
│  │  │  店铺位置 [地址输入] [选择位置按钮]                              │ │
│  │  ├─────────────────────────────────────────────────────────────────┤ │
│  │  │  省份 | 城市 | 区县 | 经纬度 [定位按钮]                         │ │
│  │  │  [一行布局，紧凑显示]                                            │ │
│  └────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│              UniApp Mobile Shop Detail (shop-detail.vue)                 │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │  店铺头图                                                           │ │
│  ├────────────────────────────────────────────────────────────────────┤ │
│  │  店铺基本信息                                                       │ │
│  ├────────────────────────────────────────────────────────────────────┤ │
│  │  商家相册 (新增)                                                    │ │
│  │  [横向滚动/网格布局展示多张图片]                                     │ │
│  │  [点击图片可全屏预览]                                               │ │
│  └────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────┘
```

## Components and Interfaces

### 1. ShopInfoPage 组件修改

#### 编辑模式状态管理
```typescript
interface EditModeState {
  isEditing: boolean;      // 是否处于编辑模式
  enableEdit: () => void;  // 启用编辑模式
  cancelEdit: () => void;  // 取消编辑
  saveChanges: () => void; // 保存修改
}
```

#### 商家相册组件接口
```typescript
interface ShopGallery {
  images: string[];           // 图片URL数组
  maxCount: number;           // 最大图片数量 (建议6-9张)
  onUpload: (file: File) => Promise<string>;  // 上传回调
  onRemove: (index: number) => void;          // 删除回调
  disabled: boolean;          // 是否禁用
}
```

### 2. 地址输入组件布局

#### 地址行布局接口
```typescript
interface AddressRowLayout {
  province: string;    // 省份
  city: string;        // 城市
  district: string;    // 区县
  longitude: string;   // 经度
  latitude: string;    // 纬度
  onLocate: () => void; // 定位按钮点击事件
}
```

### 3. 移动端商家详情页

#### 相册展示接口
```typescript
interface MobileGallery {
  images: string[];           // 图片URL数组
  onImageClick: (index: number) => void;  // 图片点击事件
  previewMode: boolean;       // 是否处于预览模式
  currentPreviewIndex: number; // 当前预览图片索引
}
```

## Data Models

### ShopForm 数据模型更新
```typescript
interface ShopForm {
  id: number | null;
  merchantId: number | null;
  categoryId: number;
  name: string;
  headerImage: string;      // Logo图片URL
  images: string;           // 商家相册图片URLs (逗号分隔或JSON数组)
  description: string;
  phone: string;
  address: string;
  province: string;
  city: string;
  district: string;
  latitude: string;
  longitude: string;
  businessHours: string;
  averagePrice: string;
  rating: number;
  status: number;
}
```

### 相册图片数据格式
```typescript
// images 字段存储格式 (JSON数组字符串)
type GalleryImages = string; // JSON.stringify(['url1', 'url2', 'url3'])

// 解析后的格式
type ParsedGalleryImages = string[];
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Edit Mode Field Enablement
*For any* form field in the Shop_Info_Page, when edit mode is active (isEditing = true), the field's disabled property should be false, allowing user input.
**Validates: Requirements 1.1, 1.2**

### Property 2: Non-Edit Mode Field Disablement
*For any* form field in the Shop_Info_Page, when edit mode is inactive (isEditing = false), the field's disabled property should be true, preventing user input.
**Validates: Requirements 1.3**

### Property 3: Address Fields Manual Input
*For any* valid text input to province, city, or district fields, the form state should update to reflect the new value when in edit mode.
**Validates: Requirements 2.2**

### Property 4: Geocoding Updates All Address Fields
*For any* successful geocoding response containing province, city, and district data, all three corresponding form fields should be updated with the returned values.
**Validates: Requirements 2.3**

### Property 5: Location Button Updates Coordinates
*For any* successful geolocation result, the latitude and longitude form fields should be updated with the obtained coordinates, and the coordinates should be within valid ranges (latitude: -90 to 90, longitude: -180 to 180).
**Validates: Requirements 3.3, 3.4**

### Property 6: Gallery Image Addition
*For any* successful image upload to the Shop_Gallery, the images array in the form data should contain the new image URL, and the array length should increase by 1.
**Validates: Requirements 4.3**

### Property 7: Gallery Image Removal
*For any* image removal action from the Shop_Gallery, the removed image URL should no longer exist in the images array, and the array length should decrease by 1.
**Validates: Requirements 4.4**

### Property 8: Gallery Thumbnail Rendering
*For any* non-empty images array in the Shop_Gallery, the number of rendered thumbnail elements should equal the length of the images array.
**Validates: Requirements 4.5**

### Property 9: Mobile Gallery Data Display
*For any* shop data containing gallery images, the Mobile_Shop_Detail page should render the same number of image elements as the images array length.
**Validates: Requirements 5.1**

## Error Handling

### 图片上传错误处理
```typescript
interface UploadError {
  type: 'FILE_TOO_LARGE' | 'INVALID_FORMAT' | 'NETWORK_ERROR' | 'SERVER_ERROR';
  message: string;
  maxSize?: number;        // 最大文件大小 (bytes)
  allowedFormats?: string[]; // 允许的格式
}
```

错误处理策略：
1. 文件过大：提示用户压缩图片，建议最大 2MB
2. 格式无效：提示支持的格式 (JPG, PNG, GIF, WEBP)
3. 网络错误：显示重试按钮
4. 服务器错误：记录日志，显示友好提示

### 定位错误处理
```typescript
interface LocationError {
  type: 'PERMISSION_DENIED' | 'POSITION_UNAVAILABLE' | 'TIMEOUT';
  message: string;
  fallback: () => void;  // 允许用户手动输入
}
```

## Testing Strategy

### 单元测试
- 测试编辑模式状态切换
- 测试表单字段的 disabled 状态
- 测试图片数组的增删操作
- 测试坐标格式化显示

### 属性测试
使用 Vitest 进行属性测试：

1. **编辑模式字段状态测试**
   - **Feature: shop-form-ui-optimization, Property 1: Edit Mode Field Enablement**
   - 生成随机编辑状态，验证所有字段的 disabled 属性

2. **地址字段输入测试**
   - **Feature: shop-form-ui-optimization, Property 3: Address Fields Manual Input**
   - 生成随机地址文本，验证表单状态更新

3. **相册图片增删测试**
   - **Feature: shop-form-ui-optimization, Property 6: Gallery Image Addition**
   - **Feature: shop-form-ui-optimization, Property 7: Gallery Image Removal**
   - 生成随机图片URL，验证数组操作正确性

### 集成测试
- 测试完整的图片上传流程
- 测试定位按钮获取坐标流程
- 测试移动端相册展示和预览功能
