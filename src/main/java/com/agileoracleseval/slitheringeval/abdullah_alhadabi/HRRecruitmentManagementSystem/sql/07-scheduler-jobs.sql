SET SERVEROUTPUT ON

BEGIN
    FOR job_record IN (
        SELECT job_name
        FROM user_scheduler_jobs
        WHERE job_name IN (
            'JOB_EXPIRE_VACANCIES',
            'JOB_ARCHIVE_PURGE',
            'JOB_DESCRIPTION_SWEEP',
            'JOB_DAILY_DIGEST'
        )
    ) LOOP
        DBMS_SCHEDULER.DROP_JOB(
            job_name => job_record.job_name,
            force    => TRUE
        );
    END LOOP;
END;
/

BEGIN
DBMS_SCHEDULER.CREATE_JOB(
    job_name        => 'JOB_EXPIRE_VACANCIES',
    job_type        => 'STORED_PROCEDURE',
    job_action      => 'EXPIRE_VACANT_JOBS',
    start_date      => SYSTIMESTAMP + INTERVAL '1' MINUTE,
    repeat_interval => 'FREQ=MINUTELY;INTERVAL=1',
    enabled         => TRUE,
    auto_drop       => FALSE,
    comments        => 'Marks live vacancies inactive after expires_at.'
);

DBMS_SCHEDULER.CREATE_JOB(
    job_name        => 'JOB_ARCHIVE_PURGE',
    job_type        => 'STORED_PROCEDURE',
    job_action      => 'ARCHIVE_AND_PURGE_JOBS',
    start_date      => SYSTIMESTAMP + INTERVAL '1' MINUTE,
    repeat_interval => 'FREQ=MINUTELY;INTERVAL=1',
    enabled         => TRUE,
    auto_drop       => FALSE,
    comments        => 'Archives inactive vacancies and applications before purge.'
);

DBMS_SCHEDULER.CREATE_JOB(
    job_name        => 'JOB_DESCRIPTION_SWEEP',
    job_type        => 'STORED_PROCEDURE',
    job_action      => 'SWEEP_JOB_DESCRIPTIONS',
    start_date      => SYSTIMESTAMP + INTERVAL '1' MINUTE,
    repeat_interval => 'FREQ=MINUTELY;INTERVAL=5',
    enabled         => TRUE,
    auto_drop       => FALSE,
    comments        => 'Normalizes live vacancy descriptions with an implicit cursor.'
);

DBMS_SCHEDULER.CREATE_JOB(
    job_name        => 'JOB_DAILY_DIGEST',
    job_type        => 'STORED_PROCEDURE',
    job_action      => 'CREATE_DAILY_APPLICATION_DIGEST',
    start_date      => SYSTIMESTAMP + INTERVAL '1' MINUTE,
    repeat_interval => 'FREQ=DAILY;INTERVAL=1',
    enabled         => TRUE,
    auto_drop       => FALSE,
    comments        => 'Creates one deduplicated application digest every 24 hours.'
);

END;
/

PROMPT Scheduler jobs created and enabled successfully.

SELECT job_name,
       enabled,
       state,
       repeat_interval,
       next_run_date
FROM user_scheduler_jobs
WHERE job_name IN (
    'JOB_EXPIRE_VACANCIES',
    'JOB_ARCHIVE_PURGE',
    'JOB_DESCRIPTION_SWEEP',
    'JOB_DAILY_DIGEST'
)
ORDER BY job_name;
