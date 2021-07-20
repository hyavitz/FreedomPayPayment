package payment;

import java.io.IOException;

public interface IPaymentDevice {

    void makePayment(int amount, int tip, int tax) throws IOException, InterruptedException;

    void cancelPayment() throws IOException, InterruptedException;

    void voidPayment() throws IOException, InterruptedException;

    void refundPayment(int amount) throws IOException, InterruptedException;

    void createToken() throws IOException;
}
