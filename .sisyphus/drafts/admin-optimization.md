# Draft: Admin System Optimization

## Requirements (confirmed)
1. 侧边栏背景颜色改为浅绿色，当前深色看不清黑色字体
2. 网页窗口缩放时页面渲染动态变化，避免数据显示不全
3. 数据概览页面需要6个图表：用户数量变化图、用户日活变化图、文章点击率饼状图、AI对话用户情绪分布图（如果修复不了暂时搁置）、测评总体趋势图、测评量表使用率饼状图
4. 文章管理新建文章支持粘贴网页链接后AI智能填充数据，删除封面字段
5. 音乐管理读取数据失败 - 需修复
6. 树洞审核无法显示用户发的图片或表情包
7. 问卷管理加上新增问卷和编辑问卷
8. 用户管理详情查看测评次数、AI对话次数和树洞发帖数

## Research Findings

### 问题5: 音乐管理
- 实体 Music.java 映射到 `music` 表，但实际表名是 `music_playlist`
- music_playlist 字段: id, name, description, cover_image, share_url, emotion_type, duration_text, play_count, status, create_time
- Music.java 字段: id, songName, singer, cover, emotionType, url, duration, playCount, status, createTime
- 字段名完全不同！需要修正映射

### 问题3: Dashboard
- 后端已返回: totalUsers, todayNew, totalArticles, totalTopics, totalQuizCount, totalChatMessages, trendData, emotionDistribution
- 前端只显示: totalUsers, totalArticles, totalQuizCount, totalChatMessages（4个卡片 + 2个图表）
- todayNew 和 totalTopics 没有显示在前端
- 情绪分布: chat_history.emotion 字段全是 NULL

### 问题8: 用户详情
- API 有 /user/{openid}/quizzes 和 /user/{openid}/chats 端点
- 但前端没有调用这些端点
- 需要添加树洞帖子数查询

## Technical Decisions
- 侧边栏: 浅绿色背景 + 深色文字
- 响应式: 使用 el-row/el-col 的 :xs/:sm/:md/:lg 属性
- 音乐: 修正 entity @TableName 注解 + 字段映射
- 情绪分布: 跳过（AI 聊天模块未写入 emotion 字段）
- 文章AI填充: 需要后端新增接口（或前端直接调用 AI API）

## Scope Boundaries
- INCLUDE: 8项优化全部
- EXCLUDE: AI配置页面后端（无 spec）
