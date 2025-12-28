# 商家入驻注册字段映射文档

## 数据库表结构 (merchants)

```sql
CREATE TABLE `merchants` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商家ID',
  `name` varchar(100) NOT NULL COMMENT '商家名称/企业名称',
  `logo` varchar(500) NULL DEFAULT NULL COMMENT '商家Logo',
  `license_no` varchar(50) NULL DEFAULT NULL COMMENT '营业执照号',
  `license_image` varchar(500) NULL DEFAULT NULL COMMENT '营业执照图片',
  `contact_name` varchar(50) NULL DEFAULT NULL COMMENT '联系人姓名',
  `contact_phone` varchar(20) NOT NULL COMMENT '联系电话',
  `contact_email` varchar(100) NULL DEFAULT NULL COMMENT '联系邮箱',
  `password` varchar(100) NULL DEFAULT NULL COMMENT '登录密码（加密）',
  `avatar` varchar(500) NULL DEFAULT NULL COMMENT '商家头像',
  `role_id` bigint NULL DEFAULT NULL COMMENT '角色ID',
  `last_login_at` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（1正常，2禁用，3待审核）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_contact_phone`(`contact_phone`)
);
```

## 前端表单字段映射

### 前端 → 数据库字段对应

| 前端字段 (registerForm) | 数据库字段 | 类型 | 必填 | 说明 |
|------------------------|-----------|------|------|------|
| `phone` | `contact_phone` | string | ✅ | 联系电话，作为登录账号，唯一索引 |
| `code` | - | string | ✅ | 短信验证码，仅用于注册验证，不存数据库 |
| `password` | `password` | string | ✅ | 登录密码，后端加密存储 |
| `confirmPassword` | - | string | ✅ | 确认密码，前端验证用，不提交 |
| `merchantName` | `name` | string | ✅ | 商家名称/企业名称 |
| `logo` | `logo` | string | ❌ | 商家Logo，图片URL |
| `avatar` | `avatar` | string | ❌ | 商家形象图/头像，图片URL |
| `contactName` | `contact_name` | string | ✅ | 联系人姓名 |
| `contactEmail` | `contact_email` | string | ❌ | 联系邮箱，接收重要通知 |
| `licenseNo` | `license_no` | string | ❌ | 营业执照号/统一社会信用代码 |
| `licenseImage` | `license_image` | string | ❌ | 营业执照图片URL |

### 后端自动处理字段

| 数据库字段 | 默认值/处理逻辑 | 说明 |
|-----------|----------------|------|
| `id` | AUTO_INCREMENT | 主键，数据库自动生成 |
| `role_id` | 默认商家角色ID | 后端设置默认商家角色 |
| `status` | 1 (正常) 或 3 (待审核) | 根据业务逻辑设置初始状态 |
| `created_at` | CURRENT_TIMESTAMP | 数据库自动生成 |
| `updated_at` | CURRENT_TIMESTAMP | 数据库自动更新 |
| `last_login_at` | NULL | 首次登录时更新 |

## API 接口

### 注册接口

**URL:** `POST /merchant/auth/register`

**请求体 (TypeScript 接口):**

```typescript
export interface MerchantRegisterData {
  // 账号信息
  phone: string              // contact_phone - 联系电话（必填）
  code: string               // 验证码（必填，不存数据库）
  password: string           // password - 登录密码（必填）
  
  // 商家基本信息
  merchantName: string       // name - 商家名称（必填）
  logo?: string              // logo - 商家Logo（选填）
  avatar?: string            // avatar - 商家头像（选填）
  
  // 联系人信息
  contactName: string        // contact_name - 联系人姓名（必填）
  contactEmail?: string      // contact_email - 联系邮箱（选填）
  
  // 营业资质
  licenseNo?: string         // license_no - 营业执照号（选填）
  licenseImage?: string      // license_image - 营业执照图片（选填）
}
```

**后端需要处理:**

1. 验证短信验证码 `code` 是否正确
2. 检查手机号 `phone` 是否已注册（唯一性）
3. 密码 `password` 加密存储
4. 将前端字段映射到数据库字段：
   - `phone` → `contact_phone`
   - `merchantName` → `name`
   - `contactName` → `contact_name`
   - `contactEmail` → `contact_email`
   - `licenseNo` → `license_no`
   - `licenseImage` → `license_image`
5. 设置默认值：
   - `role_id` - 商家角色ID
   - `status` - 1 (正常) 或 3 (待审核)
6. 返回 token 和用户信息

## 表单验证规则

### 前端验证

```typescript
const registerRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码为6位数字', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validatePasswordMatch, trigger: 'blur' }
  ],
  merchantName: [
    { required: true, message: '请输入商家名称', trigger: 'blur' }
  ],
  contactName: [
    { required: true, message: '请输入联系人姓名', trigger: 'blur' }
  ],
  contactEmail: [
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ]
}
```

## UI 设计规范

### 表单布局

- **双列布局**: 左侧账号信息，右侧商家资质
- **分组标题**: 使用 Tesla 风格的分组标题区分不同类别
- **上传组件**: 
  - Logo/形象图: 80x80px 小型上传框
  - 营业执照: 横向矩形上传框
  - 提示文本: 灰色小字说明推荐尺寸

### 颜色规范

- **主色调**: Tesla Blue `#3e6ae1`
- **文字颜色**: 
  - 主标题: `#171a20`
  - 说明文字: `#5c5e62`
  - 提示文字: `#8e8e8e`
- **边框**: `#dcdfe6`

### 图片上传建议尺寸

- **商家Logo**: 正方形，200x200px
- **形象图/头像**: 正方形，200x200px  
- **营业执照**: 横版，建议 750x450px

## 后端接口建议

### 注册流程

1. **验证手机号**: 检查是否已注册
2. **验证验证码**: 校验短信验证码
3. **字段映射**: 前端字段 → 数据库字段
4. **密码加密**: 使用 bcrypt 或类似算法
5. **创建商家**: 插入 merchants 表
6. **分配角色**: 设置 role_id
7. **生成 token**: 返回 JWT token
8. **返回用户信息**: 包含必要的商家信息

### 返回数据示例

```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userInfo": {
      "userId": "1",
      "merchantId": "1",
      "merchantName": "示例商家",
      "merchantLogo": "https://xxx.com/logo.png",
      "name": "张三",
      "phone": "13800138000",
      "avatar": "https://xxx.com/avatar.png",
      "contactEmail": "example@merchant.com",
      "roleName": "商家",
      "permissions": ["shop:manage", "note:publish", ...]
    }
  }
}
```

## 注意事项

1. **唯一性约束**: `contact_phone` 必须唯一
2. **密码安全**: 必须加密存储，不能明文
3. **图片上传**: 先上传图片获取URL，再提交表单
4. **验证码**: 注册后应立即失效该验证码
5. **状态管理**: 根据业务需要设置初始状态（正常/待审核）
6. **角色权限**: 注册时分配商家角色和对应权限

## 更新历史

- 2025-12-28: 初始版本，完整对齐数据库表结构
