package config;

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

public class RegisterDeviceApiController {

    private static Retrofit retrofit;

    public static Retrofit getRegisterDeviceApiRequestRetrofitInstance() {

        if (retrofit == null) {
            synchronized (RegisterDeviceApiController.class) {
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

    public static RegisterDeviceApiRequest getRegisterDeviceApiRequest() {
        Retrofit apiRetrofit = getRegisterDeviceApiRequestRetrofitInstance();
        return apiRetrofit.create(RegisterDeviceApiRequest.class);
    }

    public static Call<String> getRegisterDeviceApiCall(String locationId, String token, String deviceType, String deviceId, String application, String appVersion) {

        System.out.println(locationId);
        System.out.println(token);
        System.out.println(deviceType);
        System.out.println(deviceId);
        System.out.println(application);
        System.out.println(appVersion);
        return getRegisterDeviceApiRequest().registerDevice(locationId, token, deviceType, deviceId, application, appVersion);
    }
}