import java.util.*;

public class TravelPlanner {

    /**
     * Tüm alternatif ulaşım rotalarını hesaplar ve özel formatta çıktı döndürür.
     */
    public static List<String> planRoutes(Passenger passenger, TransitData transitData) {
        List<String> outputList = new ArrayList<>();

        // 1. Direkt Taksi Rotası
        String taxiOutput = formatTaxiRoute(passenger, transitData);
        if (taxiOutput != null) {
            outputList.add(taxiOutput);
        }

        // 2. Sadece Otobüs Rotası (yürüyüş mesafesi dahil)
        String busOutput = formatBusRoute(passenger, transitData);
        if (busOutput != null) {
            outputList.add(busOutput);
        }

        // 3. Sadece Tramvay Rotası
        String tramOutput = formatTramRoute(passenger, transitData);
        if (tramOutput != null) {
            outputList.add(tramOutput);
        }

        // 4. Otobüs + Tramvay Aktarması
        String busTramOutput = formatBusTramTransferRoute(passenger, transitData);
        if (busTramOutput != null) {
            outputList.add(busTramOutput);
        }

        // 5. Taksi + Toplu Taşıma Kombinasyonu
        String taxiComboOutput = formatTaxiComboRoute(passenger, transitData);
        if (taxiComboOutput != null) {
            outputList.add(taxiComboOutput);
        }

        return outputList;
    }

    // 1) Direkt Taksi Rotası
    private static String formatTaxiRoute(Passenger passenger, TransitData transitData) {
        Location current = passenger.getCurrentLocation();
        Location destination = passenger.getDestination();
        double distance = current.distanceTo(destination); // km cinsinden
        double cost = transitData.getTaxi().getOpeningFee() + distance * transitData.getTaxi().getCostPerKm();
        int time = (int) Math.ceil((distance / 40.0) * 60); // 40 km/s varsayım

        return String.format(
                "Taxi Rotası\n" +
                        "mesafe: %.2f km, Süre: %d dakika, Ücret: %.2f TL\n" +
                        "-------------------------",
                distance, time, cost
        );
    }

    // 2) Sadece Otobüs Rotası (yürüyüş bilgisi dahil)
    private static String formatBusRoute(Passenger passenger, TransitData transitData) {
        BusStopGraph busGraph = new BusStopGraph(transitData);
        Stop startBusStop = findNearestStop(passenger.getCurrentLocation(), busGraph.getBusStops().values());
        Stop endBusStop = findNearestStop(passenger.getDestination(), busGraph.getBusStops().values());
        if (startBusStop == null || endBusStop == null) return null;

        List<String> path = bfsBusPath(busGraph.getAdjacencyList(), startBusStop.getId(), endBusStop.getId());
        if (path == null) return null;

        double walkDist = passenger.getCurrentLocation().distanceTo(
                new Location(startBusStop.getLat(), startBusStop.getLon()));
        int walkTime = (int) Math.ceil((walkDist / 5.0) * 60);

        StringBuilder sb = new StringBuilder();
        sb.append("Sadece Otobüs Rotası:\n");
        sb.append(String.format("Yürüme-> %s (bus) -- Mesafe: %.2f km (%d dk)\n",
                startBusStop.getName(), walkDist, walkTime));

        for (int i = 0; i < path.size() - 1; i++) {
            String fromId = path.get(i);
            String toId = path.get(i + 1);
            Stop fromStop = busGraph.getBusStops().get(fromId);
            Stop toStop = busGraph.getBusStops().get(toId);
            List<BusStopGraph.GraphEdge> edges = busGraph.getAdjacencyList().get(fromId);
            if (edges == null) continue;
            for (BusStopGraph.GraphEdge edge : edges) {
                if (edge.getTo().equals(toId)) {
                    sb.append(String.format(
                            "%s (bus) -> %s (bus) -- Mesafe: %.2f km, Süre: %d dakika, Ücret: %.2f TL\n",
                            fromStop.getName(), toStop.getName(),
                            edge.getDistance(), edge.getTime(), edge.getCost()
                    ));
                    break;
                }
            }
        }
        sb.append("-------------------------");
        return sb.toString();
    }

