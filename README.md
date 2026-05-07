# 大学生心理健康咨询小程序

基于 **Spring Boot 3 + 微信小程序 + Vue 3 管理后台** 的校园心理健康支持系统，面向大学生心理自助支持场景，提供轻量化、低门槛的线上心理服务能力。

## 项目特点

- **AI 智能咨询**：基于外部大模型的 AI 心理咨询，支持情绪识别、危机干预、专业咨询技能
- **树洞社区**：匿名发帖与回复，保护隐私的同时释放压力
- **心理测评**：PHQ-9、GAD-7、SCL-90、SDS、SAS 等专业量表
- **自我疗愈**：正念冥想、呼吸练习、情绪记录等自助工具
- **管理后台**：Vue 3 + Element Plus 可视化管理，支持数据统计、内容管理、用户管理

## 技术栈

| 层级 | 技术 |
|------|------|
| 小程序端 | 微信原生框架 (WXML / WXSS / JavaScript) |
| 管理后台 | Vue 3 + Vite 5 + Element Plus |
| 后端框架 | Spring Boot 3.3.5 |
| ORM | MyBatis-Plus 3.5.7 |
| 数据库 | MySQL 8 |
| 认证 | JWT (jjwt 0.12.6) |
| 接口文档 | springdoc-openapi (Swagger UI) |
| AI 引擎 | 外部大模型 API（可配置） |
| 语音服务 | MiMo ASR / TTS API |

## 功能模块

### 小程序端

| 模块 | 页面 | 功能 |
|------|------|------|
| 心灵驿站 | `pages/station` | 首页入口，文章、音乐、疗愈资源推荐 |
| AI 咨询 | `pages/ai-chat` | AI 心理咨询对话，支持文字/语音输入，流式输出 |
| 树洞社区 | `pages/topic-list` / `pages/topic-detail` | 匿名发帖、评论、点赞 |
| 心理测评 | `pages/quiz-list` / `pages/quiz-detail` / `pages/quiz-result` | 量表测评、结果分析、历史记录 |
| 自我疗愈 | `pages/self-healing-list` / `pages/self-healing-detail` | 正念冥想、呼吸练习、情绪记录 |
| 心理文章 | `pages/article-list` / `pages/article-detail` | 心理健康知识科普 |
| 音乐疗法 | `pages/music-list` | 放松音乐、白噪音 |
| 通知中心 | `pages/notice-center` | 系统通知、测评提醒 |
| 个人中心 | `pages/login` / `pages/profile` | 微信登录、个人资料、测评历史 |

### 管理后台

| 模块 | 功能 |
|------|------|
| 仪表盘 | 用户趋势、日活统计、问卷分析、情绪分布等 6 项数据可视化 |
| 文章管理 | 文章增删改查，支持富文本编辑、URL 一键抓取 |
| 音乐管理 | 音乐资源上传、分类管理 |
| 疗愈管理 | 自我疗愈内容配置 |
| 话题管理 | 树洞帖子审核、内容管理 |
| 问卷管理 | 问卷题目配置、结果统计 |
| 用户管理 | 用户列表、状态管理、聊天记录查看 |
| 通知管理 | 系统通知发布 |
| AI 配置 | AI 模型参数、提示词配置 |

### AI 咨询引擎

| 能力 | 说明 |
|------|------|
| 情绪识别 | 7 种情绪类型识别（快乐、悲伤、焦虑、愤怒、恐惧、中性、惊讶） |
| 危机评估 | 4 级危机等级评估，自动触发危机干预流程 |
| 内容安全 | 关键词 + 语义双重安全检测 |
| 咨询技能 | 基础咨询、专业咨询、校园场景、危机干预、测评推荐、疗愈推荐 |
| 咨询推荐 | 根据情绪状态智能推荐心理测评和自我疗愈练习 |

## 项目结构

