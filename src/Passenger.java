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

    public String getName() { return name; }
    public int getAge() { return age; }
    public Location getCurrentLocation() { return currentLocation; }
    public Location getDestination() { return destination; }

    /**
     * Genel olarak yolcu için, herhangi bir düzenleme yapılmaz.
     * @param cost Hesaplanan orijinal maliyet.
     * @param isTaxi Eğer segment taxi ise true; toplu taşıma veya diğer segmentler için false.
     * @return Düzenlenmiş maliyet.
     */
    public double adjustCost(double cost, boolean isTaxi) {
        return cost;
    }
}
