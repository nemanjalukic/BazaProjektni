DELIMITER $$
CREATE PROCEDURE getPoslovnice()
  BEGIN
    SELECT IdPoslovnice, concat('NemanjaBoje ', IdPoslovnice, ' - ', Adresa)
    FROM poslovnica
    ORDER BY IdPoslovnice ASC;
  END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE getArtikleIzSkladista(IN idP INTEGER)
  BEGIN
    SELECT Sifra, Naziv
    FROM artikl_u_skladistu p NATURAL JOIN artikl
    WHERE p.IdPoslovnice = idP;
  END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE getRacunPoslovnice(IN idP INTEGER)
  BEGIN
    SELECT BrojRacuna
    FROM racun_poslovnice p
    WHERE p.IdPoslovnice = idP;
  END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE getNazivArtikla(IN S INTEGER, OUT NazivArtikla VARCHAR(50))
  BEGIN
    SELECT Naziv
    INTO NazivArtikla
    FROM artikl p 
    WHERE p.Sifra = S;
  END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE getNazivCijenaArtikla(IN S INTEGER)
  BEGIN
    SELECT Naziv,Cijena
    FROM artikl p 
    WHERE p.Sifra = S;
  END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE getSifruArtikla(IN nArtikla VARCHAR(50), OUT S INTEGER)
  BEGIN
    SELECT Sifra
    INTO S
    FROM artikl NATURAL JOIN proizvodjac
    WHERE Naziv = nArtikla;
  END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE getgetNazivCijenaArtikla(S INTEGER)
  BEGIN
    SELECT Naziv, Cijena
    FROM artikl p
    WHERE p.Sifra = S;
  END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE getBrojProizvodaUSkladistu(IN S INTEGER, IN ID INTEGER, OUT stanje INTEGER)
  BEGIN
    SELECT Kolicina
    INTO stanje
    FROM artikl_u_skladistu p
    WHERE p.Sifra = S AND p.IdPoslovnice = ID;
  END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE provjeriSifru(IN s INTEGER, OUT result BOOLEAN)
  BEGIN
    SELECT count(Sifra) > 0
    INTO result
    FROM artikl_u_skladistu p
    WHERE p.Sifra = s;
  END $$
DELIMITER ;


