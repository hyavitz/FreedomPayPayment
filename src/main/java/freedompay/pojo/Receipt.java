package freedompay.pojo;

import lombok.*;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * This class accepts transaction information and constructs a formatted
 * String to be used on receipts.
 *
 */

@Data
public class Receipt {

    DecimalFormat df = new DecimalFormat("#.00");
    private String vendorName;

    private String invoiceNumber;
    private String transactionDate;

    private Items items;
    private StringBuilder itemDetails = new StringBuilder();

    private String subtotalChargeAmount;
    private String taxChargeAmount;
    private String tipChargeAmount;
    private String totalChargeAmount;

    private String purchaseApprovedDecision;
    private String purchaseApprovedAmount;
    private String purchaseApprovedCardType;
    private String purchaseApprovedCardLastFour;
    private String purchaseApprovedCardEntry;
    private String transactionState;

    public Receipt(String invoiceNumber, Items items, double amount, double tax, double tip, Response response) {

        this.vendorName = "Discovery Cafe";
        this.invoiceNumber = invoiceNumber;
        this.items = items;
        this.subtotalChargeAmount = String.valueOf(amount);
        this.taxChargeAmount = String.valueOf(tax);
        this.tipChargeAmount = String.valueOf(tip);
        this.totalChargeAmount = String.valueOf((amount) + (tax) + (tip));

        Date now = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = simpleDateFormat.format(now);

        this.transactionDate = date;

        double itemPrice = 0.0;

        String padding;
        if (items != null) {
            for (Item item : items.items) {
                if (item != null) {
                    padding = addPadding(Math.round((44 - item.getProductName().length())));
                    itemDetails.append(item.getQuantity() + "\t\t" + item.getProductName() + "\t\t" + "$" + item.getUnitPrice() + "\n");
                    itemPrice += (Double.parseDouble(item.getUnitPrice().replace("$", "")) * Double.parseDouble(item.getQuantity()));
                }
            }
        } else {
            itemDetails.append("Nothing here...\n");
        }

        this.purchaseApprovedDecision = response.getMessage();
        this.purchaseApprovedAmount = response.getApprovedAmount();
        this.purchaseApprovedCardType = response.getCardType();
        this.purchaseApprovedCardLastFour = response.getMaskedCardNumber();
        this.purchaseApprovedCardEntry = response.getEntryMode();
    }

    private String addPadding(int padding) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= padding; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }

    public String getFullReceipt() {
        StringBuilder sb = new StringBuilder();
        String padding;

        padding = addPadding(Math.round(44 - vendorName.length()) / 2);
        sb.append(padding + vendorName + padding + "\n");

        invoiceNumber = "Invoice #" + invoiceNumber;
        padding = addPadding(Math.round(44 - invoiceNumber.length()) / 2);
        sb.append(padding + invoiceNumber + padding);

        sb.append("Transaction Date: " + transactionDate + "\n\n");

        String qnt = "Qty";
        String itemized = "Items";
        String itemPrice = "Price";
        sb.append(qnt + "\t\t" + itemized + "\t\t" + itemPrice + "\n\n");
        sb.append(itemDetails.toString() + "\n");

        sb.append("Subtotal: " + df.format(Double.parseDouble(subtotalChargeAmount)) + "\n");
        sb.append("Tax: " + df.format(Double.parseDouble(taxChargeAmount)) + "\n");
        sb.append("Tip: " + df.format(Double.parseDouble(tipChargeAmount)) + "\n");
        sb.append("Total: " + df.format(Double.parseDouble(totalChargeAmount)) + "\n");
        sb.append("\n");
        sb.append("Decision: " + purchaseApprovedDecision + "\n");
        sb.append(purchaseApprovedCardType + " " + purchaseApprovedCardLastFour + "\t" + df.format(Double.parseDouble(totalChargeAmount)) + "\n");
        sb.append("Method: " + purchaseApprovedCardEntry + "\n\n");
        sb.append("*** Closed ***" + "\n\n\n");

        return sb.toString();
    }
}