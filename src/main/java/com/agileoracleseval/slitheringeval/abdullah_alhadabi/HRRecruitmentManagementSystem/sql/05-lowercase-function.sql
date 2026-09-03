CREATE OR REPLACE FUNCTION fn_custom_lowercase (
    p_text IN VARCHAR2
)
RETURN VARCHAR2
DETERMINISTIC
AS
    c_uppercase CONSTANT VARCHAR2(26) := 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
    c_lowercase CONSTANT VARCHAR2(26) := 'abcdefghijklmnopqrstuvwxyz';

    v_result    VARCHAR2(32767) := NULL;
    v_character VARCHAR2(1);
    v_position  PLS_INTEGER;
BEGIN
    IF p_text IS NULL THEN
        RETURN NULL;
    END IF;

    FOR character_index IN 1 .. LENGTH(p_text) LOOP
        v_character := SUBSTR(p_text, character_index, 1);
        v_position := INSTR(c_uppercase, v_character);

        IF v_position > 0 THEN
            v_result := v_result || SUBSTR(c_lowercase, v_position, 1);
        ELSE
            v_result := v_result || v_character;
        END IF;
    END LOOP;

    RETURN v_result;
END fn_custom_lowercase;
/

SHOW ERRORS FUNCTION fn_custom_lowercase;

PROMPT Custom lowercase function created successfully.
