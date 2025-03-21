public class Location {
    private double latitude;
    private double longitude;

    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    // Haversine formülü kullanılarak iki nokta arasındaki mesafeyi (km cinsinden) hesaplar.
    public double distanceTo(Location other) {
        final int EARTH_RADIUS_KM = 6371; // Dünya yarıçapı (km)
        double dLat = Math.toRadians(other.getLatitude() - this.getLatitude());
        double dLon = Math.toRadians(other.getLongitude() - this.getLongitude());
        double lat1 = Math.toRadians(this.getLatitude());
        double lat2 = Math.toRadians(other.getLatitude());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
}
