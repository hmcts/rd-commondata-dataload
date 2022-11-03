DELETE FROM flag_details where id = 50 and flag_code = 'RA0047' and category_id=11;
INSERT INTO flag_details (id, flag_code, value_en, value_cy, category_id, mrd_created_time, mrd_updated_time, mrd_deleted_time) VALUES
    (85, 'RA0047', 'Explanation of the court or tribunal and who''s in the room at the hearing', '', 11, '2022-10-27 12:33', '2022-10-27 12:33', NULL);
COMMIT;
