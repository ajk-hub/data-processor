-- @formatter:off

SELECT * FROM BATCH_JOB_INSTANCE;
SELECT * FROM BATCH_JOB_EXECUTION_CONTEXT;
SELECT * FROM BATCH_JOB_EXECUTION_PARAMS;
SELECT * FROM BATCH_STEP_EXECUTION_CONTEXT;
SELECT * FROM BATCH_JOB_EXECUTION;
SELECT * FROM BATCH_STEP_EXECUTION;

SELECT COUNT(*) total FROM CUSTOMERS;
SELECT STATUS, COUNT(*) total FROM CUSTOMER_SYNCUP GROUP BY STATUS;
SELECT STATUS, COUNT(*) total FROM CUSTOMER_STAGING GROUP BY STATUS;
SELECT STATUS, COUNT(*) total FROM CUSTOMER_PUBLISH GROUP BY STATUS;

-- @formatter:on
