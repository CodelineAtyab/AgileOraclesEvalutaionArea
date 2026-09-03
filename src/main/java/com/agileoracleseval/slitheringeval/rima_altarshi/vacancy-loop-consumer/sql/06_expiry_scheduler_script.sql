-- Marks expired vacancies as inactive
-- and runs the expiry check automatically every minute.
------------------------------------------------------------
-- EXPIRE VACANT JOBS PROCEDURE--
-- Mark expired active vacancies as inactive

CREATE OR REPLACE PROCEDURE EXPIRE_VACANT_JOBS
AS
BEGIN

    UPDATE vacant_jobs
    SET is_active = 'N',
        updated_at = SYSTIMESTAMP
    WHERE is_active = 'Y'
      AND expires_at <= SYSTIMESTAMP;

    COMMIT;

END EXPIRE_VACANT_JOBS;
/

------------------------------------------------------------
-- EXPIRY SCHEDULER JOB--
-- Run the expiry check automatically every minute

BEGIN
    DBMS_SCHEDULER.CREATE_JOB(
        job_name        => 'JOB_EXPIRY_CHECK',
        job_type        => 'PLSQL_BLOCK',
        job_action      => 'BEGIN EXPIRE_VACANT_JOBS; END;',
        start_date      => SYSTIMESTAMP,
        repeat_interval => 'FREQ=MINUTELY;INTERVAL=1',
        enabled         => TRUE
    );
END;
/

------------------------------------------------------------
-- VERIFY EXPIRY SCHEDULER

SELECT job_name, enabled, state, repeat_interval FROM user_scheduler_jobs
WHERE job_name = 'JOB_EXPIRY_CHECK';

------------------------------------------------------------
-- VERIFY EXPIRED VACANCY STATE

SELECT job_id, title, is_active, expires_at, updated_at FROM vacant_jobs
ORDER BY job_id;