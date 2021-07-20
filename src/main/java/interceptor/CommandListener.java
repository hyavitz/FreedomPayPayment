package interceptor;

import com.google.gson.Gson;

import com.google.gson.JsonObject;
import io.PaymentDao;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import payment.IPaymentDevice;
import payment.InvoiceNumber;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

public class CommandListener implements Client.CommandListener {

    //
    public static IPaymentDevice paymentDevice;

    //
    public static boolean signatureOk; // TODO: Refactor to isApproved, isSignatureOk, isBankAuthOk, isComplete
    public static boolean bankAuthOk; // TODO: Implement this parameter to support WorldPay
    public static boolean isComplete;

    /** TODO:
     * Initial payment request contains ov_ticket_id, capture and generate transactionNumber
     * Pass transactionNumber to be stored in PAX Request as transactionNo
     * Pass transactionNumber to be stored in AP as transactionNumber
     * Pass transactionNumber to be stored in TSYS as invoice
     *
     * Initial void request contains ov_ticket_id, capture and regenerate transactionNumber
     * Pass transactionNumber to be stored in PAX Request
     *
     *
     */
    private static ResponseMessage responseMessage;

    //
    private static double amount, tip;

    //
    public static boolean isVoid = false;

    //
    private static String paymentStatus;
    private static String status;
    public static String href;

    @Override
    public void handle(NetCommand command) {

        //
        switch (command.getCommand().toString()) {

            //
            case "SALE":

                //
                try {

                    //
                    isComplete = false;
                    signatureOk = false;

                    //
                    responseMessage = new Gson().fromJson(command.getResponseMessage(), ResponseMessage.class);
                    System.out.println("CommandListener setting href to: " + responseMessage.getUnique_ticket_id().substring(4));
                    href = responseMessage.getUnique_ticket_id().substring(4);

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

                    //
                    amount = Double.parseDouble(responseMessage.getAmount());
                    int amt = (int) amount;
                    tip = Double.parseDouble(responseMessage.getTip());
                    int tp = (int) tip;

                    //
                    paymentDevice.makePayment(amt, tp, 0);

                    /**
                     *
                     *
                     *
                     */

                    // TODO: Filter results here to assign proper payment status

                    System.out.println("We shouldn't be here until payment is processed...");
                    //
                    while(!isComplete){
                        Thread.sleep(3000);
                        System.out.println("Stalling....");
                    }
                    if (isComplete && signatureOk) {
                        paymentStatus = PaymentStatus.APPROVED.getValue();
                        status = Status.PAID.getValue();
                        if (isVoid) {
                            retrofitCommandListenerResponseVoid();
                        } else {
                            retrofitCommandListenerResponse();
                        }
                    } else if(isComplete && signatureOk) {
                        paymentStatus = PaymentStatus.APPROVED.getValue();
                        status = Status.PAID.getValue();
                        if (isVoid) {
                            retrofitCommandListenerResponseVoid();
                        } else {
                            retrofitCommandListenerResponse();
                        }
                    } else if( isComplete && !signatureOk) {
                        paymentStatus = PaymentStatus.FAILED.getValue();
                        status = Status.DECLINED.getValue();
                        if (isVoid) {
                            retrofitCommandListenerResponseVoid();
                        } else {
                            retrofitCommandListenerResponse();
                        }
                    }
//                    else {
//                        paymentStatus = PaymentStatus.FAILED.getValue();
//                        status = Status.DECLINED.getValue();
//                    }

                    //

                //
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            //
            case "VOID":

                break;

            //
            case "CANCEL":

                //
                try {
                    paymentDevice.cancelPayment();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
                break;

            //
            case "READY":
                break;

            //
            default:
        }
    }

    // TODO: Extract this to a separate method
    private void retrofitCommandListenerResponse() {

        System.out.println("retrofitting SALE");

        //
        Map<String, String> options = new HashMap<>();

        //
        options.put("ov_location_id", responseMessage.getOv_location_id());
        options.put("employee_id", "99"); // TODO: Source from POS
        options.put("status", status);
        options.put("transactionNo", InvoiceNumber.generateInvoiceNumber()); // TODO: Source from <OrigRefNum>
        options.put("payment_status", paymentStatus);
        options.put("type", responseMessage.getOv_tender_type_id());
        options.put("unique_ticket_id", responseMessage.getUnique_ticket_id());
        options.put("server", ""); // TODO: Source from POS
        options.put("ov_ticket_id", responseMessage.getOv_ticket_id());
        options.put("opened_at", responseMessage.getOpened_at());
        options.put("ov_payment_id", responseMessage.getOv_payment_id());
        options.put("amount", amount + "");
        options.put("tip", tip + "");
        options.put("unique_webhook_id", String.valueOf(responseMessage.getUnique_webhook_id()));
        System.out.println(">>>"+options);

        //
        // Call<String> completionCall = PaymentAPI.postWebhookResponse(options);
        Call<String> completionCall = PaymentAPI.postWebhookResponse(
                responseMessage.getOv_location_id(), "99", status, responseMessage.getOv_tender_type_id(), responseMessage.getUnique_ticket_id(),
                "", responseMessage.getOv_ticket_id(), responseMessage.getOpened_at(), responseMessage.getOv_payment_id(), responseMessage.getAmount(),
                String.valueOf(tip/100), paymentStatus, href);

        //
        completionCall.enqueue(new Callback<>() {

            //
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                if (response.isSuccessful()) {
                    JSONObject jsonObject = new JSONObject(response.body());
                    System.out.println(jsonObject.optString("integrated_payments_id"));
                    PaymentDao.setTicketData(jsonObject.optString("integrated_payments_id"), String.valueOf(responseMessage.getUnique_ticket_id()));


                    // TODO: Pass success type to POS to handle
                }
            }

            //
            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                // TODO: Pass failure type to POS to handle
                t.printStackTrace();
            }
        });
    }

