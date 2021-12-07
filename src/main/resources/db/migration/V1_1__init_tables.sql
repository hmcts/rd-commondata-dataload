create table flag_details(
	id bigint,
	flag_code  varchar(16) NOT NULL,
	value_en text NOT NULL,
	value_cy text,
	category_id bigint NOT NULL ,

	constraint flag_details_pk primary key (flag_code),
	constraint id_unique unique (id)
);

create table flag_service(
	id bigint NOT NULL,
	service_id varchar(16),
	hearing_relevant boolean NOT NULL,
	request_reason boolean NOT NULL,
	flag_code varchar(16),
	constraint flag_service_pk primary key (id));

alter table flag_service add CONSTRAINT flag_service_flag_code_fk FOREIGN KEY (flag_code)
REFERENCES flag_details (flag_code);
