-- Returns pending notifications to the external Spring Boot
-- service using an explicit SYS_REFCURSOR.
------------------------------------------------------------
-- GET PENDING NOTIFICATIONS PROCEDURE

CREATE OR REPLACE PROCEDURE GET_PENDING_NOTIFICATIONS ( P_CURSOR OUT SYS_REFCURSOR )
AS
BEGIN

    OPEN P_CURSOR FOR
        SELECT notification_id, subject, body, type, related_job_id, created_at, attempt_count FROM notifications
        WHERE status = 'PENDING'
        ORDER BY created_at;

END GET_PENDING_NOTIFICATIONS;
/

------------------------------------------------------------
-- VERIFY PROCEDURE CREATION

SELECT object_name, object_type, status FROM user_objects
WHERE object_name = 'GET_PENDING_NOTIFICATIONS';

------------------------------------------------------------
-- TEST THE EXPLICIT CURSOR

SET SERVEROUTPUT ON;

DECLARE
    V_CURSOR          SYS_REFCURSOR;

    V_NOTIFICATION_ID notifications.notification_id%TYPE;
    V_SUBJECT         notifications.subject%TYPE;
    V_BODY            notifications.body%TYPE;
    V_TYPE            notifications.type%TYPE;
    V_RELATED_JOB_ID  notifications.related_job_id%TYPE;
    V_CREATED_AT      notifications.created_at%TYPE;
    V_ATTEMPT_COUNT   notifications.attempt_count%TYPE;

BEGIN

    GET_PENDING_NOTIFICATIONS(V_CURSOR);

    LOOP

        FETCH V_CURSOR INTO
            V_NOTIFICATION_ID,
            V_SUBJECT,
            V_BODY,
            V_TYPE,
            V_RELATED_JOB_ID,
            V_CREATED_AT,
            V_ATTEMPT_COUNT;

        EXIT WHEN V_CURSOR%NOTFOUND;

        DBMS_OUTPUT.PUT_LINE( 'ID: ' || V_NOTIFICATION_ID || ' | Subject: ' || V_SUBJECT || ' | Type: ' || V_TYPE );

    END LOOP;

    CLOSE V_CURSOR;

END;
/