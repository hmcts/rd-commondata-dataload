CREATE TABLE hearing_channel (
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

INSERT INTO hearing_channel (CategoryKey,ServiceID,Key,Value_EN,Value_CY,HintText_EN,HintText_CY,Lov_Order,ParentCategory,ParentKey,Active)
VALUES ('HearingChannel','BBA3','telephone','Telephone',null,null,null,2,null,null,'Y'),
('HearingChannel','BBA3','video','Video',null,null,null,3,null,null,'Y'),
('HearingChannel','BBA3','faceToFace','Face To Face',null,null,null,1,null,null,'Y'),
('HearingChannel','BBA3','notAttending','Not Attending',null,null,null,4,null,null,'Y'),
('HearingSubChannel','BBA3','telephone-btMeetMe','Telephone - BTMeetme',null,null,null,null,'HearingChannel','telephone','Y'),
('HearingSubChannel','BBA3','telephone-CVP','Telephone - CVP',null,null,null,null,'HearingChannel','telephone','Y'),
('HearingSubChannel','BBA3','telephone-other','Telephone - Other',null,null,null,null,'HearingChannel','telephone','Y'),
('HearingSubChannel','BBA3','telephone-skype','Telephone - Skype',null,null,null,null,'HearingChannel','telephone','Y'),
('HearingSubChannel','BBA3','video-cvp','Video - CVP',null,null,null,null,'HearingChannel','video','Y'),
('HearingSubChannel','BBA3','video-conference','Video Conference',null,null,null,null,'HearingChannel','video','Y'),
('HearingSubChannel','BBA3','video-other','Video - Other',null,null,null,null,'HearingChannel','video','Y'),
('HearingSubChannel','BBA3','video-skype','Video - Skype',null,null,null,null,'HearingChannel','video','Y'),
('HearingSubChannel','BBA3','video-teams','Video - Teams',null,null,null,null,'HearingChannel','video','Y');
