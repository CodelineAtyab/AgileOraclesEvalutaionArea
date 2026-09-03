-- Triggers

CREATE OR REPLACE TRIGGER trg_vacant_after_insert
AFTER INSERT ON vacant_jobs
FOR EACH ROW
BEGIN
    register_notification(
        'new vacancy posted',
        'job_id=' || :NEW.job_id
        || ' title=' || :NEW.title
        || ' expires=' || TO_CHAR(:NEW.expires_at, 'YYYY-MM-DD HH24:MI:SS'),
        'JOB_POSTED',
        :NEW.job_id
    );
END;
/

-- CLOB cannot use UPDATE OF description
CREATE OR REPLACE TRIGGER trg_vacant_before_upd_desc
BEFORE UPDATE ON vacant_jobs
FOR EACH ROW
BEGIN
    IF UPDATING('DESCRIPTION') AND :NEW.description IS NOT NULL THEN
        :NEW.description := my_lower(DBMS_LOB.SUBSTR(:NEW.description, 4000, 1));
    END IF;
END;
/
