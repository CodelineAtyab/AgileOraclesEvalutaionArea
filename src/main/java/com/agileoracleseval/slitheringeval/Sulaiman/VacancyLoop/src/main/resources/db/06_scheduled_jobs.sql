--------------------------------------------------------------------------
-- 06_scheduled_jobs.sql
--
-- Four scheduled jobs, each with a stated interval, safe to run again on
-- the next tick, and provable from the scheduler's own views:
--   SELECT job_name, repeat_interval, enabled, state
--     FROM user_scheduler_jobs;
--   SELECT job_name, status, actual_start_date
--     FROM user_scheduler_job_run_details ORDER BY actual_start_date;
--
-- Drop-first so the script is re-runnable (same idempotency choice as the
-- schema file). DROP_JOB is wrapped to ignore "job does not exist".
--------------------------------------------------------------------------

DECLARE
  PROCEDURE drop_if_exists(p_name IN VARCHAR2) IS
  BEGIN
    DBMS_SCHEDULER.DROP_JOB(job_name => p_name, force => TRUE);
  EXCEPTION
    WHEN OTHERS THEN
      IF SQLCODE != -27475 THEN  -- ORA-27475: "unknown job"
        RAISE;
      END IF;
  END;
BEGIN
  drop_if_exists('JOB_EXPIRE_POSTINGS');
  drop_if_exists('JOB_ARCHIVE_AND_PURGE');
  drop_if_exists('JOB_SWEEP_DESCRIPTIONS');
  drop_if_exists('JOB_DAILY_DIGEST');
END;
/


--------------------------------------------------------------------------
-- Expire. Every minute, mark timed-out postings inactive.
--------------------------------------------------------------------------
BEGIN
  DBMS_SCHEDULER.CREATE_JOB(
    job_name        => 'JOB_EXPIRE_POSTINGS',
    job_type        => 'PLSQL_BLOCK',
    job_action      => 'BEGIN sp_expire_postings; END;',
    start_date      => SYSTIMESTAMP,
    repeat_interval => 'FREQ=MINUTELY; INTERVAL=1',
    enabled         => TRUE,
    comments        => 'Flip active_flag to N once expires_at passes.'
  );
END;
/


--------------------------------------------------------------------------
-- Archive then purge, in ONE job so they share a tick and a transaction.
-- Nothing may be lost in between: archive copies first, purge deletes
-- only what is provably archived and has no un-digested applications.
-- Every minute.
--------------------------------------------------------------------------
BEGIN
  DBMS_SCHEDULER.CREATE_JOB(
    job_name        => 'JOB_ARCHIVE_AND_PURGE',
    job_type        => 'PLSQL_BLOCK',
    job_action      => 'BEGIN sp_archive_postings; sp_purge_expired; END;',
    start_date      => SYSTIMESTAMP,
    repeat_interval => 'FREQ=MINUTELY; INTERVAL=1',
    enabled         => TRUE,
    comments        => 'Copy inactive rows to archive, then purge only the '
                    || 'safely-archived, fully-digested ones.'
  );
END;
/


--------------------------------------------------------------------------
-- Sweep. Every five minutes, lower-case every live description row by
-- row. Idempotent; converges with the trigger route on identical text.
--------------------------------------------------------------------------
BEGIN
  DBMS_SCHEDULER.CREATE_JOB(
    job_name        => 'JOB_SWEEP_DESCRIPTIONS',
    job_type        => 'PLSQL_BLOCK',
    job_action      => 'BEGIN sp_sweep_descriptions; END;',
    start_date      => SYSTIMESTAMP,
    repeat_interval => 'FREQ=MINUTELY; INTERVAL=5',
    enabled         => TRUE,
    comments        => 'Bulk lower-case sweep of live descriptions.'
  );
END;
/


--------------------------------------------------------------------------
-- Digest. Once every 24 hours, reduce applications to one notification
-- per posting with its count. Stamps digested_at so a re-run produces no
-- duplicate.
--------------------------------------------------------------------------
BEGIN
  DBMS_SCHEDULER.CREATE_JOB(
    job_name        => 'JOB_DAILY_DIGEST',
    job_type        => 'PLSQL_BLOCK',
    job_action      => 'BEGIN sp_daily_digest; END;',
    start_date      => SYSTIMESTAMP,
    repeat_interval => 'FREQ=DAILY; INTERVAL=1',
    enabled         => TRUE,
    comments        => 'One digest notification per posting per day.'
  );
END;
/


--------------------------------------------------------------------------
-- Proof (run these to confirm the jobs from the scheduler's own views):
--------------------------------------------------------------------------
-- SELECT job_name, repeat_interval, enabled, state
--   FROM user_scheduler_jobs
--  WHERE job_name LIKE 'JOB\_%' ESCAPE '\'
--  ORDER BY job_name;
--
-- SELECT job_name, status, actual_start_date
--   FROM user_scheduler_job_run_details
--  ORDER BY actual_start_date DESC;
