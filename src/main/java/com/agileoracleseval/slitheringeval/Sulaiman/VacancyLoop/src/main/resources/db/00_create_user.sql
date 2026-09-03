--------------------------------------------------------------------------
-- 00_create_user.sql
-- Creates a dedicated VACANCYLOOP schema so the project does not live in
-- SYSTEM. Run this FIRST, as a DBA (SYS or SYSTEM), connected to the
-- correct pluggable database.
--
-- Connect to the right PDB before running, e.g.:
--     ALTER SESSION SET CONTAINER = ORCLPDB;
-- (or connect directly:  sqlplus sys/pw@//localhost:1521/ORCLPDB as sysdba)
--
-- After this runs, connect AS vacancyloop and run 01..06 in order:
--     CONNECT vacancyloop/Vacancy#Loop1@//localhost:1521/ORCLPDB
--------------------------------------------------------------------------

-- Drop first so this script is safe to re-run during development.
BEGIN
  EXECUTE IMMEDIATE 'DROP USER vacancyloop CASCADE';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -1918 THEN  -- ORA-01918: user does not exist
      RAISE;
    END IF;
END;
/

CREATE USER vacancyloop IDENTIFIED BY "Vacancy#Loop1"
  DEFAULT TABLESPACE users
  TEMPORARY TABLESPACE temp
  QUOTA UNLIMITED ON users;

-- Privileges the project needs:
GRANT CREATE SESSION      TO vacancyloop;  -- log in
GRANT CREATE TABLE        TO vacancyloop;  -- schema
GRANT CREATE PROCEDURE    TO vacancyloop;  -- procedures + function
GRANT CREATE TRIGGER      TO vacancyloop;  -- triggers
GRANT CREATE SEQUENCE     TO vacancyloop;  -- identity columns use sequences
GRANT CREATE JOB          TO vacancyloop;  -- DBMS_SCHEDULER jobs
GRANT MANAGE SCHEDULER    TO vacancyloop;  -- inspect scheduler views

-- DBMS_SCHEDULER is normally granted to PUBLIC, but grant explicitly to
-- be safe across environments:
GRANT EXECUTE ON DBMS_SCHEDULER TO vacancyloop;

