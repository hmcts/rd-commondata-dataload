DROP MATERIALIZED VIEW mv_list_of_values;

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

    UNION ALL

    SELECT * FROM additional_facilities

    UNION ALL

    SELECT * FROM panel_member_type

    UNION ALL

    SELECT * FROM entity_role_codes

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

-- For additional_facilities

CREATE TRIGGER tg_refresh_mv_for_additional_facilities AFTER INSERT OR UPDATE OR DELETE
ON additional_facilities
FOR EACH STATEMENT EXECUTE PROCEDURE refresh_mv_list_of_values();

-- For panel_member_type

CREATE TRIGGER tg_refresh_mv_for_panel_member_type AFTER INSERT OR UPDATE OR DELETE
ON panel_member_type
FOR EACH STATEMENT EXECUTE PROCEDURE refresh_mv_list_of_values();

-- For entity_role_codes

CREATE TRIGGER tg_refresh_mv_for_entity_role_codes AFTER INSERT OR UPDATE OR DELETE
ON entity_role_codes
FOR EACH STATEMENT EXECUTE PROCEDURE refresh_mv_list_of_values();

