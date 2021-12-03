CREATE TABLE flag_details (
id int8 NOT NULL,
flag_code varchar(16) NOT NULL,
value_en text NOT NULL,
value_cy text NULL,
category_id int8 NOT NULL,
CONSTRAINT flagdetails_pkey PRIMARY KEY (id)
);
