SET PAGESIZE 100;
SET LINESIZE 220;
SET SERVEROUTPUT ON;

PROMPT === CONNECTION ===

SELECT
    USER AS connected_user,
    SYS_CONTEXT('USERENV', 'SERVICE_NAME') AS service_name,
    SYS_CONTEXT('USERENV', 'CON_NAME') AS container_name
FROM dual;

PROMPT === TABLES ===

SELECT table_name
FROM user_tables
ORDER BY table_name;

PROMPT === ROW COUNTS ===

SELECT 'VACANT_JOBS' AS table_name, COUNT(*) AS row_count
FROM vacant_jobs
UNION ALL
SELECT 'JOBS_POSTING_ARCHIVE', COUNT(*)
FROM jobs_posting_archive
UNION ALL
SELECT 'JOB_APPLICATIONS', COUNT(*)
FROM job_applications
UNION ALL
SELECT 'JOB_APPLICATIONS_ARCHIVE', COUNT(*)
FROM job_applications_archive
UNION ALL
SELECT 'NOTIFICATIONS', COUNT(*)
FROM notifications
ORDER BY table_name;

PROMPT === REQUIRED OBJECTS ===

SELECT object_name,
       object_type,
       status
FROM user_objects
WHERE object_name IN (
    'FN_CUSTOM_LOWERCASE',
    'REGISTER_NOTIFICATION',
    'NORMALIZE_JOB_DESCRIPTION',
    'EXPIRE_VACANT_JOBS',
    'ARCHIVE_INACTIVE_JOBS',
    'PURGE_ARCHIVED_JOBS',
    'ARCHIVE_AND_PURGE_JOBS',
    'SWEEP_JOB_DESCRIPTIONS',
    'CREATE_DAILY_APPLICATION_DIGEST',
    'GET_PENDING_NOTIFICATIONS',
    'MARK_NOTIFICATION_SENT',
    'RECORD_NOTIFICATION_FAILURE',
    'TRG_VACANT_JOBS_AFTER_INS',
    'TRG_VACANT_JOBS_BEFORE_DESC'
)
ORDER BY object_type, object_name;

PROMPT === COMPILATION ERRORS ===

SELECT name,
       type,
       line,
       position,
       text
FROM user_errors
ORDER BY name, sequence;

PROMPT === SCHEDULER JOBS ===

SELECT job_name,
       enabled,
       state,
       repeat_interval,
       last_start_date,
       next_run_date
FROM user_scheduler_jobs
ORDER BY job_name;

PROMPT === INVALID OBJECTS ===

SELECT object_name,
       object_type,
       status
FROM user_objects
WHERE status <> 'VALID'
ORDER BY object_type, object_name;
