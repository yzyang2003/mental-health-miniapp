# AGENTS.md — 树洞帖子详情页

**Generated:** 2026-05-05
**File:** topic-detail/index.js (587行）

## OVERVIEW
帖子详情 + 回复列表 + 回复框。支持富文本回复、图片上传。

## STRUCTURE
```
topic-detail/
├── index.js        # 587行，主逻辑
├── index.wxml      # 帖子内容 + 回复列表 + 回复框
├── index.wxss      # 样式
└── index.json      # 页面配置
```

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| 加载帖子 | `index.js` → `fetchDetail()` | `/api/topic/detail/:id` |
| 回复列表 | `index.js` → `fetchReplies()` | 分页加载 |
| 提交回复 | `index.js` → `submitReply()` | 支持图片上传 |
| 图片上传 | `index.js` → `chooseImage()` | 调 `wx.chooseMedia` |
| 点赞功能 | `index.js` → `toggleLike()` | 乐观更新 UI |

## CONVENTIONS
- 回复分页: `pageNum=1, pageSize=20`
- 图片上传: 先调 `wx.chooseMedia`，再调后端上传接口
- 输入框高度自适应: `auto-height` + `bindlinechange`

## ANTI-PATTERNS (THIS PAGE)
- 587行单文件 → 建议拆分：回复逻辑、图片上传、点赞逻辑
- 图片上传成功后未清理临时路径 → 可能内存泄漏
- 乐观更新失败后未回滚 UI → 点赞状态可能不一致

## NOTES
- 需要登录: `ensurePageLogin()` 检查
- 胶囊对齐: `calcCapsule()` 已实现
- 富文本回复: 不支持（当前纯文本），需要可扩展
