# 敏感数据脱敏技术方案

> 本文档详细说明了 Business Reviews 系统中敏感数据脱敏的实现方案，包括注解设计、序列化器实现和使用指南。

## 📋 目录

1. [问题背景](#问题背景)
2. [解决方案概述](#解决方案概述)
3. [核心组件详解](#核心组件详解)
4. [脱敏规则说明](#脱敏规则说明)
5. [使用指南](#使用指南)
6. [工作原理](#工作原理)
7. [已应用的 VO 类](#已应用的-vo-类)
8. [扩展指南](#扩展指南)
9. [最佳实践](#最佳实践)
10. [常见问题](#常见问题)

---

## 问题背景

### 敏感数据泄露风险

在 Web 应用中，API 响应通常包含用户的敏感信息，如：

- 📱 手机号：`13812345678`
- 📧 邮箱：`zhangsan@example.com`
- 🪪 身份证号：`330106199001011234`
- 💳 银行卡号：`6222021234567890`

如果这些数据在 API 响应中直接返回给前端，可能导致：

1. **隐私泄露**：前端控制台、网络抓包都能看到完整信息
2. **合规风险**：违反《个人信息保护法》等法规要求
3. **安全隐患**：攻击者获取数据后可用于社会工程学攻击

### 传统解决方案的问题

```java
// ❌ 硬编码脱敏 - 容易遗漏，代码重复
public UserVO getUser(Long userId) {
    UserDO user = userMapper.selectById(userId);
    UserVO vo = new UserVO();
    vo.setPhone(maskPhone(user.getPhone())); // 每次都要手动调用
    return vo;
}

// ❌ 在 Service 层脱敏 - 逻辑分散，难以维护
private String maskPhone(String phone) {
    if (phone == null || phone.length() < 7) return phone;
    return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
}
```

**问题**：
- 每个字段都需要手动处理，容易遗漏
- 脱敏逻辑分散在各处，难以统一维护
- 新增字段时容易忘记脱敏

---

## 解决方案概述

采用 **注解 + Jackson 序列化器** 的方案，在 JSON 序列化阶段自动完成脱敏：

```java
// ✅ 声明式脱敏 - 一个注解搞定
public class UserVO {
    
    @Sensitive(type = SensitiveType.PHONE)
    private String phone;  // 输出: 138****5678
    
    @Sensitive(type = SensitiveType.EMAIL)
    private String email;  // 输出: abc***@example.com
}
```

### 方案优势

| 特性 | 说明 |
|------|------|
| 🎯 **声明式** | 只需添加注解，无需编写脱敏代码 |
| 🔄 **自动化** | 在 JSON 序列化时自动生效 |
| 🧩 **可扩展** | 易于添加新的脱敏类型 |
| 🛡️ **统一管理** | 所有脱敏规则集中在序列化器中 |
| ⚡ **零侵入** | 不影响 Service 层业务逻辑 |

---

## 核心组件详解

### 组件架构

```
┌─────────────────────────────────────────────────────────────┐
│                        应用层                                │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   ┌─────────────────┐                                       │
│   │     UserVO      │                                       │
│   │  ┌───────────┐  │                                       │
│   │  │   phone   │──┼──▶ @Sensitive(type=PHONE)            │
│   │  └───────────┘  │                                       │
│   └─────────────────┘                                       │
│            │                                                │
│            ▼                                                │
│   ┌─────────────────────────────────────────────────────┐   │
│   │              Jackson ObjectMapper                    │   │
│   │                                                      │   │
│   │   序列化 UserVO 时：                                  │   │
│   │   1. 发现 phone 字段有 @Sensitive 注解               │   │
│   │   2. 使用 SensitiveSerializer 处理                   │   │
│   │   3. 根据 SensitiveType.PHONE 调用脱敏方法           │   │
│   │                                                      │   │
│   └─────────────────────────────────────────────────────┘   │
│            │                                                │
│            ▼                                                │
│   ┌─────────────────┐                                       │
│   │  JSON Response  │                                       │
│   │  {              │                                       │
│   │    "phone":     │                                       │
│   │    "138****5678"│  ◀── 自动脱敏后的结果                 │
│   │  }              │                                       │
│   └─────────────────┘                                       │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

### 1. SensitiveType（脱敏类型枚举）

**路径**: `com.businessreviews.model.annotation.SensitiveType`

**职责**: 定义支持的脱敏类型及其规则描述。

```java
public enum SensitiveType {
    
    /**
     * 手机号脱敏
     * 规则: 保留前3位和后4位，中间替换为****
     * 示例: 138****1234
     */
    PHONE,
    
    /**
     * 邮箱脱敏
     * 规则: @前只显示前3个字符，其余替换为***
     * 示例: abc***@example.com
     */
    EMAIL,
    
    /**
     * 身份证号脱敏
     * 规则: 保留前6位和后4位，中间替换为****
     * 示例: 330106****1234
     */
    ID_CARD,
    
    /**
     * 银行卡号脱敏
     * 规则: 保留前4位和后4位，中间替换为****
     * 示例: 6222****8888
     */
    BANK_CARD,
    
    /**
     * 姓名脱敏
     * 规则: 只显示第一个字符，其余替换为*
     * 示例: 张**
     */
    NAME,
    
    /**
     * 地址脱敏
     * 规则: 只显示前6个字符，其余替换为***
     * 示例: 浙江省杭州***
     */
    ADDRESS,
    
    /**
     * 自定义脱敏
     * 规则: 全部替换为***
     */
    CUSTOM
}
```

---

### 2. @Sensitive（脱敏注解）

**路径**: `com.businessreviews.model.annotation.Sensitive`

**职责**: 标记需要脱敏的字段，并指定脱敏类型。

```java
@Target(ElementType.FIELD)           // 只能用于字段
@Retention(RetentionPolicy.RUNTIME)  // 运行时保留
@Documented
@JacksonAnnotationsInside            // 组合 Jackson 注解
@JsonSerialize(using = SensitiveSerializer.class)  // 指定序列化器
public @interface Sensitive {
    
    /**
     * 脱敏类型，默认为手机号脱敏
     */
    SensitiveType type() default SensitiveType.PHONE;
}
```

**关键注解说明**：

| 注解 | 作用 |
|------|------|
| `@JacksonAnnotationsInside` | 标记这是一个组合注解，包含其他 Jackson 注解 |
| `@JsonSerialize(using = ...)` | 指定使用的自定义序列化器 |

---

### 3. SensitiveSerializer（脱敏序列化器）

**路径**: `com.businessreviews.model.serializer.SensitiveSerializer`

**职责**: 实现具体的脱敏逻辑，在 JSON 序列化时自动执行。

```java
@NoArgsConstructor
@AllArgsConstructor
public class SensitiveSerializer extends JsonSerializer<String> 
        implements ContextualSerializer {

    private SensitiveType sensitiveType;

    /**
     * 核心序列化方法 - 在输出 JSON 时调用
     */
    @Override
    public void serialize(String value, JsonGenerator gen, 
            SerializerProvider serializers) throws IOException {
        
        if (value == null || value.isEmpty()) {
            gen.writeString(value);
            return;
        }
        
        // 执行脱敏处理
        String maskedValue = maskValue(value, sensitiveType);
        gen.writeString(maskedValue);
    }

    /**
     * 上下文初始化 - 获取字段上的 @Sensitive 注解信息
     */
    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, 
            BeanProperty property) throws JsonMappingException {
        
        if (property == null) {
            return prov.findNullValueSerializer(null);
        }
        
        // 读取字段上的 @Sensitive 注解
        Sensitive sensitive = property.getAnnotation(Sensitive.class);
        if (sensitive == null) {
            sensitive = property.getContextAnnotation(Sensitive.class);
        }
        
        // 如果有注解且字段类型是 String，创建对应类型的序列化器
        if (sensitive != null && 
                Objects.equals(property.getType().getRawClass(), String.class)) {
            return new SensitiveSerializer(sensitive.type());
        }
        
        return prov.findValueSerializer(property.getType(), property);
    }

    /**
     * 根据脱敏类型执行对应的脱敏逻辑
     */
    private String maskValue(String value, SensitiveType type) {
        if (type == null || value == null || value.isEmpty()) {
            return value;
        }
        
        switch (type) {
            case PHONE:     return maskPhone(value);
            case EMAIL:     return maskEmail(value);
            case ID_CARD:   return maskIdCard(value);
            case BANK_CARD: return maskBankCard(value);
            case NAME:      return maskName(value);
            case ADDRESS:   return maskAddress(value);
            case CUSTOM:
            default:        return "***";
        }
    }
    
    // ... 各类型具体脱敏实现方法
}
```

**核心接口说明**：

| 接口 | 作用 |
|------|------|
| `JsonSerializer<String>` | Jackson 的字符串序列化器基类 |
| `ContextualSerializer` | 支持上下文感知，可以读取字段注解信息 |

---

## 脱敏规则说明

### 脱敏效果一览表

| 类型 | 原始值 | 脱敏后 | 规则说明 |
|------|--------|--------|----------|
| 📱 手机号 | `13812345678` | `138****5678` | 前3后4，中间**** |
| 📧 邮箱 | `zhangsan@example.com` | `zha***@example.com` | @前保留3位，其余*** |
| 🪪 身份证 | `330106199001011234` | `330106****1234` | 前6后4，中间**** |
| 💳 银行卡 | `6222021234567890` | `6222****7890` | 前4后4，中间**** |
| 👤 姓名 | `张三丰` | `张**` | 首字保留，其余* |
| 📍 地址 | `浙江省杭州市西湖区xxx街道` | `浙江省杭州***` | 前6字符，其余*** |

### 脱敏算法详解

#### 手机号脱敏

```java
/**
 * 手机号脱敏：保留前3位和后4位，中间替换为****
 * 输入: 13812345678
 * 输出: 138****5678
 */
private String maskPhone(String phone) {
    if (phone == null || phone.length() < 7) {
        return phone;  // 长度不足，不脱敏
    }
    
    int prefixLen = 3;  // 前缀长度
    int suffixLen = 4;  // 后缀长度
    
    if (phone.length() <= prefixLen + suffixLen) {
        return phone;  // 长度不足，不脱敏
    }
    
    return phone.substring(0, prefixLen) 
         + "****" 
         + phone.substring(phone.length() - suffixLen);
}
```

#### 邮箱脱敏

```java
/**
 * 邮箱脱敏：@前只显示前3个字符，其余替换为***
 * 输入: zhangsan@example.com
 * 输出: zha***@example.com
 */
private String maskEmail(String email) {
    if (email == null || !email.contains("@")) {
        return email;  // 非法邮箱，不脱敏
    }
    
    int atIndex = email.indexOf("@");
    if (atIndex <= 3) {
        return email;  // 用户名太短，不脱敏
    }
    
    return email.substring(0, 3) + "***" + email.substring(atIndex);
}
```

#### 身份证号脱敏

```java
/**
 * 身份证号脱敏：保留前6位和后4位，中间替换为****
 * 输入: 330106199001011234
 * 输出: 330106****1234
 */
private String maskIdCard(String idCard) {
    if (idCard == null || idCard.length() < 10) {
        return idCard;  // 长度不足，不脱敏
    }
    
    return idCard.substring(0, 6) 
         + "****" 
         + idCard.substring(idCard.length() - 4);
}
```

#### 姓名脱敏

```java
/**
 * 姓名脱敏：只显示第一个字符，其余替换为*
 * 输入: 张三丰
 * 输出: 张**
 */
private String maskName(String name) {
    if (name == null || name.length() < 2) {
        return name;  // 长度不足，不脱敏
    }
    
    StringBuilder sb = new StringBuilder(name.substring(0, 1));
    for (int i = 1; i < name.length(); i++) {
        sb.append("*");
    }
    return sb.toString();
}
```

---

## 使用指南

### 基本用法

**步骤 1**：在 VO 类中导入注解

```java
import com.businessreviews.model.annotation.Sensitive;
import com.businessreviews.model.annotation.SensitiveType;
```

**步骤 2**：在需要脱敏的字段上添加注解

```java
@Data
public class UserVO {
    
    private Long id;
    private String username;
    
    // 手机号脱敏
    @Sensitive(type = SensitiveType.PHONE)
    private String phone;
    
    // 邮箱脱敏
    @Sensitive(type = SensitiveType.EMAIL)
    private String email;
    
    // 身份证脱敏
    @Sensitive(type = SensitiveType.ID_CARD)
    private String idCard;
}
```

**步骤 3**：正常返回 VO，脱敏自动生效

```java
@RestController
public class UserController {
    
    @GetMapping("/user/{id}")
    public Result<UserVO> getUser(@PathVariable Long id) {
        UserVO user = userService.getUserById(id);
        return Result.success(user);
        // 返回的 JSON 中，phone/email/idCard 已自动脱敏
    }
}
```

### 完整示例

**输入**（数据库中的原始数据）：

```java
UserVO user = new UserVO();
user.setId(1L);
user.setUsername("张三");
user.setPhone("13812345678");
user.setEmail("zhangsan@example.com");
user.setIdCard("330106199001011234");
```

**输出**（API 响应的 JSON）：

```json
{
    "id": 1,
    "username": "张三",
    "phone": "138****5678",
    "email": "zha***@example.com",
    "idCard": "330106****1234"
}
```

---

## 工作原理

### 序列化流程图

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Jackson 序列化流程                           │
└─────────────────────────────────────────────────────────────────────┘

1. Controller 返回 UserVO 对象
         │
         ▼
┌─────────────────────────────────────────────────────────────────────┐
│ Spring MVC 调用 Jackson ObjectMapper.writeValueAsString(userVO)     │
└─────────────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────────────┐
│ Jackson 遍历 UserVO 的每个字段                                       │
│                                                                      │
│   字段: phone                                                        │
│         │                                                            │
│         ▼                                                            │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │ 检查字段上的注解                                              │   │
│   │ 发现: @Sensitive(type = SensitiveType.PHONE)                 │   │
│   │                                                               │   │
│   │ 通过 @JsonSerialize 找到序列化器: SensitiveSerializer        │   │
│   └─────────────────────────────────────────────────────────────┘   │
│         │                                                            │
│         ▼                                                            │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │ SensitiveSerializer.createContextual()                       │   │
│   │                                                               │   │
│   │ 读取 @Sensitive 注解，获取 type = PHONE                      │   │
│   │ 创建新的 SensitiveSerializer(SensitiveType.PHONE) 实例       │   │
│   └─────────────────────────────────────────────────────────────┘   │
│         │                                                            │
│         ▼                                                            │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │ SensitiveSerializer.serialize("13812345678", ...)            │   │
│   │                                                               │   │
│   │ 调用 maskValue("13812345678", PHONE)                         │   │
│   │   └─▶ maskPhone("13812345678")                               │   │
│   │        └─▶ 返回 "138****5678"                                │   │
│   │                                                               │   │
│   │ gen.writeString("138****5678")  // 写入脱敏后的值             │   │
│   └─────────────────────────────────────────────────────────────┘   │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────────────┐
│ 最终 JSON: {"phone": "138****5678", ...}                            │
└─────────────────────────────────────────────────────────────────────┘
```

### ContextualSerializer 的作用

`ContextualSerializer` 接口允许序列化器感知其所在的上下文（即字段信息）：

```java
public interface ContextualSerializer {
    JsonSerializer<?> createContextual(SerializerProvider prov, 
                                        BeanProperty property);
}
```

**为什么需要它？**

因为同一个 `SensitiveSerializer` 类可能用于多个字段，每个字段的脱敏类型不同：

```java
@Sensitive(type = SensitiveType.PHONE)
private String phone;    // 需要手机号脱敏

@Sensitive(type = SensitiveType.EMAIL)
private String email;    // 需要邮箱脱敏
```

通过 `createContextual()`，序列化器可以读取当前字段的注解，获取具体的脱敏类型，然后创建对应配置的序列化器实例。

---

## 已应用的 VO 类

目前已在以下 VO 类中应用了敏感数据脱敏：

| VO 类 | 脱敏字段 | 脱敏类型 |
|-------|----------|----------|
| `UserVO` | `phone` | `PHONE` |
| `UserInfoVO` | `phone` | `PHONE` |
| `AppUserInfoVO` | `phone` | `PHONE` |
| `MerchantUserInfoVO` | `phone` | `PHONE` |
| `MerchantUserInfoVO` | `contactEmail` | `EMAIL` |

### 示例：UserVO

```java
@Data
public class UserVO implements Serializable {
    
    private String userId;
    private String username;
    private String avatar;
    private String bio;
    
    /**
     * 手机号（脱敏：138****1234）
     */
    @Sensitive(type = SensitiveType.PHONE)
    private String phone;
    
    // ... 其他字段
}
```

### 示例：MerchantUserInfoVO

```java
@Data
public class MerchantUserInfoVO implements Serializable {
    
    private String userId;
    private String merchantId;
    private String merchantName;
    
    /** 手机号 */
    @Sensitive(type = SensitiveType.PHONE)
    private String phone;

    /** 联系邮箱 */
    @Sensitive(type = SensitiveType.EMAIL)
    private String contactEmail;
    
    // ... 其他字段
}
```

---

## 扩展指南

### 添加新的脱敏类型

**步骤 1**：在 `SensitiveType` 枚举中添加新类型

```java
public enum SensitiveType {
    PHONE,
    EMAIL,
    ID_CARD,
    BANK_CARD,
    NAME,
    ADDRESS,
    CUSTOM,
    
    /**
     * 新增：车牌号脱敏
     * 规则: 保留前2位和后2位
     * 示例: 浙A****12
     */
    CAR_LICENSE
}
```

**步骤 2**：在 `SensitiveSerializer` 中添加脱敏方法

```java
private String maskValue(String value, SensitiveType type) {
    switch (type) {
        // ... 现有类型
        case CAR_LICENSE:
            return maskCarLicense(value);
        default:
            return "***";
    }
}

/**
 * 车牌号脱敏：保留前2位和后2位，中间替换为****
 * 输入: 浙A12345
 * 输出: 浙A****45
 */
private String maskCarLicense(String license) {
    if (license == null || license.length() < 5) {
        return license;
    }
    return license.substring(0, 2) + "****" + license.substring(license.length() - 2);
}
```

**步骤 3**：在 VO 中使用新类型

```java
public class VehicleVO {
    
    @Sensitive(type = SensitiveType.CAR_LICENSE)
    private String licensePlate;
}
```

---

## 最佳实践

### ✅ 推荐做法

```java
// 1. 在 VO 类（用于 API 响应）中使用脱敏注解
@Data
public class UserVO {
    @Sensitive(type = SensitiveType.PHONE)
    private String phone;
}

// 2. DO（数据库实体）不要使用脱敏注解，保持原始数据
@Data
public class UserDO {
    private String phone;  // 不脱敏，存储完整值
}

// 3. 在 Service 层进行 DO -> VO 转换
public UserVO getUser(Long id) {
    UserDO user = userMapper.selectById(id);
    return BeanUtil.copyProperties(user, UserVO.class);
    // 返回的 VO 会自动脱敏
}
```

### ❌ 避免的做法

```java
// 1. 不要在 DO 上使用脱敏注解
//    这会导致写入数据库的数据也被脱敏
@Data
@TableName("user")
public class UserDO {
    @Sensitive(type = SensitiveType.PHONE)  // ❌ 错误！
    private String phone;
}

// 2. 不要对非 String 类型使用脱敏注解
@Data
public class SomeVO {
    @Sensitive(type = SensitiveType.PHONE)  // ❌ 无效，Long 类型不支持
    private Long phoneNumber;
}
```

---

## 常见问题

### 1. 脱敏注解不生效？

**可能原因**：
- 字段类型不是 `String`
- 没有使用 Jackson 进行 JSON 序列化
- 注解导入路径错误

**解决方法**：
```java
// 确保导入正确的包
import com.businessreviews.model.annotation.Sensitive;
import com.businessreviews.model.annotation.SensitiveType;

// 确保字段类型是 String
@Sensitive(type = SensitiveType.PHONE)
private String phone;  // ✅ 正确
```

### 2. 如何获取原始未脱敏的值？

脱敏只影响 JSON 序列化输出，在 Java 代码中可以正常获取原始值：

```java
UserVO user = userService.getUser(1L);

// 在代码中可以获取原始值
String originalPhone = user.getPhone();  // "13812345678"

// 只有在 JSON 输出时才会脱敏
objectMapper.writeValueAsString(user);  // {"phone": "138****5678"}
```

### 3. 如何在特定场景下不脱敏？

可以创建一个不带 `@Sensitive` 注解的 VO：

```java
// 对外 API 使用的 VO（脱敏）
@Data
public class UserVO {
    @Sensitive(type = SensitiveType.PHONE)
    private String phone;
}

// 内部管理系统使用的 VO（不脱敏）
@Data
public class UserAdminVO {
    private String phone;  // 不脱敏，管理员可见完整信息
}
```

---

## 总结

通过 `@Sensitive` 注解和 `SensitiveSerializer` 序列化器，我们实现了：

| 目标 | 实现效果 |
|------|----------|
| 统一脱敏规则 | 所有脱敏逻辑集中在 `SensitiveSerializer` 中 |
| 声明式配置 | 只需添加注解，无需编写脱敏代码 |
| 零侵入 | 不影响 Service 层业务逻辑 |
| 易扩展 | 添加新脱敏类型只需修改枚举和序列化器 |
| 类型安全 | 编译时检查脱敏类型是否合法 |

这套方案符合《个人信息保护法》等法规对敏感数据处理的要求，可有效防止用户隐私泄露。
