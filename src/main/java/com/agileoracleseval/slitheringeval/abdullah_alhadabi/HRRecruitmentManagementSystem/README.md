# HR Recruitment Notification Worker

## Purpose

This Java 17 Spring Boot application is the notification consumer for the HR Recruitment Management System. It polls Oracle for pending notifications and delivers them to Slack. It uses Maven, Spring Boot, Spring JDBC, and the Oracle JDBC driver.

The application does not create, alter, or drop Oracle tables, triggers, procedures, functions, or scheduler jobs.

## Oracle setup and script order

The Oracle scripts are in [`sql/`](sql/). The editable ERD source is [`docs/HR-Recruitment-ERD.drawio`](docs/HR-Recruitment-ERD.drawio), and its viewable image is [`docs/HR-Recruitment-ERD.png`](docs/HR-Recruitment-ERD.png). Run the scripts in this order, preserving their existing names:

1. `00-progress-check.sql`
2. `02a-schema.sql`
3. `02b-seed.sql`
4. `03-triggers.sql`
5. `04a-event-procedures.sql`
6. `04b-lifecycle-procedures.sql`
7. `04c-digest-and-handover.sql`
8. `05-lowercase-function.sql`
9. `07-scheduler-jobs.sql`

These scripts provision the existing Oracle database; the Spring Boot application itself does not run DDL. Before running the consumer, ensure the schema is available and these procedures can be called by `HR_RECRUITMENT`:

1. `GET_PENDING_NOTIFICATIONS(OUT SYS_REFCURSOR)`
2. `MARK_NOTIFICATION_SENT(notification_id)`
3. `RECORD_NOTIFICATION_FAILURE(notification_id)`

Do not run DDL from this application.

## Spring Boot setup

Open this directory as a Maven project in IntelliJ IDEA and use Java 17. The application entry point is `com.hrrecruitment.notifications.HrRecruitmentApplication`.

The shared IntelliJ run configuration is [`.run/HR Recruitment Application.run.xml`](.run/HR%20Recruitment%20Application.run.xml). Keep real credentials only in your local IDE environment-variable settings; do not save them in this shared file. If you create a local shared-style run configuration, use a `-local.run.xml` suffix so Git ignores it.

## Environment variables

| Variable | Required | Default |
| --- | --- | --- |
| `DB_URL` | No | `jdbc:oracle:thin:@localhost:1523/FREEPDB1` |
| `DB_USERNAME` | No | `HR_RECRUITMENT` |
| `DB_PASSWORD` | Yes | None; supply locally at runtime |
| `SLACK_WEBHOOK_URL` | Yes | None; supply locally at runtime |

`application.properties` reads the password and Slack URL from environment variables only. Never put real secrets in source code, README files, committed run configurations, or `.env` files.

## Run

In IntelliJ IDEA, select **HR Recruitment Application**, add your local `DB_PASSWORD` and `SLACK_WEBHOOK_URL` under **Run | Edit Configurations | Environment variables**, then click **Run**.

For a terminal session, set the two secret environment variables locally and run:

```powershell
mvn spring-boot:run
```

The scheduled consumer polls every 60 seconds (and performs its first poll when the application starts).

## Test

Run the unit tests with:

```powershell
mvn test
```

The tests use mocks and do not require Oracle or Slack credentials. They verify that a successful Slack send marks the notification as sent, while a failed send records the failure and does not mark it as sent.

## Database-to-Slack flow

1. The repository calls `GET_PENDING_NOTIFICATIONS` and reads its Oracle `SYS_REFCURSOR` as a JDBC `ResultSet`.
2. Each pending notification is formatted with the type, subject, and body on separate Slack lines.
3. The sender posts the message to the Slack incoming webhook.
4. Only a Slack HTTP 2xx response triggers `MARK_NOTIFICATION_SENT(notification_id)`.
5. A Slack or network failure triggers `RECORD_NOTIFICATION_FAILURE(notification_id)`; the notification is never marked sent by the application in that case.
