# Admin System Optimization Plan

## TL;DR

> **Quick Summary**: 修复音乐管理数据读取失败、情绪分布显示失败、侧边栏配色、响应式布局、Dashboard 6图表、文章AI填充、树洞图片显示、问卷CRUD、用户详情增强。
>
> **Estimated Effort**: Medium
> **Parallel Execution**: YES - 3 waves
> **Critical Path**: 音乐实体修复 → Dashboard 增强 → 其他并行

---

## Context

### Original Request
用户提出 8 项优化需求：
1. 侧边栏背景改为浅绿色
2. 窗口缩放时页面动态适应
3. Dashboard 需要 6 个图表
4. 文章管理支持 AI 填充 + 删除封面
5. 音乐管理读取失败
6. 树洞审核无法显示图片
7. 问卷管理加新建/编辑
8. 用户详情增强

### Research Findings

**音乐管理（问题5 - 严重）**：
- 实体 `Music.java` 映射到 `music` 表，但实际表名是 `music_playlist`
- 字段完全不匹配：
  - `songName` → `name`
  - `singer` → `description`
  - `cover` → `cover_image`
  - `url` → `share_url`
  - `duration` → `duration_text`（类型也不同：int vs varchar）
- 需要修正 `@TableName` 注解和字段映射

**Dashboard（问题3）**：
- 后端已返回 8 个字段，但前端只显示 4 个卡片 + 2 个图表
- `todayNew` 和 `totalTopics` 未在前端显示
- `emotionDistribution` 全部为 NULL（AI 聊天模块未写入 emotion 字段）
- 测评量表使用率需要新增后端接口（按 questionnaire_id 分组统计）

**用户详情（问题8）**：
- API 有 `/user/{openid}/quizzes` 和 `/user/{openid}/chats` 端点
- 前端已定义 `getUserQuizzes()` 和 `getUserChats()` 函数
- 但前端从未调用这些函数
- 缺少树洞帖子数查询

**树洞图片（问题6）**：
- `Topic.images` 存储为 JSON 数组（`List<String>`）
- 前端详情弹窗已用 `el-image` + `preview-src-list` 渲染
- 可能的问题：图片 URL 无效或被截断

---

## Work Objectives

### Core Objective
修复 8 项优化需求，提升管理后台的完整性和用户体验。

### Definition of Done
- [ ] 侧边栏背景为浅绿色，文字清晰可读
- [ ] 页面在不同窗口宽度下正常显示
- [ ] Dashboard 显示 6 个图表（跳过情绪分布 - 无数据）
- [ ] 音乐管理正常读取和显示数据
- [ ] 树洞审核能显示用户图片
- [ ] 问卷管理支持新建和编辑问卷
- [ ] 用户详情显示测评记录、对话记录、树洞帖子数

### Must Have
- 音乐实体表名和字段映射修复
- Dashboard 6 个图表（情绪分布跳过）
- 问卷新建/编辑弹窗
- 用户详情增强

### Must NOT Have
- 不修改数据库表结构
- 不新增后端认证逻辑
- 不改变现有 API 行为
- 情绪分布图表暂时搁置（无数据）

---

## Verification Strategy

> **ZERO HUMAN INTERVENTION** - ALL verification is agent-executed.

### Test Decision
- **Automated tests**: None
- **Verification method**: 浏览器验证 + API 测试

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (基础修复):
├── Task 1: 音乐实体表名和字段映射修复 [quick]
├── Task 2: 侧边栏样式优化 [quick]
└── Task 3: 响应式布局优化 [quick]

Wave 2 (功能增强):
├── Task 4: Dashboard 6 图表实现 [deep]
├── Task 5: 树洞图片显示优化 [quick]
├── Task 6: 问卷管理 CRUD 增强 [deep]
├── Task 7: 用户详情增强 [quick]
└── Task 8: 文章管理 AI 填充 + 删除封面 [deep]

Wave FINAL (验证):
├── Task F1: 全功能验证 [deep]
└── Task F2: 范围检查 [deep]
-> Present results -> Get explicit user okay

