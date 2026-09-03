-- ========================= 04_procedures.sql =========================

-- ================= Register Notification Procedure ==================
CREATE OR REPLACE PROCEDURE register_notification (p_related_job_id IN NUMBER, p_subject IN VARCHAR2, p_body IN VARCHAR2)
AS
BEGIN
    -- Insert a new pending notification
    INSERT INTO notifications (related_job_id, subject, body, status)
    VALUES (p_related_job_id, p_subject, p_body, 'PENDING');

END;
/
------------------- Test -------------------
BEGIN
    register_notification(1, 'Test Notification', 'This is a test notification.');
END;
/

SELECT * 
FROM notifications
ORDER BY notification_id;

SELECT object_name, object_type, status
FROM user_objects
WHERE object_name = 'REGISTER_NOTIFICATION';


-- ================= Expire Jobs Procedure =================
CREATE OR REPLACE PROCEDURE expire_jobs
AS
BEGIN
    -- Mark expired active jobs as inactive
    UPDATE vacant_jobs
    SET active = 'N'
    WHERE active = 'Y'
      AND expires_at <= SYSTIMESTAMP;

END;
/
------------------- Test -------------------
SELECT job_id, title, expires_at, active
FROM vacant_jobs
ORDER BY job_id;

BEGIN
    expire_jobs;
END;
/

SELECT job_id, title, expires_at, active
FROM vacant_jobs
ORDER BY job_id;

SELECT object_name, object_type, status
FROM user_objects
WHERE object_name = 'EXPIRE_JOBS';


-- ================= Archive Jobs Procedure =================
CREATE OR REPLACE PROCEDURE archive_jobs
AS
BEGIN
    -- Copy inactive jobs to the archive table
    INSERT INTO jobs_posting_archive (job_id, title, description, expires_at, created_at, archived_at)
    SELECT v.job_id, v.title, v.description, v.expires_at, v.created_at, SYSTIMESTAMP
    FROM vacant_jobs v
    WHERE v.active = 'N'
      AND NOT EXISTS (
          SELECT 1
          FROM jobs_posting_archive a
          WHERE a.job_id = v.job_id
      );
END;
/
------------------- Test -------------------
BEGIN
    archive_jobs;
END;
/

SELECT *
FROM jobs_posting_archive
ORDER BY job_id;

SELECT object_name, object_type, status
FROM user_objects
WHERE object_name = 'ARCHIVE_JOBS';


-- ================= Purge Jobs Procedure =================
CREATE OR REPLACE PROCEDURE purge_jobs
AS
BEGIN
    -- Delete inactive jobs only after they have been archived
    DELETE FROM vacant_jobs v
    WHERE v.active = 'N'
      AND EXISTS (
          SELECT 1
          FROM jobs_posting_archive a
          WHERE a.job_id = v.job_id
      );
END;
/
------------------- Test -------------------
BEGIN
    purge_jobs;
END;
/

SELECT job_id, title, active
FROM vacant_jobs
ORDER BY job_id;

SELECT *
FROM jobs_posting_archive
ORDER BY job_id;

SELECT object_name, object_type, status
FROM user_objects
WHERE object_name = 'PURGE_JOBS';


-- ================= Daily Digest Procedure =================
CREATE OR REPLACE PROCEDURE generate_daily_digest
AS
    v_body VARCHAR2(4000) := '';
    v_count NUMBER;
BEGIN
    -- Loop through all current job postings
    FOR r IN (
        SELECT job_id, description
        FROM vacant_jobs
        ORDER BY job_id
    )
    LOOP
        -- Count applications for the current job
        SELECT COUNT(*)
        INTO v_count
        FROM job_applications
        WHERE job_id = r.job_id;

        -- Add one line for this job to the digest message
        v_body := v_body
                  || 'Job ID: ' || r.job_id
                  || ' | Description: ' || r.description
                  || ' | Applicants: ' || v_count
                  || CHR(10);
    END LOOP;
    
    -- Create only one notification containing the full digest
    IF v_body IS NOT NULL THEN

        register_notification(
            NULL,
            'Daily Job Applications Digest',
            v_body
        );
    END IF;
END;
/
------------------- Test -------------------
BEGIN
    generate_daily_digest;
END;
/

SELECT notification_id, subject, body, status
FROM notifications
ORDER BY notification_id;

SELECT object_name, object_type, status
FROM user_objects
WHERE object_name = 'GENERATE_DAILY_DIGEST';


-- ================= Archive Jobs Procedure =================
-- ===== Archives inactive vacancies and purges them after archiving =====
CREATE OR REPLACE PROCEDURE archive_jobs
AS
BEGIN
    -- Copy inactive jobs to the archive table
    INSERT INTO jobs_posting_archive (job_id, title, description, expires_at, created_at, archived_at)
    SELECT v.job_id, v.title, v.description, v.expires_at, v.created_at, SYSTIMESTAMP
    FROM vacant_jobs v
    WHERE v.active = 'N'
      AND NOT EXISTS (
          SELECT 1
          FROM jobs_posting_archive a
          WHERE a.job_id = v.job_id
      );
    -- Remove inactive jobs only after they are archived
    purge_jobs;
END;
/
------------------- Test -------------------
UPDATE vacant_jobs
SET active = 'N'
WHERE job_id = 3;
COMMIT;

BEGIN
    archive_jobs;
END;
/

SELECT *
FROM jobs_posting_archive
WHERE job_id = 3;

SELECT *
FROM vacant_jobs
WHERE job_id = 3;


-- ================= Lowercase Sweep Procedure =================
-- ===== Converts all live job descriptions to lowercase row by row =====
CREATE OR REPLACE PROCEDURE lowercase_sweep
AS
BEGIN
    -- Loop through all live job postings using an implicit cursor
    FOR r IN (
        SELECT job_id, description
        FROM vacant_jobs
        WHERE active = 'Y'
    )
    LOOP
        -- Update the description using the custom lowercase function
        UPDATE vacant_jobs
        SET description = custom_lowercase(r.description)
        WHERE job_id = r.job_id;
    END LOOP;
END;
/
------------------- Test -------------------
BEGIN
    lowercase_sweep;
END;
/

SELECT job_id, title, description, active
FROM vacant_jobs
ORDER BY job_id;