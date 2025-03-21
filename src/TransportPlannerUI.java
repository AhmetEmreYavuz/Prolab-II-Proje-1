import com.google.gson.Gson;
import java.awt.*;
import java.awt.event.*;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import javax.swing.*;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;

public class TransportPlannerUI extends JFrame {
    private TransitData transitData;

    // Sol panel form bileşenleri
    private JTextField nameField;
    private JTextField ageField;
    private JComboBox<String> passengerTypeCombo;
    private JComboBox<String> paymentTypeCombo;
    private JTextArea routeTextArea;

    // Sağ panel (harita)
    private JMapViewer mapViewer;
    // Harita üzerinde seçilen başlangıç ve varış koordinatlarını saklamak
    private Coordinate startCoordinate = null;
    private Coordinate destinationCoordinate = null;
    private MapMarkerDot startMarker;
    private MapMarkerDot destinationMarker;

    public TransportPlannerUI(TransitData transitData) {
        this.transitData = transitData;
        initComponents();
    }

    private void initComponents() {
        setTitle("Ulaşım Planlayıcı");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // SOL PANEL: Form ve rota sonuçları
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(300, getHeight()));

        // Form paneli: Ad, Yaş, Yolcu Tipi, Ödeme Yöntemi
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Ad
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Ad:"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField(15);
        formPanel.add(nameField, gbc);

        // Yaş
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Yaş:"), gbc);
        gbc.gridx = 1;
        ageField = new JTextField(15);
        formPanel.add(ageField, gbc);

        // Yolcu Tipi
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Yolcu Tipi:"), gbc);
        gbc.gridx = 1;
        passengerTypeCombo = new JComboBox<>(new String[] {"General", "Elder", "Student"});
        formPanel.add(passengerTypeCombo, gbc);

        // Ödeme Yöntemi
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Ödeme Yöntemi:"), gbc);
        gbc.gridx = 1;
        paymentTypeCombo = new JComboBox<>(new String[] {"Credit Card", "Kentkart", "Cash"});
        formPanel.add(paymentTypeCombo, gbc);

        leftPanel.add(formPanel, BorderLayout.NORTH);

        // Buton paneli: Rota Hesapla ve Reset butonları
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton planRouteButton = new JButton("Rota Hesapla");
        JButton resetButton = new JButton("Reset");
        buttonPanel.add(planRouteButton);
        buttonPanel.add(resetButton);
        leftPanel.add(buttonPanel, BorderLayout.CENTER);

        // Rota sonuçlarının gösterileceği metin alanı
        routeTextArea = new JTextArea(10, 25);
        routeTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(routeTextArea);
        leftPanel.add(scrollPane, BorderLayout.SOUTH);

        add(leftPanel, BorderLayout.WEST);

        // SAĞ PANEL: Harita
        mapViewer = new JMapViewer();
        mapViewer.setDisplayPosition(new Coordinate(40.78, 29.95), 12);
        mapViewer.setZoomContolsVisible(true);

        // Transit verisindeki durakların marker'larını ekle
        for (Stop stop : transitData.getDuraklar()) {
            Coordinate coord = new Coordinate(stop.getLat(), stop.getLon());
            MapMarkerDot marker = new MapMarkerDot(stop.getName(), coord);
            mapViewer.addMapMarker(marker);
        }

