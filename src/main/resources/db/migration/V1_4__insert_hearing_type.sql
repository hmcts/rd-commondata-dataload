CREATE TABLE hearing_type (
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


INSERT INTO hearing_type (CategoryKey,ServiceID,Key,Value_EN,Value_CY,HintText_EN,HintText_CY,Lov_Order,ParentCategory,ParentKey,Active)
VALUES ('HearingType', 'BBA3', 'BBA3-substantive', 'Substantive', null, null , null, 1, null, null, 'Y'),
('HearingType', 'BBA3', 'BBA3-directionHearings', 'Direction Hearings', null, null, null, 2, null, null, 'Y'),
('HearingType', 'BBA3', 'BBA3-chambersOutcome', 'Chambers Outcome', null, null, null, 3, null, null, 'Y');
