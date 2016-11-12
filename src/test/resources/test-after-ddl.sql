INSERT INTO entities(added_id, id, updated, body)
	VALUES (99999,
			'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
			'2004-10-19 10:23:54',
			decode('6973426162615961676154686555676C696573743A74727565', 'hex'));	--	isBabaYagaTheUgliest:true in US_ASCII

--	for IndexRepositorySpec
CREATE TABLE index_cars_on_type_and_active(
	type TEXT NOT NULL,
	active BOOL NOT NULL,
	id UUID NOT NULL UNIQUE,
	PRIMARY KEY(type, active, id)
	);

INSERT INTO index_cars_on_type_and_active(type, active, id)
	VALUES('Syrena Sport', TRUE, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22');
