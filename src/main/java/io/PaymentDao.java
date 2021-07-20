package io;

import payment.PaymentDetails;

public class PaymentDao {

    public static PaymentDetails getPaymentDetails(String file_name) {
        PaymentDetails paymentDetails = new PaymentDetails();
        String[] data = PaymentDB.loadData(file_name, true);
        paymentDetails.setDecision(data[0]);
        paymentDetails.setMessage(data[1]);
        paymentDetails.setInvoice(data[2]);
        return paymentDetails;
    }

    public static void setPaymentDetails(PaymentDetails paymentDetails, String file_name) {
        String[] data = new String[3];
        data[0] = paymentDetails.getDecision();
        data[1] = paymentDetails.getMessage();
        data[2] = paymentDetails.getInvoice();
        if (!PaymentDB.saveData(data, file_name)) {
            System.out.println("Error saving payment details");
        }
    }

    public static String getTicketData(String file_name) {
        return PaymentDB.loadData(file_name);
    }

    public static void setTicketData(String data, String file_name) {
        if (!PaymentDB.saveData(data, file_name)) {
            System.out.println("Error saving ticket data");
        }
    }
}