import com.google.gson.Gson;
import java.awt.*;
import java.awt.event.*;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
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
    private JTextField creditCardLimitField;
    private JTextField cashAmountField;
    private JTextField kentkartBalanceField;

    // 5 rota butonu
    private JButton busRouteButton;
    private JButton taxiRouteButton;
    private JButton tramRouteButton;
    private JButton busTramRouteButton;
    private JButton tramBusRouteButton;

    // Rota detaylarını gösterecek metin alanı (yeşil kısım)
    private JTextArea routeDetailArea;

    // Sağ panel (harita)
    private JMapViewer mapViewer;
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

        // Sol Panel
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setPreferredSize(new Dimension(300, getHeight()));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(leftPanel, BorderLayout.WEST);

        // Form Paneli
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Ad:"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField(15);
        formPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Yaş:"), gbc);
        gbc.gridx = 1;
        ageField = new JTextField(15);
        formPanel.add(ageField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Yolcu Tipi:"), gbc);
        gbc.gridx = 1;
        passengerTypeCombo = new JComboBox<>(new String[]{"General", "Elder", "Student"});
        formPanel.add(passengerTypeCombo, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Ödeme Yöntemi:"), gbc);
        gbc.gridx = 1;
        paymentTypeCombo = new JComboBox<>(new String[]{"Credit Card", "Kentkart", "Cash"});
        formPanel.add(paymentTypeCombo, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Kredi Kartı Limiti:"), gbc);
        gbc.gridx = 1;
        creditCardLimitField = new JTextField(15);
        creditCardLimitField.setText("0.0");
        formPanel.add(creditCardLimitField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Nakit Miktarı:"), gbc);
        gbc.gridx = 1;
        cashAmountField = new JTextField(15);
        cashAmountField.setText("0.0");
        formPanel.add(cashAmountField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Kentkart Bakiyesi:"), gbc);
        gbc.gridx = 1;
        kentkartBalanceField = new JTextField(15);
        kentkartBalanceField.setText("0.0");
        formPanel.add(kentkartBalanceField, gbc);

        leftPanel.add(formPanel);

        // Orta Kısım: Rota Hesapla / Reset
        JPanel topButtonPanel = new JPanel(new FlowLayout());
        JButton planRouteButton = new JButton("Rota Hesapla");
        JButton resetButton = new JButton("Reset");
        topButtonPanel.add(planRouteButton);
        topButtonPanel.add(resetButton);
        leftPanel.add(topButtonPanel);

        // Alt Kısım: 5 Rota Butonları + Rota Detay Alanı
        JPanel routeSectionPanel = new JPanel();
        routeSectionPanel.setLayout(new BoxLayout(routeSectionPanel, BoxLayout.Y_AXIS));

        busRouteButton = new JButton("Sadece Otobüs");
        taxiRouteButton = new JButton("Sadece Taksi");
        tramRouteButton = new JButton("Sadece Tramvay");
        busTramRouteButton = new JButton("Otobüs + Tramvay");
        tramBusRouteButton = new JButton("Tramvay + Otobüs");

        routeSectionPanel.add(busRouteButton);
        routeSectionPanel.add(Box.createRigidArea(new Dimension(0,5)));
        routeSectionPanel.add(taxiRouteButton);
        routeSectionPanel.add(Box.createRigidArea(new Dimension(0,5)));
        routeSectionPanel.add(tramRouteButton);
        routeSectionPanel.add(Box.createRigidArea(new Dimension(0,5)));
        routeSectionPanel.add(busTramRouteButton);
        routeSectionPanel.add(Box.createRigidArea(new Dimension(0,5)));
        routeSectionPanel.add(tramBusRouteButton);
        routeSectionPanel.add(Box.createRigidArea(new Dimension(0,10)));

        routeDetailArea = new JTextArea(15, 25);
        routeDetailArea.setEditable(false);
        routeDetailArea.setLineWrap(true);
        routeDetailArea.setWrapStyleWord(true);
        JScrollPane routeDetailScroll = new JScrollPane(routeDetailArea);
        routeSectionPanel.add(routeDetailScroll);

        leftPanel.add(routeSectionPanel, BorderLayout.SOUTH);

        // Sağ Panel: Harita
        mapViewer = new JMapViewer();
        mapViewer.setDisplayPosition(new Coordinate(40.78, 29.95), 12);
        mapViewer.setZoomContolsVisible(true);

        // Durakları göster
        for (Stop stop : transitData.getDuraklar()) {
            Coordinate coord = new Coordinate(stop.getLat(), stop.getLon());
            MapMarkerDot marker = new MapMarkerDot(stop.getName(), coord);
            mapViewer.addMapMarker(marker);
        }

        // MouseAdapter
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

        // Buton aksiyonları
        planRouteButton.addActionListener(e -> computeAndDisplayRoutes());
        resetButton.addActionListener(e -> resetSelections());

        // Rota butonlarının aksiyonları
        busRouteButton.addActionListener(e -> {
            showBusRoute();
            drawBusRouteOnMap(createPassengerFromForm());
        });
        taxiRouteButton.addActionListener(e -> {
            showTaxiRoute();
            drawTaxiRouteOnMap(createPassengerFromForm());
        });
        tramRouteButton.addActionListener(e -> {
            showTramRoute();
            drawTramRouteOnMap(createPassengerFromForm());
        });
        busTramRouteButton.addActionListener(e -> {
            showBusTramRoute();
            drawBusTramRouteOnMap(createPassengerFromForm());
        });
        tramBusRouteButton.addActionListener(e -> {
            showTramBusRoute();
            drawTramBusRouteOnMap(createPassengerFromForm());
        });
    }

    // Rota Hesapla butonu
    private void computeAndDisplayRoutes() {
        Passenger passenger = createPassengerFromForm();
        if(passenger.getCurrentLocation() == null || passenger.getDestination() == null) {
            JOptionPane.showMessageDialog(this, "Lütfen harita üzerinde başlangıç ve varış noktalarını seçiniz.");
            return;
        }
        List<Route> routes = TravelPlanner.planRoutes(passenger, transitData);

        if (routes.isEmpty()) {
            routeDetailArea.setText("Hiçbir rota bulunamadı.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Route r : routes) {
            // Rota açıklamasını ve altına toplam değerleri ekleyelim
            sb.append(r.getDescription())
                    .append("\nToplam Mesafe: ").append(String.format("%.2f", r.getTotalDistance())).append(" km")
                    .append(", Toplam Ücret: ").append(String.format("%.2f", r.getTotalCost())).append(" TL")
                    .append(", Toplam Süre: ").append(r.getTotalTime()).append(" dk\n\n");
        }
        routeDetailArea.setText(sb.toString());
    }

    // Reset butonu
    private void resetSelections() {
        nameField.setText("");
        ageField.setText("");
        passengerTypeCombo.setSelectedIndex(0);
        paymentTypeCombo.setSelectedIndex(0);
        creditCardLimitField.setText("0.0");
        cashAmountField.setText("0.0");
        kentkartBalanceField.setText("0.0");
        routeDetailArea.setText("");

        mapViewer.removeAllMapPolygons(); // MyMapPolyline poligon gibi göründüğü için
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

    // Yolcu oluşturma
    private Passenger createPassengerFromForm() {
        String name = nameField.getText().trim();
        int age = 0;
        try {
            age = Integer.parseInt(ageField.getText().trim());
        } catch (NumberFormatException e) { }
        Location currentLoc = (startCoordinate != null)
                ? new Location(startCoordinate.getLat(), startCoordinate.getLon())
                : null;
        Location destLoc = (destinationCoordinate != null)
                ? new Location(destinationCoordinate.getLat(), destinationCoordinate.getLon())
                : null;

        String passengerType = (String) passengerTypeCombo.getSelectedItem();
        Passenger passenger;
        switch (passengerType) {
            case "Elder":
                passenger = new ElderPassenger(name, age, currentLoc, destLoc);
                break;
            case "Student":
                passenger = new StudentPassenger(name, age, currentLoc, destLoc);
                break;
            default:
                passenger = new GeneralPassenger(name, age, currentLoc, destLoc);
                break;
        }
        return passenger;
    }

    // Ödeme işlemi
    private void processPayment(String routeName, double cost, Route route) {
        String paymentMethod = (String) paymentTypeCombo.getSelectedItem();
        JTextField balanceField;
        switch (paymentMethod) {
            case "Credit Card":
                balanceField = creditCardLimitField;
                break;
            case "Kentkart":
                balanceField = kentkartBalanceField;
                break;
            case "Cash":
            default:
                balanceField = cashAmountField;
                break;
        }
        double balance = 0.0;
        try {
            balance = Double.parseDouble(balanceField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Geçersiz bakiye değeri.");
            return;
        }
        if (balance < cost) {
            JOptionPane.showMessageDialog(this,
                    "Yetersiz bakiye. (" + paymentMethod + " bakiyesi: " + balance + ")");
            return;
        }
        balance -= cost;
        balanceField.setText(String.format("%.2f", balance));

        // Rota detayına ek olarak toplam mesafe, süre, ücret bilgisi
        routeDetailArea.setText(routeName + " rotası için ödeme yapıldı.\nKesilen ücret: "
                + String.format("%.2f", cost) + " TL\nKalan bakiye: " + String.format("%.2f", balance)
                + "\n\nToplam Mesafe: " + String.format("%.2f", route.getTotalDistance())
                + " km, Toplam Süre: " + route.getTotalTime() + " dk, Toplam Ücret: "
                + String.format("%.2f", route.getTotalCost()) + " TL");
    }

    // "Sadece Otobüs" butonu
    private void showBusRoute() {
        Passenger passenger = createPassengerFromForm();
        if (passenger.getCurrentLocation() == null || passenger.getDestination() == null) {
            JOptionPane.showMessageDialog(this, "Lütfen harita üzerinde başlangıç ve varış noktalarını seçiniz.");
            return;
        }
        Route route = TravelPlanner.formatBusRoute(passenger, transitData);
        if (route != null) {
            processPayment("Sadece Otobüs", route.getTotalCost(), route);
        } else {
            routeDetailArea.setText("Otobüs rotası bulunamadı.");
        }
    }

    private void showTaxiRoute() {
        Passenger passenger = createPassengerFromForm();
        if (passenger.getCurrentLocation() == null || passenger.getDestination() == null) {
            JOptionPane.showMessageDialog(this, "Lütfen harita üzerinde başlangıç ve varış noktalarını seçiniz.");
            return;
        }
        Route route = TravelPlanner.formatTaxiRoute(passenger, transitData);
        if (route != null) {
            processPayment("Sadece Taksi", route.getTotalCost(), route);
        } else {
            routeDetailArea.setText("Taksi rotası bulunamadı.");
        }
    }

    private void showTramRoute() {
        Passenger passenger = createPassengerFromForm();
        if (passenger.getCurrentLocation() == null || passenger.getDestination() == null) {
            JOptionPane.showMessageDialog(this, "Lütfen harita üzerinde başlangıç ve varış noktalarını seçiniz.");
            return;
        }
        Route route = TravelPlanner.formatTramRoute(passenger, transitData);
        if (route != null) {
            processPayment("Sadece Tramvay", route.getTotalCost(), route);
        } else {
            routeDetailArea.setText("Tramvay rotası bulunamadı.");
        }
    }

    private void showBusTramRoute() {
        Passenger passenger = createPassengerFromForm();
        if (passenger.getCurrentLocation() == null || passenger.getDestination() == null) {
            JOptionPane.showMessageDialog(this, "Lütfen harita üzerinde başlangıç ve varış noktalarını seçiniz.");
            return;
        }
        Route route = TravelPlanner.formatBusTramTransferRoute(passenger, transitData);
        if (route != null) {
            processPayment("Otobüs + Tramvay", route.getTotalCost(), route);
        } else {
            routeDetailArea.setText("Otobüs + Tramvay rotası bulunamadı.");
        }
    }

    private void showTramBusRoute() {
        Passenger passenger = createPassengerFromForm();
        if (passenger.getCurrentLocation() == null || passenger.getDestination() == null) {
            JOptionPane.showMessageDialog(this, "Lütfen harita üzerinde başlangıç ve varış noktalarını seçiniz.");
            return;
        }
        Route route = TravelPlanner.formatTramBusTransferRoute(passenger, transitData);
        if (route != null) {
            processPayment("Tramvay + Otobüs", route.getTotalCost(), route);
        } else {
            routeDetailArea.setText("Tramvay + Otobüs rotası bulunamadı.");
        }
    }

    // -- Haritada MyMapPolyline ile çizim metotları --
    private void clearRouteOnMap() {
        mapViewer.removeAllMapPolygons();
    }

    /**
     * Koordinat listesini alıp harita üzerinde bir poliline çizer.
     * Rota türüne göre (taxi, bus vs.) renk atayabilirsiniz.
     */
    private void drawPolyline(List<Coordinate> coords, Color lineColor) {
        clearRouteOnMap();
        MyMapPolyline polyline = new MyMapPolyline(coords);
        polyline.setColor(lineColor);
        // Örn. polyline.setStroke(new BasicStroke(3f));
        mapViewer.addMapPolygon(polyline);
        mapViewer.repaint();
    }

    private void drawTaxiRouteOnMap(Passenger passenger) {
        if (passenger.getCurrentLocation() == null || passenger.getDestination() == null) {
            return;
        }
        clearRouteOnMap();

        ArrayList<Coordinate> coords = new ArrayList<>();
        Coordinate start = new Coordinate(
                passenger.getCurrentLocation().getLatitude(),
                passenger.getCurrentLocation().getLongitude());
        Coordinate dest = new Coordinate(
                passenger.getDestination().getLatitude(),
                passenger.getDestination().getLongitude());
        coords.add(start);
        coords.add(dest);
        // Kapalı poligon oluşturmak için başlangıç noktasını da ekliyoruz
        coords.add(start);

        MyMapPolyline taxiPolyline = new MyMapPolyline(coords);
        taxiPolyline.setColor(Color.YELLOW);
        mapViewer.addMapPolygon(taxiPolyline);
        mapViewer.repaint();
    }



    private void drawBusRouteOnMap(Passenger passenger) {
        if(passenger.getCurrentLocation() == null || passenger.getDestination() == null) return;
        BusStopGraph busGraph = new BusStopGraph(transitData);
        Stop startBusStop = TravelPlanner.findNearestStop(
                passenger.getCurrentLocation(), busGraph.getBusStops().values());
        Stop endBusStop = TravelPlanner.findNearestStop(
                passenger.getDestination(), busGraph.getBusStops().values());
        if (startBusStop == null || endBusStop == null) return;

        List<String> path = TravelPlanner.bfsBusPath(
                busGraph.getAdjacencyList(), startBusStop.getId(), endBusStop.getId());
        if (path == null) return;

        ArrayList<Coordinate> coords = new ArrayList<>();

        // 1. Adım: Kullanıcının mevcut konumu (yürüyüş başlangıcı)
        coords.add(new Coordinate(
                passenger.getCurrentLocation().getLatitude(),
                passenger.getCurrentLocation().getLongitude()));

        // 2. Adım: Kullanıcının en yakın otobüs durağı (yürüyüş segmentinin bitişi)
        coords.add(new Coordinate(startBusStop.getLat(), startBusStop.getLon()));

        // 3. Adım: Otobüs durağından durağa BFS ile bulunan rota (otobüs segmenti)
        for (String stopId : path) {
            Stop s = busGraph.getBusStops().get(stopId);
            coords.add(new Coordinate(s.getLat(), s.getLon()));
        }

        // 4. Adım: Varış noktasına en yakın otobüs durağı (yürüyüş segmentinin başlangıcı)
        coords.add(new Coordinate(endBusStop.getLat(), endBusStop.getLon()));

        // 5. Adım: Kullanıcının varış noktası (yürüyüş bitişi)
        coords.add(new Coordinate(
                passenger.getDestination().getLatitude(),
                passenger.getDestination().getLongitude()));

        // Bu koordinatlar üzerinden yeşil renkte bir polyline çiziliyor
        drawPolyline(coords, Color.GREEN);
    }


    private void drawTramRouteOnMap(Passenger passenger) {
        if(passenger.getCurrentLocation() == null || passenger.getDestination() == null) return;
        TramStopGraph tramGraph = new TramStopGraph(transitData);
        Stop startTramStop = TravelPlanner.findNearestStop(
                passenger.getCurrentLocation(), tramGraph.getTramStops().values());
        Stop endTramStop = TravelPlanner.findNearestStop(
                passenger.getDestination(), tramGraph.getTramStops().values());
        if (startTramStop == null || endTramStop == null) return;

        List<String> path = TravelPlanner.bfsTramPath(
                tramGraph.getAdjacencyList(), startTramStop.getId(), endTramStop.getId());
        if (path == null) return;

        ArrayList<Coordinate> coords = new ArrayList<>();
        coords.add(new Coordinate(
                passenger.getCurrentLocation().getLatitude(),
                passenger.getCurrentLocation().getLongitude()));
        for (String stopId : path) {
            Stop s = tramGraph.getTramStops().get(stopId);
            coords.add(new Coordinate(s.getLat(), s.getLon()));
        }
        // Son tramvay durağından varış noktasına
        coords.add(new Coordinate(
                passenger.getDestination().getLatitude(),
                passenger.getDestination().getLongitude()));

        // Tramvay çizgisini yeşil çizelim
        drawPolyline(coords, Color.BLUE);
    }

    private void drawBusTramRouteOnMap(Passenger passenger) {
        if(passenger.getCurrentLocation() == null || passenger.getDestination() == null) return;
        BusStopGraph busGraph = new BusStopGraph(transitData);
        Stop startBusStop = TravelPlanner.findNearestStop(
                passenger.getCurrentLocation(), busGraph.getBusStops().values());
        Stop endBusStop = TravelPlanner.findNearestStop(
                passenger.getDestination(), busGraph.getBusStops().values());
        if (startBusStop == null || endBusStop == null) return;

        List<String> path = TravelPlanner.bfsBusPath(
                busGraph.getAdjacencyList(), startBusStop.getId(), endBusStop.getId());
        if (path == null) return;

        TramStopGraph tramGraph = new TramStopGraph(transitData);
        Stop startTramStop = TravelPlanner.findNearestStop(
                passenger.getCurrentLocation(), tramGraph.getTramStops().values());
        Stop endTramStop = TravelPlanner.findNearestStop(
                passenger.getDestination(), tramGraph.getTramStops().values());
        if (startTramStop == null || endTramStop == null) return;

        path = TravelPlanner.bfsTramPath(
                tramGraph.getAdjacencyList(), startTramStop.getId(), endTramStop.getId());
        if (path == null) return;

        ArrayList<Coordinate> coords = new ArrayList<>();

        // 1. Adım: Kullanıcının mevcut konumu (yürüyüş başlangıcı)
        coords.add(new Coordinate(
                passenger.getCurrentLocation().getLatitude(),
                passenger.getCurrentLocation().getLongitude()));

        // 2. Adım: Kullanıcının en yakın otobüs durağı (yürüyüş segmentinin bitişi)
        coords.add(new Coordinate(startBusStop.getLat(), startBusStop.getLon()));

        for (String stopId : path) {
            Stop s = tramGraph.getTramStops().get(stopId);
            coords.add(new Coordinate(s.getLat(), s.getLon()));
        }
        // Son tramvay durağından varış noktasına
        coords.add(new Coordinate(
                passenger.getDestination().getLatitude(),
                passenger.getDestination().getLongitude()));


        // Otobüs+Tramvay çizgisini turuncu yapalım
        drawPolyline(coords, Color.ORANGE);
    }


    private void drawTramBusRouteOnMap(Passenger passenger) {
        TramStopGraph tramGraph = new TramStopGraph(transitData);
        Stop startTramStop = TravelPlanner.findNearestStop(
                passenger.getCurrentLocation(), tramGraph.getTramStops().values());
        Stop endTramStop = TravelPlanner.findNearestStop(
                passenger.getDestination(), tramGraph.getTramStops().values());
        if (startTramStop == null || endTramStop == null) return;

        List<String> path  = TravelPlanner.bfsTramPath(
                tramGraph.getAdjacencyList(), startTramStop.getId(), endTramStop.getId());
        if (path == null) return;

        if(passenger.getCurrentLocation() == null || passenger.getDestination() == null) return;
        BusStopGraph busGraph = new BusStopGraph(transitData);
        Stop startBusStop = TravelPlanner.findNearestStop(
                passenger.getCurrentLocation(), busGraph.getBusStops().values());
        Stop endBusStop = TravelPlanner.findNearestStop(
                passenger.getDestination(), busGraph.getBusStops().values());
        if (startBusStop == null || endBusStop == null) return;

        path = TravelPlanner.bfsBusPath(
                busGraph.getAdjacencyList(), startBusStop.getId(), endBusStop.getId());
        if (path == null) return;


        ArrayList<Coordinate> coords = new ArrayList<>();

        // 1. Adım: Kullanıcının mevcut konumu (yürüyüş başlangıcı)
        coords.add(new Coordinate(
                passenger.getCurrentLocation().getLatitude(),
                passenger.getCurrentLocation().getLongitude()));

        // 2. Adım: Kullanıcının en yakın otobüs durağı (yürüyüş segmentinin bitişi)
        coords.add(new Coordinate(startTramStop.getLat(), startTramStop.getLon()));

        for (String stopId : path) {
            Stop s = busGraph.getBusStops().get(stopId);
            coords.add(new Coordinate(s.getLat(), s.getLon()));
        }

        coords.add(new Coordinate(endBusStop.getLat(), endBusStop.getLon()));

        // Son tramvay durağından varış noktasına
        coords.add(new Coordinate(
                passenger.getDestination().getLatitude(),
                passenger.getDestination().getLongitude()));


        // Otobüs+Tramvay çizgisini turuncu yapalım
        drawPolyline(coords, Color.ORANGE);
    }




    // JSON'dan TransitData yüklemek için
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
        System.setProperty("http.agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:91.0) Gecko/20100101 Firefox/91.0");
        final TransitData transitData = loadTransitData("C:\\Users\\gb-nb\\OneDrive\\Masaüstü\\veriseti.json");
        SwingUtilities.invokeLater(() -> {
            TransportPlannerUI ui = new TransportPlannerUI(transitData);
            ui.setVisible(true);
        });
    }
}
