import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import java.awt.Color;
import java.util.List;

public class MyMapPolyline extends MapPolygonImpl {

    public MyMapPolyline(List<Coordinate> points) {
        super(points);
        // Dolgu rengini tamamen şeffaf yaparak yalnızca kenar çizgisini görünür kılıyoruz
        setBackColor(new Color(0, 0, 0, 0));
        // İsterseniz kenar çizgisinin rengini de ayarlayabilirsiniz, örneğin:
        // setColor(Color.RED);
    }
}
