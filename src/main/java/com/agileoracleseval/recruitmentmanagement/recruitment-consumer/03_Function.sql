-- FUNCTION

-- FUNCTION= transform the text and RETURN a value
-- PROCEDURE= organise the work and call the function
-- TRIGGER= automatically start the work when description is updated

-- Lane B — The Edit
-- first create a custom function called custom_lowercase that converts uppercase letters to lowercase without using Oracle’s built-in LOWER() function. 
-- Then create the normalize_job_description procedure, which receives a description and passes it through that function. 
-- After that, create a BEFORE UPDATE OF description trigger on vacant_jobs 
-- so that whenever HR updates a job description, the trigger automatically sends the new text to the procedure and replaces :NEW.description 
-- with the cleaned lowercase version before the row is saved

-- Lane B - 6 - Convert a job description to lowercase using our own reusable function
-- Create Function that Convert uppercase to lowercase
CREATE OR REPLACE FUNCTION custom_lowercase (
    p_text IN VARCHAR2
)
RETURN VARCHAR2
AS

    v_result    VARCHAR2(4000) := '';
    v_character VARCHAR2(1);

BEGIN

    FOR i IN 1 .. LENGTH(p_text)
    LOOP

        v_character := SUBSTR(p_text, i, 1);


        IF ASCII(v_character)
           BETWEEN ASCII('A') AND ASCII('Z')
        THEN

            v_result := v_result || CHR(ASCII(v_character) + 32);

        ELSE

            v_result := v_result || v_character;

        END IF;

    END LOOP;

    RETURN v_result;

END;
/
