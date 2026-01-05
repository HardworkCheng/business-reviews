# 🔐 环境变量配置指南

> 本文档用于指导开发人员配置 **Business Reviews** 后端项目所需的环境变量。  
> 请根据您的实际情况修改变量值，**切勿将包含敏感信息的配置文件提交到版本控制系统**。

---

## 📋 配置概览

| 分类 | 变量数量 | 必需性 |
|------|---------|--------|
| 云服务器 | 1 | ✅ 必需 |
| MySQL 数据库 | 4 | ✅ 必需 |
| Redis 缓存 | 3 | ✅ 必需 |
| JWT 认证 | 1 | ✅ 必需 |
| 阿里云服务 | 4 | ⚠️ 按需 |
| AI 服务 | 2 | ⚠️ 按需 |

---

## 🖥️ 云服务器配置

| 变量名 | 描述 | 示例值 |
|--------|------|--------|
| `CLOUD_SERVER_HOST` | 云服务器公网 IP 地址 | `115.191.22.115` |

---

## 🗄️ MySQL 数据库配置

| 变量名 | 描述 | 示例值 |
|--------|------|--------|
| `MYSQL_PORT` | MySQL 端口号 | `3306` |
| `MYSQL_DB_NAME` | 数据库名称 | `business_reviews` |
| `MYSQL_USERNAME` | 数据库用户名 | `WebUser` |
| `MYSQL_PASSWORD` | 数据库密码 | `你的MySQL密码` |

### 注意事项
- 确保数据库用户拥有对 `business_reviews` 库的完整读写权限
- 生产环境建议使用强密码（至少包含大小写字母、数字和特殊字符）

---

## 📦 Redis 缓存配置

| 变量名 | 描述 | 示例值 |
|--------|------|--------|
| `REDIS_PORT` | Redis 端口号 | `6379` |
| `REDIS_USERNAME` | Redis 用户名（Redis 6.0+ 支持 ACL） | `default` |
| `REDIS_PASSWORD` | Redis 访问密码 | `你的Redis密码` |

### 注意事项
- 如果 Redis 未启用 ACL，将 `REDIS_USERNAME` 设置为 `default`
- 确保 Redis 服务已启动并可从应用服务器访问

---

## 🔑 JWT 认证配置

| 变量名 | 描述 | 示例值 |
|--------|------|--------|
| `JWT_SECRET` | JWT 签名密钥 | `一段足够长的随机字符串` |

### 生成建议
推荐使用以下方式生成安全的 JWT 密钥：

```bash
# Linux/macOS
openssl rand -base64 64

# 或使用 Node.js
node -e "console.log(require('crypto').randomBytes(64).toString('base64'))"
```

⚠️ **重要**: 密钥长度建议至少 **256 位 (32 字节)**，推荐使用 **512 位 (64 字节)**

---

## ☁️ 阿里云服务配置

### 短信服务 (SMS)

