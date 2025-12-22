# Bug 修复记录

## 2025-12-22 修复记录

### 1. 商家评价认证失败问题

**问题描述：**
用户在 UniApp 端发布商家评价时，前端发送了正确的 JWT token，但后端报错 "用户未登录"。

**错误信息：**
```
java.lang.RuntimeException: 用户未登录
at com.businessreviews.context.UserContext.requireUserId(UserContext.java:21)
at com.businessreviews.web.app.ShopController.postShopReview(ShopController.java:140)
```

**根本原因：**
在 `WebMvcConfig` 中，`/shops/**` 路径被完全排除在 `AuthInterceptor` 拦截器之外，导致所有商家相关接口（包括需要认证的 POST 请求）都不会经过认证拦截器，因此 `UserContext` 中没有设置用户 ID。

**修复方案：**

1. **修改 `WebMvcConfig.java`**
   - 将 `/shops/**` 改为更精确的路径匹配
   - 只排除不需要认证的 GET 请求路径：
     - `/shops` - 商家列表
     - `/shops/nearby` - 附近商家
     - `/shops/search` - 搜索商家
     - `/shops/registered` - 已注册商家列表

2. **修改 `AuthInterceptor.java`**
   - 在 `isOptionalAuthPath` 方法中添加商家详情和评价列表为可选认证路径
   - 支持以下 GET 请求的可选认证：
     - `/shops/{id}` - 商家详情（游客可访问，登录用户可看收藏状态）
     - `/shops/{id}/reviews` - 评价列表（公开访问）
     - `/shops/{id}/notes` - 笔记列表（公开访问）

**修改文件：**
- `backend-business-reviews-web/src/main/java/com/businessreviews/config/WebMvcConfig.java`
- `backend-business-reviews-web/src/main/java/com/businessreviews/interceptor/AuthInterceptor.java`

**测试验证：**
- ✅ 用户可以正常发布商家评价
- ✅ 游客可以查看商家详情和评价列表
- ✅ 登录用户查看商家详情时可以看到收藏状态

---

### 2. OSS 图片上传失败问题

**问题描述：**
商家在运营中心上传商家相册图片时，出现 OSS 上传失败错误。

**错误信息：**
```
com.aliyun.oss.ClientException: Failed to reset the request input stream
Caused by: java.io.IOException: Resetting to invalid mark
at java.base/java.io.BufferedInputStream.implReset(BufferedInputStream.java:583)
```

**根本原因：**
当 OSS SDK 遇到网络错误（如 Connection reset）需要重试上传时，会尝试重置 InputStream。但是：
1. `MultipartFile.getInputStream()` 返回的流不支持 mark/reset
2. 流已经被读取过，无法重置到起始位置
3. OSS SDK 的重试机制失败，抛出异常

**修复方案：**

1. **将文件内容先读取到内存**
   - 使用 `file.getBytes()` 将文件内容读取到字节数组
   - 使用 `ByteArrayInputStream` 创建可重置的输入流

2. **设置内容长度元数据**
   - 创建 `ObjectMetadata` 并设置 `contentLength`
   - 避免 OSS SDK 需要读取流来计算长度

3. **正确关闭资源**
   - 在 finally 块中关闭 InputStream
   - 确保资源不泄漏

**修改文件：**
- `backend-business-reviews-service/src/main/java/com/businessreviews/service/impl/common/OssServiceImpl.java`

**关键代码变更：**

```java
// 修改前
return uploadToOss(file.getInputStream(), objectKey);

// 修改后
byte[] fileBytes = file.getBytes();
return uploadToOss(new ByteArrayInputStream(fileBytes), objectKey, fileBytes.length);
```

```java
// 添加元数据设置
ObjectMetadata metadata = new ObjectMetadata();
metadata.setContentLength(contentLength);
putObjectRequest.setMetadata(metadata);
```

**优点：**
- ✅ ByteArrayInputStream 支持 mark/reset，可以安全重试
- ✅ 明确指定内容长度，避免 SDK 读取流
- ✅ 对于小于 10MB 的图片文件，内存占用可接受
- ✅ 提高上传成功率，特别是在网络不稳定时

**测试验证：**
- ✅ 商家可以正常上传相册图片
- ✅ 网络不稳定时 OSS SDK 可以正常重试
- ✅ 上传失败时有清晰的错误信息

---

## 技术要点总结

### 认证拦截器配置
- 使用精确的路径匹配而不是通配符
- 区分需要认证、可选认证和公开访问的接口
- 可选认证：游客可访问，但如果有 token 则解析用户信息

### OSS 上传最佳实践
- 对于小文件（< 10MB），使用字节数组避免流重置问题
- 对于大文件，考虑使用分片上传
- 始终设置 ObjectMetadata 的 contentLength
- 正确处理资源关闭，避免内存泄漏

### 调试技巧
- 查看完整的异常堆栈，找到根本原因
- 检查拦截器配置和执行顺序
- 使用日志输出关键信息（token、userId 等）
- 理解第三方 SDK 的重试机制
