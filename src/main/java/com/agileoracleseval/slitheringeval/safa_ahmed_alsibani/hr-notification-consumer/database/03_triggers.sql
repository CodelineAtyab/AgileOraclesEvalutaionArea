-- The Vacancy Loop
-- 03 - Database triggers

CREATE OR REPLACE TRIGGER trg_job_posted_notification
AFTER INSERT ON vacant_jobs
FOR EACH ROW
BEGIN
    register_notification(
        p_subject => 'New vacancy: ' || :NEW.title,
        p_body    => 'Job ' || :NEW.job_id
                     || ' has been posted: '
                     || :NEW.description,
        p_job_id  => :NEW.job_id
    );
END;
/

CREATE OR REPLACE TRIGGER trg_normalize_job_description
BEFORE UPDATE OF description ON vacant_jobs
FOR EACH ROW
DECLARE
    v_description VARCHAR2(1000);
BEGIN
    v_description := :NEW.description;

    normalize_job_description(v_description);

    :NEW.description := v_description;
END;
/