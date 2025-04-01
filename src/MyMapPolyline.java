import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import java.awt.Color;
import java.util.List;

public class MyMapPolyline extends MapPolygonImpl {
    public MyMapPolyline(List<Coordinate> points) {
        super(points);  // Sadece koordinat listesini gönderiyoruz.
        // Doldurma rengini şeffaf yaparak yalnızca kenar çizgisinin görünmesini sağlıyoruz.
        setBackColor(new Color(0, 0, 0, 0));
    }
}