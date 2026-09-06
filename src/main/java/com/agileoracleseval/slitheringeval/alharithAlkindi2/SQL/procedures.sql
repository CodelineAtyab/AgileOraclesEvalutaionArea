CREATE OR REPLACE PROCEDURE register_notification (
   p_job_id   IN notifications.job_id%TYPE,
   p_subject  IN notifications.subject%TYPE,
   p_body     IN notifications.body%TYPE
) AS
BEGIN
   INSERT INTO notifications (job_id, subject, body)
   VALUES (p_job_id, p_subject, p_body);
END register_notification;
/

CREATE OR REPLACE PROCEDURE expire_postings AS
BEGIN
   UPDATE vacant_jobs
   SET active = 0
   WHERE active = 1
     AND expires_at < SYSDATE;

   COMMIT;
END expire_postings;
/

CREATE OR REPLACE PROCEDURE archive_postings AS
BEGIN
   INSERT INTO jobs_posting_archive (id, title, description, department, posted_at, expires_at)
   SELECT vj.id, vj.title, vj.description, vj.department, vj.posted_at, vj.expires_at
   FROM vacant_jobs vj
   WHERE vj.active = 0
     AND NOT EXISTS (
        SELECT 1 FROM jobs_posting_archive ja WHERE ja.id = vj.id
     );

   COMMIT;
END archive_postings;
/

CREATE OR REPLACE PROCEDURE purge_postings AS
BEGIN
   DELETE FROM job_applications
   WHERE job_id IN (
      SELECT vj.id
      FROM vacant_jobs vj
      WHERE vj.active = 0
        AND EXISTS (SELECT 1 FROM jobs_posting_archive ja WHERE ja.id = vj.id)
        AND EXISTS (SELECT 1 FROM notifications n WHERE n.job_id = vj.id)
        AND NOT EXISTS (SELECT 1 FROM notifications n WHERE n.job_id = vj.id AND n.status = 'PENDING')
   );

   DELETE FROM vacant_jobs vj
   WHERE vj.active = 0
     AND EXISTS (SELECT 1 FROM jobs_posting_archive ja WHERE ja.id = vj.id)
     AND EXISTS (SELECT 1 FROM notifications n WHERE n.job_id = vj.id)
     AND NOT EXISTS (SELECT 1 FROM notifications n WHERE n.job_id = vj.id AND n.status = 'PENDING');

   COMMIT;
END purge_postings;
/

CREATE OR REPLACE PROCEDURE sweep_descriptions AS
BEGIN
   FOR rec IN (
      SELECT id, description
      FROM vacant_jobs
      WHERE active = 1
   ) LOOP
      UPDATE vacant_jobs
      SET description = my_lower(rec.description)
      WHERE id = rec.id;
   END LOOP;

   COMMIT;
END sweep_descriptions;
/

CREATE OR REPLACE PROCEDURE daily_digest AS
BEGIN
   FOR rec IN (
      SELECT vj.id, vj.description, COUNT(ja.id) AS applicant_count
      FROM vacant_jobs vj
      JOIN job_applications ja ON ja.job_id = vj.id
      WHERE ja.applied_at >= TRUNC(SYSDATE)
      GROUP BY vj.id, vj.description
   ) LOOP
      register_notification(
         p_job_id  => rec.id,
         p_subject => 'Daily application digest for posting ' || rec.id,
         p_body    => 'Posting ID ' || rec.id || ': "' || rec.description || '" received ' ||
                      rec.applicant_count || ' application(s) today.'
      );
   END LOOP;
END daily_digest;
/

CREATE OR REPLACE PROCEDURE get_pending_notifications (
   p_cursor OUT SYS_REFCURSOR
) AS
BEGIN
   OPEN p_cursor FOR
      SELECT id, job_id, subject, body, status, created_at
      FROM notifications
      WHERE status = 'PENDING'
      ORDER BY id;
END get_pending_notifications;
/