DELIMITER $$
CREATE PROCEDURE getDostupnostProizvoda(IN S INTEGER)
  BEGIN
    SELECT IdPoslovnice, Adresa, Telefon, Kolicina
    FROM artikl_u_skladistu p NATURAL JOIN poslovnica
    WHERE p.Sifra = S
    ORDER BY IdPoslovnice ASC;
  END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE getPromet(IN p VARCHAR(50), IN promet VARCHAR(20))
  BEGIN
    DECLARE idP INTEGER DEFAULT 1;
    IF (p = '*')
    THEN
      CASE
        WHEN promet = 'Dnevni'
        THEN
          SELECT Datum, Iznos
          FROM dnevni_promet
          ORDER BY Datum DESC;
        WHEN promet = 'Mjesecni'
        THEN
          SELECT *
          FROM mjesecni_promet;
        WHEN promet = 'Godisnji'
        THEN
          SELECT *
          FROM godisnji_promet;
      END CASE;
    ELSE
      SELECT IdPoslovnice
      INTO idP
      FROM poslovnica
      WHERE concat('NemanjaBoje ', IdPoslovnice, ' - ', Adresa) = p;

      CASE
        WHEN promet = 'Dnevni'
        THEN
          SELECT Datum, Iznos
          FROM dnevni_promet d
          WHERE d.IdPoslovnice = idP
          ORDER BY Datum DESC;
        WHEN promet = 'Mjesecni'
        THEN
          SELECT concat(MONTHNAME(Datum), ' ', YEAR(Datum)) AS Mjesec, sum(Iznos)
          FROM dnevni_promet d
          WHERE d.IdPoslovnice = idP
          GROUP BY Mjesec
          ORDER BY Datum DESC;
        WHEN promet = 'Godisnji'
        THEN
          SELECT YEAR(Datum) AS Godina, sum(Iznos)
          FROM dnevni_promet d
          WHERE d.IdPoslovnice = idP
          GROUP BY Godina
          ORDER BY Godina DESC;
      END CASE;
    END IF;
  END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE getZaposleniInfo(IN i VARCHAR(50), p VARCHAR(50))
  BEGIN
    CASE
      WHEN i != '*' AND p = '*'
      THEN SELECT *
           FROM zaposlenje z
           WHERE z.ime = i;
      WHEN i = '*' AND p != '*'
      THEN SELECT *
           FROM zaposlenje z
           WHERE z.Prezime = p;
    ELSE
      SELECT *
      FROM zaposlenje;
    END CASE;
  END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE unosZaposlenog(IN jmbZ        CHAR(13), IN imeZ VARCHAR(20), IN prezimeZ VARCHAR(50),
                                IN adresaZ     VARCHAR(50), IN telefonZ VARCHAR(20),
                                IN plataZ      DECIMAL(19, 4), IN racunZaposlenog VARCHAR(50),
                                IN poslovnicaZ VARCHAR(50), IN datumOdZ DATE, IN datumDoZ DATE)
  BEGIN
    DECLARE idPz INTEGER;

    IF (exists(SELECT jmb
               FROM zaposleni z
               WHERE z.JMB = jmbZ))
    THEN
      SIGNAL SQLSTATE '01000'
      SET MESSAGE_TEXT = 'JMB već postoji!';
    ELSEIF (exists(SELECT BrojRacuna
                   FROM bankovni_racun b
                   WHERE b.BrojRacuna = racunZaposlenog))
      THEN
        SIGNAL SQLSTATE '01000'
        SET MESSAGE_TEXT = 'Broj računa već postoji!';
    ELSEIF (plataZ < 0)
      THEN
        SIGNAL SQLSTATE '01000'
        SET MESSAGE_TEXT = 'Plata ne može biti negativna!';
    ELSE
      SELECT IdPoslovnice
      INTO idPz
      FROM poslovnica
      WHERE concat('NemanjaBoje ', IdPoslovnice, ' - ', Adresa) = poslovnicaZ;

      INSERT INTO zaposleni (JMB, Ime, Prezime, Adresa, Telefon, Plata)
      VALUES (jmbZ, imeZ, prezimeZ, adresaZ, telefonZ, plataZ);
      INSERT INTO ugovor_posao (JMB, IdPoslovnice, DatumOd, DatumDo) VALUES (jmbZ, idPZ, datumOdZ, datumDoZ);
      INSERT INTO bankovni_racun (BrojRacuna) VALUE (racunZaposlenog);
      INSERT INTO racun_zaposlenog (BrojRacuna, JMB) VALUES (racunZaposlenog, jmbZ);
    END IF;
  END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE unosNovogRacuna(IN JMBz CHAR(13), IN brRacuna VARCHAR(50))
  BEGIN
    -- ako već postoji biće error
    INSERT INTO bankovni_racun VALUE (brRacuna);
    INSERT INTO racun_zaposlenog (BrojRacuna, JMB) VALUES (brRacuna, JMBz);
  END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE obrisiZaposlenog(IN JMBz CHAR(13))
  BEGIN
    DELETE FROM zaposleni
    WHERE JMB = JMBz;
  END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE getUgovorInfo(IN JMBz CHAR(13), OUT result VARCHAR(200))
  BEGIN
    DECLARE dateFrom DATE;
    DECLARE dateTo DATE;
    DECLARE brMjeseci, ostatak INTEGER;
    DECLARE temp VARCHAR(10);

    SELECT DatumOd, DatumDo
    INTO dateFrom, dateTo
    FROM ugovor_posao u
    WHERE u.JMB = JMBz
    ORDER BY DatumOd DESC
    LIMIT 1;

    IF (isnull(dateTo))
    THEN
      SET result = concat('Zaposleni je potpisao ugovor na neodređeno vrijeme. Datum potpisivanja je ', dateFrom, '.');
    ELSE
      SET brMjeseci = timestampdiff(MONTH, dateFrom, dateTo);
      SET ostatak = brMjeseci MOD 10;
      CASE
        WHEN ostatak = 1
        THEN SET temp = 'mjesec';
        WHEN ostatak = 2 OR ostatak = 3 OR ostatak = 4
        THEN SET temp = 'mjeseca';
      ELSE SET temp = 'mjeseci';
      END CASE;

      SET result = concat('Zaposleni je potpisao ugovor u trajanju od ', brMjeseci, ' ', temp,
                          '. Datum potpisivanja je ', dateFrom, '.');
    END IF;
  END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE stonirajRacun(IN brRacuna INTEGER, IN idP INTEGER)
  BEGIN

    DECLARE uIznos DECIMAL(19, 4) DEFAULT 0;
    DECLARE sif, kol INTEGER DEFAULT 0;
    DECLARE kraj BOOLEAN DEFAULT FALSE;
    DECLARE c CURSOR FOR SELECT Sifra, Kolicina
                         FROM stavka_racuna s
                         WHERE s.BrojRacuna = brRacuna;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET kraj = TRUE;

    SELECT UkupanIznos
    INTO uIznos
    FROM fiskalni_racun f
    WHERE f.BrojRacuna = brRacuna;

    -- smanji dnevni promet u poslovnici koja je stonirala račun i vratila kupcu novac

    IF (exists(SELECT Datum
               FROM dnevni_promet d
               WHERE d.Datum = current_date AND d.IdPoslovnice = idP))
    THEN
      UPDATE dnevni_promet d
      SET d.Iznos = d.Iznos - uIznos
      WHERE d.IdPoslovnice = idP AND d.Datum = current_date;
    ELSE
      INSERT INTO dnevni_promet (Datum, IdPoslovnice, Iznos) VALUES (current_date, idP, -uIznos);
    END IF;

    -- ažuriraj kolicinu ili ubaci proizvod u skladiste poslovnice koja je stonirala racun
    OPEN c;
    petlja: LOOP
      FETCH c
      INTO sif, kol;

      IF (kraj)
      THEN LEAVE petlja; END IF;

      INSERT INTO artikl_u_skladistu (Sifra, IdPoslovnice, Kolicina) VALUES (sif, idP, kol)
      ON DUPLICATE KEY UPDATE Kolicina = Kolicina + kol;

    END LOOP petlja;
    CLOSE c;


    -- izbaci stavke racuna
    DELETE FROM stavka_racuna
    WHERE BrojRacuna = brRacuna;
    
    IF (exists(SELECT BrojFakture
               FROM faktura_kupovina d
               WHERE d.BrojRacuna=brRacuna))
	THEN 
    DELETE FROM faktura_kupovina
    WHERE BrojRacuna = brRacuna;
    END IF;

    -- obrisi racun
    DELETE FROM fiskalni_racun
    WHERE BrojRacuna = brRacuna;

  END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE provjeriValidnostRacuna(IN brRacuna INTEGER, OUT result BOOLEAN)
  BEGIN
    SELECT count(brojRacuna) > 0
    INTO result
    FROM fiskalni_racun f
    WHERE f.BrojRacuna = brRacuna;
  END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE getStavkeRacuna(brRacuna INTEGER)
  BEGIN
    SELECT Sifra, Naziv, Kolicina, Cijena 
    FROM artikl p NATURAL JOIN stavka_racuna
    WHERE BrojRacuna = brRacuna;
  END $$
