package model;

import java.sql.Timestamp;
import java.sql.Date;

public class Reservation {
    private int id;
    private String idClient;
    private int nombrePassagers;
    private Timestamp dateHeureDepart;
    private int idLieuDestination;
    
    // Champs pour affichage
    private String lieuDestination;
    private String lieuDepart;

    public Reservation() {
    }

    public Reservation(int id, String idClient, int nombrePassagers, 
                       Timestamp dateHeureDepart, int idLieuDestination) {
        this.id = id;
        this.idClient = idClient;
        this.nombrePassagers = nombrePassagers;
        this.dateHeureDepart = dateHeureDepart;
        this.idLieuDestination = idLieuDestination;
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

    public int getNombrePassagers() {
        return nombrePassagers;
    }

    public void setNombrePassagers(int nombrePassagers) {
        this.nombrePassagers = nombrePassagers;
    }

    public Timestamp getDateHeureDepart() {
        return dateHeureDepart;
    }

    public void setDateHeureDepart(Timestamp dateHeureDepart) {
        this.dateHeureDepart = dateHeureDepart;
    }

    public int getIdLieuDestination() {
        return idLieuDestination;
    }

    public void setIdLieuDestination(int idLieuDestination) {
        this.idLieuDestination = idLieuDestination;
    }

    public String getLieuDestination() {
        return lieuDestination;
    }

    public void setLieuDestination(String lieuDestination) {
        this.lieuDestination = lieuDestination;
    }

    public String getLieuDepart() {
        return lieuDepart;
    }

    public void setLieuDepart(String lieuDepart) {
        this.lieuDepart = lieuDepart;
    }

    public Date getDateReservation() {
        return dateHeureDepart != null ? new Date(dateHeureDepart.getTime()) : null;
    }
}