    // 3) Sadece Tramvay Rotası
    private static String formatTramRoute(Passenger passenger, TransitData transitData) {
        TramStopGraph tramGraph = new TramStopGraph(transitData);
        Stop startTramStop = findNearestStop(passenger.getCurrentLocation(), tramGraph.getTramStops().values());
        Stop endTramStop = findNearestStop(passenger.getDestination(), tramGraph.getTramStops().values());
        if (startTramStop == null || endTramStop == null) return null;

        List<String> path = bfsTramPath(tramGraph.getAdjacencyList(), startTramStop.getId(), endTramStop.getId());
        if (path == null) return null;

        double walkDist = passenger.getCurrentLocation().distanceTo(
                new Location(startTramStop.getLat(), startTramStop.getLon()));
        int walkTime = (int) Math.ceil((walkDist / 5.0) * 60);

        StringBuilder sb = new StringBuilder();
        sb.append("Sadece Tramvay Rotası:\n");
        sb.append(String.format("Yürüme-> %s (tram) -- Mesafe: %.2f km (%d dk)\n",
                startTramStop.getName(), walkDist, walkTime));

        for (int i = 0; i < path.size() - 1; i++) {
            String fromId = path.get(i);
            String toId = path.get(i + 1);
            Stop fromStop = tramGraph.getTramStops().get(fromId);
            Stop toStop = tramGraph.getTramStops().get(toId);
            List<TramStopGraph.GraphEdge> edges = tramGraph.getAdjacencyList().get(fromId);
            if (edges == null) continue;
            for (TramStopGraph.GraphEdge edge : edges) {
                if (edge.getTo().equals(toId)) {
                    sb.append(String.format(
                            "%s (tram) -> %s (tram) -- Mesafe: %.2f km, Süre: %d dakika, Ücret: %.2f TL\n",
                            fromStop.getName(), toStop.getName(),
                            edge.getDistance(), edge.getTime(), edge.getCost()
                    ));
                    break;
                }
            }
        }
        sb.append("-------------------------");
        return sb.toString();
    }

    // 4) Otobüs + Tramvay Aktarması
    private static String formatBusTramTransferRoute(Passenger passenger, TransitData transitData) {
        BusStopGraph busGraph = new BusStopGraph(transitData);
        TramStopGraph tramGraph = new TramStopGraph(transitData);
        Stop startBusStop = findNearestStop(passenger.getCurrentLocation(), busGraph.getBusStops().values());
        Stop endTramStop = findNearestStop(passenger.getDestination(), tramGraph.getTramStops().values());
        if (startBusStop == null || endTramStop == null) return null;

        // Transfer için: startBusStop üzerinde transfer bilgisi varsa
        if (startBusStop.getTransfer() == null || startBusStop.getTransfer().getTransferStopId() == null) {
            return null;
        }
        String transferTramStopId = startBusStop.getTransfer().getTransferStopId();

        List<String> busPath = bfsBusPath(busGraph.getAdjacencyList(), startBusStop.getId(), transferTramStopId);
        List<String> tramPath = bfsTramPath(tramGraph.getAdjacencyList(), transferTramStopId, endTramStop.getId());
        if (busPath == null || tramPath == null) return null;

        double walkDist = passenger.getCurrentLocation().distanceTo(
                new Location(startBusStop.getLat(), startBusStop.getLon()));
        int walkTime = (int) Math.ceil((walkDist / 5.0) * 60);

        StringBuilder sb = new StringBuilder();
        sb.append("Otobüs + Tramvay Aktarması:\n");
        sb.append(String.format("Yürüme-> %s (bus) -- Mesafe: %.2f km (%d dk)\n",
                startBusStop.getName(), walkDist, walkTime));

        // Otobüs segmentleri
        for (int i = 0; i < busPath.size() - 1; i++) {
            String fromId = busPath.get(i);
            String toId = busPath.get(i + 1);
            Stop fromStop = busGraph.getBusStops().get(fromId);
            Stop toStop = busGraph.getBusStops().get(toId);
            List<BusStopGraph.GraphEdge> edges = busGraph.getAdjacencyList().get(fromId);
            if (edges == null) continue;
            for (BusStopGraph.GraphEdge edge : edges) {
                if (edge.getTo().equals(toId)) {
                    sb.append(String.format(
                            "%s (bus) -> %s (bus) -- Mesafe: %.2f km, Süre: %d dakika, Ücret: %.2f TL\n",
                            fromStop.getName(), toStop.getName(),
                            edge.getDistance(), edge.getTime(), edge.getCost()
                    ));
                    break;
                }
            }
        }
        sb.append("Aktarma -> ").append(transferTramStopId).append(" (tram)\n");
        // Tram segmentleri
        for (int i = 0; i < tramPath.size() - 1; i++) {
            String fromId = tramPath.get(i);
            String toId = tramPath.get(i + 1);
            Stop fromStop = tramGraph.getTramStops().get(fromId);
            Stop toStop = tramGraph.getTramStops().get(toId);
            List<TramStopGraph.GraphEdge> edges = tramGraph.getAdjacencyList().get(fromId);
            if (edges == null) continue;
            for (TramStopGraph.GraphEdge edge : edges) {
                if (edge.getTo().equals(toId)) {
                    sb.append(String.format(
                            "%s (tram) -> %s (tram) -- Mesafe: %.2f km, Süre: %d dakika, Ücret: %.2f TL\n",
                            fromStop.getName(), toStop.getName(),
                            edge.getDistance(), edge.getTime(), edge.getCost()
                    ));
                    break;
                }
            }
        }
        sb.append("-------------------------");
        return sb.toString();
    }

