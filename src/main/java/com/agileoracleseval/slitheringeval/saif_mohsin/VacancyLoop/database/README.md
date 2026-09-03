# VacancyLoop database scripts

For Java configuration and application run instructions, see the
[main project README](../README.md).

## Source

These scripts were separated from `/Users/saif/Desktop/VacancyLoop.sql`.
The original file has not been changed.

The archive table and insertion notification trigger were added from the supplied
database DDL exports. Explicit schema qualifiers and export storage/tablespace
settings were omitted so installation uses the connected schema and its defaults.
Column definitions, constraints and trigger behavior were preserved.

## One-file setup

Use a dedicated, empty VacancyLoop schema. Do not run setup as SYS or SYSTEM,
or against the existing working database schema.

In Oracle SQL Developer:

1. Connect to the provisioned VacancyLoop schema.
2. Open a SQL worksheet for that connection.
3. Enter `@"VacancyLoop/database/setup.sql"`, replacing the path
   with the actual location on your computer.
4. Choose Run Script, not Run Statement. Keep all five numbered scripts
   beside `setup.sql`; the runner locates them relative to itself.
5. Check Script Output for errors and the final setup-complete message.

Only `setup.sql` needs to be run. It runs these files in order:

1. `01_schema.sql`
2. `02_functions_procedures.sql`
3. `03_triggers.sql`
4. `04_seed_data.sql`
5. `05_scheduler_jobs.sql`

The runner stops on SQL/PLSQL errors and checks for invalid database objects
before inserting seed data or enabling schedules. A failed installation may
leave objects or committed seed data behind; rollback cannot undo earlier DDL
or commits. Resolve the error before retrying; do not blindly rerun setup.

Database setup is a one-time step. Afterward, configure the Java application's
database connection and Slack webhook, then run Java independently. Running Java
again does not require rerunning setup.


## Prerequisites and behavior

An administrator must provision the schema separately with CREATE SESSION,
CREATE TABLE, CREATE SEQUENCE, CREATE PROCEDURE, CREATE TRIGGER and CREATE JOB,
plus an appropriate USERS tablespace quota. No account passwords are included.

Table and scheduler creation scripts are for first-time setup, not blind reruns
against the working schema. Existing tables or scheduler job names cause errors.
Oracle DDL commits implicitly. The seed script also explicitly commits, so use a
dedicated setup session without unrelated uncommitted work.

The seed keeps the final thirty applicant names and Gmail-formatted demo
addresses. These addresses are sample data, not verified email accounts.
It uses the corrected separate vacancy inserts, applicant duplicate checks,
and the final seven-day QA expiry. No demo emails should be sent.

Scheduler jobs are created last and enabled immediately, preserving the source
intervals: expiry and archive every minute, normalization every five minutes,
and digest daily. Archiving deletes live vacancies through the purge procedure,
cascading to their applications; linked notifications retain their message text.

## Excluded material

The obsolete multi-table seed attempt, repeated applicant inserts, standalone
checks, separate QA restoration block and final verification
query are not part of these submission.

## Verification

Only static structure, ordering and seed-data checks have been performed on these
split files. They have not been executed against Oracle. The function, procedure
and trigger logic is preserved from the supplied sources.
