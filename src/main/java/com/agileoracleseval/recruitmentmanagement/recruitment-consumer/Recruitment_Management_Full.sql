-- Lane A - 3 - Write a new notification into the NOTIFICATIONS table
-- Receive a subject and body, then insert them into NOTIFICATIONS
CREATE OR REPLACE PROCEDURE register_notification (p_subject IN VARCHAR2, p_body IN VARCHAR2)
AS
BEGIN

    INSERT INTO notifications (subject, body)
    VALUES (p_subject, p_body);

END;
/

-- Lane A - 2 - Detect a new vacancy insert and automatically create its notification
-- call the procedure automatically whenever HR inserts a vacancy
CREATE OR REPLACE TRIGGER trg_vacant_job_posted

AFTER INSERT ON vacant_jobs

FOR EACH ROW

BEGIN

    register_notification('New Vacancy Posted', 'Job ID ' || :NEW.job_id ||' - ' || :NEW.job_title ||' has been posted.');

END;
/

-- Lane A - 1 - HR posts a vacancy manually; the database handles the next steps
-- Testing: Import Vacancy
INSERT INTO vacant_jobs (job_title, description, expires_at)

VALUES ('Senior Java Developer','SPRING BOOT DEVELOPER REQUIRED', (SYSTIMESTAMP AT TIME ZONE 'Asia/Muscat') + NUMTODSINTERVAL(10, 'MINUTE'));

COMMIT;


-- check the Notification
SELECT notification_id,
       subject,
       body,
       status,
       created_at
FROM notifications
ORDER BY notification_id;



-- FUNCTION= transform the text and RETURN a value
-- PROCEDURE= organise the work and call the function
-- TRIGGER= automatically start the work when description is updated

-- Lane B — The Edit
-- first create a custom function called custom_lowercase that converts uppercase letters to lowercase without using Oracle’s built-in LOWER() function. 
-- Then create the normalize_job_description procedure, which receives a description and passes it through that function. 
-- After that, create a BEFORE UPDATE OF description trigger on vacant_jobs 
-- so that whenever HR updates a job description, the trigger automatically sends the new text to the procedure and replaces :NEW.description 
-- with the cleaned lowercase version before the row is saved

-- Lane B - 6 - Convert a job description to lowercase using our own reusable function
-- Create Function that Convert uppercase to lowercase
CREATE OR REPLACE FUNCTION custom_lowercase (
    p_text IN VARCHAR2
)
RETURN VARCHAR2
AS

    v_result    VARCHAR2(4000) := '';
    v_character VARCHAR2(1);

BEGIN

    FOR i IN 1 .. LENGTH(p_text)
    LOOP

        v_character := SUBSTR(p_text, i, 1);


        IF ASCII(v_character)
           BETWEEN ASCII('A') AND ASCII('Z')
        THEN

            v_result := v_result || CHR(ASCII(v_character) + 32);

        ELSE

            v_result := v_result || v_character;

        END IF;

    END LOOP;

    RETURN v_result;

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

-- Lane B - 5 - Detect a description update before it is saved and clean the new text
-- Create a trigger that is run when DESCRIPTION is updated
CREATE OR REPLACE TRIGGER trg_normalize_job_description

BEFORE UPDATE OF description
ON vacant_jobs

FOR EACH ROW

DECLARE

    v_clean_description VARCHAR2(1000);

BEGIN

    normalize_job_description(:NEW.description, v_clean_description);

    :NEW.description := v_clean_description;

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

-- Lane C - 7 - Run the vacancy expiry check automatically every minute
-- Create the Scheduler job that check every minute
BEGIN

    DBMS_SCHEDULER.CREATE_JOB (

        job_name => 'JOB_EXPIRE_VACANCIES',

        job_type => 'STORED_PROCEDURE',

        job_action => 'EXPIRE_VACANCIES',

        start_date => SYSTIMESTAMP AT TIME ZONE 'Asia/Muscat',

        repeat_interval => 'FREQ=MINUTELY;INTERVAL=1',

        enabled => TRUE

    );

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

-- Lane C - 10 - Scheduled description sweep using an implicit cursor, repeating 06 but in bulk
-- Lane C - 10 - Walk every live vacancy row by row and clean its description
-- Create the implicit-cursor procedure
CREATE OR REPLACE PROCEDURE sweep_job_descriptions
AS

    v_clean_description VARCHAR2(1000);
    v_update_count NUMBER := 0;

BEGIN

    FOR jobObj IN (

        SELECT job_id,
               description

        FROM vacant_jobs

        WHERE is_active = 'Y'

    )
    LOOP

        -- Use the same routine from Step 06
        normalize_job_description(jobObj.description,v_clean_description);


        -- Update only if the text actually changed
        IF jobObj.description <> v_clean_description
        THEN

            UPDATE vacant_jobs
            SET description = v_clean_description
            WHERE job_id = jobObj.job_id;


            v_update_count := v_update_count + 1;

        END IF;

    END LOOP;


    COMMIT;


    DBMS_OUTPUT.PUT_LINE(v_update_count ||' job descriptions cleaned.');

