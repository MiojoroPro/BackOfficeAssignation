package model;

public class Hotel {
    private int idHotel;
    private String nom;
    private String adresse;

    public Hotel() {
    }

    public Hotel(int idHotel, String nom, String adresse) {
        this.idHotel = idHotel;
        this.nom = nom;
        this.adresse = adresse;
    }

    public int getIdHotel() {
        return idHotel;
    }

    public void setIdHotel(int idHotel) {
        this.idHotel = idHotel;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }
}
