package worldpay;

import interceptor.AlertDialogStyle;
import interceptor.CommandListener;
import payment.PaymentDetails;
import payment.PaymentUtil;
import payment.IPaymentDevice;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class WorldPayPaymentDevice implements IPaymentDevice {

    private final int endOfTransaction = 0; //always zero

    //the communication socket
    Socket socket;

    //printWriter for comm port
    PrintWriter printWriter;

    //printWriter for message port
    PrintWriter printWriterMessage;

    //ArrayList to hold receipts
    ArrayList<String> receipts = new ArrayList<String>();

    //Fields to hold required variables for construction of transaction request
    private int tip = 0; //add these to get actual amount
    private int tax = 0; //add these to get actual amount

    private int transactionId;

    private int saleType = 0; //zero is sale 3 is cancel
    private int amount = 0;

    private boolean isThreeReceipts = false;

    public Socket getSocket() {
        return socket;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    /**
     * @param socket        socket that will communicate with IPC.  Is set to default port 10000, this can be changed in YESEFTCONFIG.bat
     * @param transactionId int number for transaction (generated by spos)
     * @param amount        float total amount to charge to card
     * @throws IOException
     */
    public static void sendPayment(PrintWriter printWriter, Socket socket, int transactionId, Float amount) throws IOException {

        System.out.println("WP is sending payment to socket!");
        // Get the output stream for sending to client, below is the sale format for p400
        printWriter = new PrintWriter(socket.getOutputStream(), true);
        printWriter.write("Transaction-Sale" + "\n" + "\n");
        printWriter.write("1=" + transactionId + "\n");
        printWriter.write("2=" + 0 + "\n");
        printWriter.write("3=" + amount + "\n");
        printWriter.write("99=" + 0 + "\n");
        printWriter.flush();

        printWriter.close();
    }

    /**
     * @param amount amount of the transaction
     * @param tip    amount of tip involved in transaction
     * @param tax    amount of tax involved in transaction
     *               These values will be passed to this class from POS
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    public void makePayment(int amount, int tip, int tax) throws IOException, InterruptedException {

        if (amount <= 0) {
            amount *= -1;
            refundPayment(amount);
            return;
        }

        System.out.println("WPPaymentDevice is calling makePayment()");
        this.saleType = 0; // zero is sale type for wppayment
        this.amount = amount;
        this.tip = tip;
        this.tax = tax;

        // Sums the three monetary values into a total amount to charge
        int totalSummed = this.tax + this.tip + this.amount;

        // Converts the integer sum to a float to send to device
        Float actualChargeAmount = (float) totalSummed / 100;

        // Create socket to send payment from socket = setCommunicationSocket();
        socket = setCommunicationSocket();

        // Send payment to device
        sendPayment(printWriter, socket, transactionId, actualChargeAmount);

        // Create message and receipt sockets
        System.out.println("Making a receipt thread...");
        new Thread(() -> {
            try {
                setReceiptSocket();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Got receipt socket....");
        }).start();
        System.out.println("Making a message thread...");
        new Thread(() -> {
            try {
                setMessageSocket();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Got message socket....");
        }).start();
    }

    @Override
    public void cancelPayment() throws IOException, InterruptedException {
        decline();
    }

    @Override
    public void voidPayment() throws IOException, InterruptedException {
        // all dummy values from test payment for now, need to get these values from output.txt
        //create socket to send refund to
        printWriter = new PrintWriter(socket.getOutputStream());
        voidUnsettledPayment(printWriter, 1234, "5413330089604111", 1225, "PGTR6542123292");
    }

    /**
     * Refunds a payment
     * @param amount needs the amount of the refund to be passed in
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    public void refundPayment(int amount) throws IOException, InterruptedException {
        Integer amountToConvert = amount;
        Float convertedAmount = amountToConvert.floatValue() / 100;

        // Create socket to send refund to
        socket = setCommunicationSocket();
        printWriter = new PrintWriter(socket.getOutputStream());
        refundSettledPayment(convertedAmount, printWriter, 1234);

        System.out.println("Making a receipt thread...");
        new Thread(() -> {
            try {
                setReceiptSocket();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Got receipt socket....");
        }).start();
        System.out.println("Making a message thread...");
        new Thread(() -> {
            try {
                setMessageSocket();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Got message socket....");
        }).start();
    }

    @Override
    public void createToken() throws IOException {
    }

    /**
     * @return Socket to communicate with IPC service (needs to be configured and running already)
     * @throws IOException
     */
    private Socket setCommunicationSocket() throws IOException {

        int port = 10000;

        // Connect socket to port
        Socket socket = new Socket(InetAddress.getLocalHost(), port);

        // Message for successful connection
        System.out.println("[+]Communication Socket Connection successful!" + socket.getInetAddress());
        //TODO: Close this socket after receipt print...

        return socket;
    }

    /**
     * @return socket connected to message port
     * @throws IOException
     * @throws InterruptedException
     */
    private Socket setMessageSocket() throws IOException, InterruptedException {

        int port = 8000; // 8000 is the default intra-message port, can be changed in config.bat

        // Connect socket to port
        Socket socket = new Socket(InetAddress.getLocalHost(), port);

        // Message for successful connection
        System.out.println("[+]Message Socket Connection successful!");
        readOutputFromMessageSocket(socket);

        return socket;
    }

    /**
     * @return the receipts socket
     * @throws IOException
     * @throws InterruptedException
     */
    private Socket setReceiptSocket() throws IOException, InterruptedException {

        int port = 20000; //20000 is  port, can be changed in config.bat

        // Connect socket to port
        Socket socket = new Socket(InetAddress.getLocalHost(), port);

        // Message for successful connection
        System.out.println("[+]Receipt Socket Connection successful!");
        readOutputFromReceiptSocket(socket);

        return socket;
    }

    /**
     * @param messageSocket socket pass in message socket to read its output stream
     * @throws IOException
     */
    private void readOutputFromMessageSocket(Socket messageSocket) throws IOException, InterruptedException {

        System.out.println("Reading from message socket now...");
        // Create payment details
        PaymentDetails details = new PaymentDetails();

        // Create input reader
        BufferedReader input = new BufferedReader(new InputStreamReader(messageSocket.getInputStream()));
        String inputString;

        // Create output writer
        printWriterMessage = new PrintWriter(messageSocket.getOutputStream(), true);

        // Create string builder for receipt
        StringBuilder outputString = new StringBuilder();

        while ((inputString = input.readLine()) != null) {

            // Create receipt array
            String[] receiptArray;

            // Printing messages from device to console
            System.out.println("MESSAGE SOCKET: " + socket.getPort() + "\n" + inputString + "\n");

            // Append each line of output to string builder
            outputString.append(inputString).append("\n");
            System.out.println("STRINGBUILDER = " + outputString);
            System.out.println(".\n.\n.");

            if (inputString.equals("99=0")) {
                System.out.println("Output sequence ended below is the build string:\n");
                System.out.println(outputString);

                if (outputString.toString().contains("Is Signature Ok?")) {
                    System.out.println("************ASKING FOR SIGNATURE*************");
                    isThreeReceipts = true;
                    System.out.println("Need to get boolean from this dialog to pass to commandlistener...");
                    Boolean dialogPrompt = PaymentUtil.buildAlertDialog("Verify Signature", "Is signature ok?", AlertDialogStyle.SIGNATURE, null);

                    System.out.println("Waiting for signature...");
                    System.out.println("...We got it - signature OK? " + dialogPrompt);
                    if (dialogPrompt) {
                        accept();
                        System.out.println("user accepted signature");
                        details.setDecision("Accepted");
                        details.setMessage("Approved");
                        CommandListener.isComplete = true;
                        CommandListener.isApproved = true;
                    } else {
                        decline();
                        System.out.println("user declined signature");
                        details.setDecision("Rejected");
                        details.setMessage("Declined");
                        CommandListener.isComplete = true;
                        CommandListener.isApproved = false;
                    }
                    PaymentUtil.observeData(details);
                }

                if (outputString.toString().contains("MaxCshBckLmt")) {
                    System.out.println("************ASKING FOR CASH BACK*************");
                    //for now auto accepting the payment if its looking for signature confirmation
                    //will need to pass this accept() or decline() option to pos?

                    Boolean dialogPrompt = PaymentUtil.buildAlertDialog("Cashback", "How much cash back?", AlertDialogStyle.CASH_BACK, "100");
                    if (dialogPrompt) {
                        acceptAddCashBack(printWriterMessage, "100");
                        details.setDecision("Accepted");
                        details.setMessage("Approved");
                    } else {
                        declineCashBack(printWriterMessage);
                        details.setDecision("Rejected");
                        details.setMessage("Declined");
                    }
                    PaymentUtil.observeData(details);
                }

                if (outputString.toString().contains("CALL BANK")) {
                    Boolean dialogPrompt = PaymentUtil.buildAlertDialog("Bank Auth Required", "Call bank?", AlertDialogStyle.BANK_AUTH, "12345");
                    if (dialogPrompt) {
                        acceptVoiceAuth(Integer.parseInt(String.valueOf(12345)), printWriterMessage);
                    } else {
                        declineVoiceAuth(printWriterMessage);
                    }
                }
            }

            if (inputString.contains("finalisingMsg") && inputString.contains("AuthCd") || inputString.contains("finalisingMsg") && inputString.contains("R_authCode")) {
                System.out.println("***APPROVED, CAUGHT IN FINALISINGMSG DATA IS " + inputString);
                details.setDecision("Accepted");
                details.setMessage("Approved");
                CommandListener.isComplete = true;
                CommandListener.isApproved = true;
                PaymentUtil.observeData(details);
                //the transaction is finished get the receipts
                System.out.println("GETTING RECEIPTS AFTER THIS FROM FINALISING");
                try{
                    writeReceiptsToFilesForTestScripts(receipts);
                } catch(Exception e){
                    e.printStackTrace();
                }finally {
                    for (String receipt : receipts) {
                        printReceipt(receipt);
                    }
                }
            }

            if (inputString.contains("info:authorisingMsg99=0info:finalisingMsg99=0info:declined_T") || inputString.contains("info:finalisingMsg99=0info:declined_T99=0")) {
                System.out.println("***Declined**** " + inputString);
                details.setDecision("Rejected");
                details.setMessage("Declined");
                CommandListener.isComplete = true;
                CommandListener.isApproved = false;
                PaymentUtil.observeData(details);
                //the transaction is finished get the receipts
                System.out.println("GETTING RECEIPTS AFTER THIS");
                writeReceiptsToFilesForTestScripts(receipts);
                for (String receipt : receipts) {
                    printReceipt(receipt);
                }
            }

            if (inputString.contains("R_approved99=0")) {
                System.out.println("***APPROVED, CAUGHT IN APPROVED DATA IS " + inputString);
                //the transaction is finished get the receipts
                System.out.println("GETTING RECEIPTS AFTER THIS");
                writeReceiptsToFilesForTestScripts(receipts);
                for (String receipt : receipts) {
                    printReceipt(receipt);
                }
            }
        }
    }

    private void readOutputFromReceiptSocket(Socket messageSocket) throws IOException, InterruptedException {
        // Create reader for the inputStream
        BufferedReader input = new BufferedReader(new InputStreamReader(messageSocket.getInputStream()));
        String inputLine; //a holder for the lines
        //initialize print writer for port 10000 on first use
        printWriterMessage = new PrintWriter(messageSocket.getOutputStream(), true);
        //init receipt socket
        Boolean isReceipt = false;
        ArrayList<String> receiptsList = new ArrayList<String>();
        String customerReceipt = "";
        String merchantReceipt = "";
        StringBuilder outputString = new StringBuilder();
        StringBuilder receiptString = new StringBuilder();
        //int receiptCount = 0;
        while ((inputLine = input.readLine()) != null) {
            outputString.append(inputLine).append("\n");

            /**
             * This is the end of the receipt with signature verified, works
             */
            if (inputLine.contains("AUTH CODE:") && !inputLine.contains("Is Signature Ok?")) {
                System.out.println("****END OF RECEIPT******\n");
                String receipt = outputString.toString();
                System.out.println("Receipt\n" + "\n" + receipt);
                receipts.add(receipt);
                for (String rcpt : receipts) {
                    printReceipt(rcpt);
                }
                receipt = "";
                outputString.delete(0, outputString.length() - 1);
                System.out.println("********RECEIPTS SIZE IS : " + receipts.size() + "********");
            }

            if (inputLine.contains("NOT AUTHORISED")) {
                System.out.println("FOUND A RECEIPT");
                String receipt = outputString.toString();
                receipts.add(receipt);
                System.out.println("MADE A RECEIPT ====== \n\n\n" + receipt);
                for (String rcpt : receipts) {
                    printReceipt(rcpt);
                }
                receipt = "";
                System.out.println("********RECEIPTS SIZE IS : " + receipts.size() + "********");
                outputString.delete(0, outputString.length() - 1);


            }

            if(receipts.size() == 2 && !isThreeReceipts) {
                isThreeReceipts=  false;
                writeReceiptsToFilesForTestScripts(receipts);
//                closeIPC(messageSocket);
            }

            if(receipts.size() == 3){
                isThreeReceipts = false;
                writeReceiptsToFilesForTestScripts(receipts);
//                closeIPC(messageSocket);
            }


        }

    }

    /**
     * Writes the receipts generated to a file that can be changed in the method
     * you could just as easily change this to print them vs. saving to a file
     * @param receiptsArray ArrayList<String> that contains the receipts
     */
    public void writeReceiptsToFilesForTestScripts(ArrayList<String> receiptsArray) {
        for (int i = 0; i < receiptsArray.size(); i++) {
            String receipt = receiptsArray.get(i);
//            for(String receiptString: receiptsArray){
//                print(receiptString);
//            }
            try {   //"C:\Users\Hunter\Desktop"
               // Writer output = null;
                File customerFile = new File("C:" + File.separator + "Users" + File.separator + "Hunter" + File.separator + "Desktop" + File.separator + "WP Receipts" + File.separator + "receipt" + i + ".txt");
                FileOutputStream is = new FileOutputStream(customerFile);
                OutputStreamWriter osw = new OutputStreamWriter(is);
                Writer output = new BufferedWriter(osw);
                output.write(receipt);
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Method sends correct data to accept a payment after card has been read
     *
     * @throws IOException
     */
    public void accept() throws IOException, InterruptedException {
        PrintWriter output = printWriterMessage;
        System.out.println("********************accept() called");
        output.write("2=401\n");
        output.write("3=0\n"); //0 accepted 1 declined
        output.write("99=0\n");
        output.flush();
//        setCommunicationSocket();
        System.out.println("********************accept() finished...watch terminal until 'please wait' message is gone, then receipts will be available");
//        writeReceiptsToFilesForTestScripts(receipts);
    }

    /**
     * Method to decline a payment after card has been read
     *
     * @throws IOException
     */
    public void decline() throws IOException, InterruptedException {
        PrintWriter output = printWriterMessage;
        System.out.println("*****************decline() called");
        output.write("2=401" + "\n");
        output.write("3=1\n"); //0 accepted 1 denied
        output.write("99=0\n");
        output.flush();
        setCommunicationSocket();
        System.out.println("*****************decline() finished...watch terminal until 'please wait' message is gone, then receipts will be available");
        writeReceiptsToFilesForTestScripts(receipts);
    }

    /**
     * Method sends correct data to accept a payment after card has been read
     *
     * @param output the PrintWriter to write to socket
     * @throws IOException
     */
    public void acceptAddCashBack(PrintWriter output, String cashBackAmount) throws IOException {
        System.out.println("********************acceptCashBack() called");
        output.write("2=405\n");
        output.write("3=0\n"); //0 accepted 1 declined
        output.write("5=" + cashBackAmount + "\n"); //0 accepted 1 declined
        output.write("99=0\n");
        output.flush();
        System.out.println("********************acceptCashBack() finished");
    }

    /**
     * Method sends correct data to accept a payment after card has been read
     *
     * @param output the PrintWriter to write to socket
     * @throws IOException
     */
    public void declineCashBack(PrintWriter output) throws IOException {
        System.out.println("********************declineCashBack called");
        output.write("2=405\n");
        output.write("3=1\n"); //0 accepted 1 declined
        output.write("99=0\n");
        output.flush();
        System.out.println("********************declineCashBack  finished");
    }

    public void acceptNoSignature(PrintWriter output) throws IOException {
        System.out.println("********************acceptNoSig() called");
        output.write("2=401\n");
        output.write("3=1\n"); //0 accepted 1 declined
        output.write("99=0\n");
        output.flush();
        System.out.println("********************acceptNoSig() finished");
    }

    public void sendNeedSignature(PrintWriter output, Socket socket) throws IOException {
        System.out.println("********************signature called");
        output = new PrintWriter(socket.getOutputStream(), true);
        output.write("2=65\n");
        output.write("101=INST" + transactionId + "\n"); //0 accepted 1 declined
        output.write("99=0\n");
        output.flush();
        System.out.println("********************accept() finished");
    }

    public void acceptSignatureRequired(PrintWriter output) throws IOException {
        System.out.println("********************accept()  sig required called");
        output.write("2=401\n");
        output.write("3=0\n"); //0 accepted 1 declined
        output.write("108=1\n"); //0 accepted 1 declined
        output.write("101=INST" + transactionId + "\n"); //0 accepted 1 declined
        output.write("99=0\n");
        output.flush();
        System.out.println("********************accept() sig required finished");
    }

    /**
     * Method to provide bank authorization on payment should device request it
     * //2=400
     * //3=0 (Accepted), 1  (Rejected)
     * //4=Authorisation Code received from bank (Present only if the above value is 0)
     * //99=0
     *
     * @param authCode code from bank authorization
     * @throws IOException
     */
    public void acceptVoiceAuth(int authCode, PrintWriter printWriter) throws IOException {
        System.out.println("******************acceptVoiceAuth() called");
        printWriter.write("2=400" + "\n"); //400 is the command for voice auth
        printWriter.write("3=0\n"); // 0 is accepted 1 is rejected
        printWriter.write("4=" + authCode + "\n");
        printWriter.write("99=0\n");
        printWriter.flush();
        System.out.println("******************acceptVoiceAuth() finished");
    }

    public void declineVoiceAuth(PrintWriter printWriter) throws IOException {
        System.out.println("******************acceptVoiceAuth() called");
        printWriter.write("2=400" + "\n"); //400 is the command for voice auth
        printWriter.write("3=1\n"); // 0 is accepted 1 is rejected
        printWriter.write("99=0\n");
        printWriter.flush();
        System.out.println("******************declineVoiceAuth() finished");
    }

    /**
     * Method to refund a completed payment
     * @param amount               amount to be refunded
     * @param transactionReference reference to transaction to be refunded, must be from original transaction
     */
    public void refundSettledPayment(Float amount, PrintWriter output, int transactionReference) {
        //write response to refund
        System.out.println("refundSettledPayment called");
        output.write("1=" + transactionReference + "\n");
        output.write("2=20\n");
        output.write("3=" + amount + "\n");
        output.write("99=0\n");
        output.flush();
        System.out.println("refundSettledPayment finished");
    }

    public void voidUnsettledPayment(PrintWriter output, int transactionReference, String cardNumber, int cardExpiry, String field28) {
        //create socket to send payment from
        System.out.println("***************************refundSettledPayment called");
        output.write("1=" + transactionReference + "\n");
        output.write("2=3\n");
        output.write("5=" + cardNumber + "\n");
        output.write("6=" + cardExpiry + "\n");
        output.write("13=" + field28 + "\n"); //field 28 of response message
        output.write("99=0\n");
        output.flush();
        System.out.println("***************************refundSettledPayment finished");
    }

    /**
     * CLOSES IPC APPLICATION
     *
     * @param socket need to provide socket (this is generated by setServerSocket method)
     * @throws IOException
     */
    public void closeIPC(Socket socket) throws IOException {
        //get output stream to write to socket
        PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
        output.write("1=1200\n");
        output.write("3=0\n");
        output.write("99=0\n");
        output.flush();
        BufferedWriter outputWriter = new BufferedWriter(output);
        outputWriter.write(String.valueOf(output));
    }
    // Called by getResponse()
    public static boolean printReceipt(String text) {

//        // Check for valid receipt data
////        if (resp.getReceiptText() != null) {
//
//            // TODO: Source printer address from config
//            Printable printer = new NetworkPrinter("10.10.50.33", 9100);
//            PrinterService printerService = new PrinterService(printer);
//
//            // Call print method and close device
//            printerService.print(text);
//        System.out.println("Printing...................................Saving paper....................");
//            printerService.close();
            return true;
//        }
}
}