-- Seed eight technology vacancies and thirty applicants without repeating existing live matches

INSERT INTO vacant_jobs (job_title, description, expires_at)
SELECT 'Oracle PL/SQL Developer',
       'DEVELOP PROCEDURES TRIGGERS AND ORACLE DATABASE APPLICATIONS',
       SYSTIMESTAMP + INTERVAL '1' DAY
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM vacant_jobs
    WHERE job_title = 'Oracle PL/SQL Developer'
);

INSERT INTO vacant_jobs (job_title, description, expires_at)
SELECT 'Java Spring Boot Developer',
       'BUILD JAVA APIS AND SPRING BOOT BACKEND SERVICES',
       SYSTIMESTAMP + INTERVAL '2' DAY
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM vacant_jobs
    WHERE job_title = 'Java Spring Boot Developer'
);

INSERT INTO vacant_jobs (job_title, description, expires_at)
SELECT 'OCI Cloud Engineer',
       'CONFIGURE AND MAINTAIN ORACLE CLOUD INFRASTRUCTURE',
       SYSTIMESTAMP + INTERVAL '3' DAY
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM vacant_jobs
    WHERE job_title = 'OCI Cloud Engineer'
);

INSERT INTO vacant_jobs (job_title, description, expires_at)
SELECT 'DevOps Engineer',
       'MANAGE DEPLOYMENT PIPELINES CONTAINERS AND AUTOMATION',
       SYSTIMESTAMP + INTERVAL '4' DAY
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM vacant_jobs
    WHERE job_title = 'DevOps Engineer'
);

INSERT INTO vacant_jobs (job_title, description, expires_at)
SELECT 'Cybersecurity Analyst',
       'MONITOR SYSTEMS AND INVESTIGATE SECURITY THREATS',
       SYSTIMESTAMP + INTERVAL '5' DAY
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM vacant_jobs
    WHERE job_title = 'Cybersecurity Analyst'
);

INSERT INTO vacant_jobs (job_title, description, expires_at)
SELECT 'Data Analyst',
       'ANALYSE BUSINESS DATA AND PREPARE TECHNICAL REPORTS',
       SYSTIMESTAMP + INTERVAL '6' DAY
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM vacant_jobs
    WHERE job_title = 'Data Analyst'
);

INSERT INTO vacant_jobs (job_title, description, expires_at)
SELECT 'Front-End Developer',
       'BUILD RESPONSIVE WEB APPLICATION INTERFACES',
       SYSTIMESTAMP + INTERVAL '7' DAY
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM vacant_jobs
    WHERE job_title = 'Front-End Developer'
);

INSERT INTO vacant_jobs (job_title, description, expires_at)
SELECT 'QA Automation Engineer',
       'Create and maintain automated software tests',
       SYSTIMESTAMP + INTERVAL '7' DAY
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM vacant_jobs
    WHERE job_title = 'QA Automation Engineer'
);

INSERT INTO job_applications (
    job_id,
    applicant_name,
    applicant_email
)
SELECT v.job_id,
       applicants.applicant_name,
       applicants.applicant_email
