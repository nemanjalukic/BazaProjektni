package net.etfbl.bp.helper;

import java.util.ArrayList;
import java.util.Objects;

public class Proizvod {
    private String proizvod, naziv, proizvodjac, tip,dimenzije;
    private String opis,rokUpotrebe,boja,potrosnja,pakovanje;
    private String kolicina, nabavnacijena, prodajnacijena;
    private Double vrijednost, cijena;
    private ArrayList<String> nanosenje=new ArrayList<>();
    private Integer k,sifra;
        public Proizvod(Integer sifra, String naziv, Integer kolicina, Double cijena) {
        this.sifra = sifra;
        this.naziv = naziv;
        this.k = kolicina;
        this.kolicina = kolicina + "";
        this.cijena = cijena;
        this.vrijednost = cijena * kolicina;
    }

    public Proizvod(String sifra, String naziv, Integer kolicina, Double cijena) {
        this(Integer.valueOf(sifra), naziv, kolicina, cijena);
    }
    public Proizvod(String proizvodjac,String naziv,String opis,Integer kolicina,String nabavnacijena,String prodajnacijena) {
        this.naziv = naziv;
        this.opis=opis;
        this.k = kolicina;
        this.kolicina = kolicina + "";
        this.nabavnacijena=nabavnacijena;
        this.prodajnacijena=prodajnacijena;
        this.cijena = Double.valueOf(prodajnacijena);
        this.vrijednost = cijena * kolicina;
        this.proizvodjac=proizvodjac;
    }

    public Proizvod() {

    }

    public Proizvod(String proizvod) {
        this.proizvod = proizvod;
    }

    public Double updateKolicina(Integer kolicina) {
        this.k += kolicina;
        this.kolicina = k + "";
        Double p = cijena * kolicina;
        this.vrijednost += p;
        return p;
    }

    public String getProizvod() {
        return proizvod;
    }

    public void setProizvod(String proizvod) {
        this.proizvod = proizvod;
    }

   

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }


    public String getProizvodjac() {
        return proizvodjac;
    }

    public void setProizvodjac(String proizvodjac) {
        this.proizvodjac = proizvodjac;
    }

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }

    public String getNabavnacijena() {
        return nabavnacijena;
    }

    public void setNabavnacijena(String nabavnacijena) {
        this.nabavnacijena = nabavnacijena;
    }

    public Double getVrijednost() {
        return vrijednost;
    }

    public void setVrijednost(Double vrijednost) {
        this.vrijednost = vrijednost;
    }

    public Double getCijena() {
        return cijena;
    }

    public void setCijena(Double cijena) {
        this.cijena = cijena;
    }

    public String getKolicina() {
        return kolicina;
    }

    public void setKolicina(String kolicina) {
        this.kolicina = kolicina;
        this.k = Integer.valueOf(kolicina);
    }

    public String getProdajnacijena() {
        return prodajnacijena;
    }

    public void setProdajnacijena(String prodajnacijena) {
        this.prodajnacijena = prodajnacijena;
    }

    public String getRokUpotrebe() {
        return rokUpotrebe;
    }

    public void setRokUpotrebe(String rokUpotrebe) {
        this.rokUpotrebe = rokUpotrebe;
    }

    public String getBoja() {
        return boja;
    }

    public String getPotrosnja() {
        return potrosnja;
    }

    public String getPakovanje() {
        return pakovanje;
    }


    public ArrayList<String> getNanosenje() {
        return nanosenje;
    }

    public void setBoja(String boja) {
        this.boja = boja;
    }

    public void setPotrosnja(String potrosnja) {
        this.potrosnja = potrosnja;
    }

    public void setPakovanje(String pakovanje) {
        this.pakovanje = pakovanje;
    }

    public void setNanosenje(ArrayList<String> nanosenje) {
        this.nanosenje = nanosenje;
    }

    public String getDimenzije() {
        return dimenzije;
    }

    public void setDimenzije(String dimenzije) {
        this.dimenzije = dimenzije;
    }

    public Integer getSifra() {
        return sifra;
    }

    public void setSifra(Integer sifra) {
        this.sifra = sifra;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.sifra);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Proizvod other = (Proizvod) obj;
        if (!Objects.equals(this.sifra, other.sifra)) {
            return false;
        }
        return true;
    }
    
    
    
}

