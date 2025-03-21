// CreditCardPayment.java
public class CreditCardPayment implements PaymentMethod {
    private double creditLimit;

    public CreditCardPayment(double creditLimit) {
        this.creditLimit = creditLimit;
    }

    public double getCreditLimit() {
        return creditLimit;
    }

    @Override
    public void processPayment(double amount) {
        System.out.println("Kredi kartı ödemesi işleniyor: " + amount + " TL");
    }
}
