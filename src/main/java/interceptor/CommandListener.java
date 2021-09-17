package interceptor;

import com.google.gson.Gson;

import network.Token;
import payment.IPaymentDevice;

import payment.InvoiceNumber;
import payment.Payment;
import payment.PaymentDetails;

import java.io.IOException;

/**
 * This class implements Client.CommandListener which overrides
 * the single method handle() to handle commands issued by server
 * to payment devices.  When InterceptorClient starts, this class
 * is instantiated by passing a PaymentDevice to the constructor.
 * The PaymentDevice is an interface that is implemented by an
 * instance of any specific PaymentDevice type.
 *
 * @author Hunter Yavitz 7/5/21
 */

/**
 * TODO: If !void -> Capture responseMessage.getTransactionNo() -> assign to CommandListener.transactionNumber
 *  TODO: Else -> Generate ECRRefNum from Invoice
 *  TODO: If makeSale() -> Assign transactionNumber to ECRRefNum for initial webhook transaction
 *  TODO: Else -> Assign transactionNumber to OrigECRNNum for follow-on webhook transaction, generate new ECRRefNum from Invoice
 */

/**TODO: CommandListener.transactionNumber = Generate() -> insertPayment.setTransactionNo() -> PaymentRequest.setECRRefNum()
 * TODO: makeSale() -> PaymentResponse.getRefNum [PAX] -> insertPayment.setTransactionNo()
 * TODO: voidSale() -> responseMessage.getTransactionNo() [QP] -> CommandListener.transactionNumber
 *                  -> PaymentRequest.setOrigECRRefNum()
 *                  && CommandListener.transactionNumber = Generate() -> insertPayment.setTransactionNo() -> PaymentRequest.setECRRefNum()
 */

public class CommandListener implements Client.CommandListener {

    public IPaymentDevice paymentDevice;
    ResponseMessage responseMessage;
    public static PaymentDetails paymentDetails;

    public static boolean isVoid;
    public static boolean isApproved;
    public static boolean isComplete;

    double amount;
    int amt;

    public static String href;

    public CommandListener(IPaymentDevice paymentDevice) {

        System.out.println("<5><>Creating CommandListener instance with paymentDevice[" + paymentDevice + "]");
        this.paymentDevice = paymentDevice;
    }

    /**
     * Initial payment request contains ov_ticket_id, capture and generate transactionNumber
     * Pass transactionNumber to be stored in PAX Request as transactionNo
     * Pass transactionNumber to be stored in AP as transactionNumber
     * Pass transactionNumber to be stored in TSYS as invoice
     *
     * Initial void request contains ov_ticket_id, capture and regenerate transactionNumber
     * Pass transactionNumber to be stored in PAX Request
     */

    @Override
    public void handle(NetCommand command) {

        switch (command.getCommand().toString()) {

            case "SALE":

                try {
                    System.out.println("Handling SALE");

                    responseMessage = new ResponseMessage();
                    paymentDetails = new PaymentDetails();

                    isComplete = false;
                    isApproved = false;
                    isVoid = false;

                    System.out.println("command.getResponseMessage() " + command.getResponseMessage());
                    responseMessage = new Gson().fromJson(command.getResponseMessage(), ResponseMessage.class);

                    System.out.println("Got response: " + responseMessage);
                    href = responseMessage.getUnique_ticket_id().substring(4);

                    amount = (Double.parseDouble(responseMessage.getAmount()) / 100);
                    amt = Integer.parseInt(responseMessage.getAmount());
                    System.out.println("Amount (D) : " + amount);
                    System.out.println("Amt (I) : " + amt);

                    paymentDetails.setTransactionNumber(InvoiceNumber.generateInvoiceNumber().substring(4));
                    paymentDetails.setAmount(String.valueOf(amt));
                    System.out.println("after paymentDetails.setAmount:" + paymentDetails.getAmount());
                    if (paymentDetails.getOriginalAmount() == null) {paymentDetails.setOriginalAmount(responseMessage.getAmount());}
                    System.out.println("after paymentDetails.setOriginalAmount:" + paymentDetails.getAmount());
                    paymentDetails.setInvoice(InvoiceNumber.generateInvoiceNumber());
                    paymentDetails.setHostReferenceNumber(responseMessage.getUnique_ticket_id().substring(4));
                    paymentDetails.setReferenceNumber(responseMessage.getUnique_ticket_id());

                    paymentDevice.makePayment(amt, 0, 0);

                    System.out.println("Sure would like to set this amount here at: " + paymentDetails.getAmount());

                    // TODO: Filter results here to assign proper payment status

                    while (!isComplete) {
                        Thread.sleep(1000);
                    }

                    if (isApproved) {
                        paymentDetails.setDecision(PaymentStatus.APPROVED.getValue()); // TODO: This is also being assigned in PAXS300PaymentDevice class
                        System.out.println("Comparing totals...");
                        System.out.println("Due:" + paymentDetails.getAmount());
                        System.out.println("Paid:" + paymentDetails.getApprovedAmount());
                        if (Integer.parseInt(paymentDetails.getAmount()) > Integer.parseInt(paymentDetails.getApprovedAmount())) {
                            paymentDetails.setStatus(Status.PARTIAL.getValue()); // TODO: It's not paid 'till it's paid...
                        }
                        paymentDetails.setStatus(Status.PAID.getValue()); // TODO: It's not paid 'till it's paid...

                        if (isVoid) {
                            applyPaymentRemove();
                        } else {
                            applyPaymentCapture();
                            while (!isComplete) {
                                Thread.sleep(1000);
                            }
                        }
                    } else {
                        paymentDetails.setDecision(PaymentStatus.FAILED.getValue());
                        paymentDetails.setStatus(Status.DECLINED.getValue());
                        applyPaymentCapture();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case "VOID":
                // TODO: Voids are currently processed as SALE by InterceptorClient
                break;

            case "CANCEL":
                try {
                    paymentDevice.cancelPayment();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            case "READY":
            default:
        }
    }

    private void applyPaymentCapture() {
        // TODO:  Add ov_ticket_id, webhook_id, rawData, device_id

        /**
         * (WH)ov_ticket_id -> (API)ticket_id -> (DB)pos_id -> (QP)ticket number
         * (WH)unique_webhook_id -> (API)webhook_id -> (DB)omnivore_webhook_id -> (QP)---
         * (WH)ov_payment_id -> (API)payment_id -> (DB)id_pay / client_id / omnivore_payment_id -> (QP)---
         * (WH)unique_ticket_id -> (API)unique_ticket_id -> (DB)unique_ticket_id -> (QP)---
         *
         */

        //Payment.insertPaymentCapture(responseMessage.getLocation_id(), responseMessage.getUnique_ticket_id(), transactionStatus, paymentDetails);

        System.out.println("sure would like to set CL amount, it is:" + paymentDetails.getAmount());
        Token.generateToken();
        Payment.insertPaymentCapture(
                Token.token, responseMessage.getLocation_id(), InterceptorClient.register.getSerialNumber(),
                responseMessage.getOv_ticket_id(), String.valueOf(responseMessage.getUnique_webhook_id()), responseMessage.getOv_payment_id(),
                responseMessage.getUnique_ticket_id(), paymentDetails);
    }

    private void applyPaymentRemove() {
        Token.generateToken();
        Payment.insertPaymentRemove(Token.token, responseMessage.getLocation_id(), href);
    }
}