-- Install VacancyLoop in a dedicated empty schema

WHENEVER OSERROR EXIT FAILURE ROLLBACK
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK

PROMPT Creating tables...
@@01_schema.sql

PROMPT Creating functions and procedures...
@@02_functions_procedures.sql

PROMPT Creating triggers...
@@03_triggers.sql

DECLARE
    v_invalid_count NUMBER;
BEGIN
    SELECT COUNT(*)
    INTO v_invalid_count
    FROM user_objects
    WHERE status = 'INVALID';

    IF v_invalid_count > 0 THEN
        RAISE_APPLICATION_ERROR(
            -20001,
            'Setup stopped: invalid objects found. Check USER_ERRORS before continuing.'
        );
    END IF;
END;
/

PROMPT Inserting seed data...
@@04_seed_data.sql

PROMPT Creating and enabling scheduler jobs...
@@05_scheduler_jobs.sql

PROMPT VacancyLoop database setup complete. Scheduler jobs are enabled.

WHENEVER SQLERROR CONTINUE NONE
WHENEVER OSERROR CONTINUE NONE
