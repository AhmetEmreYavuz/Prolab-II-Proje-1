import java.util.*;

public class TramStopGraph {
    private Map<String, Stop> tramStops = new HashMap<>();
    private Map<String, List<GraphEdge>> adjacencyList = new HashMap<>();

    public TramStopGraph(TransitData transitData) {
        // Sadece "tram" tipindeki durakları filtreleyelim.
        for (Stop stop : transitData.getDuraklar()) {
            if ("tram".equalsIgnoreCase(stop.getType())) {
                tramStops.put(stop.getId(), stop);
                adjacencyList.put(stop.getId(), new ArrayList<>());
            }
        }
        // Her tram durağı için, son durak değilse nextStops'ı ekleyelim.
        for (Stop stop : tramStops.values()) {
            if (stop.isSonDurak()) continue;
            List<NextStop> nextStops = stop.getNextStops();
            if (nextStops != null) {
                for (NextStop ns : nextStops) {
                    if (tramStops.containsKey(ns.getStopId())) {
                        adjacencyList.get(stop.getId()).add(
                                new GraphEdge(stop.getId(), ns.getStopId(), ns.getMesafe(), ns.getSure(), ns.getUcret())
                        );
                    }
                }
            }
        }
    }

    public Map<String, Stop> getTramStops() {
        return tramStops;
    }

    public Map<String, List<GraphEdge>> getAdjacencyList() {
        return adjacencyList;
    }

    public static class GraphEdge {
        private String from;
        private String to;
        private double distance;
        private int time;
        private double cost;

        public GraphEdge(String from, String to, double distance, int time, double cost) {
            this.from = from;
            this.to = to;
            this.distance = distance;
            this.time = time;
            this.cost = cost;
        }

        public String getFrom() { return from; }
        public String getTo() { return to; }
        public double getDistance() { return distance; }
        public int getTime() { return time; }
        public double getCost() { return cost; }
    }
}
