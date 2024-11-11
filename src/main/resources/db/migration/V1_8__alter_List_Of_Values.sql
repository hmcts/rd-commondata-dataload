
ALTER TABLE List_Of_Values ADD COLUMN external_reference VARCHAR(200);
ALTER TABLE List_Of_Values ADD COLUMN external_reference_type VARCHAR(200);

ALTER TABLE list_of_values
 ADD constraint unique_external_reference check
 ((external_reference_type is null and external_reference is null)
 or (external_reference_type is not null and external_reference is not null))
