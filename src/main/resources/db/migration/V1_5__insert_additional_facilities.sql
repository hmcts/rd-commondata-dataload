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

 --Create table panel_member_type
 CREATE TABLE panel_member_type (
 CategoryKey varchar(64) NOT NULL,
 ServiceID	varchar(16),
 Key	varchar(64)	NOT NULL,
 Value_EN varchar(128) NOT NULL,
 Value_CY varchar(128),
 HintText_EN	varchar(512),
 HintText_CY	varchar(512),
 Lov_Order BIGINT,
 ParentCategory varchar(64),
 ParentKey	varchar(64),
 Active varchar(1)
 );

 INSERT INTO public.panel_member_type (categorykey,serviceid,"key",value_en,value_cy,hinttext_en,hinttext_cy,lov_order,parentcategory,parentkey,active) VALUES
 	 ('PanelMemberType','BBA3','BBA3-DQPM','Disability Qualified Panel Member','','','',NULL,'','','Y'),
 	 ('PanelMemberType','BBA3','BBA3-MQPM1','Medically Qualified Panel Member','','','',NULL,'','','Y'),
 	 ('PanelMemberType','BBA3','BBA3-MQPM2','Medically Qualified Panel Member','','','',NULL,'','','Y'),
 	 ('PanelMemberType','BBA3','BBA3-FQPM','Financially Qualified Panel Member','','','',NULL,'','','Y'),
 	 ('PanelMemberType','BBA3','BBA3-RMM','Regional Medical Member','','','',NULL,'','','Y'),
 	 ('PanelMemberSpecialism','BBA3','BBA3-MQPM1-001','Cardiologist','','','',NULL,'PanelMemberType','BBA3-MQPM1','Y'),
 	 ('PanelMemberSpecialism','BBA3','BBA3-MQPM1-002','Carer','','','',NULL,'PanelMemberType','BBA3-MQPM1','Y'),
 	 ('PanelMemberSpecialism','BBA3','BBA3-MQPM1-003','Eye Surgeon','','','',NULL,'PanelMemberType','BBA3-MQPM1','Y'),
 	 ('PanelMemberSpecialism','BBA3','BBA3-MQPM1-004','General Practitioner','','','',NULL,'PanelMemberType','BBA3-MQPM1','Y'),
 	 ('PanelMemberSpecialism','BBA3','BBA3-MQPM2-001','Cardiologist','','','',NULL,'PanelMemberType','BBA3-MQPM2','Y'),
 	 ('PanelMemberSpecialism','BBA3','BBA3-MQPM2-002','Carer','','','',NULL,'PanelMemberType','BBA3-MQPM2','Y'),
 	 ('PanelMemberSpecialism','BBA3','BBA3-MQPM2-003','Eye Surgeon','','','',NULL,'PanelMemberType','BBA3-MQPM2','Y'),
 	 ('PanelMemberSpecialism','BBA3','BBA3-MQPM2-004','General Practitioner','','','',NULL,'PanelMemberType','BBA3-MQPM2','Y');
