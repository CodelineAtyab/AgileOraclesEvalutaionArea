--------------------------------------------------------------------------------
-- 01_schema_and_seed.sql
-- Run FIRST, on a clean/empty schema.
--------------------------------------------------------------------------------

BEGIN
    FOR t IN (SELECT table_name FROM user_tables
              WHERE table_name IN ('JOB_APPLICATIONS','NOTIFICATIONSHR',
                                    'JOBS_POSTING_ARCHIVE','VACANT_JOBS'))
    LOOP
        EXECUTE IMMEDIATE 'DROP TABLE ' || t.table_name || ' CASCADE CONSTRAINTS';
    END LOOP;
END;
/

CREATE TABLE vacant_jobs (
    job_id          NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    job_title       VARCHAR2(100)  NOT NULL,
    description     VARCHAR2(2000) NOT NULL,
    posted_at       TIMESTAMP      DEFAULT SYSTIMESTAMP NOT NULL,
    expires_at      TIMESTAMP      NOT NULL,
    is_active       CHAR(1)        DEFAULT 'Y' NOT NULL,
    CONSTRAINT chk_vacant_jobs_active CHECK (is_active IN ('Y','N')),
    CONSTRAINT chk_vacant_jobs_dates  CHECK (expires_at > posted_at)
);

CREATE TABLE jobs_posting_archive (
    job_id          NUMBER          PRIMARY KEY,
    job_title       VARCHAR2(200)   NOT NULL,
    description     VARCHAR2(4000)  NOT NULL,
    posted_at       TIMESTAMP       NOT NULL,
    expires_at      TIMESTAMP       NOT NULL,
    archived_at     TIMESTAMP       DEFAULT SYSTIMESTAMP NOT NULL
);

CREATE TABLE job_applications (
    application_id  NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    job_id          NUMBER,
    applicant_name  VARCHAR2(200)   NOT NULL,
    applied_at      TIMESTAMP       DEFAULT SYSTIMESTAMP NOT NULL,
    digested        CHAR(1)         DEFAULT 'N' NOT NULL,
    CONSTRAINT chk_job_applications_digested CHECK (digested IN ('Y','N')),
    CONSTRAINT fk_job_applications_job
        FOREIGN KEY (job_id) REFERENCES vacant_jobs (job_id)
        ON DELETE SET NULL
);

CREATE TABLE notificationshr (
    notification_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    job_id          NUMBER,
    subject         VARCHAR2(200)   NOT NULL,
    body            VARCHAR2(4000)  NOT NULL,
    sent_flag       CHAR(1)         DEFAULT 'N' NOT NULL,
    created_at      TIMESTAMP       DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT chk_notifications_sent CHECK (sent_flag IN ('Y','N'))
);

--------------------------------------------------------------------------------
-- Seed data
--------------------------------------------------------------------------------

INSERT INTO vacant_jobs (job_title, description, posted_at, expires_at, is_active)
VALUES ('Data Analyst',
        'Turn raw hiring data into dashboards leadership can use.',
        SYSTIMESTAMP,
        SYSTIMESTAMP + INTERVAL '10' DAY,
        'Y');

INSERT INTO vacant_jobs (job_title, description, posted_at, expires_at, is_active)
VALUES ('Backend Engineer',
        'Build and maintain server-side services in Java and PL/SQL.',
        SYSTIMESTAMP - INTERVAL '1' DAY,
        SYSTIMESTAMP + INTERVAL '2' MINUTE,
        'Y');

INSERT INTO vacant_jobs (job_title, description, posted_at, expires_at, is_active)
VALUES ('QA Analyst',
        'Design and run test plans for the recruitment platform.',
        SYSTIMESTAMP - INTERVAL '3' DAY,
        SYSTIMESTAMP + INTERVAL '2' MINUTE,
        'Y');

INSERT INTO vacant_jobs (job_title, description, posted_at, expires_at, is_active)
VALUES ('Frontend Developer',
        'THIS DESCRIPTION IS SHOUTING IN ALL CAPS ON PURPOSE.',
        SYSTIMESTAMP,
        SYSTIMESTAMP + INTERVAL '7' DAY,
        'Y');

INSERT INTO vacant_jobs (job_title, description, posted_at, expires_at, is_active)
VALUES ('DevOps Engineer',
        'Own CI/CD pipelines and cloud infrastructure.',
        SYSTIMESTAMP,
        SYSTIMESTAMP + INTERVAL '14' DAY,
        'Y');

COMMIT;

INSERT INTO job_applications (job_id, applicant_name, applied_at)
SELECT job_id, 'Amina Al-Balushi', SYSTIMESTAMP
FROM vacant_jobs WHERE job_title = 'QA Analyst';

INSERT INTO job_applications (job_id, applicant_name, applied_at)
SELECT job_id, 'Yousef Al-Hinai', SYSTIMESTAMP
FROM vacant_jobs WHERE job_title = 'QA Analyst';

INSERT INTO job_applications (job_id, applicant_name, applied_at)
SELECT job_id, 'Fatma Al-Riyami', SYSTIMESTAMP
FROM vacant_jobs WHERE job_title = 'QA Analyst';

INSERT INTO job_applications (job_id, applicant_name, applied_at)
SELECT job_id, 'Salim Al-Kindi', SYSTIMESTAMP
FROM vacant_jobs WHERE job_title = 'Backend Engineer';

COMMIT;
