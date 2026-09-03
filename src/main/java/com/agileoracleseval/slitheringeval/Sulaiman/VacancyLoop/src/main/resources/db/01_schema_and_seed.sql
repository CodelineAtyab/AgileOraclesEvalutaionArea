--------------------------------------------------------------------------
-- 01_schema_and_seed.sql
-- TheVacancyLoop — schema + seed data
--
-- Design notes:
--   * job_applications.digested_at tracks whether an application has been
--     counted in the daily digest, and stops the purge step from deleting
--     a vacant_jobs row that still has un-digested applications pointing
--     at it (would break the FK / silently lose data the digest hasn't
--     read yet).
--   * jobs_posting_archive has no active_flag: once a row is archived it
--     is definitionally inactive, so the column would be redundant.
--------------------------------------------------------------------------

-- Clean slate (safe to re-run during development). For a deployment into
-- a schema whose data must be preserved, comment out this block — the
-- CREATE statements below stand alone.
BEGIN
  FOR t IN (SELECT table_name FROM user_tables
            WHERE table_name IN ('JOB_APPLICATIONS','NOTIFICATION',
                                  'JOBS_POSTING_ARCHIVE','VACANT_JOBS'))
  LOOP
    EXECUTE IMMEDIATE 'DROP TABLE ' || t.table_name || ' CASCADE CONSTRAINTS';
  END LOOP;
END;
/

--------------------------------------------------------------------------
-- vacant_jobs : the live board. HR posts new vacancies here.
--------------------------------------------------------------------------
CREATE TABLE vacant_jobs (
  job_id       NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  description  VARCHAR2(4000) NOT NULL,
  posted_at    TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
  expires_at   TIMESTAMP NOT NULL,
  active_flag  CHAR(1) DEFAULT 'Y' NOT NULL
               CHECK (active_flag IN ('Y','N'))
);

--------------------------------------------------------------------------
-- jobs_posting_archive : where expired postings end up. Same shape,
-- minus active_flag.
--------------------------------------------------------------------------
CREATE TABLE jobs_posting_archive (
  job_id       NUMBER PRIMARY KEY,      -- reuses original job_id, not identity
  description  VARCHAR2(4000) NOT NULL,
  posted_at    TIMESTAMP NOT NULL,
  expires_at   TIMESTAMP NOT NULL,
  archived_at  TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL
);

--------------------------------------------------------------------------
-- notification : the outbox. Written by triggers/procedures, drained by
-- the Spring Boot consumer.
--------------------------------------------------------------------------
CREATE TABLE notification (
  notification_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  subject          VARCHAR2(200) NOT NULL,
  body             VARCHAR2(4000) NOT NULL,
  created_at       TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
  sent_flag        CHAR(1) DEFAULT 'N' NOT NULL
                   CHECK (sent_flag IN ('Y','N'))
);

--------------------------------------------------------------------------
-- job_applications : many candidates -> one vacancy.
-- ON DELETE RESTRICT (the default) forces the purge step to respect
-- un-digested applications.
--------------------------------------------------------------------------
CREATE TABLE job_applications (
  application_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  job_id         NUMBER NOT NULL
                 REFERENCES vacant_jobs (job_id),
  applied_at     TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
  digested_at    TIMESTAMP NULL
);

CREATE INDEX ix_job_applications_job_id ON job_applications (job_id);
CREATE INDEX ix_job_applications_digest ON job_applications (job_id, digested_at);

--------------------------------------------------------------------------
-- Seed data
--------------------------------------------------------------------------

-- A job that is NOT about to expire (control case — should stay untouched)
INSERT INTO vacant_jobs (description, expires_at)
VALUES ('SENIOR JAVA ENGINEER, REMOTE', SYSTIMESTAMP + INTERVAL '7' DAY);

-- A job that expired 30 seconds ago, so expiry/archive/purge are
-- observable within a minute or two of the scheduled jobs starting
INSERT INTO vacant_jobs (description, expires_at)
VALUES ('DATA ANALYST, MUSCAT OFFICE', SYSTIMESTAMP - INTERVAL '30' SECOND);

-- A job with 3 candidates already attached, to prove the digest counts
-- correctly
INSERT INTO vacant_jobs (description, expires_at)
VALUES ('PL/SQL DEVELOPER, HYBRID', SYSTIMESTAMP + INTERVAL '3' DAY);

COMMIT;

-- Applications against the third job (job_id resolved by description
-- since IDENTITY values shouldn't be hardcoded in seed scripts)
INSERT INTO job_applications (job_id)
SELECT job_id FROM vacant_jobs WHERE description = 'PL/SQL DEVELOPER, HYBRID';

INSERT INTO job_applications (job_id)
SELECT job_id FROM vacant_jobs WHERE description = 'PL/SQL DEVELOPER, HYBRID';

INSERT INTO job_applications (job_id)
SELECT job_id FROM vacant_jobs WHERE description = 'PL/SQL DEVELOPER, HYBRID';

COMMIT;
