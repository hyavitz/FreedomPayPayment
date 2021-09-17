package pax;

import com.google.gson.Gson;
import com.pax.poslink.*;
import interceptor.CommandListener;
import interceptor.NetCommand;
import network.TransactionLog;
import pax.network.PaxS300CommConfig;
import pax.payment.PaxS300PaymentRequest;
import payment.IPaymentDevice;
import payment.PaymentDetails;
import payment.PaymentUtil;

import java.text.DecimalFormat;

/**
 * This class implements IPaymentDevice for the PAX S300 payment
 * device.  It is passed to a CommandHandler constructor to override
 * the command handler for any device.
 *
 * 1) Create PosLink object
 * 2) Configure comm settings (PaxS300CommConfig)
 * 3) Configure PaymentRequest (PaxS300PaymentRequest)
 * 4) Initiate transaction
 * 5) Capture PaymentResponse (PaxS300PaymentResponse)
 * 6) Process and return response data to client
 *
 * @author Hunter Yavitz - 6/16/21
 */

public class PAXS300PaymentDevice implements IPaymentDevice {

    private PosLink posLink;
    private ProcessTransResult processTransResult;

    private final static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.00");

    private double partialAmount, subtotalAmount, totalAmount;
    private int partialAmt, subtotalAmt, totalAmt;

    /**
     * IF SALE (positive amount value)
     * Instantiate PaymentRequest object
     * Generate and assign ECRRefNum (store local) and Invoice
     * Process and capture payment data
     * Assign ECRREFNum (purge local) to webhook transactionNo
     * Insert payment
     *
     * IF VOID (non-positive amount value)
     * Instantiate PaymentRequest object
     * Tare amount value
     * Capture and assign webhook transactionNo to OrigECRRefNum
     */

    public PAXS300PaymentDevice() {
        /**
         * This is instanced only once at runtime, so member fields
         * need to be declared at creation, assigned at invocation,
         * and cleared after results processed.
         */

        System.out.println("<4><>Creating PAXS300PaymentDevice instance");
    }

    @Override
    public void makePayment(int amount, int tip, int tax) throws InterruptedException {

        System.out.println("makePayment of :" + amount);

        // Capture negative values to reprocess as void
        if (amount <= 0) {

            amount = 0;
            voidPayment();

        // Process as sale
        } else {

            this.totalAmount = ((double) amount / 100);
            this.totalAmt = amount;

            System.out.println("totalAmount:" + totalAmount);
            System.out.println("totalAmt:" + totalAmt);

            // Setup PosLink and Comm
            posLink = new PosLink(); // TODO: Need to dynamically-configure IP and Port
            posLink.SetCommSetting(PaxS300CommConfig.getCommSetting());

            // Setup request params
            posLink.PaymentRequest = PaxS300PaymentRequest.getPaymentRequestSale(totalAmt, CommandListener.href);

            System.out.println("DEVICE TRANSACTION START?");
            TransactionLog.generateLog("Start Transaction", "Payment Terminal");

            // Process payment and capture response
            processTransResult = new ProcessTransResult();
            processTransResult = posLink.ProcessTrans();

            Thread.sleep(1000);
            processPaymentResults();
        }
    }

    /**
     * Cancel - Payment in process
     */
    @Override
    public void cancelPayment() {

        // Validate current transaction
        if (posLink != null) {

            // TODO: Process as Failure for POS to handle
            posLink.CancelTrans();
        }
    }

    /**
     * Void w/ ECRN or transaction number
     */
    @Override
    public void voidPayment() throws InterruptedException {

        // Setup PosLink and Comm
        posLink = new PosLink();
        posLink.SetCommSetting(PaxS300CommConfig.getCommSetting()); // TODO: Need to dynamically-configure IP and Port

        //
        System.out.println("Setting VOID CommandListener.href is:" + CommandListener.href);
        posLink.PaymentRequest = PaxS300PaymentRequest.getPaymentRequestVoid(CommandListener.href);

        System.out.println("DEVICE TRANSACTION START?");
        TransactionLog.generateLog("Start Transaction - Void", "Payment Terminal");

        //
        processTransResult = new ProcessTransResult();
        processTransResult = posLink.ProcessTrans();

        Thread.sleep(1000);

        processPaymentResults();
    }

    /**
     * Refund - Unsupported, process as return with card present (partial optional) or void (referenceNumber required)
     */
    @Override
    public void refundPayment(int amount) throws InterruptedException {

        //
        if (amount <= 0) {

            amount = 0;
            System.out.println("Found amount less than 0, redirecting to void");
            voidPayment();
        }

        // Setup PosLink and Comm
        posLink = new PosLink();
        posLink.SetCommSetting(PaxS300CommConfig.getCommSetting());

        //
        posLink.PaymentRequest = PaxS300PaymentRequest.getPaymentRequestReturn(amount, CommandListener.href);

        System.out.println("DEVICE TRANSACTION START?");
        TransactionLog.generateLog("Start Transaction", "Payment Terminal");

        //
        processTransResult = new ProcessTransResult();
        processTransResult = posLink.ProcessTrans();

        Thread.sleep(1000);

        //
        processPaymentResults();
    }

