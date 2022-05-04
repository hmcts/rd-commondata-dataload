delete from flag_details;

INSERT INTO flag_details (id,flag_code,value_en,value_cy,category_id,mrd_created_time,mrd_updated_time,mrd_deleted_time) VALUES
	 (1,'CF0001','Case','',0,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (2,'PF0001','Party','',0,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (3,'SM0001','Special measure','',2,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (4,'RA0001','Reasonable adjustment','',2,'2022-03-05 14:33:00','2022-03-05 14:33:00',NULL),
	 (5,'RA0002','I need documents in an alternative format','',4,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (6,'RA0003','I need help with forms','',4,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (7,'RA0004','I need adjustments to get to, into and around our buildings','',4,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (8,'RA0005','I need to bring support with me to a hearing','',4,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (9,'RA0006','I need something to feel comfortable during my hearing','',4,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (10,'RA0007','I need to request a certain type of hearing','',4,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL);
INSERT INTO flag_details (id,flag_code,value_en,value_cy,category_id,mrd_created_time,mrd_updated_time,mrd_deleted_time) VALUES
	 (11,'RA0008','I need help communicating and understanding','',4,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (12,'RA0009','I need an Hearing Enhancement System (Hearing/Induction Loop, Infrared Receiver)','',11,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (13,'RA0010','Documents in a specified colour','',5,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (14,'RA0011','Documents in easy read format','',5,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (15,'RA0012','Braille documents','',5,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (16,'RA0013','Documents in large print','',5,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (17,'RA0014','Audio translation of documents','',5,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (18,'RA0015','Documents read out to me','',5,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (19,'RA0016','Information emailed to me','',5,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (20,'RA0017','Guidance on how to complete forms','',6,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL);
INSERT INTO flag_details (id,flag_code,value_en,value_cy,category_id,mrd_created_time,mrd_updated_time,mrd_deleted_time) VALUES
	 (21,'RA0018','Support filling in forms','',6,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (22,'RA0019','Step free / wheelchair access','',7,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (23,'RA0020','Use of venue wheelchair','',7,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (24,'RA0021','Parking space close to the venue','',7,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (25,'RA0022','Accessible toilet','',7,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (26,'RA0023','Help using a lift','',7,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (27,'RA0024','A different type of chair','',7,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (28,'RA0025','Guiding in the building','',7,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (29,'RA0026','Support worker or carer with me','',8,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (30,'RA0027','Friend or family with me','',8,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL);
INSERT INTO flag_details (id,flag_code,value_en,value_cy,category_id,mrd_created_time,mrd_updated_time,mrd_deleted_time) VALUES
	 (31,'RA0028','Assistance / guide dog','',8,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (32,'RA0029','Therapy animal','',8,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (33,'RA0030','Appropriate lighting','',9,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (34,'RA0031','Regular breaks','',9,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (35,'RA0032','Space to be able to get up and move around','',9,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (36,'RA0033','Private waiting area','',9,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (37,'RA0034','In person hearing','',10,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (38,'RA0035','Video hearing','',10,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (39,'RA0036','Phone hearing','',10,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (40,'RA0037','Extra time to think and explain myself','',11,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL);
INSERT INTO flag_details (id,flag_code,value_en,value_cy,category_id,mrd_created_time,mrd_updated_time,mrd_deleted_time) VALUES
	 (41,'RA0038','Intermediary','',11,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (42,'RA0039','Speech to text reporter (palantypist)','',11,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (43,'RA0040','Need to be close to who is speaking','',11,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (44,'RA0041','Lip speaker','',11,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (45,'RA0042','Sign Language Interpreter','',11,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (46,'RA0043','Hearing loop (hearing enhancement system)','',12,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (47,'RA0044','Infrared receiver (hearing enhancement system)','',12,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (48,'RA0045','Induction loop (hearing enhancement system)','',12,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (49,'RA0046','Visit to court or tribunal before the hearing','',11,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (50,'RA0047','Explanation of the court and who''s in the room at the hearing','',11,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL);
INSERT INTO flag_details (id,flag_code,value_en,value_cy,category_id,mrd_created_time,mrd_updated_time,mrd_deleted_time) VALUES
	 (51,'CF0002','Complex Case','',1,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (52,'PF0002','Vulnerable user','',2,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (53,'PF0004','Confidential party/address','',2,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (54,'PF0007','Unacceptable/disruptive customer behaviour','',2,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (55,'PF0008','Vexatious litigant','',2,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (56,'PF0009','Civil restraint order','',2,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (57,'PF0011','Banning order','',2,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (58,'PF0012','Foreign national offender','',2,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (59,'PF0013','Unaccompanied minor','',2,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (60,'CF0003','Potentially harmful evidence','',1,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL);
INSERT INTO flag_details (id,flag_code,value_en,value_cy,category_id,mrd_created_time,mrd_updated_time,mrd_deleted_time) VALUES
	 (61,'CF0004','Gender recognition','',1,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (62,'CF0005','Domestic abuse allegation','',1,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (63,'CF0006','Potential fraud','',1,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (64,'CF0007','Urgent case','',1,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (65,'CF0008','Power of arrest with Police','',1,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (66,'PF0014','Audio/Video Evidence','',2,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (67,'PF0015','Language Interpreter','',2,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (68,'CF0009','Warrant of arrest conclusion/withdrawn','',1,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (69,'PF0016','Death of a Party','',2,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (70,'PF0017','Litigation friend','',2,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL);
INSERT INTO flag_details (id,flag_code,value_en,value_cy,category_id,mrd_created_time,mrd_updated_time,mrd_deleted_time) VALUES
	 (71,'PF0018','Lacking capacity','',2,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (72,'CF0010','Class appeal','',1,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (73,'CF0011','Presidential panel','',1,'2022-03-05 14:33:00','2022-03-05 14:33:00',NULL),
	 (74,'PF0019','Detained individual','',2,'2022-07-04 12:43:00','2022-03-05 14:28:00',NULL),
	 (75,'SM0002','Screening witness from accused','',3,'2022-03-05 14:33:00','2022-03-05 14:33:00',NULL),
	 (76,'SM0003','Evidence by live link','',3,'2022-03-05 13:07:00','2022-03-05 13:07:00',NULL),
	 (77,'SM0004','Evidence given in private','',3,'2022-03-05 14:33:00','2022-03-05 14:33:00',NULL),
	 (78,'SM0005','Removal of wigs and gowns','',3,'2022-03-05 13:07:00','2022-03-05 13:07:00',NULL),
	 (79,'SM0006','Video recorded evidence in chief','',3,'2022-03-05 14:33:00','2022-03-05 14:33:00',NULL),
	 (80,'SM0007','Video recorded cross-examination or re-examination','',3,'2022-03-05 13:07:00','2022-03-05 13:07:00',NULL);
INSERT INTO flag_details (id,flag_code,value_en,value_cy,category_id,mrd_created_time,mrd_updated_time,mrd_deleted_time) VALUES
	 (81,'CF0012','RRO (Restricted Reporting Order / Anonymisation)','',1,'2022-03-05 14:33:00','2022-03-05 14:33:00',NULL),
	 (82,'CF0013','Closed material','',1,'2022-03-05 14:33:00','2022-03-05 14:33:00',NULL);
