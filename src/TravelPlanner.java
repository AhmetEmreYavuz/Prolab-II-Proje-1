import java.util.*;

public class TravelPlanner {

    /**
     * planRoutes metodu, tüm rota alternatiflerini hesaplar ve
     * Route nesnelerinden oluşan bir liste olarak geri döndürür.
     */
    public static List<Route> planRoutes(Passenger passenger, TransitData transitData) {
        List<Route> routes = new ArrayList<>();

        Route taxiRoute = formatTaxiRoute(passenger, transitData);
        if (taxiRoute != null) {
            routes.add(taxiRoute);
        }

        Route busRoute = formatBusRoute(passenger, transitData);
        if (busRoute != null) {
            routes.add(busRoute);
        }

        Route tramRoute = formatTramRoute(passenger, transitData);
        if (tramRoute != null) {
            routes.add(tramRoute);
        }

        Route busTramRoute = formatBusTramTransferRoute(passenger, transitData);
        if (busTramRoute != null) {
            routes.add(busTramRoute);
        }

        Route tramBusRoute = formatTramBusTransferRoute(passenger, transitData);
        if (tramBusRoute != null) {
            routes.add(tramBusRoute);
        }

        return routes;
    }

    // ------------------ 1) Direkt Taksi Rotası ------------------
    static Route formatTaxiRoute(Passenger passenger, TransitData transitData) {
        Location current = passenger.getCurrentLocation();
        Location destination = passenger.getDestination();
        double distance = current.distanceTo(destination); // km cinsinden
        double cost = transitData.getTaxi().getOpeningFee() + distance * transitData.getTaxi().getCostPerKm();
        int time = (int) Math.ceil((distance / 40.0) * 60); // 40 km/s varsayım

        String desc = String.format(
                "Taxi Rotası\nMesafe: %.2f km, Süre: %d dk, Ücret: %.2f TL\n-------------------------",
                distance, time, cost);
        return new Route(desc, distance, time, cost);
    }

