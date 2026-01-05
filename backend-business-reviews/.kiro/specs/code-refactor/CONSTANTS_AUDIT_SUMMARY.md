# 后端代码常量审计总结

## 🎯 审计目标

全面检查后端代码中的常量定义，发现并消除重复，提高代码质量。

---

## ✅ 已完成的优化

### 1. 创建FileUploadConstants.java ✅
**文件**: `backend-business-reviews-common/src/main/java/com/businessreviews/constants/FileUploadConstants.java`

**内容**:
- `ALLOWED_IMAGE_EXTENSIONS` - 允许的图片扩展名
- `MAX_FILE_SIZE` - 最大文件大小（10MB）
- `MAX_FILE_SIZE_DESC` - 文件大小描述
- `SIZE_1KB`, `SIZE_1MB`, `SIZE_1GB` - 文件大小单位

### 2. 更新RedisKeyConstants.java ✅
**新增常量**:
- `MERCHANT_SMS_CODE` - 商家短信验证码key
- `MERCHANT_SMS_LIMIT` - 商家短信频率限制key

### 3. 更新SmsCodeConstants.java ✅
**新增常量**:
- `CODE_LENGTH` - 验证码长度（6位）
- `SEND_INTERVAL_SECONDS` - 发送间隔（60秒）

---

## 📋 待重构的文件清单

### 高优先级（建议立即重构）

#### 1. MerchantAuthServiceImpl.java
**位置**: `backend-business-reviews-service/src/main/java/com/businessreviews/service/impl/merchant/MerchantAuthServiceImpl.java`

**需要移除的常量**:
```java
private static final String MERCHANT_SMS_CODE_PREFIX = "merchant:sms:code:";
private static final String MERCHANT_SMS_LIMIT_PREFIX = "merchant:sms:limit:";
```

**替换为**:
```java
import com.businessreviews.constants.RedisKeyConstants;

// 使用
RedisKeyConstants.MERCHANT_SMS_CODE
RedisKeyConstants.MERCHANT_SMS_LIMIT
```

**影响的方法**:
- `sendCode(String phone)`
- `loginByCode(String phone, String code)`
- `register(MerchantRegisterDTO request)`

---

#### 2. SmsManager.java
**位置**: `backend-business-reviews-manager/src/main/java/com/businessreviews/manager/SmsManager.java`

**需要移除的常量**:
```java
private static final int CODE_LENGTH = 6;
private static final long CODE_EXPIRE_SECONDS = 300;
private static final long SEND_INTERVAL_SECONDS = 60;
private static final String SMS_CODE_PREFIX = "sms:code:";
private static final String SMS_LIMIT_PREFIX = "sms:limit:";
```

**替换为**:
```java
import com.businessreviews.constants.RedisKeyConstants;
import com.businessreviews.constants.SmsCodeConstants;

// 使用
SmsCodeConstants.CODE_LENGTH
SmsCodeConstants.EXPIRE_TIME  // 替代CODE_EXPIRE_SECONDS
SmsCodeConstants.SEND_INTERVAL_SECONDS
RedisKeyConstants.SMS_CODE  // 替代SMS_CODE_PREFIX
RedisKeyConstants.SMS_LIMIT  // 替代SMS_LIMIT_PREFIX
```

**影响的方法**:
- `sendCode(String phone)`
- `verifyCode(String phone, String code)`
- `getCachedCode(String phone)`
- `removeCode(String phone)`
- `generateCode()`

---

### 中优先级（建议尽快重构）

#### 3. OssServiceImpl.java
**位置**: `backend-business-reviews-service/src/main/java/com/businessreviews/service/impl/common/OssServiceImpl.java`

**需要移除的常量**:
```java
private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp"};
private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
```

**替换为**:
```java
import com.businessreviews.constants.FileUploadConstants;

// 使用
FileUploadConstants.ALLOWED_IMAGE_EXTENSIONS
FileUploadConstants.MAX_FILE_SIZE
FileUploadConstants.MAX_FILE_SIZE_DESC
```

**影响的方法**:
- `uploadFile(MultipartFile file, String folder)`

---

#### 4. OssManager.java
**位置**: `backend-business-reviews-manager/src/main/java/com/businessreviews/manager/OssManager.java`

**需要移除的常量**:
```java
private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp"};
private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
```

**替换为**:
```java
import com.businessreviews.constants.FileUploadConstants;

// 使用
FileUploadConstants.ALLOWED_IMAGE_EXTENSIONS
FileUploadConstants.MAX_FILE_SIZE
FileUploadConstants.MAX_FILE_SIZE_DESC
```

**影响的方法**:
- `uploadFile(MultipartFile file, String folder)`

---

## 📊 重复常量统计

