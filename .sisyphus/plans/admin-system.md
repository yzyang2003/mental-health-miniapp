# Admin Management System - Full Stack Build

## TL;DR

> **Quick Summary**: Build a complete admin management system for the campus mental health WeChat mini-program, including Vue 3 frontend (12 pages) and Spring Boot backend (40 API endpoints). The system manages articles, music, self-healing exercises, topics (tree-hole), quizzes, users, chat records, and notices.
>
> **Deliverables**:
> - Vue 3 + Vite 5 + Element Plus admin frontend (12 pages, 2 shared components)
> - Spring Boot backend: 40 admin API endpoints across 11 modules
> - 2 new database tables: `admin`, `notice`
> - JWT role-based authentication (backward compatible with existing mini-program tokens)
> - SQL migration scripts + seed data
>
> **Estimated Effort**: Large
> **Parallel Execution**: YES - 4 waves
> **Critical Path**: DB migrations → JWT refactoring → Admin auth → Backend CRUD → Frontend pages → Integration

---

## Context

### Original Request
Build a complete admin management system from scratch based on the specification in `admin/README.md`. Use the best available technologies and architecture.

### Interview Summary
**Key Discussions**:
- Tech stack: Vue 3 + Vite 5 + Element Plus + Pinia (JavaScript, no TypeScript)
- No unit tests needed
- Deployment not decided yet
- Backend: Spring Boot 3.3.5 + MyBatis-Plus 3.5.7 + MySQL 8 + JWT

**Research Findings**:
- Backend has 0/35 admin endpoints - all must be built
- Frontend has 0% - must be built from scratch
- Existing entities can be reused (User, Article, Music, SelfHealing, Topic, Reply, Questionnaire, Question, QuizResult, ChatHistory)
- JWT has no role concept - needs refactoring
- No general upload endpoint exists (only topic-specific)
- No `notice` table exists
- AiConfig page has no backend spec - deferred

### Metis Review
**Identified Gaps** (addressed):
- Admin user storage: Separate `admin` table chosen (cleaner separation)
- AiConfig backend: Deferred (no spec)
- Dashboard emotion distribution: Simplified to show available metrics
- Batch question save: Full-replace strategy
- JWT backward compatibility: Optional role claim with default "user"
- Need SQL seed script for initial admin account

---

## Work Objectives

### Core Objective
Build a production-ready admin management system with 40 backend API endpoints and 12 frontend pages, enabling content management, user monitoring, and data visualization for the campus mental health mini-program.

### Concrete Deliverables
- `sql/admin_migration.sql` - New tables (admin, notice) + seed data
- `src/main/java/com/example/demo/entity/Admin.java` - Admin entity
- `src/main/java/com/example/demo/entity/Notice.java` - Notice entity
- `src/main/java/com/example/demo/mapper/AdminMapper.java` - Admin mapper
- `src/main/java/com/example/demo/mapper/NoticeMapper.java` - Notice mapper
- `src/main/java/com/example/demo/service/AdminService.java` + impl
- `src/main/java/com/example/demo/service/NoticeService.java` + impl
- `src/main/java/com/example/demo/controller/admin/*.java` - 11 admin controllers
- `src/main/java/com/example/demo/dto/admin/*.java` - Admin DTOs
- `src/main/java/com/example/demo/common/Result.java` - Response wrapper (if not exists)
- `admin/` - Complete Vue 3 frontend project
- `admin/src/api/*.js` - 11 API modules
- `admin/src/views/**/*.vue` - 12 page components
- `admin/src/components/*.vue` - 2 shared components
- `admin/src/router/index.js` - Router with auth guard
- `admin/src/stores/user.js` - Pinia store
- `admin/vite.config.js` - Vite config with proxy

### Definition of Done
- [ ] `./.tools/apache-maven-3.9.9/bin/mvn compile` succeeds with zero errors
- [ ] `cd admin && npm run build` completes without errors
- [ ] All 40 backend endpoints return appropriate HTTP status codes
- [ ] Admin login works with username/password
- [ ] All 12 frontend pages render without console errors
- [ ] Existing mini-program endpoints continue working unchanged
- [ ] SQL migrations execute cleanly on fresh database

### Must Have
- JWT backward compatibility (existing tokens without role claim must work)
- Admin authentication isolation (admin endpoints reject non-admin tokens)
- All 40 API endpoints as specified in README
- All 12 frontend pages as specified in README
- SQL migration scripts in `sql/` directory
- BCrypt password hashing for admin accounts
- Standardized `Result<T>` response wrapper for all admin endpoints

### Must NOT Have (Guardrails)
- Do NOT modify existing entity classes (only ADD new fields if needed)
- Do NOT change existing endpoint behavior or response formats
- Do NOT add unit tests (per user requirement)
- Do NOT implement AiConfig backend (no spec defined)
- Do NOT introduce MyBatis-Plus pagination interceptor (use manual pagination)
- Do NOT add TypeScript, npm dependencies to backend, or build tools beyond Vite
- Do NOT over-engineer batch question save (use full-replace strategy)
- Do NOT add animations or complex UI effects to frontend
- Do NOT create admin registration endpoint (use SQL seed only)

---

## Verification Strategy

> **ZERO HUMAN INTERVENTION** - ALL verification is agent-executed. No exceptions.

### Test Decision
- **Infrastructure exists**: YES (backend has MockMvc tests)
- **Automated tests**: None (per user requirement)
- **Framework**: N/A
- **Verification method**: Compilation checks + curl API testing + npm build verification

### QA Policy
Every task MUST include agent-executed QA scenarios.
Evidence saved to `.sisyphus/evidence/task-{N}-{scenario-slug}.{ext}`.

- **Backend**: Use Bash (curl) - Send requests, assert status + response fields
- **Frontend**: Use Bash (npm run build) - Verify build succeeds
- **Database**: Use Bash (mysql) - Verify table creation and seed data

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (Foundation - MUST complete first):
├── Task 1: SQL migrations + seed data [quick]
├── Task 2: Result wrapper + Admin entity + Notice entity [quick]
├── Task 3: Admin mapper + Notice mapper [quick]
├── Task 4: JWT refactoring for role support [deep]
├── Task 5: Admin service + Notice service [quick]
└── Task 6: WebConfig updates [quick]

Wave 2 (Backend CRUD - MAX PARALLEL):
├── Task 7: Admin auth controller (login + info) [quick]
├── Task 8: Dashboard stats controller [quick]
├── Task 9: Article admin controller [quick]
├── Task 10: Music admin controller [quick]
├── Task 11: Self-healing admin controller [quick]
├── Task 12: Topic admin controller [quick]
├── Task 13: Quiz admin controller [deep]
├── Task 14: User admin controller [quick]
├── Task 15: Chat admin controller [quick]
├── Task 16: Notice admin controller [quick]
└── Task 17: General upload controller [quick]

Wave 3 (Frontend - MAX PARALLEL):
├── Task 18: Vite project scaffolding [quick]
├── Task 19: Router + auth guard + Pinia store [quick]
├── Task 20: API layer (11 modules) [quick]
├── Task 21: Login page [quick]
├── Task 22: Layout (sidebar + header) [quick]
├── Task 23: Dashboard page [quick]
├── Task 24: Article management pages [quick]
├── Task 25: Music management pages [quick]
├── Task 26: Self-healing management pages [quick]
├── Task 27: Topic review pages [quick]
├── Task 28: Quiz management pages [deep]
├── Task 29: User management page [quick]
├── Task 30: Chat records page [quick]
├── Task 31: Notice management pages [quick]
├── Task 32: UploadImage component [quick]
└── Task 33: RichTextEditor component [quick]

Wave FINAL (Verification):
├── Task F1: Backend compilation verification [quick]
├── Task F2: Frontend build verification [quick]
├── Task F3: Integration testing (full CRUD cycle) [deep]
└── Task F4: Scope fidelity check [deep]
-> Present results -> Get explicit user okay

