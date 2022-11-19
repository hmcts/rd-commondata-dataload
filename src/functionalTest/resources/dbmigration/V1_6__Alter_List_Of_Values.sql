ALTER TABLE List_Of_Values
DROP CONSTRAINT list_of_values_key_key;


ALTER TABLE List_Of_Values
ADD CONSTRAINT compKey UNIQUE (categorykey,key,serviceid);

INSERT INTO List_Of_Values (CategoryKey,ServiceID,Key,Value_EN,Value_CY,HintText_EN,HintText_CY,Lov_Order,ParentCategory,ParentKey,Active)
VALUES ('HearingChannel','BBA3','telephone','Telephone',null,null,null,2,null,null,'Y'),
('HearingChannel','BBA3','video','Video',null,null,null,3,null,null,'Y'),
('HearingChannel','BBA3','faceToFace','Face To Face',null,null,null,1,null,null,'Y'),
('HearingChannel','BBA3','notAttending','Not Attending',null,null,null,4,null,null,'Y'),
('HearingSubChannel','BBA3','telephone-btMeetMe','Telephone - BTMeetme',null,null,null,null,'HearingChannel','telephone','Y'),
('ListingStatus','','test','test',null,null,null,null,null,null,'Y'),
('EmptySubCategory','','test','test',null,null,null,null,null,null,'Y'),
('ListingStatusSubChannel','','test','test',null,null,null,null,'ListingStatus','test','Y');

