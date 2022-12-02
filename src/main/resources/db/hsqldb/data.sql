-- One admin user, named admin1 with password 4dm1n and authority admin
INSERT INTO users(username,password,enabled) VALUES ('admin1','4dm1n',TRUE);
INSERT INTO authorities(id,username,authority) VALUES (1,'admin1','admin');
INSERT INTO jugador VALUES(1,'admin','admin','00:05:00','00:05:00',0,0,0,0,0,0,'00:00:00','admin1');

-- One jugador user, named mario with passwor mario
INSERT INTO users(username,password,enabled) VALUES ('marsannar2','mario',TRUE);
INSERT INTO authorities(id,username,authority) VALUES (8,'marsannar2','jugador');
INSERT INTO jugador VALUES(7,'mario','sanchez','00:05:00','00:05:00',2,3,6,7,5,7,'23:00:53','marsannar2');


INSERT INTO users (username,password,enabled) VALUES ('aaa','aaa',TRUE);
INSERT INTO authorities(id,username,authority) VALUES (10,'aaa','jugador');
INSERT INTO jugador VALUES(2,'aaa','aaa','00:05:00','00:05:00',2,3,6,7,5,7,'23:00:53','aaa');

-- One jugador jorsilman, named jorge with password jorge
INSERT INTO users(username,password,enabled) VALUES ('jorge','jorge',TRUE);
INSERT INTO authorities(id,username,authority) VALUES (4,'jorge','jugador');
INSERT INTO jugador VALUES(3,'jorge','sillero','00:05:00','00:05:00',2,3,6,7,5,7,'23:00:53','jorge');

-- One jugador barbaat, named barba with password barba
INSERT INTO users(username,password,enabled) VALUES ('barba','barba',TRUE);
INSERT INTO authorities(id,username,authority) VALUES (5,'barba','jugador');
INSERT INTO jugador VALUES(4,'barba','aaa','00:05:00','00:05:00',0,0,0,0,0,0,'23:00:53','barba');

-- One jugador fracaralb, named fran with passwor fran
INSERT INTO users(username,password,enabled) VALUES ('fran','fran',TRUE);
INSERT INTO authorities(id,username,authority) VALUES (6,'fran','jugador');
INSERT INTO jugador VALUES(5,'aaa','aaa','00:05:00','00:05:00',2,3,6,7,5,7,'23:00:53','fran');

-- One jugador albgalhue, named gallego with password gallego
INSERT INTO users(username,password,enabled) VALUES ('gallego','gallego',TRUE);
INSERT INTO authorities(id,username,authority) VALUES (7,'gallego','jugador');
INSERT INTO jugador VALUES(6,'aaa','aaa','00:05:00','00:05:00',2,3,6,7,5,7,'23:00:53','gallego');

-- One jugador alvnavriv, named alvaro with password alvaro
INSERT INTO users(username,password,enabled) VALUES ('alvaro','alvaro',TRUE);
INSERT INTO authorities(id,username,authority) VALUES (11,'alvaro','jugador');
INSERT INTO jugador VALUES(8,'aaa','aaa','00:05:00','00:05:00',2,3,6,7,5,7,'23:00:53','alvaro');


--INSERT INTO partida(id,momento_inicio,momento_fin,num_movimientos,victoria,jugador_id) VALUES (1,'2022-11-28 19:36:30','2022-11-28 19:40:00',0,true,7);
--INSERT INTO partida(id,momento_inicio,momento_fin,num_movimientos,victoria,jugador_id) VALUES (3,'2022-11-28 19:36:30','2022-11-28 20:10:00',100,true,4);
--INSERT INTO partida(id,momento_inicio,num_movimientos,victoria,jugador_id) VALUES (2,'2022-11-28 19:36:30',0,true,6);

