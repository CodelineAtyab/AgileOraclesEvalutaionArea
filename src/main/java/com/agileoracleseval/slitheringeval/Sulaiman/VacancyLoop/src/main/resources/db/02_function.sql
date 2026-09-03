--------------------------------------------------------------------------
-- 02_function.sql
--
-- custom_lower: a hand-rolled lower-casing function (no LOWER(), no
-- TRANSLATE() shortcut), reused by both the update trigger and the bulk
-- sweep. Pure ASCII arithmetic: any byte in 'A'..'Z' (65..90) is shifted
-- by +32 to its lowercase twin; everything else passes through unchanged
-- (digits, spaces, punctuation, already-lowercase letters).
--
-- Idempotent by construction: lowering already-lowercase text returns it
-- unchanged.
--------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION custom_lower (
  p_text IN VARCHAR2
) RETURN VARCHAR2
IS
  v_result VARCHAR2(4000);
  v_char   VARCHAR2(1);
  v_code   PLS_INTEGER;
BEGIN
  -- NULL in, NULL out — don't invent an empty string.
  IF p_text IS NULL THEN
    RETURN NULL;
  END IF;

  v_result := '';
  FOR i IN 1 .. LENGTH(p_text) LOOP
    v_char := SUBSTR(p_text, i, 1);
    v_code := ASCII(v_char);

    IF v_code BETWEEN 65 AND 90 THEN       -- 'A'..'Z'
      v_result := v_result || CHR(v_code + 32);
    ELSE
      v_result := v_result || v_char;
    END IF;
  END LOOP;

  RETURN v_result;
END custom_lower;
/
