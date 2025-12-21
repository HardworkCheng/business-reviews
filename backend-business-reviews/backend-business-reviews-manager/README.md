# Manager层模块

## 模块说明

Manager层是按照《阿里巴巴Java开发手册》规范新增的通用业务处理层，主要职责：

1. **第三方服务调用** - 封装OSS、短信、支付等第三方接口
2. **通用业务逻辑** - 处理跨Service的通用逻辑
3. **缓存管理** - 统一的缓存操作封装

## 包结构

```
com.businessreviews.manager
├── OssManager.java      # 阿里云OSS文件管理
├── SmsManager.java      # 阿里云短信服务管理
└── CacheManager.java    # 缓存管理（可选）
```

## 使用规范

- Manager层被Service层调用
- Manager层不直接操作数据库
- Manager层专注于第三方服务的封装和调用
