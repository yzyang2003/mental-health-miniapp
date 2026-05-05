# CLAUDE.md

## 项目概述

校园心理健康咨询微信小程序 + Spring Boot 3 后端（毕业设计）。

## 技术栈

- **前端**: 微信小程序原生框架 (WXML/WXSS/JavaScript)
- **后端**: Spring Boot 3.3.5 + MyBatis-Plus + MySQL 8 + JWT (jjwt 0.12.6) + springdoc-openapi
- **Java**: JDK 17

## 核心命令

```bash
# 启动后端（需先启动 MySQL）
mvn spring-boot:run

# 运行测试
mvn test

# 后端运行端口: 8080
# 接口文档: http://localhost:8080/swagger-ui.html
```

## 项目结构

```
├── pages/                    # 微信小程序页面
├── utils/config.js           # 前端 API 地址配置
├── src/main/java/com/example/demo/
│   ├── DemoApplication.java  # 主入口 (@MapperScan 已配置)
│   ├── controller/           # REST 控制器
│   ├── module/consult/       # AI 咨询 + 测评模块
│   ├── service/              # 业务逻辑
│   ├── mapper/               # MyBatis-Plus Mapper
│   └── entity/               # 实体类
├── sql/graduation_design.sql # 完整数据库建表 + 初始数据
└── pom.xml                   # Maven 配置
```

## 环境配置

1. 复制 `src/main/resources/application-local.yml.example` → `application-local.yml`
2. 填写: MySQL 密码、JWT 密钥、微信 appid/secret、AI 接口密钥
3. `application-local.yml` 已被 gitignore，切勿提交

## 前端 API 地址配置

`utils/config.js` 优先级:
1. `FORCE_BASE_URL`（硬编码，真机调试用）
2. `wx.getStorageSync('apiBaseUrlOverride')`（通过微信控制台设置）
3. 模拟器 → `127.0.0.1:8080`；真机 → `LAN_HOST:8080`

真机联调时需将 `LAN_HOST` 改为你电脑的局域网 IPv4 地址。

## 测试模式

测试使用独立 MockMvc（不加载 Spring 上下文）:
```java
MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
```

## 安全说明

- `project.private.config.json` - 微信开发者工具私有配置（已 gitignore）
- `uploads/` - 用户上传的图片（已 gitignore）
- JWT 拦截器保护除 `/api/health` 和 `/api/login` 外的所有接口

## 数据库

- 数据库名: `graduation_design` (utf8mb4)
- 主建表脚本: `sql/graduation_design.sql`
- 增量迁移: `sql/` 目录下各脚本（手动执行，无迁移工具）

## 主要页面模块

| 模块 | 路径 | 功能 |
|------|------|------|
| 心灵驿站 | `pages/station/` | 首页，文章/音乐/自我疗愈入口 |
| 心理文章 | `pages/article-list/` | 文章列表，支持分类/搜索/收藏 |
| 音乐疗愈 | `pages/music-list/` | 音乐歌单列表 |
| 自我疗愈 | `pages/self-healing-list/` | 练习列表 |
| 咨询支持 | `pages/consult/` | 咨询首页，测评/AI咨询入口 |
| 心理测评 | `pages/quiz-list/` | 问卷列表 |
| AI 咨询 | `pages/ai-chat/` | 与AI"小爱"对话 |
| 树洞社区 | `pages/topic-list/` | 匿名发帖/回复 |
| 个人中心 | `pages/profile/` | 用户资料管理 |
