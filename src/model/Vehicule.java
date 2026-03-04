package model;

public class Vehicule {
    private int id;
    private String immatriculation;
    private int capacite;
    private char carburant; // D = Diesel, E = Essence

    public Vehicule() {
    }

    public Vehicule(int id, String immatriculation, int capacite, char carburant) {
        this.id = id;
        this.immatriculation = immatriculation;
        this.capacite = capacite;
        this.carburant = carburant;
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
}
