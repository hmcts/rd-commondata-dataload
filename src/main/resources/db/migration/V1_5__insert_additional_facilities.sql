CREATE TABLE additional_facilities (
	categorykey varchar(64) NOT NULL,
	serviceid varchar(16) NULL,
	"key" varchar(64) NOT NULL,
	value_en varchar(128) NOT NULL,
	value_cy varchar(128) NULL,
	hinttext_en varchar(512) NULL,
	hinttext_cy varchar(512) NULL,
	lov_order int8 NULL,
	parentcategory varchar(64) NULL,
	parentkey varchar(64) NULL,
	active varchar(1) NULL
);

INSERT INTO additional_facilities (categorykey,serviceid,"key",value_en,value_cy,hinttext_en,hinttext_cy,lov_order,parentcategory,parentkey,active) VALUES
	 ('AdditionalFacilities',NULL,'AF-IDC','Immigration Detention Centre',NULL,NULL,NULL,NULL,NULL,NULL,'Y'),
	 ('AdditionalFacilities',NULL,'AF-ICC','In Camera Court',NULL,NULL,NULL,NULL,NULL,NULL,'Y'),
	 ('AdditionalFacilities',NULL,'AF-SSC','Same Sex Courtroom',NULL,NULL,NULL,NULL,NULL,NULL,'Y'),
	 ('AdditionalFacilities',NULL,'AF-SD','Secure Dock',NULL,NULL,NULL,NULL,NULL,NULL,'Y'),
	 ('AdditionalFacilities',NULL,'AF-WS','Witness Screen',NULL,NULL,NULL,NULL,NULL,NULL,'Y'),
	 ('AdditionalFacilities',NULL,'AF-WR','Witness Room',NULL,NULL,NULL,NULL,NULL,NULL,'Y'),
	 ('AdditionalFacilities',NULL,'AF-VC','Video Conferencing',NULL,NULL,NULL,NULL,NULL,NULL,'Y'),
	 ('AdditionalFacilities',NULL,'AF-VF','Video Facility',NULL,NULL,NULL,NULL,NULL,NULL,'Y'),
	 ('AdditionalFacilities',NULL,'AF-PVL','Prison Video Link',NULL,NULL,NULL,NULL,NULL,NULL,'Y');

UPDATE interpreter_and_sign_language SET value_en='Belorussian' WHERE value_en='Belorussia';

CREATE TABLE entity_role_codes (
  categorykey varchar(64) NOT NULL,
  serviceid varchar(16),
  key varchar(64) NOT NULL,
  value_en varchar(128) NOT NULL,
  value_cy varchar(128),
  hinttext_en varchar(512),
  hinttext_cy varchar(512),
  lov_order BIGINT,
  parentcategory varchar(64),
  parentkey varchar(64),
  active varchar(1)
);

INSERT INTO entity_role_codes (categorykey,serviceid,"key",value_en,value_cy,hinttext_en,hinttext_cy,lov_order,parentcategory,parentkey,active) VALUES
 ('EntityRoleCode', 'BBA3', 'BBA3-appellant', 'Appellant', null, null, null, null, null, null, 'Y'),
 ('EntityRoleCode', 'BBA3', 'BBA3-appointee', 'Appointee', null, null, null, null, null, null, 'Y'),
 ('EntityRoleCode', 'BBA3', 'BBA3-jointParty', 'Joint Party', null, null, null, null, null, null, 'Y'),
 ('EntityRoleCode', 'BBA3', 'BBA3-otherParty', 'Other Party', null, null, null, null, null, null, 'Y');
