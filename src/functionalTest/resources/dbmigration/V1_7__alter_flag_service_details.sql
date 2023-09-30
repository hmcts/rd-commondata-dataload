ALTER TABLE flag_service ADD COLUMN default_status VARCHAR(64) not null default 'Active';
ALTER TABLE flag_service ADD COLUMN available_externally Boolean not null default False;
