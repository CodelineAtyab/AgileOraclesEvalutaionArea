-- Creates a reusable procedure for registering notifications.
------------------------------------------------------------

CREATE OR REPLACE PROCEDURE REGISTER_NOTIFICATION (
    P_SUBJECT        IN VARCHAR2,
    P_BODY           IN CLOB,
    P_TYPE           IN VARCHAR2,
    P_RELATED_JOB_ID IN NUMBER DEFAULT NULL,
    P_CREATED_BY     IN VARCHAR2 DEFAULT NULL
)
AS
BEGIN

    INSERT INTO notifications (
        subject,
        body,
        type,
        related_job_id,
        created_by
    )
    VALUES (
        P_SUBJECT,
        P_BODY,
        P_TYPE,
        P_RELATED_JOB_ID,
        P_CREATED_BY
    );

END REGISTER_NOTIFICATION;
/

------------------------------------------------------------
-- VERIFY PROCEDURE CREATION

SELECT object_name,
       object_type,
       status
FROM user_objects
WHERE object_name = 'REGISTER_NOTIFICATION';