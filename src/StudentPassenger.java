// StudentPassenger.java
public class StudentPassenger extends Passenger {
    private double discountRate; // Örneğin %50 indirim için 0.5

    public StudentPassenger(String name, int age, Location currentLocation, Location destination) {
        super(name, age, currentLocation, destination);
        this.discountRate = 0.5; // Varsayılan %50 indirim
    }

    public double getDiscountRate() {
        return discountRate;
    }
}
