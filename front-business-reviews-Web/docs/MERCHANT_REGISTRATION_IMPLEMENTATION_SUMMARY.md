# 商家入驻功能实现总结文档

## 1. 概览
根据需求，我们完成了商家入驻功能的全面升级，包括前端页面的字段扩展、UI与交互优化，以及后端接口的完整实现、数据库字段扩展和安全性增强。

**UI 升级亮点**:
- 显著增大的上传区域，提升操作体验。
- 采用 Tesla 风格的蓝色按钮和极简设计。
- 移除了冗余的提示文字，界面更清爽。
- **一致性优化**: 所有上传区域（Logo、头像、营业执照）的高度统一为 `140px`。

## 2. 前端实现 (Vue.js + Element Plus)

### 2.1 页面更新 (`src/views/login/index.vue`)
- **字段变更**:
  - 新增 `merchantOwnerName` (商家姓名)，移除 `contactName`。
  - 标签更新: "形象图" -> "商家头像"，"执照号" -> "营业执照号"。
  
- **UI/UX 优化**:
  - **弹窗尺寸**: `1100px`。
  - **上传组件**: 
    - 统一高度: Logo、头像、营业执照均为 **`140px`**。
    - 图标尺寸增加至 `32px`。
  - **按钮风格**: 
    - 入驻按钮统一使用 Tesla Blue (`#3e6ae1`)。
    - 悬停效果与全站风格保持一致。

### 2.2 API 更新 (`src/api/auth.ts`)
- `MerchantRegisterData` 接口已同步更新。

## 3. 后端实现 (Spring Boot)

### 3.1 实体与 DTO 更新
- `MerchantDO` & `MerchantRegisterDTO`: 使用 `merchantOwnerName`，移除 `contactName`。

### 3.2 核心服务 (`MerchantAuthServiceImpl`)
- 完整的注册流程，包含密码加密 (`PasswordUtil`) 和自动创建关联数据（UniApp账号、默认门店）。

## 4. 数据库变更

### 4.1 迁移脚本
- 添加字段: `sql/migrations/add_merchant_owner_name.sql`
- 删除字段: `sql/migrations/remove_contact_name.sql`

## 5. 待执行操作提醒
请务必在数据库中执行上述 SQL 语句。
