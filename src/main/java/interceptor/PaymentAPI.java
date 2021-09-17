//package interceptor;
//
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import payment.ApplyPaymentApiRequest;
//import okhttp3.Interceptor;
//import okhttp3.OkHttpClient;
//import okhttp3.logging.HttpLoggingInterceptor;
//import retrofit2.Call;
//import retrofit2.Retrofit;
//import retrofit2.converter.gson.GsonConverterFactory;
//import retrofit2.converter.scalars.ScalarsConverterFactory;
//
//import javax.net.ssl.HostnameVerifier;
//import javax.net.ssl.SSLSession;
//import java.util.concurrent.TimeUnit;
//
//public class PaymentAPI {
//
//    private static Retrofit sRetrofit;
//
//    public static Retrofit getInstance() {
//
//        //
//        if(sRetrofit == null) {
//            synchronized (PaymentAPI.class) {
//                if(sRetrofit == null) {
//
//                    // Interceptor to load balance servers
//                    Interceptor interceptor = new PaymentApiInterceptor();
//
//                    // TODO: Fix SSL Unverified Certificate error
//                    OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
//                            .readTimeout(30, TimeUnit.SECONDS)
//                            .connectTimeout(30, TimeUnit.SECONDS)
//                            .addInterceptor(interceptor)
//                            .hostnameVerifier(new HostnameVerifier() {
//                                @Override
//                                public boolean verify(String hostname, SSLSession session) {
//                                    if (hostname.contains("softpointdev.com")
//                                        || hostname.contains("softpoint.us")
//                                        || hostname.contains("softpointcloud.com")) {
//                                        return true;
//                                    }
//                                    return false;
//                                }
//                            });
//
//                    //
//                    HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor();
//                    logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//                    okHttpClientBuilder.addInterceptor(logInterceptor);
//
//                    //
//                    OkHttpClient sOkHttpClient = okHttpClientBuilder.build();
//
//                    //
//                    Gson gson = new GsonBuilder()
//                            .setLenient()
//                            .create();
//
//                    // TODO: Source server from config array
//                    sRetrofit = new Retrofit.Builder()
//                            .baseUrl("https://softpointdev.com/") // TODO: Consolidate constants
//                            .addConverterFactory(ScalarsConverterFactory.create())
//                            .addConverterFactory(GsonConverterFactory.create(gson))
//                            .client(sOkHttpClient)
//                            .build();
//                }
//            }
//        }
//        return sRetrofit;
//    }
//
//
//
//    private static ApplyPaymentApiRequest getWebhookResponse() {
//
//       Retrofit api = getInstance();
//        return api.create(ApplyPaymentApiRequest.class);
//    }
//
//    // SALE
//    public static Call<String> postWebhookResponse(String ov_location_id, String employee_id, String status,
//                                                   String type, String unique_ticket_id, String server, String ov_ticket_id,
//                                                   String opened_at, String ov_payment_id, String amount, String tip,
//                                                   String payment_status, String transactionNo){
//
//        return getWebhookResponse().insertPayment(ov_location_id, employee_id, status,
//                type, unique_ticket_id, server, ov_ticket_id, opened_at, ov_payment_id,
//                amount, tip,
//                payment_status, transactionNo);
//    }
//
//    public static Call<String> postWebhookRequestVoid(String id_pay) {
//
//        return getWebhookResponse().insertPaymentRemove(id_pay);
//    }
//}