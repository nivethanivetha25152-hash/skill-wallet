# Skill Wallet — Full-stack starter project

**Tagline:** Learn. Exchange. Earn. Grow.

A professional-looking starter for a peer-to-peer skill exchange and learning platform. It includes a responsive React dashboard with all planned areas, a Java Spring Boot REST API scaffold, and a MySQL schema with seed/demo records.

> **Important:** This is a development starter, not a production-ready service yet. Several frontend actions are demo interactions and authentication, Google OAuth, email delivery, real-time chat, quiz scoring, certificate issuance, wallet ledger rules, and admin authorization still need to be implemented and tested before real users use it. Demo jobs and profile data are not real opportunities or accounts.

## Features represented in the UI

- Overview dashboard, progress, weekly activity and recommendations
- Skill marketplace, search and category filters, offer-skill draft modal
- Requests workspace with pending/cancelled demo request tracking and chat/session shortcuts
- Login page with Forgot Password and Continue with Google options (OAuth/email delivery remain setup placeholders)
- Wallet overview and transaction history preview
- Session bookings and session history
- Learning paths, lesson progress previews and quiz entry point
- Achievement badges and streaks
- Career centre: resume builder, interview practice, jobs and portfolio entry points
- Community feed, topics and post interactions
- Messages preview
- Admin analytics, member list and moderation preview
- Responsive desktop/tablet/mobile layouts
- Toast feedback and backend connectivity indicator

## Stack

- Frontend: React 18, Vite, Lucide icons, CSS
- Backend: Java 17, Spring Boot 3.5, Spring Web, Validation, JDBC
- Database: MySQL 8+

## Project structure

```text
skill-wallet-pro/
├── frontend/
│   ├── index.html
│   ├── package.json
│   └── src/
│       ├── main.jsx
│       └── styles.css
├── backend/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/skillwallet/api/
│       │   ├── SkillWalletApplication.java
│       │   └── ApiController.java
│       └── resources/
│           ├── application.properties
│           ├── schema.sql
│           └── data.sql
└── README.md
```

## Requirements

Install these first:

1. Node.js 20+ (Node 22 LTS recommended)
2. Java JDK 17+
3. MySQL Server 8+ and MySQL Workbench (or another MySQL client)
4. Maven 3.9+ (or generate/use a Maven wrapper from your IDE)
5. VS Code with the Java Extension Pack (recommended)

## Step 1 — Open in VS Code

1. Extract the ZIP.
2. In VS Code, choose **File → Open Folder** and select `skill-wallet-pro`.
3. Open a terminal from **Terminal → New Terminal**.

## Step 2 — Start MySQL

Make sure your MySQL server is running. The default local configuration expects:

- Database: `skill_wallet` (created automatically if the MySQL user has permission)
- Username: `root`
- Password: blank

If your MySQL root account has a password, set environment variables before starting the backend in the same terminal. In PowerShell:

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="YOUR_MYSQL_PASSWORD"
$env:DB_URL="jdbc:mysql://localhost:3306/skill_wallet?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
```

Change `YOUR_MYSQL_PASSWORD` to your own password locally. Never commit real passwords to GitHub.

The backend runs `schema.sql` and `data.sql` at startup. Seed users use `example.local` email addresses and non-login placeholder strings. They are **not usable login accounts**.

## Step 3 — Start the backend

Open a terminal in VS Code:

```powershell
cd backend
mvn spring-boot:run
```

The API should start on `http://localhost:8080`.

Check the database/API health endpoint in your browser:

`http://localhost:8080/api/health`

Expected response resembles:

```json
{
  "status": "UP",
  "service": "skill-wallet-api",
  "database": "connected"
}
```

If `mvn` is not recognized, install Maven or open the backend folder in IntelliJ IDEA and run `SkillWalletApplication`. If MySQL authentication fails, correct the environment variables above.

## Step 4 — Start the frontend

Open a **second** VS Code terminal:

```powershell
cd frontend
npm install
npm run dev
```

Open the local URL Vite prints, usually `http://localhost:5173`.

The page can run in demo mode even if the backend is offline. The footer shows whether the API health check succeeded. Requests and wallet preview values persist in browser localStorage. Google sign-in and password-reset controls are UI options only until OAuth/email services are configured.

## Main website pages\n\nThe sidebar contains Overview, Explore skills, My wallet, Sessions, Requests, Learning paths, Achievements, Career centre, Community, Messages, and Admin panel. The Requests page currently tracks demo requests in the browser; the backend has a skill-request API scaffold, but full cross-user notification/acceptance wiring is not yet complete.\n\n## API endpoints included in this starter

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/health` | API and database health |
| GET | `/api/overview` | Admin/overview counters |
| GET | `/api/skills` | List active skills |
| POST | `/api/skills` | Create a skill listing |
| POST | `/api/skill-requests` | Create a skill request |
| GET | `/api/wallet/{userId}` | Wallet balance and recent ledger rows |
| GET | `/api/learning-paths` | Published learning paths |
| GET | `/api/jobs` | Published job listings |
| GET | `/api/community/posts` | Published community posts |
| POST | `/api/community/posts` | Create a community post |
| GET | `/api/sessions` | Session list |

Example skill creation body:

```json
{
  "ownerId": 1,
  "name": "Spring Boot Basics",
  "category": "Development",
  "level": "Beginner friendly",
  "description": "Learn REST APIs and dependency injection.",
  "points": 60
}
```

Example community post body:

```json
{
  "authorId": 1,
  "title": "My first Java project",
  "body": "I finished a small console application today.",
  "tags": "Java,Learning"
}
```

## Database tables scaffolded

Users, skills, skill requests, wallet transactions, sessions, learning paths, lesson progress, quizzes, quiz attempts, certificates, jobs, community posts, comments, direct messages, notifications, achievements, user achievements, skill verifications and moderation reports.

## Before real deployment

Complete these security/product tasks before using real accounts:

- Implement Spring Security with password hashing (BCrypt), registration/login, session/JWT handling, password reset and Google OAuth.
- Enforce authorization on every user/admin API. The current admin page is a UI preview, not a security boundary.
- Implement wallet transactions in database transactions with server-side balance checks, idempotency and an auditable ledger; never trust point values sent by a browser.
- Add DTO validation, pagination, error responses, rate limiting and audit logging.
- Add persisted session booking workflows, secure chat/WebSockets, notification delivery and email configuration.
- Implement quiz questions/scoring, certificate verification and moderation actions.
- Replace demo jobs with verified sources and add reporting/consent/privacy policies.
- Add automated tests, environment-specific configuration, database backups, HTTPS and secrets management.

## Troubleshooting

- **`vite is not recognized`**: run `npm install` inside `frontend`, then `npm run dev`.
- **Port 8080 is already in use**: stop the other Java app or change `server.port` in `application.properties`.
- **MySQL access denied**: set `DB_USERNAME` and `DB_PASSWORD` correctly.
- **Frontend says API offline**: start the backend and verify `http://localhost:8080/api/health`.
- **SQL startup error**: check MySQL version and the first SQL error in the backend terminal; schema initialization is strict to help expose errors.
