-- Creates triggers for vacancy notifications,
-- job application notifications, and description normalization.
------------------------------------------------------------
-- VACANCY POSTED TRIGGER--
-- Register a notification automatically after a new vacancy is posted

CREATE OR REPLACE TRIGGER TRG_VACANCY_POSTED
AFTER INSERT ON vacant_jobs
FOR EACH ROW
BEGIN

    REGISTER_NOTIFICATION(
        'New Vacancy: ' || :NEW.title,
        'A new vacancy has been posted for ' || :NEW.title ||
        ' in ' || NVL(:NEW.location, 'Not specified') || '.',
        'NEW_VACANCY',
        :NEW.job_id,
        'TRIGGER'
    );

END TRG_VACANCY_POSTED;
/

------------------------------------------------------------
-- VERIFY VACANCY POSTED TRIGGER

SELECT object_name, object_type, status FROM user_objects
WHERE object_name = 'TRG_VACANCY_POSTED';

------------------------------------------------------------
-- JOB APPLICATION CREATED TRIGGER--
-- Register a notification automatically after a new job application is submitted

CREATE OR REPLACE TRIGGER TRG_JOB_APPLICATION_CREATED
AFTER INSERT ON job_applications
FOR EACH ROW
BEGIN

    REGISTER_NOTIFICATION(
        'New Job Application',
        'A new application was submitted by '
            || :NEW.applicant_name
            || ' for Job ID '
            || :NEW.job_id
            || '.',
        'NEW_APPLICATION',
        :NEW.job_id,
        'TRIGGER'
    );

END TRG_JOB_APPLICATION_CREATED;
/

------------------------------------------------------------
-- VERIFY JOB APPLICATION TRIGGER

SELECT object_name, object_type, status FROM user_objects
WHERE object_name = 'TRG_JOB_APPLICATION_CREATED';

------------------------------------------------------------
-- VACANCY DESCRIPTION LOWERCASE TRIGGER--
-- Convert updated vacancy descriptions to lowercase before saving

CREATE OR REPLACE TRIGGER TRG_VACANCY_DESCRIPTION_LOWER
BEFORE UPDATE OF description ON vacant_jobs
FOR EACH ROW
BEGIN

    :NEW.description := F_TO_LOWER(:NEW.description);

END TRG_VACANCY_DESCRIPTION_LOWER;
/

------------------------------------------------------------
-- VERIFY DESCRIPTION TRIGGER

SELECT object_name,
       object_type,
       status
FROM user_objects
WHERE object_name = 'TRG_VACANCY_DESCRIPTION_LOWER';

------------------------------------------------------------
-- TEST AUTOMATIC LOWERCASE CONVERSION

UPDATE vacant_jobs
SET description = 'JAVA SPRING BOOT REST API DEVELOPER'
WHERE job_id = 1;

COMMIT;

------------------------------------------------------------
-- VERIFY LOWERCASE RESULT

SELECT job_id, title, description FROM vacant_jobs
WHERE job_id = 1;

------------------------------------------------------------
-- VERIFY AUTOMATIC NOTIFICATION CREATION

SELECT notification_id,  subject, type, related_job_id, status, created_at FROM notifications
ORDER BY notification_id;