package freedompay;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import freedompay.constants.Transaction;
import freedompay.network.FreedomPayConnectionHelper;
import freedompay.network.FreedomPayDataStreamManager;
import freedompay.pojo.FreedomPayTransactionRequest;
import freedompay.pojo.FreedomPayTransactionResponse;
import freedompay.pojo.Items;
import freedompay.pojo.Receipt;
import interceptor.CommandListener;
import payment.IPaymentDevice;
import payment.InvoiceNumber;
import payment.PaymentDetails;
import payment.PaymentUtil;
import printer.NetworkPrinter;
import printer.Printable;
import printer.PrinterService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class FreedomPayPaymentDevice implements IPaymentDevice {

    /**
     *
     * This is called by the CommandListener class, which implements
     * the Client.CommandListener class, and its methods are used to handle
     * commands by the InterceptorClient class.
     *
     * Protocol:
     *      POS requests a new TCP connection
     *      FP accepts the connection
     *      POS sends request
     *      FP returns response
     *      POS closes connection
     *
     * All instances are singletons and should be destroyed after transaction
     * resolved.  Store invoice, merchant reference code, request id, token,
     * and customer code for any follow-on transactions.
     *
     * @author Hunter Yavitz - 3/10/21
     * @update 6/25/21
    */

    private static HttpURLConnection freedomPayConnectionHelper;
    private static FreedomPayDataStreamManager freedomPayDataStreamManager;
    private static FreedomPayPaymentDevice freedomPayPaymentDevice;

    public FreedomPayPaymentDevice(){}

    public static FreedomPayPaymentDevice getFreedomPayPaymentDeviceInstance() throws IOException {
        if (freedomPayPaymentDevice == null) {
            freedomPayPaymentDevice = new FreedomPayPaymentDevice();
        }
        freedomPayConnectionHelper = FreedomPayConnectionHelper.getFreedomPayConnectionInstance();
        freedomPayDataStreamManager = FreedomPayDataStreamManager.getFreedomPayDataStreamManagerInstance();
        return freedomPayPaymentDevice;
    }

    public static String invoice;

    private static final int TIMEOUT = 30000;
    private static long lastPing;
    private static Boolean isGood;

    private static String requestId;
    private static String merchantReferenceCode;

    private static boolean isToken = true;
    private static boolean createToken = false;
    private static String token;

    private static String decision;
    private static String message;

    private static FreedomPayTransactionRequest freedomPayTransactionRequest;
    private static FreedomPayTransactionResponse freedomPayTransactionResponse;
    private static String responseXML;
    private static byte[] requestBytes;

    private static double amountDouble, tipDouble, taxDouble;
    private static int amountInteger, tipInteger, taxInteger;

    private static Items items;

    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
    private static final String dateAsISO = df.format(new Date());

    private static String getMerchantReferenceCode() { return UUID.randomUUID().toString().replace("-", ""); }

    @Override
    public void makePayment(int amount, int tip, int tax) throws IOException, InterruptedException {

        if (amount <= 0) {
            createToken();
        } else {
            amountInteger = amount;
            tipInteger = tip;
            taxInteger = tax;
            executeTransaction(Transaction.SALE);
        }

        // TODO: Source from API call
        items = ItemData.getItemData();
    }

    @Override
    public void cancelPayment() throws IOException, InterruptedException {

        executeTransaction(Transaction.CANCEL);
    }

    @Override
    public void voidPayment() throws IOException, InterruptedException {

        executeTransaction(Transaction.VOID);
    }

    @Override
    public void refundPayment(int amount) throws IOException, InterruptedException {

        amountInteger = amount;
        executeTransaction(Transaction.REFUND);
    }

    @Override
    public void createToken() throws IOException, InterruptedException {

        amountInteger = taxInteger = tipInteger = 0;
        createToken = true;
        isToken = false;

        executeTransaction(Transaction.AUTH);
    }

    // TODO: Source from API call
    private void setToken() { token = TokenStore.getToken(generateCustomerNumber()); }

    private void executeTransaction(Transaction transaction) throws IOException, InterruptedException {

        freedomPayTransactionRequest = new FreedomPayTransactionRequest();
        freedomPayTransactionResponse = new FreedomPayTransactionResponse();

        // TODO: Source from config - refactor into method
        freedomPayTransactionRequest.setStoreId("1496617013");
        freedomPayTransactionRequest.setTerminalId("2510855011");
        freedomPayTransactionRequest.setLaneId("0");
        freedomPayTransactionRequest.setClientEnvironment("QuickPoint");

        if (merchantReferenceCode == null || merchantReferenceCode.isEmpty()) {
            merchantReferenceCode = getMerchantReferenceCode();
        }
        freedomPayTransactionRequest.setMerchantReferenceCode(merchantReferenceCode);

        // TODO: Use payment details class to set customer details here ->
        /**
         *
         * Items, customer code, token, invoice?
         * freedomPayTransactionRequest.setItems(ItemData.getItemData());
         *
         */

        if (isToken) {
            setToken();
            if (token != null) {
                freedomPayTransactionRequest.setCardNumber(token);
            }
            isToken = false;
        }

        if (createToken) {
            freedomPayTransactionRequest.setTokenType("6");
            createToken = false;
        }

        if (invoice == null || invoice.equals("")) {
            invoice = InvoiceNumber.generateInvoiceNumber();
        }

        freedomPayTransactionRequest.setInvoiceNumber(invoice);
        freedomPayTransactionRequest.setAllowPartial("Y");

        freedomPayTransactionRequest.setRequestType(transaction.toString().charAt(0) + transaction.toString().substring(1).toLowerCase());

        switch (transaction.toString()) {

            case "SALE":
                setAmountValues(amountInteger, taxInteger, tipInteger);
                if (!getTransactionResponse()) {
                    System.out.println("case Sale getTransactionResponse() was false");
                    cancelPayment();
                }
                break;

            case "PRE_AUTH":

            // TODO: Source follow-on data from API call
            case "AUTH":
                setAmountValues(amountInteger, taxInteger, tipInteger);
                if (requestId != null && !requestId.isEmpty()) {
                    freedomPayTransactionRequest.setRequestId(requestId);
                }
                if (!getTransactionResponse()) {
                    cancelPayment();
                }
                break;

            case "POST_AUTH":

            case "CANCEL":
                if (!getTransactionResponse()) {
                    System.out.println("Cancel Transaction failed - No response");
                }
                break;

            // TODO: Source follow-on data from API call
            case "VOID":
                if (requestId != null && !requestId.isEmpty()) {
                    freedomPayTransactionRequest.setRequestId(requestId);
                }

                if (!getTransactionResponse()) {
                    cancelPayment();
                }
                break;

            case "RETURN":

            // TODO: Source follow-on data from API call
            case "REFUND":
                setAmountValues(amountInteger, 0, 0);

                if (requestId != null && !requestId.isEmpty()) {
                    freedomPayTransactionRequest.setRequestId(requestId);
                }

                if (!getTransactionResponse()) {
                    cancelPayment();
                }
                break;

            case "TOKEN":
            default:
        }
    }

    private Boolean getTransactionResponse() throws IOException, InterruptedException {

        String requestXML = serializeToXmlString(freedomPayTransactionRequest);
        System.out.println("Request XML: " + requestXML);

        responseXML = null;

        requestBytes = serializeToByteArray(requestXML);

        lastPing = System.currentTimeMillis();
        System.out.println("Start Time: " + lastPing);

        // TODO: This connection request might not work in a thread.
//        Thread transactionThread = new Thread(
//        );
//        transactionThread.start();
        getTransactionResponseData(); // TODO: This is the blocking method

        Thread timerThread = new Thread(() -> {

            isGood = true;

            // TODO: Increment counter here in while loop;
            //while (responseXML == null || responseXML.isEmpty()) {
            while (responseXML == null) {
                System.out.println("yeah?->" + (lastPing > (System.currentTimeMillis() - 10000000)));
                System.out.println("Top of while loop: " + freedomPayTransactionRequest.getRequestType() + " : " + responseXML);
                System.out.println("counter-length: " + (System.currentTimeMillis() - 10000));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Bottom of while loop: " + freedomPayTransactionRequest.getRequestType() + " : " + responseXML);
                if ((System.currentTimeMillis() - lastPing) > TIMEOUT) {
                    System.out.println("ResponseXML should be still be null: " + (responseXML == null));
                    System.out.println("Transaction Request Timeout, let's cancel this bitch.");
                    //transactionThread.interrupt();
                    isGood = false;
                    break;
                }
            }
        });
        System.out.println("isGood: " + isGood);
        timerThread.start();

        while (isGood == null) {
                Thread.sleep(3000);
            System.out.println("isGood: " + isGood);
        }
        System.out.println("isGood: " + isGood);
        wrapUp();
        return isGood;
    }

    private boolean forgetIt() throws IOException {
        System.out.println("End Time: " + System.currentTimeMillis());
        System.out.println("Total Time: " + (System.currentTimeMillis() - lastPing));
        return true;
    }

    private boolean wrapUp() throws IOException, InterruptedException {
        System.out.println("End Time: " + System.currentTimeMillis());
        System.out.println("Total Time: " + (System.currentTimeMillis() - lastPing));

        // TODO: Wrap up?
        // TODO: Source follow-on request data from API call
        System.out.println("----------------------------------->\n");
        System.out.println("Token : " + isToken + " : " + token);
        System.out.println("Request ID : " + requestId);
        System.out.println("Merchant Reference Code : " + merchantReferenceCode);
        System.out.println("Response XML: " + responseXML);
        System.out.println("<----------------------------------\n");

        freedomPayTransactionResponse = deserializeFromXmlString(responseXML);

        /**
         * TODO: Evaluate and populate using payment details class
         */

        if (freedomPayTransactionResponse.getToken() != null && !freedomPayTransactionResponse.getToken().isEmpty()) {
            isToken = true;
            token = freedomPayTransactionResponse.getToken();
            // TODO: Use payment details to store this
            TokenStore.storeToken(generateCustomerNumber(), token);
        }

        if (freedomPayTransactionResponse.getMerchantReferenceCode() != null && !freedomPayTransactionResponse.getMerchantReferenceCode().isEmpty()) {
            merchantReferenceCode = freedomPayTransactionResponse.getMerchantReferenceCode();
        }

        if (freedomPayTransactionResponse.getRequestId() != null && !freedomPayTransactionResponse.getRequestId().isEmpty()) {
            requestId = freedomPayTransactionResponse.getRequestId();
        }

        if (freedomPayTransactionResponse.getApprovedAmount() != null && !freedomPayTransactionResponse.getApprovedAmount().isEmpty()) {
            amountDouble = Double.parseDouble(freedomPayTransactionResponse.getApprovedAmount());
        }

        if (freedomPayTransactionResponse.getTipAmount() != null && !freedomPayTransactionResponse.getTipAmount().isEmpty()) {
            tipDouble = Double.parseDouble(freedomPayTransactionResponse.getTipAmount());
        }

        if (freedomPayTransactionResponse.getDecision() != null && !freedomPayTransactionResponse.getDecision().isEmpty()) {
            decision = freedomPayTransactionResponse.getDecision();
            System.out.println("Decision: " + decision);
            message = freedomPayTransactionResponse.getMessage();
            System.out.println("Message: " + message);
        }

        if (!createToken) {
            printReceipt(freedomPayTransactionResponse);
        }

        switch (decision) {

            case "A":
                CommandListener.isApproved = true;
                CommandListener.isComplete = true;
                setPaymentDetails();

                // TODO: Alert POS - Approved / Accepted - Complete
                return true;

            case "E":

                // TODO: Alert POS - Declined / Invalid Card Data - Try Again - Same Card

            case "F":

                // TODO: Alert POS - Declined / Communication Failure - Try Again - Same Card

            case "R":

                // TODO: Alert POS - Declined / Insufficient Funds - Try Again - Different Card

                switch (message) {
                    case "UserCancel":
                        break;
                    case "Lane Timeout": // TODO: Ensure this happens after 30 seconds
                        cancelPayment();
                        break;
                    default:
                }

            default:

                CommandListener.isApproved = false;
                CommandListener.isComplete = true;
                setPaymentDetails();
        }

        return false;
    }

    private void getTransactionResponseData() {

        try {
            responseXML = freedomPayDataStreamManager.openStream(freedomPayConnectionHelper, requestBytes);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Connection Error");
            // TODO: Alert POS - Transaction Response Failure - (2)
        }
    }

    private static String serializeToXmlString(FreedomPayTransactionRequest request) throws JsonProcessingException {

        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

        // TODO: Log using API call
        File xmlOutput = new File("request_log.xml");

        try (FileWriter fileWriter = new FileWriter(xmlOutput)) {
            fileWriter.write(xmlMapper.writeValueAsString(request));
        } catch (IOException e) {
            e.printStackTrace();
            // TODO: Alert POS - Transaction Request Failure - (2)
        }

        return xmlMapper.writeValueAsString(request);
    }

    private static byte[] serializeToByteArray(String xmlString) {
        return xmlString.getBytes(StandardCharsets.UTF_8);
    }

    private static FreedomPayTransactionResponse deserializeFromXmlString(String response) throws IOException {

        // TODO: Log using API call
        File xmlOutput = new File(invoice + "response_log.xml");
        XmlMapper xmlMapper = new XmlMapper();

        try (FileWriter fileWriter = new FileWriter(xmlOutput)) {
            fileWriter.write(xmlMapper.writeValueAsString(response));
        } catch (IOException e) {
            e.printStackTrace();
            // TODO: Alert POS - Transaction Request Failure - (2)
        }

        // TODO: EOF error? Need @NoArgsConstructor?
        return xmlMapper.readValue(response, FreedomPayTransactionResponse.class);
    }

    // TODO: Refactor into separate class
    private boolean printReceipt(FreedomPayTransactionResponse response) {

        if (response.getReceiptText() != null && !response.getReceiptText().isEmpty()) {

            // TODO: Source printer address from config
            Printable printer = new NetworkPrinter("10.10.50.33", 9100);
            PrinterService printerService = new PrinterService(printer);

            printerService.print(new Receipt(invoice, items, amountDouble, taxDouble, tipDouble, freedomPayTransactionResponse).getFullReceipt());
            printerService.cutFull();
            printerService.close();

            return true;
        }

        return false;
    }

    private void setAmountValues(int amount, int tax, int tip) {

        amountDouble = (double) (amount / 100);
        taxDouble = (double) (tax / 100);
        tipDouble = (double) (tip / 100);

        freedomPayTransactionRequest.setChargeAmount(String.valueOf(amountDouble));
        freedomPayTransactionRequest.setTaxAmount(String.valueOf(taxDouble));
        freedomPayTransactionRequest.setTipAmount(String.valueOf(tipDouble));
    }

    private void setCustomerData() {

        // TODO: Source from response data
        freedomPayTransactionRequest.setBillToFirstName("Sally");
        freedomPayTransactionRequest.setBillToLastName("Jones");
        freedomPayTransactionRequest.setBillToStreet1("333 W Main St");
        freedomPayTransactionRequest.setBillToCity("Phoenix");
        freedomPayTransactionRequest.setBillToState("AZ");
        freedomPayTransactionRequest.setBillToPostalCode("12345");
        freedomPayTransactionRequest.setCustomerPoDate(dateAsISO);
        freedomPayTransactionRequest.setCustomerCode(generateCustomerNumber());
        freedomPayTransactionRequest.setItems(items);
    }

    private String generateCustomerNumber() {

        return String.valueOf(
                (freedomPayTransactionRequest.getBillToFirstName()
                        + freedomPayTransactionRequest.getBillToStreet1()
                        + freedomPayTransactionRequest.getBillToCity())
                        .hashCode()).replace("-", "");
    }

    private void setPaymentDetails() {

        PaymentDetails paymentDetails = new PaymentDetails();

        paymentDetails.setInvoice(invoice);
        paymentDetails.setCustomerCode(generateCustomerNumber());
        paymentDetails.setDecision(decision);
        paymentDetails.setMessage(freedomPayTransactionResponse.getMessage());
        paymentDetails.setMerchantReferenceCode(freedomPayTransactionResponse.getMerchantReferenceCode());
        paymentDetails.setRequestId(freedomPayTransactionResponse.getRequestId());
        paymentDetails.setToken(token);

        // TODO: Store this with API call
        PaymentUtil.observeData(paymentDetails);
    }
}