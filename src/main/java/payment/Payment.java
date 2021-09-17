package payment;

import interceptor.CommandListener;
import network.TransactionLog;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.HashMap;
import java.util.Map;

public class Payment {

    private Payment() {}

//    public static void insertPaymentPending(String uniqueTicketId, String ticketId, String paymentId, String openedAt, double amount, double tip, @Nullable PendingPaymentListener pendingPaymentListener) {
//    public static void insertPaymentPending(String uniqueTicketId, String ticketId, String paymentId, String openedAt, double amount, double tip, @Nullable PendingPaymentListener pendingPaymentListener) {
//
//        Map<String, String> options = new HashMap<>();
//        options.put("unique_ticket_id", uniqueTicketId);
//        options.put("ov_ticket_id", ticketId);
//        options.put("ov_payment_id", paymentId);
//        options.put("when", openedAt);
//        options.put("amount", String.valueOf(amount));
//        options.put("tip", String.valueOf(tip));
//
//        //Call<String> applyPaymentPendingCall = ApplyPaymentApiController.getApplyPaymentPendingApiCall(options);
//        Call<String> applyPaymentPendingCall = ApplyPaymentApiController.getApplyPaymentPendingApiCall(uniqueTicketId, ticketId, paymentId, openedAt, amount, tip);
//
//        System.out.println("About to enqueue create ticket...");
//        applyPaymentPendingCall.enqueue(new Callback<>() {
//
//            @Override
//            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
//                assert response.body() != null;
//                JSONObject responseJSONObject = new JSONObject(response.body());
//                assert pendingPaymentListener != null;
//                pendingPaymentListener.onReturn(responseJSONObject.getString("integrated_payments_id"), responseJSONObject.optString("transactionNo"));
//                new Thread(() -> {
//                    try {
//                        TransactionLog.generateLog("IC Transaction Finished", "test");
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    // TODO: GenerateLog 'validate payment'
//                }).start();
//                // TODO: response.body() isSuccessful
//            }
//
//            @Override
//            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
//                new Thread(() -> {
//                    // TODO: GenerateLog 'validate payment'
//                }).start();
//                // TODO: response.body() !isSuccessful
//                assert pendingPaymentListener != null;
//                pendingPaymentListener.onError();
//            }
//        });
//    }

//    public static void insertPaymentRequestResponse(@Nullable String uniqueTicketId, @Nullable String pendingId, @Nullable String paymentRequest, @Nullable String paymentResponse) {
//
//        Map<String, String> options = new HashMap<>();
//        options.put("unique_ticket_id", uniqueTicketId);
//        options.put("pending_id", pendingId);
//        options.put("payment_request", paymentRequest);
//        options.put("payment_response", paymentResponse);
//
//        //Call<String> applyPaymentRequestResponseCall = ApplyPaymentApiController.getApplyPaymentRequestResponseApiCall(options);
//        Call<String> applyPaymentRequestResponseCall = ApplyPaymentApiController.getApplyPaymentRequestResponseApiCall(uniqueTicketId, pendingId, paymentRequest, paymentResponse);
//
//        applyPaymentRequestResponseCall.enqueue(new Callback<>() {
//            @Override
//            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
//                assert response.body() != null;
//                JSONObject responseJSONObject = new JSONObject(response.body());
//                new Thread(() -> {
//                    try {
//                        TransactionLog.generateLog("PaymentRequestResponse", "Test");
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    // TODO: GenerateLog 'validate payment'
//                }).start();
//
//                // TODO: Check return pendingId
//
//                // TODO: response.body() isSuccessful
//            }
//
//            @Override
//            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
//                System.out.println("CommandListener.isComplete: " + CommandListener.isComplete);
//                CommandListener.isComplete = true;
//                System.out.println("CommandListener.isComplete: " + CommandListener.isComplete);
//                new Thread(() -> {
//                    // TODO: GenerateLog 'validate payment'
//                }).start();
//                // TODO: response.body() !isSuccessful
//
//            }
//        });
//    }

