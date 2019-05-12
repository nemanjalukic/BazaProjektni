package net.etfbl.bp.helper;

public class Promet {
    private String datum;
    private Double iznos;

    public Promet(String datum, Double iznos) {
        this.datum = datum;
        this.iznos = iznos;
    }

    public String getDatum() {
        return datum;
    }

    public void setDatum(String datum) {
        this.datum = datum;
    }

    public Double getIznos() {
        return iznos;
    }

    public void setIznos(Double iznos) {
        this.iznos = iznos;
    }
}
