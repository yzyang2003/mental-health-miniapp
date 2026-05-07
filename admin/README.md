# 管理后台（Admin）— 校园心理健康咨询小程序内容维护系统

> 本项目是「大学生心理健康咨询微信小程序」的前端管理后台，用于维护小程序端的内容更新、用户管理和数据监控。  
> 技术栈：**Vue 3 + Vite 5 + Element Plus + Pinia + Vue Router 4 + Axios**

---

## 一、技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.x | 使用 `<script setup>` 语法 |
| Vite | 5.x | 构建工具 |
| Element Plus | 最新 | UI 组件库 |
| Pinia | 最新 | 状态管理 |
| Vue Router | 4.x | 路由（含登录守卫）|
| Axios | 最新 | HTTP 请求 |
| @wangeditor/editor-for-vue | 最新 | 富文本编辑器（文章编辑用）|
| ECharts | 5.x | Dashboard 图表 |

## 二、启动命令

```bash
cd admin
npm create vite@latest . -- --template vue   # 首次初始化（或直接按下方手动创建）
npm install
npm install element-plus @element-plus/icons-vue
npm install vue-router@4 pinia axios
npm install @wangeditor/editor @wangeditor/editor-for-vue
npm install echarts
npm run dev                                     # 启动开发服务器，默认端口 5173
npm run build                                   # 构建生产版本
```

> Vite 开发服务器会自动将 `/api` 请求代理到 `http://localhost:8080`（后端 Spring Boot），无需处理跨域。

## 三、项目结构

```
admin/
├── public/
├── src/
│   ├── api/                          # 接口定义（每个模块一个文件）
│   │   ├── request.js                # Axios 实例封装（baseURL、token 拦截器、错误处理）
│   │   ├── admin.js                  # POST /api/admin/login, GET /api/admin/info
│   │   ├── dashboard.js              # GET /api/admin/dashboard/stats
│   │   ├── article.js                # 文章 CRUD + 分页
│   │   ├── music.js                  # 音乐管理
│   │   ├── healing.js                # 自愈练习管理
│   │   ├── topic.js                  # 树洞帖子管理（含审核）
│   │   ├── quiz.js                   # 问卷 + 题目管理
│   │   ├── user.js                   # 用户列表
│   │   ├── chat.js                   # AI 对话记录
│   │   └── notice.js                 # 公告管理
│   ├── views/                        # 页面组件
│   │   ├── Login/
│   │   │   └── index.vue             # 登录页（用户名+密码表单）
│   │   ├── Layout/
│   │   │   └── index.vue             # 主布局：el-aside 侧边栏 + el-header + el-main
│   │   ├── Dashboard/
│   │   │   └── index.vue             # 数据概览：4个统计卡片 + ECharts 趋势图
│   │   ├── Article/
│   │   │   ├── List.vue              # 文章列表（el-table + 搜索 + 分页）
│   │   │   └── Edit.vue              # 文章编辑（富文本编辑器）
│   │   ├── Music/
│   │   │   ├── List.vue              # 音乐列表
│   │   │   └── Edit.vue              # 音乐编辑（弹窗或抽屉）
│   │   ├── Healing/
│   │   │   ├── List.vue              # 自愈练习列表
│   │   │   └── Edit.vue              # 自愈练习编辑
│   │   ├── Topic/
│   │   │   ├── List.vue              # 树洞帖子列表（含状态筛选）
│   │   │   └── Detail.vue            # 帖子详情弹窗（内容+图片+回复）
│   │   ├── Quiz/
│   │   │   ├── List.vue              # 问卷列表
│   │   │   └── Questions.vue         # 题目编辑（按问卷查看所有题目）
│   │   ├── User/
│   │   │   └── List.vue              # 用户列表（含详情跳转）
│   │   ├── Chat/
│   │   │   └── List.vue              # AI 对话记录（按用户分组，聊天气泡样式）
│   │   ├── Notice/
│   │   │   ├── List.vue              # 公告列表
│   │   │   └── Edit.vue              # 公告编辑
│   │   └── AiConfig/
│   │       └── index.vue             # AI 系统提示词/情绪关键词配置
│   ├── components/                   # 公共可复用组件
│   │   ├── UploadImage.vue           # 图片上传组件（调用后端 POST /upload）
│   │   └── RichTextEditor.vue        # 富文本编辑器封装（wangeditor）
│   ├── router/
│   │   └── index.js                  # 路由配置（含 beforeEach 登录守卫）
│   ├── stores/
│   │   └── user.js                   # Pinia store：token、adminInfo
│   ├── utils/
│   │   └── auth.js                   # localStorage 读写 token 工具函数
│   ├── styles/
│   │   └── global.scss               # 全局样式（可选，Element Plus 已够用）
│   ├── App.vue
│   └── main.js                       # 挂载 ElementPlus、Router、Pinia
├── index.html
├── vite.config.js                    # 关键：proxy 配置 /api → localhost:8080
└── package.json
```

