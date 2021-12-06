create table flag_details(
	id bigint,
	flag_code  varchar(16) NOT NULL,
	value_en text NOT NULL,
	value_cy text,
	category_id bigint NOT NULL ,

	constraint flag_details_pk primary key (flag_code),
	constraint id_unique unique (id)
);
