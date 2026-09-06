BEGIN
   DBMS_SCHEDULER.CREATE_JOB (
      job_name        => 'JOB_EXPIRE_POSTINGS',
      job_type        => 'STORED_PROCEDURE',
      job_action      => 'EXPIRE_POSTINGS',
      start_date      => SYSTIMESTAMP,
      repeat_interval => 'FREQ=MINUTELY; INTERVAL=1',
      enabled         => TRUE,
      comments        => 'Step 07 - flips active=0 on expired postings.'
   );
END;
/

BEGIN
   DBMS_SCHEDULER.CREATE_JOB (
      job_name        => 'JOB_ARCHIVE_POSTINGS',
      job_type        => 'STORED_PROCEDURE',
      job_action      => 'ARCHIVE_POSTINGS',
      start_date      => SYSTIMESTAMP,
      repeat_interval => 'FREQ=MINUTELY; INTERVAL=1',
      enabled         => TRUE,
      comments        => 'Step 08 - copies inactive postings into the archive.'
   );
END;
/

BEGIN
   DBMS_SCHEDULER.CREATE_JOB (
      job_name        => 'JOB_PURGE_POSTINGS',
      job_type        => 'STORED_PROCEDURE',
      job_action      => 'PURGE_POSTINGS',
      start_date      => SYSTIMESTAMP,
      repeat_interval => 'FREQ=MINUTELY; INTERVAL=1',
      enabled         => TRUE,
      comments        => 'Step 09 - removes archived, notified postings from the board.'
   );
END;
/

BEGIN
   DBMS_SCHEDULER.CREATE_JOB (
      job_name        => 'JOB_SWEEP_DESCRIPTIONS',
      job_type        => 'STORED_PROCEDURE',
      job_action      => 'SWEEP_DESCRIPTIONS',
      start_date      => SYSTIMESTAMP,
      repeat_interval => 'FREQ=MINUTELY; INTERVAL=5',
      enabled         => TRUE,
      comments        => 'Step 10 - sweeps every live posting through my_lower.'
   );
END;
/

BEGIN
   DBMS_SCHEDULER.CREATE_JOB (
      job_name        => 'JOB_DAILY_DIGEST',
      job_type        => 'STORED_PROCEDURE',
      job_action      => 'DAILY_DIGEST',
      start_date      => SYSTIMESTAMP,
      repeat_interval => 'FREQ=DAILY; INTERVAL=1',
      enabled         => TRUE,
      comments        => 'Step 12 - one digest notification per posting per day.'
   );
END;
/
