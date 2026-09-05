-- The Vacancy Loop
-- 04 - Workflow procedures and cursor implementations

CREATE OR REPLACE PROCEDURE expire_vacant_jobs
AS
BEGIN
    UPDATE vacant_jobs
    SET is_active = 'N'
    WHERE is_active = 'Y'
      AND expires_at <= SYSTIMESTAMP;
END;
/

CREATE OR REPLACE PROCEDURE archive_inactive_jobs
AS
BEGIN
    INSERT INTO jobs_posting_archive (
        job_id,
        title,
        description,
        posted_at,
        expires_at
    )
    SELECT v.job_id,
           v.title,
           v.description,
           v.posted_at,
           v.expires_at
    FROM vacant_jobs v
    WHERE v.is_active = 'N'
      AND NOT EXISTS (
          SELECT 1
          FROM jobs_posting_archive a
          WHERE a.job_id = v.job_id
      );
END;
/

CREATE OR REPLACE PROCEDURE purge_archived_jobs
AS
BEGIN
    DELETE FROM vacant_jobs v
    WHERE v.is_active = 'N'
      AND EXISTS (
          SELECT 1
          FROM jobs_posting_archive a
          WHERE a.job_id = v.job_id
      );
END;
/

CREATE OR REPLACE PROCEDURE tidy_live_job_descriptions
AS
    v_description    vacant_jobs.description%TYPE;
    v_updated_count  NUMBER := 0;
BEGIN
    -- Implicit cursor: Oracle opens, fetches and closes it.
    FOR rec IN (
        SELECT job_id,
               description
        FROM vacant_jobs
        WHERE is_active = 'Y'
        ORDER BY job_id
    ) LOOP
        v_description := rec.description;

        normalize_job_description(v_description);

        UPDATE vacant_jobs
        SET description = v_description
        WHERE job_id = rec.job_id
          AND description <> v_description;

        v_updated_count := v_updated_count + SQL%ROWCOUNT;
    END LOOP;

    DBMS_OUTPUT.PUT_LINE(
        'Descriptions updated: ' || v_updated_count
    );
END;
/

CREATE OR REPLACE PROCEDURE create_daily_application_digest
AS
    -- Explicit cursor: opened, fetched and closed manually.
    CURSOR c_live_jobs IS
        SELECT job_id,
               description
        FROM vacant_jobs
        WHERE is_active = 'Y'
        ORDER BY job_id;

    v_job_id             vacant_jobs.job_id%TYPE;
    v_description        vacant_jobs.description%TYPE;
    v_application_count  NUMBER;
    v_digest_count       NUMBER;
    v_body               notification.body%TYPE;
BEGIN
    SELECT COUNT(*)
    INTO v_digest_count
    FROM notification
    WHERE subject = 'Daily application digest'
      AND created_at >= TRUNC(SYSDATE)
      AND created_at < TRUNC(SYSDATE) + 1;

    IF v_digest_count > 0 THEN
        RETURN;
    END IF;

    OPEN c_live_jobs;

    LOOP
        FETCH c_live_jobs
        INTO v_job_id,
             v_description;

        EXIT WHEN c_live_jobs%NOTFOUND;

        SELECT COUNT(*)
        INTO v_application_count
        FROM job_applications
        WHERE job_id = v_job_id;

        v_body :=
            v_body
            || 'Job ID: '
            || v_job_id
            || ', Description: '
            || v_description
            || ', Applications: '
            || v_application_count
            || CHR(10);
    END LOOP;

    CLOSE c_live_jobs;

    IF v_body IS NOT NULL THEN
        register_notification(
            p_subject => 'Daily application digest',
            p_body    => v_body,
            p_job_id  => NULL
        );
    END IF;
END;
/

CREATE OR REPLACE PROCEDURE get_pending_notifications (
    p_notification_cursor OUT SYS_REFCURSOR
)
AS
BEGIN
    OPEN p_notification_cursor FOR
        SELECT notification_id,
               subject,
               body,
               created_at
        FROM notification
        WHERE status = 'PENDING'
        ORDER BY notification_id;
END;
/

CREATE OR REPLACE PROCEDURE mark_notification_sent (
    p_notification_id IN NUMBER
)
AS
BEGIN
    UPDATE notification
    SET status  = 'SENT',
        sent_at = SYSTIMESTAMP
    WHERE notification_id = p_notification_id
      AND status = 'PENDING';
END;
/