    // ------------------ 2) Sadece Otobüs Rotası ------------------
    static Route formatBusRoute(Passenger passenger, TransitData transitData) {
        BusStopGraph busGraph = new BusStopGraph(transitData);
        Stop startBusStop = findNearestStop(passenger.getCurrentLocation(), busGraph.getBusStops().values());
        Stop endBusStop = findNearestStop(passenger.getDestination(), busGraph.getBusStops().values());
        if (startBusStop == null || endBusStop == null) return null;

        double walkingSpeed = 5.0; // km/s
        double taxiSpeed = 40.0;   // km/s

        // Başlangıç segmenti: Kullanıcının konumundan en yakın otobüs durağına olan mesafe
        double segDistance = passenger.getCurrentLocation().distanceTo(
                new Location(startBusStop.getLat(), startBusStop.getLon()));
        String segDesc;
        int segTime = 0;
        double segCost = 0.0;
        if (segDistance < 3.0) {
            segTime = (int) Math.ceil((segDistance / walkingSpeed) * 60);
            // Yürüme segmentinde ücret toplu taşıma olduğundan adjustCost uygulanır (Student/Elder indirim/ücretsiz)
            segCost = passenger.adjustCost(0.0, false);
            segDesc = String.format("Yürüme -> %s (bus)", startBusStop.getName());
        } else {
            segTime = (int) Math.ceil((segDistance / taxiSpeed) * 60);
            segCost = transitData.getTaxi().getOpeningFee() + segDistance * transitData.getTaxi().getCostPerKm();
            segDesc = String.format("Taksi -> %s (bus)", startBusStop.getName());
        }

        List<String> path = bfsBusPath(busGraph.getAdjacencyList(), startBusStop.getId(), endBusStop.getId());
        if (path == null) return null;

        StringBuilder sb = new StringBuilder();
        sb.append("Sadece Otobüs Rotası:\n");
        sb.append(String.format("%s -- Mesafe: %.2f km, Süre: %d dk\n", segDesc, segDistance, segTime));

        double totalDistance = segDistance;
        int totalTime = segTime;
        double totalCost = segCost;

        // Otobüs segmentleri (BFS yolu üzerinden)
        for (int i = 0; i < path.size() - 1; i++) {
            String fromId = path.get(i);
            String toId = path.get(i + 1);
            Stop fromStop = busGraph.getBusStops().get(fromId);
            Stop toStop = busGraph.getBusStops().get(toId);
            List<BusStopGraph.GraphEdge> edges = busGraph.getAdjacencyList().get(fromId);
            if (edges == null) continue;
            for (BusStopGraph.GraphEdge edge : edges) {
                if (edge.getTo().equals(toId)) {
                    totalDistance += edge.getDistance();
                    totalTime += edge.getTime();
                    // Otobüs segmenti, toplu taşıma olduğundan adjustCost uygulanır (öğrenci/yaşlı için indirim/ücretsiz)
                    double adjustedCost = passenger.adjustCost(edge.getCost(), false);
                    totalCost += adjustedCost;
                    sb.append(String.format(
                            "%s (bus) -> %s (bus) -- Mesafe: %.2f km, Süre: %d dk, Ücret: %.2f TL\n",
                            fromStop.getName(), toStop.getName(),
                            edge.getDistance(), edge.getTime(), adjustedCost));
                    break;
                }
            }
        }

        // Son segment: Otobüs durağından kullanıcının varış noktasına olan mesafe
        double finalSegmentDist = passenger.getDestination().distanceTo(
                new Location(endBusStop.getLat(), endBusStop.getLon()));
        int finalSegmentTime = 0;
        double finalSegmentCost = 0.0;
        String finalSegDesc;
        if (finalSegmentDist < 3.0) {
            finalSegmentTime = (int) Math.ceil((finalSegmentDist / walkingSpeed) * 60);
            finalSegmentCost = passenger.adjustCost(0.0, false);
            finalSegDesc = "Yürüme";
        } else {
            finalSegmentTime = (int) Math.ceil((finalSegmentDist / taxiSpeed) * 60);
            finalSegmentCost = transitData.getTaxi().getOpeningFee() + finalSegmentDist * transitData.getTaxi().getCostPerKm();
            finalSegDesc = "Taksi";
        }
        sb.append(String.format("%s -> Varış (otobüs durağı: %s) -- Mesafe: %.2f km, Süre: %d dk, Ücret: %.2f TL\n",
                finalSegDesc, endBusStop.getName(), finalSegmentDist, finalSegmentTime, finalSegmentCost));

        totalDistance += finalSegmentDist;
        totalTime += finalSegmentTime;
        totalCost += finalSegmentCost;

        sb.append("-------------------------");
        return new Route(sb.toString(), totalDistance, totalTime, totalCost);
    }

