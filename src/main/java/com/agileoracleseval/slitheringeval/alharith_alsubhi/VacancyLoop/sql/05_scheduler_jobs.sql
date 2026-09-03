-- Scheduler jobs

BEGIN
    BEGIN DBMS_SCHEDULER.DROP_JOB('JOB_EXPIRE_VACANCIES', TRUE); EXCEPTION WHEN OTHERS THEN NULL; END;
    BEGIN DBMS_SCHEDULER.DROP_JOB('JOB_ARCHIVE_PURGE', TRUE); EXCEPTION WHEN OTHERS THEN NULL; END;
    BEGIN DBMS_SCHEDULER.DROP_JOB('JOB_SWEEP_DESC', TRUE); EXCEPTION WHEN OTHERS THEN NULL; END;
    BEGIN DBMS_SCHEDULER.DROP_JOB('JOB_DAILY_DIGEST', TRUE); EXCEPTION WHEN OTHERS THEN NULL; END;
END;
/

BEGIN
    DBMS_SCHEDULER.CREATE_JOB(
        job_name        => 'JOB_EXPIRE_VACANCIES',
        job_type        => 'STORED_PROCEDURE',
        job_action      => 'EXPIRE_VACANCIES',
        start_date      => SYSTIMESTAMP,
        repeat_interval => 'FREQ=MINUTELY; INTERVAL=1',
        enabled         => TRUE,
        comments        => 'set active_flag=N when expires_at passed'
    );
END;
/

BEGIN
    DBMS_SCHEDULER.CREATE_JOB(
        job_name        => 'JOB_ARCHIVE_PURGE',
        job_type        => 'STORED_PROCEDURE',
        job_action      => 'ARCHIVE_THEN_PURGE',
        start_date      => SYSTIMESTAMP,
        repeat_interval => 'FREQ=MINUTELY; INTERVAL=1',
        enabled         => TRUE,
        comments        => 'archive inactive then purge'
    );
END;
/

BEGIN
    DBMS_SCHEDULER.CREATE_JOB(
        job_name        => 'JOB_SWEEP_DESC',
        job_type        => 'STORED_PROCEDURE',
        job_action      => 'SWEEP_DESCRIPTIONS',
        start_date      => SYSTIMESTAMP,
        repeat_interval => 'FREQ=MINUTELY; INTERVAL=5',
        enabled         => TRUE,
        comments        => 'lower descriptions with my_lower'
    );
END;
/

BEGIN
    DBMS_SCHEDULER.CREATE_JOB(
        job_name        => 'JOB_DAILY_DIGEST',
        job_type        => 'STORED_PROCEDURE',
        job_action      => 'MAKE_APPLICATION_DIGEST',
        start_date      => SYSTIMESTAMP,
        repeat_interval => 'FREQ=DAILY; INTERVAL=1',
        enabled         => TRUE,
        comments        => 'one daily apps digest notification'
    );
END;
/
