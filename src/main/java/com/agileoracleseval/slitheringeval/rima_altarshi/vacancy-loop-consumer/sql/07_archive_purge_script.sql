-- Archives inactive vacancies before removing them
-- from the live vacancy table.
------------------------------------------------------------
-- FOREIGN KEY DELETE BEHAVIOR--
-- Remove applications automatically when their vacancy is purged

ALTER TABLE job_applications
DROP CONSTRAINT fk_application_job;

ALTER TABLE job_applications
ADD CONSTRAINT fk_application_job
FOREIGN KEY (job_id)
REFERENCES vacant_jobs(job_id)
ON DELETE CASCADE;

-- Keep notification history after a vacancy is purged
-- by clearing only the related job reference

ALTER TABLE notifications
DROP CONSTRAINT fk_notification_job;

ALTER TABLE notifications
ADD CONSTRAINT fk_notification_job
FOREIGN KEY (related_job_id)
REFERENCES vacant_jobs(job_id)
ON DELETE SET NULL;

------------------------------------------------------------
-- ARCHIVE INACTIVE JOBS PROCEDUR--
-- Copy inactive vacancies to the archive only once

CREATE OR REPLACE PROCEDURE ARCHIVE_INACTIVE_JOBS
AS
BEGIN

    INSERT INTO jobs_posting_archive (
        job_id,
        posted_by,
        title,
        description,
        location,
        job_type,
        posted_at,
        expires_at,
        updated_at
    )
    SELECT
        V.job_id,
        V.posted_by,
        V.title,
        V.description,
        V.location,
        V.job_type,
        V.posted_at,
        V.expires_at,
        V.updated_at
    FROM vacant_jobs V
    WHERE V.is_active = 'N'
      AND NOT EXISTS (
          SELECT 1
          FROM jobs_posting_archive A
          WHERE A.job_id = V.job_id
      );

    COMMIT;

END ARCHIVE_INACTIVE_JOBS;
/

------------------------------------------------------------
-- PURGE INACTIVE JOBS PROCEDUR--
-- Delete only inactive vacancies that already exist in the archive

CREATE OR REPLACE PROCEDURE PURGE_INACTIVE_JOBS
AS
BEGIN

    DELETE FROM vacant_jobs V
    WHERE V.is_active = 'N'
      AND EXISTS (
          SELECT 1
          FROM jobs_posting_archive A
          WHERE A.job_id = V.job_id
      );

    COMMIT;

END PURGE_INACTIVE_JOBS;
/

------------------------------------------------------------
-- VERIFY ARCHIVE AND PURGE PROCEDURES

SELECT object_name, object_type, status FROM user_objects
WHERE object_name IN (
    'ARCHIVE_INACTIVE_JOBS',
    'PURGE_INACTIVE_JOBS'
)
ORDER BY object_name;

------------------------------------------------------------
-- ARCHIVE SCHEDULER JOB--
-- Archive inactive jobs first, then purge only the safely archived rows

BEGIN
    DBMS_SCHEDULER.CREATE_JOB(
        job_name        => 'JOB_ARCHIVER',
        job_type        => 'PLSQL_BLOCK',
        job_action      => '
        BEGIN
            ARCHIVE_INACTIVE_JOBS;
            PURGE_INACTIVE_JOBS;
        END;',
        start_date      => SYSTIMESTAMP,
        repeat_interval => 'FREQ=MINUTELY;INTERVAL=1',
        enabled         => TRUE
    );
END;
/

------------------------------------------------------------
-- VERIFY ARCHIVE SCHEDULER

SELECT job_name, enabled, state, repeat_interval FROM user_scheduler_jobs
WHERE job_name = 'JOB_ARCHIVER';

------------------------------------------------------------
-- VERIFY ARCHIVED VACANCIES

SELECT archive_id, job_id, title, posted_by, archived_at FROM jobs_posting_archive
ORDER BY archive_id;

------------------------------------------------------------
-- VERIFY PURGED VACANCIES

SELECT job_id, title, is_active FROM vacant_jobs
ORDER BY job_id;

------------------------------------------------------------
-- VERIFY NOTIFICATION HISTORY

SELECT notification_id, subject, related_job_id, status FROM notifications
ORDER BY notification_id;