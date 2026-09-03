-- ============================================================
-- Triggers - write to vacant_jobs, set off the right work only.
-- ============================================================

-- Step 02: the table notices an insert -> writes a JOB_POSTED notification.
CREATE OR REPLACE TRIGGER trg_vacant_jobs_after_insert
AFTER INSERT ON vacant_jobs
FOR EACH ROW
BEGIN
  register_notification(
    p_job_id   => :NEW.job_id,
    p_category => 'JOB_POSTED',
    p_subject  => 'New vacancy posted: ' || :NEW.job_title,
    p_body     => 'A new vacancy "' || :NEW.job_title ||
                  '" has been posted and will expire on ' ||
                  TO_CHAR(:NEW.expires_at, 'DD-MON-YYYY HH24:MI') || '.'
  );
END trg_vacant_jobs_after_insert;
/

-- Step 05: fires only when job_description is part of the UPDATE (avoids
-- firing on unrelated updates, e.g. the active_flag flip in lane C).
-- Sets :NEW directly instead of issuing a nested UPDATE, so there is no
-- mutating-table error and no recursion.
CREATE OR REPLACE TRIGGER trg_vacant_jobs_before_update
BEFORE UPDATE OF job_description ON vacant_jobs
FOR EACH ROW
DECLARE
  v_normalized VARCHAR2(1000);
BEGIN
  normalize_job_description(:NEW.job_description, v_normalized);
  :NEW.job_description := v_normalized;
END trg_vacant_jobs_before_update;
/
