-- ========================= 05_triggers.sql =========================
-- =========== Triggers for automating vacancy operations ===========

-- ================= New Vacancy Notification Trigger =================
CREATE OR REPLACE TRIGGER trg_vacant_job_insert
AFTER INSERT ON vacant_jobs
FOR EACH ROW
BEGIN
    -- Create a notification when HR posts a new vacancy
    register_notification(
        :NEW.job_id,
        'New Vacancy Posted',
        'A new vacancy for ' || :NEW.title || ' has been posted.'
    );
END;
/
------------------- Test -------------------
INSERT INTO vacant_jobs (title, description, expires_at, active)
VALUES ('Cloud Engineer', 'MANAGE CLOUD INFRASTRUCTURE', SYSTIMESTAMP + NUMTODSINTERVAL(1, 'DAY'), 'Y');
COMMIT;

SELECT notification_id, related_job_id, subject, body, status
FROM notifications
ORDER BY notification_id;


-- ================= Description Lowercase Trigger =================
CREATE OR REPLACE TRIGGER trg_vacant_job_lowercase
BEFORE INSERT OR UPDATE OF description ON vacant_jobs
FOR EACH ROW
BEGIN
    -- Convert the job description to lowercase
    -- using our custom function
    :NEW.description := custom_lowercase(:NEW.description);
END;
/
------------------- Test -------------------
UPDATE vacant_jobs
SET description = 'THIS DESCRIPTION SHOULD BECOME LOWERCASE'
WHERE job_id = 1;
COMMIT;

SELECT job_id, description
FROM vacant_jobs
WHERE job_id = 1;