## 四、路由设计

```js
// src/router/index.js 核心结构

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/views/Layout/index.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard',   name: 'Dashboard',   component: () => import('@/views/Dashboard/index.vue'),   meta: { title: '数据概览' } },
      { path: 'article',     name: 'ArticleList', component: () => import('@/views/Article/List.vue'),      meta: { title: '文章管理' } },
      { path: 'article/edit/:id?', name: 'ArticleEdit', component: () => import('@/views/Article/Edit.vue'), meta: { title: '文章编辑' } },
      { path: 'music',       name: 'MusicList',   component: () => import('@/views/Music/List.vue'),        meta: { title: '音乐管理' } },
      { path: 'healing',     name: 'HealingList', component: () => import('@/views/Healing/List.vue'),      meta: { title: '自愈练习管理' } },
      { path: 'topic',       name: 'TopicList',   component: () => import('@/views/Topic/List.vue'),        meta: { title: '树洞审核' } },
      { path: 'quiz',        name: 'QuizList',    component: () => import('@/views/Quiz/List.vue'),         meta: { title: '问卷管理' } },
      { path: 'quiz/:id/questions', name: 'QuizQuestions', component: () => import('@/views/Quiz/Questions.vue'), meta: { title: '题目编辑' } },
      { path: 'user',        name: 'UserList',    component: () => import('@/views/User/List.vue'),         meta: { title: '用户管理' } },
      { path: 'chat',        name: 'ChatList',    component: () => import('@/views/Chat/List.vue'),         meta: { title: '对话记录' } },
      { path: 'notice',      name: 'NoticeList',  component: () => import('@/views/Notice/List.vue'),       meta: { title: '公告管理' } },
      { path: 'ai-config',   name: 'AiConfig',    component: () => import('@/views/AiConfig/index.vue'),    meta: { title: 'AI配置' } },
    ]
  }
]
```

**登录守卫逻辑：**
```js
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('admin_token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})
```

## 五、Axios 封装（api/request.js）

```js
import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// 请求拦截器：自动注入 token
request.interceptors.request.use(config => {
  const token = localStorage.getItem('admin_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：统一错误处理
request.interceptors.response.use(
  response => response.data,
  error => {
    const status = error.response?.status
    if (status === 401) {
      localStorage.removeItem('admin_token')
      window.location.href = '/login'
    } else {
      ElMessage.error(error.response?.data?.message || '请求失败')
    }
    return Promise.reject(error)
  }
)

export default request
```

## 六、Vite 代理配置（vite.config.js）

```js
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: { '@': path.resolve(__dirname, 'src') }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      }
    }
  }
})
```

## 七、后端需要提供的接口清单

> 以下接口均需要 Spring Boot 后端提供，前缀统一为 `/api/admin/`，均需 JWT 认证（`role=admin`）。

### 7.1 管理员认证

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/admin/login` | 登录（username + password），返回 `{ token, admin: {...} }` |
| GET | `/api/admin/info` | 获取当前登录管理员信息 |

### 7.2 数据统计

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/dashboard/stats` | 返回总用户数、今日新增、文章数、帖子数、测评次数、对话消息数、近7天趋势、情绪分布 |

### 7.3 文章管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/article/list` | 分页列表（params: page, size, keyword, status）|
| GET | `/api/admin/article/{id}` | 文章详情（含 content）|
| POST | `/api/admin/article` | 新建文章 |
| PUT | `/api/admin/article/{id}` | 编辑文章 |
| PUT | `/api/admin/article/{id}/status` | 修改状态（上/下架）|
| DELETE | `/api/admin/article/{id}` | 删除文章 |

### 7.4 音乐管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/music/list` | 分页列表 |
| POST | `/api/admin/music` | 新增 |
| PUT | `/api/admin/music/{id}` | 编辑 |
| DELETE | `/api/admin/music/{id}` | 删除 |

### 7.5 自愈练习管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/healing/list` | 分页列表 |
| POST | `/api/admin/healing` | 新增 |
| PUT | `/api/admin/healing/{id}` | 编辑 |
| DELETE | `/api/admin/healing/{id}` | 删除 |

