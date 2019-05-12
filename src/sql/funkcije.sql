DELIMITER $$
CREATE FUNCTION iznosZaIsplatuDobavljacu(bf INTEGER)
  RETURNS DOUBLE
  BEGIN
    DECLARE iznosZaUplatu, uplaceno, ukupno DOUBLE;
    SELECT sum(Iznos)
    INTO uplaceno
    FROM isplata_dobavljacu i
    WHERE i.BrojFakture = bf;

    SELECT UkupanIznos
    INTO ukupno
    FROM faktura_nabavka n
    WHERE n.BrojFakture = bf;

    SET iznosZaUplatu = ukupno - uplaceno;

    RETURN iznosZaUplatu;
  END $$
DELIMITER ;

DELIMITER $$
CREATE FUNCTION iznosZaUplatuKupca(bf INTEGER)
  RETURNS DOUBLE
  BEGIN
    DECLARE iznosZaUplatu, uplaceno, ukupno DOUBLE;
    SELECT sum(Iznos)
    INTO uplaceno
    FROM uplata_kupca i
    WHERE i.BrojFakture = bf;

    SELECT UkupanIznos
    INTO ukupno
    FROM faktura_kupovina n NATURAL JOIN fiskalni_racun
    WHERE n.BrojFakture = bf;

    SET iznosZaUplatu = ukupno - uplaceno;

    RETURN iznosZaUplatu;
  END $$
DELIMITER ;


-- zaposlen / nezaposlen
-- provjera vazenja ugovora
DELIMITER $$
CREATE FUNCTION radniOdnos(JMBz CHAR(13))
  RETURNS VARCHAR(10)
  BEGIN
    DECLARE radniOdnos VARCHAR(10) DEFAULT 'Nezaposlen';
    DECLARE datum DATE;

    SELECT DatumDo
    INTO datum
    FROM ugovor_posao u
    WHERE u.JMB = JMBz
    ORDER BY DatumOd DESC
    LIMIT 1;

    IF (isnull(datum) OR datum > current_date)
    THEN
      SET radniOdnos = 'Zaposlen';
    END IF;

    RETURN radniOdnos;
  END $$
DELIMITER ;

-- true - ako je važeći
DELIMITER $$
CREATE FUNCTION provjeriUgovor(JMBz CHAR(13))
  RETURNS BOOLEAN
  BEGIN
    DECLARE result BOOLEAN DEFAULT FALSE;
    DECLARE datum DATE;

    SELECT DatumDo
    INTO datum
    FROM ugovor_posao u
    WHERE u.JMB = JMBz
    ORDER BY DatumOd DESC
    LIMIT 1;

    IF (isnull(datum) OR datum > current_date)
    THEN
      SET result = TRUE;
    END IF;
    RETURN result;
  END $$
DELIMITER ;