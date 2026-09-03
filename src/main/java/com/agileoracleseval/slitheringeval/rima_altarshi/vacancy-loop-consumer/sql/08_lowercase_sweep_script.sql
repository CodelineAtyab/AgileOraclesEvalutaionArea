-- Normalizes all active vacancy descriptions using
-- the reusable F_TO_LOWER function.
------------------------------------------------------------
-- NORMALIZE ALL DESCRIPTIONS PROCEDURE--
-- Sweep active vacancy descriptions and update only text
-- that actually needs normalization

CREATE OR REPLACE PROCEDURE NORMALIZE_ALL_DESCRIPTIONS
AS
    V_COUNT NUMBER := 0;
    V_LOWER_DESCRIPTION VARCHAR2(4000);
BEGIN

    -- Implicit cursor: process each active vacancy row by row
    FOR JOB_REC IN (
        SELECT job_id,
               description
        FROM vacant_jobs
        WHERE is_active = 'Y'
    )
    LOOP

        V_LOWER_DESCRIPTION :=
            F_TO_LOWER(JOB_REC.description);

        -- Update only if the normalized text is different
        IF V_LOWER_DESCRIPTION <> JOB_REC.description THEN

            UPDATE vacant_jobs
            SET description = V_LOWER_DESCRIPTION,
                updated_at = SYSTIMESTAMP
            WHERE job_id = JOB_REC.job_id;

            V_COUNT := V_COUNT + 1;

        END IF;

    END LOOP;

    COMMIT;

    DBMS_OUTPUT.PUT_LINE( V_COUNT || ' vacancy descriptions normalized.' );

END NORMALIZE_ALL_DESCRIPTIONS;
/

------------------------------------------------------------
-- VERIFY PROCEDURE CREATION

SELECT object_name,
       object_type,
       status
FROM user_objects
WHERE object_name = 'NORMALIZE_ALL_DESCRIPTIONS';


------------------------------------------------------------
-- DESCRIPTION SWEEP SCHEDULER--
-- Run the lowercase description sweep automatically every minute

BEGIN
    DBMS_SCHEDULER.CREATE_JOB(
        job_name        => 'JOB_DESCRIPTION_SWEEP',
        job_type        => 'PLSQL_BLOCK',
        job_action      => 'BEGIN NORMALIZE_ALL_DESCRIPTIONS; END;',
        start_date      => SYSTIMESTAMP,
        repeat_interval => 'FREQ=MINUTELY;INTERVAL=1',
        enabled         => TRUE
    );
END;
/

------------------------------------------------------------
-- VERIFY DESCRIPTION SWEEP SCHEDULER

SELECT job_name,
       enabled,
       state,
       repeat_interval
FROM user_scheduler_jobs
WHERE job_name = 'JOB_DESCRIPTION_SWEEP';

------------------------------------------------------------
-- TEST THE SCHEDULED SWEEP INDEPENDENTLY--
-- Disable the edit trigger temporarily so uppercase text can
-- remain in the table long enough for the scheduler to process it

ALTER TRIGGER TRG_VACANCY_DESCRIPTION_LOWER DISABLE;


UPDATE vacant_jobs
SET description = 'JAVA DEVELOPER WITH SPRING BOOT AND ORACLE'
WHERE job_id = 1;

COMMIT;


------------------------------------------------------------
-- VERIFY UPPERCASE TEXT BEFORE THE SWEEP

SELECT job_id,
       title,
       description
FROM vacant_jobs
WHERE job_id = 1;

------------------------------------------------------------
-- VERIFY NORMALIZED TEXT AFTER THE SWEEP

SELECT job_id,
       title,
       description
FROM vacant_jobs
WHERE job_id = 1;

------------------------------------------------------------
-- RE-ENABLE DESCRIPTION TRIGGER

ALTER TRIGGER TRG_VACANCY_DESCRIPTION_LOWER ENABLE;

------------------------------------------------------------
-- VERIFY TRIGGER STATUS

SELECT trigger_name,
       status
FROM user_triggers
WHERE trigger_name = 'TRG_VACANCY_DESCRIPTION_LOWER';