| 常量名称 | 重复次数 | 位置 | 状态 |
|---------|---------|------|------|
| SMS_CODE_PREFIX | 2次 | SmsManager, RedisKeyConstants | ⏳ 待重构 |
| SMS_LIMIT_PREFIX | 2次 | SmsManager, RedisKeyConstants | ⏳ 待重构 |
| MERCHANT_SMS_CODE | 2次 | MerchantAuthServiceImpl, RedisKeyConstants | ⏳ 待重构 |
| MERCHANT_SMS_LIMIT | 2次 | MerchantAuthServiceImpl, RedisKeyConstants | ⏳ 待重构 |
| ALLOWED_EXTENSIONS | 2次 | OssServiceImpl, OssManager | ⏳ 待重构 |
| MAX_FILE_SIZE | 2次 | OssServiceImpl, OssManager | ⏳ 待重构 |
| CODE_LENGTH | 1次 | SmsManager | ⏳ 待提取 |
| CODE_EXPIRE_SECONDS | 1次 | SmsManager | ⏳ 待提取 |
| SEND_INTERVAL_SECONDS | 1次 | SmsManager | ⏳ 待提取 |

**总计**: 9个常量需要重构，涉及4个文件

---

## 🎯 重构优先级建议

### 第一批（立即执行）
1. ✅ 创建 `FileUploadConstants.java`
2. ✅ 更新 `RedisKeyConstants.java`
3. ✅ 更新 `SmsCodeConstants.java`

### 第二批（本周完成）
4. ⏳ 重构 `MerchantAuthServiceImpl.java`
5. ⏳ 重构 `SmsManager.java`

### 第三批（下周完成）
6. ⏳ 重构 `OssServiceImpl.java`
7. ⏳ 重构 `OssManager.java`

---

## 📈 预期收益

### 代码质量
- ✅ 消除重复代码约50行
- ✅ 统一常量管理
- ✅ 提高代码可读性

### 维护成本
- ✅ 修改常量只需一处
- ✅ 降低出错风险
- ✅ 新人更容易理解

### 团队协作
- ✅ 统一编码规范
- ✅ 减少代码审查时间
- ✅ 提高开发效率

---

## ⚠️ 重构注意事项

### 1. 测试验证
每次重构后必须:
- ✅ 运行单元测试
- ✅ 验证相关功能
- ✅ 检查编译错误

### 2. 向后兼容
- ✅ 不改变API接口
- ✅ 不改变业务逻辑
- ✅ 只优化代码组织

### 3. 分批实施
- ✅ 不要一次性修改所有文件
- ✅ 每批修改后测试验证
- ✅ 确保系统稳定运行

---

## 🚀 快速重构指南

### 步骤1: 更新import语句
```java
// 添加
import com.businessreviews.constants.RedisKeyConstants;
import com.businessreviews.constants.SmsCodeConstants;
import com.businessreviews.constants.FileUploadConstants;
```

### 步骤2: 移除重复常量
```java
// 删除
private static final String SMS_CODE_PREFIX = "sms:code:";
private static final String SMS_LIMIT_PREFIX = "sms:limit:";
// ... 其他重复常量
```

### 步骤3: 替换使用处
```java
// 修改前
String key = SMS_CODE_PREFIX + phone;

// 修改后
String key = RedisKeyConstants.SMS_CODE + phone;
```

### 步骤4: 编译测试
```bash
mvn clean compile
mvn test
```

---

## 📝 重构检查清单

### MerchantAuthServiceImpl.java
- [ ] 移除 `MERCHANT_SMS_CODE_PREFIX`
- [ ] 移除 `MERCHANT_SMS_LIMIT_PREFIX`
- [ ] 添加 `RedisKeyConstants` import
- [ ] 替换所有使用处
- [ ] 运行测试验证

### SmsManager.java
- [ ] 移除 `CODE_LENGTH`
- [ ] 移除 `CODE_EXPIRE_SECONDS`
- [ ] 移除 `SEND_INTERVAL_SECONDS`
- [ ] 移除 `SMS_CODE_PREFIX`
- [ ] 移除 `SMS_LIMIT_PREFIX`
- [ ] 添加 `RedisKeyConstants` import
- [ ] 添加 `SmsCodeConstants` import
- [ ] 替换所有使用处
- [ ] 运行测试验证

### OssServiceImpl.java
- [ ] 移除 `ALLOWED_EXTENSIONS`
- [ ] 移除 `MAX_FILE_SIZE`
- [ ] 添加 `FileUploadConstants` import
- [ ] 替换所有使用处
- [ ] 运行测试验证

### OssManager.java
- [ ] 移除 `ALLOWED_EXTENSIONS`
- [ ] 移除 `MAX_FILE_SIZE`
- [ ] 添加 `FileUploadConstants` import
- [ ] 替换所有使用处
- [ ] 运行测试验证

---

## 🎉 总结

### 审计发现
- 🔍 检查了整个后端代码库
- 📊 发现9个重复或分散的常量
- 📁 涉及4个Service/Manager类

### 已完成工作
- ✅ 创建FileUploadConstants.java
- ✅ 更新RedisKeyConstants.java
- ✅ 更新SmsCodeConstants.java

### 待完成工作
- ⏳ 重构4个Service/Manager类
- ⏳ 移除约50行重复代码
- ⏳ 统一常量使用方式

### 预期效果
- 📉 代码重复率降低
- 📈 代码质量提升
- 🎯 维护成本降低

---

**审计时间**: 2025-12-25  
**审计人**: Kiro AI Assistant  
**状态**: ✅ 常量类已创建，⏳ 待重构Service类
