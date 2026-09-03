-- ========================= 03_custom_lowercase_function.sql =========================
-- ===== Custom function to convert text to lowercase without using LOWER() =====

CREATE OR REPLACE FUNCTION custom_lowercase (p_text IN VARCHAR2)
RETURN VARCHAR2
AS
    v_result VARCHAR2(4000) := '';
    v_char   CHAR(1);
BEGIN
    -- Go through the text one character at a time
    FOR i IN 1 .. LENGTH(p_text) LOOP

        v_char := SUBSTR(p_text, i, 1);

        -- Check if the character is an uppercase English letter
        IF ASCII(v_char) BETWEEN ASCII('A') AND ASCII('Z') THEN
        
            -- Convert uppercase character to lowercase
            v_result := v_result || CHR(ASCII(v_char) + (ASCII('a') - ASCII('A')));
        ELSE

            -- Keep numbers, spaces and other characters unchanged
            v_result := v_result || v_char;

        END IF;

    END LOOP;

    -- Return the lowercase text
    RETURN v_result;

END;
/

--------------------------------------------------------------------------------
SELECT object_name, status
FROM user_objects
WHERE object_name = 'CUSTOM_LOWERCASE';

SELECT custom_lowercase('HELLO WORLD') FROM dual;
SELECT custom_lowercase('BUILD AND MAINTAIN JAVA APPLICATIONS') FROM dual;
SELECT custom_lowercase('Java Developer 2026!') FROM dual;