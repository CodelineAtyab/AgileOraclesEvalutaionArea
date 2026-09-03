# Vacancy Loop

built with:

- Oracle PL/SQL
- Java 17
- Spring Boot
- Maven
- Slack Incoming Webhook

## Database Setup

Run the SQL scripts in this order:

1. database/01_schema.sql
2. database/02_function.sql
3. database/03_procedures.sql
4. database/04_triggers.sql
5. database/05_seed.sql
6. database/06_scheduler.sql

## Environment Variables

The Spring Boot consumer requires:

DB_URL
DB_USERNAME
DB_PASSWORD
SLACK_WEBHOOK_URL

## Run Spring Boot

Windows:

./mvnw.cmd clean package
./mvnw.cmd spring-boot:run

## Notification Delivery

Pending notifications have SENT_FLAG = 'N'.

After a notification is successfully delivered to Slack,
MARK_NOTIFICATION_SENT changes SENT_FLAG to 'Y'.

This prevents successfully delivered notifications
from being sent again.