Critical Path: Task 1 → Task 4 → F1-F2 → user okay
Parallel Speedup: ~60% faster than sequential
```

---

## TODOs

- [x] 1. 音乐实体表名和字段映射修复

  **What to do**:
  - 修改 `src/main/java/com/example/demo/entity/Music.java`:
    - `@TableName("music")` → `@TableName("music_playlist")`
    - 字段映射修改：
      - `songName` → `name`（@TableField("name")）
      - `singer` → 移除（music_playlist 没有此字段）
      - `cover` → `coverImage`（@TableField("cover_image")）
      - `url` → `shareUrl`（@TableField("share_url")）
      - `duration` (Integer) → `durationText` (String)（@TableField("duration_text")）
      - 新增 `description` 字段（@TableField("description")）
  - 修改 `admin/src/views/Music/List.vue`:
    - 表格列：songName → name, singer → description, cover → coverImage, url → shareUrl
    - 编辑表单字段同步修改
  - 修改 `admin/src/api/music.js`: 无需修改（API 调用不变）

  **Must NOT do**:
  - 不修改数据库表结构
  - 不修改 AdminMusicController（Controller 使用实体类，自动适配）

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (Wave 1)
  - **Blocks**: Task F1
  - **Blocked By**: None

  **References**:
  - `src/main/java/com/example/demo/entity/Music.java` - 当前实体
  - `admin/src/views/Music/List.vue` - 前端页面
  - 数据库 `music_playlist` 表结构: id, name, description, cover_image, share_url, emotion_type, duration_text, play_count, status, create_time

  **QA Scenarios**:
  ```
  Scenario: 音乐列表正常加载
    Tool: 浏览器
    Steps:
      1. 访问 http://localhost:5173/music
      2. 验证表格显示音乐数据
      3. 验证歌曲名、封面、URL 正确显示
    Expected Result: 音乐列表正常显示 6 条数据
    Evidence: .sisyphus/evidence/task-1-music-list.png
  ```

  **Commit**: YES
  - Message: `fix(admin): 修复音乐实体表名和字段映射`
  - Files: `src/main/java/com/example/demo/entity/Music.java`, `admin/src/views/Music/List.vue`

- [x] 2. 侧边栏样式优化

  **What to do**:
  - 修改 `admin/src/views/Layout/index.vue`:
    - `.layout-aside` 背景色从 `#304156` 改为浅绿色 `#e8f5e9`
    - `.logo` 文字颜色从 `#fff` 改为 `#2e7d32`
    - `.layout-menu` 背景色设为透明
    - 菜单文字颜色设为深色 `#333`
    - 活跃菜单项背景色设为 `#c8e6c9`
    - 悬停效果：`#dcedc8`

  **Must NOT do**:
  - 不改变菜单结构和功能

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (Wave 1)
  - **Blocks**: Task F1
  - **Blocked By**: None

  **References**:
  - `admin/src/views/Layout/index.vue` - 当前样式

  **QA Scenarios**:
  ```
  Scenario: 侧边栏颜色正确
    Tool: 浏览器
    Steps:
      1. 访问 http://localhost:5173/dashboard
      2. 验证侧边栏背景为浅绿色
      3. 验证菜单文字清晰可读
    Expected Result: 浅绿色背景 + 深色文字
    Evidence: .sisyphus/evidence/task-2-sidebar.png
  ```

  **Commit**: YES
  - Message: `style(admin): 侧边栏改为浅绿色主题`
  - Files: `admin/src/views/Layout/index.vue`

- [x] 3. 响应式布局优化

  **What to do**:
  - 修改 `admin/src/views/Dashboard/index.vue`:
    - 统计卡片使用 `el-col` 的 `:xs="12" :sm="12" :md="6"` 响应式
    - 图表使用 `:xs="24" :md="12"` 小屏幕全宽
  - 修改 `admin/src/views/Layout/index.vue`:
    - 小屏幕时侧边栏默认折叠
    - 添加媒体查询处理极小屏幕
  - 修改其他列表页面:
    - 表格添加 `max-height` 和横向滚动
    - 搜索表单使用 `el-form` 的 `inline` 模式

  **Must NOT do**:
  - 不改变页面功能

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (Wave 1)
  - **Blocks**: Task F1
  - **Blocked By**: None

  **References**:
  - Element Plus 响应式栅格: `el-col` 的 `xs`, `sm`, `md`, `lg`, `xl` 属性

  **QA Scenarios**:
  ```
  Scenario: 窗口缩放时页面正常
    Tool: 浏览器
    Steps:
      1. 访问 http://localhost:5173/dashboard
      2. 调整浏览器窗口宽度到 1200px、800px、500px
      3. 验证卡片和图表自动适应
    Expected Result: 不同宽度下布局正常
    Evidence: .sisyphus/evidence/task-3-responsive.png
  ```

  **Commit**: YES
  - Message: `fix(admin): 添加响应式布局支持`
  - Files: `admin/src/views/Dashboard/index.vue`, `admin/src/views/Layout/index.vue`

