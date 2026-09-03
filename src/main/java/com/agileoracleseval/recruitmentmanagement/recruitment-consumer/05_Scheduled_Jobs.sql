-- SCHEDULED JOBS

-- Lane C - 7 - Run the vacancy expiry check automatically every minute
-- Create the Scheduler job that check every minute
BEGIN

    DBMS_SCHEDULER.CREATE_JOB (

        job_name => 'JOB_EXPIRE_VACANCIES',

        job_type => 'STORED_PROCEDURE',

        job_action => 'EXPIRE_VACANCIES',

        start_date => SYSTIMESTAMP AT TIME ZONE 'Asia/Muscat',

        repeat_interval => 'FREQ=MINUTELY;INTERVAL=1',

        enabled => TRUE

    );

END;
/

-- Lane C - 10 - Run the description sweep automatically every 5 minutes
-- Create a Scheduler Job, assuming every 5 min
BEGIN

    DBMS_SCHEDULER.CREATE_JOB (

        job_name =>'JOB_DESCRIPTION_SWEEP',
        job_type => 'STORED_PROCEDURE',
        job_action => 'SWEEP_JOB_DESCRIPTIONS',
        start_date => SYSTIMESTAMP AT TIME ZONE 'Asia/Muscat',
        repeat_interval =>'FREQ=MINUTELY;INTERVAL=5',
        enabled =>TRUE

    );

END;
/

-- Lane D - 12 - Run the application digest automatically once every 24 hours
-- Scheduler job for JOB_DAILY_APPLICATION_DIGEST
BEGIN

    DBMS_SCHEDULER.CREATE_JOB (

        job_name => 'JOB_DAILY_APPLICATION_DIGEST',
        job_type => 'STORED_PROCEDURE',
        job_action => 'CREATE_DAILY_APPLICATION_DIGEST',
        start_date => (SYSTIMESTAMP AT TIME ZONE 'Asia/Muscat') + NUMTODSINTERVAL(1, 'DAY'),
        repeat_interval => 'FREQ=DAILY;INTERVAL=1',
        enabled => TRUE

    );

END;
/
