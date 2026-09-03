-- =========================================
-- 1. REGISTER_NOTIFICATION
-- Inserts a new notification into the table
-- =========================================
CREATE OR REPLACE PROCEDURE register_notification ...
CREATE OR REPLACE PROCEDURE register_notification (
    p_subject IN VARCHAR2,
    p_body    IN VARCHAR2
)
AS
BEGIN
INSERT INTO notifications (
    subject,
    body
)
VALUES (
           p_subject,
           p_body
       );
END;
/
-- =========================================
-- 2. CLEAN_DESCRIPTION
-- Cleans one description using MY_LOWER
-- =========================================
CREATE OR REPLACE PROCEDURE clean_description (
    p_description IN VARCHAR2,
    p_clean_description OUT VARCHAR2
)
AS
BEGIN

    p_clean_description := my_lower(p_description);
END;
/
-- =========================================
-- 3. EXPIRE_VACANCIES
-- Marks expired vacancies as inactive
-- =========================================
CREATE OR REPLACE PROCEDURE expire_vacancies
AS
BEGIN

UPDATE vacant_jobs
SET active_flag = 'N'
WHERE active_flag = 'Y'
  AND expires_at <= SYSTIMESTAMP;

COMMIT;

END;
/
-- =========================================
-- 4. ARCHIVE_INACTIVE_JOBS
-- Copies inactive vacancies to the archive
-- =========================================
CREATE OR REPLACE PROCEDURE archive_inactive_jobs
AS
BEGIN
INSERT INTO jobs_posting_archive (
    job_id,
    title,
    description,
    expires_at,
    created_at
)
SELECT
    job_id,
    title,
    description,
    expires_at,
    created_at
FROM vacant_jobs v
WHERE active_flag = 'N'
  AND NOT EXISTS (
    SELECT 1
    FROM jobs_posting_archive a
    WHERE a.job_id = v.job_id
);
COMMIT;
END;
/
-- =========================================
-- 5. PURGE_ARCHIVED_JOBS
-- Removes jobs only after they are archived
-- =========================================
CREATE OR REPLACE PROCEDURE purge_archived_jobs
AS
BEGIN
DELETE FROM vacant_jobs v
WHERE v.active_flag = 'N'
  AND EXISTS (
    SELECT 1
    FROM jobs_posting_archive a
    WHERE a.job_id = v.job_id
);
COMMIT;
END;
/
-- =========================================
-- 6. TIDY_ALL_DESCRIPTIONS
-- Cleans live descriptions using implicit cursor
-- =========================================
CREATE OR REPLACE PROCEDURE tidy_all_descriptions
AS
    v_clean_description VARCHAR2(2000);
BEGIN
FOR r IN (
        SELECT job_id, description
        FROM vacant_jobs
        WHERE active_flag = 'Y'
    )
    LOOP
        v_clean_description := my_lower(r.description);
        IF r.description <> v_clean_description THEN
UPDATE vacant_jobs
SET description = v_clean_description
WHERE job_id = r.job_id;
END IF;
END LOOP;
COMMIT;
END;
/
-- =========================================
-- 7. CREATE_DAILY_DIGEST
-- Creates one daily application-count notification
-- =========================================
REATE OR REPLACE PROCEDURE create_daily_digest
AS
    v_body VARCHAR2(4000) := '';
BEGIN
FOR r IN (
        SELECT
            v.job_id,
            v.description,
            COUNT(a.application_id) AS applicant_count
        FROM vacant_jobs v
        LEFT JOIN job_applications a
            ON v.job_id = a.job_id
        GROUP BY
            v.job_id,
            v.description
        ORDER BY
            v.job_id
    )
    LOOP
        v_body :=
            v_body ||
            'Job ' || r.job_id ||
            ' - ' || r.description ||
            ' - Applicants: ' || r.applicant_count ||
            CHR(10);
END LOOP;
    register_notification(
        'Daily Application Digest',
        v_body
    );
COMMIT;
END;
/
-- =========================================
-- 8. GET_PENDING_NOTIFICATIONS
-- Returns pending notifications using SYS_REFCURSOR
-- =========================================
CREATE OR REPLACE PROCEDURE get_pending_notifications (
    p_notifications OUT SYS_REFCURSOR
)
AS
BEGIN
OPEN p_notifications FOR
SELECT
    notification_id,
    subject,
    body
FROM notifications
WHERE sent_flag = 'N'
ORDER BY notification_id;
END;
/
-- =========================================
-- 9. MARK_NOTIFICATION_SENT
-- Marks a notification as successfully sent
-- =========================================
CREATE OR REPLACE PROCEDURE mark_notification_sent (
    p_notification_id IN NUMBER
)
AS
BEGIN
UPDATE notifications
SET sent_flag = 'Y'
WHERE notification_id = p_notification_id;
COMMIT;
END;
/
