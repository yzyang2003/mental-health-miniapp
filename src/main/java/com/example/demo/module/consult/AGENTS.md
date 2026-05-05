# AGENTS.md — AI 咨询模块

**Generated:** 2026-05-05
**Module:** 独立子系统，有自己的 controller/entity/mapper/service

## OVERVIEW
AI 咨询模块 — 外部 AI API 调用 + 安全检测 + 推理逻辑。独立的 entity/mapper/service 层，不与主程序混用。

## STRUCTURE
```
module/consult/
├── controller/      # 咨询专用控制器
├── client/          # AI API 客户端
├── inference/       # 推理逻辑
├── safety/          # 安全检测（内容审核）
├── dto/             # 咨询专用 DTO
├── entity/          # 咨询专用实体
├── mapper/          # 咨询专用 Mapper
└── service/         # 咨询专用 Service
    ├── QuizScoringService.java
    ├── Scl90ReportService.java
    └── QuizAiGuidanceService.java
```

## WHERE TO LOOK
| Task | Location | Notes |
|------|----------|-------|
| AI 调用逻辑 | `inference/OpenAiInferenceService.java` | 外部 API，超时 30s |
| 内容审核 | `safety/ContentSafetyService.java` | 集成 AI 内容审核 |
| 量表评分 | `service/QuizScoringService.java` | 提取的评分逻辑 |
| SCL-90 报告 | `service/Scl90ReportService.java` | SCL-90 因子配置从 DB 读取 |
| 速率限制 | `service/RateLimitService.java` | 10 req/min/用户 |

## CONVENTIONS
- 所有 Service 实现在 `service/impl/` 子目录
- DTO 按功能分包（quiz/, chat/）
- Mapper 使用 MyBatis-Plus，继承 BaseMapper
- 安全审核失败 → 返回规则建议（不调 AI）

## ANTI-PATTERNS (THIS MODULE)
- 禁止在 controller 直接调用外部 AI API（必须经过 service）
- 禁止在 service 内硬编码 SCL-90 因子（必须从 DB 读取）
- 禁止 `as any` / `@ts-ignore` 式 Java 忽略（用 Optional / 空检查）

## NOTES
- 最大 token: 700，温度: 0.4（演示场景优化）
- 超时重试: 1次，指数退避
- 速率限制: 10 req/min/用户（通过 RateLimitService）
