CREATE TABLE cancellation_reasons (
	categorykey varchar(64) NOT NULL,
	serviceid varchar(16),
	key varchar(64) NOT NULL,
	value_en varchar(128) NOT NULL,
	value_cy varchar(128),
	hinttext_en varchar(512),
	hinttext_cy varchar(512),
	lov_order bigint,
	parentcategory varchar(64),
	parentkey varchar(64),
	active varchar(1)
);


INSERT INTO cancellation_reasons (categorykey,serviceid,"key",value_en,value_cy,hinttext_en,hinttext_cy,lov_order,parentcategory,parentkey,active) VALUES
	 ('CancellationReason','BBA3','withdrawn','Withdrawn',NULL,NULL,NULL,NULL,NULL,NULL,'Y'),
	 ('CancellationReason','BBA3','struckOut','Struck Out',NULL,NULL,NULL,NULL,NULL,NULL,'Y'),
	 ('CancellationReason','BBA3','partyUnableToAttend','Party Unable To Attend',NULL,NULL,NULL,NULL,NULL,NULL,'Y'),
	 ('CancellationReason','BBA3','exclusion','Exclusion',NULL,NULL,NULL,NULL,NULL,NULL,'Y'),
	 ('CancellationReason','BBA3','incompleteTribunal','Incomplete Tribunal',NULL,NULL,NULL,NULL,NULL,NULL,'Y'),
	 ('CancellationReason','BBA3','listedInError','Listed In error',NULL,NULL,NULL,NULL,NULL,NULL,'Y'),
	 ('CancellationReason','BBA3','other','Other',NULL,NULL,NULL,NULL,NULL,NULL,'Y');

