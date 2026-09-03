# VacancyLoop

An Oracle PL/SQL recruitment workflow with a Java Spring Boot consumer that
sends pending database notifications to Slack.

Oracle manages vacancies, applications, archiving and notification creation.
Java retrieves pending notifications, posts them to a Slack incoming webhook,
and marks each successful delivery as sent.

## Entity relationship diagram

View the [VacancyLoop ERD (PDF)](docs/VacancyLoop_ERD.pdf).

## Requirements

- Java 17 and an Oracle database supporting identity columns and DBMS_SCHEDULER
  (the development database uses Oracle 19c).
- Oracle SQL Developer for database setup.
- An administrator-provisioned, dedicated empty schema with CREATE SESSION,
  CREATE TABLE, CREATE SEQUENCE, CREATE PROCEDURE, CREATE TRIGGER and CREATE JOB,
  plus an appropriate tablespace quota.
- A Slack incoming webhook for the channel receiving notifications.
- Network access to Oracle and Slack, and Maven repositories for the first build.

The Maven wrapper is included; a separate Maven installation is not required.
The project uses Spring Boot 4.1.1, Spring JDBC and the Oracle JDBC driver.

## 1. Set up the database once

1. Connect to the dedicated schema in Oracle SQL Developer. Do not use SYS or
   SYSTEM, or a schema already containing the working VacancyLoop objects.
2. Open a worksheet on that connection and enter
   `@"/full/path/to/VacancyLoop/database/setup.sql"`, replacing the path with
   the actual location on your computer.
3. Choose **Run Script (F5)**, not Run Statement.
4. Check Script Output for errors and the final setup-complete message.

Only run `database/setup.sql`. It runs the five numbered scripts in order:
schema, functions/procedures, triggers, seed data, and scheduler jobs. Keep those
scripts together in the database directory. See the
[database guide](database/README.md) for details.

Setup inserts eight technology vacancies with thirty applicants, three to five
per vacancy. Gmail-formatted applicant addresses are sample data; the application
does not email applicants.

Setup also enables the expiry and archive jobs (every minute), description
normalization (every five minutes), and application digest (daily).
These database jobs operate independently of Java.

**Do not rerun setup before each application run.** It is a fresh-install script,
not a database reset. DDL and seed commits mean a failed installation can leave
objects or data behind; resolve errors before retrying.

## 2. Open and configure the Java project

Open this `VacancyLoop` directory, or its `pom.xml`, in IntelliJ as a Maven
project. Use this project's POM rather than the evaluation repository's outer
POM, and set the project SDK to Java 17.

Create an Application run configuration with main class
`notification_consumer.NotificationConsumerApplication`, using the
`notification-consumer` module and this directory as the working directory.

In that configuration's **Environment variables** field, set:

| Variable | Value |
| --- | --- |
| `DB_URL` | Your Oracle JDBC URL; defaults to `jdbc:oracle:thin:@//localhost:1521/ORCLPDB1` |
| `DB_USERNAME` | Your schema username; defaults to `VacancyLoop` |
| `DB_PASSWORD` | The password for that Oracle schema; required |
| `SLACK_WEBHOOK_URL` | Your Slack incoming webhook URL; required |

Enter these as environment variables, not program arguments. Use your own
credentials and webhook. Keep the run configuration local and do not commit
passwords, webhook URLs or secret-bearing configuration files.

## 3. Run the consumer

Run `NotificationConsumerApplication` in IntelliJ. This sends real messages to
the configured Slack channel and updates `notifications.sent_at` in Oracle.

Alternatively, with the same environment variables set in your terminal, run
`./mvnw spring-boot:run` from this directory. On Windows use
`mvnw.cmd spring-boot:run`.

Example console output (counts and IDs vary):

```text
Pending notifications: 2
Sent notification: 35
Sent notification: 36
```

The consumer runs once and exits; it does not continuously poll. Run it again
to deliver notifications created since the previous run. If nothing new is
pending, it prints `Pending notifications: 0`.

Each notification is marked sent only after the Slack request succeeds. A
delivery error is logged as `Failed notification: ...`; check the console even
if the process exits normally. A message accepted by Slack but not successfully
marked in Oracle may be delivered again on a later run. Run only one consumer
instance at a time to avoid concurrent duplicate delivery.

## Build and checks

From this directory, run `./mvnw test` for the included notification-record unit
test, or `./mvnw clean package` to test and package the application. On Windows
replace `./mvnw` with `mvnw.cmd`.

The included unit test does not connect to Oracle or Slack and is not an
end-to-end integration test. SQL workflow test scripts are not included in
this submission. The separated database scripts and setup runner have been
checked statically, but have not yet been executed together as a fresh install.

## Current limitations

- The supplied archive table allows job titles up to 100 characters, while the
  live table allows 150. Use titles no longer than 100 characters until these
  limits are aligned.
- Archiving removes the live vacancy and cascades deletion to its applications;
  existing notification text remains available.
- The daily digest summarizes applications currently stored in the database,
  rather than filtering to applications received that day.
- Slack delivery has no automatic retry/backoff; failed pending messages can be
  retried by running the consumer again.
