package model;

public class Distance {
    private int id;
    private int fromId;
    private int toId;
    private double km;

    public Distance() {
    }

    public Distance(int id, int fromId, int toId, double km) {
        this.id = id;
        this.fromId = fromId;
        this.toId = toId;
        this.km = km;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFromId() {
        return fromId;
    }

    public void setFromId(int fromId) {
        this.fromId = fromId;
    }

    public int getToId() {
        return toId;
    }

    public void setToId(int toId) {
        this.toId = toId;
    }

    public double getKm() {
        return km;
    }

    public void setKm(double km) {
        this.km = km;
    }
}
