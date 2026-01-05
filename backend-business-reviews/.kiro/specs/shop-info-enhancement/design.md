# Design Document: Shop Info Enhancement

## Overview

本设计文档描述了商家运营中心店铺信息管理页面的优化方案，包括：
1. 移除店铺图片相册和调试信息区域
2. 添加页面级修改按钮
3. 优化地址输入体验（支持自动填充和手动输入省市区）
4. 根据地址自动计算经纬度
5. 修复首页访问时的登录重定向问题

## Architecture

### 前端架构

```
┌─────────────────────────────────────────────────────────────┐
│                    Vue Router (路由守卫)                      │
│  ┌─────────────────────────────────────────────────────────┐│
│  │  beforeEach: 检查token → 未登录重定向到/login            ││
│  └─────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   Shop Info Page (list.vue)                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Page Header │  │  Basic Info  │  │ Contact &    │      │
│  │  + Edit Btn  │  │  + Logo      │  │ Address      │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│  ┌──────────────┐  ┌──────────────┐                        │
│  │  Operation   │  │  Action      │  [已移除: 店铺图片]     │
│  │  Data        │  │  Buttons     │  [已移除: 调试信息]     │
│  └──────────────┘  └──────────────┘                        │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    高德地图 API 服务                          │
│  ┌─────────────────┐  ┌─────────────────┐                  │
│  │  PlaceSearch    │  │  Geocoder       │                  │
│  │  (地点搜索)      │  │  (地理编码)      │                  │
│  └─────────────────┘  └─────────────────┘                  │
└─────────────────────────────────────────────────────────────┘
```

## Components and Interfaces

### 1. ShopInfoPage 组件修改

#### 页面头部增强
```typescript
interface PageHeader {
  title: string;           // "店铺信息管理"
  description: string;     // 页面描述
  editButton: {
    visible: boolean;      // 是否显示编辑按钮
    onClick: () => void;   // 点击事件处理
  };
}
```

#### 编辑模式状态
```typescript
interface EditModeState {
  isEditing: boolean;      // 是否处于编辑模式
  enableEdit: () => void;  // 启用编辑模式
  cancelEdit: () => void;  // 取消编辑
  saveChanges: () => void; // 保存修改
}
```

### 2. 地址输入组件增强

#### 地址自动填充接口
```typescript
interface AddressAutocomplete {
  address: string;         // 详细地址
  province: string;        // 省份 (可编辑)
  city: string;            // 城市 (可编辑)
  district: string;        // 区县 (可编辑)
  latitude: string;        // 纬度 (自动计算)
  longitude: string;       // 经度 (自动计算)
}

interface GeocodingService {
  geocodeAddress(address: string): Promise<{
    province: string;
    city: string;
    district: string;
    latitude: number;
    longitude: number;
  }>;
}
```

### 3. 路由守卫增强

```typescript
interface RouteGuard {
  beforeEach: (to: Route, from: Route, next: Function) => void;
  checkAuthentication: () => boolean;
  redirectToLogin: () => void;
  redirectToShops: () => void;
}
```

## Data Models

### ShopForm 数据模型
```typescript
interface ShopForm {
  id: number | null;
  merchantId: number | null;
  categoryId: number;
  name: string;
  headerImage: string;      // Logo图片URL
  description: string;
  phone: string;
  address: string;
  province: string;         // 可编辑
  city: string;             // 可编辑
  district: string;         // 可编辑
  latitude: string;         // 自动计算
  longitude: string;        // 自动计算
  businessHours: string;
  averagePrice: string;
  rating: number;
  tasteScore: number;
  environmentScore: number;
  serviceScore: number;
  reviewCount: number;
  popularity: number;
  status: number;
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Address Geocoding Consistency
*For any* valid Chinese address string, when the geocoding service processes it, the returned coordinates should be within valid latitude (-90 to 90) and longitude (-180 to 180) ranges, and the province/city/district should be non-empty strings.
**Validates: Requirements 3.1, 3.4**

### Property 2: Route Guard Authentication Check
*For any* route access attempt without a valid token in localStorage, the router should redirect to the login page path '/login'.
**Validates: Requirements 4.1**

### Property 3: Authenticated User Routing
*For any* route access attempt with a valid token in localStorage to the root path '/', the router should redirect to the shops management page '/shops'.
**Validates: Requirements 4.2**

### Property 4: Form Field Editability
*For any* province, city, or district field after auto-fill, the field should accept manual input and update the form state accordingly.
**Validates: Requirements 3.2, 3.3**

## Error Handling

### 地理编码错误处理
```typescript
interface GeocodingError {
  type: 'NETWORK_ERROR' | 'INVALID_ADDRESS' | 'NO_RESULT' | 'API_ERROR';
  message: string;
  fallback: () => void;  // 允许用户手动输入
}
```

错误处理策略：
1. 网络错误：显示提示，允许用户手动输入经纬度
2. 地址无效：提示用户检查地址格式
3. 无结果：允许用户手动输入省市区和经纬度
4. API错误：记录日志，显示友好提示

### 路由守卫错误处理
1. Token过期：清除本地存储，重定向到登录页
2. 网络错误：显示错误提示，保持当前页面

## Testing Strategy

### 单元测试
- 测试地址解析函数的正确性
- 测试路由守卫的认证逻辑
- 测试表单验证规则

### 属性测试
使用 Vitest 进行属性测试：

1. 地理编码一致性测试
   - 生成随机有效地址
   - 验证返回的坐标在有效范围内

2. 路由守卫测试
   - 测试无token时的重定向行为
   - 测试有token时的路由行为

### 集成测试
- 测试完整的地址输入到经纬度填充流程
- 测试登录重定向流程
- 测试编辑模式的启用和保存流程
