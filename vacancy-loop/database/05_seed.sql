
INSERT INTO vacant_jobs (title, description, expires_at) VALUES ('Oracle Developer', 'WE NEED AN ORACLE DEVELOPER', SYSTIMESTAMP + INTERVAL '2' MINUTE);
INSERT INTO vacant_jobs (title, description, expires_at) VALUES ('Data Analyst', 'LOOKING FOR A DATA ANALYST', SYSTIMESTAMP + INTERVAL '1' DAY);
INSERT INTO vacant_jobs (title, description, expires_at) VALUES ('Backend Developer', 'WE NEED A BACKEND DEVELOPER', SYSTIMESTAMP + INTERVAL '1' DAY);
COMMIT;



INSERT INTO job_applications (job_id, candidate_name, candidate_email) VALUES (3, 'Mariam', 'mariam@email.com');
INSERT INTO job_applications (job_id, candidate_name, candidate_email) VALUES (3, 'Noor', 'noor@email.com');
INSERT INTO job_applications (job_id, candidate_name, candidate_email) VALUES (3, 'Fatma', 'fatma@email.com');
COMMIT;