-- Create and enable the four schedules after schema, routines, triggers and seed data

BEGIN
    DBMS_SCHEDULER.CREATE_JOB(
        job_name        => 'JOB_EXPIRE_VACANCIES',
        job_type        => 'STORED_PROCEDURE',
        job_action      => 'EXPIRE_VACANCIES',
        start_date      => SYSTIMESTAMP,
        repeat_interval => 'FREQ=MINUTELY;INTERVAL=1',
        enabled         => TRUE,
        auto_drop       => FALSE
    );

    DBMS_SCHEDULER.CREATE_JOB(
        job_name        => 'JOB_ARCHIVE_VACANCIES',
        job_type        => 'STORED_PROCEDURE',
        job_action      => 'ARCHIVE_INACTIVE_VACANCIES',
        start_date      => SYSTIMESTAMP,
        repeat_interval => 'FREQ=MINUTELY;INTERVAL=1',
        enabled         => TRUE,
        auto_drop       => FALSE
    );

    DBMS_SCHEDULER.CREATE_JOB(
        job_name        => 'JOB_NORMALIZE_DESCRIPTIONS',
        job_type        => 'STORED_PROCEDURE',
        job_action      => 'NORMALIZE_ALL_DESCRIPTIONS',
        start_date      => SYSTIMESTAMP,
        repeat_interval => 'FREQ=MINUTELY;INTERVAL=5',
        enabled         => TRUE,
        auto_drop       => FALSE
    );

    DBMS_SCHEDULER.CREATE_JOB(
        job_name        => 'JOB_DAILY_APPLICATION_DIGEST',
        job_type        => 'STORED_PROCEDURE',
        job_action      => 'CREATE_DAILY_APPLICATION_DIGEST',
        start_date      => SYSTIMESTAMP,
        repeat_interval => 'FREQ=DAILY;INTERVAL=1',
        enabled         => TRUE,
        auto_drop       => FALSE
    );
END;
/
