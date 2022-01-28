INSERT INTO entity_role_codes (categorykey,serviceid,"key",value_en,value_cy,hinttext_en,hinttext_cy,lov_order,parentcategory,parentkey,active) VALUES
 	 ('EntityRoleCode','BBA3','Applicant','Applicant',NULL,NULL,NULL,NULL,NULL,NULL,'Y'),
	 ('EntityRoleCode','BBA3','Representative','Legal Representative',NULL,NULL,NULL,NULL,NULL,NULL,'Y'),
	 ('EntityRoleCode','BBA3','Respondent','Respondent',NULL,NULL,NULL,NULL,NULL,NULL,'Y');

UPDATE entity_role_codes
SET parentkey = 'Applicant' WHERE value_en = 'Appellant';

UPDATE entity_role_codes
SET parentkey = 'Representative' WHERE value_en = 'Appointee';

UPDATE entity_role_codes
SET parentkey = 'Respondent' WHERE value_en = 'Joint Party';

UPDATE entity_role_codes
SET parentkey = 'Respondent' WHERE value_en = 'Other Party';
