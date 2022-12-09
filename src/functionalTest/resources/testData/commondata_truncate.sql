truncate flag_details cascade;
truncate List_Of_Values;
truncate dataload_schedular_audit;
truncate dataload_exception_records;

INSERT INTO flag_details (id,flag_code,value_en,value_cy,category_id,mrd_created_time,mrd_updated_time,mrd_deleted_time) VALUES
	 (1,'CF0001','Case','',0,'07-04-2022 12:43:00','17-06-2022 13:33:00',NULL),
	 (2,'PF0001','Party','',0,'07-04-2022 12:43:00','17-06-2022 13:33:00',NULL),
	 (3,'SM0001','Special measure','',2,'07-04-2022 12:43:00','17-06-2022 13:33:00',NULL),
	 (4,'RA0001','Reasonable adjustment','',2,'07-04-2022 12:43:00','17-06-2022 13:33:00',NULL),
	 (5,'RA0002','I need documents in an alternative format','',4,'07-04-2022 12:43:00','17-06-2022 13:33:00',NULL),
	 (6,'RA0003','I need help with forms','',4,'07-04-2022 12:43:00','17-06-2022 13:33:00',NULL),
	 (7,'RA0004','I need adjustments to get to, into and around our buildings','',4,'07-04-2022 12:43:00','17-06-2022 13:33:00',NULL),
	 (8,'RA0005','I need to bring support with me to a hearing','',4,'07-04-2022 12:43:00','17-06-2022 13:33:00',NULL),
	 (9,'RA0006','I need something to feel comfortable during my hearing','',4,'07-04-2022 12:43:00','17-06-2022 13:33:00',NULL),
	 (10,'RA0007','I need to request a certain type of hearing','',4,'07-04-2022 12:43:00','17-06-2022 13:33:00',NULL),
	 (11,'RA0008','I need help communicating and understanding','',4,'07-04-2022 12:43:00','17-06-2022 13:33:00',NULL),
	 (12,'RA0009','I need an Hearing Enhancement System (Hearing/Induction Loop, Infrared Receiver)','',11,'07-04-2022 12:43:00','17-06-2022 13:33:00',NULL),
	 (13,'RA0010','Documents in a specified colour','',5,'07-04-2022 12:43:00','17-06-2022 13:33:00',NULL),
	 (14,'RA0011','Documents in easy read format','',5,'07-04-2022 12:43:00','17-06-2022 13:33:00',NULL),
	 (15,'RA0012','Braille documents','',5,'07-04-2022 12:43:00','17-06-2022 13:33:00',NULL),
	 (16,'RA0013','Documents in large print','',5,'07-04-2022 12:43:00','17-06-2022 13:33:00',NULL),
	 (17,'RA0014','Audio translation of documents','',5,'07-04-2022 12:43:00','17-06-2022 13:33:00',NULL),
	 (18,'RA0015','Documents read out to me','',5,'07-04-2022 12:43:00','17-06-2022 13:33:00',NULL),
	 (19,'RA0016','Information emailed to me','',5,'07-04-2022 12:43:00','17-06-2022 13:33:00',NULL),
	 (20,'RA0017','Guidance on how to complete forms','',6,'07-04-2022 12:43:00','17-06-2022 13:33:00',NULL);
commit;