| 变量名 | 描述 | 获取方式 |
|--------|------|----------|
| `SMS_ALIYUN_ACCESS_KEY_ID` | 阿里云 AccessKey ID | [阿里云控制台](https://ram.console.aliyun.com/manage/ak) |
| `SMS_ALIYUN_ACCESS_KEY_SECRET` | 阿里云 AccessKey Secret | [阿里云控制台](https://ram.console.aliyun.com/manage/ak) |

### 对象存储 (OSS)

| 变量名 | 描述 | 获取方式 |
|--------|------|----------|
| `ALIYUN_OSS_ACCESS_KEY_ID` | OSS AccessKey ID | [阿里云控制台](https://ram.console.aliyun.com/manage/ak) |
| `ALIYUN_OSS_ACCESS_KEY_SECRET` | OSS AccessKey Secret | [阿里云控制台](https://ram.console.aliyun.com/manage/ak) |

### 注意事项
- 建议使用 RAM 子用户的 AccessKey，而非主账号 AccessKey
- 为子用户授予最小必要权限（如仅授予 SMS 发送权限、OSS 读写权限）
- 如果短信和 OSS 使用同一 AccessKey，两组变量可设置相同值

---

## 🤖 AI 服务配置

| 变量名 | 描述 | 用途 | 获取方式 |
|--------|------|------|----------|
| `AI_DEEPSEEK_API_KEY` | DeepSeek API 密钥 | 用于商家运营中心的 AI 评论分析和智能回复 | [DeepSeek 开放平台](https://platform.deepseek.com/) |
| `AI_QWEN_VISION_API_KEY` | 通义千问视觉模型 API 密钥 | 用于 UniApp 的 AI 笔记生成 | [阿里云百炼](https://bailian.console.aliyun.com/) |

---

## 💻 IDE 配置方法

### IntelliJ IDEA / WebStorm

1. 打开 **Run/Debug Configurations** (运行/调试配置)
2. 选择你的 Spring Boot 应用配置
3. 展开 **Environment variables** 字段
4. 点击右侧 `...` 按钮打开环境变量编辑器
5. 添加以下环境变量（可直接复制粘贴）：

```properties
# ===============================
# 云服务器
# ===============================
CLOUD_SERVER_HOST=115.191.22.115

# ===============================
# MySQL
# ===============================
MYSQL_PORT=3306
MYSQL_DB_NAME=business_reviews
MYSQL_USERNAME=WebUser
MYSQL_PASSWORD=你的MySQL密码

# ===============================
# Redis
# ===============================
REDIS_PORT=6379
REDIS_USERNAME=default
REDIS_PASSWORD=你的Redis密码

# ===============================
# JWT
# ===============================
JWT_SECRET=一段足够长的随机字符串

# ===============================
# 阿里云
# ===============================
SMS_ALIYUN_ACCESS_KEY_ID=xxx
SMS_ALIYUN_ACCESS_KEY_SECRET=xxx
ALIYUN_OSS_ACCESS_KEY_ID=xxx
ALIYUN_OSS_ACCESS_KEY_SECRET=xxx

# ===============================
# AI
# ===============================
AI_DEEPSEEK_API_KEY=xxx
AI_QWEN_VISION_API_KEY=xxx
```

### VS Code

在 `.vscode/launch.json` 中添加环境变量配置：

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Business Reviews",
      "request": "launch",
      "mainClass": "com.businessreviews.web.BusinessReviewsApplication",
      "env": {
        "CLOUD_SERVER_HOST": "115.191.22.115",
        "MYSQL_PORT": "3306",
        "MYSQL_DB_NAME": "business_reviews",
        "MYSQL_USERNAME": "WebUser",
        "MYSQL_PASSWORD": "你的MySQL密码",
        "REDIS_PORT": "6379",
        "REDIS_USERNAME": "default",
        "REDIS_PASSWORD": "你的Redis密码",
        "JWT_SECRET": "一段足够长的随机字符串",
        "SMS_ALIYUN_ACCESS_KEY_ID": "xxx",
        "SMS_ALIYUN_ACCESS_KEY_SECRET": "xxx",
        "ALIYUN_OSS_ACCESS_KEY_ID": "xxx",
        "ALIYUN_OSS_ACCESS_KEY_SECRET": "xxx",
        "AI_DEEPSEEK_API_KEY": "xxx",
        "AI_QWEN_VISION_API_KEY": "xxx"
      }
    }
  ]
}
```

---

## 🐳 Docker / Docker Compose

如果使用 Docker 部署，可以在 `docker-compose.yml` 中配置：

```yaml
version: '3.8'
services:
  backend:
    image: business-reviews-backend
    environment:
      - CLOUD_SERVER_HOST=115.191.22.115
      - MYSQL_PORT=3306
      - MYSQL_DB_NAME=business_reviews
      - MYSQL_USERNAME=WebUser
      - MYSQL_PASSWORD=${MYSQL_PASSWORD}
      - REDIS_PORT=6379
      - REDIS_USERNAME=default
      - REDIS_PASSWORD=${REDIS_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
      - SMS_ALIYUN_ACCESS_KEY_ID=${SMS_ALIYUN_ACCESS_KEY_ID}
      - SMS_ALIYUN_ACCESS_KEY_SECRET=${SMS_ALIYUN_ACCESS_KEY_SECRET}
      - ALIYUN_OSS_ACCESS_KEY_ID=${ALIYUN_OSS_ACCESS_KEY_ID}
      - ALIYUN_OSS_ACCESS_KEY_SECRET=${ALIYUN_OSS_ACCESS_KEY_SECRET}
      - AI_DEEPSEEK_API_KEY=${AI_DEEPSEEK_API_KEY}
      - AI_QWEN_VISION_API_KEY=${AI_QWEN_VISION_API_KEY}
```

然后创建 `.env` 文件（**不要提交到 Git**）：

```env
MYSQL_PASSWORD=你的MySQL密码
REDIS_PASSWORD=你的Redis密码
JWT_SECRET=一段足够长的随机字符串
SMS_ALIYUN_ACCESS_KEY_ID=xxx
SMS_ALIYUN_ACCESS_KEY_SECRET=xxx
ALIYUN_OSS_ACCESS_KEY_ID=xxx
ALIYUN_OSS_ACCESS_KEY_SECRET=xxx
AI_DEEPSEEK_API_KEY=xxx
AI_QWEN_VISION_API_KEY=xxx
```

---

## ⚠️ 安全注意事项

1. **切勿将敏感信息提交到 Git 仓库**
   - 确保 `.env` 文件已添加到 `.gitignore`
   - 不要在代码中硬编码密钥和密码

2. **定期轮换密钥**
   - 建议每 90 天更换一次关键密码
   - API 密钥泄露后立即重新生成

3. **使用最小权限原则**
   - 数据库用户只授予必要权限
   - 阿里云使用 RAM 子用户

4. **生产环境额外保护**
   - 考虑使用密钥管理服务（如阿里云 KMS）
   - 使用环境专属配置文件

---

## 🆘 常见问题

### Q: 启动时报错 "Could not resolve placeholder"
**A**: 检查是否所有必需的环境变量都已正确配置，变量名区分大小写。

### Q: 数据库连接失败
**A**: 确认以下几点：
- `CLOUD_SERVER_HOST` 和 `MYSQL_PORT` 是否正确
- 数据库 `business_reviews` 是否已创建
- 用户 `WebUser` 是否有足够权限
- 防火墙是否开放了 3306 端口

### Q: Redis 连接超时
**A**: 检查 Redis 服务状态，确认端口 6379 已开放，密码是否正确。

---

## 📞 联系与支持

如有配置问题，请联系项目负责人或查阅项目 README 文档。

---

*最后更新: 2026-01-05*