DELIMITER ;


DELIMITER $$
CREATE PROCEDURE getDobavljace()
  BEGIN
    SELECT Naziv,BrojRacuna
    FROM dobavljac d NATURAL JOIN pravno_lice;    
  END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE getKupce()
  BEGIN
    SELECT Naziv,BrojRacuna
    FROM kupac k NATURAL JOIN pravno_lice;    
  END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE provjeriDobavljaca(IN nazivD VARCHAR(50), OUT idD INTEGER)
  BEGIN
    SELECT ID
    INTO idD
    FROM pravno_lice d
    WHERE d.Naziv = nazivD;

  END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE getKupac(IN nazivD VARCHAR(50))
  BEGIN
    SELECT ID
    FROM pravno_lice d 
    WHERE d.Naziv = nazivD;

  END $$
DELIMITER ;


DELIMITER $$
CREATE PROCEDURE unosNoveNabavke(IN idP INTEGER, IN idD INTEGER, OUT brN INTEGER)
  BEGIN
    INSERT INTO faktura_nabavka (IdPoslovnice, ID, DatumNabavke, UkupanIznos)
    VALUES (idP, idD, current_date, 0);

    SELECT last_insert_id()
    INTO brN;
  END $$
DELIMITER 

DELIMITER $$
CREATE PROCEDURE provjeriArtikl(IN nP VARCHAR(50), IN nA VARCHAR(50), OUT idP INTEGER, OUT s INTEGER)
  BEGIN
    SELECT IdProizvodjaca
    INTO idP
    FROM proizvodjac p
    WHERE p.Naziv = nP;
    
	SELECT Sifra
	INTO s
	FROM artikl p
	WHERE p.Naziv= nA AND p.IdProizvodjaca = idP;
  END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE unosNanosenja(IN nacinN VARCHAR(50),IN sifraP INTEGER)
  BEGIN
    INSERT INTO nanosenje VALUES(nacinN,sifraP);
  END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE unosFasada(IN sifraP INTEGER, IN bojaP VARCHAR(50),IN potrosnjaP VARCHAR(50),IN pakovanjeP VARCHAR(50))
  BEGIN
    INSERT INTO fasada VALUES(sifraP,bojaP,potrosnjaP,pakovanjeP);
  END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE unosEnterijer(IN sifraP INTEGER, IN bojaP VARCHAR(50),IN potrosnjaP VARCHAR(50),IN pakovanjeP VARCHAR(50),IN datumDo DATE)
  BEGIN
    INSERT INTO enterijer VALUES(sifraP,bojaP,potrosnjaP,pakovanjeP,datumDo);
  END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE unosAuto(IN sifraP INTEGER, IN bojaP VARCHAR(50),IN potrosnjaP VARCHAR(50),IN pakovanjeP VARCHAR(50))
  BEGIN
    INSERT INTO auto VALUES(sifraP,bojaP,potrosnjaP,pakovanjeP);
  END $$
