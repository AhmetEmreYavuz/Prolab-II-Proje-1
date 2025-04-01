import com.google.gson.Gson;
import java.awt.*;
import java.awt.event.*;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import javax.swing.*;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import java.util.List;

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

        // SOL PANEL
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(300, getHeight()));
        add(leftPanel, BorderLayout.WEST);

        // --- ÜST KISIM: Form Paneli ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Ad
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Ad:"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField(15);
        formPanel.add(nameField, gbc);

        // Yaş
        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Yaş:"), gbc);
        gbc.gridx = 1;
        ageField = new JTextField(15);
        formPanel.add(ageField, gbc);

        // Yolcu Tipi
        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Yolcu Tipi:"), gbc);
        gbc.gridx = 1;
        passengerTypeCombo = new JComboBox<>(new String[]{"General", "Elder", "Student"});
        formPanel.add(passengerTypeCombo, gbc);

        // Ödeme Yöntemi
        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Ödeme Yöntemi:"), gbc);
        gbc.gridx = 1;
        paymentTypeCombo = new JComboBox<>(new String[]{"Credit Card", "Kentkart", "Cash"});
        formPanel.add(paymentTypeCombo, gbc);

        // Kredi Kartı Limiti
        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Kredi Kartı Limiti:"), gbc);
        gbc.gridx = 1;
        creditCardLimitField = new JTextField(15);
        creditCardLimitField.setText("0.0");
        formPanel.add(creditCardLimitField, gbc);

        // Nakit Miktarı
        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Nakit Miktarı:"), gbc);
        gbc.gridx = 1;
        cashAmountField = new JTextField(15);
        cashAmountField.setText("0.0");
        formPanel.add(cashAmountField, gbc);

        // Kentkart Bakiyesi
        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Kentkart Bakiyesi:"), gbc);
        gbc.gridx = 1;
        kentkartBalanceField = new JTextField(15);
        kentkartBalanceField.setText("0.0");
        formPanel.add(kentkartBalanceField, gbc);

        leftPanel.add(formPanel, BorderLayout.NORTH);

        // --- ORTA KISIM: Rota Hesapla ve Reset butonları ---
        JPanel topButtonPanel = new JPanel(new FlowLayout());
        JButton planRouteButton = new JButton("Rota Hesapla");
        JButton resetButton = new JButton("Reset");
        topButtonPanel.add(planRouteButton);
        topButtonPanel.add(resetButton);
        leftPanel.add(topButtonPanel, BorderLayout.CENTER);

        // --- ALT KISIM: 5 ROTA BUTONU + ROTA DETAY ALANI ---
        JPanel routeSectionPanel = new JPanel();
        routeSectionPanel.setLayout(new BoxLayout(routeSectionPanel, BoxLayout.Y_AXIS));
        leftPanel.add(routeSectionPanel, BorderLayout.SOUTH);

        // 5 butonu (kırmızı kısım) dikey yerleştirelim
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

        // Rota detaylarını gösterecek metin alanı (yeşil kısım)
        routeDetailArea = new JTextArea(15, 25);
        routeDetailArea.setEditable(false);
        routeDetailArea.setLineWrap(true);
        routeDetailArea.setWrapStyleWord(true);
        JScrollPane routeDetailScroll = new JScrollPane(routeDetailArea);
        routeSectionPanel.add(routeDetailScroll);

        // --- SAĞ PANEL: Harita ---
        mapViewer = new JMapViewer();
        mapViewer.setDisplayPosition(new Coordinate(40.78, 29.95), 12);
        mapViewer.setZoomContolsVisible(true);
        for (Stop stop : transitData.getDuraklar()) {
            Coordinate coord = new Coordinate(stop.getLat(), stop.getLon());
            MapMarkerDot marker = new MapMarkerDot(stop.getName(), coord);
            mapViewer.addMapMarker(marker);
        }
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
        add(mapViewer, BorderLayout.CENTER);

        // Butonların aksiyonları
        planRouteButton.addActionListener(e -> planRoute());
        resetButton.addActionListener(e -> resetSelections());

        // 5 rota butonunun aksiyonları (ödemeyi gerçekleştirecek şekilde güncellendi)
        busRouteButton.addActionListener(e -> showBusRoute());
        taxiRouteButton.addActionListener(e -> showTaxiRoute());
        tramRouteButton.addActionListener(e -> showTramRoute());
        busTramRouteButton.addActionListener(e -> showBusTramRoute());
        tramBusRouteButton.addActionListener(e -> showTramBusRoute());
    }

    // Rota hesapla butonuna basıldığında her bir rota için açıklamanın altına toplam mesafe, ücret ve süre bilgilerini ekler.
    private void planRoute() {
        Passenger passenger = createPassengerFromForm();
        if(passenger.getCurrentLocation() == null || passenger.getDestination() == null) {
            JOptionPane.showMessageDialog(this, "Lütfen harita üzerinde başlangıç ve varış noktalarını seçiniz.");
            return;
        }
        StringBuilder sb = new StringBuilder();

        Route busRoute = TravelPlanner.formatBusRoute(passenger, transitData);
        sb.append("Sadece Otobüs: ");
        if (busRoute != null) {
            sb.append(busRoute.getDescription());
            sb.append("\nToplam Mesafe: ").append(busRoute.getTotalDistance());
            sb.append(" km, Toplam Ücret: ").append(busRoute.getTotalCost());
            sb.append(" TL, Toplam Süre: ").append(busRoute.getTotalTime()).append(" dk");
        } else {
            sb.append("Bulunamadı");
        }
        sb.append("\n\n");

        Route taxiRoute = TravelPlanner.formatTaxiRoute(passenger, transitData);
        sb.append("Sadece Taksi: ");
        if (taxiRoute != null) {
            sb.append(taxiRoute.getDescription());
            sb.append("\nToplam Mesafe: ").append(taxiRoute.getTotalDistance());
            sb.append(" km, Toplam Ücret: ").append(taxiRoute.getTotalCost());
            sb.append(" TL, Toplam Süre: ").append(taxiRoute.getTotalTime()).append(" dk");
        } else {
            sb.append("Bulunamadı");
        }
        sb.append("\n\n");

        Route tramRoute = TravelPlanner.formatTramRoute(passenger, transitData);
        sb.append("Sadece Tramvay: ");
        if (tramRoute != null) {
            sb.append(tramRoute.getDescription());
            sb.append("\nToplam Mesafe: ").append(tramRoute.getTotalDistance());
            sb.append(" km, Toplam Ücret: ").append(tramRoute.getTotalCost());
            sb.append(" TL, Toplam Süre: ").append(tramRoute.getTotalTime()).append(" dk");
        } else {
            sb.append("Bulunamadı");
        }
        sb.append("\n\n");

        Route busTramRoute = TravelPlanner.formatBusTramTransferRoute(passenger, transitData);
        sb.append("Otobüs + Tramvay: ");
        if (busTramRoute != null) {
            sb.append(busTramRoute.getDescription());
            sb.append("\nToplam Mesafe: ").append(busTramRoute.getTotalDistance());
            sb.append(" km, Toplam Ücret: ").append(busTramRoute.getTotalCost());
            sb.append(" TL, Toplam Süre: ").append(busTramRoute.getTotalTime()).append(" dk");
        } else {
            sb.append("Bulunamadı");
        }
        sb.append("\n\n");

        Route tramBusRoute = TravelPlanner.formatTramBusTransferRoute(passenger, transitData);
        sb.append("Tramvay + Otobüs: ");
        if (tramBusRoute != null) {
            sb.append(tramBusRoute.getDescription());
            sb.append("\nToplam Mesafe: ").append(tramBusRoute.getTotalDistance());
            sb.append(" km, Toplam Ücret: ").append(tramBusRoute.getTotalCost());
            sb.append(" TL, Toplam Süre: ").append(tramBusRoute.getTotalTime()).append(" dk");
        } else {
            sb.append("Bulunamadı");
        }

        routeDetailArea.setText(sb.toString());
    }

    // Reset butonu tüm form alanlarını, seçimleri ve harita işaretçilerini sıfırlar.
    private void resetSelections() {
        nameField.setText("");
        ageField.setText("");
        passengerTypeCombo.setSelectedIndex(0);
        paymentTypeCombo.setSelectedIndex(0);
        creditCardLimitField.setText("0.0");
        cashAmountField.setText("0.0");
        kentkartBalanceField.setText("0.0");
        routeDetailArea.setText("");

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

    // Formdaki verilerden Passenger nesnesi oluşturur.
    private Passenger createPassengerFromForm() {
        String name = nameField.getText().trim();
        int age = 0;
        try {
            age = Integer.parseInt(ageField.getText().trim());
        } catch (NumberFormatException e) {
            // Hata durumunda 0 olarak kalabilir
        }
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

    // Seçili ödeme yöntemine göre, rota için hesaplanan toplam ücret kadar bakiyeden düşüm yapar ve kalan bakiyeyi metin alanında gösterir.
    private void processPayment(String routeName, double cost) {
        String paymentMethod = (String) paymentTypeCombo.getSelectedItem();
        JTextField balanceField = null;
        if(paymentMethod.equals("Credit Card")) {
            balanceField = creditCardLimitField;
        } else if(paymentMethod.equals("Kentkart")) {
            balanceField = kentkartBalanceField;
        } else if(paymentMethod.equals("Cash")) {
            balanceField = cashAmountField;
        }
        double balance = 0.0;
        try {
            balance = Double.parseDouble(balanceField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Geçersiz bakiye değeri.");
            return;
        }
        if (balance < cost) {
            JOptionPane.showMessageDialog(this, "Yetersiz bakiye. (" + paymentMethod + " bakiye: " + balance + ")");
            return;
        }
        balance -= cost;
        balanceField.setText(String.format("%.2f", balance));
        routeDetailArea.setText(routeName + " rotası için ödeme yapıldı. Kesilen ücret: "
                + cost + ". Kalan bakiye: " + String.format("%.2f", balance));
    }

    // Aşağıdaki metodlar ilgili rota hesaplamasını yapar; hesaplanan rota varsa, rota için belirlenen toplam ücreti bakiyeden düşer.
    private void showBusRoute() {
        Passenger passenger = createPassengerFromForm();
        if(passenger.getCurrentLocation() == null || passenger.getDestination() == null) {
            JOptionPane.showMessageDialog(this, "Lütfen harita üzerinde başlangıç ve varış noktalarını seçiniz.");
            return;
        }
        Route route = TravelPlanner.formatBusRoute(passenger, transitData);
        if (route != null) {
            processPayment("Sadece Otobüs", route.getTotalCost());
        } else {
            routeDetailArea.setText("Otobüs rotası bulunamadı.");
        }
    }

    private void showTaxiRoute() {
        Passenger passenger = createPassengerFromForm();
        if(passenger.getCurrentLocation() == null || passenger.getDestination() == null) {
            JOptionPane.showMessageDialog(this, "Lütfen harita üzerinde başlangıç ve varış noktalarını seçiniz.");
            return;
        }
        Route route = TravelPlanner.formatTaxiRoute(passenger, transitData);
        if (route != null) {
            processPayment("Sadece Taksi", route.getTotalCost());
        } else {
            routeDetailArea.setText("Taksi rotası bulunamadı.");
        }
    }

    private void showTramRoute() {
        Passenger passenger = createPassengerFromForm();
        if(passenger.getCurrentLocation() == null || passenger.getDestination() == null) {
            JOptionPane.showMessageDialog(this, "Lütfen harita üzerinde başlangıç ve varış noktalarını seçiniz.");
            return;
        }
        Route route = TravelPlanner.formatTramRoute(passenger, transitData);
        if (route != null) {
            processPayment("Sadece Tramvay", route.getTotalCost());
        } else {
            routeDetailArea.setText("Tramvay rotası bulunamadı.");
        }
    }

    private void showBusTramRoute() {
        Passenger passenger = createPassengerFromForm();
        if(passenger.getCurrentLocation() == null || passenger.getDestination() == null) {
            JOptionPane.showMessageDialog(this, "Lütfen harita üzerinde başlangıç ve varış noktalarını seçiniz.");
            return;
        }
        Route route = TravelPlanner.formatBusTramTransferRoute(passenger, transitData);
        if (route != null) {
            processPayment("Otobüs + Tramvay", route.getTotalCost());
        } else {
            routeDetailArea.setText("Otobüs + Tramvay rotası bulunamadı.");
        }
    }

    private void showTramBusRoute() {
        Passenger passenger = createPassengerFromForm();
        if(passenger.getCurrentLocation() == null || passenger.getDestination() == null) {
            JOptionPane.showMessageDialog(this, "Lütfen harita üzerinde başlangıç ve varış noktalarını seçiniz.");
            return;
        }
        Route route = TravelPlanner.formatTramBusTransferRoute(passenger, transitData);
        if (route != null) {
            processPayment("Tramvay + Otobüs", route.getTotalCost());
        } else {
            routeDetailArea.setText("Tramvay + Otobüs rotası bulunamadı.");
        }
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
        System.setProperty("http.agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:91.0) Gecko/20100101 Firefox/91.0");
        final TransitData transitData = loadTransitData("C:\\Users\\gb-nb\\OneDrive\\Masaüstü\\veriseti.json");
        SwingUtilities.invokeLater(() -> {
            TransportPlannerUI ui = new TransportPlannerUI(transitData);
            ui.setVisible(true);
        });
    }
}