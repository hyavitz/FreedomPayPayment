package pax.payment;

import com.pax.poslink.PosLink;

public class PaxS300Receipt {

    public static String getReceipt(PosLink posLink, String amount, String tax, String tip) {

        //
        StringBuilder sb = new StringBuilder();

        //
        sb.append("Discovery Cafe\n\n")
                .append("Invoice #")
                .append(posLink.PaymentRequest.InvNum)
                .append("\n")
                .append("Transaction Date: ")
                .append(posLink.PaymentResponse.Timestamp)
                .append("\n\n")
                .append("Qty\tItems\tPrice\n\n")
                .append("1\tSome Really Neat Thing\t$0.00\n\n")
                .append("Subtotal: ")
                .append(amount) // subtotal
                .append("\n")
                .append("Tax: ")
                .append(tax) // tax
                .append("\n")
                .append("Tip: ")
                .append(tip) // tip
                .append("\n")
                .append("Total: ")
                .append(posLink.PaymentResponse.ApprovedAmount)
                .append("\n")
                .append("Decision: ")
                .append(posLink.PaymentResponse.Message)
                .append("\n")
                .append(posLink.PaymentResponse.CardType)
                .append(" ")
                .append(posLink.PaymentResponse.CardInfo)
                .append("\n\n")
                .append("*** Closed ***\n\n");

        return sb.toString();
    }
}