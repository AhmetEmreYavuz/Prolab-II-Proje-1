// Taxi.java
public class Taxi extends Vehicle {
    private double openingFee;
    private double costPerKm;

    public Taxi(String id, String name, double openingFee, double costPerKm) {
        super(id, name);
        this.openingFee = openingFee;
        this.costPerKm = costPerKm;
    }

    public double getOpeningFee() {
        return openingFee;
    }

    public double getCostPerKm() {
        return costPerKm;
    }
}
