CREATE OR REPLACE PROCEDURE create_daily_application_digest
AS
    v_body              CLOB;
    v_line              VARCHAR2(4000);
    v_application_count PLS_INTEGER;
    v_posting_count     PLS_INTEGER := 0;
BEGIN
    DBMS_LOB.CREATETEMPORARY(v_body, TRUE);

    FOR job_record IN (
        SELECT job_id, job_title, description
        FROM vacant_jobs
        WHERE active_flag = 'Y'
        ORDER BY job_id
    ) LOOP
        v_application_count := 0;

        FOR application_record IN (
            SELECT application_id
            FROM job_applications
            WHERE job_id = job_record.job_id
            ORDER BY application_id
        ) LOOP
            v_application_count := v_application_count + 1;
        END LOOP;

        v_line :=
            'Job ID: ' || TO_CHAR(job_record.job_id) ||
            ' | Title: ' || job_record.job_title ||
            ' | Description: ' || job_record.description ||
            ' | Applications: ' || TO_CHAR(v_application_count) ||
            CHR(10);

        DBMS_LOB.WRITEAPPEND(
            v_body,
            LENGTH(v_line),
            v_line
        );

        v_posting_count := v_posting_count + 1;
    END LOOP;

    IF v_posting_count > 0 THEN
        register_notification(
            p_notification_type => 'DAILY_DIGEST',
            p_subject           =>
                'Daily application digest - ' ||
                TO_CHAR(SYSDATE, 'YYYY-MM-DD'),
            p_body              => v_body,
            p_dedup_key         =>
                'DAILY_DIGEST:' || TO_CHAR(SYSDATE, 'YYYYMMDD')
        );
    END IF;

    DBMS_LOB.FREETEMPORARY(v_body);

    DBMS_OUTPUT.PUT_LINE(
        v_posting_count || ' posting(s) included in the daily digest.'
    );
END create_daily_application_digest;
/

SHOW ERRORS PROCEDURE create_daily_application_digest;

CREATE OR REPLACE PROCEDURE get_pending_notifications (
    p_cursor OUT SYS_REFCURSOR
)
AS
BEGIN
    OPEN p_cursor FOR
        SELECT notification_id,
               notification_type,
               subject,
               body,
               created_at,
               attempt_count
        FROM notifications
        WHERE status = 'PENDING'
        ORDER BY created_at, notification_id;
END get_pending_notifications;
/

SHOW ERRORS PROCEDURE get_pending_notifications;

CREATE OR REPLACE PROCEDURE mark_notification_sent (
    p_notification_id IN notifications.notification_id%TYPE
)
AS
BEGIN
    UPDATE notifications
    SET status = 'SENT',
        sent_at = SYSTIMESTAMP,
        attempt_count = attempt_count + 1
    WHERE notification_id = p_notification_id
      AND status = 'PENDING';

    IF SQL%ROWCOUNT = 0 THEN
        RAISE_APPLICATION_ERROR(
            -20011,
            'Notification is not pending or does not exist.'
        );
    END IF;
END mark_notification_sent;
/

SHOW ERRORS PROCEDURE mark_notification_sent;

CREATE OR REPLACE PROCEDURE record_notification_failure (
    p_notification_id IN notifications.notification_id%TYPE
)
AS
BEGIN
    UPDATE notifications
    SET attempt_count = attempt_count + 1
    WHERE notification_id = p_notification_id
      AND status = 'PENDING';

    IF SQL%ROWCOUNT = 0 THEN
        RAISE_APPLICATION_ERROR(
            -20012,
            'Notification is not pending or does not exist.'
        );
    END IF;
END record_notification_failure;
/

SHOW ERRORS PROCEDURE record_notification_failure;

PROMPT Digest and handover procedures created successfully.