```
├── pages/                          # 微信小程序页面 (19 个页面)
├── components/                     # 公共组件
├── utils/                          # 前端工具模块 (请求、认证、流式处理等)
├── admin/                          # Vue 3 管理后台
│   ├── src/
│   │   ├── api/                    # 接口请求
│   │   ├── views/                  # 页面组件
│   │   ├── components/             # 通用组件
│   │   ├── router/                 # 路由配置
│   │   └── stores/                 # 状态管理
│   └── dist/                       # 构建产物
├── src/main/java/com/example/demo/
│   ├── controller/                 # REST 控制器
│   ├── controller/admin/           # Admin 接口
│   ├── module/consult/             # AI 咨询模块 (独立子系统)
│   │   ├── client/                 # AI API 客户端
│   │   ├── emotion/                # 情绪识别
│   │   ├── safety/                 # 内容安全
│   │   └── skills/                 # 咨询技能
│   ├── service/                    # 业务服务
│   ├── entity/                     # 数据实体
│   ├── mapper/                     # MyBatis Mapper
│   └── config/                     # 配置类
├── src/test/java/                  # 单元测试
├── sql/                            # 数据库脚本
└── pom.xml                         # Maven 配置
```

## 快速启动

### 环境要求

- JDK 17
- Maven 3.9+
- MySQL 8+
- Node.js 18+ (管理后台开发)
- 微信开发者工具

### 1. 准备数据库

```sql
CREATE DATABASE graduation_design DEFAULT CHARACTER SET utf8mb4;
```

导入表结构和初始数据：

```bash
mysql -u root -p graduation_design < sql/graduation_design.sql
mysql -u root -p graduation_design < sql/admin_migration.sql
```

### 2. 配置后端

```bash
# 复制配置模板
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
```

编辑 `application-local.yml`，填写：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/graduation_design?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8mb4
    username: root
    password: 你的MySQL密码

jwt:
  secret: 你的JWT密钥

wechat:
  appid: 你的小程序AppID
  secret: 你的小程序AppSecret

ai:
  api:
    url: AI接口地址
    key: AI接口密钥
    model: 模型名称
```

### 3. 启动后端

```bash
# 使用项目内置 Maven
./.tools/apache-maven-3.9.9/bin/mvn spring-boot:run
```

后端启动后：

- API 服务：`http://localhost:8080`
- Swagger UI：`http://localhost:8080/swagger-ui.html`
- OpenAPI：`http://localhost:8080/v3/api-docs`

### 4. 启动管理后台

```bash
cd admin
npm install
npm run dev
```

管理后台地址：`http://localhost:5173`

### 5. 运行小程序

使用微信开发者工具打开项目根目录，即可预览调试。

真机调试需确保：
- 手机与电脑在同一局域网
- 修改 `utils/config.js` 中的 `LAN_HOST` 为电脑局域网 IP

## 测试

```bash
# 运行全部测试 (146 个测试用例，约 10 秒)
./.tools/apache-maven-3.9.9/bin/mvn test
```

测试覆盖：
- 控制器层：HealthController、ArticleController、ConsultChatController
- AI 咨询模块：情绪识别、危机评估、内容安全、咨询技能
- 独立 MockMvc 测试，无需启动 Spring 容器

## 接口文档

后端启动后访问 Swagger UI 查看完整接口文档：

```
http://localhost:8080/swagger-ui.html
```

主要接口分组：
- `/api/login` - 微信登录
- `/api/user` - 用户信息
- `/api/station/*` - 首页资源
- `/api/topic/*` - 树洞社区
- `/api/quiz/*` - 心理测评
- `/api/consult/*` - AI 咨询
- `/api/admin/*` - 管理后台接口

## 安全说明

以下敏感文件已加入 `.gitignore`，不会提交到仓库：

- `application-local.yml` - 本地数据库、JWT、API 密钥配置
- `project.private.config.json` - 微信开发者私有配置
- `admin/node_modules/` - 前端依赖
- `uploads/` - 用户上传文件

部署前请检查：
- 数据库连接信息
- 微信 AppSecret
- AI 接口密钥
- JWT 密钥强度

## 项目适用场景

- 毕业设计展示与答辩
- 心理健康类小程序原型参考
- Spring Boot + 微信小程序全栈开发学习
- AI 对话系统集成参考
- Vue 3 + Element Plus 管理后台开发参考

## License

本项目仅供学习交流使用。
