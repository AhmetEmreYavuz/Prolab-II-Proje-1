// Vehicle.java
public abstract class Vehicle {
    private String id;
    private String name;

    public Vehicle(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
