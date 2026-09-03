--------------------------------------------------------------------------
-- 05_sweep.sql
--
-- Bulk sweep procedure: walks every live posting row by row and
-- lower-cases its description via custom_lower. Deliberately row-by-row
-- (an implicit cursor via the FOR loop) rather than a single set-based
-- UPDATE.
--
-- Idempotent: custom_lower on already-lowercase text returns it
-- unchanged, so running this a second time writes the same values and
-- changes nothing observable. It also lands on identical text to the
-- trigger route, since both call the same function.
--
-- We only touch active rows and only write when the lowered text
-- actually differs, to avoid needless redo and needless row versions.
--------------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE sp_sweep_descriptions
IS
  v_lowered vacant_jobs.description%TYPE;
BEGIN
  FOR rec IN (
    SELECT job_id, description
      FROM vacant_jobs
     WHERE active_flag = 'Y'
  ) LOOP
    v_lowered := custom_lower(rec.description);

    IF v_lowered != rec.description THEN
      UPDATE vacant_jobs
         SET description = v_lowered
       WHERE job_id = rec.job_id;
    END IF;
  END LOOP;
END sp_sweep_descriptions;
/