FROM vacant_jobs v
JOIN (
    SELECT 'Oracle PL/SQL Developer' job_title,
           'Ahmed Al Harthy' applicant_name,
           'ahmed.alharthy@gmail.com' applicant_email
    FROM dual
    UNION ALL
    SELECT 'Oracle PL/SQL Developer',
           'Sara Al Balushi',
           'sara.albalushi@gmail.com'
    FROM dual
    UNION ALL
    SELECT 'Oracle PL/SQL Developer',
           'Mohammed Al Lawati',
           'mohammed.allawati@gmail.com'
    FROM dual
    UNION ALL
    SELECT 'Oracle PL/SQL Developer',
           'Fatma Al Habsi',
           'fatma.alhabsi@gmail.com'
    FROM dual
    UNION ALL
    SELECT 'Oracle PL/SQL Developer',
           'Ali Al Rashdi',
           'ali.alrashdi@gmail.com'
    FROM dual

    UNION ALL
    SELECT 'Java Spring Boot Developer',
           'Noor Al Farsi',
           'noor.alfarsi@gmail.com'
    FROM dual
    UNION ALL
    SELECT 'Java Spring Boot Developer',
           'Yusuf Al Hinai',
           'yusuf.alhinai@gmail.com'
    FROM dual
    UNION ALL
    SELECT 'Java Spring Boot Developer',
           'Maryam Al Kindi',
           'maryam.alkindi@gmail.com'
    FROM dual
    UNION ALL
    SELECT 'Java Spring Boot Developer',
           'Khalid Al Busaidi',
           'khalid.albusaidi@gmail.com'
    FROM dual

    UNION ALL
    SELECT 'OCI Cloud Engineer',
           'Aisha Al Shukaili',
           'aisha.alshukaili@gmail.com'
    FROM dual
    UNION ALL
    SELECT 'OCI Cloud Engineer',
           'Omar Al Amri',
           'omar.alamri@gmail.com'
    FROM dual
    UNION ALL
    SELECT 'OCI Cloud Engineer',
           'Huda Al Siyabi',
           'huda.alsiyabi@gmail.com'
    FROM dual
    UNION ALL
    SELECT 'OCI Cloud Engineer',
           'Salim Al Maawali',
           'salim.almaawali@gmail.com'
    FROM dual

    UNION ALL
    SELECT 'DevOps Engineer',
           'Layla Al Rawahi',
           'layla.alrawahi@gmail.com'
    FROM dual
    UNION ALL
    SELECT 'DevOps Engineer',
           'AlJolanda Al Handhali',
           'aljolanda.alhandhali@gmail.com'
    FROM dual
    UNION ALL
    SELECT 'DevOps Engineer',
           'Reem Al Mandhari',
           'reem.almandhari@gmail.com'
    FROM dual
    UNION ALL
    SELECT 'DevOps Engineer',
           'Nasser Al Riyami',
           'nasser.alriyami@gmail.com'
    FROM dual

    UNION ALL
    SELECT 'Cybersecurity Analyst',
           'Zahra Al Mukhaini',
           'zahra.almukhaini@gmail.com'
    FROM dual
    UNION ALL
    SELECT 'Cybersecurity Analyst',
           'Abdullah Al Saadi',
           'abdullah.alsaadi@gmail.com'
    FROM dual
    UNION ALL
    SELECT 'Cybersecurity Analyst',
           'Maha Al Zadjali',
           'maha.alzadjali@gmail.com'
    FROM dual

    UNION ALL
    SELECT 'Data Analyst',
           'Rayan Al Jabri',
           'rayan.aljabri@gmail.com'
    FROM dual
    UNION ALL
    SELECT 'Data Analyst',
           'Iman Al Sabti',
           'iman.alsabti@gmail.com'
    FROM dual
    UNION ALL
    SELECT 'Data Analyst',
           'Faisal Al Nabhani',
           'faisal.alnabhani@gmail.com'
    FROM dual

    UNION ALL
    SELECT 'Front-End Developer',
           'Amal Al Mahrouqi',
           'amal.almahrouqi@gmail.com'
    FROM dual
    UNION ALL
    SELECT 'Front-End Developer',
           'Tariq Al Azri',
           'tariq.alazri@gmail.com'
    FROM dual
    UNION ALL
    SELECT 'Front-End Developer',
           'Basma Al Balushi',
           'basma.albalushi@gmail.com'
    FROM dual
    UNION ALL
    SELECT 'Front-End Developer',
           'Sami Al Harrasi',
           'sami.alharrasi@gmail.com'
    FROM dual

    UNION ALL
    SELECT 'QA Automation Engineer',
           'Hana Al Toubi',
           'hana.altoubi@gmail.com'
    FROM dual
    UNION ALL
    SELECT 'QA Automation Engineer',
           'Majid Al Badi',
           'majid.albadi@gmail.com'
    FROM dual
    UNION ALL
    SELECT 'QA Automation Engineer',
           'Rasha Al Shibli',
           'rasha.alshibli@gmail.com'
    FROM dual

) applicants
    ON applicants.job_title = v.job_title
WHERE NOT EXISTS (
    SELECT 1
    FROM job_applications existing_application
    WHERE existing_application.job_id = v.job_id
      AND existing_application.applicant_email =
          applicants.applicant_email
);

COMMIT;
