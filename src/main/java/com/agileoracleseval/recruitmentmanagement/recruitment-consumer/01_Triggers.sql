-- TRIGGERS

-- Lane A - 2 - Detect a new vacancy insert and automatically create its notification
-- call the procedure automatically whenever HR inserts a vacancy
CREATE OR REPLACE TRIGGER trg_vacant_job_posted

AFTER INSERT ON vacant_jobs

FOR EACH ROW

BEGIN

    register_notification('New Vacancy Posted', 'Job ID ' || :NEW.job_id ||' - ' || :NEW.job_title ||' has been posted.');

END;
/

-- Lane B - 5 - Detect a description update before it is saved and clean the new text
-- Create a trigger that is run when DESCRIPTION is updated
CREATE OR REPLACE TRIGGER trg_normalize_job_description

BEFORE UPDATE OF description
ON vacant_jobs

FOR EACH ROW

DECLARE

    v_clean_description VARCHAR2(1000);

BEGIN

    normalize_job_description(:NEW.description, v_clean_description);

    :NEW.description := v_clean_description;

END;
/
