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

public class CheckHeartbeatApiController {

    private static Retrofit retrofit;

    public static Retrofit getCheckHeartbeatApiRequestRetrofitInstance() {

        if (retrofit == null) {
            synchronized (CheckHeartbeatApiController.class) {
                if (retrofit == null) {

                    Interceptor interceptor = InterceptorFactory.getInterceptor();
                    OkHttpClient okHttpClient = OkHttpClientFactory.getClient(interceptor);

                    Gson gson = new GsonBuilder()
                            .setLenient()
                            .create();

                    retrofit = new Retrofit.Builder()
                            .baseUrl(Constant.HOST) // TODO: Consolidate constants
                            .addConverterFactory(ScalarsConverterFactory.create())
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .client(okHttpClient)
                            .build();
                }
            }
        }
        return retrofit;
    }

    public static CheckHeartbeatApiRequest getCheckHeartbeatApiRequest() {
        Retrofit heartbeatApiRequest = getCheckHeartbeatApiRequestRetrofitInstance();
        return heartbeatApiRequest.create(CheckHeartbeatApiRequest.class);
    }

    public static Call<String> getCheckHeartbeatApiCall() {
        return getCheckHeartbeatApiRequest().checkHeartbeat();
    }
}