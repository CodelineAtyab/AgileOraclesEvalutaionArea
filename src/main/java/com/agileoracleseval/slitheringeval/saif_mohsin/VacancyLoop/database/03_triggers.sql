-- Create VacancyLoop triggers

CREATE OR REPLACE TRIGGER trg_vacant_jobs_lowercase
BEFORE UPDATE OF description ON vacant_jobs
FOR EACH ROW
DECLARE
    v_description vacant_jobs.description%TYPE;
BEGIN
    v_description := :NEW.description;
    normalize_description(v_description);
    :NEW.description := v_description;
END trg_vacant_jobs_lowercase;
/

CREATE OR REPLACE TRIGGER trg_vacant_jobs_notify
AFTER INSERT ON vacant_jobs
FOR EACH ROW
BEGIN
    register_notification(
        p_job_id  => :NEW.job_id,
        p_subject => 'New vacancy posted: ' || :NEW.job_title,
        p_body    => 'Job ID: ' || :NEW.job_id
                     || CHR(10)
                     || 'Description: ' || :NEW.description
                     || CHR(10)
                     || 'Expires at: '
                     || TO_CHAR(
                            :NEW.expires_at,
                            'YYYY-MM-DD HH24:MI:SS'
                        )
    );
END trg_vacant_jobs_notify;
/

ALTER TRIGGER trg_vacant_jobs_notify ENABLE;
