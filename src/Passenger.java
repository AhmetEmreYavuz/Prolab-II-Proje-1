// Passenger.java
public class Passenger {
    private String name;
    private int age;
    private Location currentLocation;
    private Location destination;

    public Passenger(String name, int age, Location currentLocation, Location destination) {
        this.name = name;
        this.age = age;
        this.currentLocation = currentLocation;
        this.destination = destination;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public Location getDestination() {
        return destination;
    }
}

