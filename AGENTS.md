# AGENTS.md

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
├── pages/                        # 微信小程序页面（19个）
│   ├── station/index             # 驿站首页（推荐卡片入口）
│   ├── music-list/index          # 音乐疗愈（歌单列表）
│   ├── self-healing-list/index   # 自我疗愈（练习列表）
│   ├── self-healing-detail/index # 自我疗愈详情（计时器/进度环/庆祝）
│   ├── article-list/index        # 心理文章列表
│   ├── article-detail/index      # 心理文章详情
│   ├── consult/index             # 咨询中心（AI+量表入口）
│   ├── topic-list/index          # 树洞社区
│   ├── topic-detail/index        # 帖子详情+回复
│   ├── ai-chat/index             # AI 咨询聊天
│   ├── quiz-list/index           # 量表列表
│   ├── quiz-detail/index         # 量表答题
│   ├── quiz-result/index         # 测评结果
│   ├── login/index               # 微信登录
│   ├── profile/index             # 个人中心
│   ├── notice-center/index       # 通知中心
│   ├── my-quiz-history/index     # 我的测评历史
│   └── webview/index             # webview（未备案不可用）
├── components/                   # 公共组件
│   ├── bottom-nav/               # 底部导航栏
│   ├── chat-avatar/              # 聊天头像
│   └── minimal-back/             # 极简返回按钮
├── utils/                        # 前端工具
│   ├── config.js                 # API 地址配置
│   ├── request.js                # 请求封装（自动注入 JWT）
│   ├── auth.js                   # 认证工具（ensurePageLogin）
│   ├── selfHealingCatalog.js     # 疗愈工具（适配器+完成状态）
│   ├── stationNormalize.js       # 驿站数据标准化
│   ├── quizCatalog.js            # 量表目录
│   ├── quizNormalize.js          # 量表数据标准化
│   └── topic.js                  # 树洞工具
├── src/main/java/com/example/demo/
│   ├── DemoApplication.java      # 主入口 (@MapperScan 已配置)
│   ├── controller/               # REST 控制器
│   │   ├── LoginController.java  # 登录（code2session + JWT）
│   │   ├── HealthController.java # 健康检查
│   │   ├── UserController.java   # 用户信息
│   │   ├── station/              # 驿站内容（文章/音乐/歌单/疗愈）
│   │   ├── topic/                # 树洞帖子
│   │   └── reply/                # 帖子回复
│   ├── module/consult/           # AI 咨询模块（独立子系统）
│   │   ├── controller/           # 咨询专用控制器
│   │   ├── client/               # AI API 客户端
│   │   ├── inference/            # 推理逻辑
│   │   ├── safety/               # 安全检测
│   │   ├── dto/                  # 咨询专用 DTO
│   │   ├── entity/               # 咨询专用实体
│   │   ├── mapper/               # 咨询专用 Mapper
│   │   └── service/              # 咨询专用 Service
│   ├── service/                  # 业务逻辑
│   ├── mapper/                   # MyBatis-Plus Mapper
│   ├── entity/                   # 实体类
│   ├── dto/                      # 数据传输对象
│   ├── config/                   # 配置类（WebConfig等）
│   ├── interceptor/              # JWT 拦截器
│   └── common/                   # 公共类（统一响应等）
├── sql/                          # 数据库脚本（30个）
│   ├── graduation_design.sql     # 主建表脚本
│   ├── music_playlist.sql        # 歌单表
│   ├── self_healing.sql          # 自我疗愈表
│   └── station_seed_data.sql     # 驿站种子数据
└── pom.xml                       # Maven 配置
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
- Swagger UI 白名单: `/swagger-ui/**`, `/v3/api-docs/**`

## 数据库

- 数据库名: `graduation_design` (utf8mb4)
- 主建表脚本: `sql/graduation_design.sql`
- 增量迁移: `sql/` 目录下各脚本（手动执行，无迁移工具）

### 核心实体

| 实体 | 表 | 说明 |
|------|-----|------|
| User | user | 用户（openid/昵称/头像） |
| Article | article | 心理文章（markdown内容） |
| Music | music | 音乐资源 |
| MusicPlaylist | music_playlist | 歌单（含歌曲ID数组） |
| SelfHealing | self_healing | 自我疗愈练习 |
| Topic | topic | 树洞帖子 |
| Reply | reply | 帖子回复 |

## 前端 CSS 架构

`app.wxss` 定义全局样式：
- CSS 变量: `--space-page-x`, `--space-card-padding`, `--space-block-gap` (均为 15rpx)
- `.card` - 通用卡片（白色背景+圆角+阴影）
- `.hero-card` - 统一 Hero 卡片（渐变背景+边框+阴影）
- `.hero-title` / `.hero-desc` - Hero 卡片标题/描述（居中+文字阴影）

各页面只需覆盖渐变色，例如：
```css
.hero-card { background: linear-gradient(135deg, #6eaa8a, #82b79a); }
```

## 微信小程序注意事项

- `border-radius` 会创建裁切上下文，`overflow:visible` 不一定生效
- `<button>` 组件有默认内边距，需用 `::after { border: none }` + `display:flex` 居中
- 未备案小程序不可使用 `web-view` 组件
- 真机调试需手机与电脑在同一局域网

## AI 咨询模块

`module/consult/` 是独立子系统，有自己的 controller/entity/mapper/service：
- 外部 AI API 调用（通过 `ai.api.*` 配置）
- 安全检测（`safety` 包）
- 推理逻辑（`inference` 包）
- 最大 token: 700，温度: 0.4（演示场景优化）

## 开发流程约定

- 后端修改后运行 `mvn compile` + `mvn test` 验证
- 前端修改后需在微信开发者工具中刷新预览
- 数据库变更需在 `sql/` 目录创建增量脚本
