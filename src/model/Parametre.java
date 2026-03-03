package model;

public class Parametre {
    private int id;
    private double vitesseMoyenne; // km/h
    private int tempsAttente; // minutes

    public Parametre() {
    }

    public Parametre(int id, double vitesseMoyenne, int tempsAttente) {
        this.id = id;
        this.vitesseMoyenne = vitesseMoyenne;
        this.tempsAttente = tempsAttente;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getVitesseMoyenne() {
        return vitesseMoyenne;
    }

    public void setVitesseMoyenne(double vitesseMoyenne) {
        this.vitesseMoyenne = vitesseMoyenne;
    }

    public int getTempsAttente() {
        return tempsAttente;
    }

    public void setTempsAttente(int tempsAttente) {
        this.tempsAttente = tempsAttente;
    }

    /**
     * Calcule le temps de trajet en minutes pour une distance donnée
     */
    public int calculerTempsTrajet(double distanceKm) {
        return (int) Math.ceil((distanceKm / vitesseMoyenne) * 60);
    }
}
