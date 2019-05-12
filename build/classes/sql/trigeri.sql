DELIMITER $$
CREATE TRIGGER azuriraj_nakon_nabavke AFTER INSERT ON stavka_nabavke FOR EACH ROW
  BEGIN
    -- povećaj ukupan iznos nabavke novom stavkom
    UPDATE faktura_nabavka n
    SET n.UkupanIznos = n.UkupanIznos + new.NabavnaCijena * new.Kolicina
    WHERE n.BrojFakture = new.BrojFakture;

    -- dodaj proizvod u skladište ili povećaj količinu
    INSERT INTO artikl_u_skladistu VALUES (new.Sifra, (
      SELECT IdPoslovnice
      FROM faktura_nabavka n
      WHERE n.BrojFakture = new.BrojFakture), new.Kolicina)
    ON DUPLICATE KEY UPDATE artikl_u_skladistu.Kolicina = artikl_u_skladistu.Kolicina + new.Kolicina;

  END $$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER azuriraj_nakon_prodaje AFTER INSERT ON stavka_racuna FOR EACH ROW
  BEGIN
    -- povećaj ukupan iznos računa novom stavkom
    UPDATE fiskalni_racun f
    SET f.UkupanIznos = f.UkupanIznos + new.Kolicina * new.Cijena
    WHERE f.BrojRacuna = new.BrojRacuna;

    -- umanji količinu proizvoda u skladištu prodajom
    UPDATE artikl_u_skladistu p
    SET p.Kolicina = p.Kolicina - new.Kolicina
    WHERE p.Sifra = new.Sifra AND p.IdPoslovnice = (
      SELECT IdPoslovnice
      FROM fiskalni_racun fr
      WHERE fr.BrojRacuna = new.BrojRacuna);

  END $$
DELIMITER ;

-- pripremam novu evidenciju dnevnog prometa
DELIMITER $$
CREATE TRIGGER unesi_datum_u_dnevni_promet AFTER INSERT ON fiskalni_racun FOR EACH ROW
  BEGIN
    IF (NOT exists(SELECT Datum
                   FROM dnevni_promet d
                   WHERE d.Datum = current_date AND d.IdPoslovnice = new.IdPoslovnice))
    THEN
      INSERT INTO dnevni_promet VALUES (current_date, 0,new.IdPoslovnice);
    END IF;
  END $$
DELIMITER ;

-- azuriram dnevni promet kako se vrsi prodaja
DELIMITER $$
CREATE TRIGGER azuriraj_dnevni_promet AFTER UPDATE ON fiskalni_racun FOR EACH ROW
  BEGIN
    UPDATE dnevni_promet d
    SET d.Iznos = d.Iznos + (new.UkupanIznos - old.UkupanIznos)
    WHERE d.IdPoslovnice = new.IdPoslovnice AND d.Datum = current_date;
  END $$
DELIMITER ;


DELIMITER $$
CREATE TRIGGER azuriraj_nakon_brisanja_zaposlenog BEFORE DELETE ON zaposleni FOR EACH ROW
  BEGIN
    DELETE FROM racun_zaposlenog
    WHERE JMB = old.JMB;
    DELETE FROM ugovor_posao
    WHERE JMB = old.JMB;
  END $$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER obrisi_racun BEFORE DELETE ON racun_zaposlenog FOR EACH ROW
  BEGIN
    DELETE FROM bankovni_racun
    WHERE BrojRacuna = old.BrojRacuna;
  END $$
DELIMITER ;

-- provjeri iznos prilikom isplate plate
DELIMITER $$
CREATE TRIGGER provjeri_isplatu_plate BEFORE INSERT ON isplata_plate FOR EACH ROW
  BEGIN
    DECLARE pl DECIMAL(19, 4) DEFAULT 0;
    SELECT Plata
    INTO pl
    FROM zaposleni z
    WHERE z.JMB = new.JMB;
    IF (pl != new.Iznos)
    THEN
      SET new.Iznos = 0;
      SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Iznos plate nije odgovarajući!';
    END IF;

    IF (NOT provjeriUgovor(new.JMB))
    THEN
      SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Ugovor je istekao!';
    END IF;
  END $$
DELIMITER ;

-- da se ne može uplatiti više od ukupnog iznosa nabavke
DELIMITER $$
CREATE TRIGGER provjeri_isplatu_dobavljacu BEFORE INSERT ON isplata_dobavljacu FOR EACH ROW
  BEGIN -- ako je preostali iznos manji od uplate
    IF (iznosZaIsplatuDobavljacu(new.BrojFakture) < new.Iznos)
    THEN
      SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Uplaćujete više nego što treba!';
    END IF;
  END $$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER provjeri_uplatu_kupca BEFORE INSERT ON uplata_kupca FOR EACH ROW
  BEGIN -- ako je preostali iznos manji od uplate
    IF (iznosZaUplatuKupca(new.BrojFakture) < new.Iznos)
    THEN
      SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Uplaćujete više nego što treba!';
    END IF;
  END $$
DELIMITER ;