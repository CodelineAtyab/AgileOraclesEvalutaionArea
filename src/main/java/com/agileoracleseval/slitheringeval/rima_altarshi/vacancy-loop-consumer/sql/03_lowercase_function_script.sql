-- Converts text to lowercase without using Oracle LOWER().
------------------------------------------------------------

CREATE OR REPLACE FUNCTION F_TO_LOWER (
    P_TEXT IN VARCHAR2
)
RETURN VARCHAR2
AS
    V_RESULT VARCHAR2(4000) := '';
    V_CHAR   VARCHAR2(1);
BEGIN

    FOR I IN 1 .. LENGTH(P_TEXT)
    LOOP
        V_CHAR := SUBSTR(P_TEXT, I, 1);

        IF ASCII(V_CHAR) BETWEEN ASCII('A') AND ASCII('Z') THEN
            V_RESULT := V_RESULT || CHR(ASCII(V_CHAR) + 32);
        ELSE
            V_RESULT := V_RESULT || V_CHAR;
        END IF;

    END LOOP;

    RETURN V_RESULT;

END F_TO_LOWER;
/

------------------------------------------------------------
-- TEST THE FUNCTION

SELECT F_TO_LOWER('AGILE ORACLE DATABASE') AS lowercase_text
FROM dual;
------------
--*************************************

-- -- For uppercase conversion, check 'a' to 'z' and subtract 32 instead.

--IF ASCII(V_CHAR) BETWEEN ASCII('a') AND ASCII('z') THEN

    --V_RESULT := V_RESULT || CHR(ASCII(V_CHAR) - 32);

--ELSE

    --V_RESULT := V_RESULT || V_CHAR;

--END IF;

-------**********************************