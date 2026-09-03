Vacancy Loop -- PL/SQL and Spring Boot Notification System

Project Overview

Vacancy Loop is a database-driven vacancy management and notification
system.

The project uses Oracle PL/SQL to manage vacancies, applications,
scheduled database tasks, archiving, and notifications. A Spring Boot
Java application connects to Oracle, retrieves pending notifications
through a SYS_REFCURSOR, sends them to Slack using an Incoming
Webhook, and marks successfully delivered notifications as SENT.

Technologies Used

Oracle Database

PL/SQL

DBMS_SCHEDULER

Java 17

Spring Boot

Maven

Spring JDBC

Oracle JDBC Driver

Slack Incoming Webhooks

IntelliJ IDEA

Oracle SQL Developer

Main Database Tables

The system uses four main entities:

VACANT_JOBS -- stores vacancies currently on the live board.

JOB_APPLICATIONS -- stores applications and references an existing
vacancy through a foreign key.

NOTIFICATIONS -- stores generated notifications with statuses such
as PENDING and SENT.

JOBS_POSTING_ARCHIVE -- stores expired vacancies before they are
removed from the live board.

System Flow

HR inserts a vacancy
↓
INSERT trigger fires
↓
Notification is registered
↓
Vacancy remains active
↓
Expiry scheduler marks expired vacancy inactive
↓
Archive scheduler copies it to JOBS_POSTING_ARCHIVE
↓
Purge removes the safely archived row from VACANT_JOBS

Notifications leave Oracle through this flow:

NOTIFICATIONS (PENDING)
↓
GET_PENDING_NOTIFICATIONS
↓
SYS_REFCURSOR
↓
Spring Boot / JDBC
↓
SlackService
↓
Slack Incoming Webhook
↓
Slack Channel
↓
Status changed to SENT

PL/SQL Components

Function

custom_lower

A user-defined function that converts vacancy description text to
lowercase. It is used by the description-processing logic rather than
directly using Oracle's built-in LOWER() function.

Procedures

The project includes procedures for:

Registering notifications

Processing job descriptions

Expiring vacancies

Archiving inactive vacancies

Purging safely archived vacancies from the live board

Sweeping job descriptions

Creating the daily application digest

Returning pending notifications through a SYS_REFCURSOR

Important procedures include:

register_notification

process_job_description

expire_jobs

archive_inactive_jobs

purge_inactive_jobs

sweep_job_descriptions

create_application_digest

get_pending_notifications

Triggers

Vacancy INSERT Trigger

When a new vacancy is inserted, the database automatically creates a
notification.

Example:

New Vacancy Posted
Job ID 22 - Eng Doctor has been posted.

Description UPDATE Trigger

When a vacancy description is updated, the trigger calls the
description-processing procedure, which uses custom_lower to store the
description in lowercase.

Cursors

Both cursor styles are demonstrated.

Implicit Cursors

Implicit cursor FOR loops are used for database-side row-by-row
processing, including:

Description sweep

Daily application digest

Explicit Cursor

get_pending_notifications returns a SYS_REFCURSOR containing pending
notifications. Spring Boot receives this cursor through JDBC and
processes it as a Java ResultSet.

Scheduled Jobs

The project uses DBMS_SCHEDULER for automatic processing.

Expiry Job

Checks vacancies and changes expired vacancies from active to inactive.

Archive Job

Copies inactive vacancies to JOBS_POSTING_ARCHIVE.

After a vacancy has been safely archived, the purge procedure removes
the corresponding row from VACANT_JOBS.

The required order is:

Expire → Archive → Purge

Description Sweep Job

Runs the bulk description-processing routine so that the bulk route
produces the same lowercase result as the single-row update route.

Daily Application Digest

Runs once every 24 hours and creates one combined notification
summarizing application counts instead of one notification per
applicant.

Example:

Daily Application Digest

Job ID: 1 | Description: java developer needed for backend team | Applications: 3
Job ID: 3 | Description: network engineer | Applications: 1
Job ID: 4 | Description: cloud engineer | Applications: 0

Job Applications

JOB_APPLICATIONS has a foreign-key relationship with VACANT_JOBS.

This ensures that an application can only reference a vacancy that
exists on the live board. Attempts to apply for a vacancy that has
already been removed are rejected by Oracle's foreign-key constraint.

Spring Boot Consumer

The Spring Boot project is responsible for transferring pending Oracle
notifications to Slack.

Notification.java

A model class that stores:

Notification ID

Subject

Body

Status

NotificationConsumerService.java

This service:

Connects to Oracle using JDBC.

Calls get_pending_notifications.

Registers the Oracle cursor output parameter.

Receives the SYS_REFCURSOR as a ResultSet.

Walks through pending notifications.

Creates a Notification object for each row.

Sends each notification through SlackService.

Changes successfully delivered notifications from PENDING to
SENT.

Records the delivery time in SENT_AT.

SlackService.java

Sends notification content to a Slack Incoming Webhook as JSON.

Notification Status

Before delivery:

STATUS = PENDING

After successful Slack delivery:

STATUS = SENT
SENT_AT = delivery timestamp

Already-sent notifications are not selected again by
get_pending_notifications, which prevents duplicate delivery.



All Oracle SQL and PL/SQL work can be kept in the single
vacancy_loop.sql file, with clearly labelled sections for the schema,
seed data, function, procedures, triggers, cursors, scheduled jobs, and
tests.

Configuration and Security

Secrets should not be committed to GitHub.

Example configuration:

spring.datasource.url=jdbc:oracle:thin:@//localhost:1521/FREEPDB1
spring.datasource.username=system
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

slack.webhook.url=${SLACK_WEBHOOK_URL}

The real database password and Slack webhook URL should be provided
through environment variables.

Testing Completed

The following scenarios were tested:

New vacancy insertion creates a notification.

Vacancy notification is delivered to Slack.

Description updates are converted to lowercase through the custom
function.

Expired vacancies become inactive automatically.

Inactive vacancies are archived.

Safely archived vacancies are removed from the live board.

Valid applications can reference existing vacancies.

Applications for nonexistent or removed vacancies are rejected by
the foreign key.

The description sweep uses an implicit cursor.

The daily digest creates one combined notification.

Pending notifications are returned through a SYS_REFCURSOR.

Spring Boot reads the cursor using JDBC.

Slack receives the notifications.

Successfully delivered notifications change from PENDING to
SENT.

SENT_AT records the successful delivery time.

Conclusion

Vacancy Loop combines Oracle database automation with a Spring Boot
notification consumer.

Oracle PL/SQL handles the core business rules, triggers, functions,
cursors, scheduled jobs, archiving, applications, and notification
creation. Spring Boot retrieves pending notifications through JDBC and
sends them to Slack.

The result is an automated workflow from vacancy creation and scheduled
database processing through to external Slack notification delivery.