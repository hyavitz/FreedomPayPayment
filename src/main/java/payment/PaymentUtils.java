//package payment;
//
//import android.text.TextUtils;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.annotation.WorkerThread;
//
//import com.clover.sdk.v3.payments.Payment;
//import com.softpointdev.quickpoint.QuickPointApplication;
//import com.softpointdev.quickpoint.network.softpoint.pojo.PaymentDetails;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.text.DecimalFormat;
//import java.util.HashMap;
//import java.util.Map;
//
//import retrofit2.Response;
//import timber.log.Timber;
//
//import static com.softpointdev.commonpoint.common.ui.BaseApplication.getStaticContext;
//import static com.softpointdev.quickpoint.QuickPointApplication.MASTER_POOL;
//import static com.softpointdev.quickpoint.QuickPointApplication.SLAVE_POOL;
//import static com.softpointdev.quickpoint.QuickPointApplication.getKioskpointApi;
//import static com.softpointdev.quickpoint.QuickPointApplication.getSessionManager;
//import static com.softpointdev.quickpoint.QuickPointApplication.safeString;
//import static com.softpointdev.quickpoint.activity.BaseActivity.setPendingId;
//
///**
// * Utility class for correctly sending payment data from an Android device to SoftPoint APIs.
// *
// * @author Joshua Monson
// *
// * Created On 2019/06/13
// * Updated On 2019/06/13
// */
//@Deprecated
//public final class PaymentUtils {
//
//    /**
//     * Private constructor to stop instances of this class from getting created.
//     */
//    private PaymentUtils() {}
//
//    /**
//     * Insert a pending payment at the start of a transaction after it has been received on the device.
//     *
//     */
//    @WorkerThread
//    public static void insertPaymentPending(String uniqueTicketId, String ticketId, String openedAt, String paymentId, double totalAmount, double tipAmount, @Nullable PendingPaymentListener listener) {
//        Timber.v("Starting Payment");
//
//        try {
//
//            Response<String> response = getKioskpointApi().insertWebhookPaymentPending(null, "00", "Pending", "Pending", uniqueTicketId, "", ticketId, openedAt, paymentId, totalAmount, tipAmount).execute();
//            if(response.isSuccessful()) {
//                Timber.v("Starting Payment Response: %s", response.body());
//
//                try {
//                    JSONObject json = new JSONObject(response.body());
//
//                    if(listener != null) {
//                        setPendingId(json.getString("integrated_payments_id"));
//                        listener.onReturn(json.getString("integrated_payments_id"), json.optString("clover_order_id"), json.optString("transactionNo"));
//                    }
//                    return;
//                } catch (Exception e) {
//                    Timber.e(e, "Pending Payment JSON Exception: %s", response.body());
//                }
//            }
//
//        } catch (Exception e) {
//            Timber.w(e, "Pending Payment Exception:");
//        }
//
//        if(listener != null) {
//            listener.onError();
//        }
//    }
//
//    /**
//     * RequestResponse message to send to the SoftPoint servers. This keeps track of what is sent to a payment application as well as the response or what was retrieved back.
//     *
//     * @param pendingId The pending id of the payment being made
//     * @param uniqueTicketId The SoftPoint unique ticket id
//     * @param pay_request The request made to the payment application
//     * @param pay_response The response from the payment application
//     * @param exception The exception thrown if any
//     * @param tag The tag to sent
//     */
//    @WorkerThread
//    public static void insertPaymentRequestResponse(@NonNull String uniqueTicketId, @Nullable String pendingId, @Nullable String pay_request, @Nullable String pay_response, @Nullable String exception, @Nullable String tag) {
//
//        try {
//            Response<String> response = getKioskpointApi().insertPaymentRequestResponse(uniqueTicketId, pendingId, pay_request, pay_response, exception, tag).execute();
//
//            if(response.isSuccessful()) {
//                if(!TextUtils.isEmpty(response.body())) {
//                    Timber.v("RequestResponse: %s", response.body());
//                    try {
//                        JSONObject object = new JSONObject(response.body());
//                        String pending = object.optString("pending_id", "");
//                        if(!TextUtils.isEmpty(pending)) {
//                            //return pending;
//                        }
//                    } catch (JSONException e) {
//                        Timber.w(e, "RequestResponse JSON Exception: ");
//                    }
//                }
//            }
//        } catch (Exception e) {
//            Timber.w(e, "RequestResponse Exception: ");
//        }
//    }
//
//    @WorkerThread
//    public static void submitWebhookPayment(String paymentStatus, String status, String pendingId, String uniqueTicketId, String ticketId, String cloverOrderId, String paymentId, String webhookId, boolean already_run, PaymentDetails details, WebhookPaymentListener listener){
//
//        // Already run is a phone payment to be done later/on our APIs so not done by the device itself
//
//        // Send log that the insert is starting.
//        SLAVE_POOL.submit(() -> {
//            try {
//                getKioskpointApi().sendLog("Webhook Start", "Starting Payment Send").execute();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
//
//        // The payment ahs already been inserted into our database though some other means
//        if(already_run) {
//
//            Timber.v(new Exception("FAKE"), "Already Run Log attempt %s %s %s", status, uniqueTicketId, paymentId);
//            // Webhook done
//            SLAVE_POOL.submit(() -> {
//                try {
//                    getKioskpointApi().sendLog("Webhook Response", "Data Already Run").execute();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            });
//            // TODO: this needs to be done by the Idle activity
//
//            if(listener != null) {
//                listener.onReturn(pendingId, "Payment Successful!");
//            }
//            return;
//
//        } else {
//            try {
//                Map<String, String> options = new HashMap<>();
//
//                options.put("ov_ticket_id", safeString(ticketId));
//                options.put("unique_ticket_id", safeString(uniqueTicketId));
//                options.put("ov_payment_id", safeString(paymentId));
//                options.put("unique_webhook_id", safeString(webhookId));
//                options.put("pending_id", safeString(pendingId));
//
//                // TODO: These two fields need to be checked and verifyed that they are correct
//                options.put("amount", safeString(details.getAmount()));
//                options.put("tip", safeString(details.getTipAmount()));
//
//                System.out.println("<><><> amounts" + details.getTipAmount() + " " + details.getAmount());
//
//                options.put("cardType", safeString(details.getCardType()));
//                options.put("cardHolder", safeString(details.getCardHolder()));
//                options.put("payment_status", safeString(paymentStatus));
//                options.put("status", safeString(status));
//                options.put("last4", safeString(details.getLast4()));
//                options.put("aio_accountid", safeString(details.getAio_accountid()));
//                options.put("authCode", safeString(details.getAuth_code()));
//                options.put("refNum", safeString(details.getRef_num()));
//                options.put("entryType", safeString(details.getEntryType()));
//                options.put("HRef", safeString(details.getHref()));
//                options.put("resCode", safeString(details.getResult_code()));
//                options.put("resMessage", safeString(details.getResult_message()));
//                options.put("resDetailedMessage", safeString(details.getResult_detailedMessage()));
//                options.put("transactionNo", safeString(details.getTransactionNo()));
//                options.put("server", safeString(""));
//                options.put("type", "DPOV");
//                options.put("int_version", safeString(details.getIntegrator_version()));
//                options.put("fw_version", safeString(details.getFw_version()));
//                options.put("pay_response", safeString(details.getPay_response())); // can't send this as its to long and is causing other issues apparently
//                options.put("gc_number", safeString(details.getGcNumber()));
//                options.put("receiptEmvTagMap", safeString(details.getEmvTagData()));
//
//                options.put("clover_order_id", safeString(cloverOrderId));
//                options.put("clover_payment_id", safeString(details.getCloverPaymentId()));
//
//                options.put("cc_token", safeString(details.getToken()));
//                options.put("exp_data", safeString(details.getExp_date()));
//                if(details.isPreAuth()) {
//                    options.put("pre_auth", "1");
//                    options.put("pre_auth_increment", details.isPreAuthIncrement() ? "1" : "");
//                    options.put("pre_auth_integrated_payment_id", safeString(details.getPreAuthIntegratedId()));
//                }
//                options.put("paymentBeforeSurcharge", safeString(details.getPaymentBeforeSurcharge()));
//                options.put("tipBeforeSurcharge", safeString(details.getTipBeforeSurcharge()));
//                options.put("surchargeAmount", safeString(details.getSurchargeAmount()));
//                options.put("surchargeCardType", safeString(details.getSurchargeCardType()));
//                if((status.equalsIgnoreCase("Paid") || status.equalsIgnoreCase("TipAdjust"))
//                        && getSessionManager().isPayAfterPrinting()){
//                    options.put("send_pay_after_printing", "1");
//                }
//
//                if (!TextUtils.isEmpty(details.getProcessedAmount())) {
//                    options.put("processedAmount", safeString(details.getProcessedAmount()));
//                }
//
//                options.put("originalAmount", safeString(details.getOriginalAmount()));
//                options.put("originalTip", safeString(details.getOriginalTip()));
//
//                if(details.isCredit()){
//                    options.put("transaction_type", "Credit");
//                }
//
//                Payment cloverPayment = details.getCloverData();
//
//                if (cloverPayment != null) {
//                    if (cloverPayment.getTender() != null)
//                        options.put("clover_tender_id", safeString(cloverPayment.getTender().getId()));
//                    if (cloverPayment.getEmployee() != null) {
//                        options.put("clover_employee_id", safeString(cloverPayment.getEmployee().getId()));
//                    } else {
//                        Timber.d("getEmployee is null");
//                    }
//                    if (cloverPayment.getCardTransaction() != null) {
//                        options.put("clover_last4", safeString(cloverPayment.getCardTransaction().getLast4()));
//                        if (cloverPayment.getCardTransaction().getCardType() != null) {
//                            options.put("clover_ctype", safeString(cloverPayment.getCardTransaction().getCardType().name()));
//                        }
//                        options.put("clover_auth", safeString(cloverPayment.getCardTransaction().getAuthCode()));
//                        options.put("clover_ref_id", safeString(cloverPayment.getCardTransaction().getReferenceId()));
//                        if (cloverPayment.getCardTransaction().getEntryType() != null) {
//                            options.put("clover_etype", safeString(cloverPayment.getCardTransaction().getEntryType().name()));
//                        }
//                        if (cloverPayment.getCardTransaction().getExtra() != null) {
//                            options.put("clover_cvmres", safeString(cloverPayment.getCardTransaction().getExtra().get("cvmResult")));
//                        }
//                    }
//                    if (cloverPayment.getCreatedTime() != null)
//                        options.put("clover_date", safeString(cloverPayment.getCreatedTime().toString()));
//                    String clover_amount;
//                    if (cloverPayment.getAmount() == null) {
//                        clover_amount = "";
//                    } else {
//                        clover_amount = cloverPayment.getAmount() + "";
//                    }
//                    options.put("clover_amount", safeString(clover_amount));
//                    long clover_tipAmount;
//                    if (cloverPayment.getTipAmount() == null) {
//                        clover_tipAmount = 0;
//                    } else {
//                        clover_tipAmount = cloverPayment.getTipAmount();
//                    }
//                    options.put("clover_tip", safeString(clover_tipAmount + ""));
//                    if (cloverPayment.getResult() != null)
//                        options.put("clover_res", safeString(cloverPayment.getResult().name()));
//                }
//
//                try {
//                    Response<String> response = getKioskpointApi().insertWebhookPayment(options).execute();
//                    if(response.isSuccessful()) {
//
//                        // Successfully finished webhook
//                        SLAVE_POOL.submit(() -> {
//                            try {
//                                Timber.d("Data Success: " + response.isSuccessful() + " Return: " + response.body());
//                                getKioskpointApi().sendLog("Webhook Response", "Data Success: " + response.isSuccessful() + " Return: " + response.body()).execute();
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        });
//
//                        JSONObject json = new JSONObject(response.body());
//                        Timber.d(json.toString());
//                        if(json.has("pay_type")) {
//                            if (json.getString("pay_type").equalsIgnoreCase("nip")) {
//                                getSessionManager().savePayType("nip");
//                            } else {
//                                getSessionManager().savePayType("ip");
//                            }
//                        } else {
//                            getSessionManager().savePayType("ip");
//                        }
//
//                        ((QuickPointApplication) QuickPointApplication.getStaticContext()).setPrintSettings();
//                        //receiptSettings.setPay_type(getSessionManager().getPayType());
//
//                        if(json.has("ResponseCode")) {
//
//                            if(listener != null) {
//                                if (json.getString("ResponseCode").equals("1")) {
//                                    if (!TextUtils.isEmpty(json.optString("underpaid"))
//                                            && json.getString("underpaid").equals("Yes")) {
//                                        listener.onUnderPaid(json.optString("integrated_payments_id"), json.getString("amount"), details.getProcessedAmount());
//                                    }else {
//                                        listener.onReturn(json.optString("integrated_payments_id"), "Payment Successful!");
//                                    }
//                                } else {
//                                    listener.onReturn(json.optString("integrated_payments_id"), json.optString("ResponseMessage"));
//                                }
//                            }
//                            return;
//                        }
//                    }
//
//                } catch (Exception e) {
//                    SLAVE_POOL.submit(() -> {
//                        try {
//                            getKioskpointApi().sendLog("Webhook Failed", "Failed to send: " + e.getMessage()).execute();
//                        } catch (Exception ex) {
//                            ex.printStackTrace();
//                        }
//                    });
//                }
//
//                if(listener != null) {
//                    if(!getStaticContext().haveNetworkConnection()) {
//                        listener.onError("No Network Connection!");
//                    } else {
//                        listener.onError("Failed to send Payment... Retrying");
//                    }
//                }
//
//                // Failed, Error displayed before this. Wait a second or two and then attempt again.
//                try {
//                    Thread.sleep(1500);
//                } catch (Exception ignore) {
//
//                }
//
//                MASTER_POOL.submit(() -> submitWebhookPayment(paymentStatus, status, pendingId, uniqueTicketId, ticketId, cloverOrderId, paymentId, webhookId, already_run, details, listener));
//                return;
//            } catch (Exception e) {
//                Timber.w(e);
//            }
//        }
//
//        if(listener != null) {
//            listener.onError("Failed to send Payment... " + ticketId);
//        }
//    }
//
//    public interface PendingPaymentListener {
//
//        void onReturn(@Nullable String pendingId, @Nullable String cloverOrderId, @Nullable String transactionNo);
//
//        void onError();
//    }
//
//    public interface WebhookPaymentListener {
//        void onUnderPaid(String integratedId, String amount, String processedAmount);
//
//        void onReturn(String integratedId, String message);
//
//        void onError(String error);
//    }
//
//    public static String jsonOptString(JSONObject json, String key) {
//        if (json.isNull(key))
//            return "";
//        else
//            return json.optString(key);
//    }
//
//    public static String toSafeCurrencyFormat(String value) {
//        if (value == null || value.isEmpty()) {
//            return "0.00";
//        }
//
//        try {
//            DecimalFormat df = new DecimalFormat("0.00");
//            value = df.format(Double.parseDouble(value));
//        } catch (Exception e) {
//            return "0.00";
//        }
//        return value;
//    }
//
//    public static int SafeInt(String s) {
//        if (TextUtils.isEmpty(s))
//            return 0;
//        try {
//            return Integer.parseInt(s);
//        } catch (NumberFormatException e) {
//            return 0;
//        }
//    }
//
//    public static double SafeDouble(String s) {
//        if (TextUtils.isEmpty(s))
//            return 0d;
//        try {
//            return Double.parseDouble(s);
//        } catch (NumberFormatException e) {
//            return 0d;
//        }
//    }
//}
