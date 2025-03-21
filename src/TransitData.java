// TransitData.java
import java.util.List;

public class TransitData {
    private String city;
    private TaxiFare taxi;
    private List<Stop> duraklar;

    // Getters and setters
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public TaxiFare getTaxi() { return taxi; }
    public void setTaxi(TaxiFare taxi) { this.taxi = taxi; }

    public List<Stop> getDuraklar() { return duraklar; }
    public void setDuraklar(List<Stop> duraklar) { this.duraklar = duraklar; }
}
