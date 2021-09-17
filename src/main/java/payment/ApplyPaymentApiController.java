package payment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import data.Constant;
import interceptor.InterceptorFactory;
import interceptor.OkHttpClientFactory;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.util.Map;

public class ApplyPaymentApiController {

    private static Retrofit retrofit;

    public static Retrofit getApplyPaymentApiRequestRetrofitInstance() {

        if (retrofit == null) {
            synchronized (ApplyPaymentApiController.class) {
                if (retrofit == null) {

                    Interceptor interceptor = InterceptorFactory.getInterceptor();
                    OkHttpClient okHttpClient = OkHttpClientFactory.getClient(interceptor);

                    Gson gson = new GsonBuilder()
                            .setLenient()
                            .create();

                    retrofit = new Retrofit.Builder()
                            .baseUrl(Constant.HOST)
                            .addConverterFactory(ScalarsConverterFactory.create())
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .client(okHttpClient)
                            .build();
                }
            }
        }
        return retrofit;
    }

    public static ApplyPaymentApiRequest getApplyPaymentApiRequest() {
        Retrofit apiRetrofit = getApplyPaymentApiRequestRetrofitInstance();
        return apiRetrofit.create(ApplyPaymentApiRequest.class);
    }

    public static Call<String> getApplyPaymentPendingApiCall(Map<String, String> options) {
        return getApplyPaymentApiRequest().insertPaymentPending(options);
    }

    public static Call<String> getApplyPaymentPendingApiCall(String uniqueTicketId, String ticketId, String paymentId, String openedAt, double amount, double tip) {
        return getApplyPaymentApiRequest().insertPaymentPending(uniqueTicketId, ticketId, paymentId, openedAt, String.valueOf(amount), String.valueOf(tip));
    }

    public static Call<String> getApplyPaymentRequestResponseApiCall(Map<String, String> options) {
        return getApplyPaymentApiRequest().insertPaymentRequestResponse(options);
    }

    public static Call<String> getApplyPaymentRequestResponseApiCall(String uniqueTicketId, String pendingId, String paymentRequest, String paymentResponse) {
        return getApplyPaymentApiRequest().insertPaymentRequestResponse(uniqueTicketId, pendingId, paymentRequest, paymentResponse);
    }

//    public static Call<String> getApplyPaymentCaptureApiCall(Map<String, String> options) {
//        return getApplyPaymentApiRequest().insertPaymentCapture(options);
//    }

    public static Call<String> getApplyPaymentCaptureApiCall(
            String token, String locationId, String deviceId, String transactionNumber, String ovTicketId, String uniqueWebhookId,
            String ovPaymentId, String uniqueTicketId, String amount, String status, String paymentStatus, String resultCode,
            String resultMessage, String authCode, String maskedCardNumber, String cardType, String hostReferenceNumber,
            String referenceNumber, String rawData) {

        return getApplyPaymentApiRequest().insertPaymentCapture(token, locationId, deviceId, transactionNumber, ovTicketId,
                uniqueWebhookId, ovPaymentId, uniqueTicketId, amount, status, paymentStatus, resultCode, resultMessage, authCode,
                maskedCardNumber, cardType, hostReferenceNumber, referenceNumber, rawData);
    }

    public static Call<String> getApplyPaymentRemoveApiCall(String token, String locationId, String paymentId) {
        return getApplyPaymentApiRequest().insertPaymentRemove(token, locationId, paymentId);
    }
}