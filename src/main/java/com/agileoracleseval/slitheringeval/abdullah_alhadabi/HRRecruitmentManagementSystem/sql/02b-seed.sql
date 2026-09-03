SET SERVEROUTPUT ON

DECLARE
    v_java_job_id NUMBER;
    v_db_job_id   NUMBER;
    v_qa_job_id   NUMBER;
BEGIN
    INSERT INTO vacant_jobs (
        job_title,
        description,
        expires_at
    )
    VALUES (
        'Senior Java Developer',
        'SENIOR JAVA DEVELOPER WITH SPRING BOOT EXPERIENCE',
        SYSTIMESTAMP + INTERVAL '2' HOUR
    )
    RETURNING job_id INTO v_java_job_id;

    INSERT INTO vacant_jobs (
        job_title,
        description,
        expires_at
    )
    VALUES (
        'Oracle Database Engineer',
        'ORACLE DATABASE ENGINEER - ABOUT TO EXPIRE',
        SYSTIMESTAMP + INTERVAL '3' MINUTE
    )
    RETURNING job_id INTO v_db_job_id;

    INSERT INTO vacant_jobs (
        job_title,
        description,
        expires_at
    )
    VALUES (
        'QA Automation Engineer',
        'QA AUTOMATION ENGINEER WITH JAVA AND SELENIUM',
        SYSTIMESTAMP + INTERVAL '1' DAY
    )
    RETURNING job_id INTO v_qa_job_id;

    INSERT INTO job_applications (
        job_id,
        applicant_name,
        applicant_email
    )
    VALUES (
        v_java_job_id,
        'Candidate One',
        'candidate.one@example.com'
    );

    INSERT INTO job_applications (
        job_id,
        applicant_name,
        applicant_email
    )
    VALUES (
        v_java_job_id,
        'Candidate Two',
        'candidate.two@example.com'
    );

    INSERT INTO job_applications (
        job_id,
        applicant_name,
        applicant_email
    )
    VALUES (
        v_java_job_id,
        'Candidate Three',
        'candidate.three@example.com'
    );

    COMMIT;

    DBMS_OUTPUT.PUT_LINE(
        'Seeded jobs: ' ||
        v_java_job_id || ', ' ||
        v_db_job_id || ', ' ||
        v_qa_job_id
    );

    DBMS_OUTPUT.PUT_LINE(
        'Seeded three applications for job ' || v_java_job_id
    );
END;
/

SELECT job_id,
       job_title,
       description,
       active_flag,
       expires_at
FROM vacant_jobs
ORDER BY job_id;

SELECT job_id, COUNT(*) AS application_count
FROM job_applications
GROUP BY job_id
ORDER BY job_id;

SELECT notification_type,
       subject,
       status,
       dedup_key
FROM notifications
ORDER BY notification_id;