    // ------------------ 3) Sadece Tramvay Rotası ------------------
    static Route formatTramRoute(Passenger passenger, TransitData transitData) {
        TramStopGraph tramGraph = new TramStopGraph(transitData);
        Stop startTramStop = findNearestStop(passenger.getCurrentLocation(), tramGraph.getTramStops().values());
        Stop endTramStop = findNearestStop(passenger.getDestination(), tramGraph.getTramStops().values());
        if (startTramStop == null || endTramStop == null) return null;

        double walkingSpeed = 5.0;
        double taxiSpeed = 40.0;
        double segDistance = passenger.getCurrentLocation().distanceTo(
                new Location(startTramStop.getLat(), startTramStop.getLon()));
        String segDesc;
        int segTime = 0;
        double segCost = 0.0;
        if (segDistance < 3.0) {
            segTime = (int) Math.ceil((segDistance / walkingSpeed) * 60);
            segCost = passenger.adjustCost(0.0, false);
            segDesc = String.format("Yürüme -> %s (tram)", startTramStop.getName());
        } else {
            segTime = (int) Math.ceil((segDistance / taxiSpeed) * 60);
            segCost = transitData.getTaxi().getOpeningFee() + segDistance * transitData.getTaxi().getCostPerKm();
            segDesc = String.format("Taksi -> %s (tram)", startTramStop.getName());
        }
        List<String> path = bfsTramPath(tramGraph.getAdjacencyList(), startTramStop.getId(), endTramStop.getId());
        if (path == null) return null;

        StringBuilder sb = new StringBuilder();
        sb.append("Sadece Tramvay Rotası:\n");
        sb.append(String.format("%s -- Mesafe: %.2f km, Süre: %d dk\n", segDesc, segDistance, segTime));

        double totalDistance = segDistance;
        int totalTime = segTime;
        double totalCost = segCost;

        for (int i = 0; i < path.size() - 1; i++) {
            String fromId = path.get(i);
            String toId = path.get(i + 1);
            Stop fromStop = tramGraph.getTramStops().get(fromId);
            Stop toStop = tramGraph.getTramStops().get(toId);
            List<TramStopGraph.GraphEdge> edges = tramGraph.getAdjacencyList().get(fromId);
            if (edges == null) continue;
            for (TramStopGraph.GraphEdge edge : edges) {
                if (edge.getTo().equals(toId)) {
                    totalDistance += edge.getDistance();
                    totalTime += edge.getTime();
                    double adjustedCost = passenger.adjustCost(edge.getCost(), false);
                    totalCost += adjustedCost;
                    sb.append(String.format(
                            "%s (tram) -> %s (tram) -- Mesafe: %.2f km, Süre: %d dk, Ücret: %.2f TL\n",
                            fromStop.getName(), toStop.getName(),
                            edge.getDistance(), edge.getTime(), adjustedCost));
                    break;
                }
            }
        }

        double finalSegmentDist = passenger.getDestination().distanceTo(
                new Location(endTramStop.getLat(), endTramStop.getLon()));
        int finalSegmentTime = 0;
        double finalSegmentCost = 0.0;
        String finalSegDesc;
        if (finalSegmentDist < 3.0) {
            finalSegmentTime = (int) Math.ceil((finalSegmentDist / walkingSpeed) * 60);
            finalSegmentCost = passenger.adjustCost(0.0, false);
            finalSegDesc = "Yürüme";
        } else {
            finalSegmentTime = (int) Math.ceil((finalSegmentDist / taxiSpeed) * 60);
            finalSegmentCost = transitData.getTaxi().getOpeningFee() + finalSegmentDist * transitData.getTaxi().getCostPerKm();
            finalSegDesc = "Taksi";
        }
        sb.append(String.format("%s -> Varış (tram durağı: %s) -- Mesafe: %.2f km, Süre: %d dk, Ücret: %.2f TL\n",
                finalSegDesc, endTramStop.getName(), finalSegmentDist, finalSegmentTime, finalSegmentCost));

        totalDistance += finalSegmentDist;
        totalTime += finalSegmentTime;
        totalCost += finalSegmentCost;

        sb.append("-------------------------");
        return new Route(sb.toString(), totalDistance, totalTime, totalCost);
    }

