package network;

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

public class GenerateTransactionLogApiController {

    private static Retrofit retrofit;

    public static Retrofit getGenerateLogApiRequestRetrofitInstance() {

        if (retrofit == null) {
            synchronized (GenerateTransactionLogApiController.class) {
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


    public static GenerateTransactionLogApiRequest getGenerateLogApiRequest() {
        Retrofit apiRetrofit = getGenerateLogApiRequestRetrofitInstance();
        return apiRetrofit.create(GenerateTransactionLogApiRequest.class);
    }

    public static Call<String> getGenerateLogApiCall(String from, String position, String where, String exact, String token, String locationId,
                                                     String deviceType, String deviceId, String application, String applicationVersion) { // TODO: Probably need more here, like WHERE or WHICH

        return getGenerateLogApiRequest().generateLog(from, position, where, exact, token, locationId, deviceType, deviceId, application, applicationVersion);

    }
}
