--------------------------------------------------------------------------
-- 99_demo_proofs.sql
-- Runs the database-side checks in sequence, with labels. Delivery to
-- Slack is proven separately by running the Spring Boot consumer; this
-- file proves everything on the database side from SQL alone.
--
-- Run with:  @99_demo_proofs.sql
-- (SET SERVEROUTPUT ON so the DBMS_OUTPUT labels print.)
--------------------------------------------------------------------------
SET SERVEROUTPUT ON
SET LINESIZE 200
SET PAGESIZE 100

PROMPT
PROMPT =====================================================================
PROMPT PROOF 1 — One manual insert produces exactly one notification
PROMPT =====================================================================

-- capture the notification count before, insert one job, count after.
DECLARE
  v_before NUMBER;
  v_after  NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_before FROM notification;

  INSERT INTO vacant_jobs (description, expires_at)
  VALUES ('PROOF ONE ENGINEER', SYSTIMESTAMP + INTERVAL '30' DAY);

  SELECT COUNT(*) INTO v_after FROM notification;

  DBMS_OUTPUT.PUT_LINE('Notifications before insert : ' || v_before);
  DBMS_OUTPUT.PUT_LINE('Notifications after  insert : ' || v_after);
  DBMS_OUTPUT.PUT_LINE('Delta (must be exactly 1)   : ' || (v_after - v_before));
END;
/
COMMIT;

PROMPT
PROMPT -- the notification the insert produced:
SELECT notification_id, subject, body
  FROM notification
 ORDER BY notification_id DESC
 FETCH FIRST 1 ROW ONLY;


PROMPT
PROMPT =====================================================================
PROMPT PROOF 2 — A CAPS description comes back lower case (TRIGGER route)
PROMPT =====================================================================

UPDATE vacant_jobs
   SET description = 'SHOUTING IN CAPITALS VIA TRIGGER'
 WHERE description = 'PROOF ONE ENGINEER';
COMMIT;

PROMPT -- should read 'shouting in capitals via trigger':
SELECT description
  FROM vacant_jobs
 WHERE description = 'shouting in capitals via trigger';


PROMPT
PROMPT =====================================================================
PROMPT PROOF 3 — The bulk SWEEP route also lower-cases
PROMPT =====================================================================

-- Force an uppercase value straight in (bypassing the update trigger by
-- using a fresh insert), then run the sweep and check it was lowered.
INSERT INTO vacant_jobs (description, expires_at)
VALUES ('SWEEP SHOULD LOWERCASE THIS', SYSTIMESTAMP + INTERVAL '30' DAY);
COMMIT;

BEGIN
  sp_sweep_descriptions;
END;
/
COMMIT;

PROMPT -- should read 'sweep should lowercase this':
SELECT description
  FROM vacant_jobs
 WHERE description = 'sweep should lowercase this';

PROMPT -- idempotency: run the sweep again, nothing should change:
BEGIN
  sp_sweep_descriptions;
END;
/
PROMPT -- still 'sweep should lowercase this' (unchanged on second run):
SELECT description
  FROM vacant_jobs
 WHERE description = 'sweep should lowercase this';


PROMPT
PROMPT =====================================================================
PROMPT PROOF 4 — Three applications on one posting -> one digest line, count 3
PROMPT =====================================================================

-- The seed data already put 3 applications on 'PL/SQL DEVELOPER, HYBRID'
-- (now lower-cased by the sweep). Run the digest and inspect the line.
BEGIN
  sp_daily_digest;
END;
/
COMMIT;

PROMPT -- exactly one digest line for the 3-application posting, count = 3:
SELECT subject, body
  FROM notification
 WHERE subject = 'Daily application digest'
   AND body LIKE '%3 application(s).%';

PROMPT -- idempotency: run digest again, no NEW digest rows for those apps:
DECLARE
  v_before NUMBER;
  v_after  NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_before FROM notification
   WHERE subject = 'Daily application digest';

  sp_daily_digest;

  SELECT COUNT(*) INTO v_after FROM notification
   WHERE subject = 'Daily application digest';

  DBMS_OUTPUT.PUT_LINE('Digest lines before re-run : ' || v_before);
  DBMS_OUTPUT.PUT_LINE('Digest lines after  re-run : ' || v_after);
  DBMS_OUTPUT.PUT_LINE('Delta (must be 0)          : ' || (v_after - v_before));
END;
/
COMMIT;


PROMPT
PROMPT =====================================================================
PROMPT PROOF 5 — The board expires, files and clears itself; archive accounts
PROMPT           for every row that left
PROMPT =====================================================================

-- Insert a posting already past its expiry, with no applications, so the
-- expire->archive->purge chain can take it on the next job ticks.
INSERT INTO vacant_jobs (description, expires_at)
VALUES ('ALREADY EXPIRED, NO APPLICANTS', SYSTIMESTAMP - INTERVAL '1' MINUTE);
COMMIT;

PROMPT -- Run the procedures manually to prove the chain without waiting
PROMPT -- for the minute ticks (the jobs call these same routines):
BEGIN
  sp_expire_postings;    -- flag it inactive
  sp_archive_postings;   -- copy to archive
  sp_purge_expired;      -- delete from the board
END;
/
COMMIT;

PROMPT -- it should be GONE from the live board:
SELECT COUNT(*) AS still_on_board
  FROM vacant_jobs
 WHERE description = 'already expired, no applicants'
    OR description = 'ALREADY EXPIRED, NO APPLICANTS';

PROMPT -- and PRESENT in the archive (every row that left is accounted for):
SELECT job_id, description, archived_at
  FROM jobs_posting_archive
 WHERE description = 'ALREADY EXPIRED, NO APPLICANTS'
    OR description = 'already expired, no applicants';


PROMPT
PROMPT =====================================================================
PROMPT PROOF 6 — Scheduler view: four jobs, provable from Oracle's own views
PROMPT =====================================================================
SELECT job_name, repeat_interval, enabled, state
  FROM user_scheduler_jobs
 WHERE job_name LIKE 'JOB\_%' ESCAPE '\'
 ORDER BY job_name;

PROMPT -- recent successful runs of our four jobs only:
SELECT job_name, status, actual_start_date
  FROM user_scheduler_job_run_details
 WHERE job_name LIKE 'JOB\_%' ESCAPE '\'
 ORDER BY actual_start_date DESC
 FETCH FIRST 12 ROWS ONLY;

PROMPT
PROMPT =====================================================================
PROMPT ALL DATABASE-SIDE PROOFS COMPLETE.
PROMPT Delivery to Slack is proven by running the Spring Boot consumer.
PROMPT =====================================================================
