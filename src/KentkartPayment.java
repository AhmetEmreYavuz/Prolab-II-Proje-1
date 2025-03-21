// KentkartPayment.java
public class KentkartPayment implements PaymentMethod {
    private double balance;

    public KentkartPayment(double balance) {
        this.balance = balance;
    }

    public double getBalance() {
        return balance;
    }

    @Override
    public void processPayment(double amount) {
        System.out.println("Kentkart ödemesi işleniyor: " + amount + " TL");
    }
}