### 7.6 树洞帖子管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/topic/list` | 分页列表（params: page, size, status）|
| GET | `/api/admin/topic/{id}` | 帖子详情（含回复列表）|
| PUT | `/api/admin/topic/{id}/status` | 审核（1=通过，2=删除）|
| GET | `/api/admin/topic/{id}/replies` | 该帖子的回复列表 |
| PUT | `/api/admin/reply/{id}/status` | 删除回复 |

### 7.7 问卷管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/quiz/list` | 问卷列表（含题目数统计）|
| GET | `/api/admin/quiz/{id}` | 问卷详情 |
| POST | `/api/admin/quiz` | 新增问卷 |
| PUT | `/api/admin/quiz/{id}` | 编辑问卷 |
| PUT | `/api/admin/quiz/{id}/status` | 启用/停用 |
| GET | `/api/admin/quiz/{id}/questions` | 获取某问卷的所有题目 |
| POST | `/api/admin/quiz/{id}/questions` | 批量保存题目（新增+编辑+删除）|

### 7.8 用户管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/user/list` | 分页列表（params: page, size, keyword）|
| GET | `/api/admin/user/{openid}` | 用户详情（含测评记录、对话数）|
| GET | `/api/admin/user/{openid}/quizzes` | 该用户的测评记录 |
| GET | `/api/admin/user/{openid}/chats` | 该用户的对话记录 |

### 7.9 对话记录

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/chat/list` | 全局对话列表（params: page, size, openid, keyword, startDate, endDate）|
| GET | `/api/admin/chat/user/{openid}` | 某用户的完整对话记录（聊天气泡展示用）|

### 7.10 公告管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/notice/list` | 公告列表 |
| POST | `/api/admin/notice` | 新增 |
| PUT | `/api/admin/notice/{id}` | 编辑 |
| DELETE | `/api/admin/notice/{id}` | 删除 |

