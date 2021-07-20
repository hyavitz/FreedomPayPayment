package payment;

import java.text.SimpleDateFormat;
import java.util.Date;

public class InvoiceNumber {

    public static String generateInvoiceNumber() {
        Date now = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String date = simpleDateFormat.format(now);
        return date;
    }
}
