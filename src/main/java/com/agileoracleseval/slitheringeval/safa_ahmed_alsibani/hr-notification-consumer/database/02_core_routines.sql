-- The Vacancy Loop
-- 02 - Custom lowercase function and support procedures

CREATE OR REPLACE FUNCTION custom_lower (
    p_text IN VARCHAR2
)
RETURN VARCHAR2
DETERMINISTIC
IS
    v_result      VARCHAR2(32767) := '';
    v_character   CHAR(1);
    v_ascii_code  NUMBER;
BEGIN
    IF p_text IS NULL THEN
        RETURN NULL;
    END IF;

    FOR i IN 1 .. LENGTH(p_text) LOOP
        v_character := SUBSTR(p_text, i, 1);
        v_ascii_code := ASCII(v_character);

        IF v_ascii_code BETWEEN 65 AND 90 THEN
            v_result := v_result || CHR(v_ascii_code + 32);
        ELSE
            v_result := v_result || v_character;
        END IF;
    END LOOP;

    RETURN v_result;
END;
/

CREATE OR REPLACE PROCEDURE register_notification (
    p_subject  IN VARCHAR2,
    p_body     IN VARCHAR2,
    p_job_id   IN NUMBER DEFAULT NULL
)
AS
BEGIN
    INSERT INTO notification (
        job_id,
        subject,
        body
    )
    VALUES (
        p_job_id,
        p_subject,
        p_body
    );
END;
/

CREATE OR REPLACE PROCEDURE normalize_job_description (
    p_description IN OUT VARCHAR2
)
AS
BEGIN
    p_description := custom_lower(p_description);
END;
/