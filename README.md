# Mental Health Miniapp

A campus-oriented mental health support system built with **Spring Boot** and **WeChat Mini Program**.

The project focuses on lightweight psychological support scenarios for college students, including:

- WeChat login and JWT-based authentication
- Mental health resource browsing
- Anonymous tree-hole posting and replies
- Psychological assessment and result history
- AI-based counseling and follow-up interaction
- Personal profile management

## Tech Stack

### Frontend

- WeChat Mini Program native framework
- WXML / WXSS / JavaScript

### Backend

- Spring Boot 3
- MyBatis-Plus
- MySQL
- JWT
- springdoc-openapi

## Main Modules

- `pages/login`: WeChat login
- `pages/station`: mental health resource hub
- `pages/topic-list` / `pages/topic-detail`: anonymous tree-hole community
- `pages/quiz-list` / `pages/quiz-detail` / `pages/quiz-result`: psychological assessments
- `pages/ai-chat`: AI counseling
- `pages/profile`: user profile

Backend modules include:

- login and token validation
- article / music / self-healing resource services
- topic and reply services
- questionnaire, question, quiz result, and chat history services
- AI completion client and chat orchestration

## Project Structure

```text
.
- app.js / app.json / app.wxss          # Mini Program root
- pages/                                # Mini Program pages
- components/                           # Reusable frontend components
- utils/                                # Frontend request/config helpers
- src/main/java/com/example/demo/       # Spring Boot backend
- src/main/resources/                   # Backend configuration
- src/test/java/                        # Backend tests
- sql/                                  # Database schema and seed scripts
- pom.xml                               # Maven project file
```

## Quick Start

### 1. Prepare the database

Create a MySQL database, for example:

```sql
CREATE DATABASE graduation_design DEFAULT CHARACTER SET utf8mb4;
```

Then import the SQL scripts under `sql/`.

At minimum, you should prepare the core schema and required seed data according to your local setup.

### 2. Prepare backend local config

Copy:

```text
src/main/resources/application-local.yml.example
```

to:

```text
src/main/resources/application-local.yml
```

and fill in your local values, including:

- MySQL password
- JWT secret
- WeChat Mini Program `appid` / `secret`
- AI API endpoint / key / model

Do **not** commit `application-local.yml`.

### 3. Start the backend

Requirements:

- JDK 17
- Maven 3.9+
- MySQL 8+

Run:

```bash
mvn spring-boot:run
```

The backend default port is:

```text
8080
```

If startup succeeds, you can access:

- API docs: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### 4. Configure the Mini Program frontend

Open the project in WeChat DevTools.

Pay attention to:

- `project.private.config.json` is intentionally not committed
- backend base URL is resolved in `utils/config.js`
- real-device debugging may require updating `LAN_HOST` in `utils/config.js` to your current local IPv4 address

### 5. Run the Mini Program

After backend and frontend config are ready:

- open the project with WeChat DevTools
- build and preview in the simulator
- if testing on a real device, ensure the phone and computer are on the same LAN

## Tests

The backend currently includes controller-level tests such as:

- `HealthControllerTest`
- `ArticleControllerTest`
- `ConsultChatControllerStandaloneTest`

Run tests with:

```bash
mvn test
```

## Security Notes

- Real local secrets are not committed
- `application-local.yml` is ignored
- `project.private.config.json` is ignored
- thesis documents, recordings, temporary files, and local tool directories are ignored

Before sharing or deploying this project, re-check:

- database credentials
- WeChat `AppSecret`
- AI API keys
- frontend base URL settings

## Current Status

This repository contains the codebase for a complete graduation project implementation, covering frontend pages, backend APIs, database scripts, and basic tests.

It is suitable for:

- project demonstration
- code review
- graduation design presentation
- secondary development based on the current architecture
