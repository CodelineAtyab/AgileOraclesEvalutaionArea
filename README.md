# The Vacancy Loop

The Vacancy Loop is an Oracle PL/SQL and Spring Boot project that automates the lifecycle of job vacancies.

HR only inserts a vacancy. After that, the database handles the rest automatically.

## Main Features

- Automatically creates a notification when a vacancy is posted
- Converts updated job descriptions to lowercase
- Expires vacancies based on `expires_at`
- Archives expired vacancies
- Removes archived vacancies from the live board
- Periodically cleans live vacancy descriptions
- Stores job applications with foreign key validation
- Creates a daily application digest
- Exposes pending notifications through a PL/SQL `SYS_REFCURSOR`
- Spring Boot consumes pending notifications and sends them to Slack
- Successfully delivered notifications are marked as `SENT`

## Technologies

- Oracle SQL / PL/SQL
- Oracle `DBMS_SCHEDULER`
- Java 17
- Spring Boot
- Maven
- JDBC
- Slack Incoming Webhook

## Database Tables

- `VACANT_JOBS`
- `JOB_APPLICATIONS`
- `JOBS_POSTING_ARCHIVE`
- `NOTIFICATIONS`

## Main Flow

```text
HR INSERT
   ↓
VACANT_JOBS
   ↓
Database automation
   ↓
NOTIFICATIONS
   ↓
Spring Boot Consumer
   ↓
Slack
   ↓
Notification marked SENT