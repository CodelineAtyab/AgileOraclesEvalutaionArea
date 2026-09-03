-- PROCEDURES
-- Cursor-based procedures are separated in 04_Cursors.sql

-- Lane A - 3 - Write a new notification into the NOTIFICATIONS table
-- Receive a subject and body, then insert them into NOTIFICATIONS
CREATE OR REPLACE PROCEDURE register_notification (p_subject IN VARCHAR2, p_body IN VARCHAR2)
AS
BEGIN

    INSERT INTO notifications (subject, body)
    VALUES (p_subject, p_body);

END;
/

-- Lane B - 5 & 6 - Pass the updated description through the custom lowercase function
-- Receive a description, call the reusable function, and give the cleaned description back.
CREATE OR REPLACE PROCEDURE normalize_job_description (p_description     IN  VARCHAR2, p_clean_description OUT VARCHAR2)
AS

BEGIN

    p_clean_description := custom_lowercase(p_description);

END;
/

-- Lane C - 7 - Time Runs Out
-- a procedure checks VACANT_JOBS for rows whose expires_at has passed and changes is_active from Y to N. 
-- A Scheduler job then calls that procedure automatically every minute, so nobody needs to run it manually

-- Lane C - 7 - Mark vacancies inactive when their expiry time has passed
-- Create a procedure
CREATE OR REPLACE PROCEDURE expire_vacancies
AS

    v_expired_count NUMBER := 0;
    v_oman_time TIMESTAMP WITH TIME ZONE;

BEGIN

    v_oman_time := SYSTIMESTAMP AT TIME ZONE 'Asia/Muscat';

    UPDATE vacant_jobs
    SET is_active = 'N'
    WHERE is_active = 'Y'
      AND expires_at <= v_oman_time;

    v_expired_count := SQL%ROWCOUNT;

    COMMIT;

    DBMS_OUTPUT.PUT_LINE(v_expired_count || ' vacancies expired.');

END;
/

-- Lane C - 8 & 9 - Archive
-- Lane C - 8 - Copy inactive vacancies to the archive and move their application links
-- Create the archive procedure
CREATE OR REPLACE PROCEDURE archive_vacancies
AS

    v_archive_count NUMBER := 0;

BEGIN

-- COPY INACTIVE VACANCIES TO ARCHIVE
    INSERT INTO jobs_posting_archive (
        job_id,
        job_title,
        description,
        posted_at,
        expires_at
    )

    SELECT
        v.job_id,
        v.job_title,
        v.description,
        v.posted_at,
        v.expires_at
    FROM vacant_jobs v
    WHERE v.is_active = 'N'

      AND NOT EXISTS (SELECT 1 FROM jobs_posting_archive a WHERE a.job_id = v.job_id);


    v_archive_count := SQL%ROWCOUNT;


-- MOVE APPLICATION RELATIONSHIP

    UPDATE job_applications ja
    SET archived_job_id = ja.job_id,
        job_id = NULL
    WHERE ja.job_id IS NOT NULL
      AND EXISTS (SELECT 1 FROM jobs_posting_archive a WHERE a.job_id = ja.job_id);


    COMMIT;


    DBMS_OUTPUT.PUT_LINE(v_archive_count ||' vacancies archived.');

END;
/


-- Lane C - 9 - Remove inactive vacancies only after they already exist in the archive
-- Create a Procedure that remove inactive vacancies
CREATE OR REPLACE PROCEDURE purge_archived_vacancies
AS

    v_purge_count NUMBER := 0;

BEGIN

    DELETE FROM vacant_jobs v
    WHERE v.is_active = 'N'

      AND EXISTS (SELECT 1  
      FROM jobs_posting_archive a 
      WHERE a.job_id = v.job_id);

    v_purge_count := SQL%ROWCOUNT;

    COMMIT;


    DBMS_OUTPUT.PUT_LINE(v_purge_count ||' archived vacancies removed from live board.');

END;
/

-- Lane E - 14
-- Lane E - 15 - Mark a notification as SENT after Java successfully sends it to Slack
-- procedure that marks a notification as sent
CREATE OR REPLACE PROCEDURE mark_notification_sent (p_notification_id IN NUMBER)
AS
BEGIN

    UPDATE notifications
    SET status = 'SENT',
        sent_at = SYSTIMESTAMP AT TIME ZONE 'Asia/Muscat'
    WHERE notification_id = p_notification_id
      AND status = 'PENDING';

    COMMIT;

END;
/