    private void retrofitCommandListenerResponseVoid() {

        System.out.println("retrofitting VOID");

        //
//        Map<String, String> options = new HashMap<>();

        // Void 1
//        options.put("id_pay", responseMessage.getPayment_unique_id());
//        options.put("transactionNo", responseMessage.getTransactionNo());
//        options.put("ref_num", href);
//        options.put("pay_type", "ip");
//        options.put("amount", amount + "");
//        options.put("pin", "");
//        options.put("from", "QuickPointDesktop");

        // Void 2
//        options.put("id_pay", responseMessage.getPayment_unique_id());
//        options.put("transactionNo", responseMessage.getTransactionNo());
//        options.put("pay_type", "ip");
//        options.put("pin", "");
//        options.put("amount", amount + "");
//        options.put("from", "QuickPointDesktop");

        // Void 3
//        options.put("id_pay", responseMessage.getPayment_unique_id());
//        options.put("amount", amount + "");
//        options.put("pay_type", "ip");
//        options.put("action", "void");
//        options.put("pin", "");

//        System.out.println("with these values: " + options);

//        Call<String> completionCall = PaymentAPI.postWebhookRequestVoid(options);

        // Void 1
        //Call<String> completionCall = PaymentAPI.postWebhookRequestVoid1(responseMessage.getPayment_unique_id(), responseMessage.getTransactionNo(),
//                                                                        href, "ip", amount + "", "", "QuickPointDesktop");

        // Void 2
        //Call<String> completionCall2 = PaymentAPI.postWebhookRequestVoid2(responseMessage.getPayment_unique_id(), responseMessage.getTransactionNo(),
//                                                                        "ip", "", amount + "", "QuickPointDesktop");

        // Void 3
        //Call<String> completionCall3 = PaymentAPI.postWebhookRequestVoid3(responseMessage.getPayment_unique_id(), amount + "", "ip", "void", "");

        // Void A
        String id_pay = PaymentDao.getTicketData(String.valueOf(responseMessage.getUnique_ticket_id()));
        Call<String> completionCall = PaymentAPI.postWebhookRequestVoid(id_pay);

        //
        completionCall.enqueue(new Callback<>() {

            //
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                if (response.isSuccessful()) {
                    System.out.println(response);
                    // TODO: Pass success type to POS to handle
                    System.out.println("remove payment response: " + response);
                }
            }

            //
            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                // TODO: Pass failure type to POS to handle
                t.printStackTrace();
            }
        });
    }

    public CommandListener(IPaymentDevice iPaymentDevice) {

        //
        paymentDevice = iPaymentDevice;
    }
}