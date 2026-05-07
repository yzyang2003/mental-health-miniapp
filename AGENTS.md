# AGENTS.md

## Project Overview

Campus mental health counseling WeChat mini-program + Spring Boot 3 backend (graduation project).

- **Frontend**: WeChat mini-program native framework (WXML/WXSS/JavaScript) — no npm, no package.json
- **Backend**: Spring Boot 3.3.5 + MyBatis-Plus 3.5.7 + MySQL 8 + JWT (jjwt 0.12.6) + springdoc-openapi
- **Java**: 17 (pom.xml `<java.version>17</java.version>`)
- **AI**: MiMo-V2.5 via external API (configurable), voice ASR/TTS via `mimo.api.*` config

## Key Commands

```bash
# Maven wrapper is NOT at the standard location. Use this path:
./.tools/apache-maven-3.9.9/bin/mvn spring-boot:run   # start backend (requires MySQL running)
./.tools/apache-maven-3.9.9/bin/mvn test               # run all tests (146 tests, ~10s)
./.tools/apache-maven-3.9.9/bin/mvn compile             # compile only (faster check)

# There is no standard mvnw. Do NOT use 'mvn' directly unless it's on PATH.
# Backend port: 8080
# Swagger UI: http://localhost:8080/swagger-ui.html
```

## Project Structure

```
├── pages/                     # WeChat mini-program pages (17 pages)
│   ├── ai-chat/index          # AI counselor chat (typewriter, voice, streaming)
│   ├── station/index          # Home station (article/music/healing entry)
│   ├── consult/index          # Consultation center (AI + quiz entry)
│   ├── quiz-list/detail/result # Psychological quizzes
│   ├── topic-list/detail      # Anonymous tree-hole community
│   ├── self-healing-list/detail # Self-healing exercises (timer, progress ring)
│   ├── article-list/detail    # Mental health articles
│   ├── music-list             # Music therapy playlists
│   ├── login/profile          # Auth and profile
│   └── notice-center/my-quiz-history/webview
├── components/                # Shared components
│   ├── bottom-nav/            # Tab bar
│   ├── chat-avatar/           # Chat avatar
│   ├── markdown-view/         # Markdown renderer (rich-text)
│   ├── minimal-back/          # Back button
│   └── skeleton/              # Loading skeleton
├── utils/                     # Frontend utilities (all plain JS, no build step)
│   ├── config.js              # API base URL (simulator/real device logic)
│   ├── request.js             # HTTP wrapper with auto JWT injection
│   ├── auth.js                # ensurePageLogin()
│   ├── chat-stream.js         # Stream/SSE handling for AI chat
│   ├── chat-typewriter.js     # Typewriter effect for assistant replies
│   ├── chat-scroll.js         # Auto-scroll management
│   ├── chat-state.js          # Page state save/restore
│   ├── markdown-parser.js     # Markdown → HTML (<strong>/<em>/<code>)
│   └── quizCatalog.js, quizNormalize.js, selfHealingCatalog.js, etc.
├── src/main/java/com/example/demo/
│   ├── DemoApplication.java   # Entry point (@MapperScan configured)
│   ├── controller/            # REST controllers (Health, Login, User, station/, topic/, reply/)
│   ├── module/consult/        # AI consult module (SELF-CONTAINED — own entity/mapper/service)
│   │   ├── controller/        # ConsultChatController, ConsultQuizController
│   │   ├── client/            # OpenAiCompletionClient (external AI API)
│   │   ├── inference/         # OpenAiInferenceService
│   │   ├── safety/            # ContentSafetyService, DefaultContentSafetyService, CrisisEventLogService
│   │   ├── emotion/           # EmotionRecognitionService, CrisisLevelAssessmentService, EmotionResult
│   │   ├── skills/            # Counseling skills (BasicCounseling, ProfessionalCounseling, CampusScenario,
│   │   │                      #   CrisisIntervention, QuizRecommendation, TherapyRecommendation, ResourceSkills)
│   │   ├── dto/               # ChatRequest, ChatResponse, ChatHistoryVO, Quiz DTOs
│   │   ├── entity/            # ChatHistory, Questionnaire, Question, QuizResult
│   │   ├── mapper/            # MyBatis-Plus mappers
│   │   └── service/           # AIChatService, VoiceService, RateLimitService, QuizService
│   ├── service/               # Main app services (User, Topic, Reply, Article, Music, SelfHealing)
│   ├── mapper/                # Main app mappers
│   ├── entity/                # Main app entities
│   ├── config/                # WebConfig, etc.
│   ├── interceptor/           # JWT interceptor
│   └── common/                # Unified response wrapper
├── sql/                       # Database scripts (~32 files, manual execution — NO migration tool)
│   ├── graduation_design.sql  # Main schema + seed data
│   └── *.sql                  # Incremental changes (run manually)
└── pom.xml
```

## Environment Setup

1. Copy `src/main/resources/application-local.yml.example` → `application-local.yml`
2. Fill in: MySQL password, JWT secret, WeChat appid/secret, AI API key, MiMo API key
3. `application-local.yml` is gitignored — never commit

**Critical**: `application.yml` uses `spring.config.import` to load `application-local.yml` from classpath. The local file overrides `SPRING_DATASOURCE_*` env vars.

