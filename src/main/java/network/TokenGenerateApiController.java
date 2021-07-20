package network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import interceptor.InterceptorFactory;
import interceptor.OkHttpClientFactory;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class TokenGenerateApiController {

    private static Retrofit retrofit;
    private static OkHttpClient okHttpClient;

    public static Retrofit getGenerateTokenApiRequestRetrofitInstance() {

        if (retrofit == null) {
            synchronized (TokenGenerateApiController.class) {
                if (retrofit == null) {

                    Interceptor interceptor = InterceptorFactory.getInterceptor();
                    OkHttpClient okHttpClient = OkHttpClientFactory.getClient(interceptor);

                    Gson gson = new GsonBuilder()
                            .setLenient()
                            .create();

                    retrofit = new Retrofit.Builder()
                            .baseUrl("https://softpointdev.com/") // TODO: Consolidate constants
                            .addConverterFactory(ScalarsConverterFactory.create())
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .client(okHttpClient)
                            .build();
                }
            }
        }
        return retrofit;
    }

    public static TokenGenerateApiRequest getGenerateTokenApiRequest() {
        Retrofit tokenApiRequestRetrofit = getGenerateTokenApiRequestRetrofitInstance();
        return tokenApiRequestRetrofit.create(TokenGenerateApiRequest.class);
    }

    public static Call<String> getGenerateTokenApiCall() {
        return getGenerateTokenApiRequest().generateToken();
    }
}