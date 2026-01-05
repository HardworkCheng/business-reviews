# 🚀 美食点评系统 - 云服务器部署完整指南

> **版本**: v1.0  
> **更新时间**: 2026-01-04  
> **适用系统**: CentOS 7/8, Ubuntu 20.04/22.04, Debian 11+

本文档将指导你将整个项目（Java 后端 + UniApp H5 + Vue 商家端）部署到云服务器上。

---

## 📋 目录

1. [服务器要求与准备](#1-服务器要求与准备)
2. [基础环境安装](#2-基础环境安装)
3. [数据库配置](#3-数据库配置)
4. [后端部署](#4-后端部署)
5. [前端构建与部署](#5-前端构建与部署)
6. [Nginx 配置](#6-nginx-配置)
7. [域名与 HTTPS 配置](#7-域名与-https-配置)
8. [常见问题排查](#8-常见问题排查)

---

## 1. 服务器要求与准备

### 1.1 推荐配置

| 项目 | 最低配置 | 推荐配置 |
|------|---------|---------|
| CPU | 2 核 | 4 核 |
| 内存 | 4 GB | 8 GB |
| 硬盘 | 40 GB SSD | 100 GB SSD |
| 带宽 | 3 Mbps | 5 Mbps+ |
| 系统 | CentOS 7+ | Ubuntu 22.04 |

### 1.2 开放端口

确保云服务器安全组/防火墙开放以下端口：

| 端口 | 用途 |
|------|------|
| 22 | SSH 远程连接 |
| 80 | HTTP |
| 443 | HTTPS |
| 8080 | 后端 API（可仅内网开放） |
| 3306 | MySQL（建议仅内网） |
| 6379 | Redis（建议仅内网） |

### 1.3 连接服务器

```bash
# Windows 用户可使用 PowerShell 或 PuTTY
ssh root@你的服务器IP

# 创建普通用户（推荐，避免使用 root）
adduser deploy
usermod -aG sudo deploy  # Ubuntu
# 或 usermod -aG wheel deploy  # CentOS
su - deploy
```

---

## 2. 基础环境安装

### 2.1 更新系统

```bash
# Ubuntu/Debian
sudo apt update && sudo apt upgrade -y

# CentOS
sudo yum update -y
```

### 2.2 安装 JDK 17

```bash
# Ubuntu/Debian
sudo apt install -y openjdk-17-jdk

# CentOS
sudo yum install -y java-17-openjdk java-17-openjdk-devel

# 验证
java -version
```

### 2.3 安装 Node.js 18+

```bash
# 使用 NodeSource 安装（推荐）
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install -y nodejs

# 或使用 nvm（更灵活）
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
source ~/.bashrc
nvm install 18
nvm use 18

# 验证
node -v
npm -v
```

### 2.4 安装 Nginx

```bash
# Ubuntu/Debian
sudo apt install -y nginx

# CentOS
sudo yum install -y nginx

# 启动并设置开机自启
sudo systemctl start nginx
sudo systemctl enable nginx
```

### 2.5 安装 MySQL 8.0

```bash
# Ubuntu
sudo apt install -y mysql-server

# 安全配置
sudo mysql_secure_installation

# 创建数据库和用户
sudo mysql -u root -p
```

```sql
-- MySQL 命令
CREATE DATABASE business_reviews CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'reviews'@'localhost' IDENTIFIED BY '你的强密码';
GRANT ALL PRIVILEGES ON business_reviews.* TO 'reviews'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 2.6 安装 Redis

```bash
# Ubuntu/Debian
sudo apt install -y redis-server

# 启动
sudo systemctl start redis-server
sudo systemctl enable redis-server

# 验证
redis-cli ping
# 应返回 PONG
```

---

## 3. 数据库配置

### 3.1 导入数据库

将本地的 SQL 文件上传到服务器：

```bash
# 在本地执行（将文件上传到服务器）
scp merchants.sql root@你的服务器IP:/home/deploy/

# 在服务器上导入
mysql -u reviews -p business_reviews < /home/deploy/merchants.sql
```

### 3.2 创建项目目录

```bash
sudo mkdir -p /opt/business-reviews
sudo chown -R deploy:deploy /opt/business-reviews
cd /opt/business-reviews
mkdir -p backend frontend-h5 frontend-web logs
```

---

## 4. 后端部署

### 4.1 本地打包

在你的 Windows 开发机上执行：

```bash
cd backend-business-reviews

# 打包（跳过测试）
mvn clean package -DskipTests

# 生成的 JAR 文件在：
# backend-business-reviews-web/target/backend-business-reviews-web-1.0.0.jar
```

### 4.2 上传 JAR 包

```bash
# 在本地执行
scp backend-business-reviews-web/target/backend-business-reviews-web-1.0.0.jar deploy@你的服务器IP:/opt/business-reviews/backend/app.jar
```

### 4.3 创建生产环境配置

在服务器上创建配置文件：

```bash
nano /opt/business-reviews/backend/application-prod.yml
```

填入以下内容（根据实际情况修改）：

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/business_reviews?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8mb4
    username: reviews
    password: 你的数据库密码
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  data:
    redis:
      host: localhost
      port: 6379
      password: ""  # 如果设置了密码则填写

# 阿里云 OSS 配置
aliyun:
  oss:
    endpoint: oss-cn-hangzhou.aliyuncs.com
    access-key-id: 你的AccessKeyId
    access-key-secret: 你的AccessKeySecret
    bucket-name: 你的BucketName

# AI 服务配置
langchain4j:
  open-ai:
    chat-model:
      base-url: https://api.deepseek.com
      api-key: 你的DeepSeek-API-Key
      model-name: deepseek-chat
```

### 4.4 创建 Systemd 服务

```bash
sudo nano /etc/systemd/system/business-reviews.service
```

填入：

```ini
[Unit]
Description=Business Reviews Backend API
After=network.target mysql.service redis.service

[Service]
Type=simple
User=deploy
WorkingDirectory=/opt/business-reviews/backend
ExecStart=/usr/bin/java -Xms512m -Xmx1024m -jar app.jar --spring.profiles.active=prod --spring.config.additional-location=./application-prod.yml
Restart=always
RestartSec=10
StandardOutput=append:/opt/business-reviews/logs/backend.log
StandardError=append:/opt/business-reviews/logs/backend-error.log

[Install]
WantedBy=multi-user.target
```

### 4.5 启动后端服务

```bash
sudo systemctl daemon-reload
sudo systemctl start business-reviews
sudo systemctl enable business-reviews

# 查看状态
sudo systemctl status business-reviews

# 查看日志
tail -f /opt/business-reviews/logs/backend.log
```

---

## 5. 前端构建与部署

### 5.1 UniApp H5 构建

在本地开发机上：

```bash
cd front-business-reviews-Mobile

# 修改生产环境配置
nano .env.production
```

```env
VITE_API_BASE_URL=https://你的域名/api
```

```bash
# 构建 H5 版本
npm run build:h5

# 生成的文件在 dist/build/h5 目录
```

### 5.2 Vue 商家端构建

```bash
cd front-business-reviews-Web

# 修改生产环境配置（如果有）
# 确保 API 地址指向正确的后端

# 构建
npm run build

# 生成的文件在 dist 目录
```

### 5.3 上传前端文件

```bash
# 上传 UniApp H5
scp -r dist/build/h5/* deploy@你的服务器IP:/opt/business-reviews/frontend-h5/

# 上传 Vue 商家端
scp -r dist/* deploy@你的服务器IP:/opt/business-reviews/frontend-web/
```

---

## 6. Nginx 配置

### 6.1 创建站点配置

```bash
sudo nano /etc/nginx/sites-available/business-reviews
```

填入以下配置：

```nginx
# HTTP 重定向到 HTTPS（如果有域名和证书）
server {
    listen 80;
    server_name 你的域名.com www.你的域名.com;
    return 301 https://$server_name$request_uri;
}

# 主站点配置
server {
    listen 443 ssl http2;
    server_name 你的域名.com www.你的域名.com;

    # SSL 证书（如果有）
    # ssl_certificate /etc/nginx/ssl/你的域名.pem;
    # ssl_certificate_key /etc/nginx/ssl/你的域名.key;

    # 如果没有证书，先用 HTTP（将 listen 443 改为 listen 80，注释掉 ssl 相关）

    # Gzip 压缩
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml;
    gzip_min_length 1000;

    # UniApp H5 前端（用户端）
    location / {
        root /opt/business-reviews/frontend-h5;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # Vue 商家端
    location /merchant {
        alias /opt/business-reviews/frontend-web;
        index index.html;
        try_files $uri $uri/ /merchant/index.html;
    }

    # API 反向代理
    location /api {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # WebSocket 支持（如果需要）
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        
        # SSE 流式响应支持
        proxy_buffering off;
        proxy_cache off;
        proxy_read_timeout 86400;
    }

    # 静态文件缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 30d;
        add_header Cache-Control "public, immutable";
    }
}
```

### 6.2 仅使用 IP（无域名）的简化配置

如果暂时没有域名，使用以下配置：

```bash
sudo nano /etc/nginx/sites-available/business-reviews
```

```nginx
server {
    listen 80;
    server_name _;  # 匹配所有

    # UniApp H5
    location / {
        root /opt/business-reviews/frontend-h5;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # Vue 商家端
    location /merchant/ {
        alias /opt/business-reviews/frontend-web/;
        index index.html;
        try_files $uri $uri/ /merchant/index.html;
    }

    # API 反向代理
    location /api/ {
        proxy_pass http://127.0.0.1:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_buffering off;
    }
}
```

### 6.3 启用配置

```bash
# Ubuntu/Debian
sudo ln -s /etc/nginx/sites-available/business-reviews /etc/nginx/sites-enabled/
sudo rm /etc/nginx/sites-enabled/default  # 删除默认配置

# CentOS（配置文件在 /etc/nginx/conf.d/）
sudo mv /etc/nginx/sites-available/business-reviews /etc/nginx/conf.d/business-reviews.conf

# 测试配置
sudo nginx -t

# 重载 Nginx
sudo systemctl reload nginx
```

---

## 7. 域名与 HTTPS 配置

### 7.1 配置域名解析

在你的域名服务商控制台添加 A 记录：

| 主机记录 | 类型 | 记录值 |
|---------|------|-------|
| @ | A | 你的服务器IP |
| www | A | 你的服务器IP |

### 7.2 申请免费 SSL 证书（Let's Encrypt）

```bash
# 安装 Certbot
sudo apt install -y certbot python3-certbot-nginx

# 自动申请并配置证书
sudo certbot --nginx -d 你的域名.com -d www.你的域名.com

# 设置自动续期
sudo crontab -e
# 添加以下行：
0 0 1 * * /usr/bin/certbot renew --quiet
```

---

## 8. 常见问题排查

### 8.1 后端启动失败

```bash
# 查看详细日志
journalctl -u business-reviews -f

# 常见原因：
# 1. 数据库连接失败 - 检查 MySQL 是否启动，用户名密码是否正确
# 2. 端口被占用 - netstat -tlnp | grep 8080
# 3. 内存不足 - free -h
```

### 8.2 前端页面空白

```bash
# 检查文件是否存在
ls -la /opt/business-reviews/frontend-h5/

# 检查 Nginx 配置
sudo nginx -t

# 查看 Nginx 错误日志
sudo tail -f /var/log/nginx/error.log
```

### 8.3 API 请求 502/504

```bash
# 检查后端是否运行
sudo systemctl status business-reviews

# 检查端口是否监听
netstat -tlnp | grep 8080

# 检查防火墙
sudo ufw status  # Ubuntu
sudo firewall-cmd --list-all  # CentOS
```

### 8.4 图片上传失败

1. 检查阿里云 OSS 配置是否正确
2. 确保 OSS Bucket 的跨域访问 (CORS) 配置允许你的域名
3. 检查 AccessKey 权限

---

## 🎉 部署完成检查清单

- [ ] MySQL 数据库已创建并导入数据
- [ ] Redis 服务正常运行
- [ ] 后端 JAR 包已上传并启动
- [ ] 后端日志无报错
- [ ] UniApp H5 前端已构建并上传
- [ ] Vue 商家端已构建并上传
- [ ] Nginx 配置已生效
- [ ] 通过 IP/域名 可正常访问用户端
- [ ] 通过 IP/域名/merchant 可正常访问商家端
- [ ] API 接口正常响应（登录、获取数据等）
- [ ] SSL 证书已配置（可选）

---

## 📞 访问地址

部署完成后，你的系统可通过以下地址访问：

| 端 | 地址 |
|----|------|
| 用户端 (H5) | `http://你的域名/` 或 `http://服务器IP/` |
| 商家端 | `http://你的域名/merchant/` 或 `http://服务器IP/merchant/` |
| API | `http://你的域名/api/` |

---

**祝你部署顺利！如有问题，请检查日志并逐步排查。** 🚀
