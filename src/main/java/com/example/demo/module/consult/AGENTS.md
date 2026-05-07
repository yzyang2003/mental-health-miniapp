# AGENTS.md — AI Consult Module

**Self-contained sub-system** with its own entity/mapper/service. Does NOT share data layer with the main app.

## Structure

```
module/consult/
├── controller/        # ConsultChatController (chat, reset, voice, tts, stt)
│                      # ConsultQuizController (quiz submit, result, list)
├── client/            # OpenAiCompletionClient (external AI API)
├── inference/         # OpenAiInferenceService
├── safety/            # ContentSafetyService (interface), DefaultContentSafetyService (keyword-based)
│                      # CrisisEventLogService, EnhancedContentSafetyService, SafetyResult (enum)
├── emotion/           # EmotionRecognitionService (7 emotions), CrisisLevelAssessmentService (4 levels)
│                      # EmotionResult (primary emotion, intensity, list)
├── skills/            # Counseling skills (injected as Spring beans)
│   ├── BasicCounselingSkills.java         # Empathy responses, crisis responses
│   ├── ProfessionalCounselingSkills.java  # CBT, person-centered therapy
│   ├── CampusScenarioSkills.java          # Academic, interpersonal, emotional, career
│   ├── CrisisInterventionService.java     # 4-level crisis response with hotlines
│   ├── QuizRecommendationService.java     # Emotion → quiz mapping (DB IDs: 3,4,7,8,9)
│   ├── TherapyRecommendationService.java  # Emotion → self-healing mapping (DB IDs: 1-5)
│   └── ResourceSkills.java               # Campus/external/crisis resources
├── dto/               # ChatRequest, ChatResponse, ChatHistoryVO, Quiz DTOs
├── entity/            # ChatHistory, Questionnaire, Question, QuizResult
├── mapper/            # MyBatis-Plus mappers (BaseMapper<T>)
└── service/           # AIChatService (interface), AIChatServiceImpl (core orchestrator)
                       # VoiceService (MiMo ASR/TTS), RateLimitService, QuizService
```

## Key Files

| Task | File | Notes |
|------|------|-------|
| Chat orchestration | `service/impl/AIChatServiceImpl.java` | Core: safety → emotion → crisis → AI → response |
| AI API call | `client/OpenAiCompletionClient.java` | External API, 30s read timeout |
| Content safety | `safety/DefaultContentSafetyService.java` | 12 crisis keywords, 6 unsafe keywords |
| Emotion detection | `emotion/EmotionRecognitionService.java` | 7 emotion types |
| Crisis assessment | `emotion/CrisisLevelAssessmentService.java` | 4-level (LOW/MODERATE/HIGH/CRISIS) |
| Quiz recommendation | `skills/QuizRecommendationService.java` | Maps emotions to existing DB quizzes |
| Therapy recommendation | `skills/TherapyRecommendationService.java` | Maps emotions to self-healing exercises |
| Voice ASR/TTS | `service/VoiceService.java` | MiMo API integration, requires `mimo.api.key` |
| Rate limiting | `service/RateLimitService.java` | 10 req/min/user |
| System prompt | `AIChatServiceImpl.SYSTEM_PROMPT` | Unicode-escaped Chinese (校园心理陪伴角色) |

## API Endpoints

```
POST /api/consult/chat/send      → sendMessage (text chat)
POST /api/consult/chat/reset     → resetChatHistory (clear DB history)
GET  /api/consult/chat/history   → getChatHistory (paginated)
POST /api/consult/chat/stt       → speechToText (MiMo ASR)
POST /api/consult/chat/tts       → textToSpeech (MiMo TTS, returns file path)
POST /api/consult/chat/voice     → processVoiceMessage (ASR + AI reply)
POST /api/consult/quiz/submit    → submitQuiz (quiz scoring + result)
GET  /api/consult/quiz/result    → getQuizResult
GET  /api/consult/quiz/list      → listQuestionnaires
```

## Anti-Patterns

- ❌ Do NOT call external AI API directly from controller — must go through `AIChatService`
- ❌ Do NOT hardcode SCL-90 factor configs — must read from DB via `Scl90ReportService`
- ❌ Do NOT add `@Primary` to services unless intentionally overriding default bean (risk: `BeanDefinitionOverrideException`)

## AI Config

```yaml
ai.api.url:     # OpenAI-compatible endpoint
ai.api.key:     # API key
ai.api.model:   # Model name
ai.api.max-tokens: 700
ai.api.temperature: 0.4
ai.api.connect-timeout: 5000
ai.api.read-timeout: 30000
ai.api.max-retry: 1
```

## Notes

- `ChatResponse` is simple: `{ reply: string, timestamp: datetime }` — no recommendation data in committed code
- `AIChatServiceImpl` uses in-memory state for some tracking (resets on server restart)
- All mappers extend `BaseMapper<T>` (MyBatis-Plus)
- Lombok everywhere: `@Data`, `@RequiredArgsConstructor`, `@Slf4j`
