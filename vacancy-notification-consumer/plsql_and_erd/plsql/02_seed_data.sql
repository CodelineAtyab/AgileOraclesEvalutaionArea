-- ========================= 02_seed_data.sql =========================
-- ============ Insert sample data for testing the system ============

BEGIN
    ---------------------- VACANT JOBS ----------------------
    -- Active job that expires after 3 days
    INSERT INTO vacant_jobs (title, description, expires_at, active)
    VALUES ('Java Developer', 'BUILD AND MAINTAIN JAVA APPLICATIONS', SYSTIMESTAMP + NUMTODSINTERVAL(3, 'DAY'), 'Y');

    -- Active job that expires after 5 minutes
    -- This job can be used to test the expiry and archive process
    INSERT INTO vacant_jobs (title, description, expires_at, active)
    VALUES ('Database Developer', 'WORK WITH ORACLE DATABASE AND PL SQL', SYSTIMESTAMP + NUMTODSINTERVAL(5, 'MINUTE'), 'Y');

    -- Active job that expires after 2 days
    INSERT INTO vacant_jobs (title, description, expires_at, active)
    VALUES ( 'Backend Engineer', 'DEVELOP REST APIS USING SPRING BOOT', SYSTIMESTAMP + NUMTODSINTERVAL(2, 'DAY'), 'Y');


    ---------------------- JOB APPLICATIONS ----------------------
    -- First candidate applies for Job 1
    INSERT INTO job_applications (job_id, applicant_name, applicant_email)
    VALUES (1, 'Al-Jolanda', 'aljolanda@example.com');

    -- Second candidate applies for Job 1
    INSERT INTO job_applications (job_id, applicant_name, applicant_email)
    VALUES ( 1, 'Sara Mohammed', 'sara@example.com');

    -- Third candidate applies for Job 1
    -- Job 1 now has three applications for testing the daily digest
    INSERT INTO job_applications ( job_id, applicant_name, applicant_email)
    VALUES (1, 'Khalid Salim', 'khalid@example.com');

    -- Candidate applies for Job 2
    INSERT INTO job_applications (job_id, applicant_name, applicant_email)
    VALUES (2, 'Maha Said', 'maha@example.com');


    ---------------------- SAMPLE NOTIFICATION ----------------------
    -- Pending notification for testing the Spring Boot consumer
    INSERT INTO notifications (related_job_id, subject, body, status)
    VALUES ( 1, 'New vacancy posted', 'A new vacancy for Java Developer has been posted.', 'PENDING');

    -- Save all inserted data
    COMMIT;
END;
/

--------------------------------------------------------------------------------
SELECT * FROM vacant_jobs; -- Display all vacant jobs
SELECT * FROM job_applications; -- Display all job applications
SELECT * FROM notifications; -- Display all notifications
SELECT * FROM jobs_posting_archive; -- Archive should still be empty at this stage