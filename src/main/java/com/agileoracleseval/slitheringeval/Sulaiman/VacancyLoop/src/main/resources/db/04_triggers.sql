--------------------------------------------------------------------------
-- 04_triggers.sql
--
-- Run order: 01_schema_and_seed -> 02_function (custom_lower) ->
-- 03_procedures (register_notification, sp_normalize_description, ...) ->
-- 04_triggers. Filenames are numbered so filename order == safe compile
-- order; every routine these triggers call already exists by the time
-- this file runs, so both triggers compile VALID on the first pass.
--------------------------------------------------------------------------

--------------------------------------------------------------------------
-- trg_vacant_jobs_after_insert
-- HR's insert is the only manual act. The insert alone must set the
-- notification in motion; nobody calls anything by hand.
--------------------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_vacant_jobs_after_insert
AFTER INSERT ON vacant_jobs
FOR EACH ROW
DECLARE
  v_subject VARCHAR2(200);
  v_body    VARCHAR2(4000);
BEGIN
  v_subject := 'New vacancy posted';
  v_body    := 'Job ID ' || :NEW.job_id || ': ' || :NEW.description ||
               ' (expires ' ||
               TO_CHAR(:NEW.expires_at, 'YYYY-MM-DD HH24:MI') || ')';

  register_notification(p_subject => v_subject, p_body => v_body);
END;
/

--------------------------------------------------------------------------
-- trg_vacant_jobs_before_update
-- Any update that touches description hands the text on for treatment
-- before the row is actually written. The trigger's only job is to
-- notice and delegate; the procedure owns the hand-off.
--
-- :NEW.description is passed as an IN OUT actual parameter directly —
-- the procedure's mutation lands back into :NEW with no second UPDATE
-- statement, so there is no recursion and no mutating-table error.
-- "UPDATE OF description" means this fires only when description is in
-- the SET list, not on every column update.
--------------------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_vacant_jobs_before_update
BEFORE UPDATE OF description ON vacant_jobs
FOR EACH ROW
BEGIN
  sp_normalize_description(:NEW.description);
END;
/
