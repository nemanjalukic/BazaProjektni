package net.etfbl.bp.helper;


public class Dostupnost {
    private String adresa, telefon;
    private Integer id, stanje;

    public Dostupnost(Integer id, String adresa, String telefon, Integer stanje) {
        this.adresa = adresa;
        this.id = id;
        this.stanje = stanje;
        this.telefon = telefon;
    }

    public String getAdresa() {
        return adresa;
    }

    public Integer getId() {
        return id;
    }

    public Integer getStanje() {
        return stanje;
    }

    public String getTelefon() {
        return telefon;
    }
}
