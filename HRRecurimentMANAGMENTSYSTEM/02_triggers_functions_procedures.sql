--------------------------------------------------------------------------------
-- 02_triggers_functions_procedures.sql
-- Run SECOND, after 01_schema_and_seed.sql.
--------------------------------------------------------------------------------

-- Step 06: FUNCTION
CREATE OR REPLACE FUNCTION fn_custom_lower (
    p_text IN VARCHAR2
) RETURN VARCHAR2 AS
    v_result VARCHAR2(4000) := '';
    v_char   CHAR(1);
    v_code   PLS_INTEGER;
BEGIN
    IF p_text IS NULL THEN
        RETURN NULL;
    END IF;

    FOR i IN 1 .. LENGTH(p_text) LOOP
        v_char := SUBSTR(p_text, i, 1);
        v_code := ASCII(v_char);

        IF v_code BETWEEN 65 AND 90 THEN
            v_result := v_result || CHR(v_code + 32);
        ELSE
            v_result := v_result || v_char;
        END IF;
    END LOOP;

    RETURN v_result;
END fn_custom_lower;
/

-- Step 03: PROCEDURE
CREATE OR REPLACE PROCEDURE register_notificationhr (
    p_job_id IN vacant_jobs.job_id%TYPE
) AS
    v_title vacant_jobs.job_title%TYPE;
    v_desc  vacant_jobs.description%TYPE;
BEGIN
    SELECT job_title, description
    INTO   v_title, v_desc
    FROM   vacant_jobs
    WHERE  job_id = p_job_id;

    INSERT INTO notificationshr (job_id, subject, body, sent_flag)
    VALUES (
        p_job_id,
        'New vacancy posted: ' || v_title,
        'A new job posting (ID ' || p_job_id || ') has gone live: ' || v_title ||
        '. Description: ' || v_desc,
        'N'
    );
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        NULL;
END register_notificationhr;
/

-- Step 02: TRIGGER
CREATE OR REPLACE TRIGGER trg_vacant_jobs_after_insert
AFTER INSERT ON vacant_jobs
FOR EACH ROW
BEGIN
    register_notificationhr(:NEW.job_id);
END trg_vacant_jobs_after_insert;
/

-- Step 05: TRIGGER
CREATE OR REPLACE TRIGGER trg_vacant_jobs_before_update
BEFORE UPDATE OF description ON vacant_jobs
FOR EACH ROW
WHEN (NEW.description IS NOT NULL)
BEGIN
    :NEW.description := fn_custom_lower(:NEW.description);
END trg_vacant_jobs_before_update;
/

-- Steps 07-09: PROCEDURE
CREATE OR REPLACE PROCEDURE prc_expire_archive_purge
AS
BEGIN
    UPDATE vacant_jobs
    SET    is_active = 'N'
    WHERE  is_active = 'Y'
      AND  expires_at < SYSTIMESTAMP;

    INSERT INTO jobs_posting_archive (job_id, job_title, description, posted_at, expires_at)
    SELECT job_id, job_title, description, posted_at, expires_at
    FROM   vacant_jobs
    WHERE  is_active = 'N';

    DELETE FROM vacant_jobs
    WHERE  is_active = 'N';

    COMMIT;
END prc_expire_archive_purge;
/

-- View used by Step 12
CREATE OR REPLACE VIEW v_all_postings AS
SELECT job_id, job_title, description FROM vacant_jobs
UNION ALL
SELECT job_id, job_title, description FROM jobs_posting_archive;
/

-- Step 12: PROCEDURE
CREATE OR REPLACE PROCEDURE prc_daily_digest
AS
BEGIN
    FOR rec IN (
        SELECT ja.job_id,
               p.description,
               COUNT(*) AS applicant_count
        FROM   job_applications ja
        JOIN   v_all_postings p ON p.job_id = ja.job_id
        WHERE  ja.digested = 'N'
        GROUP BY ja.job_id, p.description
    ) LOOP
        INSERT INTO notificationshr (job_id, subject, body, sent_flag)
        VALUES (
            rec.job_id,
            'Daily Application Digest',
            'Job ID: ' || rec.job_id ||
            ' | Description: ' || rec.description ||
            ' | Applications: ' || rec.applicant_count,
            'N'
        );

        UPDATE job_applications
        SET    digested = 'Y'
        WHERE  job_id = rec.job_id
          AND  digested = 'N';
    END LOOP;

    COMMIT;
END prc_daily_digest;
/

-- Step 10: PROCEDURE
CREATE OR REPLACE PROCEDURE prc_sweep_lowercase_descriptions
AS
BEGIN
    FOR rec IN (
        SELECT job_id, description
        FROM   vacant_jobs
        WHERE  is_active = 'Y'
    ) LOOP
        UPDATE vacant_jobs
        SET    description = fn_custom_lower(rec.description)
        WHERE  job_id = rec.job_id;
    END LOOP;

    COMMIT;
END prc_sweep_lowercase_descriptions;
/

-- Supports step 15 (Java marks a notification sent after a successful post)
CREATE OR REPLACE PROCEDURE mark_notification_sent (
    p_notification_id IN notificationshr.notification_id%TYPE
) AS
BEGIN
    UPDATE notificationshr
    SET    sent_flag = 'Y'
    WHERE  notification_id = p_notification_id;

    COMMIT;
END mark_notification_sent;
/

-- Step 13: PROCEDURE
CREATE OR REPLACE PROCEDURE get_pending_notifications (
    p_cursor OUT SYS_REFCURSOR
)
AS
BEGIN
    OPEN p_cursor FOR
    SELECT notification_id,
           job_id,
           subject,
           body
    FROM   notificationshr
    WHERE  sent_flag = 'N'
    ORDER BY notification_id;
END get_pending_notifications;
/

SELECT object_name, object_type, status
FROM   user_objects
WHERE  object_name IN (
    'FN_CUSTOM_LOWER', 'REGISTER_NOTIFICATIONHR',
    'TRG_VACANT_JOBS_AFTER_INSERT', 'TRG_VACANT_JOBS_BEFORE_UPDATE',
    'PRC_EXPIRE_ARCHIVE_PURGE', 'V_ALL_POSTINGS', 'PRC_DAILY_DIGEST',
    'PRC_SWEEP_LOWERCASE_DESCRIPTIONS', 'MARK_NOTIFICATION_SENT',
    'GET_PENDING_NOTIFICATIONS'
)
ORDER BY object_type, object_name;
