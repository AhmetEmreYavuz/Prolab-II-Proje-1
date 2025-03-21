// CashPayment.java
public class CashPayment implements PaymentMethod {
    private double cashAmount;

    public CashPayment(double cashAmount) {
        this.cashAmount = cashAmount;
    }

    public double getCashAmount() {
        return cashAmount;
    }

    @Override
    public void processPayment(double amount) {
        System.out.println("Nakit ödeme işleniyor: " + amount + " TL");
    }
}