Critical Path: Task 1 → Task 4 → Task 7 → Task 13 → Task 18 → Task 32 → F1-F4 → user okay
Parallel Speedup: ~65% faster than sequential
Max Concurrent: 11 (Wave 2 & 3)
```

### Dependency Matrix

| Task | Depends On | Blocks |
|------|------------|--------|
| 1 | - | 2, 3, 4, 5, 6, 7-17 |
| 2 | 1 | 7-17 |
| 3 | 1 | 7-17 |
| 4 | 1 | 7-17 |
| 5 | 1 | 7-17 |
| 6 | 4 | 7-17 |
| 7-17 | 2, 3, 4, 5, 6 | 18-33 |
| 18 | - | 19-33 |
| 19 | 18 | 20-33 |
| 20 | 18 | 21-33 |
| 21-33 | 19, 20 | F1-F4 |
| F1-F4 | all | - |

### Agent Dispatch Summary

- **Wave 1**: 6 tasks - T1-T3 → `quick`, T4 → `deep`, T5-T6 → `quick`
- **Wave 2**: 11 tasks - T7-T12, T14-T17 → `quick`, T13 → `deep`
- **Wave 3**: 16 tasks - T18-T20 → `quick`, T21-T27, T29-T33 → `quick`, T28 → `deep`
- **FINAL**: 4 tasks - F1-F2 → `quick`, F3-F4 → `deep`

---

## TODOs

- [ ] 1. SQL Migrations + Seed Data

  **What to do**:
  - Create `sql/admin_migration.sql` with two new tables:
    - `admin` table: `id BIGINT PRIMARY KEY AUTO_INCREMENT, username VARCHAR(50) NOT NULL UNIQUE, password_hash VARCHAR(255) NOT NULL, nickname VARCHAR(50), avatar VARCHAR(255), status TINYINT DEFAULT 1, last_login DATETIME, create_time DATETIME DEFAULT CURRENT_TIMESTAMP, update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP`
    - `notice` table: `id BIGINT PRIMARY KEY AUTO_INCREMENT, title VARCHAR(100) NOT NULL, content TEXT, status TINYINT DEFAULT 1, create_time DATETIME DEFAULT CURRENT_TIMESTAMP, update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP`
  - Add seed data: Insert one admin user with BCrypt-hashed password (`admin` / `admin123`)
  - Add `emotion` column to `chat_history` table (VARCHAR(20), nullable) for dashboard emotion distribution
  - Add `role` column to `user` table (VARCHAR(20) DEFAULT 'user') for future role-based features

  **Must NOT do**:
  - Do NOT modify existing table structures (only ADD new columns)
  - Do NOT drop any existing tables or columns
  - Do NOT add foreign key constraints

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple SQL script creation, straightforward table definitions
  - **Skills**: []
  - **Skills Evaluated but Omitted**:
    - `sql`: Not needed for basic DDL

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 1 (first task)
  - **Blocks**: Tasks 2, 3, 4, 5, 6, 7-17
  - **Blocked By**: None (can start immediately)

  **References**:
  - `sql/graduation_design.sql` - Existing schema for table structure conventions
  - `src/main/java/com/example/demo/entity/User.java` - Existing entity for field naming conventions

  **Acceptance Criteria**:
  - [ ] `sql/admin_migration.sql` file exists
  - [ ] `admin` table created with all specified columns
  - [ ] `notice` table created with all specified columns
  - [ ] Seed admin user inserted (username: admin, password: admin123 hashed)
  - [ ] `emotion` column added to `chat_history`
  - [ ] `role` column added to `user` table

  **QA Scenarios**:
  ```
  Scenario: SQL migration executes cleanly
    Tool: Bash (mysql)
    Preconditions: Fresh database or existing graduation_design database
    Steps:
      1. Run: mysql -u root -p graduation_design < sql/admin_migration.sql
      2. Verify: mysql -u root -p graduation_design -e "DESCRIBE admin"
      3. Verify: mysql -u root -p graduation_design -e "DESCRIBE notice"
      4. Verify: mysql -u root -p graduation_design -e "SELECT COUNT(*) FROM admin WHERE username='admin'"
    Expected Result: Tables created, admin user exists with count=1
    Evidence: .sisyphus/evidence/task-1-sql-migration.txt

  Scenario: Seed data has valid BCrypt hash
    Tool: Bash (mysql)
    Preconditions: Migration executed
    Steps:
      1. Run: mysql -u root -p graduation_design -e "SELECT password_hash FROM admin WHERE username='admin'"
      2. Verify output starts with '$2a$' or '$2b$' (BCrypt prefix)
    Expected Result: BCrypt hash present
    Evidence: .sisyphus/evidence/task-1-seed-data.txt
  ```

  **Commit**: YES
  - Message: `feat(admin-backend): add database migrations for admin and notice tables`
  - Files: `sql/admin_migration.sql`
  - Pre-commit: migration SQL syntax check

- [ ] 2. Result Wrapper + Admin Entity + Notice Entity

  **What to do**:
  - Create `src/main/java/com/example/demo/common/Result.java` if not exists (generic response wrapper with code, message, data fields)
  - Create `src/main/java/com/example/demo/entity/Admin.java` following existing entity patterns:
    - Fields: id, username, passwordHash, nickname, avatar, status, lastLogin, createTime, updateTime
    - Annotations: @Data, @TableName("admin"), @TableId(type = IdType.AUTO)
    - Use @TableField for non-standard column mappings
  - Create `src/main/java/com/example/demo/entity/Notice.java` following existing entity patterns:
    - Fields: id, title, content, status, createTime, updateTime
    - Annotations: @Data, @TableName("notice"), @TableId(type = IdType.AUTO)

  **Must NOT do**:
  - Do NOT modify existing entity classes
  - Do NOT add business logic to entities (keep them as data carriers)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple entity class creation following established patterns
  - **Skills**: []
  - **Skills Evaluated but Omitted**:
    - `spring-boot`: Not needed for basic entity creation

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 1 (after Task 1)
  - **Blocks**: Tasks 3, 4, 5, 6, 7-17
  - **Blocked By**: Task 1

  **References**:
  - `src/main/java/com/example/demo/entity/User.java` - Existing entity pattern to follow
  - `src/main/java/com/example/demo/entity/Article.java` - Another entity example with different fields
  - `src/main/java/com/example/demo/common/` - Check if Result.java already exists

  **Acceptance Criteria**:
  - [ ] `Result.java` exists with generic type parameter
  - [ ] `Admin.java` entity created with correct annotations
  - [ ] `Notice.java` entity created with correct annotations
  - [ ] Both entities compile without errors

  **QA Scenarios**:
  ```
  Scenario: Entities compile correctly
    Tool: Bash (mvn compile)
    Preconditions: Task 1 migration executed
    Steps:
      1. Run: ./.tools/apache-maven-3.9.9/bin/mvn compile -q
      2. Check exit code is 0
    Expected Result: BUILD SUCCESS
    Evidence: .sisyphus/evidence/task-2-compile.txt

  Scenario: Result wrapper has correct structure
    Tool: Bash (grep)
    Preconditions: Result.java created
    Steps:
      1. Run: grep -n "class Result" src/main/java/com/example/demo/common/Result.java
      2. Verify generic type parameter exists
    Expected Result: Class definition with <T> parameter
    Evidence: .sisyphus/evidence/task-2-result-structure.txt
  ```

  **Commit**: YES
  - Message: `feat(admin-backend): add Result wrapper and Admin/Notice entities`
  - Files: `src/main/java/com/example/demo/common/Result.java`, `src/main/java/com/example/demo/entity/Admin.java`, `src/main/java/com/example/demo/entity/Notice.java`
  - Pre-commit: `mvn compile -q`

- [ ] 3. Admin Mapper + Notice Mapper

  **What to do**:
  - Create `src/main/java/com/example/demo/mapper/AdminMapper.java` extending BaseMapper<Admin>
  - Create `src/main/java/com/example/demo/mapper/NoticeMapper.java` extending BaseMapper<Notice>
  - Add @Mapper annotation to both
  - Verify @MapperScan is configured in DemoApplication.java (already is per AGENTS.md)

  **Must NOT do**:
  - Do NOT add custom SQL methods to mappers (keep them bare like existing mappers)
  - Do NOT modify existing mapper classes

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Trivial mapper creation (just extends BaseMapper)
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 1 (after Task 2)
  - **Blocks**: Tasks 5, 6, 7-17
  - **Blocked By**: Task 2

  **References**:
  - `src/main/java/com/example/demo/mapper/UserMapper.java` - Existing mapper pattern
  - `src/main/java/com/example/demo/DemoApplication.java` - @MapperScan configuration

  **Acceptance Criteria**:
  - [ ] `AdminMapper.java` extends BaseMapper<Admin>
  - [ ] `NoticeMapper.java` extends BaseMapper<Notice>
  - [ ] Both mappers compile without errors

  **QA Scenarios**:
  ```
  Scenario: Mappers compile correctly
    Tool: Bash (mvn compile)
    Preconditions: Entities created in Task 2
    Steps:
      1. Run: ./.tools/apache-maven-3.9.9/bin/mvn compile -q
      2. Check exit code is 0
    Expected Result: BUILD SUCCESS
    Evidence: .sisyphus/evidence/task-3-compile.txt
  ```

  **Commit**: YES
  - Message: `feat(admin-backend): add Admin and Notice mappers`
  - Files: `src/main/java/com/example/demo/mapper/AdminMapper.java`, `src/main/java/com/example/demo/mapper/NoticeMapper.java`
  - Pre-commit: `mvn compile -q`

- [ ] 4. JWT Refactoring for Role Support

  **What to do**:
  - Modify `src/main/java/com/example/demo/utils/JwtUtil.java`:
    - Add `generateToken(String openid, String role)` overload (keep existing `generateToken(String openid)` for backward compatibility)
    - Add `getRoleFromToken(String token)` method (returns "user" if no role claim exists)
    - Add `ROLE_KEY` constant
  - Modify `src/main/java/com/example/demo/interceptor/JwtInterceptor.java`:
    - Extract role from token and store as request attribute
    - For `/api/admin/**` paths, check if role is "admin" (return 403 if not)
    - For non-admin paths, accept both "user" and "admin" roles
  - Modify `src/main/java/com/example/demo/config/WebConfig.java`:
    - Add `/api/admin/login` to exclusion list
    - Add `/api/upload` to exclusion list

  **Must NOT do**:
  - Do NOT break existing token validation (tokens without role claim must still work)
  - Do NOT change the token's `sub` claim (must remain openid)
  - Do NOT remove existing `generateToken(String openid)` method

  **Recommended Agent Profile**:
  - **Category**: `deep`
    - Reason: Critical security component requiring careful backward-compatible changes
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 1 (after Task 1)
  - **Blocks**: Tasks 7-17
  - **Blocked By**: Task 1

  **References**:
  - `src/main/java/com/example/demo/utils/JwtUtil.java` - Current JWT implementation
  - `src/main/java/com/example/demo/interceptor/JwtInterceptor.java` - Current interceptor
  - `src/main/java/com/example/demo/config/WebConfig.java` - Current exclusion list

  **Acceptance Criteria**:
  - [ ] `JwtUtil` has both `generateToken(String)` and `generateToken(String, String)` methods
  - [ ] `JwtUtil` has `getRoleFromToken(String)` method
  - [ ] `JwtInterceptor` extracts role and stores as request attribute
  - [ ] `/api/admin/**` paths require role=admin
  - [ ] `/api/admin/login` excluded from auth
  - [ ] Existing tokens without role claim still work (default to "user")

  **QA Scenarios**:
  ```
  Scenario: Existing tokens still work
    Tool: Bash (curl)
    Preconditions: Backend running with existing user token
    Steps:
      1. Run: curl http://localhost:8080/api/user/info -H "Authorization: Bearer <existing-token>"
      2. Verify: HTTP 200 response
    Expected Result: User info returned successfully
    Evidence: .sisyphus/evidence/task-4-backward-compat.txt

  Scenario: Admin endpoint requires admin role
    Tool: Bash (curl)
    Preconditions: Backend running
    Steps:
      1. Run: curl http://localhost:8080/api/admin/dashboard/stats
      2. Verify: HTTP 401 (no token)
      3. Run: curl http://localhost:8080/api/admin/dashboard/stats -H "Authorization: Bearer <user-token>"
      4. Verify: HTTP 403 (wrong role)
    Expected Result: Admin endpoints reject non-admin tokens
    Evidence: .sisyphus/evidence/task-4-admin-auth.txt

  Scenario: Admin login excluded from auth
    Tool: Bash (curl)
    Preconditions: Backend running
    Steps:
      1. Run: curl -X POST http://localhost:8080/api/admin/login -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123"}'
      2. Verify: HTTP 200 (not 401)
    Expected Result: Login endpoint accessible without token
    Evidence: .sisyphus/evidence/task-4-login-excluded.txt
  ```

  **Commit**: YES
  - Message: `feat(admin-backend): add JWT role support with backward compatibility`
  - Files: `src/main/java/com/example/demo/utils/JwtUtil.java`, `src/main/java/com/example/demo/interceptor/JwtInterceptor.java`, `src/main/java/com/example/demo/config/WebConfig.java`
  - Pre-commit: `mvn compile -q`

- [ ] 5. Admin Service + Notice Service

  **What to do**:
  - Create `src/main/java/com/example/demo/service/AdminService.java` interface:
    - `Admin login(String username, String password)` - verify password with BCrypt
    - `Admin getAdminInfo(Long id)` - get admin by ID
    - `boolean verifyPassword(String raw, String hashed)` - BCrypt verification
  - Create `src/main/java/com/example/demo/service/impl/AdminServiceImpl.java`:
    - Use `@RequiredArgsConstructor` for mapper injection
    - Implement BCrypt password verification using Spring Security Crypto
  - Create `src/main/java/com/example/demo/service/NoticeService.java` interface:
    - Standard CRUD: list, getById, create, update, delete
  - Create `src/main/java/com/example/demo/service/impl/NoticeServiceImpl.java`:
    - Follow existing service patterns (manual pagination, LambdaQueryWrapper)
  - Add `spring-security-crypto` dependency to `pom.xml` for BCrypt

  **Must NOT do**:
  - Do NOT add registration functionality (only login)
  - Do NOT store plain text passwords
  - Do NOT use full Spring Security (just the crypto module)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Standard service layer creation following established patterns
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 1 (after Task 3)
  - **Blocks**: Tasks 7-17
  - **Blocked By**: Task 3

  **References**:
  - `src/main/java/com/example/demo/service/impl/UserServiceImpl.java` - Existing service pattern
  - `pom.xml` - Current dependencies (need to add spring-security-crypto)

  **Acceptance Criteria**:
  - [ ] `AdminService` interface created with login and getAdminInfo methods
  - [ ] `AdminServiceImpl` implemented with BCrypt verification
  - [ ] `NoticeService` interface created with CRUD methods
  - [ ] `NoticeServiceImpl` implemented with manual pagination
  - [ ] `spring-security-crypto` dependency added to pom.xml
  - [ ] All classes compile without errors

  **QA Scenarios**:
  ```
  Scenario: Services compile correctly
    Tool: Bash (mvn compile)
    Preconditions: Mappers created in Task 3
    Steps:
      1. Run: ./.tools/apache-maven-3.9.9/bin/mvn compile -q
      2. Check exit code is 0
    Expected Result: BUILD SUCCESS
    Evidence: .sisyphus/evidence/task-5-compile.txt

  Scenario: BCrypt dependency added
    Tool: Bash (grep)
    Preconditions: pom.xml modified
    Steps:
      1. Run: grep -n "spring-security-crypto" pom.xml
      2. Verify dependency exists
    Expected Result: Dependency found in pom.xml
    Evidence: .sisyphus/evidence/task-5-dependency.txt
  ```

  **Commit**: YES
  - Message: `feat(admin-backend): add Admin and Notice services with BCrypt support`
  - Files: `src/main/java/com/example/demo/service/AdminService.java`, `src/main/java/com/example/demo/service/impl/AdminServiceImpl.java`, `src/main/java/com/example/demo/service/NoticeService.java`, `src/main/java/com/example/demo/service/impl/NoticeServiceImpl.java`, `pom.xml`
  - Pre-commit: `mvn compile -q`

- [ ] 6. WebConfig Updates

  **What to do**:
  - Verify `/api/admin/login` is in WebConfig exclusion list (should be done in Task 4, this task verifies)
  - Verify `/api/upload` is in WebConfig exclusion list
  - Add `/api/admin/dashboard/stats` to exclusion list if needed (dashboard may be accessed before full auth setup)
  - Test that all exclusions work correctly

  **Must NOT do**:
  - Do NOT remove any existing exclusions
  - Do NOT add unnecessary exclusions

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple configuration verification and minor updates
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 1 (after Task 4)
  - **Blocks**: Tasks 7-17
  - **Blocked By**: Task 4

  **References**:
  - `src/main/java/com/example/demo/config/WebConfig.java` - Current exclusion list

  **Acceptance Criteria**:
  - [ ] `/api/admin/login` excluded from JWT auth
  - [ ] `/api/upload` excluded from JWT auth
  - [ ] All exclusions verified with curl tests

  **QA Scenarios**:
  ```
  Scenario: Login endpoint accessible without token
    Tool: Bash (curl)
    Preconditions: Backend running
    Steps:
      1. Run: curl -X POST http://localhost:8080/api/admin/login -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123"}'
      2. Verify: HTTP 200 (not 401)
    Expected Result: Login works without authentication
    Evidence: .sisyphus/evidence/task-6-login-test.txt

  Scenario: Upload endpoint accessible without token
    Tool: Bash (curl)
    Preconditions: Backend running
    Steps:
      1. Run: curl -X POST http://localhost:8080/api/upload -F "file=@test.jpg"
      2. Verify: HTTP 200 or 400 (not 401)
    Expected Result: Upload endpoint accessible
    Evidence: .sisyphus/evidence/task-6-upload-test.txt
  ```

  **Commit**: YES
  - Message: `feat(admin-backend): verify WebConfig exclusions for admin endpoints`
  - Files: `src/main/java/com/example/demo/config/WebConfig.java`
  - Pre-commit: `mvn compile -q`

- [ ] 7. Admin Auth Controller (Login + Info)

  **What to do**:
  - Create `src/main/java/com/example/demo/controller/admin/AdminAuthController.java`:
    - `POST /api/admin/login` - Accept `{username, password}`, return `{token, admin: {...}}`
    - `GET /api/admin/info` - Return current admin info from JWT token
  - Create `src/main/java/com/example/demo/dto/admin/AdminLoginRequest.java` - DTO for login request
  - Create `src/main/java/com/example/demo/dto/admin/AdminLoginResponse.java` - DTO for login response
  - Use `Result<T>` wrapper for all responses
  - Generate JWT with role="admin" on successful login

  **Must NOT do**:
  - Do NOT create registration endpoint
  - Do NOT store plain text passwords
  - Do NOT return password hash in responses

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Standard auth controller implementation
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 8-17)
  - **Blocks**: Tasks 18-33
  - **Blocked By**: Tasks 2, 3, 4, 5, 6

  **References**:
  - `src/main/java/com/example/demo/controller/LoginController.java` - Existing login pattern
  - `src/main/java/com/example/demo/utils/JwtUtil.java` - JWT generation

  **Acceptance Criteria**:
  - [ ] `POST /api/admin/login` returns token and admin info
  - [ ] `GET /api/admin/info` returns current admin info
  - [ ] Login with wrong password returns 401
  - [ ] JWT token contains role="admin"

  **QA Scenarios**:
  ```
  Scenario: Admin login success
    Tool: Bash (curl)
    Preconditions: Admin user seeded in database
    Steps:
      1. Run: curl -X POST http://localhost:8080/api/admin/login -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123"}'
      2. Verify: HTTP 200
      3. Verify: Response contains "token" field
      4. Verify: Response contains "admin" object
    Expected Result: Login successful with JWT token
    Evidence: .sisyphus/evidence/task-7-login-success.txt

  Scenario: Admin login failure
    Tool: Bash (curl)
    Preconditions: Backend running
    Steps:
      1. Run: curl -X POST http://localhost:8080/api/admin/login -H "Content-Type: application/json" -d '{"username":"admin","password":"wrong"}'
      2. Verify: HTTP 401
    Expected Result: Login rejected with error message
    Evidence: .sisyphus/evidence/task-7-login-failure.txt

  Scenario: Get admin info
    Tool: Bash (curl)
    Preconditions: Valid admin token obtained
    Steps:
      1. Run: curl http://localhost:8080/api/admin/info -H "Authorization: Bearer <admin-token>"
      2. Verify: HTTP 200
      3. Verify: Response contains admin object without password_hash
    Expected Result: Admin info returned
    Evidence: .sisyphus/evidence/task-7-admin-info.txt
  ```

  **Commit**: YES
  - Message: `feat(admin-backend): implement admin login and info endpoints`
  - Files: `src/main/java/com/example/demo/controller/admin/AdminAuthController.java`, `src/main/java/com/example/demo/dto/admin/AdminLoginRequest.java`, `src/main/java/com/example/demo/dto/admin/AdminLoginResponse.java`
  - Pre-commit: `mvn compile -q`

- [ ] 8. Dashboard Stats Controller

  **What to do**:
  - Create `src/main/java/com/example/demo/controller/admin/DashboardController.java`:
    - `GET /api/admin/dashboard/stats` - Return aggregated statistics
  - Create `src/main/java/com/example/demo/dto/admin/DashboardStatsVO.java`:
    - Fields: totalUsers, todayNew, totalArticles, totalTopics, totalQuizCount, totalChatMessages
    - Nested: trendData (List of last 7 days), emotionDistribution (Map of emotion -> count)
  - Implement aggregation queries:
    - totalUsers: COUNT from user table
    - todayNew: COUNT from user table where create_time = today
    - totalArticles: COUNT from article table
    - totalTopics: COUNT from topic table
    - totalQuizCount: COUNT from quiz_result table
    - totalChatMessages: COUNT from chat_history table
    - trendData: GROUP BY date for last 7 days
    - emotionDistribution: GROUP BY emotion from chat_history

  **Must NOT do**:
  - Do NOT use complex joins (keep queries simple)
  - Do NOT cache results (real-time aggregation is fine for admin panel)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Standard aggregation queries with existing mappers
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 7, 9-17)
  - **Blocks**: Tasks 18-33
  - **Blocked By**: Tasks 2, 3, 4, 5, 6

  **References**:
  - `src/main/java/com/example/demo/entity/User.java` - User table structure
  - `src/main/java/com/example/demo/entity/Article.java` - Article table structure
  - `src/main/java/com/example/demo/module/consult/entity/ChatHistory.java` - Chat history structure

  **Acceptance Criteria**:
  - [ ] `GET /api/admin/dashboard/stats` returns all required fields
  - [ ] Response includes trendData array with 7 elements
  - [ ] Response includes emotionDistribution map
  - [ ] All counts are accurate

  **QA Scenarios**:
  ```
  Scenario: Dashboard stats returned
    Tool: Bash (curl)
    Preconditions: Admin token obtained, some test data exists
    Steps:
      1. Run: curl http://localhost:8080/api/admin/dashboard/stats -H "Authorization: Bearer <admin-token>"
      2. Verify: HTTP 200
      3. Verify: Response contains totalUsers, todayNew, totalArticles, totalTopics, totalQuizCount, totalChatMessages
      4. Verify: trendData is array of 7 elements
      5. Verify: emotionDistribution is object with emotion keys
    Expected Result: Complete dashboard statistics
    Evidence: .sisyphus/evidence/task-8-dashboard-stats.txt

  Scenario: Dashboard stats require admin role
    Tool: Bash (curl)
    Preconditions: Backend running
    Steps:
      1. Run: curl http://localhost:8080/api/admin/dashboard/stats -H "Authorization: Bearer <user-token>"
      2. Verify: HTTP 403
    Expected Result: Non-admin tokens rejected
    Evidence: .sisyphus/evidence/task-8-dashboard-auth.txt
  ```

  **Commit**: YES
  - Message: `feat(admin-backend): implement dashboard statistics endpoint`
  - Files: `src/main/java/com/example/demo/controller/admin/DashboardController.java`, `src/main/java/com/example/demo/dto/admin/DashboardStatsVO.java`
  - Pre-commit: `mvn compile -q`

- [ ] 9. Article Admin Controller

  **What to do**:
  - Create `src/main/java/com/example/demo/controller/admin/AdminArticleController.java`:
    - `GET /api/admin/article/list` - Paginated list with keyword, status filters
    - `GET /api/admin/article/{id}` - Article detail with full content
    - `POST /api/admin/article` - Create new article
    - `PUT /api/admin/article/{id}` - Update existing article
    - `PUT /api/admin/article/{id}/status` - Toggle publish status
    - `DELETE /api/admin/article/{id}` - Soft delete (set status=2)
  - Create DTOs: `AdminArticleRequest`, `AdminArticleListVO`, `AdminArticleDetailVO`
  - Reuse existing `ArticleMapper` and `Article` entity
  - Follow existing pagination pattern (manual offset calculation)

  **Must NOT do**:
  - Do NOT modify existing `ArticleController` (mini-program endpoints)
  - Do NOT hard delete articles (use soft delete with status)
  - Do NOT add image upload logic (use general upload endpoint)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Standard CRUD controller following established patterns
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 7-8, 10-17)
  - **Blocks**: Tasks 18-33
  - **Blocked By**: Tasks 2, 3, 4, 5, 6

  **References**:
  - `src/main/java/com/example/demo/controller/station/ArticleController.java` - Existing article endpoints
  - `src/main/java/com/example/demo/entity/Article.java` - Article entity fields

  **Acceptance Criteria**:
  - [ ] All 6 endpoints implemented and functional
  - [ ] Pagination works with page/size params
  - [ ] Keyword search filters by title
  - [ ] Status filter works (published/draft/deleted)
  - [ ] Status toggle updates article status
  - [ ] Soft delete sets status=2

  **QA Scenarios**:
  ```
  Scenario: Article CRUD cycle
    Tool: Bash (curl)
    Preconditions: Admin token obtained
    Steps:
      1. Create: POST /api/admin/article with title, content, status
      2. Verify: HTTP 201, response contains new article ID
      3. Read: GET /api/admin/article/{id}
      4. Verify: HTTP 200, article details returned
      5. Update: PUT /api/admin/article/{id} with new title
      6. Verify: HTTP 200, title updated
      7. Status toggle: PUT /api/admin/article/{id}/status
      8. Verify: HTTP 200, status changed
      9. Delete: DELETE /api/admin/article/{id}
      10. Verify: HTTP 200, article soft deleted
    Expected Result: Full CRUD cycle works
    Evidence: .sisyphus/evidence/task-9-article-crud.txt

  Scenario: Article list with pagination
    Tool: Bash (curl)
    Preconditions: Multiple articles exist
    Steps:
      1. Run: curl "http://localhost:8080/api/admin/article/list?page=1&size=10" -H "Authorization: Bearer <admin-token>"
      2. Verify: HTTP 200
      3. Verify: Response contains list, total, page, size
    Expected Result: Paginated list returned
    Evidence: .sisyphus/evidence/task-9-article-list.txt
  ```

  **Commit**: YES
  - Message: `feat(admin-backend): implement article admin CRUD endpoints`
  - Files: `src/main/java/com/example/demo/controller/admin/AdminArticleController.java`, `src/main/java/com/example/demo/dto/admin/AdminArticle*.java`
  - Pre-commit: `mvn compile -q`

- [ ] 10. Music Admin Controller

  **What to do**:
  - Create `src/main/java/com/example/demo/controller/admin/AdminMusicController.java`:
    - `GET /api/admin/music/list` - Paginated list
    - `POST /api/admin/music` - Create new music
    - `PUT /api/admin/music/{id}` - Update existing music
    - `DELETE /api/admin/music/{id}` - Soft delete
  - Create DTOs: `AdminMusicRequest`, `AdminMusicListVO`
  - Reuse existing `MusicMapper` and `Music` entity

  **Must NOT do**:
  - Do NOT modify existing `MusicController`
  - Do NOT implement playlist management (separate feature)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Standard CRUD controller
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 7-9, 11-17)
  - **Blocks**: Tasks 18-33
  - **Blocked By**: Tasks 2, 3, 4, 5, 6

  **References**:
  - `src/main/java/com/example/demo/controller/station/MusicController.java` - Existing music endpoints
  - `src/main/java/com/example/demo/entity/Music.java` - Music entity fields

  **Acceptance Criteria**:
  - [ ] All 4 endpoints implemented
  - [ ] Pagination works
  - [ ] CRUD operations functional

  **QA Scenarios**:
  ```
  Scenario: Music CRUD cycle
    Tool: Bash (curl)
    Preconditions: Admin token obtained
    Steps:
      1. Create: POST /api/admin/music with songName, singer, coverUrl, audioUrl
      2. Verify: HTTP 201
      3. Read: GET /api/admin/music/list
      4. Verify: HTTP 200, music in list
      5. Update: PUT /api/admin/music/{id} with new songName
      6. Verify: HTTP 200
      7. Delete: DELETE /api/admin/music/{id}
      8. Verify: HTTP 200
    Expected Result: Full CRUD cycle works
    Evidence: .sisyphus/evidence/task-10-music-crud.txt
  ```

  **Commit**: YES
  - Message: `feat(admin-backend): implement music admin CRUD endpoints`
  - Files: `src/main/java/com/example/demo/controller/admin/AdminMusicController.java`, `src/main/java/com/example/demo/dto/admin/AdminMusic*.java`
  - Pre-commit: `mvn compile -q`

- [ ] 11. Self-Healing Admin Controller

  **What to do**:
  - Create `src/main/java/com/example/demo/controller/admin/AdminHealingController.java`:
    - `GET /api/admin/healing/list` - Paginated list
    - `POST /api/admin/healing` - Create new healing exercise
    - `PUT /api/admin/healing/{id}` - Update existing
    - `DELETE /api/admin/healing/{id}` - Soft delete
  - Create DTOs: `AdminHealingRequest`, `AdminHealingListVO`
  - Reuse existing `SelfHealingMapper` and `SelfHealing` entity

  **Must NOT do**:
  - Do NOT modify existing `SelfHealingController`

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Standard CRUD controller
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 7-10, 12-17)
  - **Blocks**: Tasks 18-33
  - **Blocked By**: Tasks 2, 3, 4, 5, 6

  **References**:
  - `src/main/java/com/example/demo/controller/station/SelfHealingController.java` - Existing endpoints
  - `src/main/java/com/example/demo/entity/SelfHealing.java` - Entity fields

  **Acceptance Criteria**:
  - [ ] All 4 endpoints implemented
  - [ ] Pagination works
  - [ ] CRUD operations functional

  **QA Scenarios**:
  ```
  Scenario: Healing CRUD cycle
    Tool: Bash (curl)
    Preconditions: Admin token obtained
    Steps:
      1. Create: POST /api/admin/healing with title, coverUrl, description, steps, duration
      2. Verify: HTTP 201
      3. Read: GET /api/admin/healing/list
      4. Verify: HTTP 200
      5. Update: PUT /api/admin/healing/{id}
      6. Verify: HTTP 200
      7. Delete: DELETE /api/admin/healing/{id}
      8. Verify: HTTP 200
    Expected Result: Full CRUD cycle works
    Evidence: .sisyphus/evidence/task-11-healing-crud.txt
  ```

  **Commit**: YES
  - Message: `feat(admin-backend): implement self-healing admin CRUD endpoints`
  - Files: `src/main/java/com/example/demo/controller/admin/AdminHealingController.java`, `src/main/java/com/example/demo/dto/admin/AdminHealing*.java`
  - Pre-commit: `mvn compile -q`

- [ ] 12. Topic Admin Controller

  **What to do**:
  - Create `src/main/java/com/example/demo/controller/admin/AdminTopicController.java`:
    - `GET /api/admin/topic/list` - Paginated list with status filter
    - `GET /api/admin/topic/{id}` - Topic detail with replies
    - `PUT /api/admin/topic/{id}/status` - Approve (status=1) or delete (status=2)
    - `GET /api/admin/topic/{id}/replies` - List replies for topic
    - `PUT /api/admin/reply/{id}/status` - Delete reply (status=2)
  - Create DTOs: `AdminTopicListVO`, `AdminTopicDetailVO`, `AdminReplyListVO`
  - Reuse existing `TopicMapper`, `ReplyMapper`, `Topic`, `Reply` entities

  **Must NOT do**:
  - Do NOT modify existing `TopicController` or `ReplyController`
  - Do NOT hard delete topics or replies

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Standard CRUD with status management
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 7-11, 13-17)
  - **Blocks**: Tasks 18-33
  - **Blocked By**: Tasks 2, 3, 4, 5, 6

  **References**:
  - `src/main/java/com/example/demo/controller/topic/TopicController.java` - Existing topic endpoints
  - `src/main/java/com/example/demo/controller/reply/ReplyController.java` - Existing reply endpoints
  - `src/main/java/com/example/demo/entity/Topic.java` - Topic entity
  - `src/main/java/com/example/demo/entity/Reply.java` - Reply entity

  **Acceptance Criteria**:
  - [ ] All 5 endpoints implemented
  - [ ] Status filter works (all/pending/normal/deleted)
  - [ ] Topic detail includes replies
  - [ ] Approve/delete updates status correctly

  **QA Scenarios**:
  ```
  Scenario: Topic review workflow
    Tool: Bash (curl)
    Preconditions: Admin token obtained, topic exists with status=0 (pending)
    Steps:
      1. List: GET /api/admin/topic/list?status=0
      2. Verify: Topic in list
      3. Approve: PUT /api/admin/topic/{id}/status with status=1
      4. Verify: HTTP 200
      5. List: GET /api/admin/topic/list?status=1
      6. Verify: Topic now in normal list
    Expected Result: Topic status updated correctly
    Evidence: .sisyphus/evidence/task-12-topic-review.txt

  Scenario: Topic detail with replies
    Tool: Bash (curl)
    Preconditions: Topic with replies exists
    Steps:
      1. Run: GET /api/admin/topic/{id}
      2. Verify: HTTP 200
      3. Verify: Response contains topic content and replies array
    Expected Result: Topic detail with replies returned
    Evidence: .sisyphus/evidence/task-12-topic-detail.txt
  ```

  **Commit**: YES
  - Message: `feat(admin-backend): implement topic admin review endpoints`
  - Files: `src/main/java/com/example/demo/controller/admin/AdminTopicController.java`, `src/main/java/com/example/demo/dto/admin/AdminTopic*.java`
  - Pre-commit: `mvn compile -q`

- [ ] 13. Quiz Admin Controller

  **What to do**:
  - Create `src/main/java/com/example/demo/controller/admin/AdminQuizController.java`:
    - `GET /api/admin/quiz/list` - Questionnaire list with question count
    - `GET /api/admin/quiz/{id}` - Questionnaire detail
    - `POST /api/admin/quiz` - Create questionnaire
    - `PUT /api/admin/quiz/{id}` - Update questionnaire
    - `PUT /api/admin/quiz/{id}/status` - Enable/disable
    - `GET /api/admin/quiz/{id}/questions` - Get all questions
    - `POST /api/admin/quiz/{id}/questions` - Batch save questions (full replace)
  - Create DTOs: `AdminQuizRequest`, `AdminQuizListVO`, `AdminQuestionRequest`
  - Reuse existing `QuestionnaireMapper`, `QuestionMapper`, `Questionnaire`, `Question` entities
  - Implement batch save as: delete all questions for quiz, insert new array (transactional)

  **Must NOT do**:
  - Do NOT modify existing `ConsultQuizController`
  - Do NOT implement diff-based question updates (use full replace)

  **Recommended Agent Profile**:
  - **Category**: `deep`
    - Reason: Complex batch save logic with transaction management
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 7-12, 14-17)
  - **Blocks**: Tasks 18-33
  - **Blocked By**: Tasks 2, 3, 4, 5, 6

  **References**:
  - `src/main/java/com/example/demo/module/consult/controller/ConsultQuizController.java` - Existing quiz endpoints
  - `src/main/java/com/example/demo/module/consult/entity/Questionnaire.java` - Questionnaire entity
  - `src/main/java/com/example/demo/module/consult/entity/Question.java` - Question entity

  **Acceptance Criteria**:
  - [ ] All 7 endpoints implemented
  - [ ] Question count included in list
  - [ ] Batch save deletes old questions and inserts new ones
  - [ ] Batch save is transactional (all or nothing)
  - [ ] Status toggle works

  **QA Scenarios**:
  ```
  Scenario: Quiz CRUD with batch question save
    Tool: Bash (curl)
    Preconditions: Admin token obtained
    Steps:
      1. Create questionnaire: POST /api/admin/quiz
      2. Verify: HTTP 201, ID returned
      3. Batch save questions: POST /api/admin/quiz/{id}/questions with 3 questions
      4. Verify: HTTP 200
      5. Get questions: GET /api/admin/quiz/{id}/questions
      6. Verify: 3 questions returned
      7. Update questions: POST /api/admin/quiz/{id}/questions with 2 questions (different)
      8. Verify: HTTP 200
      9. Get questions: GET /api/admin/quiz/{id}/questions
      10. Verify: 2 questions returned (old ones deleted)
    Expected Result: Batch save replaces all questions
    Evidence: .sisyphus/evidence/task-13-quiz-batch.txt

  Scenario: Quiz list with question count
    Tool: Bash (curl)
    Preconditions: Questionnaire with questions exists
    Steps:
      1. Run: GET /api/admin/quiz/list
      2. Verify: Each questionnaire has questionCount field
      3. Verify: questionCount matches actual question count
    Expected Result: Accurate question counts
    Evidence: .sisyphus/evidence/task-13-quiz-list.txt
  ```

  **Commit**: YES
  - Message: `feat(admin-backend): implement quiz admin endpoints with batch question save`
  - Files: `src/main/java/com/example/demo/controller/admin/AdminQuizController.java`, `src/main/java/com/example/demo/dto/admin/AdminQuiz*.java`
  - Pre-commit: `mvn compile -q`

- [ ] 14. User Admin Controller

  **What to do**:
  - Create `src/main/java/com/example/demo/controller/admin/AdminUserController.java`:
    - `GET /api/admin/user/list` - Paginated list with keyword search
    - `GET /api/admin/user/{openid}` - User detail with quiz records and chat count
    - `GET /api/admin/user/{openid}/quizzes` - User's quiz history
    - `GET /api/admin/user/{openid}/chats` - User's chat records
  - Create DTOs: `AdminUserListVO`, `AdminUserDetailVO`, `AdminUserQuizVO`, `AdminUserChatVO`
  - Reuse existing `UserMapper`, `QuizResultMapper`, `ChatHistoryMapper`

  **Must NOT do**:
  - Do NOT modify existing `UserController`
  - Do NOT allow editing user data (read-only)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Read-only queries with existing mappers
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 7-13, 15-17)
  - **Blocks**: Tasks 18-33
  - **Blocked By**: Tasks 2, 3, 4, 5, 6

  **References**:
  - `src/main/java/com/example/demo/controller/UserController.java` - Existing user endpoints
  - `src/main/java/com/example/demo/entity/User.java` - User entity
  - `src/main/java/com/example/demo/module/consult/entity/QuizResult.java` - Quiz result entity
  - `src/main/java/com/example/demo/module/consult/entity/ChatHistory.java` - Chat history entity

  **Acceptance Criteria**:
  - [ ] All 4 endpoints implemented
  - [ ] User detail includes quiz records and chat count
  - [ ] Keyword search filters by nickname or openid
  - [ ] Pagination works

  **QA Scenarios**:
  ```
  Scenario: User list with search
    Tool: Bash (curl)
    Preconditions: Admin token obtained, users exist
    Steps:
      1. Run: GET /api/admin/user/list?page=1&size=10
      2. Verify: HTTP 200, user list returned
      3. Run: GET /api/admin/user/list?keyword=test
      4. Verify: Filtered results
    Expected Result: User list with search works
    Evidence: .sisyphus/evidence/task-14-user-list.txt

  Scenario: User detail with quiz records
    Tool: Bash (curl)
    Preconditions: User with quiz results exists
    Steps:
      1. Run: GET /api/admin/user/{openid}
      2. Verify: HTTP 200
      3. Verify: Response contains quizzes array and chatCount
    Expected Result: User detail with related data
    Evidence: .sisyphus/evidence/task-14-user-detail.txt
  ```

  **Commit**: YES
  - Message: `feat(admin-backend): implement user admin endpoints`
  - Files: `src/main/java/com/example/demo/controller/admin/AdminUserController.java`, `src/main/java/com/example/demo/dto/admin/AdminUser*.java`
  - Pre-commit: `mvn compile -q`

- [ ] 15. Chat Admin Controller

  **What to do**:
  - Create `src/main/java/com/example/demo/controller/admin/AdminChatController.java`:
    - `GET /api/admin/chat/list` - Global list with filters (openid, keyword, date range)
    - `GET /api/admin/chat/user/{openid}` - Full chat history for a user
  - Create DTOs: `AdminChatListVO`, `AdminChatMessageVO`
  - Reuse existing `ChatHistoryMapper`

  **Must NOT do**:
  - Do NOT modify existing `ConsultChatController`
  - Do NOT implement chat deletion (read-only)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple read-only queries
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 7-14, 16-17)
  - **Blocks**: Tasks 18-33
  - **Blocked By**: Tasks 2, 3, 4, 5, 6

  **References**:
  - `src/main/java/com/example/demo/module/consult/controller/ConsultChatController.java` - Existing chat endpoints
  - `src/main/java/com/example/demo/module/consult/entity/ChatHistory.java` - Chat history entity

  **Acceptance Criteria**:
  - [ ] Both endpoints implemented
  - [ ] List supports filters: openid, keyword, startDate, endDate
  - [ ] User chat returns full message history

  **QA Scenarios**:
  ```
  Scenario: Chat list with filters
    Tool: Bash (curl)
    Preconditions: Admin token obtained, chat history exists
    Steps:
      1. Run: GET /api/admin/chat/list?page=1&size=10
      2. Verify: HTTP 200, chat list returned
      3. Run: GET /api/admin/chat/list?openid=test-user
      4. Verify: Filtered by openid
    Expected Result: Chat list with filters works
    Evidence: .sisyphus/evidence/task-15-chat-list.txt

  Scenario: User chat history
    Tool: Bash (curl)
    Preconditions: User with chat history exists
    Steps:
      1. Run: GET /api/admin/chat/user/{openid}
      2. Verify: HTTP 200
      3. Verify: Full message history returned
    Expected Result: Complete chat for user
    Evidence: .sisyphus/evidence/task-15-chat-history.txt
  ```

  **Commit**: YES
  - Message: `feat(admin-backend): implement chat admin endpoints`
  - Files: `src/main/java/com/example/demo/controller/admin/AdminChatController.java`, `src/main/java/com/example/demo/dto/admin/AdminChat*.java`
  - Pre-commit: `mvn compile -q`

- [ ] 16. Notice Admin Controller

  **What to do**:
  - Create `src/main/java/com/example/demo/controller/admin/AdminNoticeController.java`:
    - `GET /api/admin/notice/list` - Notice list
    - `POST /api/admin/notice` - Create notice
    - `PUT /api/admin/notice/{id}` - Update notice
    - `DELETE /api/admin/notice/{id}` - Delete notice
  - Create DTOs: `AdminNoticeRequest`, `AdminNoticeListVO`
  - Use newly created `NoticeMapper` and `Notice` entity

  **Must NOT do**:
  - Do NOT implement notice publishing to mini-program (out of scope)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Standard CRUD controller
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 7-15, 17)
  - **Blocks**: Tasks 18-33
  - **Blocked By**: Tasks 2, 3, 4, 5, 6

  **References**:
  - `src/main/java/com/example/demo/entity/Notice.java` - Notice entity
  - `src/main/java/com/example/demo/mapper/NoticeMapper.java` - Notice mapper

  **Acceptance Criteria**:
  - [ ] All 4 endpoints implemented
  - [ ] CRUD operations functional

  **QA Scenarios**:
  ```
  Scenario: Notice CRUD cycle
    Tool: Bash (curl)
    Preconditions: Admin token obtained
    Steps:
      1. Create: POST /api/admin/notice with title, content
      2. Verify: HTTP 201
      3. Read: GET /api/admin/notice/list
      4. Verify: HTTP 200, notice in list
      5. Update: PUT /api/admin/notice/{id} with new title
      6. Verify: HTTP 200
      7. Delete: DELETE /api/admin/notice/{id}
      8. Verify: HTTP 200
    Expected Result: Full CRUD cycle works
    Evidence: .sisyphus/evidence/task-16-notice-crud.txt
  ```

  **Commit**: YES
  - Message: `feat(admin-backend): implement notice admin CRUD endpoints`
  - Files: `src/main/java/com/example/demo/controller/admin/AdminNoticeController.java`, `src/main/java/com/example/demo/dto/admin/AdminNotice*.java`
  - Pre-commit: `mvn compile -q`

- [ ] 17. General Upload Controller

  **What to do**:
  - Create `src/main/java/com/example/demo/controller/admin/UploadController.java`:
    - `POST /api/upload` - Accept multipart file, return `{url, filename}`
  - Support images and audio files
  - Save to `./uploads/{type}/{yyyyMMdd}/{UUID}.{ext}` pattern
  - Return public URL path
  - Add file size limit (10MB recommended)

  **Must NOT do**:
  - Do NOT modify existing topic upload endpoint
  - Do NOT store files in database (filesystem only)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: File upload endpoint following existing pattern
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 7-16)
  - **Blocks**: Tasks 18-33
  - **Blocked By**: Tasks 2, 3, 4, 5, 6

  **References**:
  - `src/main/java/com/example/demo/controller/topic/TopicController.java` - Existing upload pattern
  - `uploads/` directory structure

  **Acceptance Criteria**:
  - [ ] `POST /api/upload` accepts multipart file
  - [ ] Returns `{url, filename}` response
  - [ ] Files saved to correct directory structure
  - [ ] File size limit enforced

  **QA Scenarios**:
  ```
  Scenario: File upload success
    Tool: Bash (curl)
    Preconditions: Admin token obtained, test image file exists
    Steps:
      1. Run: curl -X POST http://localhost:8080/api/upload -H "Authorization: Bearer <admin-token>" -F "file=@test.jpg"
      2. Verify: HTTP 200
      3. Verify: Response contains url and filename
      4. Verify: File exists in uploads/ directory
    Expected Result: File uploaded successfully
    Evidence: .sisyphus/evidence/task-17-upload-success.txt

  Scenario: File upload without auth
    Tool: Bash (curl)
    Preconditions: Backend running
    Steps:
      1. Run: curl -X POST http://localhost:8080/api/upload -F "file=@test.jpg"
      2. Verify: HTTP 401
    Expected Result: Upload requires authentication
    Evidence: .sisyphus/evidence/task-17-upload-auth.txt
  ```

  **Commit**: YES
  - Message: `feat(admin-backend): implement general file upload endpoint`
  - Files: `src/main/java/com/example/demo/controller/admin/UploadController.java`
  - Pre-commit: `mvn compile -q`

- [ ] 18. Vite Project Scaffolding

  **What to do**:
  - Initialize Vue 3 + Vite project in `admin/` directory
  - Install dependencies: element-plus, @element-plus/icons-vue, vue-router@4, pinia, axios, @wangeditor/editor, @wangeditor/editor-for-vue, echarts
  - Configure `vite.config.js`:
    - Set `@` alias to `src/`
    - Set proxy `/api` to `http://localhost:8080`
    - Set dev server port to 5173
  - Create `admin/src/main.js` with ElementPlus, Pinia, Router setup
  - Create `admin/src/App.vue` with `<router-view />`
  - Create `admin/index.html` entry point

  **Must NOT do**:
  - Do NOT add TypeScript configuration
  - Do NOT add ESLint/Prettier (keep it simple)
  - Do NOT add testing frameworks

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Standard Vite project initialization
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 3 (first task)
  - **Blocks**: Tasks 19-33
  - **Blocked By**: None (can start immediately)

  **References**:
  - `admin/README.md` - Project specification

  **Acceptance Criteria**:
  - [ ] `admin/package.json` exists with all dependencies
  - [ ] `admin/vite.config.js` configured with proxy and alias
  - [ ] `admin/src/main.js` mounts ElementPlus, Pinia, Router
  - [ ] `npm run dev` starts successfully on port 5173
  - [ ] `npm run build` completes without errors

  **QA Scenarios**:
  ```
  Scenario: Project builds successfully
    Tool: Bash (npm)
    Preconditions: Node.js installed
    Steps:
      1. Run: cd admin && npm install
      2. Run: cd admin && npm run build
      3. Verify: exit code 0
      4. Verify: dist/ directory created
    Expected Result: Build successful
    Evidence: .sisyphus/evidence/task-18-build.txt

  Scenario: Dev server starts
    Tool: Bash (npm)
    Preconditions: Dependencies installed
    Steps:
      1. Run: cd admin && npm run dev &
      2. Sleep 3 seconds
      3. Run: curl http://localhost:5173
      4. Verify: HTML response with Vue app
    Expected Result: Dev server running
    Evidence: .sisyphus/evidence/task-18-dev-server.txt
  ```

  **Commit**: YES
  - Message: `feat(admin-frontend): initialize Vue 3 + Vite project`
  - Files: `admin/package.json`, `admin/vite.config.js`, `admin/src/main.js`, `admin/src/App.vue`, `admin/index.html`
  - Pre-commit: `cd admin && npm run build`

- [ ] 19. Router + Auth Guard + Pinia Store

  **What to do**:
  - Create `admin/src/router/index.js`:
    - Define all routes as specified in README
    - Implement `beforeEach` guard: redirect to `/login` if no token
    - Set page titles from route meta
  - Create `admin/src/stores/user.js`:
    - Pinia store with state: token, adminInfo
    - Actions: login, logout, getInfo
    - Persist token to localStorage
  - Create `admin/src/utils/auth.js`:
    - getToken(), setToken(), removeToken() helpers

  **Must NOT do**:
  - Do NOT add token refresh logic (keep it simple)
  - Do NOT add route-level permissions (admin-only app)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Standard router and store setup
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 3 (after Task 18)
  - **Blocks**: Tasks 20-33
  - **Blocked By**: Task 18

  **References**:
  - `admin/README.md` section 4 - Route design
  - `admin/README.md` section 10.2 - Login page example

  **Acceptance Criteria**:
  - [ ] All routes defined with correct paths and components
  - [ ] Auth guard redirects unauthenticated users to `/login`
  - [ ] Pinia store manages token and adminInfo
  - [ ] Token persisted to localStorage

  **QA Scenarios**:
  ```
  Scenario: Route guard works
    Tool: Bash (curl)
    Preconditions: Dev server running
    Steps:
      1. Run: curl http://localhost:5173/dashboard
      2. Verify: Redirects to /login (or returns login page HTML)
    Expected Result: Unauthenticated access blocked
    Evidence: .sisyphus/evidence/task-19-route-guard.txt

  Scenario: All routes accessible
    Tool: Bash (curl)
    Preconditions: Dev server running, admin logged in
    Steps:
      1. For each route: /dashboard, /article, /music, /healing, /topic, /quiz, /user, /chat, /notice, /ai-config
      2. Verify: Each returns HTML response
    Expected Result: All routes render
    Evidence: .sisyphus/evidence/task-19-routes.txt
  ```

  **Commit**: YES
  - Message: `feat(admin-frontend): add router with auth guard and Pinia store`
  - Files: `admin/src/router/index.js`, `admin/src/stores/user.js`, `admin/src/utils/auth.js`
  - Pre-commit: `cd admin && npm run build`

- [ ] 20. API Layer (11 Modules)

  **What to do**:
  - Create `admin/src/api/request.js`:
    - Axios instance with baseURL `/api`, timeout 15000
    - Request interceptor: inject Bearer token
    - Response interceptor: unwrap data, handle 401 (redirect to login), show error messages
  - Create 11 API modules:
    - `admin.js`: login, getInfo
    - `dashboard.js`: getStats
    - `article.js`: CRUD + status toggle
    - `music.js`: CRUD
    - `healing.js`: CRUD
    - `topic.js`: list, detail, status, replies
    - `quiz.js`: CRUD + batch questions
    - `user.js`: list, detail, quizzes, chats
    - `chat.js`: list, userChat
    - `notice.js`: CRUD
    - `upload.js`: uploadFile

  **Must NOT do**:
  - Do NOT add retry logic
  - Do NOT add request cancellation
  - Do NOT add loading state management in API layer

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Standard API module creation
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 3 (after Task 18)
  - **Blocks**: Tasks 21-33
  - **Blocked By**: Task 18

  **References**:
  - `admin/README.md` section 5 - Axios wrapper
  - `admin/README.md` section 10.3-10.4 - API examples

  **Acceptance Criteria**:
  - [ ] `request.js` handles token injection and 401 redirect
  - [ ] All 11 API modules created with correct endpoints
  - [ ] Each function matches README endpoint specification

  **QA Scenarios**:
  ```
  Scenario: API modules compile
    Tool: Bash (npm)
    Preconditions: Project initialized
    Steps:
      1. Run: cd admin && npm run build
      2. Verify: No import errors
    Expected Result: All API modules importable
    Evidence: .sisyphus/evidence/task-20-api-build.txt

  Scenario: Request interceptor adds token
    Tool: Bash (curl)
    Preconditions: Dev server running, token in localStorage
    Steps:
      1. Open browser DevTools Network tab
      2. Make API request
      3. Verify: Authorization header present
    Expected Result: Token injected automatically
    Evidence: .sisyphus/evidence/task-20-token-interceptor.txt
  ```

  **Commit**: YES
  - Message: `feat(admin-frontend): implement API layer with 11 modules`
  - Files: `admin/src/api/request.js`, `admin/src/api/*.js` (11 files)
  - Pre-commit: `cd admin && npm run build`

- [ ] 21. Login Page

  **What to do**:
  - Create `admin/src/views/Login/index.vue`:
    - Centered card with login form
    - Username input with User icon
    - Password input with Lock icon, show-password toggle
    - Login button with loading state
    - Form validation (required fields)
    - Call `POST /api/admin/login` on submit
    - Store token and redirect to `/dashboard` on success
    - Show error message on failure

  **Must NOT do**:
  - Do NOT add "remember me" functionality
  - Do NOT add registration link
  - Do NOT add social login options

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple login form component
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 22-33)
  - **Blocks**: None
  - **Blocked By**: Tasks 19, 20

  **References**:
  - `admin/README.md` section 10.2 - Login page example

  **Acceptance Criteria**:
  - [ ] Login form with username and password fields
  - [ ] Form validation prevents empty submissions
  - [ ] Loading state during API call
  - [ ] Success redirects to `/dashboard`
  - [ ] Failure shows error message

  **QA Scenarios**:
  ```
  Scenario: Login page renders
    Tool: Playwright (or curl)
    Preconditions: Dev server running
    Steps:
      1. Navigate to http://localhost:5173/login
      2. Verify: Login form visible
      3. Verify: Username input present
      4. Verify: Password input present
      5. Verify: Login button present
    Expected Result: Login page rendered correctly
    Evidence: .sisyphus/evidence/task-21-login-page.png

  Scenario: Login form validation
    Tool: Playwright
    Preconditions: Dev server running
    Steps:
      1. Navigate to /login
      2. Click Login button without entering data
      3. Verify: Validation error messages appear
    Expected Result: Form validation works
    Evidence: .sisyphus/evidence/task-21-login-validation.png
  ```

  **Commit**: YES
  - Message: `feat(admin-frontend): implement login page`
  - Files: `admin/src/views/Login/index.vue`
  - Pre-commit: `cd admin && npm run build`

- [ ] 22. Layout (Sidebar + Header)

  **What to do**:
  - Create `admin/src/views/Layout/index.vue`:
    - el-aside sidebar with el-menu (collapsible)
    - Menu items for all routes (Dashboard, Article, Music, Healing, Topic, Quiz, User, Chat, Notice, AI Config)
    - el-header with admin nickname and logout button
    - el-main with `<router-view />`
  - Add responsive sidebar collapse toggle
  - Style sidebar with logo at top

  **Must NOT do**:
  - Do NOT add breadcrumb navigation
  - Do NOT add full-screen toggle
  - Do NOT add theme switcher

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Standard admin layout component
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 21, 23-33)
  - **Blocks**: None
  - **Blocked By**: Tasks 19, 20

  **References**:
  - `admin/README.md` section 8.2 - Layout design

  **Acceptance Criteria**:
  - [ ] Sidebar with all menu items
  - [ ] Header with admin info and logout
  - [ ] Sidebar collapsible
  - [ ] Router-view renders in main area

  **QA Scenarios**:
  ```
  Scenario: Layout renders with all menu items
    Tool: Playwright
    Preconditions: Dev server running, admin logged in
    Steps:
      1. Navigate to http://localhost:5173/dashboard
      2. Verify: Sidebar visible
      3. Verify: Menu items: Dashboard, Article, Music, Healing, Topic, Quiz, User, Chat, Notice, AI Config
      4. Verify: Header with admin name and logout button
    Expected Result: Layout rendered correctly
    Evidence: .sisyphus/evidence/task-22-layout.png

  Scenario: Sidebar collapse works
    Tool: Playwright
    Preconditions: Dev server running, admin logged in
    Steps:
      1. Click collapse toggle button
      2. Verify: Sidebar collapses to icons only
      3. Click toggle again
      4. Verify: Sidebar expands
    Expected Result: Sidebar toggle works
    Evidence: .sisyphus/evidence/task-22-sidebar-collapse.png
  ```

  **Commit**: YES
  - Message: `feat(admin-frontend): implement layout with sidebar and header`
  - Files: `admin/src/views/Layout/index.vue`
  - Pre-commit: `cd admin && npm run build`

- [ ] 23. Dashboard Page

  **What to do**:
  - Create `admin/src/views/Dashboard/index.vue`:
    - 4 statistic cards in el-row: Total Users, Today New, Total Articles, Total Topics, Total Quiz, Total Chat Messages
    - 2 charts in el-row:
      - Left: 7-day trend line chart (ECharts)
      - Right: Emotion distribution pie chart (ECharts)
  - Call `GET /api/admin/dashboard/stats` on mount
  - Use ECharts for charts (import and initialize)
  - Add loading state while data loads

  **Must NOT do**:
  - Do NOT add date range picker (fixed to last 7 days)
  - Do NOT add export functionality
  - Do NOT add real-time refresh

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Standard dashboard with ECharts
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 21-22, 24-33)
  - **Blocks**: None
  - **Blocked By**: Tasks 19, 20

  **References**:
  - `admin/README.md` section 8.3 - Dashboard design

  **Acceptance Criteria**:
  - [ ] 4 statistic cards display correct data
  - [ ] Line chart shows 7-day trend
  - [ ] Pie chart shows emotion distribution
  - [ ] Loading state while data loads

  **QA Scenarios**:
  ```
  Scenario: Dashboard renders with data
    Tool: Playwright
    Preconditions: Dev server running, admin logged in, backend with data
    Steps:
      1. Navigate to http://localhost:5173/dashboard
      2. Verify: 4 statistic cards visible
      3. Verify: Line chart rendered
      4. Verify: Pie chart rendered
    Expected Result: Dashboard fully rendered
    Evidence: .sisyphus/evidence/task-23-dashboard.png

  Scenario: Dashboard handles empty data
    Tool: Playwright
    Preconditions: Dev server running, admin logged in, empty database
    Steps:
      1. Navigate to /dashboard
      2. Verify: Cards show 0 or appropriate defaults
      3. Verify: Charts render without errors
    Expected Result: Dashboard handles empty state
    Evidence: .sisyphus/evidence/task-23-dashboard-empty.png
  ```

  **Commit**: YES
  - Message: `feat(admin-frontend): implement dashboard with ECharts`
  - Files: `admin/src/views/Dashboard/index.vue`
  - Pre-commit: `cd admin && npm run build`

- [ ] 24. Article Management Pages

  **What to do**:
  - Create `admin/src/views/Article/List.vue`:
    - Search bar: keyword input, status dropdown, query button
    - el-table: ID, title, cover thumbnail, create time, status tag, actions
    - Actions: Edit (route to edit page), Toggle status (el-switch), Delete (el-popconfirm)
    - el-pagination at bottom
  - Create `admin/src/views/Article/Edit.vue`:
    - el-form: title, summary, cover (UploadImage), status (el-radio)
    - Rich text editor (wangeditor) for content
    - Save button (create or update based on route params)
    - Back button

  **Must NOT do**:
  - Do NOT add article preview functionality
  - Do NOT add drag-and-drop sorting
  - Do NOT add batch operations

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Standard CRUD pages with existing components
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 21-23, 25-33)
  - **Blocks**: None
  - **Blocked By**: Tasks 19, 20, 32, 33

  **References**:
  - `admin/README.md` section 8.4 - Article management design

  **Acceptance Criteria**:
  - [ ] List page with search, table, pagination
  - [ ] Edit page with form and rich text editor
  - [ ] Create new article works
  - [ ] Edit existing article works
  - [ ] Toggle status works
  - [ ] Delete with confirmation works

  **QA Scenarios**:
  ```
  Scenario: Article list renders
    Tool: Playwright
    Preconditions: Dev server running, admin logged in
    Steps:
      1. Navigate to http://localhost:5173/article
      2. Verify: Search bar visible
      3. Verify: Table with columns: ID, Title, Cover, Created, Status, Actions
      4. Verify: Pagination visible
    Expected Result: Article list page rendered
    Evidence: .sisyphus/evidence/task-24-article-list.png

  Scenario: Article edit page works
    Tool: Playwright
    Preconditions: Dev server running, admin logged in
    Steps:
      1. Navigate to /article/edit (or /article/edit/1 for edit)
      2. Verify: Form with title, summary, cover, status fields
      3. Verify: Rich text editor visible
      4. Fill in title and content
      5. Click Save
      6. Verify: Success message or redirect
    Expected Result: Article edit page functional
    Evidence: .sisyphus/evidence/task-24-article-edit.png
  ```

  **Commit**: YES
  - Message: `feat(admin-frontend): implement article management pages`
  - Files: `admin/src/views/Article/List.vue`, `admin/src/views/Article/Edit.vue`
  - Pre-commit: `cd admin && npm run build`

- [ ] 25. Music Management Pages

  **What to do**:
  - Create `admin/src/views/Music/List.vue`:
    - el-table: song name, singer, cover, audio URL (truncated), actions
    - Actions: Edit (el-drawer), Add (drawer), Delete
    - el-pagination
  - Create `admin/src/views/Music/Edit.vue` (or use drawer component):
    - el-form: song name, singer, cover (UploadImage), audio URL, playlist
    - Save button

  **Must NOT do**:
  - Do NOT add audio player preview
  - Do NOT add batch upload

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Standard CRUD pages
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 21-24, 26-33)
  - **Blocks**: None
  - **Blocked By**: Tasks 19, 20, 32

  **References**:
  - `admin/README.md` section 8.5 - Music management design

  **Acceptance Criteria**:
  - [ ] List page with table and pagination
  - [ ] Add/Edit drawer with form
  - [ ] Delete with confirmation

  **QA Scenarios**:
  ```
  Scenario: Music list renders
    Tool: Playwright
    Preconditions: Dev server running, admin logged in
    Steps:
      1. Navigate to http://localhost:5173/music
      2. Verify: Table with music entries
      3. Verify: Add button visible
    Expected Result: Music list page rendered
    Evidence: .sisyphus/evidence/task-25-music-list.png
  ```

  **Commit**: YES
  - Message: `feat(admin-frontend): implement music management pages`
  - Files: `admin/src/views/Music/List.vue`, `admin/src/views/Music/Edit.vue`
  - Pre-commit: `cd admin && npm run build`

- [ ] 26. Self-Healing Management Pages

  **What to do**:
  - Create `admin/src/views/Healing/List.vue`:
    - Similar structure to Music list
    - el-table: title, cover, description, duration, actions
    - Actions: Edit, Add, Delete
  - Create `admin/src/views/Healing/Edit.vue`:
    - el-form: title, cover (UploadImage), description (textarea), steps (multi-line), target duration

  **Must NOT do**:
  - Do NOT add step reordering
  - Do NOT add preview functionality

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Standard CRUD pages
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 21-25, 27-33)
  - **Blocks**: None
  - **Blocked By**: Tasks 19, 20, 32

  **References**:
  - `admin/README.md` section 8.6 - Self-healing management design

  **Acceptance Criteria**:
  - [ ] List page with table and pagination
  - [ ] Add/Edit form with all fields
  - [ ] Delete with confirmation

  **QA Scenarios**:
  ```
  Scenario: Healing list renders
    Tool: Playwright
    Preconditions: Dev server running, admin logged in
    Steps:
      1. Navigate to http://localhost:5173/healing
      2. Verify: Table with healing exercises
    Expected Result: Healing list page rendered
    Evidence: .sisyphus/evidence/task-26-healing-list.png
  ```

  **Commit**: YES
  - Message: `feat(admin-frontend): implement self-healing management pages`
  - Files: `admin/src/views/Healing/List.vue`, `admin/src/views/Healing/Edit.vue`
  - Pre-commit: `cd admin && npm run build`

- [ ] 27. Topic Review Pages

  **What to do**:
  - Create `admin/src/views/Topic/List.vue`:
    - Status filter: el-radio-group (All/Pending/Normal/Deleted)
    - el-table: content preview (truncated), publisher openid, image count, reply count, status tag, publish time
    - Actions: View Detail (opens el-dialog)
  - Create `admin/src/views/Topic/Detail.vue` (dialog component):
    - Full post content
    - Image display (el-image with preview)
    - Reply list (el-timeline style)
    - Action buttons: Approve (status=1), Delete (status=2)

  **Must NOT do**:
  - Do NOT add image carousel (use el-image preview)
  - Do NOT add reply editing
  - Do NOT add bulk approve/delete

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Standard review pages
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 21-26, 28-33)
  - **Blocks**: None
  - **Blocked By**: Tasks 19, 20

  **References**:
  - `admin/README.md` section 8.7 - Topic review design

  **Acceptance Criteria**:
  - [ ] List page with status filter
  - [ ] Detail dialog with content, images, replies
  - [ ] Approve/delete actions work

  **QA Scenarios**:
  ```
  Scenario: Topic list with filter
    Tool: Playwright
    Preconditions: Dev server running, admin logged in, topics exist
    Steps:
      1. Navigate to http://localhost:5173/topic
      2. Verify: Status filter visible
      3. Click "Pending" filter
      4. Verify: Table shows only pending topics
    Expected Result: Topic filter works
    Evidence: .sisyphus/evidence/task-27-topic-list.png

  Scenario: Topic detail dialog
    Tool: Playwright
    Preconditions: Dev server running, topic with replies exists
    Steps:
      1. Navigate to /topic
      2. Click "View Detail" on a topic
      3. Verify: Dialog opens with full content
      4. Verify: Images displayed
      5. Verify: Replies listed
    Expected Result: Topic detail dialog works
    Evidence: .sisyphus/evidence/task-27-topic-detail.png
  ```

  **Commit**: YES
  - Message: `feat(admin-frontend): implement topic review pages`
  - Files: `admin/src/views/Topic/List.vue`, `admin/src/views/Topic/Detail.vue`
  - Pre-commit: `cd admin && npm run build`

- [ ] 28. Quiz Management Pages

  **What to do**:
  - Create `admin/src/views/Quiz/List.vue`:
    - el-table: title, type, question count, status toggle, actions
    - Actions: Manage Questions (route to questions page)
  - Create `admin/src/views/Quiz/Questions.vue`:
    - Back button + questionnaire title
    - el-table: question content (truncated), option count, sort order, actions
    - Actions: Edit (el-dialog), Add Question button
    - Save All button (batch save)
  - Edit dialog: question content input, dynamic option list, score input

  **Must NOT do**:
  - Do NOT add question reordering (drag-and-drop)
  - Do NOT add question import/export
  - Do NOT add question preview

  **Recommended Agent Profile**:
  - **Category**: `deep`
    - Reason: Complex batch save logic and dynamic forms
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 21-27, 29-33)
  - **Blocks**: None
  - **Blocked By**: Tasks 19, 20

  **References**:
  - `admin/README.md` section 8.8 - Quiz management design

  **Acceptance Criteria**:
  - [ ] List page with question count
  - [ ] Questions page with table
  - [ ] Edit dialog with dynamic options
  - [ ] Add question works
  - [ ] Batch save replaces all questions

  **QA Scenarios**:
  ```
  Scenario: Quiz list renders
    Tool: Playwright
    Preconditions: Dev server running, admin logged in
    Steps:
      1. Navigate to http://localhost:5173/quiz
      2. Verify: Table with questionnaires
      3. Verify: Question count column
    Expected Result: Quiz list page rendered
    Evidence: .sisyphus/evidence/task-28-quiz-list.png

  Scenario: Question batch save
    Tool: Playwright
    Preconditions: Dev server running, questionnaire exists
    Steps:
      1. Navigate to /quiz/{id}/questions
      2. Click "Add Question"
      3. Fill in question content and options
      4. Click "Save All"
      5. Verify: Success message
      6. Refresh page
      7. Verify: Question saved
    Expected Result: Batch save works
    Evidence: .sisyphus/evidence/task-28-quiz-batch.png
  ```

  **Commit**: YES
  - Message: `feat(admin-frontend): implement quiz management pages`
  - Files: `admin/src/views/Quiz/List.vue`, `admin/src/views/Quiz/Questions.vue`
  - Pre-commit: `cd admin && npm run build`

- [ ] 29. User Management Page

  **What to do**:
  - Create `admin/src/views/User/List.vue`:
    - Search: keyword input (nickname/openid)
    - el-table: nickname, avatar, province/city, gender, registration time, chat count
    - Actions: View Detail (el-drawer)
  - Detail drawer:
    - User basic info
    - Recent quiz records (el-table: quiz name, score, conclusion, time)
    - Recent chat summary (last 10 messages)

  **Must NOT do**:
  - Do NOT add user editing
  - Do NOT add user deletion
  - Do NOT add export functionality

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Read-only user management
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 21-28, 30-33)
  - **Blocks**: None
  - **Blocked By**: Tasks 19, 20

  **References**:
  - `admin/README.md` section 8.9 - User management design

  **Acceptance Criteria**:
  - [ ] List page with search
  - [ ] Detail drawer with user info, quizzes, chats
  - [ ] Search filters correctly

  **QA Scenarios**:
  ```
  Scenario: User list with search
    Tool: Playwright
    Preconditions: Dev server running, admin logged in, users exist
    Steps:
      1. Navigate to http://localhost:5173/user
      2. Verify: User table visible
      3. Enter search keyword
      4. Verify: Table filters
    Expected Result: User search works
    Evidence: .sisyphus/evidence/task-29-user-list.png

  Scenario: User detail drawer
    Tool: Playwright
    Preconditions: User with quiz records exists
    Steps:
      1. Navigate to /user
      2. Click "View Detail" on a user
      3. Verify: Drawer opens
      4. Verify: User info displayed
      5. Verify: Quiz records listed
      6. Verify: Chat messages shown
    Expected Result: User detail drawer works
    Evidence: .sisyphus/evidence/task-29-user-detail.png
  ```

  **Commit**: YES
  - Message: `feat(admin-frontend): implement user management page`
  - Files: `admin/src/views/User/List.vue`
  - Pre-commit: `cd admin && npm run build`

- [ ] 30. Chat Records Page

  **What to do**:
  - Create `admin/src/views/Chat/List.vue`:
    - Filter bar: openid input, date range picker, keyword input, query button
    - el-table: user openid, latest message preview, message count, last active time
    - Actions: View Chat (opens dialog/drawer)
  - Chat detail dialog/drawer:
    - WeChat-style chat bubbles
    - User messages: right-aligned, blue background
    - Assistant messages: left-aligned, gray background
    - Timestamps on each message

  **Must NOT do**:
  - Do NOT add message search within chat
  - Do NOT add chat export
  - Do NOT add message animations

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Read-only chat viewer
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 21-29, 31-33)
  - **Blocks**: None
  - **Blocked By**: Tasks 19, 20

  **References**:
  - `admin/README.md` section 8.10 - Chat records design

  **Acceptance Criteria**:
  - [ ] List page with filters
  - [ ] Chat detail with bubble UI
  - [ ] Messages displayed in correct order

  **QA Scenarios**:
  ```
  Scenario: Chat list renders
    Tool: Playwright
    Preconditions: Dev server running, admin logged in, chat history exists
    Steps:
      1. Navigate to http://localhost:5173/chat
      2. Verify: Filter bar visible
      3. Verify: Chat list table visible
    Expected Result: Chat list page rendered
    Evidence: .sisyphus/evidence/task-30-chat-list.png

  Scenario: Chat detail with bubbles
    Tool: Playwright
    Preconditions: User with chat history exists
    Steps:
      1. Navigate to /chat
      2. Click "View Chat" on a user
      3. Verify: Dialog opens with chat messages
      4. Verify: User messages right-aligned (blue)
      5. Verify: Assistant messages left-aligned (gray)
    Expected Result: Chat bubble UI works
    Evidence: .sisyphus/evidence/task-30-chat-detail.png
  ```

  **Commit**: YES
  - Message: `feat(admin-frontend): implement chat records page`
  - Files: `admin/src/views/Chat/List.vue`
  - Pre-commit: `cd admin && npm run build`

- [ ] 31. Notice Management Pages

  **What to do**:
  - Create `admin/src/views/Notice/List.vue`:
    - el-table: title, content preview, publish time, status, actions
    - Actions: Add, Edit (el-dialog), Delete
  - Create `admin/src/views/Notice/Edit.vue` (or use dialog):
    - el-form: title, content (textarea), status

  **Must NOT do**:
  - Do NOT add notice scheduling
  - Do NOT add notice templates

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple CRUD pages
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 21-30, 32-33)
  - **Blocks**: None
  - **Blocked By**: Tasks 19, 20

  **References**:
  - `admin/README.md` section 8.11 - Notice management design

  **Acceptance Criteria**:
  - [ ] List page with table
  - [ ] Add/Edit dialog with form
  - [ ] Delete with confirmation

  **QA Scenarios**:
  ```
  Scenario: Notice CRUD works
    Tool: Playwright
    Preconditions: Dev server running, admin logged in
    Steps:
      1. Navigate to http://localhost:5173/notice
      2. Click "Add" button
      3. Fill in title and content
      4. Click Save
      5. Verify: Notice appears in list
    Expected Result: Notice CRUD works
    Evidence: .sisyphus/evidence/task-31-notice-crud.png
  ```

  **Commit**: YES
  - Message: `feat(admin-frontend): implement notice management pages`
  - Files: `admin/src/views/Notice/List.vue`, `admin/src/views/Notice/Edit.vue`
  - Pre-commit: `cd admin && npm run build`

- [ ] 32. UploadImage Component

  **What to do**:
  - Create `admin/src/components/UploadImage.vue`:
    - el-upload component with image preview
    - Accept image files only
    - Call `POST /api/upload` on upload
    - Return uploaded URL via v-model
    - Show loading state during upload
    - Display uploaded image preview

  **Must NOT do**:
  - Do NOT add drag-and-drop upload
  - Do NOT add multiple file upload
  - Do NOT add image cropping

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Standard upload component
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 21-31, 33)
  - **Blocks**: Tasks 24, 25, 26
  - **Blocked By**: Task 18

  **References**:
  - `admin/README.md` section 8.4 - UploadImage usage

  **Acceptance Criteria**:
  - [ ] Image upload works
  - [ ] Preview shows uploaded image
  - [ ] v-model returns image URL
  - [ ] Loading state during upload

  **QA Scenarios**:
  ```
  Scenario: Image upload works
    Tool: Playwright
    Preconditions: Dev server running, admin logged in
    Steps:
      1. Navigate to any page with UploadImage (e.g., /article/edit)
      2. Click upload area
      3. Select image file
      4. Verify: Upload completes
      5. Verify: Image preview shown
    Expected Result: Image upload works
    Evidence: .sisyphus/evidence/task-32-upload-image.png
  ```

  **Commit**: YES
  - Message: `feat(admin-frontend): implement UploadImage component`
  - Files: `admin/src/components/UploadImage.vue`
  - Pre-commit: `cd admin && npm run build`

- [ ] 33. RichTextEditor Component

  **What to do**:
  - Create `admin/src/components/RichTextEditor.vue`:
    - Wrap @wangeditor/editor-for-vue
    - Configure toolbar with basic formatting options
    - Support image upload via UploadImage
    - v-model for content binding
    - HTML output format

  **Must NOT do**:
  - Do NOT add custom toolbar plugins
  - Do NOT add markdown mode
  - Do NOT add video upload

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Standard wangeditor wrapper
  - **Skills**: []

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 21-32)
  - **Blocks**: Task 24
  - **Blocked By**: Task 18

  **References**:
  - `admin/README.md` section 8.4 - RichTextEditor usage

  **Acceptance Criteria**:
  - [ ] Editor renders with toolbar
  - [ ] Content editable
  - [ ] v-model works
  - [ ] Image upload works in editor

  **QA Scenarios**:
  ```
  Scenario: Rich text editor works
    Tool: Playwright
    Preconditions: Dev server running, admin logged in
    Steps:
      1. Navigate to http://localhost:5173/article/edit
      2. Verify: Editor toolbar visible
      3. Type content in editor
      4. Verify: Content editable
      5. Click image upload
      6. Verify: Image inserted
    Expected Result: Rich text editor works
    Evidence: .sisyphus/evidence/task-33-rich-text-editor.png
  ```

  **Commit**: YES
  - Message: `feat(admin-frontend): implement RichTextEditor component`
  - Files: `admin/src/components/RichTextEditor.vue`
  - Pre-commit: `cd admin && npm run build`

---

## Final Verification Wave

> 4 review agents run in PARALLEL. ALL must APPROVE. Present consolidated results to user and get explicit "okay" before completing.

- [ ] F1. **Backend Compilation Verification** — `quick`
  Run `./.tools/apache-maven-3.9.9/bin/mvn compile` and verify zero errors. Check that all new Java files compile correctly. Verify no warnings about missing dependencies.
  Output: `Compile [PASS/FAIL] | Warnings [0] | VERDICT`

- [ ] F2. **Frontend Build Verification** — `quick`
  Run `cd admin && npm install && npm run build` and verify zero errors. Check that dist/ directory is created with expected output.
  Output: `Build [PASS/FAIL] | Output [dist/ size] | VERDICT`

- [ ] F3. **Integration Testing** — `deep`
  Start backend server. Execute full CRUD cycle for each module: Article, Music, Healing, Topic, Quiz, Notice. Test admin login, dashboard stats, user list, chat list. Verify all endpoints return correct HTTP status codes and response structures.
  Output: `Endpoints [N/N pass] | CRUD [N/N complete] | VERDICT`

- [ ] F4. **Scope Fidelity Check** — `deep`
  Compare implemented features against README spec. Verify all 40 endpoints exist. Verify all 12 pages exist. Check "Must NOT" compliance: no modified existing endpoints, no TypeScript, no tests. Flag any unaccounted changes.
  Output: `Endpoints [N/40] | Pages [N/12] | Compliance [PASS/FAIL] | VERDICT`

---

## Commit Strategy

- **Wave 1**: `feat(admin-backend): add database migrations, entities, and JWT role support`
- **Wave 2**: `feat(admin-backend): implement 40 admin API endpoints`
- **Wave 3**: `feat(admin-frontend): build Vue 3 admin management system`
- **Final**: `docs(admin): update README with deployment guide`

---

## Success Criteria

### Verification Commands
```bash
# Backend compilation
./.tools/apache-maven-3.9.9/bin/mvn compile  # Expected: BUILD SUCCESS

# Frontend build
cd admin && npm run build  # Expected: Build completed successfully

# Admin login test
curl -X POST http://localhost:8080/api/admin/login -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123"}'
# Expected: {"code":200,"data":{"token":"eyJ...","admin":{...}}}

# Dashboard stats test
curl http://localhost:8080/api/admin/dashboard/stats -H "Authorization: Bearer <token>"
# Expected: {"code":200,"data":{"totalUsers":N,"todayNew":N,...}}
```

### Final Checklist
- [ ] All 40 backend endpoints implemented and functional
- [ ] All 12 frontend pages built and rendering
- [ ] Admin login/logout flow works end-to-end
- [ ] Existing mini-program endpoints unchanged
- [ ] SQL migrations execute cleanly
- [ ] No TypeScript, no tests, no over-engineering
- [ ] All "Must NOT Have" constraints respected
