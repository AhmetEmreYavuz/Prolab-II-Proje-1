// ElderPassenger.java
public class ElderPassenger extends Passenger {
    private int freeRidesLimit; // Örneğin 20 ücretsiz yolculuk limiti

    public ElderPassenger(String name, int age, Location currentLocation, Location destination) {
        super(name, age, currentLocation, destination);
        this.freeRidesLimit = 20; // Varsayılan 20 ücretsiz yolculuk
    }

    public int getFreeRidesLimit() {
        return freeRidesLimit;
    }
}
