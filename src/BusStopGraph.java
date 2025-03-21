import java.util.*;

public class BusStopGraph {
    private Map<String, Stop> busStops = new HashMap<>();
    private Map<String, List<GraphEdge>> adjacencyList = new HashMap<>();

    public BusStopGraph(TransitData transitData) {
        // Sadece "bus" tipindeki durakları filtreleyelim.
        for (Stop stop : transitData.getDuraklar()) {
            if ("bus".equalsIgnoreCase(stop.getType())) {
                busStops.put(stop.getId(), stop);
                adjacencyList.put(stop.getId(), new ArrayList<>());
            }
        }
        // Her bus durağı için, son durak değilse nextStops'ı ekleyelim.
        for (Stop stop : busStops.values()) {
            if (stop.isSonDurak()) continue; // son durak olduğundan bağlantı ekleme.
            List<NextStop> nextStops = stop.getNextStops();
            if (nextStops != null) {
                for (NextStop ns : nextStops) {
                    if (busStops.containsKey(ns.getStopId())) {
                        adjacencyList.get(stop.getId()).add(
                                new GraphEdge(stop.getId(), ns.getStopId(), ns.getMesafe(), ns.getSure(), ns.getUcret())
                        );
                    }
                }
            }
        }
    }

    public Map<String, Stop> getBusStops() {
        return busStops;
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
