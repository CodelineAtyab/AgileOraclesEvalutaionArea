-- Inserts sample users, vacancies and applications
-- required to test the Vacancy Loop workflow.
------------------------------------------------------------
-- SAMPLE USERS---

-- Insert sample users for posting vacancies

INSERT INTO users (username, full_name, email)
VALUES ('hr_admin', 'HR Administrator', 'hr@vacancyloop.com');

INSERT INTO users (username, full_name, email)
VALUES ('hr_manager', 'HR Manager', 'manager@vacancyloop.com');


------------------------------------------------------------
-- SAMPLE VACANCIES ---

-- Insert an active vacancy with uppercase text
-- for lowercase-function testing

INSERT INTO vacant_jobs ( posted_by, title, description, location, job_type, expires_at )
SELECT
    user_id,
    'Java Developer',
    'JAVA BACKEND DEVELOPER WITH SPRING BOOT EXPERIENCE',
    'Muscat',
    'FULL_TIME',
    SYSTIMESTAMP + INTERVAL '1' DAY
FROM users
WHERE username = 'hr_admin';


-- Insert a vacancy that expires soon
-- for scheduler testing

INSERT INTO vacant_jobs ( posted_by, title, description, location, job_type, expires_at )
SELECT
    user_id,
    'Oracle Developer',
    'ORACLE SQL AND PL/SQL DEVELOPER',
    'Muscat',
    'FULL_TIME',
    SYSTIMESTAMP + INTERVAL '2' MINUTE
FROM users
WHERE username = 'hr_manager';

------------------------------------------------------------
-- SAMPLE JOB APPLICATIONS ---

-- Insert three applications for the same vacancy
-- to verify the daily digest application count

INSERT INTO job_applications ( job_id, applicant_name, applicant_email )
SELECT
    job_id,
    'Ahmed Ali',
    'ahmed@gmail.com'
FROM vacant_jobs
WHERE title = 'Java Developer';


INSERT INTO job_applications ( job_id, applicant_name, applicant_email )
SELECT
    job_id,
    'Sara Mohammed',
    'sara@gmail.com'
FROM vacant_jobs
WHERE title = 'Java Developer';


INSERT INTO job_applications ( job_id, applicant_name, applicant_email )
SELECT
    job_id,
    'Noor Hassan',
    'noor@gmail.com'
FROM vacant_jobs
WHERE title = 'Java Developer';


------------------------------------------------------------
-- SAVE SAMPLE DATA
COMMIT;

------------------------------------------------------------
-- VERIFY INSERTED USERS

SELECT * FROM users
ORDER BY user_id;

------------------------------------------------------------
-- VERIFY INSERTED VACANCIES

SELECT job_id, posted_by, title, description, is_active, posted_at, expires_at FROM vacant_jobs
ORDER BY job_id;

------------------------------------------------------------
-- VERIFY INSERTED JOB APPLICATIONS

SELECT application_id, job_id, applicant_name, applicant_email, status, applied_at FROM job_applications
ORDER BY application_id;