package model;

import java.sql.Timestamp;

public class Affectation {
    private int id;
    private int idVehicule;
    private int idReservation;
    private Timestamp dateHeureDepart;
    private Timestamp dateHeureRetour;

    public Affectation() {
    }

    public Affectation(int id, int idVehicule, int idReservation, 
                       Timestamp dateHeureDepart, Timestamp dateHeureRetour) {
        this.id = id;
        this.idVehicule = idVehicule;
        this.idReservation = idReservation;
        this.dateHeureDepart = dateHeureDepart;
        this.dateHeureRetour = dateHeureRetour;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdVehicule() {
        return idVehicule;
    }

    public void setIdVehicule(int idVehicule) {
        this.idVehicule = idVehicule;
    }

    public int getIdReservation() {
        return idReservation;
    }

    public void setIdReservation(int idReservation) {
        this.idReservation = idReservation;
    }

    public Timestamp getDateHeureDepart() {
        return dateHeureDepart;
    }

    public void setDateHeureDepart(Timestamp dateHeureDepart) {
        this.dateHeureDepart = dateHeureDepart;
    }

    public Timestamp getDateHeureRetour() {
        return dateHeureRetour;
    }

    public void setDateHeureRetour(Timestamp dateHeureRetour) {
        this.dateHeureRetour = dateHeureRetour;
    }
}
