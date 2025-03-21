// RouteCalculator.java
import java.util.List;

public class RouteCalculator {
    // Örneğin, güzergâh üzerindeki toplam mesafeyi hesaplayan metot
    public static double calculateRouteDistance(List<Location> route) {
        double totalDistance = 0;
        for (int i = 1; i < route.size(); i++) {
            totalDistance += route.get(i - 1).distanceTo(route.get(i));
        }
        return totalDistance;
    }

    // Gerekirse süre, ücret hesaplamaları gibi diğer metotlar eklenebilir.
}
