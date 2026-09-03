-- Updates notification delivery results after the
-- Spring Boot consumer attempts to send messages to Slack.
------------------------------------------------------------
-- UPDATE NOTIFICATION DELIVERY PROCEDURE

CREATE OR REPLACE PROCEDURE UPDATE_NOTIFICATION_DELIVERY (
    P_NOTIFICATION_ID IN NUMBER,
    P_STATUS          IN VARCHAR2,
    P_ERROR_MESSAGE   IN VARCHAR2 DEFAULT NULL
)
AS
BEGIN

    UPDATE notifications
    SET status = P_STATUS,

        sent_at =
            CASE
                WHEN P_STATUS = 'SENT' THEN SYSTIMESTAMP
                ELSE sent_at
            END,

        attempt_count = attempt_count + 1,

        last_error =
            CASE
                WHEN P_STATUS = 'FAILED' THEN P_ERROR_MESSAGE
                ELSE NULL
            END

    WHERE notification_id = P_NOTIFICATION_ID;

    COMMIT;

END UPDATE_NOTIFICATION_DELIVERY;
/

------------------------------------------------------------
-- VERIFY PROCEDURE CREATION

SELECT object_name, object_type, status FROM user_objects
WHERE object_name = 'UPDATE_NOTIFICATION_DELIVERY';

------------------------------------------------------------
-- VERIFY NOTIFICATION DELIVERY STATUS

SELECT notification_id, subject, status, sent_at, attempt_count, last_error FROM notifications
ORDER BY notification_id;