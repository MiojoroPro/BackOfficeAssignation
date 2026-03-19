package model;

import java.sql.Time;

public class Vehicule {
    private int id;
    private String immatriculation;
    private int capacite;
    private char carburant; // D = Diesel, E = Essence
    private Time heureDisponibilite; // Heure a partir de laquelle le vehicule est disponible chaque jour (defaut: 00:00)

    public Vehicule() {
    }

    public Vehicule(int id, String immatriculation, int capacite, char carburant) {
        this.id = id;
        this.immatriculation = immatriculation;
        this.capacite = capacite;
        this.carburant = carburant;
    }

    public Vehicule(int id, String immatriculation, int capacite, char carburant, Time heureDisponibilite) {
        this.id = id;
        this.immatriculation = immatriculation;
        this.capacite = capacite;
        this.carburant = carburant;
        this.heureDisponibilite = heureDisponibilite;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public boolean isDiesel() {
        return carburant == 'D';
    }

    public boolean isEssence() {
        return carburant == 'E';
    }

    public String getCarburantLibelle() {
        return isDiesel() ? "Diesel" : "Essence";
    }

    public Time getHeureDisponibilite() {
        return heureDisponibilite;
    }

    public void setHeureDisponibilite(Time heureDisponibilite) {
        this.heureDisponibilite = heureDisponibilite;
    }

    /**
     * Retourne l'heure de disponibilite effective (minuit par defaut si non definie)
     */
    public Time getHeureDisponibiliteEffective() {
        return heureDisponibilite != null ? heureDisponibilite : Time.valueOf("00:00:00");
    }
}
