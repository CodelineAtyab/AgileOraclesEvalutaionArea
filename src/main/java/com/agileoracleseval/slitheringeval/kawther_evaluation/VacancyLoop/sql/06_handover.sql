-- ============================================================
-- Step 13: the database hands pending notifications out as a live
-- result set (explicit ref cursor) - not a copy, not a query the
-- caller wrote itself.
--
-- Design decision: notifications are marked delivered_flag='Y' at
-- hand-off time, inside this same procedure, before the caller ever
-- reads a row. This gives at-most-once delivery to Slack: if the Java
-- consumer dies mid-drain, a message is never resent (at worst it is
-- lost once) rather than being posted twice. Acceptable trade-off for
-- an HR notification feed.
--
-- v_batch_time is computed once and reused in both the UPDATE and the
-- SELECT, so the cursor returns exactly (and only) the rows this call
-- just marked - never rows a previous or concurrent call already
-- claimed.
-- ============================================================
CREATE OR REPLACE PROCEDURE get_pending_notifications (p_cursor OUT SYS_REFCURSOR)
AS
  v_batch_time TIMESTAMP := SYSTIMESTAMP;
BEGIN
  UPDATE notification_outbox
     SET delivered_flag = 'Y',
         delivered_on   = v_batch_time
   WHERE delivered_flag = 'N';

  OPEN p_cursor FOR
    SELECT notification_id, job_id, msg_category, msg_subject, msg_body, generated_on
    FROM notification_outbox
    WHERE delivered_on = v_batch_time
    ORDER BY notification_id;

  COMMIT;
END get_pending_notifications;
/
