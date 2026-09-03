-- Own lower function (not oracle LOWER)

CREATE OR REPLACE FUNCTION my_lower (p_text IN VARCHAR2)
RETURN VARCHAR2
IS
    v_out  VARCHAR2(4000) := '';
    v_ch   VARCHAR2(1);
    v_code NUMBER;
BEGIN
    IF p_text IS NULL THEN
        RETURN NULL;
    END IF;

    FOR i IN 1 .. LENGTH(p_text) LOOP
        v_ch   := SUBSTR(p_text, i, 1);
        v_code := ASCII(v_ch);

        IF v_code >= 65 AND v_code <= 90 THEN
            v_out := v_out || CHR(v_code + 32);
        ELSE
            v_out := v_out || v_ch;
        END IF;
    END LOOP;

    RETURN v_out;
END my_lower;
/
