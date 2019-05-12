-- samo kao temp tabela za view ispod
CREATE VIEW zaposleni_u_poslovnici(JMB, RadniOdnos, Poslovnica) AS
  SELECT JMB, radniOdnos(JMB) AS RadniOdnos, concat('NemanjaBoje', ' ', IdPoslovnice, ' - ', Adresa) AS Poslovnica
  FROM ugovor_posao
    NATURAL JOIN poslovnica;
-- podaci o zaposlenim
CREATE VIEW zaposlenje(JMB, Ime, Prezime, Adresa, Telefon, Plata, RadniOdnos, Poslovnica) AS
  SELECT *
  FROM zaposleni z NATURAL JOIN zaposleni_u_poslovnici;

CREATE VIEW mjesecni_promet(Mjesec, Iznos) AS
  SELECT concat(MONTHNAME(Datum), ' ', YEAR(Datum)) AS Mjesec, sum(Iznos)
  FROM dnevni_promet
  GROUP BY Mjesec
  ORDER BY Datum DESC;

CREATE VIEW godisnji_promet(Godina, Iznos) AS
  SELECT YEAR(Datum) AS Godina, sum(Iznos)
  FROM dnevni_promet
  GROUP BY Godina
  ORDER BY Godina DESC;
