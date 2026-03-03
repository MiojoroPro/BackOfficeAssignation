package model;

public class Lieu {
    private int id;
    private String code;
    private String libelle;
    private String type; // HOTEL ou AEROPORT

    public Lieu() {
    }

    public Lieu(int id, String code, String libelle, String type) {
        this.id = id;
        this.code = code;
        this.libelle = libelle;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isHotel() {
        return "HOTEL".equals(type);
    }

    public boolean isAeroport() {
        return "AEROPORT".equals(type);
    }
}
