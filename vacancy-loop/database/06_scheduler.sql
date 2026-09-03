--Scheduled Job 1 -- Expire Vacancies
BEGIN
    DBMS_SCHEDULER.CREATE_JOB (
        job_name        => 'JOB_EXPIRE_VACANCIES',
        job_type        => 'STORED_PROCEDURE',
        job_action      => 'EXPIRE_VACANCIES',
        start_date      => SYSTIMESTAMP,
        repeat_interval => 'FREQ=MINUTELY;INTERVAL=1',
        enabled         => TRUE
    );
END;
/

--test
SELECT
    job_name,
    state,
    repeat_interval
FROM user_scheduler_jobs
WHERE job_name = 'JOB_EXPIRE_VACANCIES';

SELECT
    job_id,
    title,
    expires_at,
    active_flag
FROM vacant_jobs
WHERE title = 'Test Scheduler Job';

--Scheduled Job 2 -- Archive Scheduled
BEGIN
    DBMS_SCHEDULER.CREATE_JOB (
        job_name        => 'JOB_ARCHIVE_AND_PURGE',
        job_type        => 'PLSQL_BLOCK',
        job_action      => '
            BEGIN
                archive_inactive_jobs;
                purge_archived_jobs;
            END;',
        start_date      => SYSTIMESTAMP,
        repeat_interval => 'FREQ=MINUTELY;INTERVAL=1',
        enabled         => TRUE
    );
END;
/

SELECT
    job_name,
    state,
    repeat_interval
FROM user_scheduler_jobs
WHERE job_name = 'JOB_ARCHIVE_AND_PURGE';

SELECT
    job_id,
    title
FROM jobs_posting_archive
ORDER BY job_id;

SELECT
    job_id,
    title,
    active_flag
FROM vacant_jobs
ORDER BY job_id;

--Scheduled Job 3 -- Tidy Descriptions
BEGIN
    DBMS_SCHEDULER.CREATE_JOB (
        job_name        => 'JOB_TIDY_DESCRIPTIONS',
        job_type        => 'STORED_PROCEDURE',
        job_action      => 'TIDY_ALL_DESCRIPTIONS',
        start_date      => SYSTIMESTAMP,
        repeat_interval => 'FREQ=MINUTELY;INTERVAL=5',
        enabled         => TRUE
    );
END;
/

--Scheduled 4 --Daily Digest
BEGIN
    DBMS_SCHEDULER.CREATE_JOB (
        job_name        => 'JOB_DAILY_DIGEST',
        job_type        => 'STORED_PROCEDURE',
        job_action      => 'CREATE_DAILY_DIGEST',
        start_date      => SYSTIMESTAMP,
        repeat_interval => 'FREQ=DAILY;INTERVAL=1',
        enabled         => TRUE
    );
END;
/

--check available schedulerd jobs
SELECT
    job_name,
    state,
    repeat_interval
FROM user_scheduler_jobs
ORDER BY job_name;

SELECT
    job_name,
    status,
    actual_start_date,
    run_duration
FROM user_scheduler_job_run_details
ORDER BY log_date DESC;

--output
--JOB_ARCHIVE_AND_PURGE	SUCCEEDED	02-SEP-26 01.05.55.286840000 PM GMT	+00 00:00:00.000000
--JOB_EXPIRE_VACANCIES	SUCCEEDED	02-SEP-26 01.05.50.289606000 PM GMT	+00 00:00:00.000000
--JOB_DAILY_DIGEST	SUCCEEDED	02-SEP-26 01.04.46.258561000 PM GMT	+00 00:00:00.000000
--JOB_TIDY_DESCRIPTIONS	SUCCEEDED	02-SEP-26 01.02.59.640147000 PM GMT	+00 00:00:00.000000



SELECT
    notification_id,
    subject,
    sent_flag
FROM notifications
ORDER BY notification_id;