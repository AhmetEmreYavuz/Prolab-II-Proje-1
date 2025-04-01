import org.openstreetmap.gui.jmapviewer.Coordinate;

import java.util.List;

public class Route {
    private String description;
    private double totalDistance; // km
    private int totalTime;        // dakika
    private double totalCost;     // TL
    private List<Coordinate> coordinates;

    public Route(String description, double totalDistance, int totalTime, double totalCost) {
        this.description = description;
        this.totalDistance = totalDistance;
        this.totalTime = totalTime;
        this.totalCost = totalCost;
    }

    public String getDescription() { return description; }
    public double getTotalDistance() { return totalDistance; }
    public int getTotalTime() { return totalTime; }
    public double getTotalCost() { return totalCost; }

    @Override
    public String toString() {
        return description + "\nMesafe: " + String.format("%.2f", totalDistance) + " km, Süre: " + totalTime +
                " dakika, Ücret: " + String.format("%.2f", totalCost) + " TL";
    }

    public List<Coordinate> getCoordinates() {
        return this.coordinates;
    }

}