    public static void insertPaymentCapture(String token, String locationId, String deviceId, String ovTicketId,
                                            String uniqueWebhookId, String ovPaymentId, String uniqueTicketId,
                                            PaymentDetails paymentDetails) {

        System.out.println("i got this amount here of :" + paymentDetails.getAmount());

//    public static void insertPaymentCapture(String locationId, String uniqueTicketId, String status, PaymentDetails paymentDetails) {

        new Thread(() -> {
            // TODO: GenerateLog 'validate payment'
            try {
                TransactionLog.generateLog("Insert Payment", "CAPTURE");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }).start();

//
//        System.out.println("Mapping out payment...");
//
//            Map<String, String> options = new HashMap<>();
//
//            options.put("ov_ticket_id", ticketId);
//            options.put("unique_ticket_id", uniqueTicketId);
//            options.put("ov_payment_id", paymentId);
//            options.put("unique_webhook_id", webhookId);
//            options.put("pending_id", pendingId);
//
//            options.put("amount", paymentDetails.getAmount());
//            options.put("tip", paymentDetails.getTip());
//
//            options.put("cardType", paymentDetails.getCardType());
//            options.put("cardHolder", paymentDetails.getCardHolder());
//            options.put("payment_status", paymentStatus);
//            options.put("status", status);
//            options.put("last4", paymentDetails.getMaskedCardNumber());
//            options.put("aio_accountid", paymentDetails.getAioAccountId());
//            options.put("authCode", paymentDetails.getAuthCode());
//            options.put("refNum", paymentDetails.getReferenceNumber());
//            options.put("entryType", paymentDetails.getEntryMode());
//            options.put("HRef", paymentDetails.getHostReferenceNumber());
//            options.put("resCode", paymentDetails.getResultCode());
//            options.put("resMessage", paymentDetails.getResultMessage());
//            options.put("resDetailedMessage", paymentDetails.getResultMessageDetail());
//            options.put("transactionNo", paymentDetails.getTransactionNumber());
//
//            options.put("server", "99"); // TODO: Source value
//
//            options.put("type", "DPOV"); // TODO: Source value
//
//            options.put("int_version", paymentDetails.getVersionIntegrator());
//            options.put("fw_version", paymentDetails.getFwVersion());
//            options.put("pay_response", paymentDetails.getResultResponse()); // TODO: Ensure this sends
//
//            options.put("gc_number", paymentDetails.getGiftCardNumber());
//            options.put("receiptEmvTagMap", paymentDetails.getEmvTagData());
//
//            options.put("cc_token", paymentDetails.getToken());
//            options.put("exp_data", paymentDetails.getExpiryDate());
//
//            if (paymentDetails.isPreAuth()) {
//                options.put("pre_auth", "1");
//                options.put("pre_auth_increment", paymentDetails.isIncrementalAuth() ? "1" : "");
//                options.put("pre_auth_integrated_payment_id", paymentDetails.getPreAuthIntegratedId());
//            }
//
//            options.put("paymentBeforeSurcharge", paymentDetails.getAmountBeforeSurcharge());
//            options.put("tipBeforeSurcharge", paymentDetails.getTipBeforeSurcharge());
//            options.put("surchargeAmount", paymentDetails.getSurcharge());
//            options.put("surchargeCardType", paymentDetails.getSurchargeCardType());
//
////            if (!paymentDetails.getTotalProcessed().equalsIgnoreCase("") && !paymentDetails.getTotalProcessed().isEmpty() && paymentDetails.getTotalProcessed() != null) {
////                options.put("processedAmount", paymentDetails.getTotalProcessed());
////            }
//
//            options.put("originalAmount", paymentDetails.getOriginalAmount());
//            options.put("originalTip", paymentDetails.getOriginalTip());
//
//            if (paymentDetails.isCredit()) {
//                options.put("transaction_type", "Credit");
//            }
//
//            //Call<String> insertPaymentCaptureCall = ApplyPaymentApiController.getApplyPaymentCaptureApiCall(options);


        System.out.println("Would sure like to set the amount in this call, it is:" + paymentDetails.getAmount());

            Call<String> insertPaymentCaptureCall = ApplyPaymentApiController.getApplyPaymentCaptureApiCall(
                    token, locationId, deviceId, paymentDetails.getTransactionNumber(), ovTicketId, uniqueWebhookId,
                    ovPaymentId, uniqueTicketId, paymentDetails.getAmount(), paymentDetails.getStatus(), paymentDetails.getDecision(),
                    paymentDetails.getResultCode(), paymentDetails.getResultMessage(), paymentDetails.getAuthCode(),
                    paymentDetails.getMaskedCardNumber(), paymentDetails.getCardType(), paymentDetails.getHostReferenceNumber(),
                    paymentDetails.getReferenceNumber(), paymentDetails.getRawData());

            insertPaymentCaptureCall.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                    System.out.println("IC FINISH TRANSACTION");

                    assert response.body() != null;
                    JSONObject responseJSONObject = new JSONObject(response.body());

                    if (responseJSONObject.optString("ResponseCode").equalsIgnoreCase("1")) {
                        new Thread(() -> {
                            try {
                                TransactionLog.generateLog("InsertPayment", "Test");
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            // TODO: GenerateLog 'validate payment'
                        }).start();

                        // TODO: onSuccess
                    } else {
                        // TODO: paymentFailed
                    }

                    new Thread(() -> {
                        // TODO: GenerateLog 'validate payment'
                    }).start();
                    // TODO: !onSuccess
                }

                @Override
                public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                    System.out.println("onFail-CommandListener.isComplete: " + CommandListener.isComplete);
                    CommandListener.isComplete = true;
                    System.out.println("CommandListener.isComplete: " + CommandListener.isComplete);

                    new Thread(() -> {
                        // TODO: GenerateLog 'validate payment'
                    }).start();

                    // TODO: onFail

                    try {
                        Thread.sleep(1500);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    public static void insertPaymentRemove(String token, String locationId, String id_pay) {

        Call<String> applyPaymentRemoveCall = ApplyPaymentApiController.getApplyPaymentRemoveApiCall(token, locationId, id_pay);

        applyPaymentRemoveCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                System.out.println("CommandListener.isComplete: " + CommandListener.isComplete);
                CommandListener.isComplete = true;
                System.out.println("CommandListener.isComplete: " + CommandListener.isComplete);
                assert response.body() != null;
                JSONObject responseJSONObject = new JSONObject(response.body());
                new Thread(() -> {
                    // TODO: GenerateLog 'validate payment'
                    try {
                        TransactionLog.generateLog("RemovePayment", "Test");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }).start();

                // TODO: Check return pendingId

                // TODO: response.body() isSuccessful
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                System.out.println("CommandListener.isComplete: " + CommandListener.isComplete);
                CommandListener.isComplete = true;
                System.out.println("CommandListener.isComplete: " + CommandListener.isComplete);
                new Thread(() -> {
                    // TODO: GenerateLog 'validate payment'
                }).start();
                // TODO: response.body() !isSuccessful

            }
        });

    }

    public interface PendingPaymentListener {
        void onReturn(@Nullable String pendingId, @Nullable String transactionNumber);
        void onError();
    }

    public interface TransactionWebhookListener {
        void onUnderPaid(String integratedId, String amount, String appliedAmount);
        void onReturn(String integratedId, String message);
        void onError(String error);
    }
}