### 7.11 文件上传

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/upload` | 上传图片/音频，返回 `{ url: "...", filename: "..." }` |

> 后端已有该接口，管理后台的 `UploadImage.vue` 组件直接调用即可。

## 八、各页面详细设计

### 8.1 登录页 (`/login`)

- 简洁居中表单：用户名输入框 + 密码输入框 + 登录按钮
- 调用 `POST /api/admin/login`，成功后将 `token` 存入 `localStorage('admin_token')`，跳转 `/dashboard`
- 错误时 ElMessage 提示

### 8.2 主布局 (`Layout/index.vue`)

```
┌────────────┬──────────────────────────┐
│  el-aside  │        el-main           │
│  侧边栏    │                          │
│            │     <router-view />      │
│  Logo     │                          │
│  菜单导航  │                          │
│            ├──────────────────────────┤
│  数据概览  │                          │
│  文章管理  │                          │
│  音乐管理  │                          │
│  自愈练习  │                          │
│  树洞审核  │                          │
│  问卷管理  │                          │
│  用户管理  │                          │
│  对话记录  │                          │
│  公告管理  │                          │
│  AI配置    │                          │
└────────────┴──────────────────────────┘
```

- 侧边栏用 `el-menu`（可折叠）
- 右上角显示管理员昵称 + 退出登录按钮

### 8.3 Dashboard (`/dashboard`)

**顶部四个统计卡片（el-row + el-col）：**
- 总用户数（el-icon-user + 数字）
- 文章总数（el-icon-document + 数字）
- 测评次数（el-icon-reading + 数字）
- 对话消息数（el-icon-chat-dot-round + 数字）

**下方两个图表：**
- 左：近 7 天测评趋势（ECharts 折线图）
- 右：情绪分布（ECharts 饼图，数据来自 chat_history 情绪识别）

### 8.4 文章管理 (`/article`)

**列表页：**
- 搜索栏：关键词输入框 + 状态下拉（全部/已发布/草稿）+ 查询按钮
- el-table：ID、标题、封面缩略图（40x40）、创建时间、状态（el-tag 区分）、操作列
- 操作列：「编辑」（跳转编辑页）、上/下架（el-switch）、「删除」（el-popconfirm 确认）
- 底部 el-pagination 分页

**编辑页：**
- el-form：标题（el-input）、摘要（el-input textarea）、封面图（UploadImage 组件）、状态（el-radio）
- 正文：RichTextEditor（wangeditor）
- 保存按钮：调用 POST 或 PUT 接口

### 8.5 音乐管理 (`/music`)

- el-table：歌曲名、歌手、封面、音频URL（截断显示）、操作
- 操作：「编辑」（el-drawer 抽屉）、新增（抽屉）、删除
- 编辑表单：歌曲名、歌手、封面URL（UploadImage）、音频URL（el-input）、播放列表归属

### 8.6 自愈练习管理 (`/healing`)

- 同音乐管理结构
- 编辑表单：标题、封面图、练习描述（textarea）、步骤内容（可多行编辑）、目标时长（秒）

### 8.7 树洞帖子审核 (`/topic`)

**列表页：**
- 状态筛选：el-radio-group（全部/审核中/正常/已删除）
- el-table：内容预览（截断30字）、发布者openid、图片数量、回复数、状态（el-tag）、发布时间
- 操作：「查看详情」（打开 el-dialog 弹窗）

**详情弹窗：**
- 完整帖子内容
- 图片轮播展示（el-image-viewer 或 el-carousel）
- 下方回复列表（el-timeline 样式）
- 底部操作按钮：「审核通过」（PUT /topic/{id}/status → 1）、「删除」（PUT /topic/{id}/status → 2）

### 8.8 问卷管理 (`/quiz`)

**问卷列表：**
- el-table：问卷标题、类型（type）、题目数、状态（el-switch）、操作
- 操作：「管理题目」（跳转 `/quiz/{id}/questions`）

**题目编辑页：**
- 顶部：返回按钮 + 问卷标题
- el-table 展示所有题目：题目内容（截断）、选项数量（JSON 数组长度）、排序、操作
- 操作：「编辑」（el-dialog 弹窗，内含题目内容 el-input + 动态选项列表 el-form + 分数 el-input-number）
- 「新增题目」按钮
- 底部「保存所有更改」按钮（批量提交到 `POST /quiz/{id}/questions`）

### 8.9 用户管理 (`/user`)

- el-table：昵称、头像、地区（省/市）、性别、注册时间、对话数
- 搜索：关键词（昵称/openid）
- 操作：「详情」→ 弹窗或抽屉，展示：
  - 用户基本信息
  - 最近测评记录（el-table：问卷名称、得分、结论、时间）
  - 最近对话摘要（最近10条）

### 8.10 AI对话记录 (`/chat`)

**列表页（按用户分组）：**
- 筛选栏：openid 输入框 + 时间范围（el-date-picker）+ 关键词输入框 + 查询
- el-table：用户openid、最新消息预览、消息数、最近活跃时间
- 操作：「查看对话」

**对话详情弹窗/抽屉：**
- 聊天记录样式（参照微信聊天界面）：
  - user 消息：右侧气泡（蓝色背景）
  - assistant 消息：左侧气泡（灰色背景）
- 每条消息显示时间

### 8.11 公告管理 (`/notice`)

- el-table：标题、内容预览、发布时间、状态
- 操作：新增、编辑（el-dialog/el-drawer）、删除

### 8.12 AI配置 (`/ai-config`)

- 展示当前 AI 系统提示词（el-input textarea，大文本框）
- 情绪关键词配置（el-tag + el-input 动态添加/删除）
- 危机关键词列表
- 保存按钮

## 九、数据库表参考

> 以下是后端已有表结构，管理后台需要对这些表进行 CRUD 操作。

```sql
-- 用户表
CREATE TABLE user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  openid VARCHAR(50) NOT NULL UNIQUE,
  nick_name VARCHAR(50),
  avatar_url VARCHAR(255),
  province VARCHAR(20), city VARCHAR(20), country VARCHAR(20),
  gender TINYINT DEFAULT 0,  -- 0未知 1男 2女
  language VARCHAR(10),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 树洞帖子表
