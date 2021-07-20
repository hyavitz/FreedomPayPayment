package interceptor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import network.ApplyPaymentApiRequest;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.util.concurrent.TimeUnit;

public class PaymentAPI {

    private static Retrofit sRetrofit;

    public static Retrofit getInstance() {

        //
        if(sRetrofit == null) {
            synchronized (PaymentAPI.class) {
                if(sRetrofit == null) {

                    // Interceptor to load balance servers
                    Interceptor interceptor = new PaymentApiInterceptor();

                    // TODO: Fix SSL Unverified Certificate error
                    OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
                            .readTimeout(30, TimeUnit.SECONDS)
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .addInterceptor(interceptor)
                            .hostnameVerifier(new HostnameVerifier() {
                                @Override
                                public boolean verify(String hostname, SSLSession session) {
                                    if (hostname.contains("softpointdev.com")
                                        || hostname.contains("softpoint.us")
                                        || hostname.contains("softpointcloud.com")) {
                                        return true;
                                    }
                                    return false;
                                }
                            });

                    //
                    HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor();
                    logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                    okHttpClientBuilder.addInterceptor(logInterceptor);

                    //
                    OkHttpClient sOkHttpClient = okHttpClientBuilder.build();

                    //
                    Gson gson = new GsonBuilder()
                            .setLenient()
                            .create();

                    // TODO: Source server from config array
                    sRetrofit = new Retrofit.Builder()
                            .baseUrl("https://softpointdev.com/")
                            .addConverterFactory(ScalarsConverterFactory.create())
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .client(sOkHttpClient)
                            .build();
                }
            }
        }
        return sRetrofit;
    }

    private static ApplyPaymentApiRequest getWebhookResponse() {

       Retrofit api = getInstance();
        return api.create(ApplyPaymentApiRequest.class);
    }

    // SALE
    public static Call<String> postWebhookResponse(String ov_location_id, String employee_id, String status,
                                                   String type, String unique_ticket_id, String server, String ov_ticket_id,
                                                   String opened_at, String ov_payment_id, String amount, String tip,
                                                   String payment_status, String transactionNo){

        return getWebhookResponse().insertWebhookPayment(ov_location_id, employee_id, status,
                type, unique_ticket_id, server, ov_ticket_id, opened_at, ov_payment_id,
                amount, tip,
                payment_status, transactionNo);
    }

//    // VOID 1
//    public static Call<String> postWebhookRequestVoid1(String id_pay, String transactionNo, String ref_num,
//                                                      String pay_type, String amount, String pin, String from) {
//
//        return getWebhookResponse().removeWebhookPayment(id_pay, transactionNo, ref_num, pay_type, amount, pin, from);
//    }
//
//    public static Call<String> postWebhookRequestVoid1(Map<String, String> options) {
//
//        return getWebhookResponse().removeWebhookPayment(options);
//    }
//
//    // VOID 2
//    public static Call<String> postWebhookRequestVoid2(String id_pay, String transactionNo, String pay_type,
//                                                       String pin, String amount, String from) {
//
//        return getWebhookResponse().removeWebhookPayment(id_pay, transactionNo, pay_type, pin, amount, from);
//    }
//
//    public static Call<String> postWebhookRequestVoid2(Map<String, String> options) {
//
//        return getWebhookResponse().removeWebhookPayment(options);
//    }
//
//    // VOID 3
//    public static Call<String> postWebhookRequestVoid3(String id_pay, String amount, String pay_type, String action, String pin) {
//
//        return getWebhookResponse().removeWebhookPayment(id_pay, amount, pay_type, action, pin);
//    }
//
//    public static Call<String> postWebhookRequestVoid3(Map<String, String> options) {
//
//        return getWebhookResponse().removeWebhookPayment(options);
//    }

    // VOID A
    public static Call<String> postWebhookRequestVoid(String id_pay) {

        return getWebhookResponse().removeWebhookPayment(id_pay);
    }
}