package freedompay;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import config.DeviceConfig;
import payment.InvoiceNumber;
import freedompay.network.FreedomPayConnectionHelper;
import freedompay.network.FreedomPayDataStreamManager;
import freedompay.pojo.*;
import interceptor.CommandListener;
import payment.IPaymentDevice;
import payment.PaymentDetails;
import payment.PaymentUtil;
import printer.NetworkPrinter;
import printer.Printable;
import printer.PrinterService;

import java.io.*;
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

    // Declare connection objects
    private static FreedomPayConnectionHelper fpConnection;

    static {
        try {
            fpConnection = FreedomPayConnectionHelper.getFPConnectionInstance();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static FreedomPayDataStreamManager fpDataStream = FreedomPayDataStreamManager.getFPDataStreamInstance();

    // TODO: Source from API call
    // Sourced from DeviceConfig class which pulls data from freedompay_config.txt file configured at integration
    private final static DeviceConfig deviceConfig = new DeviceConfig("./config/freedompay_config.txt");

    // Assign config values from device config object
    private static final long storeId = deviceConfig.STORE_ID; // FP issued credentials
    private static final long terminalId = deviceConfig.TERMINAL_ID; // FP issued credentials
    private static final int laneId = deviceConfig.LANE_ID; // TID from POI
    private static final String clientEnvironment = deviceConfig.CLIENT_ENVIRONMENT; // Dev, Prod, etc
    public static final String invoice = InvoiceNumber.generateInvoiceNumber(); // ex: date + incremental

    // Timeout
    private static final int TIMEOUT = 30000;
    private static long lastTime = 0L;

    // Declare optional fields for follow-on requests
    private static String requestId;
    private static String merchantReferenceCode;

    // Declare optional token values
    private static boolean isToken = true;
    private static boolean createToken = false;
    private static String token;

    // Payment details
    private static PaymentDetails paymentDetails;
    private static String decision;
    private static String message;

    // Declare request and response objects
    private static Request posRequest;
    private static Response posResponse;
    private static String responseXML;
    private static byte[] requestBytes;

    // Declare charge values
    private static double amountDouble, tipDouble, taxDouble;
    private static int amountInteger, tipInteger, taxInteger;

    // TODO: Create builder for these...
    private static Items items;

    // Create ISO date object for customer data
    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
    private final String dateAsISO = df.format(new Date());

    // Generate unique transaction id
    private String getMerchantReferenceCode() {

        //
        return UUID.randomUUID().toString().replace("-", "");
    }

    //
    public FreedomPayPaymentDevice() throws IOException {}

    @Override
    public void makePayment(int amt, int tp, int tx) throws IOException {

        //
        fpConnection = FreedomPayConnectionHelper.getFPConnectionInstance();
        fpDataStream = FreedomPayDataStreamManager.getFPDataStreamInstance();

        // Assign initial amount
        amountInteger = amt;
        tipInteger = tp;
        taxInteger = tx;

        // Build item data
        items = ItemData.getItemData();

        // Clear previous data for transaction that don't need it
        // invoice, merchantReferenceCode, requestId, token, isToken, createToken


        //
        execute("Sale");
    }

    @Override
    public void cancelPayment() throws IOException {

        //
        fpConnection = FreedomPayConnectionHelper.getFPConnectionInstance();
        fpDataStream = FreedomPayDataStreamManager.getFPDataStreamInstance();

        //
        execute("Cancel");
    }

    @Override
    public void voidPayment() throws IOException {

        //
        fpConnection = FreedomPayConnectionHelper.getFPConnectionInstance();
        fpDataStream = FreedomPayDataStreamManager.getFPDataStreamInstance();

        //
        execute("Void");
    }

    @Override
    public void refundPayment(int amt) throws IOException {

        //
        fpConnection = FreedomPayConnectionHelper.getFPConnectionInstance();
        fpDataStream = FreedomPayDataStreamManager.getFPDataStreamInstance();

        //
        amountInteger = amt;

        //
        execute("Refund");
    }

    @Override
    public void createToken() throws IOException {

        //
        amountInteger = taxInteger = tipInteger = 0;
        createToken = true;
        isToken = false;

        //
        execute("Auth");
    }

    //
    private void setToken() {

        //
        token = TokenStore.getToken(generateCustomerNumber());
    }

    // Called by every transaction method.
    private boolean execute(String req) throws IOException {

        // Create new Response object for each transaction.
        posRequest = new Request();
        posResponse = new Response();

        // Assign values for current PaymentDevice instance
        posRequest.setStoreId(String.valueOf(storeId));
        posRequest.setTerminalId(String.valueOf(terminalId));
        posRequest.setLaneId(String.valueOf(laneId));
        posRequest.setClientEnvironment(clientEnvironment);

        //
        if (merchantReferenceCode == null
                || merchantReferenceCode.isEmpty()
                || merchantReferenceCode.equals("")) {
            merchantReferenceCode = getMerchantReferenceCode();
        }
        posRequest.setMerchantReferenceCode(merchantReferenceCode);

        //
        setCustomerData();

        //
        if (isToken) {
            setToken();
            if (token != null) {
                posRequest.setCardNumber(token);
            }
            isToken = false;
        }

        //
        if (createToken) {
            posRequest.setTokenType("6");
            createToken = false;
        }

        //
        posRequest.setItems(ItemData.getItemData());

        //
        posRequest.setInvoiceNumber(invoice);
        posRequest.setAllowPartial("Y");

        // Assign request type
        posRequest.setRequestType(req);

        // Route request type and pass requestId, merchantReferenceCode, or token as needed
        switch (req) {

            // Authorize card for future capture transaction or token generation
            case "Auth":

            // Auth and Capture in single transaction
            case "Sale":

                // Assign single charges
                setAmountValues(amountInteger, taxInteger, tipInteger);

                //
                return getResponse();

            // Close sale on previous auth transaction
            case "Capture":

                // Assign previous charge, tip, and tax -> Convert from cents
                setAmountValues(amountInteger, taxInteger, tipInteger);

                // Assign previous request id
                if (requestId != null
                        && !requestId.isEmpty()
                        && !requestId.equals("")) {
                    posRequest.setRequestId(requestId);
                }

                //
                return getResponse();

            // If card present or request id present
            case "Refund":

                // Assign previous amount
                setAmountValues(amountInteger, 0, 0);

                // Assign previous request id
                if (requestId != null
                        && !requestId.isEmpty()
                        && !requestId.equals("")) {
                    posRequest.setRequestId(requestId);
                }

                //
                return getResponse();

            // If POS has obtained response from previous transaction
            case "Void":

                // Assign previous request id
                if (requestId != null
                        && !requestId.isEmpty()
                        && !requestId.equals("")) {
                    posRequest.setRequestId(requestId);
                }

                //
                return getResponse();

            // If POS has not obtained response from previous transaction
            case "Cancel":

                //
                return getResponse();
        }

        // Assume execute failed if not handle by switch
        return false;
    }

    // Called by execute() with requestType argument.
    private boolean getResponse() {

        // Send request package and return response package
        try {

            // Package Request object to XML String
            String requestXML = serializeToXmlString(posRequest);

            // Serialize XML String to byte array
            requestBytes = serializeToByteArray(requestXML);

            // Reset Timeout
            lastTime = System.currentTimeMillis();
            System.out.println("Start Time: " + lastTime);

            // Open connection stream, send byte array, capture response
            new Thread(() -> getData()).start();

            //
            while (responseXML == null) {
                System.out.print(".");
                if ((System.currentTimeMillis() - lastTime) > TIMEOUT) {
                    System.out.println("Time Out!");
                    return false;
                }
            }

            //
            System.out.println("End Time: " + System.currentTimeMillis());
            System.out.println("Total Time: " + (System.currentTimeMillis() - lastTime));

            //
            System.out.println("Token : " + isToken + " : " + token);
            System.out.println("Request ID : " + requestId);
            System.out.println("Merchant Reference Code : " + merchantReferenceCode);

            //
            System.out.println(responseXML);

            // Deserialize and construct Response object from data
            posResponse = deserializeFromXmlString(responseXML);

            // Token
            if (posResponse.getToken() != null
                    && !posResponse.getToken().isEmpty()
                    && !posResponse.equals("")) {
                createToken = false;
                isToken = true;
                token = posResponse.getToken();
                TokenStore.storeToken(generateCustomerNumber(), token);
            }

            // Merchant Reference Code
            if (posResponse.getMerchantReferenceCode() != null
                    && !posResponse.getMerchantReferenceCode().isEmpty()
                    && !posResponse.getMerchantReferenceCode().equals("")) {
                merchantReferenceCode = posResponse.getMerchantReferenceCode();
            }

            // Request ID
            if (posResponse.getRequestId() != null
                    && !posResponse.getRequestId().isEmpty()
                    && !posResponse.getRequestId().equals("")) {
                requestId = posResponse.getRequestId();
            }

            // Approved Amount
            if (posResponse.getApprovedAmount() != null
                    && !posResponse.getApprovedAmount().isEmpty()
                    && !posResponse.getApprovedAmount().equals("")) {
                amountDouble = Double.parseDouble(posResponse.getApprovedAmount());
            }

            // Approved Tip
            if (posResponse.getTipAmount() != null
                    && !posResponse.getTipAmount().isEmpty()
                    && !posResponse.getTipAmount().equals("")) {
                tipDouble = Double.parseDouble(posResponse.getTipAmount());
            }

            // Get decision code from Response object -> prompt POS for appropriate action
            if (posResponse.getDecision() != null
                    && !posResponse.getDecision().isEmpty()
                    && !posResponse.getDecision().equals("")) {
                decision = posResponse.getDecision();
            }

            // Generate receipt
            if (!createToken) {
                printReceipt(posResponse);
            }

            // Decision codes returned from FCC
            switch (decision) {

                // Request was accepted
                case "A":

                    //
                    CommandListener.signatureOk = true;
                    CommandListener.isComplete = true;
                    setPaymentDetails();

                    //
                    return true;

                // Request was missing required fields or contained incorrect credentials - Contact ISV
                case "E":

                // Request failed due to internal condition (communication / server error) - Retry transaction
                case "F":

                    //
                    CommandListener.signatureOk = false;
                    CommandListener.isComplete = true;
                    setPaymentDetails();

                    //
                    return false;

                // Request rejected due to card error (insufficient funds, bad number, etc) - Retry with different card
                case "R":

                    //
                    switch (message) {
                        case "UserCancel":
                            break;
                        case "Lane Timeout":
                            cancelPayment();
                            break;
                    }

                    //
                    CommandListener.signatureOk = false;
                    CommandListener.isComplete = true;
                    setPaymentDetails();

                    //
                    return false;

                // Request failed due to other decision code outside of client scope - Contact FCC
                default:

                    //
                    CommandListener.signatureOk = false;
                    CommandListener.isComplete = true;
                    setPaymentDetails();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Assume response failed if not handled by switch
        return false;
    }

    private void getData() {
        try {
            System.out.println("Getting data...");
            responseXML = fpDataStream.openStream(fpConnection.fpConnection, requestBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Got it! \n" + responseXML);
    }

    // Called by getRequest() - Send Request object -> Return String
    private static String serializeToXmlString(Request request) throws JsonProcessingException {

        // Map request object to XML
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        File xmlOutput = new File("request_log.xml");

        //
        try (FileWriter fileWriter = new FileWriter(xmlOutput)) {
            fileWriter.write(xmlMapper.writeValueAsString(request));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //
        return xmlMapper.writeValueAsString(request);
    }

    // Called by getRequest() - Connection expects bytes
    private static byte[] serializeToByteArray(String xmlString) {

        // Package request XML to byte array
        return xmlString.getBytes(StandardCharsets.UTF_8);
    }

    // Called by getRequest() - Send String -> Return Response object
    private static Response deserializeFromXmlString(String response) throws IOException {

        File xmlOutput = new File(invoice + "response_log.xml");
        XmlMapper xmlMapper = new XmlMapper();

        //
        try (FileWriter fileWriter = new FileWriter(xmlOutput)) {
            fileWriter.write(xmlMapper.writeValueAsString(response));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Unpack response XML to response object
        return xmlMapper.readValue(response, Response.class);
    }

    // Called by getResponse()
    private boolean printReceipt(Response response) {

        // Check for valid receipt data
        if (response.getReceiptText() != null
                && !response.getReceiptText().isEmpty()
                && !response.getReceiptText().equals("")) {

            // TODO: Source printer address from config
            Printable printer = new NetworkPrinter("10.10.50.33", 9100);
            PrinterService printerService = new PrinterService(printer);

            // Call print method and close device
            printerService.print(new Receipt(invoice, items, amountDouble, taxDouble, tipDouble, posResponse).getFullReceipt());
            printerService.cutFull();
            printerService.close();

            //
            return true;
        }

        // No receipt data
        return false;
    }

    // Called by execute() with amount values
    private void setAmountValues(int amount, int tax, int tip) {

        //
        amountDouble = (double) (amount / 100);
        taxDouble = (double) (tax / 100);
        tipDouble = (double) (tip / 100);

        //
        posRequest.setChargeAmount(String.valueOf(amountDouble));
        posRequest.setTaxAmount(String.valueOf(taxDouble));
        posRequest.setTipAmount(String.valueOf(tipDouble));
    }

    // Assign customer data to request object
    private void setCustomerData() {

        // TODO: Source from POS
        posRequest.setBillToFirstName("Sally");
        posRequest.setBillToLastName("Jones");
        posRequest.setBillToStreet1("333 W Main St");
        posRequest.setBillToCity("Phoenix");
        posRequest.setBillToState("AZ");
        posRequest.setBillToPostalCode("12345");
        posRequest.setCustomerPODate(dateAsISO);
        posRequest.setCustomerCode(generateCustomerNumber());
        posRequest.setItems(items);
    }

    // Generates a unique customer code from hash value of fields below used for key for token storage
    private String generateCustomerNumber() {

        //
        return String.valueOf((posRequest.getBillToFirstName() + posRequest.getBillToStreet1() + posRequest.getBillToCity())
                .hashCode())
                .replace("-", "");
    }

    // Assign payment details and observe data
    private void setPaymentDetails() {

        //
        paymentDetails = new PaymentDetails();

        //
        paymentDetails.setInvoice(invoice);
        paymentDetails.setCustomerCode(generateCustomerNumber());
        paymentDetails.setDecision(decision);
        paymentDetails.setMessage(posResponse.getMessage());
        paymentDetails.setMerchantReferenceCode(posResponse.getMerchantReferenceCode());
        paymentDetails.setRequestId(posResponse.getRequestId());
        paymentDetails.setToken(token);

        //
        PaymentUtil.observeData(paymentDetails);
    }
}