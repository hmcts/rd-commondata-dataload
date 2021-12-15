-- Create mv mv_list_of_values

CREATE MATERIALIZED VIEW mv_list_of_values AS

  (
    SELECT * FROM hearing_channel

    UNION ALL

    SELECT * FROM  hearing_type

    UNION ALL

    SELECT * FROM hearing_priority

    UNION ALL

    SELECT * FROM non_standard_duration_codes

    UNION ALL

    SELECT * FROM case_type

    UNION ALL

    SELECT * FROM cancellation_reasons

    UNION ALL

    SELECT * FROM interpreter_and_sign_language
  );

--Create unique index on mv_list_of_values

CREATE UNIQUE INDEX unique_list_of_values_indx ON mv_list_of_values
(CategoryKey,ServiceID,Key,Value_EN,Value_CY,HintText_EN,HintText_CY,Lov_Order,ParentCategory,ParentKey,Active);

-- Create function to refresh mv

CREATE OR REPLACE FUNCTION refresh_mv_list_of_values()
  RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
  REFRESH MATERIALIZED VIEW CONCURRENTLY mv_list_of_values;
  RETURN NULL;
END;
$$;

-- Write triggers for each table

--For  hearing_channel

CREATE TRIGGER tg_refresh_mv_for_hearing_channel AFTER INSERT OR UPDATE OR DELETE
ON hearing_channel
FOR EACH STATEMENT EXECUTE PROCEDURE refresh_mv_list_of_values();

-- For  hearing_type

CREATE TRIGGER tg_refresh_mv_for_hearing_type AFTER INSERT OR UPDATE OR DELETE
ON hearing_type
FOR EACH STATEMENT EXECUTE PROCEDURE refresh_mv_list_of_values();

-- For  hearing_priority

CREATE TRIGGER tg_refresh_mv_for_hearing_priority AFTER INSERT OR UPDATE OR DELETE
ON hearing_priority
FOR EACH STATEMENT EXECUTE PROCEDURE refresh_mv_list_of_values();

-- For  non_standard_duration_codes

CREATE TRIGGER tg_refresh_mv_for_non_standard_duration_codes AFTER INSERT OR UPDATE OR DELETE
ON non_standard_duration_codes
FOR EACH STATEMENT EXECUTE PROCEDURE refresh_mv_list_of_values();

-- For  case_type

CREATE TRIGGER tg_refresh_mv_for_case_type AFTER INSERT OR UPDATE OR DELETE
ON case_type
FOR EACH STATEMENT EXECUTE PROCEDURE refresh_mv_list_of_values();

-- For  cancellation_reasons

CREATE TRIGGER tg_refresh_mv_for_cancellation_reasons AFTER INSERT OR UPDATE OR DELETE
ON cancellation_reasons
FOR EACH STATEMENT EXECUTE PROCEDURE refresh_mv_list_of_values();

-- For  interpreter_and_sign_language

CREATE TRIGGER tg_refresh_mv_for_interpreter_and_sign_language AFTER INSERT OR UPDATE OR DELETE
ON interpreter_and_sign_language
FOR EACH STATEMENT EXECUTE PROCEDURE refresh_mv_list_of_values();

