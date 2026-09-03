-- ============================================================
-- Function - our own lowercasing routine.
-- Oracle's LOWER() is off-limits by the brief (step 06); this is a
-- manual ASCII-shift implementation, reused by both the update trigger
-- (step 05) and the sweep job (step 10) so both routes land on
-- identical text.
-- ============================================================
CREATE OR REPLACE FUNCTION lowercase_text (p_text IN VARCHAR2) RETURN VARCHAR2
AS
  v_result VARCHAR2(4000);
  v_char   CHAR(1);
  v_code   NUMBER;
BEGIN
  v_result := NULL;
  FOR i IN 1 .. LENGTH(p_text) LOOP
    v_char := SUBSTR(p_text, i, 1);
    v_code := ASCII(v_char);
    IF v_code BETWEEN 65 AND 90 THEN
      v_result := v_result || CHR(v_code + 32);
    ELSE
      v_result := v_result || v_char;
    END IF;
  END LOOP;
  RETURN v_result;
END lowercase_text;
/
