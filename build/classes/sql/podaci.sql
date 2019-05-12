INSERT INTO poslovnica (Adresa, Telefon) VALUES ('Carice Milice 3, Doboj', '+387 52 321 456');
INSERT INTO poslovnica (Adresa, Telefon) VALUES ('Stevana Sremca 4, Kostajnica', '+387 66 313 789');
INSERT INTO poslovnica (Adresa, Telefon) VALUES ('Kneza Lazara 18, Banja Luka', '+387 52 123 456');

INSERT INTO zaposleni VALUES ('1603994162519', 'Marko', 'Stijak', 'Stevana Sremca 4, Kostajnica', '+38766 313 789', 850.00);
INSERT INTO zaposleni VALUES ('1865994546215', 'Nenad', 'Golubovic', 'Nikole Tesle 18, Banja Luka', '+38765 485 987', 850.00);
INSERT INTO zaposleni VALUES ('1505994123548', 'Nemanja', 'Lukić', 'Kneza Lazara 26, Doboj', '+38765 471 256', 850.00);
INSERT INTO zaposleni VALUES ('1605996147854', 'Slaviša', 'Borojević', 'Stevana Sremca 18, Kostajnica', '+38765 789 456', 850.00);
INSERT INTO zaposleni VALUES ('2602992156314', 'Nikola', 'Pilipović', 'Miloša Obilića 74, Banja Luka', '+38766 452 152', 850.00);
INSERT INTO zaposleni VALUES ('2207990123569', 'Petar', 'Radojčić', 'Svetosavska 16, Doboj', '+38765 477 456', 850.00);

INSERT INTO bankovni_racun SET BrojRacuna = '5589654752000235456548';
INSERT INTO racun_zaposlenog VALUES ('5589654752000235456548', '1603994162519');
INSERT INTO bankovni_racun SET BrojRacuna = '5589654752000245648489';
INSERT INTO racun_zaposlenog VALUES ('5589654752000245648489', '1865994546215');
INSERT INTO bankovni_racun SET BrojRacuna = '5589654865645116565165';
INSERT INTO racun_zaposlenog VALUES ('5589654865645116565165', '1505994123548');
INSERT INTO bankovni_racun SET BrojRacuna = '5546546854684684684684';
INSERT INTO racun_zaposlenog VALUES ('5546546854684684684684', '1605996147854');
INSERT INTO bankovni_racun SET BrojRacuna = '5589664846846835165465';
INSERT INTO racun_zaposlenog VALUES ('5589664846846835165465', '2602992156314');
INSERT INTO bankovni_racun SET BrojRacuna = '5589654646845328646534';
INSERT INTO racun_zaposlenog VALUES ('5589654646845328646534', '2207990123569');

INSERT INTO ugovor_posao VALUES ('1603994162519', 1, '2016-05-05', null);
INSERT INTO ugovor_posao VALUES ('1865994546215', 2, '2016-05-05', '2019-02-15');
INSERT INTO ugovor_posao VALUES ('1505994123548', 3, '2016-05-05', '2020-08-25');
INSERT INTO ugovor_posao VALUES ('1605996147854', 1, '2016-05-05', null);
INSERT INTO ugovor_posao VALUES ('2602992156314', 2, '2016-05-05', '2017-11-16');
INSERT INTO ugovor_posao VALUES ('2207990123569', 3, '2016-05-05', null);

INSERT INTO bankovni_racun
SET BrojRacuna = '4584864531468548563515';
INSERT INTO pravno_lice (BrojRacuna, Naziv) VALUES ('4584864531468548563515', 'Bekament d.o.o');
INSERT INTO dobavljac VALUE (last_insert_id());
INSERT INTO bankovni_racun
SET BrojRacuna = '4584515165186155484684';
INSERT INTO pravno_lice (BrojRacuna, Naziv) VALUES ('4584515165186155484684','Jub d.o.o' );
INSERT INTO dobavljac VALUE (last_insert_id());
INSERT INTO bankovni_racun
SET BrojRacuna = '4486445318468153135655';
INSERT INTO pravno_lice (BrojRacuna, Naziv) VALUES ('4486445318468153135655', 'Chromos Svijetolos d.o.o');
INSERT INTO dobavljac VALUE (last_insert_id());
INSERT INTO bankovni_racun
SET BrojRacuna = '4464846854168168541653';
INSERT INTO pravno_lice (BrojRacuna, Naziv) VALUES ('4464846854168168541653', 'Duga Tehna d.o.o');
INSERT INTO dobavljac VALUE (last_insert_id());
INSERT INTO bankovni_racun
SET BrojRacuna = '4789654544546634685554';
INSERT INTO pravno_lice (BrojRacuna, Naziv) VALUES ('4789654544546634685554', 'Beorol d.o.o');
INSERT INTO dobavljac VALUE (last_insert_id());
INSERT INTO bankovni_racun
SET BrojRacuna = '4745566816818168161651';
INSERT INTO pravno_lice (BrojRacuna, Naziv) VALUES ('4745566816818168161651', 'Hemmax d.o.o');
INSERT INTO dobavljac VALUE (last_insert_id());
INSERT INTO bankovni_racun
SET BrojRacuna = '4321564894646548946115';
INSERT INTO pravno_lice (BrojRacuna, Naziv) VALUES ('4321564894646548946115', 'HB Body d.o.o');
INSERT INTO dobavljac VALUE (last_insert_id());
INSERT INTO bankovni_racun
SET BrojRacuna = '4323334894646548946115';
INSERT INTO pravno_lice (BrojRacuna, Naziv) VALUES ('4323334894646548946115', 'Keramika Modus');
INSERT INTO dobavljac VALUE (last_insert_id());

