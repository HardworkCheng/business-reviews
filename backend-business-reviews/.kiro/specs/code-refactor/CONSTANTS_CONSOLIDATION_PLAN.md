# 后端代码常量整合优化方案

## 🎯 目标

消除后端代码中重复定义的常量，统一管理，提高代码质量和可维护性。

---

## 🔍 发现的重复常量

### 1. SMS相关常量（重复3处）

#### 位置1: `RedisKeyConstants.java` ✅ 已存在
```java
public static final String SMS_CODE = "sms:code:";
public static final String SMS_LIMIT = "sms:limit:";
```

#### 位置2: `MerchantAuthServiceImpl.java` ❌ 重复
```java
private static final String MERCHANT_SMS_CODE_PREFIX = "merchant:sms:code:";
private static final String MERCHANT_SMS_LIMIT_PREFIX = "merchant:sms:limit:";
```

#### 位置3: `SmsManager.java` ❌ 重复
```java
private static final String SMS_CODE_PREFIX = "sms:code:";
private static final String SMS_LIMIT_PREFIX = "sms:limit:";
```

---

### 2. 文件上传相关常量（重复2处）

#### 位置1: `OssServiceImpl.java` ❌ 重复
```java
private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp"};
private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
```

#### 位置2: `OssManager.java` ❌ 重复
```java
private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp"};
private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
```

---

### 3. 验证码相关常量（仅1处）

#### 位置: `SmsManager.java` ⚠️ 建议提取
```java
private static final int CODE_LENGTH = 6;
private static final long CODE_EXPIRE_SECONDS = 300;
private static final long SEND_INTERVAL_SECONDS = 60;
```

---

## ✅ 优化方案

### 方案1: 扩展RedisKeyConstants

**文件**: `backend-business-reviews-common/src/main/java/com/businessreviews/constants/RedisKeyConstants.java`

```java
package com.businessreviews.constants;

/**
 * Redis Key 常量
 */
public class RedisKeyConstants {
    
    private RedisKeyConstants() {}
    
    // ========== 用户相关 ==========
    
    /** 用户信息缓存 */
    public static final String USER_INFO = "user:info:";
    
    /** 短信验证码 */
    public static final String SMS_CODE = "sms:code:";
    
    /** 短信发送频率限制 */
    public static final String SMS_LIMIT = "sms:limit:";
    
    /** 修改手机号次数限制(24小时内) */
    public static final String CHANGE_PHONE_LIMIT = "user:change:phone:limit:";
    
    // ========== 商家相关 ==========
    
    /** 商家短信验证码 */
    public static final String MERCHANT_SMS_CODE = "merchant:sms:code:";
    
    /** 商家短信发送频率限制 */
    public static final String MERCHANT_SMS_LIMIT = "merchant:sms:limit:";
    
    /** 商家信息缓存 */
    public static final String MERCHANT_INFO = "merchant:info:";
}
```

---

### 方案2: 创建FileUploadConstants

**文件**: `backend-business-reviews-common/src/main/java/com/businessreviews/constants/FileUploadConstants.java`

```java
package com.businessreviews.constants;

/**
 * 文件上传常量
 */
public class FileUploadConstants {
    
    private FileUploadConstants() {}
    
    /**
     * 允许的图片文件扩展名
     */
    public static final String[] ALLOWED_IMAGE_EXTENSIONS = {
        ".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp"
    };
    
    /**
     * 最大文件大小: 10MB
     */
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    
    /**
     * 最大文件大小描述
     */
    public static final String MAX_FILE_SIZE_DESC = "10MB";
    
    /**
     * 文件大小单位
     */
    public static final int SIZE_1KB = 1024;
    public static final int SIZE_1MB = 1024 * 1024;
    public static final long SIZE_1GB = 1024 * 1024 * 1024L;
}
```

---

### 方案3: 扩展SmsCodeConstants

**文件**: `backend-business-reviews-common/src/main/java/com/businessreviews/constants/SmsCodeConstants.java`

```java
package com.businessreviews.constants;

/**
 * 短信验证码常量
 */
public class SmsCodeConstants {
    
    private SmsCodeConstants() {}
    
    /**
     * 验证码长度
     */
    public static final int CODE_LENGTH = 6;
    
    /**
     * 验证码有效期（秒）- 5分钟
     */
    public static final long CODE_EXPIRE_SECONDS = 300;
    
    /**
     * 发送间隔限制（秒）- 60秒
     */
    public static final long SEND_INTERVAL_SECONDS = 60;
    
    /**
     * 验证码模板ID（根据实际短信服务商配置）
     */
    public static final String TEMPLATE_ID = "SMS_123456789";
}
```

---

## 📝 需要修改的文件清单

### 1. 更新RedisKeyConstants.java
- ✅ 添加 `MERCHANT_SMS_CODE`
- ✅ 添加 `MERCHANT_SMS_LIMIT`

### 2. 创建FileUploadConstants.java
- ✅ 定义 `ALLOWED_IMAGE_EXTENSIONS`
- ✅ 定义 `MAX_FILE_SIZE`
- ✅ 定义文件大小单位常量

