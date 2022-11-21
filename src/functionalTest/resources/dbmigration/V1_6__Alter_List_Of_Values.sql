ALTER TABLE List_Of_Values
DROP CONSTRAINT list_of_values_key_key;


ALTER TABLE List_Of_Values
ADD CONSTRAINT compKey UNIQUE (categorykey,key,serviceid);
