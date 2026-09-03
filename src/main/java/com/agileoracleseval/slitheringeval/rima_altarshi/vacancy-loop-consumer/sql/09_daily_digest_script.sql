-- Creates one daily notification summarizing application
-- counts for all active vacancies.
------------------------------------------------------------
-- DAILY APPLICATION DIGEST PROCEDURE

CREATE OR REPLACE PROCEDURE GENERATE_DAILY_APPLICATION_DIGEST
AS
    V_BODY       CLOB := '';
    V_APP_COUNT  NUMBER;
    V_JOB_COUNT  NUMBER := 0;
    V_EXISTS     NUMBER := 0;
BEGIN

-- Prevent creating more than one daily digest on the same day
    SELECT COUNT(*)
    INTO V_EXISTS
    FROM notifications
    WHERE type = 'DAILY_DIGEST'
      AND created_at >= TRUNC(SYSDATE)
      AND created_at < TRUNC(SYSDATE) + 1;

    IF V_EXISTS = 0 THEN

-- Implicit cursor: process each active vacancy row by row
        FOR JOB_REC IN (
            SELECT job_id,
                   description
            FROM vacant_jobs
            WHERE is_active = 'Y'
            ORDER BY job_id
        )
        LOOP

            SELECT COUNT(*)
            INTO V_APP_COUNT
            FROM job_applications
            WHERE job_id = JOB_REC.job_id;

            V_BODY := V_BODY
                      || 'Job ID: ' || JOB_REC.job_id || CHR(10)
                      || 'Description: ' || JOB_REC.description || CHR(10)
                      || 'Applications: ' || V_APP_COUNT || CHR(10)
                      || CHR(10);

            V_JOB_COUNT := V_JOB_COUNT + 1;

        END LOOP;

        IF V_JOB_COUNT > 0 THEN

            REGISTER_NOTIFICATION(
                'Daily Application Digest',
                V_BODY,
                'DAILY_DIGEST',
                NULL,
                'SCHEDULER'
            );

        END IF;

    END IF;

    COMMIT;

    DBMS_OUTPUT.PUT_LINE( V_JOB_COUNT || ' vacancies included in the daily digest.' );

END GENERATE_DAILY_APPLICATION_DIGEST;
/

------------------------------------------------------------
-- VERIFY PROCEDURE CREATION

SELECT object_name, object_type, status FROM user_objects
WHERE object_name = 'GENERATE_DAILY_APPLICATION_DIGEST';

------------------------------------------------------------
-- TEST DAILY DIGEST PROCEDURE

BEGIN
    GENERATE_DAILY_APPLICATION_DIGEST;
END;
/

------------------------------------------------------------
-- VERIFY GENERATED DAILY DIGEST

SELECT notification_id, subject, body, type, status, created_at FROM notifications
WHERE type = 'DAILY_DIGEST'
ORDER BY notification_id;

------------------------------------------------------------
-- DAILY DIGEST SCHEDULER--
-- Generate the application digest automatically every 24 hours

BEGIN
    DBMS_SCHEDULER.CREATE_JOB(
        job_name        => 'JOB_DAILY_DIGEST',
        job_type        => 'PLSQL_BLOCK',
        job_action      => 'BEGIN GENERATE_DAILY_APPLICATION_DIGEST; END;',
        start_date      => SYSTIMESTAMP,
        repeat_interval => 'FREQ=DAILY;INTERVAL=1',
        enabled         => TRUE
    );
END;
/

------------------------------------------------------------
-- VERIFY DAILY DIGEST SCHEDULER

SELECT job_name, enabled, state, repeat_interval FROM user_scheduler_jobs
WHERE job_name = 'JOB_DAILY_DIGEST';