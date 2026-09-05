# The Vacancy Loop

An Oracle PL/SQL and Spring Boot recruitment workflow that automates vacancy notifications, description normalization, expiry, archiving, purging, daily application digests, and Slack delivery.

## Technology Stack

- Oracle Database
- PL/SQL
- Oracle DBMS_SCHEDULER
- Java 17
- Spring Boot 4.1.1
- Spring JDBC
- Maven
- Slack Incoming Webhooks

## System Flow

1. HR inserts a vacancy into `vacant_jobs`.
2. A database trigger creates a `PENDING` notification automatically.
3. Updated descriptions are converted to lowercase using a custom PL/SQL function.
4. Scheduled jobs expire old vacancies, archive them, and purge them safely.
5. A daily scheduler job creates one application digest for all active vacancies.
6. The Spring Boot consumer retrieves pending notifications through a `SYS_REFCURSOR`.
7. Each notification is sent to Slack.
8. A notification is marked `SENT` only after Slack confirms successful delivery.

## Project Structure

```text
hr-notification-consumer/
├── database/
│   ├── 01_schema_and_seed.sql
│   ├── 02_core_routines.sql
│   ├── 03_triggers.sql
│   ├── 04_workflow_procedures.sql
│   ├── 05_scheduler_jobs.sql
│   └── 06_verification.sql
├── docs/
│   └── vacancy-loop-erd.png
├── src/
│   └── main/
│       ├── java/org/example/hrnotificationconsumer/
│       │   ├── HrNotificationConsumerApplication.java
│       │   ├── NotificationConsumer.java
│       │   ├── NotificationMessage.java
│       │   ├── NotificationRepository.java
│       │   └── SlackClient.java
│       └── resources/
│           └── application.properties
├── pom.xml
└── README.md