-- =========================================
-- 1. TRG_JOB_INSERT_NOTIFICATION
-- Creates a notification automatically
-- after a new vacancy is inserted
-- =========================================
CREATE OR REPLACE TRIGGER trg_job_insert_notification
AFTER INSERT ON vacant_jobs
FOR EACH ROW
BEGIN
    register_notification(
        'New Vacancy',
        'Job ' || :NEW.job_id || ' - ' || :NEW.title || ' has been posted'
    );
END;
/

CREATE OR REPLACE TRIGGER trg_clean_description
BEFORE UPDATE OF description
       ON vacant_jobs
           FOR EACH ROW
DECLARE
v_clean_description VARCHAR2(2000);
BEGIN
    clean_description(
        :NEW.description,
        v_clean_description
    );
    :NEW.description := v_clean_description;
END;
/

-- =========================================
-- 2. TRG_CLEAN_DESCRIPTION
-- Cleans the job description automatically
-- before an updated description is saved
-- =========================================
CREATE OR REPLACE TRIGGER trg_clean_description
BEFORE UPDATE OF description
       ON vacant_jobs
           FOR EACH ROW
DECLARE
v_clean_description VARCHAR2(2000);
BEGIN
    clean_description(
        :NEW.description,
        v_clean_description
    );
    :NEW.description := v_clean_description;
END;
/
