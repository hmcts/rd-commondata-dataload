create table flag_service(
	id bigint NOT NULL,
	service_id varchar(16),
	hearing_relevant boolean NOT NULL,
	request_reason boolean NOT NULL,
	flag_code varchar(16),
	constraint flag_service_pk primary key (id));

alter table flag_service add CONSTRAINT flag_service_flag_code_fk FOREIGN KEY (flag_code)
REFERENCES flag_details (flag_code);

CREATE TABLE dataload_exception_records(
 id SERIAL NOT NULL,
 table_Name varchar(64),
 scheduler_name varchar(64) NOT NULL,
 scheduler_start_time timestamp NOT NULL,
 key varchar(256),
 field_in_error varchar(256),
 error_description varchar(512),
 updated_timestamp timestamp NOT NULL,
 row_id bigint,
 CONSTRAINT dataload_exception_records_pk PRIMARY KEY (ID)
);

CREATE TABLE dataload_schedular_audit(
  id serial NOT NULL,
  scheduler_name varchar(64) NOT NULL,
  file_name varchar(128),
  scheduler_start_time timestamp NOT NULL,
  scheduler_end_time timestamp,
  status varchar(32),
  CONSTRAINT dataload_schedular_audit_pk PRIMARY KEY (id)
);






