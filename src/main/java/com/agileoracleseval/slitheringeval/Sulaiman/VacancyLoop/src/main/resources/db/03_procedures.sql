--------------------------------------------------------------------------
-- 03_procedures.sql
--
-- Run order: after 01_schema_and_seed and 02_function (custom_lower),
-- before 04_triggers. Once this file runs, the two triggers can be
-- recompiled to VALID.
--
-- Every routine below does one job, is named for that job, and is safe
-- to run again on the next tick (the scheduler calls several of them).
--------------------------------------------------------------------------


--------------------------------------------------------------------------
-- register_notification
-- The one generic outbox writer. Takes a subject and a body, nothing
-- job-specific in its signature, so it stays callable from anywhere
-- (the insert trigger, the digest, future callers). Job identity, when
-- relevant, is folded into the body text by the caller.
--------------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE register_notification (
  p_subject IN VARCHAR2,
  p_body    IN VARCHAR2
)
IS
BEGIN
  INSERT INTO notification (subject, body)
  VALUES (p_subject, p_body);
END register_notification;
/


--------------------------------------------------------------------------
-- sp_normalize_description
-- Called by the BEFORE UPDATE trigger with :NEW.description as an IN OUT
-- actual parameter. Mutates the text in place via custom_lower; the
-- change lands back in :NEW with no second UPDATE statement, so there is
-- no recursion and no mutating-table error.
--------------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE sp_normalize_description (
  p_description IN OUT VARCHAR2
)
IS
BEGIN
  p_description := custom_lower(p_description);
END sp_normalize_description;
/


--------------------------------------------------------------------------
-- sp_expire_postings
-- Flips active_flag to 'N' for every live row whose expires_at has passed.
-- The WHERE clause excludes rows already 'N', so a second run on the next
-- tick finds nothing to do — safe to repeat.
--------------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE sp_expire_postings
IS
BEGIN
  UPDATE vacant_jobs
     SET active_flag = 'N'
   WHERE active_flag = 'Y'
     AND expires_at <= SYSTIMESTAMP;
END sp_expire_postings;
/


--------------------------------------------------------------------------
-- sp_archive_postings
-- Copies inactive rows into jobs_posting_archive (same shape, minus the
-- active flag). Skips job_ids already archived, so a re-run is a no-op.
-- Runs before the purge; the two are chained in one scheduled job so
-- nothing is lost in between.
--------------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE sp_archive_postings
IS
BEGIN
  INSERT INTO jobs_posting_archive (job_id, description, posted_at, expires_at)
  SELECT vj.job_id, vj.description, vj.posted_at, vj.expires_at
    FROM vacant_jobs vj
   WHERE vj.active_flag = 'N'
     AND NOT EXISTS (
           SELECT 1 FROM jobs_posting_archive a
            WHERE a.job_id = vj.job_id
         );
END sp_archive_postings;
/


--------------------------------------------------------------------------
-- sp_purge_expired
-- Deletes inactive rows from vacant_jobs, but ONLY those that:
--   (a) are provably in the archive already  -> nothing lost, and
--   (b) have no un-digested applications left -> the daily digest still
--       gets to count them, and the FK from job_applications is never
--       violated.
-- A job that fails either test simply survives to the next tick.
--------------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE sp_purge_expired
IS
BEGIN
  DELETE FROM vacant_jobs vj
   WHERE vj.active_flag = 'N'
     AND EXISTS (
           SELECT 1 FROM jobs_posting_archive a
            WHERE a.job_id = vj.job_id
         )
     AND NOT EXISTS (
           SELECT 1 FROM job_applications ja
            WHERE ja.job_id = vj.job_id
              AND ja.digested_at IS NULL
         );
END sp_purge_expired;
/


--------------------------------------------------------------------------
-- sp_daily_digest
-- Reads applications grouped per posting and reduces them to ONE
-- notification per posting: id, description, and how many applied.
--
-- Only un-digested applications are counted; each row processed is then
-- stamped digested_at, so a second run in the same window finds nothing
-- and produces no duplicate digest. This stamping is also what later
-- lets sp_purge_expired remove the posting safely.
--------------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE sp_daily_digest
IS
BEGIN
  FOR rec IN (
    SELECT ja.job_id,
           vj.description,
           COUNT(*) AS application_count
      FROM job_applications ja
      JOIN vacant_jobs vj ON vj.job_id = ja.job_id
     WHERE ja.digested_at IS NULL
     GROUP BY ja.job_id, vj.description
  ) LOOP
    register_notification(
      p_subject => 'Daily application digest',
      p_body    => 'Posting ' || rec.job_id || ' (' || rec.description ||
                   '): ' || rec.application_count || ' application(s).'
    );

    -- Stamp exactly the rows we just counted for this posting.
    UPDATE job_applications
       SET digested_at = SYSTIMESTAMP
     WHERE job_id = rec.job_id
       AND digested_at IS NULL;
  END LOOP;
END sp_daily_digest;
/


--------------------------------------------------------------------------
-- sp_get_pending_notifications
-- Opens a SYS_REFCURSOR over the un-sent notifications and hands it back
-- LIVE to the caller — it does not fetch or loop here. This is what the
-- Spring Boot consumer drains. Marking a row sent is the consumer's job,
-- so this stays a pure read that can be called again safely.
--------------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE sp_get_pending_notifications (
  p_cursor OUT SYS_REFCURSOR
)
IS
BEGIN
  OPEN p_cursor FOR
    SELECT notification_id, subject, body, created_at
      FROM notification
     WHERE sent_flag = 'N'
     ORDER BY notification_id;
END sp_get_pending_notifications;
/


--------------------------------------------------------------------------
-- sp_mark_notification_sent
-- Marks a single notification sent. The consumer calls this after each
-- successful Slack post, so a crash mid-drain can't re-send what already
-- went out.
--------------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE sp_mark_notification_sent (
  p_notification_id IN NUMBER
)
IS
BEGIN
  UPDATE notification
     SET sent_flag = 'Y'
   WHERE notification_id = p_notification_id
     AND sent_flag = 'N';
END sp_mark_notification_sent;
/
