--Delete the record id=50 and update with id=85
UPDATE flag_details SET id='85', value_en='Explanation of the court or tribunal and who''s in the room at the hearing',
      mrd_created_time='2022-10-27 12:33',mrd_updated_time='2022-10-27 12:33' where id = 50 and flag_code = 'RA0047' and category_id=11;
COMMIT;
