package model;

import java.sql.Timestamp;

/**
 * Classe de détails pour l'affichage des affectations avec toutes les informations
 */
public class AffectationDetails {
    private int idAffectation;
    private int idVehicule;
    private String immatriculation;
    private int capacite;
    private char carburant;
    private int idReservation;
    private String idClient;
    private int nombrePassagers;
    private String lieuDepart;
    private String lieuArrivee;
    private Timestamp dateHeureDepart;
    private Timestamp dateHeureRetour;

    public AffectationDetails() {
    }

    public int getIdAffectation() {
        return idAffectation;
    }

    public void setIdAffectation(int idAffectation) {
        this.idAffectation = idAffectation;
    }

    public int getIdVehicule() {
        return idVehicule;
    }

    public void setIdVehicule(int idVehicule) {
        this.idVehicule = idVehicule;
    }

    public String getImmatriculation() {
        return immatriculation;
    }

    public void setImmatriculation(String immatriculation) {
        this.immatriculation = immatriculation;
    }

    public int getCapacite() {
        return capacite;
    }

    public void setCapacite(int capacite) {
        this.capacite = capacite;
    }

    public char getCarburant() {
        return carburant;
    }

    public void setCarburant(char carburant) {
        this.carburant = carburant;
    }

    public String getCarburantLibelle() {
        return carburant == 'D' ? "Diesel" : "Essence";
    }

    public boolean isDiesel() {
        return carburant == 'D';
    }

    public int getIdReservation() {
        return idReservation;
    }

    public void setIdReservation(int idReservation) {
        this.idReservation = idReservation;
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

    public String getLieuDepart() {
        return lieuDepart;
    }

    public void setLieuDepart(String lieuDepart) {
        this.lieuDepart = lieuDepart;
    }

    public String getLieuArrivee() {
        return lieuArrivee;
    }

    public void setLieuArrivee(String lieuArrivee) {
        this.lieuArrivee = lieuArrivee;
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
