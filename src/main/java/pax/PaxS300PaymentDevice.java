package pax;

import com.pax.poslink.*;
import interceptor.CommandListener;
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
 * 6) Process and return response data to POS
 *
 * @author Hunter Yavitz - 6/16/21
 */

public class PaxS300PaymentDevice implements IPaymentDevice {

    //
    private static PosLink posLink;
    private static ProcessTransResult processTransResult;
    private static final PaymentDetails paymentDetails = new PaymentDetails();

    // TODO: ECRRefNum -> OrigECRRefNum
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


    private static double subtotalAmount, totalAmount, tipAmount, taxAmount;

    //
    private static final DecimalFormat decimalFormat = new DecimalFormat("#.00");

    //
    @Override
    public void makePayment(int amount, int tip, int tax) {

        // TODO: Capture negative amounts and reprocess as voids
        if (amount <= 0) {


            amount = 0;
            tip = 0;
            tax = 0;
            System.out.println("Found amount less than 0, redirecting to void");
            voidPayment();

        } else {

            // Validate amounts
            tip = Math.max(tip, 0);
            tax = Math.max(tax, 0);

            // Setup PosLink and Comm
            posLink = new PosLink();
            posLink.SetCommSetting(PaxS300CommConfig.getCommSetting());

            // Setup Request Params
            posLink.PaymentRequest = PaxS300PaymentRequest.getPaymentRequestSale(amount, tip, tax);

            // Convert Amounts - Cents to Dollars
            subtotalAmount = (double) amount / 100;
            tipAmount = (double) tip / 100;
            taxAmount = (double) tax / 100;
            totalAmount = subtotalAmount + tipAmount + taxAmount;

            // Process Payment
            processTransResult = posLink.ProcessTrans();



            // TODO: EXTRA DATA
            try {

                //
                taxAmount = Double.parseDouble(posLink.PaymentResponse.ExtData.substring(posLink.PaymentResponse.ExtData.indexOf("<TaxAmount>") + 11,
                        posLink.PaymentResponse.ExtData.indexOf("</TaxAmount>"))) / 100;
                taxAmount = Double.parseDouble(decimalFormat.format(taxAmount));

                //
                tipAmount = Double.parseDouble(posLink.PaymentResponse.ExtData.substring(posLink.PaymentResponse.ExtData.indexOf("<TipAmount>") + 11,
                        posLink.PaymentResponse.ExtData.indexOf("</TipAmount>"))) / 100;
                tipAmount = Double.parseDouble(decimalFormat.format(tipAmount));

                //
                subtotalAmount = (Double.parseDouble(posLink.PaymentResponse.RequestedAmount) / 100) - (tipAmount + taxAmount);
                subtotalAmount = Double.parseDouble(decimalFormat.format(subtotalAmount));

                //
                totalAmount = subtotalAmount + tipAmount + taxAmount;
                totalAmount = Double.parseDouble(decimalFormat.format(totalAmount));

                // TODO: THIS IS IT!!!
                //href = posLink.PaymentResponse.ExtData.substring(posLink.PaymentResponse.ExtData.indexOf("<HRef>") + 6, posLink.PaymentResponse.ExtData.indexOf("</HRef"));
                System.out.println("The HRef is: " + CommandListener.href);

                //
            } catch (Exception e) {

                //
                System.out.println("Extra Data Unavailable - No Reference Number");
            }

            //
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
    public void voidPayment() {

        // Setup PosLink and Comm
        posLink = new PosLink();
        posLink.SetCommSetting(PaxS300CommConfig.getCommSetting());

        //
        posLink.PaymentRequest = PaxS300PaymentRequest.getPaymentRequestVoid(""); // TODO: EXTRA DATA
        //posLink.PaymentRequest.ExtData = "<HRefNum>10603053</HRefNum>";
        //posLink.PaymentRequest.ECRRefNum = href;

        //
        processTransResult = posLink.ProcessTrans();

        //
        processPaymentResults();
    }

    /**
     * Refund - Unsupported, process as return with card present (partial optional) or void (referenceNumber required)
     */
    @Override
    public void refundPayment(int amount) {

        //
        if (amount <= 0) {

            amount = 0;
            System.out.println("Found amount less than 0, redirecting to void");
            voidPayment();
        }

        // Setup PosLink and Comm
        posLink = new PosLink();
        posLink.SetCommSetting(PaxS300CommConfig.getCommSetting());

        // TODO: Check isCardPresent, referenceNumber, or partial amount
        posLink.PaymentRequest = PaxS300PaymentRequest.getPaymentRequestReturn(amount);

        //
        processTransResult = posLink.ProcessTrans();

        //
        processPaymentResults();
    }

    /**
     * Token - Unsupported
     */
    @Override
    public void createToken() {
    }

    /**
     * Adjust - Add tip, partial refunds, etc.
     */
    private void adjustPayment() {

        // TODO: Improve this method
        posLink.PaymentRequest = PaxS300PaymentRequest.getPaymentRequestAdjust(CommandListener.href, (int) (subtotalAmount * 100), (int) (tipAmount * 100));
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
    private static void processPaymentResults() {

        //
        String decision = null;
        String message = null;

        //
        switch (processTransResult.Code) {

            case OK:

                System.out.println("processTransResult.Msg: " + processTransResult.Msg); // SUCC
                System.out.println("processTransResult.Response: " + processTransResult.Response); // ---
                System.out.println("processTransResult.Code: " + processTransResult.Code); // OK
                System.out.println("processTransResult.Responses: " + processTransResult.Responses); // [---]
                System.out.println("Reference Number: " + CommandListener.href);
                System.out.println("Extra Data: " + posLink.PaymentResponse.ExtData);

                System.out.println("HERE IT IS!!!>>>" + posLink.PaymentResponse.ResultTxt);

                String resultText = posLink.PaymentResponse.ResultTxt;

                switch (/*processTransResult.Response*/resultText) {

                    case "OK":
                        // TODO: Handle as payment successful
                        decision = "Accepted";
                        message = "Approved";
                        //CommandListener.href = href; // <><><><> TODO: Generate value for follow-on transactions
                        CommandListener.signatureOk = true;
                        break;
                    case "DECLINE":
                        // TODO: Handle as payment declined, retry different card
                        decision = "Rejected";
                        message = "Declined";
                        CommandListener.signatureOk = false;
                        break;
                    case "TIMEOUT":
                        // TODO: Handle as failure, retry same card
                        decision = "Rejected";
                        message = "Declined";
                        CommandListener.signatureOk = false;
                        break;
                    case "ABORTED":
                        // TODO: Handle as failure, retry same card
                        decision = "Rejected";
                        message = "Declined";
                        CommandListener.signatureOk = false;
                        System.out.println("ABORTED! - " + decision + " " + message);
                        break;
                    case "DUP TRANSACTION":
                        // TODO: Put something here
                        break;
                    default:
                        // TODO: Handle as failure, other
                }

                // TODO: Sharpen this filter
//                System.out.println("Filtering results...");
//                if (processTransResult.Msg.contains("SUCC")) {
//                    System.out.println("SUCCEED");
//                    decision = "Accepted";
//                    message = "Approved";
//                    //CommandListener.href = href; // TODO: Generate value for follow-on transactions
//                    CommandListener.signatureOk = true;
//                } else {
//                    System.out.println("FAIL");
//                    decision = "Rejected";
//                    message = "Declined";
//                    CommandListener.signatureOk = false;
//                }
//                System.out.println("OTHER");

                // TODO: Ensure payment details class is complete
                paymentDetails.setDecision(decision);
                paymentDetails.setMessage(message);
                //paymentDetails.setInvoice(href);

                System.out.println("Observing PaymentDetails and Setting CommandListener.isComplete");
                PaymentUtil.observeData(paymentDetails);
                CommandListener.isComplete = true;

                break;

            case TimeOut:

                //
                System.out.println("Pax was too slow...");
                paymentDetails.setDecision("TIMEOUT");
                paymentDetails.setMessage("Device Timeout");

                break;

            case ERROR:

                //
                System.out.println("Whoops! Pax had an error.");
                paymentDetails.setDecision("ERROR");
                paymentDetails.setMessage("Device Error");

                break;

            default:

                //
                System.out.println("Observing PaymentDetails and Setting CommandListener.isComplete");
                PaymentUtil.observeData(paymentDetails);
                CommandListener.isComplete = true;
        }
    }
}