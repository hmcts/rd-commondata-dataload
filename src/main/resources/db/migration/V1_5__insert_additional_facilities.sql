CREATE TABLE Additional_Facilities (
CategoryKey varchar(64) not null,
ServiceID varchar(16),
Key varchar(64) not null,
Value_EN varchar(128) not null,
Value_CY varchar(128),
HintText_EN varchar(512),
HintText_CY varchar(512),
Lov_Order BIGINT,
ParentCategory varchar(64),
ParentKey varchar(64),
Active varchar(1)
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
	 ('AdditionalFacilities',NULL,'AF-PVL','Prison Video Link',NULL,NULL,NULL,NULL,NULL,NULL,'Y'),
	 ('InterpreterLanguage',NULL,'bel-bel','Belorussian',NULL,NULL,NULL,NULL,NULL,NULL,'Y');
