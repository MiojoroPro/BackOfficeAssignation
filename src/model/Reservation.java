package model;

import java.sql.Timestamp;

public class Reservation {
    private int id;
    private String idClient;
    private int nbpassagers;
    private Timestamp dateheure;
    private int idHotel;
    private String hotelNom;

    public Reservation() {
    }

    public Reservation(int id, String idClient, int nbpassagers, Timestamp dateheure, int idHotel, String hotelNom) {
        this.id = id;
        this.idClient = idClient;
        this.nbpassagers = nbpassagers;
        this.dateheure = dateheure;
        this.idHotel = idHotel;
        this.hotelNom = hotelNom;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdClient() {
        return idClient;
    }

    public void setIdClient(String idClient) {
        this.idClient = idClient;
    }

    public int getNbpassagers() {
        return nbpassagers;
    }

    public void setNbpassagers(int nbpassagers) {
        this.nbpassagers = nbpassagers;
    }

    public Timestamp getDateheure() {
        return dateheure;
    }

    public void setDateheure(Timestamp dateheure) {
        this.dateheure = dateheure;
    }

    public int getIdHotel() {
        return idHotel;
    }

    public void setIdHotel(int idHotel) {
        this.idHotel = idHotel;
    }

    public String getHotelNom() {
        return hotelNom;
    }

    public void setHotelNom(String hotelNom) {
        this.hotelNom = hotelNom;
    }
}
