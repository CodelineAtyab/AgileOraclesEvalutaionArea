CREATE OR REPLACE TRIGGER trg_vacant_jobs_notify
   AFTER INSERT ON vacant_jobs
   FOR EACH ROW
BEGIN
   register_notification(
      p_job_id  => :NEW.id,
      p_subject => 'New job posted: ' || :NEW.title,
      p_body    => 'A new vacancy "' || :NEW.title || '" has been posted in ' ||
                   NVL(:NEW.department, 'an unspecified department') ||
                   '. It expires on ' || TO_CHAR(:NEW.expires_at, 'DD-MON-YYYY HH24:MI') || '.'
   );
END trg_vacant_jobs_notify;
/

CREATE OR REPLACE TRIGGER trg_vacant_jobs_lowercase
   BEFORE UPDATE OF description ON vacant_jobs
   FOR EACH ROW
BEGIN
   :NEW.description := my_lower(:NEW.description);
END trg_vacant_jobs_lowercase;
/
