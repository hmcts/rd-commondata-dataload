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

INSERT INTO interpreter_and_sign_language (categorykey,serviceid,"key",value_en,value_cy,hinttext_en,hinttext_cy,lov_order,parentcategory,parentkey,active) VALUES
	 ('InterpreterLanguage',NULL,'bel-bel','Belorussian',NULL,NULL,NULL,NULL,NULL,NULL,'Y');