DELIMITER ;
DELIMITER $$
CREATE PROCEDURE unosFarba(IN sifraP INTEGER, IN tipP VARCHAR(50),IN bojaP VARCHAR(50),IN potrosnjaP VARCHAR(50),IN pakovanjeP VARCHAR(50))
  BEGIN
    INSERT INTO farba VALUES(sifraP,tipP,bojaP,potrosnjaP,pakovanjeP);
  END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE unosPlocice(IN sifraP INTEGER, IN tipP VARCHAR(50),IN dimenzijeP VARCHAR(50))
  BEGIN
    INSERT INTO plocice VALUES(sifraP,tipP,dimenzijeP);
  END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE getInfoZaNabavku(IN  brF    INTEGER, OUT id INTEGER, OUT adresaPoslovnice VARCHAR(50),
                                  OUT nazivD VARCHAR(50), OUT datum DATE, OUT uIznos DOUBLE, OUT uplaceno DOUBLE)
  BEGIN
    SELECT IdPoslovnice, Naziv, DatumNabavke, UkupanIznos
    INTO id, nazivD, datum, uIznos
    FROM faktura_nabavka n NATURAL JOIN pravno_lice
    WHERE n.BrojFakture = brF;

    SELECT Adresa
    INTO adresaPoslovnice
    FROM poslovnica p
    WHERE p.IdPoslovnice = id;

    SELECT sum(Iznos)
    INTO uplaceno
    FROM isplata_dobavljacu i
    WHERE i.BrojFakture = brF;

    IF (ISNULL(uplaceno))
    THEN SET uplaceno = 0; END IF;

    SELECT Sifra, Naziv, Kolicina, NabavnaCijena
    FROM stavka_nabavke s NATURAL JOIN  artikl
    WHERE s.BrojFakture = brF;
  END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE getInfoZaKupovinu(IN  brF    INTEGER, OUT idP INTEGER, OUT adresaPoslovnice VARCHAR(50),
                                  OUT nazivK VARCHAR(50), OUT datum DATE, OUT uIznos DOUBLE, OUT uplaceno DOUBLE)
  
  BEGIN
	DECLARE brR INTEGER;
    DECLARE idK INTEGER;
    SELECT IdPoslovnice, ID, DatumKupovine, BrojRacuna
    INTO idP,idK, datum,brR
    FROM faktura_kupovina n
    WHERE n.BrojFakture = brF;
    
    SELECT Naziv
    INTO nazivK
    FROM pravno_lice li
    WHERE li.ID=idK;
	
    SELECT UkupanIznos
    INTO uIznos
    FROM fiskalni_racun f
    WHERE f.BrojRacuna=brR;
    
    SELECT Adresa
    INTO adresaPoslovnice
    FROM poslovnica p
    WHERE p.IdPoslovnice = idP;

    SELECT sum(Iznos)
    INTO uplaceno
    FROM uplata_kupca i
    WHERE i.BrojFakture = brF;

    IF (ISNULL(uplaceno))
    THEN SET uplaceno = 0; END IF;

    SELECT Sifra, Naziv, Kolicina, Cijena
    FROM stavka_racuna s NATURAL JOIN  artikl
    WHERE s.BrojRacuna= brR;
  END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE provjeriValidnostBrojaFakture(IN brFakture INTEGER, OUT result BOOLEAN)
  BEGIN
    SELECT count(BrojFakture) > 0
    INTO result
    FROM faktura_nabavka n
    WHERE n.BrojFakture = brFakture;
  END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE provjeriValidnostBrojaFaktureKupovina(IN brFakture INTEGER, OUT result BOOLEAN)
  BEGIN
    SELECT count(BrojFakture) > 0
    INTO result
    FROM faktura_kupovina n
    WHERE n.BrojFakture = brFakture;
  END $$
DELIMITER ;
