package net.etfbl.bp.helper;

public class Zaposleni {
    String jmb, ime, prezime, adresa, telefon, poslovnica, racunZaposlenog, radniOdnos, datumPotpisivanja, datumPrekida;
    Double plata;
    

    public Zaposleni(String ime, String prezime, String adresa, String telefon, String poslovnica, Double plata) {
        this.ime = ime;
        this.prezime = prezime;
        this.adresa = adresa;
        this.telefon = telefon;
        this.poslovnica = poslovnica;
        this.plata = plata;
    }

    public Zaposleni(String jmb, String ime, String prezime, String adresa, String telefon, String plata, String racunZaposlenog, String poslovnica,
                     String datumPotpisivanja, String datumPrekida) {
        this.jmb = jmb;
        this.ime = ime;
        this.prezime = prezime;
        this.adresa = adresa;
        this.telefon = telefon;
        this.poslovnica = poslovnica;
        this.racunZaposlenog = racunZaposlenog;
        this.plata = Double.valueOf(plata);
        this.datumPotpisivanja = datumPotpisivanja;
        this.datumPrekida = datumPrekida;
    }

    public Zaposleni(String jmb, String ime, String prezime, String adresa, String telefon, Double plata, String radniOdnos, String poslovnica) {
        this.jmb = jmb;
        this.ime = ime;
        this.prezime = prezime;
        this.adresa = adresa;
        this.telefon = telefon;
        this.poslovnica = poslovnica;
        this.plata = plata;
        this.radniOdnos = radniOdnos;
    }

    public String getDatumPotpisivanja() {
        return datumPotpisivanja;
    }

    public void setDatumPotpisivanja(String datumPotpisivanja) {
        this.datumPotpisivanja = datumPotpisivanja;
    }

    public String getDatumPrekida() {
        return datumPrekida;
    }

    public void setDatumPrekida(String datumPrekida) {
        this.datumPrekida = datumPrekida;
    }

    public String getRadniOdnos() {
        return radniOdnos;
    }

    public void setRadniOdnos(String radniOdnos) {
        this.radniOdnos = radniOdnos;
    }

    public String getJmb() {
        return jmb;
    }

    public void setJmb(String jmb) {
        this.jmb = jmb;
    }

    public String getIme() {
        return ime;
    }

    public void setIme(String ime) {
        this.ime = ime;
    }

    public String getPrezime() {
        return prezime;
    }

    public void setPrezime(String prezime) {
        this.prezime = prezime;
    }

    public String getAdresa() {
        return adresa;
    }

    public void setAdresa(String adresa) {
        this.adresa = adresa;
    }

    public String getTelefon() {
        return telefon;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    public String getPoslovnica() {
        return poslovnica;
    }

    public void setPoslovnica(String poslovnica) {
        this.poslovnica = poslovnica;
    }

    public String getRacunZaposlenog() {
        return racunZaposlenog;
    }

    public void setRacunZaposlenog(String racunZaposlenog) {
        this.racunZaposlenog = racunZaposlenog;
    }

    public Double getPlata() {
        return plata;
    }

    public void setPlata(Double plata) {
        this.plata = plata;
    }


}
