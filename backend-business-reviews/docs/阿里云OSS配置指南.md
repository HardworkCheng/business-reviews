# 阿里云OSS配置指南

## 前置准备

### 1. 创建OSS Bucket

1. 登录 [阿里云OSS控制台](https://oss.console.aliyun.com/)
2. 点击 "创建Bucket"
3. 填写配置:
   - **Bucket名称**: `business-reviews` (或你自定义的名称)
   - **地域**: 选择离你用户最近的地域(例如: 华东1-杭州)
   - **存储类型**: 标准存储
   - **读写权限**: **公共读** (重要!)
   - **服务端加密**: 无
   
4. 点击 "确定" 创建

### 2. 获取AccessKey

1. 登录 [阿里云RAM访问控制](https://ram.console.aliyun.com/manage/ak)
2. 点击 "创建AccessKey"
3. 记录下:
   - **AccessKey ID**
   - **AccessKey Secret** (只显示一次,请妥善保存)

## 配置步骤

### 方式1: 直接修改 application.yml (开发环境)

编辑 `backend-business-reviews/src/main/resources/application.yml`:

```yaml
aliyun:
  oss:
    # 根据你的Bucket地域修改endpoint
    # 华东1-杭州: oss-cn-hangzhou.aliyuncs.com
    # 华东2-上海: oss-cn-shanghai.aliyuncs.com
    # 华北2-北京: oss-cn-beijing.aliyuncs.com
    # 华南1-深圳: oss-cn-shenzhen.aliyuncs.com
    endpoint: oss-cn-hangzhou.aliyuncs.com
    
    # 填写你的AccessKey ID
    access-key-id: LTAI5tXXXXXXXXXXXXXX
    
    # 填写你的AccessKey Secret
    access-key-secret: xxxxxxxxxxxxxxxxxxxxxxxxxxxxx
    
    # 填写你的Bucket名称
    bucket-name: business-reviews
    
    # 访问URL前缀 = https://<bucket-name>.<endpoint>
    url-prefix: https://business-reviews.oss-cn-hangzhou.aliyuncs.com
```

### 方式2: 使用环境变量 (生产环境推荐)

#### Windows (PowerShell):
```powershell
$env:ALIYUN_ACCESS_KEY_ID="你的AccessKey ID"
$env:ALIYUN_ACCESS_KEY_SECRET="你的AccessKey Secret"
```

#### Linux/Mac:
```bash
export ALIYUN_ACCESS_KEY_ID="你的AccessKey ID"
export ALIYUN_ACCESS_KEY_SECRET="你的AccessKey Secret"
```

然后 `application.yml` 保持默认配置即可:
```yaml
aliyun:
  oss:
    endpoint: oss-cn-hangzhou.aliyuncs.com
    access-key-id: ${ALIYUN_ACCESS_KEY_ID:your-access-key-id}
    access-key-secret: ${ALIYUN_ACCESS_KEY_SECRET:your-access-key-secret}
    bucket-name: business-reviews
    url-prefix: https://business-reviews.oss-cn-hangzhou.aliyuncs.com
```

## 配置说明

| 配置项 | 说明 | 示例 |
|--------|------|------|
| `endpoint` | OSS服务endpoint,根据Bucket地域设置 | `oss-cn-hangzhou.aliyuncs.com` |
| `access-key-id` | 阿里云AccessKey ID | `LTAI5tXXXXXXXXXXXXXX` |
| `access-key-secret` | 阿里云AccessKey Secret | `xxxxxxxxxxxxxxxxxxxxx` |
| `bucket-name` | 创建的Bucket名称 | `business-reviews` |
| `url-prefix` | 访问URL前缀 | `https://<bucket-name>.<endpoint>` |

## 重要说明

### 1. Bucket权限设置
- **必须设置为"公共读"**, 否则前端无法访问图片
- 设置方法: OSS控制台 -> 选择Bucket -> 权限管理 -> 读写权限 -> 公共读

### 2. 跨域配置(CORS)
如果前端访问图片时出现跨域错误,需要配置CORS:

1. OSS控制台 -> 选择Bucket -> 权限管理 -> 跨域设置
2. 点击 "创建规则"
3. 填写:
   - **来源**: `*` (允许所有来源) 或你的前端域名
   - **允许Methods**: GET, POST, PUT, DELETE, HEAD
   - **允许Headers**: `*`
   - **暴露Headers**: ETag
   - **缓存时间**: 600

### 3. URL格式
上传成功后,返回的URL格式为:
```
https://<bucket-name>.<endpoint>/<folder>/<date>/<filename>

例如:
https://business-reviews.oss-cn-hangzhou.aliyuncs.com/avatars/2025/12/04/abc123.jpg
```

## 测试步骤

1. **配置完成后重启后端**
   ```bash
   # 重启Spring Boot应用
   ```

2. **刷新前端页面**

3. **登录并进入设置页面**

4. **点击头像上传图片**

5. **检查上传结果**:
   - 控制台应该显示: `文件上传成功到OSS: https://...`
   - 头像应该立即显示
   - 刷新页面头像依然显示

6. **验证OSS控制台**:
   - 登录OSS控制台
   - 进入你的Bucket
   - 文件管理 -> 应该能看到 `avatars/2025/12/04/xxx.jpg`

## 常见问题

### 1. 上传失败: InvalidAccessKeyId
- 检查 `access-key-id` 是否正确
- 检查AccessKey是否已启用

### 2. 上传失败: Access Denied
- 检查 `access-key-secret` 是否正确
- 检查RAM用户是否有OSS操作权限

### 3. 图片无法访问: 403 Forbidden
- 检查Bucket权限是否设置为"公共读"
- 检查CORS配置是否正确

### 4. 图片不显示
- 检查返回的URL格式是否正确
- 检查 `url-prefix` 配置是否正确
- 在浏览器直接访问图片URL测试

## 安全建议

1. **不要将AccessKey硬编码在代码中**
2. **使用环境变量管理敏感信息**
3. **定期更新AccessKey**
4. **为不同环境使用不同的Bucket**
5. **启用Bucket日志审计**

## 费用说明

- **存储费用**: 标准存储 0.12元/GB/月
- **流量费用**: 外网流出流量 0.5元/GB
- **请求费用**: PUT请求 0.01元/万次, GET请求 0.01元/万次

建议: 
- 小型应用每月费用约 10-50 元
- 开启CDN加速可降低流量费用
