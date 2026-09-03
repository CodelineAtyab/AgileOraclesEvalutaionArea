-- ========================= 07_notification_cursor.sql =========================
-- ===== Returns pending notifications to the Spring Boot consumer =====

CREATE OR REPLACE PROCEDURE get_pending_notifications (p_notifications OUT SYS_REFCURSOR)
AS
BEGIN
    -- Open a result set containing all pending notifications
    OPEN p_notifications FOR
        SELECT notification_id, related_job_id, subject, body, created_at, status, sent_at
        FROM notifications
        WHERE status = 'PENDING'
        ORDER BY notification_id;
END;
/
------------------- Test -------------------
VARIABLE notif_cursor REFCURSOR;
BEGIN
    get_pending_notifications(:notif_cursor);
END;
/
PRINT notif_cursor;

SELECT object_name, object_type, status
FROM user_objects
WHERE object_name = 'GET_PENDING_NOTIFICATIONS';