END;
/

-- Lane C - 10 - Run the description sweep automatically every 5 minutes
-- Create a Scheduler Job, assuming every 5 min
BEGIN

    DBMS_SCHEDULER.CREATE_JOB (

        job_name =>'JOB_DESCRIPTION_SWEEP',
        job_type => 'STORED_PROCEDURE',
        job_action => 'SWEEP_JOB_DESCRIPTIONS',
        start_date => SYSTIMESTAMP AT TIME ZONE 'Asia/Muscat',
        repeat_interval =>'FREQ=MINUTELY;INTERVAL=5',
        enabled =>TRUE

    );

END;
/

-- Lane D -  11 & 12: Candidates apply, then Oracle creates one daily digest notification
-- Lane D - 12 - Read applications row by row, count them per posting, and create one daily digest
-- Simpler daily digest procedure
CREATE OR REPLACE PROCEDURE create_daily_application_digest
AS
    v_posting_id          NUMBER;
    v_description         VARCHAR2(1000);
    v_current_posting_id  NUMBER := NULL;
    v_current_description VARCHAR2(1000);
    v_application_count   NUMBER := 0;
    v_digest_body         VARCHAR2(2000) := '';

BEGIN

    -- Read applications one row at a time - implicit
    FOR appObj IN (
        SELECT
            application_id,
            job_id,
            archived_job_id
        FROM job_applications
        WHERE applied_at >= (SYSTIMESTAMP AT TIME ZONE 'Asia/Muscat') - NUMTODSINTERVAL(1, 'DAY')
        ORDER BY
            job_id NULLS LAST,
            archived_job_id NULLS LAST,
            application_id
    )
    LOOP

        -- Find which posting this application belongs to (Live or Archive)
        IF appObj.job_id IS NOT NULL
        THEN
            v_posting_id := appObj.job_id;

            SELECT description
            INTO v_description
            FROM vacant_jobs
            WHERE job_id = appObj.job_id;

        ELSE

            v_posting_id := appObj.archived_job_id;

            SELECT description
            INTO v_description
            FROM jobs_posting_archive
            WHERE job_id = appObj.archived_job_id;

        END IF;

        -- First application processing 
        IF v_current_posting_id IS NULL
        THEN

            v_current_posting_id := v_posting_id;
            v_current_description := v_description;
            v_application_count := 1;

        -- Another application for the same job
        ELSIF v_current_posting_id = v_posting_id
        THEN

            v_application_count := v_application_count + 1;


        -- We reached a different posting
        ELSE

            v_digest_body :=
                v_digest_body ||
                'Job ID: ' ||
                v_current_posting_id ||
                ' | Description: ' ||
                v_current_description ||
                ' | Applications: ' ||
                v_application_count ||
                CHR(10);


            -- Start counting the new posting
            v_current_posting_id := v_posting_id;
            v_current_description := v_description;
            v_application_count := 1;

        END IF;

    END LOOP;


    -- Add the last posting after the loop finishes
    IF v_current_posting_id IS NOT NULL
    THEN

        v_digest_body :=
            v_digest_body ||
            'Job ID: ' ||
            v_current_posting_id ||
            ' | Description: ' ||
            v_current_description ||
            ' | Applications: ' ||
            v_application_count;

    END IF;


    -- Create only ONE notification
    IF v_digest_body IS NOT NULL
    THEN

        register_notification('Daily Application Digest',v_digest_body);

        DBMS_OUTPUT.PUT_LINE('Daily application digest created.');

    ELSE

        DBMS_OUTPUT.PUT_LINE('No applications found for the daily digest.');

    END IF;


    COMMIT;

END;
/

SET SERVEROUTPUT ON;

BEGIN
    create_daily_application_digest;
END;
/

-- Lane D - 12 - Run the application digest automatically once every 24 hours
-- Scheduler job for JOB_DAILY_APPLICATION_DIGEST
BEGIN

    DBMS_SCHEDULER.CREATE_JOB (

        job_name => 'JOB_DAILY_APPLICATION_DIGEST',
        job_type => 'STORED_PROCEDURE',
        job_action => 'CREATE_DAILY_APPLICATION_DIGEST',
        start_date => (SYSTIMESTAMP AT TIME ZONE 'Asia/Muscat') + NUMTODSINTERVAL(1, 'DAY'),
        repeat_interval => 'FREQ=DAILY;INTERVAL=1',
        enabled => TRUE

    );

END;
/

-- Lane E - 13: handing pending notifications to Java through an explicit
-- Create the procedure
CREATE OR REPLACE PROCEDURE get_pending_notifications (p_cursor OUT SYS_REFCURSOR)
AS

BEGIN

    OPEN p_cursor FOR
        SELECT
            notification_id,
            subject,
            body,
            status,
            created_at
        FROM notifications
        WHERE status = 'PENDING'
        ORDER BY notification_id;
END;
/



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