- [x] 4. Dashboard 6 图表实现

  **What to do**:
  - 修改后端 `DashboardController.java`:
    - 新增 `userTrend`: 近 7 天用户注册趋势（GROUP BY DATE(create_time)）
    - 新增 `quizByType`: 按问卷类型分组统计（GROUP BY questionnaire_id）
    - 新增 `dailyActive`: 近 7 天日活用户（去重 openid）
  - 修改 `DashboardStatsVO.java`:
    - 新增字段：`userTrend`, `quizByType`, `dailyActive`
  - 修改前端 `Dashboard/index.vue`:
    - 6 个图表布局：2 行 x 3 列
    - 图表 1: 用户数量变化图（折线图）- 使用 userTrend
    - 图表 2: 用户日活变化图（折线图）- 使用 dailyActive
    - 图表 3: 测评总体趋势图（折线图）- 使用现有 trendData
    - 图表 4: 测评量表使用率饼状图（饼图）- 使用 quizByType
    - 图表 5: 文章点击率饼状图（饼图）- 需要新增后端字段或跳过
    - 图表 6: 情绪分布图（跳过 - 无数据）
    - 更新 4 个统计卡片：添加 todayNew 和 totalTopics

  **Must NOT do**:
  - 不修改现有 API 行为
  - 情绪分布图表暂时搁置

  **Recommended Agent Profile**:
  - **Category**: `deep`
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (Wave 2)
  - **Blocks**: Task F1
  - **Blocked By**: None

  **References**:
  - `src/main/java/com/example/demo/controller/admin/DashboardController.java`
  - `src/main/java/com/example/demo/dto/admin/DashboardStatsVO.java`
  - `admin/src/views/Dashboard/index.vue`
  - 数据库表：user, quiz_result, chat_history, article

  **QA Scenarios**:
  ```
  Scenario: Dashboard 显示 6 个图表
    Tool: 浏览器
    Steps:
      1. 访问 http://localhost:5173/dashboard
      2. 验证显示 6 个统计卡片
      3. 验证显示 6 个图表
    Expected Result: 6 卡片 + 6 图表正常渲染
    Evidence: .sisyphus/evidence/task-4-dashboard.png
  ```

  **Commit**: YES
  - Message: `feat(admin): Dashboard 6 图表实现`
  - Files: `DashboardController.java`, `DashboardStatsVO.java`, `admin/src/views/Dashboard/index.vue`

- [x] 5. 树洞图片显示优化

  **What to do**:
  - 修改 `admin/src/views/Topic/List.vue`:
    - 表格中添加图片缩略图预览（显示前 3 张）
    - 详情弹窗优化图片网格布局
    - 添加图片加载失败处理

  **Must NOT do**:
  - 不修改后端 API

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (Wave 2)
  - **Blocks**: Task F1
  - **Blocked By**: None

  **References**:
  - `admin/src/views/Topic/List.vue` - 当前实现

  **QA Scenarios**:
  ```
  Scenario: 树洞图片正常显示
    Tool: 浏览器
    Steps:
      1. 访问 http://localhost:5173/topic
      2. 点击有图片的帖子"查看详情"
      3. 验证图片正常显示
    Expected Result: 图片网格布局正常
    Evidence: .sisyphus/evidence/task-5-topic-images.png
  ```

  **Commit**: YES
  - Message: `fix(admin): 树洞图片显示优化`
  - Files: `admin/src/views/Topic/List.vue`

- [x] 6. 问卷管理 CRUD 增强

  **What to do**:
  - 修改后端 `AdminQuizController.java`:
    - 新增 `DELETE /{id}` 删除问卷接口
  - 修改 `admin/src/views/Quiz/List.vue`:
    - 添加"新建问卷"按钮
    - 添加"编辑"按钮（打开编辑弹窗）
    - 添加"删除"按钮（带确认）
    - 编辑弹窗：标题、描述、类型、状态
  - 修改 `admin/src/api/quiz.js`:
    - 添加 `deleteQuiz(id)` 函数

  **Must NOT do**:
  - 不修改现有 CRUD 接口（只新增 delete）

  **Recommended Agent Profile**:
  - **Category**: `deep`
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (Wave 2)
  - **Blocks**: Task F1
  - **Blocked By**: None

  **References**:
  - `admin/src/views/Quiz/List.vue` - 当前实现
  - `admin/src/api/quiz.js` - API 层
  - `src/main/java/com/example/demo/controller/admin/AdminQuizController.java` - 后端已有 CRUD

  **QA Scenarios**:
  ```
  Scenario: 问卷新建/编辑/删除
    Tool: 浏览器
    Steps:
      1. 访问 http://localhost:5173/quiz
      2. 点击"新建问卷"，填写表单，保存
      3. 验证新问卷出现在列表
      4. 点击"编辑"，修改标题，保存
      5. 验证标题已更新
      6. 点击"删除"，确认
      7. 验证问卷已删除
    Expected Result: 完整 CRUD 功能正常
    Evidence: .sisyphus/evidence/task-6-quiz-crud.png
  ```

  **Commit**: YES
  - Message: `feat(admin): 问卷管理 CRUD 增强`
  - Files: `admin/src/views/Quiz/List.vue`, `admin/src/api/quiz.js`