        // Mouse click & drag işlemlerini yönetecek adapter
        MouseAdapter mapMouseAdapter = new MouseAdapter() {
            private Point dragStart;
            private boolean dragging = false;
            private Point lastDragPoint;

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    dragStart = e.getPoint();
                    lastDragPoint = e.getPoint();
                    dragging = false;
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    int dx = e.getX() - lastDragPoint.x;
                    int dy = e.getY() - lastDragPoint.y;
                    if (Math.abs(dx) > 5 || Math.abs(dy) > 5) {
                        dragging = true;
                    }
                    mapViewer.moveMap(dx, dy);
                    lastDragPoint = e.getPoint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (!dragging && dragStart != null && dragStart.distance(e.getPoint()) < 5) {
                        ICoordinate ic = mapViewer.getPosition(e.getPoint());
                        Coordinate coord = new Coordinate(ic.getLat(), ic.getLon());
                        if (startCoordinate == null) {
                            startCoordinate = coord;
                            startMarker = new MapMarkerDot("Başlangıç", startCoordinate);
                            startMarker.setBackColor(Color.GREEN);
                            mapViewer.addMapMarker(startMarker);
                        } else if (destinationCoordinate == null) {
                            destinationCoordinate = coord;
                            destinationMarker = new MapMarkerDot("Varış", destinationCoordinate);
                            destinationMarker.setBackColor(Color.RED);
                            mapViewer.addMapMarker(destinationMarker);
                        }
                    }
                    dragStart = null;
                    lastDragPoint = null;
                    dragging = false;
                }
            }
        };
        mapViewer.addMouseListener(mapMouseAdapter);
        mapViewer.addMouseMotionListener(mapMouseAdapter);

        JPanel mapPanel = new JPanel(new BorderLayout());
        mapPanel.add(mapViewer, BorderLayout.CENTER);
        add(mapPanel, BorderLayout.CENTER);

        planRouteButton.addActionListener(e -> planRoute());
        resetButton.addActionListener(e -> resetSelections());
    }

    private void planRoute() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen adınızı giriniz.");
            return;
        }
        int age;
        try {
            age = Integer.parseInt(ageField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Geçerli bir yaş giriniz.");
            return;
        }
        if (startCoordinate == null || destinationCoordinate == null) {
            JOptionPane.showMessageDialog(this, "Lütfen başlangıç ve varış konumlarını harita üzerinde seçiniz.");
            return;
        }

        Location currentLocation = new Location(startCoordinate.getLat(), startCoordinate.getLon());
        Location destination = new Location(destinationCoordinate.getLat(), destinationCoordinate.getLon());

        String passengerType = (String) passengerTypeCombo.getSelectedItem();
        Passenger passenger;
        switch (passengerType) {
            case "Elder":
                passenger = new ElderPassenger(name, age, currentLocation, destination);
                break;
            case "Student":
                passenger = new StudentPassenger(name, age, currentLocation, destination);
                break;
            default:
                passenger = new GeneralPassenger(name, age, currentLocation, destination);
                break;
        }

        String paymentType = (String) paymentTypeCombo.getSelectedItem();

        // Tüm alternatif rotaları hesapla (TravelPlanner, güncellenmiş haliyle tüm rotaları içeriyor)
        List<String> routes = TravelPlanner.planRoutes(passenger, transitData);

        StringBuilder sb = new StringBuilder();
        sb.append("Ödeme Türü: ").append(paymentType).append("\n\n");
        for (String route : routes) {
            sb.append(route.toString()).append("\n");
            sb.append("-------------------------------------\n");
        }
        routeTextArea.setText(sb.toString());
    }

    private void resetSelections() {
        nameField.setText("");
        ageField.setText("");
        passengerTypeCombo.setSelectedIndex(0);
        paymentTypeCombo.setSelectedIndex(0);
        routeTextArea.setText("");
        startCoordinate = null;
        destinationCoordinate = null;
        if (startMarker != null) {
            mapViewer.removeMapMarker(startMarker);
            startMarker = null;
        }
        if (destinationMarker != null) {
            mapViewer.removeMapMarker(destinationMarker);
            destinationMarker = null;
        }
        mapViewer.repaint();
    }

    private static TransitData loadTransitData(String filePath) {
        Gson gson = new Gson();
        try (Reader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, TransitData.class);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Veriseti okunamadı: " + e.getMessage());
            System.exit(1);
        }
        return null;
    }

    public static void main(String[] args) {
        System.setProperty("http.agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:91.0) Gecko/20100101 Firefox/91.0");
        final TransitData transitData = loadTransitData("C:\\Users\\gb-nb\\OneDrive\\Masaüstü\\veriseti.json");
        SwingUtilities.invokeLater(() -> {
            TransportPlannerUI ui = new TransportPlannerUI(transitData);
            ui.setVisible(true);
        });
    }
}
