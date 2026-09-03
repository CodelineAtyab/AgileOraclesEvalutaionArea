CREATE OR REPLACE TRIGGER trg_vacant_jobs_after_ins
AFTER INSERT ON vacant_jobs
FOR EACH ROW
BEGIN
    register_notification(
        p_notification_type => 'JOB_POSTED',
        p_subject           => 'New vacancy posted: ' || :NEW.job_title,
        p_body              =>
            'Job ID: ' || TO_CHAR(:NEW.job_id) || CHR(10) ||
            'Title: ' || :NEW.job_title || CHR(10) ||
            'Description: ' || :NEW.description || CHR(10) ||
            'Expires at: ' ||
                TO_CHAR(:NEW.expires_at, 'YYYY-MM-DD HH24:MI:SS'),
        p_dedup_key         => 'JOB_POSTED:' || TO_CHAR(:NEW.job_id)
    );
END trg_vacant_jobs_after_ins;
/

SHOW ERRORS TRIGGER trg_vacant_jobs_after_ins;

CREATE OR REPLACE TRIGGER trg_vacant_jobs_before_desc
BEFORE UPDATE OF description ON vacant_jobs
FOR EACH ROW
DECLARE
    v_description VARCHAR2(2000);
BEGIN
    IF :NEW.description <> :OLD.description THEN
        v_description := :NEW.description;
        normalize_job_description(v_description);
        :NEW.description := v_description;
    END IF;
END trg_vacant_jobs_before_desc;
/

SHOW ERRORS TRIGGER trg_vacant_jobs_before_desc;

PROMPT Vacancy triggers created successfully.
