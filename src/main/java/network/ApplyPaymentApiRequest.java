package network;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;

import java.util.Map;

public interface ApplyPaymentApiRequest {

    String ROOT_PATH = "DPS/";
    String VERSION_PATH = "version/";
    String VERSION = "060/";
    String API_PATH = "api/";

    String WH_PAYMENT_API = ROOT_PATH + VERSION_PATH + VERSION + API_PATH;

    String INSERT_PAYMENT_ENDPOINT = "dp_ov_insert_wh_payment.php";
    String REMOVE_PAYMENT_ENDPOINT = "dp_ov_remove_payment.php";

    String QP_WH_INSERT_PAYMENT = WH_PAYMENT_API + INSERT_PAYMENT_ENDPOINT;
    String QP_WH_REMOVE_PAYMENT = WH_PAYMENT_API + REMOVE_PAYMENT_ENDPOINT;

    @POST(QP_WH_INSERT_PAYMENT)
    Call<String> insertWebhookPayment(@Query("ov_location_id") String ov_location_id,
                                      @Query("employee_id") String employee_id, @Query("status") String status,
                                      @Query("type") String type, @Query("unique_ticket_id") String unique_ticket_id,
                                      @Query("server") String server, @Query("ov_ticket_id") String ov_ticket_id,
                                      @Query("opened_at") String opened_at, @Query("ov_payment_id") String ov_payment_id,
                                      @Query("amount") String amount, @Query("tip") String tip,
                                      @Query("payment_status") String payment_status,
                                      @Query("transactionNo") String transactionNo);

    @POST(QP_WH_REMOVE_PAYMENT)
    Call<String> removeWebhookPayment(@Query("id_pay") String id_pay);

    Call<String> removeWebhookPayment(String id_pay, String transactionNo, String ref_num, String pay_type, String amount, String pin, String from);

//    @POST(QP_WH_REGISTER_DEVICE)
//    Call<String> registerDevice(@Query("location_id"), String location_id, @Query("token") String token,
//                                @Query("device_type") String device_type, @Query("device_id"), String device_id,
//                                @Query("application") String application, @Query("app_version"), String app_version);

}