- [x] 7. 用户详情增强

  **What to do**:
  - 修改 `admin/src/views/User/List.vue`:
    - 详情抽屉添加"测评记录"标签页
    - 详情抽屉添加"对话记录"标签页
    - 详情抽屉添加"树洞帖子数"统计
    - 调用 `getUserQuizzes()` 和 `getUserChats()` 获取数据
  - 修改后端 `AdminUserController.java`:
    - `detail` 接口添加 `topicCount` 字段

  **Must NOT do**:
  - 不修改现有 API 行为

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (Wave 2)
  - **Blocks**: Task F1
  - **Blocked By**: None

  **References**:
  - `admin/src/views/User/List.vue` - 当前实现
  - `admin/src/api/user.js` - API 层（已有 getUserQuizzes, getUserChats）
  - `src/main/java/com/example/demo/controller/admin/AdminUserController.java`

  **QA Scenarios**:
  ```
  Scenario: 用户详情显示完整数据
    Tool: 浏览器
    Steps:
      1. 访问 http://localhost:5173/user
      2. 点击用户"详情"
      3. 验证显示测评次数、对话次数、树洞帖子数
      4. 切换到"测评记录"标签页
      5. 验证显示测评历史
    Expected Result: 用户详情完整显示
    Evidence: .sisyphus/evidence/task-7-user-detail.png
  ```

  **Commit**: YES
  - Message: `feat(admin): 用户详情增强`
  - Files: `admin/src/views/User/List.vue`, `AdminUserController.java`

- [x] 8. 文章管理 AI 填充 + 删除封面

  **What to do**:
  - 修改 `admin/src/views/Article/Edit.vue`:
    - 删除封面图上传字段
    - 添加"AI 填充"输入框：粘贴网页链接后自动填充标题和内容
    - AI 填充逻辑：调用后端新增接口 `POST /api/admin/article/ai-fill`
  - 修改后端 `AdminArticleController.java`:
    - 新增 `POST /api/admin/article/ai-fill` 接口
    - 接收 `{url: string}`，返回 `{title: string, content: string, summary: string}`
    - 实现：使用 Jsoup 抓取网页内容，提取标题和正文

  **Must NOT do**:
  - 不修改现有文章 CRUD 接口
  - AI 填充失败时不影响手动编辑

  **Recommended Agent Profile**:
  - **Category**: `deep`
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES (Wave 2)
  - **Blocks**: Task F1
  - **Blocked By**: None

  **References**:
  - `admin/src/views/Article/Edit.vue` - 当前实现
  - `src/main/java/com/example/demo/controller/admin/AdminArticleController.java`

  **QA Scenarios**:
  ```
  Scenario: AI 填充文章
    Tool: 浏览器
    Steps:
      1. 访问 http://localhost:5173/article/edit
      2. 在"AI 填充"输入框粘贴一个网页链接
      3. 点击"填充"按钮
      4. 验证标题、内容自动填充
    Expected Result: AI 填充功能正常
    Evidence: .sisyphus/evidence/task-8-ai-fill.png
  ```

  **Commit**: YES
  - Message: `feat(admin): 文章 AI 填充 + 删除封面`
  - Files: `admin/src/views/Article/Edit.vue`, `AdminArticleController.java`

---

## Final Verification Wave

- [x] F1. **全功能验证** — `deep`
  逐项验证所有 8 项优化是否完成：侧边栏颜色、响应式、Dashboard 图表、音乐管理、树洞图片、问卷 CRUD、用户详情、文章 AI 填充。
  Output: `Features [8/8 pass] | VERDICT`

- [x] F2. **范围检查** — `deep`
  确认没有超出范围的修改：未修改数据库表结构、未改变现有 API 行为、未添加新依赖。
  Output: `Compliance [PASS/FAIL] | VERDICT`

---

## Success Criteria

### Final Checklist
- [ ] 侧边栏浅绿色背景
- [ ] 窗口缩放时页面正常
- [ ] Dashboard 6 图表
- [ ] 音乐管理正常工作
- [ ] 树洞图片显示
- [ ] 问卷新建/编辑功能
- [ ] 用户详情增强
- [ ] 文章 AI 填充功能