### 3. 更新SmsCodeConstants.java
- ✅ 添加 `CODE_LENGTH`
- ✅ 添加 `CODE_EXPIRE_SECONDS`
- ✅ 添加 `SEND_INTERVAL_SECONDS`

### 4. 重构MerchantAuthServiceImpl.java
- ❌ 移除 `MERCHANT_SMS_CODE_PREFIX`
- ❌ 移除 `MERCHANT_SMS_LIMIT_PREFIX`
- ✅ 使用 `RedisKeyConstants.MERCHANT_SMS_CODE`
- ✅ 使用 `RedisKeyConstants.MERCHANT_SMS_LIMIT`

### 5. 重构SmsManager.java
- ❌ 移除 `SMS_CODE_PREFIX`
- ❌ 移除 `SMS_LIMIT_PREFIX`
- ❌ 移除 `CODE_LENGTH`
- ❌ 移除 `CODE_EXPIRE_SECONDS`
- ❌ 移除 `SEND_INTERVAL_SECONDS`
- ✅ 使用 `RedisKeyConstants.SMS_CODE`
- ✅ 使用 `RedisKeyConstants.SMS_LIMIT`
- ✅ 使用 `SmsCodeConstants.CODE_LENGTH`
- ✅ 使用 `SmsCodeConstants.CODE_EXPIRE_SECONDS`
- ✅ 使用 `SmsCodeConstants.SEND_INTERVAL_SECONDS`

### 6. 重构OssServiceImpl.java
- ❌ 移除 `ALLOWED_EXTENSIONS`
- ❌ 移除 `MAX_FILE_SIZE`
- ✅ 使用 `FileUploadConstants.ALLOWED_IMAGE_EXTENSIONS`
- ✅ 使用 `FileUploadConstants.MAX_FILE_SIZE`

### 7. 重构OssManager.java
- ❌ 移除 `ALLOWED_EXTENSIONS`
- ❌ 移除 `MAX_FILE_SIZE`
- ✅ 使用 `FileUploadConstants.ALLOWED_IMAGE_EXTENSIONS`
- ✅ 使用 `FileUploadConstants.MAX_FILE_SIZE`

---

## 📊 优化效果统计

### 代码行数变化
- **新增文件**: 1个（FileUploadConstants.java）
- **修改文件**: 6个
- **移除重复代码**: 约50行
- **新增常量定义**: 约30行
- **净减少**: 约20行

### 重复常量消除
- ✅ SMS相关常量：3处 → 1处
- ✅ 文件上传常量：2处 → 1处
- ✅ 验证码配置常量：1处（提取到常量类）

### 代码质量提升
- ✅ 消除魔法数字
- ✅ 统一常量管理
- ✅ 提高可维护性
- ✅ 降低出错风险

---

## 🎯 实施优先级

### 高优先级（立即实施）
1. ✅ 创建 `FileUploadConstants.java`
2. ✅ 更新 `RedisKeyConstants.java`
3. ✅ 更新 `SmsCodeConstants.java`

### 中优先级（尽快实施）
4. ✅ 重构 `MerchantAuthServiceImpl.java`
5. ✅ 重构 `SmsManager.java`

### 低优先级（可延后）
6. ✅ 重构 `OssServiceImpl.java`
7. ✅ 重构 `OssManager.java`

---

## ⚠️ 注意事项

### 1. 向后兼容
- 所有修改不影响API接口
- 功能保持完全一致
- 只是代码组织优化

### 2. 测试验证
- 修改后需要运行单元测试
- 验证SMS发送功能
- 验证文件上传功能

### 3. 依赖关系
- 确保common模块被正确依赖
- 检查import语句
- 验证编译无错误

---

## 🚀 实施步骤

### 步骤1: 创建/更新常量类
```bash
# 1. 创建FileUploadConstants.java
# 2. 更新RedisKeyConstants.java
# 3. 更新SmsCodeConstants.java
```

### 步骤2: 重构Service类
```bash
# 1. 更新MerchantAuthServiceImpl.java
# 2. 更新SmsManager.java
# 3. 更新OssServiceImpl.java
# 4. 更新OssManager.java
```

### 步骤3: 编译测试
```bash
cd backend-business-reviews
mvn clean compile
mvn test
```

### 步骤4: 验证功能
```bash
# 启动应用
mvn spring-boot:run

# 测试SMS功能
# 测试文件上传功能
```

---

## 📈 预期收益

### 短期收益
- ✅ 代码更简洁
- ✅ 消除重复
- ✅ 易于理解

### 长期收益
- ✅ 维护成本降低
- ✅ 修改风险降低
- ✅ 团队协作更顺畅
- ✅ 新人上手更快

---

## 🎉 总结

**发现问题**:
- SMS相关常量重复定义3处
- 文件上传常量重复定义2处
- 验证码配置分散在代码中

**解决方案**:
1. 创建FileUploadConstants统一管理文件上传常量
2. 扩展RedisKeyConstants添加商家SMS相关key
3. 扩展SmsCodeConstants添加验证码配置常量
4. 重构6个Service类使用统一常量

**预期效果**:
- 消除约50行重复代码
- 提高代码质量和可维护性
- 降低维护成本和出错风险

---

**创建时间**: 2025-12-25  
**分析人**: Kiro AI Assistant  
**状态**: 📋 待实施