CREATE TABLE topic (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  content TEXT NOT NULL,
  images JSON,  -- 图片URL数组
  publisher_openid VARCHAR(50) NOT NULL,
  anonymous TINYINT(1) DEFAULT 1,
  status TINYINT DEFAULT 1,  -- 0审核中 1正常 2已删除
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 树洞回复表
CREATE TABLE reply (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  topic_id BIGINT NOT NULL,
  content TEXT NOT NULL,
  replier_openid VARCHAR(50) NOT NULL,
  replied_user_openid VARCHAR(50),
  anonymous TINYINT(1) DEFAULT 1,
  status TINYINT DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 问卷主表
CREATE TABLE questionnaire (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(100) NOT NULL,
  description VARCHAR(500),
  cover VARCHAR(255),
  type VARCHAR(20) NOT NULL,  -- PHQ9_DEMO, GAD7_DEMO, SCL90_DEMO
  scoring_rule JSON,
  status TINYINT DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 问卷题目表
CREATE TABLE question (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  questionnaire_id BIGINT NOT NULL,
  content TEXT NOT NULL,
  options JSON NOT NULL,  -- [{text: "完全不会", score: 0}, ...]
  sort_order INT DEFAULT 0,
  status TINYINT DEFAULT 1
);

-- 测评结果表
CREATE TABLE quiz_result (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  openid VARCHAR(50) NOT NULL,
  questionnaire_id BIGINT NOT NULL,
  answers JSON NOT NULL,
  score INT NOT NULL,
  max_score INT,
  score_percent INT,
  conclusion TEXT NOT NULL,
  suggestions TEXT,
  ai_chat_hint VARCHAR(500),
  ai_guidance TEXT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- AI聊天记录表
CREATE TABLE chat_history (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  openid VARCHAR(50) NOT NULL,
  role VARCHAR(20) NOT NULL,  -- user / assistant
  content TEXT NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

> 注意：`article`、`music`、`self_healing` 表结构请参考后端实体类或 Swagger 文档。

## 十、核心文件示例

### 10.1 main.js

```js
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import router from './router'
import App from './App.vue'

const app = createApp(App)

// 注册所有 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })
app.mount('#app')
```

### 10.2 Login 页面示例

```vue
<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>校园心理健康管理系统</h2>
      <el-form :model="form" @submit.prevent="handleLogin">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleLogin" style="width:100%">
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminLogin } from '@/api/admin'

const router = useRouter()
const form = ref({ username: '', password: '' })
const loading = ref(false)

async function handleLogin() {
  if (!form.value.username || !form.value.password) {
    return ElMessage.warning('请输入用户名和密码')
  }
  loading.value = true
  try {
    const res = await adminLogin(form.value)
    localStorage.setItem('admin_token', res.token)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>
```

### 10.3 api/admin.js 示例

```js
import request from './request'

export function adminLogin(data) {
  return request.post('/admin/login', data)
}

export function getAdminInfo() {
  return request.get('/admin/info')
}
```

### 10.4 api/article.js 示例

```js
import request from './request'

export function getArticleList(params) {
  return request.get('/admin/article/list', { params })
}

export function getArticleDetail(id) {
  return request.get(`/admin/article/${id}`)
}

export function createArticle(data) {
  return request.post('/admin/article', data)
}

export function updateArticle(id, data) {
  return request.put(`/admin/article/${id}`, data)
}

export function updateArticleStatus(id, status) {
  return request.put(`/admin/article/${id}/status`, { status })
}

export function deleteArticle(id) {
  return request.delete(`/admin/article/${id}`)
}
```

## 十一、开发步骤（按顺序执行）

1. **初始化项目**：`npm create vite@latest . -- --template vue`，安装依赖
2. **配置 vite.config.js**：设置 `@` 别名 + `/api` 代理到 `localhost:8080`
3. **搭建 main.js**：挂载 ElementPlus、Pinia、Router
4. **实现 utils/auth.js**：localStorage 读写 token
5. **实现 api/request.js**：Axios 封装 + 拦截器
6. **实现路由 router/index.js**：路由表 + 登录守卫
7. **实现 stores/user.js**：Pinia store 存管理员信息
8. **实现登录页**：调用 `/api/admin/login`
9. **实现 Layout 主布局**：侧边栏菜单 + 顶栏
10. **实现 Dashboard**：调用统计接口，展示 ECharts 图表
11. **逐个实现各管理页面**：Article → Topic → Quiz → User → Chat → Music → Healing → Notice → AiConfig
12. **实现公共组件**：UploadImage、RichTextEditor

## 十二、注意事项

1. **JWT 兼容**：后端拦截器需同时支持小程序 token（role=user）和管理后台 token（role=admin），管理接口要求 `role=admin`
2. **图片上传**：`UploadImage.vue` 组件调用 `POST /api/upload`（后端已有），返回的 URL 存入数据库
3. **JSON 字段**：`question.options`、`topic.images`、`quiz_result.answers` 均为 MySQL JSON 类型，前端需要 JSON.parse 处理
4. **富文本内容**：文章正文使用 HTML 格式存储，wangeditor 输出的就是 HTML
5. **状态字段**：`topic.status`（0=审核中，1=正常，2=已删除）、`questionnaire.status`（0=停用，1=启用）
6. **真机调试**：如需在其他设备访问管理后台，修改 `vite.config.js` 中 `server.host` 为 `'0.0.0.0'`