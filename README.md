# VacancyApp (Spring Boot)

The Spring Boot phase of a database-led vacancy board. **Oracle owns all business logic**
(expiry, archive, purge, description normalization, digest generation, aggregation, scheduled
PL/SQL jobs). This app is only an **API layer, database consumer, UI backend, and Slack producer** —
it consumes Oracle stored procedures / `SYS_REFCURSOR` outputs and never re-implements PL/SQL logic.

## What it does

- `GET  /vacancies/available` — currently open, non-expired vacancies (via `GET_AVAILABLE_VACANCIES`).
- `POST /vacancies` — HR creates a vacancy (insert into `vacant_jobs`).
- `POST /vacancies/{jobId}/applications` — a candidate applies (insert into `job_applications`).
- Browser vacancy board at `http://localhost:8080/`.
- **Every 5 minutes** it calls `GET_AVAILABLE_VACANCIES`, `GET_PENDING_NOTIFICATIONS`,
  and `GET_VACANCY_DASHBOARD` (three REF cursors). If pending notifications reach **7**, it sends
  the digest to Slack immediately.
- **Every 15 minutes** it sends two Slack messages: a vacancy dashboard and a notification digest
  (up to 7 pending). On a successful digest it calls `MARK_NOTIFICATION_SENT` for each notification;
  on failure it calls `MARK_NOTIFICATION_FAILED`.

## Prerequisites

- JDK 17+, Maven, and the Oracle schema (`vacant_jobs`, `job_applications`, `notifications`,
  plus the 14 procedures/functions) already deployed in `FREEPDB1`.

## Configuration (secrets stay out of source)

1. Copy `sample.env` to `.env` in the project root and fill in real values:
   ```
   DB_URL=jdbc:oracle:thin:@localhost:1521/FREEPDB1
   DB_USERNAME=vacancy_app
   DB_PASSWORD=your-password
   SLACK_WEBHOOK_URL=https://hooks.slack.com/services/XXX/YYY/ZZZ
   SLACK_ENABLED=true
   ```
2. Add `.env` to your `.gitignore` so it is never committed.
3. `.env` is loaded automatically at startup by `spring-dotenv`.

### Creating the Slack webhook (do once, manually)

1. Go to <https://api.slack.com/apps> → **Create New App** → *From scratch*.
2. Enable **Incoming Webhooks**, click **Add New Webhook to Workspace**, pick a channel.
3. Copy the generated webhook URL into `SLACK_WEBHOOK_URL` in `.env`.

To run without Slack (e.g. first local test), set `SLACK_ENABLED=false` — messages are logged instead.

## Run

```
mvn clean package
mvn spring-boot:run
```

Then open <http://localhost:8080/>.

### Quick API checks

```
curl http://localhost:8080/vacancies/available

curl -X POST http://localhost:8080/vacancies \
  -H "Content-Type: application/json" \
  -d '{"title":"Java Developer","description":"Build APIs","department":"Engineering","expiresAt":"2026-12-31T17:00:00"}'

curl -X POST http://localhost:8080/vacancies/1/applications \
  -H "Content-Type: application/json" \
  -d '{"applicantName":"Jane Doe","applicantEmail":"jane@example.com"}'
```

Error responses: `404` unknown job, `409` job not open/expired, `400` invalid input.
