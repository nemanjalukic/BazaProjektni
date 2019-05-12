package net.etfbl.bp.helper;


public class Poslovnica {
    private int Id;
    private String naziv;
    
    public Poslovnica(int Id,String naziv){
    this.Id=Id;
    this.naziv=naziv;
    }

    public int getId() {
        return Id;
    }

    public String getNaziv() {
        return naziv;
    }
    
}
