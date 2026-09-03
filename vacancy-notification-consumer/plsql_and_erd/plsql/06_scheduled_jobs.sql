-- ========================= 06_scheduled_jobs.sql =========================
-- ================ Scheduled Jobs for Vacancy Loop =================


-- ================= Expiry Scheduled Job =================
-- Runs every minute and marks expired vacancies as inactive
BEGIN
    DBMS_SCHEDULER.CREATE_JOB (
        job_name        => 'JOB_EXPIRE_VACANCIES',
        job_type        => 'STORED_PROCEDURE',
        job_action      => 'EXPIRE_JOBS',
        start_date      => SYSTIMESTAMP,
        repeat_interval => 'FREQ=MINUTELY;INTERVAL=1',
        enabled         => TRUE
    );
END;
/
------------------- Test -------------------
SELECT job_name, enabled, state, repeat_interval
FROM user_scheduler_jobs
WHERE job_name = 'JOB_EXPIRE_VACANCIES';

SELECT job_name, status, actual_start_date, run_duration
FROM user_scheduler_job_run_details
WHERE job_name = 'JOB_EXPIRE_VACANCIES'
ORDER BY actual_start_date DESC;


-- ================= Archive Scheduled Job =================
-- Runs every minute and archives inactive vacancies
BEGIN
    DBMS_SCHEDULER.CREATE_JOB (
        job_name        => 'JOB_ARCHIVE_VACANCIES',
        job_type        => 'STORED_PROCEDURE',
        job_action      => 'ARCHIVE_JOBS',
        start_date      => SYSTIMESTAMP,
        repeat_interval => 'FREQ=MINUTELY;INTERVAL=1',
        enabled         => TRUE
    );
END;
/
------------------- Test -------------------
SELECT job_name, enabled, state, repeat_interval
FROM user_scheduler_jobs
WHERE job_name = 'JOB_ARCHIVE_VACANCIES';

SELECT job_name, status, actual_start_date, run_duration
FROM user_scheduler_job_run_details
WHERE job_name = 'JOB_ARCHIVE_VACANCIES'
ORDER BY actual_start_date DESC;


-- ================= Lowercase Sweep Scheduled Job =================
-- Runs every minute and normalizes all live job descriptions
BEGIN
    DBMS_SCHEDULER.CREATE_JOB (
        job_name        => 'JOB_LOWERCASE_SWEEP',
        job_type        => 'STORED_PROCEDURE',
        job_action      => 'LOWERCASE_SWEEP',
        start_date      => SYSTIMESTAMP,
        repeat_interval => 'FREQ=MINUTELY;INTERVAL=1',
        enabled         => TRUE
    );
END;
/
------------------- Test -------------------
SELECT job_name, enabled, state, repeat_interval
FROM user_scheduler_jobs
WHERE job_name = 'JOB_LOWERCASE_SWEEP';

SELECT job_name, status, actual_start_date, run_duration
FROM user_scheduler_job_run_details
WHERE job_name = 'JOB_LOWERCASE_SWEEP'
ORDER BY actual_start_date DESC;


-- ================= Daily Digest Scheduled Job =================
-- Runs once every 24 hours and creates one application digest
BEGIN
    DBMS_SCHEDULER.CREATE_JOB (
        job_name        => 'JOB_DAILY_DIGEST',
        job_type        => 'STORED_PROCEDURE',
        job_action      => 'GENERATE_DAILY_DIGEST',
        start_date      => SYSTIMESTAMP,
        repeat_interval => 'FREQ=DAILY;INTERVAL=1',
        enabled         => TRUE
    );
END;
/
------------------- Test -------------------
SELECT job_name, enabled, state, repeat_interval
FROM user_scheduler_jobs
WHERE job_name = 'JOB_DAILY_DIGEST';

SELECT job_name, status, actual_start_date, run_duration
FROM user_scheduler_job_run_details
WHERE job_name = 'JOB_DAILY_DIGEST'
ORDER BY actual_start_date DESC;





-- ========================= Check All Scheduled Jobs =========================
SELECT job_name,
       enabled,
       state,
       repeat_interval
FROM user_scheduler_jobs
WHERE job_name IN (
    'JOB_EXPIRE_VACANCIES',
    'JOB_ARCHIVE_VACANCIES',
    'JOB_LOWERCASE_SWEEP',
    'JOB_DAILY_DIGEST'
)
ORDER BY job_name;



SELECT job_name,
       status,
       actual_start_date,
       run_duration
FROM user_scheduler_job_run_details
WHERE job_name IN (
    'JOB_EXPIRE_VACANCIES',
    'JOB_ARCHIVE_VACANCIES',
    'JOB_LOWERCASE_SWEEP',
    'JOB_DAILY_DIGEST'
)
ORDER BY actual_start_date DESC;


