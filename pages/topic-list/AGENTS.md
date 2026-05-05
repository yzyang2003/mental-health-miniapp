# AGENTS.md — 树洞社区列表页

**Generated:** 2026-05-05
**File:** topic-list/index.js (747行，最大页面）

## OVERVIEW
树洞帖子列表 + 发帖入口。支持搜索、排序、分页加载。

## STRUCTURE
```
topic-list/
├── index.js        # 747行，主逻辑（需拆分）
├── index.wxml      # 列表渲染 + 搜索栏
├── index.wxss      # 样式
└── index.json      # 页面配置
```

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| 发帖逻辑 | `index.js` → `goToPost()` | 跳转 topic-detail 带参数 |
| 搜索功能 | `index.js` → `onSearchInput()` | 实时搜索，300ms 防抖 |
| 分页加载 | `index.js` → `loadMore()` | 触底加载，pageNum 递增 |
| 帖子卡片 | `index.wxml` → `<view wx:for="{{list}}">` | 头像 + 标题 + 预览 |

## CONVENTIONS
- 搜索防抖: 300ms（`setTimeout` 清理用 `onUnload`）
- 分页参数: `pageNum=1, pageSize=20`
- 发帖入口: 浮动按钮 `.fab-post`，固定在右下角

## ANTI-PATTERNS (THIS PAGE)
- 747行单文件 → 建议拆分：搜索逻辑、分页逻辑、列表渲染
- 搜索状态与列表状态耦合 → 考虑分离 data 字段
- 防抖 timer 未用 `this` 存储 → 页面卸载时可能漏清理

## NOTES
- 需要登录: `ensurePageLogin()` 检查
- 胶囊对齐: `calcCapsule()` 已实现
- 空状态: 搜索无结果 + 列表为空，两种空态
