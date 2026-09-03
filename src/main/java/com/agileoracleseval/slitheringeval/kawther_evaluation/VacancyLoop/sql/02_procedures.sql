-- ============================================================
-- Procedures - one job each, named for what it does.
-- ============================================================

-- register_notification: the single place that writes into the outbox.
-- Name is fixed by the brief (step 03).
CREATE OR REPLACE PROCEDURE register_notification (
  p_job_id   IN NUMBER,
  p_category IN VARCHAR2,
  p_subject  IN VARCHAR2,
  p_body     IN VARCHAR2
)
AS
BEGIN
  INSERT INTO notification_outbox (job_id, msg_category, msg_subject, msg_body)
  VALUES (p_job_id, p_category, p_subject, p_body);
END register_notification;
/

-- normalize_job_description: hands the raw text to the shared lowercasing
-- function (lowercase_text, in 04_functions.sql). Used by the update
-- trigger (step 05); run 04_functions.sql before this file.
CREATE OR REPLACE PROCEDURE normalize_job_description (
  p_original_text   IN  VARCHAR2,
  p_normalized_text OUT VARCHAR2
)
AS
BEGIN
  p_normalized_text := lowercase_text(p_original_text);
END normalize_job_description;
/

-- expire_postings: step 07. Flips active_flag off once expires_at has
-- passed. Idempotent by construction - only touches rows still 'Y'.
CREATE OR REPLACE PROCEDURE expire_postings
AS
BEGIN
  UPDATE vacant_jobs
     SET active_flag = 'N'
   WHERE active_flag = 'Y'
     AND expires_at <= SYSTIMESTAMP;
  COMMIT;
END expire_postings;
/

-- archive_and_purge_expired_postings: steps 08+09 in one call, one
-- transaction. Copy-then-delete never commits in between, so nothing
-- can be lost between the two steps; NOT EXISTS makes a re-run safe
-- even if the same row was somehow already archived.
CREATE OR REPLACE PROCEDURE archive_and_purge_expired_postings
AS
BEGIN
  INSERT INTO jobs_posting_archive (job_id, job_title, job_description, posted_on, expires_at)
    SELECT v.job_id, v.job_title, v.job_description, v.posted_on, v.expires_at
    FROM vacant_jobs v
    WHERE v.active_flag = 'N'
      AND NOT EXISTS (SELECT 1 FROM jobs_posting_archive a WHERE a.job_id = v.job_id);

  DELETE FROM vacant_jobs WHERE active_flag = 'N';

  COMMIT;
END archive_and_purge_expired_postings;
/

-- sweep_job_descriptions: step 10. Implicit cursor over every live
-- posting, reusing the same lowercase_text function as the update
-- trigger. The WHERE guard on the UPDATE skips rows already normalized,
-- so a second run in a row changes nothing (idempotent). Each row is
-- updated inside its own BEGIN/EXCEPTION block so one misbehaving row
-- is logged and skipped instead of aborting the whole sweep.
CREATE OR REPLACE PROCEDURE sweep_job_descriptions
AS
BEGIN
  FOR rec IN (SELECT job_id, job_description FROM vacant_jobs WHERE active_flag = 'Y') LOOP
    BEGIN
      UPDATE vacant_jobs
         SET job_description = lowercase_text(rec.job_description)
       WHERE job_id = rec.job_id
         AND job_description != lowercase_text(rec.job_description);
    EXCEPTION
      WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('sweep_job_descriptions: skipped job_id ' || rec.job_id || ' due to: ' || SQLERRM);
    END;
  END LOOP;
  COMMIT;
END sweep_job_descriptions;
/

-- send_daily_application_digest: step 12. Implicit cursor grouping
-- not-yet-digested applications per posting into exactly one
-- notification each (id, description, count). digested_flag on
-- job_applications makes this safe to run any number of times: each
-- application is ever counted once, no matter how often the job fires.
-- Each posting's notification+flag update is wrapped in its own
-- BEGIN/EXCEPTION block, and the flag is only flipped to 'Y' after that
-- posting's notification actually succeeded - so a misbehaving row
-- (one posting's notification fails) is logged and skipped, its
-- applications stay pending for the next run, and every other posting
-- in the same batch is unaffected.
CREATE OR REPLACE PROCEDURE send_daily_application_digest
AS
BEGIN
  FOR rec IN (
    SELECT v.job_id,
           v.job_title,
           v.job_description,
           COUNT(a.application_id) AS applicant_count
    FROM vacant_jobs v
    JOIN job_applications a ON a.job_id = v.job_id
    WHERE a.digested_flag = 'N'
    GROUP BY v.job_id, v.job_title, v.job_description
  ) LOOP
    BEGIN
      register_notification(
        p_job_id   => rec.job_id,
        p_category => 'DAILY_DIGEST',
        p_subject  => 'Daily application digest: ' || rec.job_title,
        p_body     => 'Posting #' || rec.job_id || ' (' || rec.job_description || ') received ' ||
                      rec.applicant_count || ' new application(s).'
      );

      UPDATE job_applications
         SET digested_flag = 'Y'
       WHERE job_id = rec.job_id
         AND digested_flag = 'N';
    EXCEPTION
      WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('send_daily_application_digest: skipped job_id ' || rec.job_id
                              || ' due to: ' || SQLERRM || ' - its applications stay pending for the next run.');
    END;
  END LOOP;

  COMMIT;
END send_daily_application_digest;
/