    // 5) Taksi + Toplu Taşıma Kombinasyonu (3 km kuralı)
    private static String formatTaxiComboRoute(Passenger passenger, TransitData transitData) {
        double distanceToBus = 99999;
        BusStopGraph busGraph = new BusStopGraph(transitData);
        Stop nearestBus = findNearestStop(passenger.getCurrentLocation(), busGraph.getBusStops().values());
        if (nearestBus != null) {
            distanceToBus = passenger.getCurrentLocation().distanceTo(
                    new Location(nearestBus.getLat(), nearestBus.getLon()));
        }
        if (distanceToBus < 3.0) {
            return "Taksi+Toplu Taşıma Kombinasyonu:\n" +
                    "Yolcu durağa 3 km'den az yakın olduğu için taksi kullanılmayacaktır.\n" +
                    "-------------------------";
        }
        double distance = distanceToBus;
        int time = (int) Math.ceil((distance / 40.0) * 60);
        double cost = transitData.getTaxi().getOpeningFee() + distance * transitData.getTaxi().getCostPerKm();

        return String.format(
                "Taksi+Toplu Taşıma Kombinasyonu:\n" +
                        "Durağa olan mesafe: %.2f km, Süre: %d dk, Ücret: %.2f TL\n" +
                        "-------------------------",
                distance, time, cost
        );
    }

    // ------------------ Ortak Yardımcı Metotlar ------------------

    private static Stop findNearestStop(Location loc, Collection<Stop> stops) {
        Stop nearest = null;
        double minDist = Double.MAX_VALUE;
        for (Stop s : stops) {
            double d = loc.distanceTo(new Location(s.getLat(), s.getLon()));
            if (d < minDist) {
                minDist = d;
                nearest = s;
            }
        }
        return nearest;
    }

    // BFS: Otobüs durakları için
    private static List<String> bfsBusPath(Map<String, List<BusStopGraph.GraphEdge>> adjacencyList,
                                           String startId, String endId) {
        Queue<String> queue = new LinkedList<>();
        Map<String, String> parent = new HashMap<>();
        Set<String> visited = new HashSet<>();

        queue.add(startId);
        visited.add(startId);
        parent.put(startId, null);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(endId)) {
                return reconstructPath(parent, endId);
            }
            List<BusStopGraph.GraphEdge> edges = adjacencyList.get(current);
            if (edges != null) {
                for (BusStopGraph.GraphEdge edge : edges) {
                    String neighbor = edge.getTo();
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        parent.put(neighbor, current);
                        queue.add(neighbor);
                    }
                }
            }
        }
        return null;
    }

    // BFS: Tramvay durakları için
    private static List<String> bfsTramPath(Map<String, List<TramStopGraph.GraphEdge>> adjacencyList,
                                            String startId, String endId) {
        Queue<String> queue = new LinkedList<>();
        Map<String, String> parent = new HashMap<>();
        Set<String> visited = new HashSet<>();

        queue.add(startId);
        visited.add(startId);
        parent.put(startId, null);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(endId)) {
                return reconstructPath(parent, endId);
            }
            List<TramStopGraph.GraphEdge> edges = adjacencyList.get(current);
            if (edges != null) {
                for (TramStopGraph.GraphEdge edge : edges) {
                    String neighbor = edge.getTo();
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        parent.put(neighbor, current);
                        queue.add(neighbor);
                    }
                }
            }
        }
        return null;
    }

    private static List<String> reconstructPath(Map<String, String> parent, String endId) {
        List<String> path = new ArrayList<>();
        String cur = endId;
        while (cur != null) {
            path.add(0, cur);
            cur = parent.get(cur);
        }
        return path;
    }
}
