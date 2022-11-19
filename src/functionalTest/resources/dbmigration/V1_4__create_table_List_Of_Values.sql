CREATE TABLE List_Of_Values (
 categorykey varchar(64) NOT NULL,
 serviceid varchar(16),
 key varchar(64) NOT NULL UNIQUE,
 value_en varchar(128) NOT NULL,
 value_cy varchar(128),
 hinttext_en varchar(512),
 hinttext_cy varchar(512),
 lov_order bigint,
 parentcategory varchar(64),
 parentkey varchar(64),
 active varchar(1)
);
