package printer;

import pax.payment.PaxS300Receipt;

public class PaxS300Printer {

    //
    public static void printReceipt(String receipt) {

        // TODO: Source printer address from config
        Printable printer = new NetworkPrinter("10.10.50.31", 9100);
        PrinterService printerService = new PrinterService(printer);

        //
        printerService.print(receipt);
        printerService.cutFull();
        printerService.close();
    }
}
