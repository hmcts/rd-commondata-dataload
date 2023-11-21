-- sourced from:
-- https://github.com/spring-projects/spring-batch/blob/972951a40af03c9c0553fb4c438f9aaeb1c9f1b5/spring-batch-core/src/main/resources/org/springframework/batch/core/migration/5.0/migration-postgresql.sql
--ALTER TABLE BATCH_STEP_EXECUTION ADD CREATE_TIME TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:00';
--
--ALTER TABLE BATCH_STEP_EXECUTION ALTER COLUMN START_TIME DROP NOT NULL;
--
--ALTER TABLE BATCH_JOB_EXECUTION_PARAMS DROP COLUMN DATE_VAL;
--
--ALTER TABLE BATCH_JOB_EXECUTION_PARAMS DROP COLUMN LONG_VAL;
--
--ALTER TABLE BATCH_JOB_EXECUTION_PARAMS DROP COLUMN DOUBLE_VAL;
--
--ALTER TABLE BATCH_JOB_EXECUTION_PARAMS ALTER COLUMN TYPE_CD TYPE VARCHAR(100);
--
--ALTER TABLE BATCH_JOB_EXECUTION_PARAMS RENAME TYPE_CD TO PARAMETER_TYPE;
--
--ALTER TABLE BATCH_JOB_EXECUTION_PARAMS ALTER COLUMN KEY_NAME TYPE VARCHAR(100);
--
--ALTER TABLE BATCH_JOB_EXECUTION_PARAMS RENAME KEY_NAME TO PARAMETER_NAME;
--
--ALTER TABLE BATCH_JOB_EXECUTION_PARAMS ALTER COLUMN STRING_VAL TYPE VARCHAR(2500);
--
--ALTER TABLE BATCH_JOB_EXECUTION_PARAMS RENAME STRING_VAL TO PARAMETER_VALUE;