    // ------------------ 4) Otobüs + Tramvay Aktarması ------------------
    static Route formatBusTramTransferRoute(Passenger passenger, TransitData transitData) {
        BusStopGraph busGraph = new BusStopGraph(transitData);
        TramStopGraph tramGraph = new TramStopGraph(transitData);

        Stop startBusStop = null;
        double minDist = Double.MAX_VALUE;
        for (Stop s : busGraph.getBusStops().values()) {
            if (s.getTransfer() != null && s.getTransfer().getTransferStopId() != null) {
                double d = passenger.getCurrentLocation().distanceTo(new Location(s.getLat(), s.getLon()));
                if (d < minDist) {
                    minDist = d;
                    startBusStop = s;
                }
            }
        }
        if (startBusStop == null) return null;

        Stop endTramStop = findNearestStop(passenger.getDestination(), tramGraph.getTramStops().values());
        if (endTramStop == null) return null;

        String transferTramStopId = startBusStop.getTransfer().getTransferStopId();
        int transferTime = startBusStop.getTransfer().getTransferSure();
        double transferCost = startBusStop.getTransfer().getTransferUcret();

        double walkingSpeed = 5.0;
        double taxiSpeed = 40.0;

        StringBuilder sb = new StringBuilder();
        sb.append("Otobüs + Tramvay Aktarması:\n");

        double startSegDist = passenger.getCurrentLocation().distanceTo(
                new Location(startBusStop.getLat(), startBusStop.getLon()));
        String startSegDesc;
        int startSegTime = 0;
        double startSegCost = 0.0;
        if (startSegDist < 3.0) {
            startSegTime = (int) Math.ceil((startSegDist / walkingSpeed) * 60);
            startSegCost = passenger.adjustCost(0.0, false);
            startSegDesc = String.format("Yürüme -> %s (bus)", startBusStop.getName());
        } else {
            startSegTime = (int) Math.ceil((startSegDist / taxiSpeed) * 60);
            startSegCost = transitData.getTaxi().getOpeningFee() + startSegDist * transitData.getTaxi().getCostPerKm();
            startSegDesc = String.format("Taksi -> %s (bus)", startBusStop.getName());
        }
        sb.append(String.format("%s -- Mesafe: %.2f km, Süre: %d dk\n",
                startSegDesc, startSegDist, startSegTime));
        double totalDistance = startSegDist;
        int totalTime = startSegTime;
        double totalCost = startSegCost;

        sb.append(String.format("Transfer: %s (bus) -> %s (tram) -- Süre: %d dk, Ücret: %.2f TL\n",
                startBusStop.getName(), transferTramStopId, transferTime, transferCost));
        totalTime += transferTime;
        totalCost += transferCost;

        List<String> tramPath = bfsTramPath(tramGraph.getAdjacencyList(), transferTramStopId, endTramStop.getId());
        if (tramPath == null) return null;
        for (int i = 0; i < tramPath.size() - 1; i++) {
            String fromId = tramPath.get(i);
            String toId = tramPath.get(i + 1);
            Stop fromStop = tramGraph.getTramStops().get(fromId);
            Stop toStop = tramGraph.getTramStops().get(toId);
            List<TramStopGraph.GraphEdge> edges = tramGraph.getAdjacencyList().get(fromId);
            if (edges == null) continue;
            for (TramStopGraph.GraphEdge edge : edges) {
                if (edge.getTo().equals(toId)) {
                    totalDistance += edge.getDistance();
                    totalTime += edge.getTime();
                    double adjustedCost = passenger.adjustCost(edge.getCost(), false);
                    totalCost += adjustedCost;
                    sb.append(String.format(
                            "%s (tram) -> %s (tram) -- Mesafe: %.2f km, Süre: %d dk, Ücret: %.2f TL\n",
                            fromStop.getName(), toStop.getName(),
                            edge.getDistance(), edge.getTime(), adjustedCost));
                    break;
                }
            }
        }
        sb.append("-------------------------");
        return new Route(sb.toString(), totalDistance, totalTime, totalCost);
    }