INSERT INTO proizvodjac (Naziv) VALUES ('Bekament');
INSERT INTO proizvodjac (Naziv) VALUES ('Keramika Modus');
INSERT INTO proizvodjac (Naziv) VALUES ('HB Body');
INSERT INTO proizvodjac (Naziv) VALUES ('Hemmax d.o.o');
INSERT INTO proizvodjac (Naziv) VALUES ('Beorol');
INSERT INTO proizvodjac (Naziv) VALUES ('Duga Tehna');
INSERT INTO proizvodjac (Naziv) VALUES ('Chromos Svijetolos');
INSERT INTO proizvodjac (Naziv) VALUES ('Jub');

INSERT INTO bankovni_racun (BrojRacuna) VALUES ('3548615155131516511681');
INSERT INTO racun_poslovnice (BrojRacuna, IdPoslovnice) VALUES ('3548615155131516511681', 1);
INSERT INTO bankovni_racun (BrojRacuna) VALUES ('3568948615168516516511');
INSERT INTO racun_poslovnice (BrojRacuna, IdPoslovnice) VALUES ('3568948615168516516511', 2);
INSERT INTO bankovni_racun (BrojRacuna) VALUES ('4846516531651651351551');
INSERT INTO racun_poslovnice (BrojRacuna, IdPoslovnice) VALUES ('4846516531651651351551', 3);
INSERT INTO bankovni_racun (BrojRacuna) VALUES ('6543219876543168891655');
INSERT INTO racun_poslovnice (BrojRacuna, IdPoslovnice) VALUES ('6543219876543168891655', 1);
INSERT INTO bankovni_racun (BrojRacuna) VALUES ('4849651354165341653416');
INSERT INTO racun_poslovnice (BrojRacuna, IdPoslovnice) VALUES ('4849651354165341653416', 2);
INSERT INTO bankovni_racun (BrojRacuna) VALUES ('6543198186468484984646');
INSERT INTO racun_poslovnice (BrojRacuna, IdPoslovnice) VALUES ('6543198186468484984646', 3);


INSERT INTO tip VALUE('Vanjske');
INSERT INTO tip VALUE('Unutrasnje');
INSERT INTO tip VALUE('Drvo');
INSERT INTO tip VALUE('Metal');
INSERT INTO tip VALUE('Beton');

INSERT INTO bankovni_racun
SET BrojRacuna = '4584864531468548563111';
INSERT INTO pravno_lice (BrojRacuna, Naziv) VALUES ('4584864531468548563111', 'Građ promet d.o.o');
INSERT INTO kupac VALUE (last_insert_id());
INSERT INTO bankovni_racun
SET BrojRacuna = '4584515165186155484222';
INSERT INTO pravno_lice (BrojRacuna, Naziv) VALUES ('4584515165186155484222','Proda Mont d.o.o' );
INSERT INTO kupac VALUE (last_insert_id());
INSERT INTO bankovni_racun
SET BrojRacuna = '4486445318468153135333';
INSERT INTO pravno_lice (BrojRacuna, Naziv) VALUES ('4486445318468153135333', 'DBMAX d.o.o');
INSERT INTO kupac VALUE (last_insert_id());
INSERT INTO bankovni_racun
SET BrojRacuna = '4464846854168168541444';
INSERT INTO pravno_lice (BrojRacuna, Naziv) VALUES ('4464846854168168541444', 'NIK-Gradnje d.o.o');
INSERT INTO kupac VALUE (last_insert_id());


INSERT INTO faktura_nabavka (IdPoslovnice, ID, DatumNabavke) VALUES (1, 1, current_date);

INSERT INTO artikl VALUES (NULL, 'Nesto', 19.5,1,'farba1');
INSERT INTO farba VALUES (last_insert_id(), 'Drvo','bijela','1kg','1l');
INSERT INTO stavka_nabavke VALUES (last_insert_id(),10,15,1);
