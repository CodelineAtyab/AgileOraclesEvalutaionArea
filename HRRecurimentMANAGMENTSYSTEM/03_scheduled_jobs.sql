--------------------------------------------------------------------------------
-- 03_scheduled_jobs.sql
-- Run THIRD, after 02_triggers_functions_procedures.sql.
--------------------------------------------------------------------------------

BEGIN
    DBMS_SCHEDULER.CREATE_JOB (
        job_name        => 'job_expire_archive_purge',
        job_type        => 'STORED_PROCEDURE',
        job_action      => 'PRC_EXPIRE_ARCHIVE_PURGE',
        start_date      => SYSTIMESTAMP,
        repeat_interval => 'FREQ=MINUTELY; INTERVAL=1',
        enabled         => TRUE
    );

    DBMS_SCHEDULER.CREATE_JOB (
        job_name        => 'job_sweep_lowercase',
        job_type        => 'STORED_PROCEDURE',
        job_action      => 'PRC_SWEEP_LOWERCASE_DESCRIPTIONS',
        start_date      => SYSTIMESTAMP,
        repeat_interval => 'FREQ=MINUTELY; INTERVAL=5',
        enabled         => TRUE
    );

    DBMS_SCHEDULER.CREATE_JOB (
        job_name        => 'job_daily_digest',
        job_type        => 'STORED_PROCEDURE',
        job_action      => 'PRC_DAILY_DIGEST',
        start_date      => SYSTIMESTAMP,
        repeat_interval => 'FREQ=DAILY; BYHOUR=8',
        enabled         => TRUE
    );
END;
/

SELECT job_name, enabled, state, repeat_interval
FROM   user_scheduler_jobs;
