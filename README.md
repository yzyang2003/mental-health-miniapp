# 大学生心理健康咨询小程序

这是一个基于 **Spring Boot + 微信小程序** 开发的校园心理健康支持系统，面向大学生心理自助支持场景，提供轻量化、低门槛的线上心理服务能力。

项目主要围绕以下需求展开：

- 微信登录与身份鉴权
- 心理健康资源浏览
- 匿名树洞发帖与回复
- 心理测评与历史记录查询
- AI 心理咨询与延伸交流
- 个人资料管理

## 一、技术栈

### 1. 前端

- 微信小程序原生框架
- WXML / WXSS / JavaScript

### 2. 后端

- Spring Boot 3
- MyBatis-Plus
- MySQL
- JWT
- springdoc-openapi

## 二、主要功能模块

### 1. 小程序端页面

- `pages/login`：微信登录
- `pages/station`：心灵驿站
- `pages/topic-list` / `pages/topic-detail`：树洞社区
- `pages/quiz-list` / `pages/quiz-detail` / `pages/quiz-result`：心理测评
- `pages/ai-chat`：AI 咨询
- `pages/profile`：个人中心

### 2. 后端核心能力

- 登录鉴权与 Token 校验
- 文章、音乐、自我疗愈资源服务
- 树洞帖子与回复服务
- 问卷、题目、测评结果、聊天记录服务
- AI 对话调用与上下文组织

## 三、项目结构

```text
.
- app.js / app.json / app.wxss          # 小程序根配置
- pages/                                # 小程序页面
- components/                           # 公共组件
- utils/                                # 前端请求与配置工具
- src/main/java/com/example/demo/       # Spring Boot 后端代码
- src/main/resources/                   # 后端配置文件
- src/test/java/                        # 后端测试代码
- sql/                                  # 数据库脚本
- pom.xml                               # Maven 配置文件
```

## 四、快速启动

### 1. 准备数据库

先创建数据库，例如：

```sql
CREATE DATABASE graduation_design DEFAULT CHARACTER SET utf8mb4;
```

然后根据本地环境导入 `sql/` 目录下的表结构和初始化脚本。

### 2. 配置后端本地参数

将以下文件：

```text
src/main/resources/application-local.yml.example
```

复制为：

```text
src/main/resources/application-local.yml
```

然后填写你本地实际使用的配置，包括：

- MySQL 密码
- JWT 密钥
- 微信小程序 `appid` / `secret`
- AI 接口地址、密钥和模型名称

注意：

- `application-local.yml` 只用于本地开发
- 该文件已经加入忽略规则，不要提交到 Git

### 3. 启动后端

运行环境要求：

- JDK 17
- Maven 3.9+
- MySQL 8+

启动命令：

```bash
mvn spring-boot:run
```

默认端口：

```text
8080
```

启动成功后可访问：

- 接口文档：`http://localhost:8080/swagger-ui.html`
- OpenAPI 描述：`http://localhost:8080/v3/api-docs`

### 4. 配置小程序端

使用微信开发者工具打开本项目后，需要重点注意以下内容：

- `project.private.config.json` 不会提交到仓库
- 后端请求地址由 `utils/config.js` 统一管理
- 真机调试时，可能需要将 `utils/config.js` 中的 `LAN_HOST` 改为你当前电脑的局域网 IPv4 地址

### 5. 运行小程序

后端启动并完成本地配置后，即可在微信开发者工具中进行预览和调试。

如果需要真机联调，请确保：

- 手机与电脑处于同一局域网
- 后端服务已正常启动
- 小程序端请求地址配置正确

## 五、测试说明

当前后端已包含部分控制器级测试，例如：

- `HealthControllerTest`
- `ArticleControllerTest`
- `ConsultChatControllerStandaloneTest`

运行测试命令：

```bash
mvn test
```

## 六、安全说明

仓库当前已排除以下本地敏感或无须公开的内容：

- 本地配置文件 `application-local.yml`
- 微信开发者私有配置 `project.private.config.json`
- 论文、录屏、文档中间产物
- 本地虚拟环境、构建输出目录、临时目录

在继续开发或部署前，建议再次核对：

- 数据库连接信息
- 微信 `AppSecret`
- AI 接口密钥
- 小程序端请求地址配置

## 七、项目现状

当前仓库已经包含完整的毕业设计项目代码，覆盖：

- 小程序端页面与交互
- Spring Boot 后端接口
- 数据库脚本
- 基础测试代码

适用于以下场景：

- 毕业设计展示
- 项目演示
- 代码审阅
- 在当前基础上继续二次开发
