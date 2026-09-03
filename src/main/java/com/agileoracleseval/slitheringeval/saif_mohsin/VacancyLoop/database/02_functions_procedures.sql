-- Create the custom function and procedures in dependency order

CREATE OR REPLACE FUNCTION custom_lowercase (
    p_text IN VARCHAR2
)
RETURN VARCHAR2
IS
    v_result  vacant_jobs.description%TYPE := '';
    v_char    VARCHAR2(1);
    v_position PLS_INTEGER;

    v_uppercase CONSTANT VARCHAR2(26) :=
        'ABCDEFGHIJKLMNOPQRSTUVWXYZ';

    v_lowercase CONSTANT VARCHAR2(26) :=
        'abcdefghijklmnopqrstuvwxyz';
BEGIN
    IF p_text IS NULL THEN
        RETURN NULL;
    END IF;

    FOR i IN 1 .. LENGTH(p_text) LOOP
        v_char := SUBSTR(p_text, i, 1);
        v_position := INSTR(v_uppercase, v_char);

        IF v_position > 0 THEN
            v_result :=
                v_result || SUBSTR(v_lowercase, v_position, 1);
        ELSE
            v_result := v_result || v_char;
        END IF;
    END LOOP;

    RETURN v_result;
END custom_lowercase;
/

CREATE OR REPLACE PROCEDURE register_notification (
    p_job_id  IN notifications.job_id%TYPE,

    p_subject IN notifications.subject%TYPE,
    p_body    IN notifications.body%TYPE
)
IS
BEGIN
    INSERT INTO notifications (
        job_id,
        subject,
        body
    )
    VALUES (
        p_job_id,
        p_subject,
        p_body
    );

END register_notification;
/

CREATE OR REPLACE PROCEDURE normalize_description (
    p_description IN OUT vacant_jobs.description%TYPE
)
IS
BEGIN
    p_description := custom_lowercase(p_description);
END normalize_description;
/

CREATE OR REPLACE PROCEDURE expire_vacancies
IS
BEGIN
    UPDATE vacant_jobs
    SET is_active = 'N'
    WHERE is_active = 'Y'
      AND expires_at <= SYSTIMESTAMP;
END expire_vacancies;
/

CREATE OR REPLACE PROCEDURE purge_archived_vacancies
IS
BEGIN
    DELETE FROM vacant_jobs v
    WHERE v.is_active = 'N'
      AND EXISTS (
          SELECT 1
          FROM jobs_posting_archive a
          WHERE a.job_id = v.job_id
      );
END purge_archived_vacancies;
/

CREATE OR REPLACE PROCEDURE archive_inactive_vacancies
IS
BEGIN
    INSERT INTO jobs_posting_archive (
        job_id,
        job_title,
        description,
        posted_at,
        expires_at
    )
    SELECT v.job_id,
           v.job_title,
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

    purge_archived_vacancies;
END archive_inactive_vacancies;

/

CREATE OR REPLACE PROCEDURE normalize_all_descriptions
IS
    v_description vacant_jobs.description%TYPE;
BEGIN
    FOR job_record IN (
        SELECT job_id,
               description
        FROM vacant_jobs
        WHERE is_active = 'Y'
    )
    LOOP
        v_description := job_record.description;
        normalize_description(v_description);

        IF v_description <> job_record.description THEN
            UPDATE vacant_jobs
            SET description = v_description
            WHERE job_id = job_record.job_id;
        END IF;
    END LOOP;
END normalize_all_descriptions;
/

CREATE OR REPLACE PROCEDURE create_daily_application_digest
IS
    v_subject       notifications.subject%TYPE;
    v_body          notifications.body%TYPE;
    v_digest_exists NUMBER;
    v_has_results   BOOLEAN := FALSE;
BEGIN
    v_subject := 'Daily application digest - '
                 || TO_CHAR(SYSDATE, 'DD-MM-YYYY');

    SELECT COUNT(*)
    INTO v_digest_exists
    FROM notifications
    WHERE subject = v_subject;

    IF v_digest_exists > 0 THEN
        RETURN;
    END IF;

    v_body := 'Daily application digest' || CHR(10);

    FOR application_record IN (
        SELECT v.job_id,
               v.description,
               COUNT(a.application_id) AS application_count
        FROM vacant_jobs v
        JOIN job_applications a
          ON a.job_id = v.job_id
        GROUP BY v.job_id,
                 v.description
        ORDER BY v.job_id
    )
    LOOP
        v_has_results := TRUE;

        v_body := v_body
                  || 'Job ID: ' || application_record.job_id
                  || ' | Description: ' || application_record.description
                  || ' | Applications: '
                  || application_record.application_count
                  || CHR(10);
    END LOOP;

    IF v_has_results THEN
        register_notification(
            p_job_id  => NULL,
            p_subject => v_subject,
            p_body    => v_body
        );
    END IF;
END create_daily_application_digest;
/

CREATE OR REPLACE PROCEDURE get_pending_notifications (
    p_notifications OUT SYS_REFCURSOR
)
IS
BEGIN
    OPEN p_notifications FOR
        SELECT notification_id,
               subject,
               body,
               created_at
        FROM notifications
        WHERE sent_at IS NULL
        ORDER BY notification_id;
END get_pending_notifications;
/

CREATE OR REPLACE PROCEDURE mark_notification_sent (
    p_notification_id IN notifications.notification_id%TYPE
)
IS
BEGIN
    UPDATE notifications
    SET sent_at = SYSTIMESTAMP
    WHERE notification_id = p_notification_id
      AND sent_at IS NULL;
END mark_notification_sent;
/
