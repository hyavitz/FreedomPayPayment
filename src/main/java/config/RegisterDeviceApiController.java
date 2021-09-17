package config;

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

    public static RegisterDeviceApiRequest getRegisterDeviceApiRequest() {
        Retrofit apiRetrofit = getRegisterDeviceApiRequestRetrofitInstance();
        return apiRetrofit.create(RegisterDeviceApiRequest.class);
    }

    public static Call<String> getRegisterDeviceApiCall(Map<String, String> options) {
        return getRegisterDeviceApiRequest().registerDevice(options);
    }

    public static Call<String> getRegisterDeviceApiCall(String token, String locationId, String deviceId, String deviceType, String application, String appVersion) {
        System.out.println("Calling register device with : " + locationId + " token " + token + " device type: " + deviceType + " id " + deviceId + " application "+ application);
        return getRegisterDeviceApiRequest().registerDevice(token, locationId, deviceId, deviceType, application, appVersion);
    }
}