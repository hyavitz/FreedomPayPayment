package payment;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;

import java.util.Map;

import static data.Constant.*;

public interface ApplyPaymentApiRequest {

    @FormUrlEncoded
    @POST(APPLY_PAYMENT_PENDING_API)
    Call<String> insertPaymentPending(@FieldMap Map<String, String> options);

    @POST(APPLY_PAYMENT_PENDING_API)
    Call<String> insertPaymentPending(
            @Query("unique_ticket_id") String uniqueTicketId, @Query("ov_ticket_id") String ticketId,
            @Query("ov_payment_id") String paymentId, @Query("when") String openedAt,
            @Query("amount") String amount, @Query("tip") String tip);

    @FormUrlEncoded
    @POST(APPLY_PAYMENT_REQUEST_RESPONSE_API)
    Call<String> insertPaymentRequestResponse(@FieldMap Map<String, String> options);

    @POST(APPLY_PAYMENT_REQUEST_RESPONSE_API)
    Call<String> insertPaymentRequestResponse(
            @Query("token") String token, @Query("location_id") String locationId,
            @Query("device_id") String deviceId, @Query("device_type") String deviceType,
            @Query("application") String application, @Query("app_version") String applicationVersion);

    @POST(APPLY_PAYMENT_REQUEST_RESPONSE_API)
    Call<String> insertPaymentRequestResponse(
            @Query("unique_ticket_id") String uniqueTicketId, @Query("pending_id") String pendingId,
            @Query("request") String paymentRequest, @Query("response") String paymentResponse);

//    @FormUrlEncoded
//    @POST(APPLY_PAYMENT_CAPTURE_API)
//    Call<String> insertPaymentCapture(@FieldMap Map<String, String> options);

//    @POST(APPLY_PAYMENT_CAPTURE_API)
//    Call<String> insertPaymentCapture(
//            @Query("token") String token, @Query("location_id") String locationId,
//            @Query("unique_ticket_id") String uniqueTicketId, @Query("status") String status);

    @POST(APPLY_PAYMENT_CAPTURE_API)
    Call<String> insertPaymentCapture(
            @Query("token") String token, @Query("location_id") String locationId,
            @Query("device_id") String deviceId, @Query("transactionNo") String transactionNumber,
            @Query("ov_ticket_id") String ovTicketId, @Query("unique_webhook_id") String uniqueWebhookId,
            @Query("ov_payment_id") String ovPaymentId, @Query("unique_ticket_id") String uniqueTicketId,
            @Query("amount") String amount, @Query("status") String status, @Query("payment_status") String paymentStatus,
            @Query("result_code") String resultCode, @Query("result_message") String resultMessage, // TODO: These are almost certainly not correctly formed...
            @Query("authCode") String authCode, @Query("last4") String maskedCardNumber,
            @Query("cardType") String cardType, @Query("HRef") String hostReferenceNumber,
            @Query("refNum") String referenceNumber, @Query("pay_response") String rawData);

    @POST(APPLY_PAYMENT_REMOVE_API)
    Call<String> insertPaymentRemove(
            @Query("token") String token, @Query("location_id") String locationId,
            @Query("id_pay") String paymentId);
}