## Frontend API Address

`utils/config.js` resolution order:
1. `FORCE_BASE_URL` (hardcoded override)
2. `wx.getStorageSync('apiBaseUrlOverride')` (runtime override)
3. Simulator → `127.0.0.1:8080` | Real device → `LAN_HOST:8080`

Real device debugging requires updating `LAN_HOST` to your current LAN IPv4. The app auto-prompts for IP if backend is unreachable on real device.

## Testing

```bash
./.tools/apache-maven-3.9.9/bin/mvn test
```

Tests use **standalone MockMvc** (no Spring context loaded):
```java
MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
```

Test classes live under `src/test/java/com/example/demo/module/consult/`:
- `controller/` — ConsultChatControllerStandaloneTest, HealthControllerTest, ArticleControllerTest
- `emotion/` — EmotionRecognitionServiceTest, CrisisLevelAssessmentServiceTest
- `safety/` — DefaultContentSafetyServiceTest
- `skills/` — QuizRecommendationServiceTest, TherapyRecommendationServiceTest, CampusScenarioSkillsTest, CrisisInterventionServiceTest

## Architecture — AI Consult Module

The `module/consult/` is a **self-contained sub-system** with its own entity/mapper/service layer. It does NOT share mappers/entities with the main app.

**Request flow**:
1. `ConsultChatController` receives message → `RateLimitService` check → `AIChatService.sendMessage()`
2. `AIChatServiceImpl` orchestrates: content safety → emotion recognition → crisis check → AI API call → response
3. `OpenAiCompletionClient` calls external AI API (configurable via `ai.api.*`)
4. `VoiceService` handles ASR/TTS via MiMo API (`mimo.api.*`)

**Key services**:
- `AIChatServiceImpl` — Core chat logic, history management, system prompt construction
- `ContentSafetyService` — Keyword-based crisis/unsafe detection (12 crisis keywords)
- `EmotionRecognitionService` — 7-type emotion detection (happy, sad, anxious, angry, fearful, neutral, surprised)
- `CrisisInterventionService` — 4-level crisis response with hotlines
- `QuizRecommendationService` — Maps emotions to DB quizzes (IDs 3=PHQ-9, 4=GAD-7, 7=SCL-90, 8=SDS, 9=SAS)
- `TherapyRecommendationService` — Maps emotions to self-healing exercises (IDs 1-5)
- `VoiceService` — MiMo ASR/TTS integration (requires `mimo.api.key` config)

**AI parameters**: max_tokens=700, temperature=0.4, connect_timeout=5s, read_timeout=30s, max_retry=1

## Database

- Name: `graduation_design` (utf8mb4)
- Schema: `sql/graduation_design.sql`
- Migrations: manual — add incremental `.sql` files in `sql/`
- **No migration tool** (no Flyway/Liquibase)

## WeChat Mini-Program Gotchas

- `border-radius` creates a clipping context — `overflow: visible` may not work
- `<button>` has default padding — use `::after { border: none }` + `display: flex` for centering
- `web-view` component unavailable for unregistered (未备案) mini-programs
- Real device debugging requires phone and computer on same LAN
- `app.json` has `permission.scope.record` for voice recording

## Security

- JWT interceptor protects all endpoints except `/api/health`, `/api/login`, `/swagger-ui/**`, `/v3/api-docs/**`
- `project.private.config.json` — WeChat dev tool private config (gitignored)
- `uploads/` — user uploads (gitignored)
- `application-local.yml` — local secrets (gitignored)

## Git

- Remote: `https://github.com/yzyang2003/mental-health-miniapp.git`
- Branch: `main`
- Commit style: conventional-ish, Chinese descriptions (e.g. `feat: ...`, `fix: ...`, `docs: ...`)
- Latest commit: `5b52c11 feat: 树洞演示数据 + 修复匿名回复和输入框遮挡问题`

## Conventions

- Backend: Lombok `@Data`, `@RequiredArgsConstructor`, `@Slf4j` used pervasively
- Frontend: plain JS modules with `module.exports`, no build step, no TypeScript
- Chat frontend split into modules: `chat-stream.js`, `chat-typewriter.js`, `chat-scroll.js`, `chat-state.js`
- CSS: global variables in `app.wxss` (`--space-page-x`, `--space-card-padding`, etc.)
- Consult module has its own `AGENTS.md` at `src/main/java/com/example/demo/module/consult/AGENTS.md`

## Admin Management System

Vue 3 + Vite 5 + Element Plus admin panel at `admin/`. Run with:
```bash
cd admin && npm run dev    # dev server on port 5173
cd admin && npm run build  # production build
```

Backend admin endpoints at `/api/admin/*` require JWT with `role=admin`. Login: `POST /api/admin/login` (username/password).

Admin frontend pages: Dashboard, Article, Music, Healing, Topic, Quiz, User, Chat, Notice, AI Config.

Dashboard has 6 charts: user trend, daily active, quiz trend, quiz type usage, article stats, emotion distribution.

Article edit supports AI fill: paste a URL to auto-fetch title/content via Jsoup.

**IDEA Lombok**: Enable annotation processing in Settings → Build → Compiler → Annotation Processors.
