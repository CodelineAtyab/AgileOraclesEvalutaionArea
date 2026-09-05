-- The Vacancy Loop
-- 05 - Oracle Scheduler jobs

BEGIN
    FOR rec IN (
        SELECT job_name
        FROM user_scheduler_jobs
        WHERE job_name IN (
            'HR_EXPIRE_VACANCIES',
            'HR_ARCHIVE_PURGE',
            'HR_DESCRIPTION_SWEEP',
            'HR_DAILY_DIGEST'
        )
    ) LOOP
        DBMS_SCHEDULER.DROP_JOB(
            job_name => rec.job_name,
            force    => TRUE
        );
    END LOOP;
END;
/

BEGIN
    DBMS_SCHEDULER.CREATE_JOB(
        job_name        => 'HR_EXPIRE_VACANCIES',
        job_type        => 'PLSQL_BLOCK',
        job_action      => 'BEGIN expire_vacant_jobs; COMMIT; END;',
        start_date      => SYSTIMESTAMP,
        repeat_interval => 'FREQ=MINUTELY;INTERVAL=1',
        enabled         => TRUE,
        auto_drop       => FALSE,
        comments        => 'Marks expired vacancies as inactive.'
    );

    DBMS_SCHEDULER.CREATE_JOB(
        job_name        => 'HR_ARCHIVE_PURGE',
        job_type        => 'PLSQL_BLOCK',
        job_action      => 'BEGIN archive_inactive_jobs; purge_archived_jobs; COMMIT; END;',
        start_date      => SYSTIMESTAMP,
        repeat_interval => 'FREQ=MINUTELY;INTERVAL=1',
        enabled         => TRUE,
        auto_drop       => FALSE,
        comments        => 'Archives inactive vacancies before purging them.'
    );

    DBMS_SCHEDULER.CREATE_JOB(
        job_name        => 'HR_DESCRIPTION_SWEEP',
        job_type        => 'PLSQL_BLOCK',
        job_action      => 'BEGIN tidy_live_job_descriptions; COMMIT; END;',
        start_date      => SYSTIMESTAMP,
        repeat_interval => 'FREQ=MINUTELY;INTERVAL=5',
        enabled         => TRUE,
        auto_drop       => FALSE,
        comments        => 'Normalizes active vacancy descriptions every five minutes.'
    );

    DBMS_SCHEDULER.CREATE_JOB(
        job_name        => 'HR_DAILY_DIGEST',
        job_type        => 'PLSQL_BLOCK',
        job_action      => 'BEGIN create_daily_application_digest; COMMIT; END;',
        start_date      => SYSTIMESTAMP,
        repeat_interval => 'FREQ=DAILY;INTERVAL=1',
        enabled         => TRUE,
        auto_drop       => FALSE,
        comments        => 'Creates one application digest every twenty-four hours.'
    );
END;
/