    // ------------------ 5) Yeni Versiyon: Tramvay + Bus Aktarması ------------------
    static Route formatTramBusTransferRoute(Passenger passenger, TransitData transitData) {
        TramStopGraph tramGraph = new TramStopGraph(transitData);
        BusStopGraph busGraph = new BusStopGraph(transitData);

        Stop startTramStop = null;
        double minDist = Double.MAX_VALUE;
        for (Stop s : tramGraph.getTramStops().values()) {
            if (s.getTransfer() != null && s.getTransfer().getTransferStopId() != null) {
                double d = passenger.getCurrentLocation().distanceTo(new Location(s.getLat(), s.getLon()));
                if (d < minDist) {
                    minDist = d;
                    startTramStop = s;
                }
            }
        }
        if (startTramStop == null) return null;

        Stop endBusStop = findNearestStop(passenger.getDestination(), busGraph.getBusStops().values());
        if (endBusStop == null) return null;

        String transferBusStopId = startTramStop.getTransfer().getTransferStopId();
        int transferTime = startTramStop.getTransfer().getTransferSure();
        double transferCost = startTramStop.getTransfer().getTransferUcret();

        double walkingSpeed = 5.0;
        double taxiSpeed = 40.0;

        StringBuilder sb = new StringBuilder();
        sb.append("Tramvay + Bus Aktarması:\n");

        double startSegDist = passenger.getCurrentLocation().distanceTo(
                new Location(startTramStop.getLat(), startTramStop.getLon()));
        String startSegDesc;
        int startSegTime = 0;
        double startSegCost = 0.0;
        if (startSegDist < 3.0) {
            startSegTime = (int) Math.ceil((startSegDist / walkingSpeed) * 60);
            startSegCost = passenger.adjustCost(0.0, false);
            startSegDesc = String.format("Yürüme -> %s (tram)", startTramStop.getName());
        } else {
            startSegTime = (int) Math.ceil((startSegDist / taxiSpeed) * 60);
            startSegCost = transitData.getTaxi().getOpeningFee() + startSegDist * transitData.getTaxi().getCostPerKm();
            startSegDesc = String.format("Taksi -> %s (tram)", startTramStop.getName());
        }
        sb.append(String.format("%s -- Mesafe: %.2f km, Süre: %d dk\n", startSegDesc, startSegDist, startSegTime));
        double totalDistance = startSegDist;
        int totalTime = startSegTime;
        double totalCost = startSegCost;

        sb.append(String.format("Transfer: %s (tram) -> %s (bus) -- Süre: %d dk, Ücret: %.2f TL\n",
                startTramStop.getName(), transferBusStopId, transferTime, transferCost));
        totalTime += transferTime;
        totalCost += transferCost;

        List<String> busPath = bfsBusPath(busGraph.getAdjacencyList(), transferBusStopId, endBusStop.getId());
        if (busPath == null) return null;
        for (int i = 0; i < busPath.size() - 1; i++) {
            String fromId = busPath.get(i);
            String toId = busPath.get(i + 1);
            Stop fromStop = busGraph.getBusStops().get(fromId);
            Stop toStop = busGraph.getBusStops().get(toId);
            List<BusStopGraph.GraphEdge> edges = busGraph.getAdjacencyList().get(fromId);
            if (edges == null) continue;
            for (BusStopGraph.GraphEdge edge : edges) {
                if (edge.getTo().equals(toId)) {
                    totalDistance += edge.getDistance();
                    totalTime += edge.getTime();
                    double adjustedCost = passenger.adjustCost(edge.getCost(), false);
                    totalCost += adjustedCost;
                    sb.append(String.format("%s (bus) -> %s (bus) -- Mesafe: %.2f km, Süre: %d dk, Ücret: %.2f TL\n",
                            fromStop.getName(), toStop.getName(),
                            edge.getDistance(), edge.getTime(), adjustedCost));
                    break;
                }
            }
        }

        double finalSegDist = passenger.getDestination().distanceTo(
                new Location(endBusStop.getLat(), endBusStop.getLon()));
        String finalSegDesc;
        int finalSegTime = 0;
        double finalSegCost = 0.0;
        if (finalSegDist < 3.0) {
            finalSegTime = (int) Math.ceil((finalSegDist / walkingSpeed) * 60);
            finalSegCost = passenger.adjustCost(0.0, false);
            finalSegDesc = "Yürüme";
        } else {
            finalSegTime = (int) Math.ceil((finalSegDist / taxiSpeed) * 60);
            finalSegCost = transitData.getTaxi().getOpeningFee() + finalSegDist * transitData.getTaxi().getCostPerKm();
            finalSegDesc = "Taksi";
        }
        sb.append(String.format("%s -> Varış (bus durağı: %s) -- Mesafe: %.2f km, Süre: %d dk, Ücret: %.2f TL\n",
                finalSegDesc, endBusStop.getName(), finalSegDist, finalSegTime, finalSegCost));
        totalDistance += finalSegDist;
        totalTime += finalSegTime;
        totalCost += finalSegCost;

        sb.append("-------------------------");
        return new Route(sb.toString(), totalDistance, totalTime, totalCost);
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
