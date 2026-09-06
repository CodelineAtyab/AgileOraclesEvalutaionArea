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

DECLARE
   v_cursor SYS_REFCURSOR;
   v_id notifications.id%TYPE;
   v_job_id notifications.job_id%TYPE;
   v_subject notifications.subject%TYPE;
   v_body notifications.body%TYPE;
   v_status notifications.status%TYPE;
   v_created notifications.created_at%TYPE;
   v_count NUMBER := 0;
BEGIN
   get_pending_notifications(v_cursor);
   LOOP
      FETCH v_cursor INTO v_id, v_job_id, v_subject, v_body, v_status, v_created;
      EXIT WHEN v_cursor%NOTFOUND;
      v_count := v_count + 1;
   END LOOP;
   CLOSE v_cursor;
   DBMS_OUTPUT.PUT_LINE('Pending notifications: ' || v_count);
END;
/
