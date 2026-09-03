# Vacancy Notification Consumer

Spring Boot command-line consumer for the PL/SQL + Spring Boot vacancy notification workflow.

The Oracle database owns the vacancy rules, triggers, scheduler jobs, and notification queue. This Java application only consumes pending notification rows, sends them to Slack, and marks successfully delivered rows as `SENT`.

## Tech Stack

- Java 17
- Spring Boot 3.3.5
- Spring JDBC
- Oracle JDBC driver `ojdbc11`
- Slack Incoming Webhook API

## How It Works

1. Starts Spring Boot as a non-web application.
2. Runs once through `CommandLineRunner`.
3. Opens an Oracle connection from the configured `DataSource`.
4. Calls `GET_PENDING_NOTIFICATIONS(p_notifications OUT SYS_REFCURSOR)` with `CallableStatement`.
5. Reads each pending row from the returned cursor in database order.
6. Builds a Slack message using `notification_id`, `related_job_id`, `subject`, `body`, `status`, and `created_at`.
7. Uses a special Slack title/body layout when the subject is `Daily Job Applications Digest`.
8. Sends the message to Slack through `SLACK_WEBHOOK_URL`.
9. Updates the row to `status = 'SENT'` and `sent_at = SYSTIMESTAMP` only after Slack returns a successful HTTP response.
10. Leaves the row as `PENDING` if Slack delivery fails so it can be retried later.

The app does not expose REST endpoints and does not keep running as a server because `spring.main.web-application-type=none` is configured.

## Project Structure

```text
vacancy-notification-consumer/
  pom.xml
  README.md
  .env.example
  src/main/java/com/vacancy/consumer/
    VacancyNotificationConsumerApplication.java
    NotificationConsumer.java
    SlackService.java
  src/main/resources/
    application.properties
  plsql_and_erd/
    erd/ERD.png
    plsql/01_create_tables.sql
    plsql/02_seed_data.sql
    plsql/03_custom_lowercase_function.sql
    plsql/04_procedures.sql
    plsql/05_triggers.sql
    plsql/06_scheduled_jobs.sql
    plsql/07_notification_cursor.sql
```

## Prerequisites

- JDK 17.
- An Oracle database reachable from this machine.
- A Slack incoming webhook URL.
- The database objects from `plsql_and_erd/plsql` applied in numeric order.

The Maven wrapper is included, so a separate Maven installation is not required.

## Configuration

Credentials are read from environment variables. For local runs, use `.env.example` as the template for a `.env` file.

```text
DB_URL=jdbc:oracle:thin:@//host:port/service
DB_USERNAME=your_database_user
DB_PASSWORD=your_database_password
SLACK_WEBHOOK_URL=https://hooks.slack.com/services/your/webhook/url
```

`application.properties` imports both `vacancy-notification-consumer/.env` and `.env` as optional files. This supports running from either the repository root or the module directory.

Do not commit real credentials. The local `.env` file is ignored by Git.

## Database Setup

Run the SQL scripts in `plsql_and_erd/plsql` in this order:

1. `01_create_tables.sql`
2. `02_seed_data.sql`
3. `03_custom_lowercase_function.sql`
4. `04_procedures.sql`
5. `05_triggers.sql`
6. `06_scheduled_jobs.sql`
7. `07_notification_cursor.sql`

The Spring Boot consumer specifically depends on the `notifications` table and this procedure:

```sql
GET_PENDING_NOTIFICATIONS(p_notifications OUT SYS_REFCURSOR)
```

The procedure must return these columns:

```text
notification_id, related_job_id, subject, body, created_at, status, sent_at
```

Successfully sent rows are marked with this update in `NotificationConsumer.java`:

```sql
UPDATE NOTIFICATIONS
   SET status = 'SENT',
       sent_at = SYSTIMESTAMP
 WHERE notification_id = ?
```

If the notification table name changes, update `MARK_SENT_SQL` in `NotificationConsumer.java`.

## Run

From the module directory:

```bash
cd vacancy-notification-consumer
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
cd vacancy-notification-consumer
.\mvnw.cmd spring-boot:run
```

## Build Jar

From the module directory:

```bash
./mvnw clean package
java -jar target/vacancy-notification-consumer-0.0.1-SNAPSHOT.jar
```

## Runtime Behavior

- If there are no pending notifications, the app exits without sending Slack messages.
- If Slack returns a non-2xx response, the current notification remains `PENDING`.
- If Slack succeeds but the database update fails, the SQL exception is allowed to fail the run.
- Console output shows which notification is being sent and whether it was marked as `SENT`.

## Important Files

- `pom.xml`: Java version, Spring Boot parent, Spring JDBC, Oracle JDBC, and Spring Boot Maven plugin.
- `application.properties`: app name, `.env` imports, Oracle datasource settings, Slack webhook setting, and non-web mode.
- `VacancyNotificationConsumerApplication.java`: starts Spring Boot, runs the consumer, then exits cleanly.
- `NotificationConsumer.java`: calls the Oracle cursor procedure, formats Slack messages, and marks successful notifications as sent.
- `SlackService.java`: posts JSON payloads to the Slack incoming webhook.
- `plsql_and_erd/plsql/07_notification_cursor.sql`: defines the cursor procedure used by the consumer.

## Troubleshooting

- Missing `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, or `SLACK_WEBHOOK_URL` will prevent startup.
- Oracle connection failures usually mean the JDBC URL, service name, network access, or credentials are incorrect.
- `GET_PENDING_NOTIFICATIONS` errors mean the PL/SQL script was not applied or the procedure is invalid.
- Slack HTTP errors mean the webhook URL is invalid, revoked, or blocked.
- Repeated Slack messages usually mean the row was not marked as `SENT`; check database permissions and `MARK_SENT_SQL`.
