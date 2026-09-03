Vacancy Loop
============

Oracle PL/SQL hiring board + Spring Boot notification consumer.

Folders
-------
docs/                     ERD
sql/                      DB scripts (run 01 to 06 in order)
notification-consumer/    Maven Spring Boot app (Step 8)

DB connection used
------------------
user: vacancy
password: vacancy
host: localhost
port: 1521
service: FREEPDB1

Run Java
--------
cd notification-consumer
mvn spring-boot:run

Or open notification-consumer in IntelliJ and run NotificationConsumerApp.
