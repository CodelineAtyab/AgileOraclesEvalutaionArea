-- Seed data

DELETE FROM job_applications;
DELETE FROM notifications;
DELETE FROM jobs_posting_archive;
DELETE FROM vacant_jobs;
COMMIT;

INSERT INTO vacant_jobs (title, description, expires_at, active_flag)
VALUES ('Junior Java Developer',
        'Need someone who knows basic spring and sql',
        SYSTIMESTAMP + INTERVAL '2' DAY, 'Y');

INSERT INTO vacant_jobs (title, description, expires_at, active_flag)
VALUES ('HR Assistant',
        'HELP WITH INTERVIEWS AND PAPERWORK',
        SYSTIMESTAMP + INTERVAL '1' DAY, 'Y');

INSERT INTO vacant_jobs (title, description, expires_at, active_flag)
VALUES ('Temp Reception Cover',
        'SHORT COVER ROLE FOR FRONT DESK',
        SYSTIMESTAMP + INTERVAL '2' MINUTE, 'Y');

COMMIT;

INSERT INTO job_applications (job_id, applicant_name, applicant_email)
SELECT job_id, 'Sara Al Balushi', 'sara@example.com'
  FROM vacant_jobs WHERE title = 'Junior Java Developer';

INSERT INTO job_applications (job_id, applicant_name, applicant_email)
SELECT job_id, 'Omar Al Habsi', 'omar@example.com'
  FROM vacant_jobs WHERE title = 'Junior Java Developer';

INSERT INTO job_applications (job_id, applicant_name, applicant_email)
SELECT job_id, 'Fatma Al Zadjali', 'fatma@example.com'
  FROM vacant_jobs WHERE title = 'Junior Java Developer';

INSERT INTO job_applications (job_id, applicant_name, applicant_email)
SELECT job_id, 'Yousuf Al Lawati', 'yousuf@example.com'
  FROM vacant_jobs WHERE title = 'HR Assistant';

COMMIT;
