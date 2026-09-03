CREATE OR REPLACE PROCEDURE expire_vacant_jobs (
    p_now IN TIMESTAMP DEFAULT SYSTIMESTAMP
)
AS
    v_expired_count PLS_INTEGER;
BEGIN
    UPDATE vacant_jobs
    SET active_flag = 'N'
    WHERE active_flag = 'Y'
      AND expires_at <= p_now;

    v_expired_count := SQL%ROWCOUNT;

    DBMS_OUTPUT.PUT_LINE(
        v_expired_count || ' vacancy/vacancies expired.'
    );
END expire_vacant_jobs;
/

SHOW ERRORS PROCEDURE expire_vacant_jobs;

CREATE OR REPLACE PROCEDURE archive_inactive_jobs
AS
    v_job_count         PLS_INTEGER;
    v_application_count PLS_INTEGER;
BEGIN
    INSERT INTO jobs_posting_archive (
        job_id,
        job_title,
        description,
        expires_at,
        created_at
    )
    SELECT
        v.job_id,
        v.job_title,
        fn_custom_lowercase(v.description),
        v.expires_at,
        v.created_at
    FROM vacant_jobs v
    WHERE v.active_flag = 'N'
      AND NOT EXISTS (
          SELECT 1
          FROM jobs_posting_archive a
          WHERE a.job_id = v.job_id
      );

    v_job_count := SQL%ROWCOUNT;

    INSERT INTO job_applications_archive (
        application_id,
        job_id,
        applicant_name,
        applicant_email,
        applied_at
    )
    SELECT
        app.application_id,
        app.job_id,
        app.applicant_name,
        app.applicant_email,
        app.applied_at
    FROM job_applications app
    WHERE EXISTS (
              SELECT 1
              FROM vacant_jobs v
              WHERE v.job_id = app.job_id
                AND v.active_flag = 'N'
          )
      AND NOT EXISTS (
              SELECT 1
              FROM job_applications_archive archived_app
              WHERE archived_app.application_id = app.application_id
          );

    v_application_count := SQL%ROWCOUNT;

    DBMS_OUTPUT.PUT_LINE(
        v_job_count || ' vacancy/vacancies archived.'
    );

    DBMS_OUTPUT.PUT_LINE(
        v_application_count || ' application(s) archived.'
    );
END archive_inactive_jobs;
/

SHOW ERRORS PROCEDURE archive_inactive_jobs;

CREATE OR REPLACE PROCEDURE purge_archived_jobs
AS
    v_application_count PLS_INTEGER;
    v_job_count         PLS_INTEGER;
BEGIN
    DELETE FROM job_applications app
    WHERE EXISTS (
        SELECT 1
        FROM vacant_jobs v
        JOIN jobs_posting_archive a
          ON a.job_id = v.job_id
        WHERE v.job_id = app.job_id
          AND v.active_flag = 'N'
    );

    v_application_count := SQL%ROWCOUNT;

    DELETE FROM vacant_jobs v
    WHERE v.active_flag = 'N'
      AND EXISTS (
          SELECT 1
          FROM jobs_posting_archive a
          WHERE a.job_id = v.job_id
      )
      AND NOT EXISTS (
          SELECT 1
          FROM job_applications app
          WHERE app.job_id = v.job_id
      );

    v_job_count := SQL%ROWCOUNT;

    DBMS_OUTPUT.PUT_LINE(
        v_application_count || ' live application(s) purged.'
    );

    DBMS_OUTPUT.PUT_LINE(
        v_job_count || ' archived vacancy/vacancies purged from the live board.'
    );
END purge_archived_jobs;
/

SHOW ERRORS PROCEDURE purge_archived_jobs;

CREATE OR REPLACE PROCEDURE archive_and_purge_jobs
AS
BEGIN
    archive_inactive_jobs;
    purge_archived_jobs;
END archive_and_purge_jobs;
/

SHOW ERRORS PROCEDURE archive_and_purge_jobs;

CREATE OR REPLACE PROCEDURE sweep_job_descriptions
AS
    v_clean_description VARCHAR2(2000);
    v_changed_count     PLS_INTEGER := 0;
BEGIN
    FOR job_record IN (
        SELECT job_id, description
        FROM vacant_jobs
        WHERE active_flag = 'Y'
    ) LOOP
        v_clean_description := fn_custom_lowercase(job_record.description);

        IF v_clean_description <> job_record.description THEN
            UPDATE vacant_jobs
            SET description = v_clean_description
            WHERE job_id = job_record.job_id;

            v_changed_count := v_changed_count + SQL%ROWCOUNT;
        END IF;
    END LOOP;

    DBMS_OUTPUT.PUT_LINE(
        v_changed_count || ' vacancy description(s) normalized.'
    );
END sweep_job_descriptions;
/

SHOW ERRORS PROCEDURE sweep_job_descriptions;

PROMPT Lifecycle procedures created successfully.
