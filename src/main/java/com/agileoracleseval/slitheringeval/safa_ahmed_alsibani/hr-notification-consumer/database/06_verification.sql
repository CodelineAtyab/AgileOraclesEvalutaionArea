-- The Vacancy Loop
-- 06 - Verification and demonstration script
-- Run with F5 in Oracle SQL Developer

SET SERVEROUTPUT ON
SET LINESIZE 300
SET PAGESIZE 100

COLUMN subject FORMAT A35
COLUMN body FORMAT A120
COLUMN status FORMAT A10
COLUMN job_name FORMAT A25
COLUMN repeat_interval FORMAT A35

-- ---------------------------------------------------------
-- Clean only the controlled verification records
-- ---------------------------------------------------------

DELETE FROM notification
WHERE subject IN (
    'New vacancy: Verification Developer',
    'New vacancy: Expired Verification Job'
);

DELETE FROM notification
WHERE subject = 'Daily application digest'
  AND created_at >= TRUNC(SYSDATE)
  AND created_at < TRUNC(SYSDATE) + 1;

DELETE FROM jobs_posting_archive
WHERE job_id IN (9001, 9002);

DELETE FROM vacant_jobs
WHERE job_id IN (9001, 9002);

COMMIT;

-- ---------------------------------------------------------
-- Test the custom lowercase function
-- ---------------------------------------------------------

SELECT custom_lower('HELLO ORACLE Database') AS lowercase_result
FROM dual;

-- ---------------------------------------------------------
-- Test automatic notification trigger
-- ---------------------------------------------------------

INSERT INTO vacant_jobs (
    job_id,
    title,
    description,
    posted_at,
    expires_at,
    is_active
)
VALUES (
    9001,
    'Verification Developer',
    'BUILD SECURE VERIFICATION SERVICES',
    SYSTIMESTAMP,
    SYSTIMESTAMP + INTERVAL '1' DAY,
    'Y'
);

COMMIT;

SELECT notification_id,
       job_id,
       subject,
       status
FROM notification
WHERE job_id = 9001;

-- ---------------------------------------------------------
-- Test description normalization trigger
-- ---------------------------------------------------------

UPDATE vacant_jobs
SET description = 'BUILD AND TEST ORACLE SERVICES'
WHERE job_id = 9001;

COMMIT;

SELECT job_id,
       description
FROM vacant_jobs
WHERE job_id = 9001;

-- ---------------------------------------------------------
-- Test expiry, archive and purge sequence
-- ---------------------------------------------------------

INSERT INTO vacant_jobs (
    job_id,
    title,
    description,
    posted_at,
    expires_at,
    is_active
)
VALUES (
    9002,
    'Expired Verification Job',
    'ARCHIVE THIS EXPIRED VACANCY',
    SYSTIMESTAMP - INTERVAL '1' DAY,
    SYSTIMESTAMP - INTERVAL '1' MINUTE,
    'Y'
);

COMMIT;

BEGIN
    expire_vacant_jobs;
    archive_inactive_jobs;
    purge_archived_jobs;
    COMMIT;
END;
/

SELECT 'VACANT_JOBS' AS location,
       job_id,
       title,
       description
FROM vacant_jobs
WHERE job_id IN (9001, 9002)

UNION ALL

SELECT 'ARCHIVE' AS location,
       job_id,
       title,
       description
FROM jobs_posting_archive
WHERE job_id IN (9001, 9002)

ORDER BY job_id;

-- ---------------------------------------------------------
-- Test repeat-safe description sweep
-- ---------------------------------------------------------

BEGIN
    tidy_live_job_descriptions;
    COMMIT;
END;
/

BEGIN
    tidy_live_job_descriptions;
    COMMIT;
END;
/

-- The second execution should print:
-- Descriptions updated: 0

-- ---------------------------------------------------------
-- Test one daily digest
-- ---------------------------------------------------------

BEGIN
    create_daily_application_digest;
    create_daily_application_digest;
    COMMIT;
END;
/

SELECT notification_id,
       subject,
       body,
       status
FROM notification
WHERE subject = 'Daily application digest'
ORDER BY notification_id;

-- Only one daily digest should exist for the current day.

-- ---------------------------------------------------------
-- Test the SYS_REFCURSOR used by the Java consumer
-- ---------------------------------------------------------

VARIABLE pending_cursor REFCURSOR

EXEC get_pending_notifications(:pending_cursor);

PRINT pending_cursor;

-- ---------------------------------------------------------
-- Verify all four scheduled jobs
-- ---------------------------------------------------------

SELECT job_name,
       enabled,
       state,
       repeat_interval,
       last_start_date,
       next_run_date
FROM user_scheduler_jobs
WHERE job_name IN (
    'HR_EXPIRE_VACANCIES',
    'HR_ARCHIVE_PURGE',
    'HR_DESCRIPTION_SWEEP',
    'HR_DAILY_DIGEST'
)
ORDER BY job_name;

-- ---------------------------------------------------------
-- Verify recent scheduler execution history
-- ---------------------------------------------------------

SELECT job_name,
       status,
       actual_start_date,
       run_duration
FROM user_scheduler_job_run_details
WHERE job_name IN (
    'HR_EXPIRE_VACANCIES',
    'HR_ARCHIVE_PURGE',
    'HR_DESCRIPTION_SWEEP',
    'HR_DAILY_DIGEST'
)
ORDER BY actual_start_date DESC
FETCH FIRST 20 ROWS ONLY;