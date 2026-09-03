CREATE OR REPLACE PROCEDURE register_notification (
    p_notification_type IN notifications.notification_type%TYPE,
    p_subject           IN notifications.subject%TYPE,
    p_body              IN notifications.body%TYPE,
    p_dedup_key         IN notifications.dedup_key%TYPE DEFAULT NULL
)
AS
    v_existing_key_count PLS_INTEGER;
BEGIN
    INSERT INTO notifications (
        notification_type,
        subject,
        body,
        status,
        dedup_key
    )
    VALUES (
        p_notification_type,
        p_subject,
        p_body,
        'PENDING',
        p_dedup_key
    );
EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
        -- A non-null deduplication key makes repeated scheduler work safe.
        SELECT COUNT(*)
        INTO v_existing_key_count
        FROM notifications
        WHERE dedup_key = p_dedup_key;

        IF p_dedup_key IS NULL OR v_existing_key_count = 0 THEN
            RAISE;
        END IF;
END register_notification;
/

SHOW ERRORS PROCEDURE register_notification;

CREATE OR REPLACE PROCEDURE normalize_job_description (
    p_description IN OUT VARCHAR2
)
AS
BEGIN
    p_description := fn_custom_lowercase(p_description);
END normalize_job_description;
/

SHOW ERRORS PROCEDURE normalize_job_description;

PROMPT Event procedures created successfully.
