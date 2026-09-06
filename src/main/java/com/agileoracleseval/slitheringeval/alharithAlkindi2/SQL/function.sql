CREATE OR REPLACE FUNCTION my_lower (p_text IN VARCHAR2)
   RETURN VARCHAR2
AS
   v_result   VARCHAR2(4000);
   v_char     CHAR(1);
   v_code     PLS_INTEGER;
BEGIN
   IF p_text IS NULL THEN
      RETURN NULL;
   END IF;

   v_result := '';

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
END my_lower;
/