    /**
     * Token - Unsupported
     */
    @Override
    public void createToken() {
        System.out.println("don't be stupid.");
    }

    /**
     * Adjust - Add tip, partial refunds, etc.
     */
    private void adjustPayment() {

        // TODO: Improve this method
        posLink.PaymentRequest = PaxS300PaymentRequest.getPaymentRequestAdjust(totalAmt, CommandListener.href);
    }

    /**
     * Result Code          Result Text         Message
     * 000000               OK                  Success
     * 000100               DECLINE
     * 100001               TIMEOUT             -------
     * 100002               ABORTED             -------
     * 100003               INVALID (data)      Chip Malfunction, Chip Read Error, Chip Blocked, (EMV errors...)
     * 100004               UNSUPPORTED TRANS   -------
     * <p>
     * 100010               COMM ERROR
     * <p>
     * <p>
     * <p>
     * 100011               DUP TRANSACTION     Success
     */

    // OK, TimeOut, ERROR
    private void processPaymentResults() throws InterruptedException {

        //
        System.out.println("DEVICE TRANSACTION FINISH?");
        TransactionLog.generateLog("Finish Transaction", "Payment Terminal");
        Thread.sleep(1000);


        /**
         * Currently POS requested amounts can be adjusted on POS, with adjusted total sent to QP
         * and then to device.  Device itself cannot make adjustments to total, so after partial approval,
         * QP signals to POS that check has been paid.
         *
         * Need to capture POS check total from webhook to compare
         * later...
         *
         */
        CommandListener.paymentDetails.setStatus(posLink.PaymentResponse.ResultTxt); // OK, DECLINE, etc
        CommandListener.paymentDetails.setHostCode(posLink.PaymentResponse.HostCode); // #...12
        CommandListener.paymentDetails.setHostResponse(posLink.PaymentResponse.HostResponse); // 00

        CommandListener.paymentDetails.setApprovedAmount(posLink.PaymentResponse.ApprovedAmount); // TODO: Reflects partial payment (Includes tip)
        CommandListener.paymentDetails.setRequestedAmount(posLink.PaymentResponse.RequestedAmount); // TODO: Reflects partial payment

        CommandListener.paymentDetails.setOriginalAmount(CommandListener.paymentDetails.getOriginalAmount()); //
        CommandListener.paymentDetails.setMessage(posLink.PaymentResponse.Message); // Success, Do not honor, etc
        CommandListener.paymentDetails.setDecision(posLink.PaymentResponse.AvsResponse);
        CommandListener.paymentDetails.setDecision(posLink.PaymentResponse.CvResponse);

        CommandListener.paymentDetails.setReferenceNumber(posLink.PaymentResponse.RefNum); // 4

        CommandListener.paymentDetails.setMaskedCardNumber(posLink.PaymentResponse.BogusAccountNum); // 1234
        CommandListener.paymentDetails.setAuthCode(posLink.PaymentResponse.AuthCode); // VTLMC1

        CommandListener.paymentDetails.setDecision(posLink.PaymentResponse.ResultCode); // Null
        CommandListener.paymentDetails.setRawResponse(posLink.PaymentResponse.RawResponse); // TODO: Explicit set?
        CommandListener.paymentDetails.setTransactionRemainingAmount(posLink.PaymentResponse.TransactionRemainingAmount); // TODO: Explicit set?
        CommandListener.paymentDetails.setRemainingBalance(posLink.PaymentResponse.RemainingBalance); // TODO: Explicit set?
        CommandListener.paymentDetails.setExtraBalance(posLink.PaymentResponse.ExtraBalance); // TODO: Explicit set?
        CommandListener.paymentDetails.setHostDetailedMessage(posLink.PaymentResponse.HostDetailedMessage); // TODO: Explicit set?

        // Card type,


        System.out.println("Just for fun: " + CommandListener.paymentDetails.toString());

        // Raw data from response
        try {
            String rawData = new Gson().toJson(posLink.PaymentResponse);
            System.out.println("Raw data:" + rawData);

            CommandListener.paymentDetails.setRawData(rawData);

        } catch (Exception e) {
            System.out.println("NO RAW DATA AVAILABLE");
        }

        Thread.sleep(1000);

        switch (processTransResult.Code) {

            case OK:

                switch (posLink.PaymentResponse.ResultTxt) {

                    case "OK":
                        // TODO: Handle as payment successful
                        CommandListener.isApproved = true;
                    case "DECLINE":
                        // TODO: Handle as payment declined, retry different card
                    case "TIMEOUT":
                        // TODO: Handle as failure, retry same card
                    case "ABORTED":
                        // TODO: Handle as failure, retry same card
                    case "DUP TRANSACTION":
                        // TODO: Put something here
                    default:
                        // TODO: Handle as failure, other
                }
            case TimeOut:
            case ERROR:
            default:

                CommandListener.isComplete = true;
        }
    }
}