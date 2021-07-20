package interceptor;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RetrofitFactory {

    // The servers to use that we can send API calls to.
    private static List<String> sApiServers;

    // The current server being used
    private static volatile int sCurrentServer;

    private static Retrofit sRetrofit;
    private static OkHttpClient sOkHttpClient;

    public static int getCurrentServer() {
        return sCurrentServer;
    }

    public static int getNumberOfServers() {
        return getServers().size();
    }

    public static String getCurrentServerName() {
        return getServers().get(sCurrentServer);
    }

    public static synchronized void switchServer(int server) {
        sCurrentServer = server % getNumberOfServers();
    }

    public static synchronized void switchServer() {
        switchServer(getCurrentServer() + 1);
    }

    public static List<String> getServers() {
        if (sApiServers == null) {
            sApiServers = new ArrayList<>();

           // sApiServers.addAll(Arrays.asList(Constant.serverUrl));

            //String primary_server = getSessionManager().getPrimaryServer();
            String primary_server = "softpointdev.com";
            if(primary_server.contains("node") || primary_server.contains("lb.softpointcloud.com")) {
                primary_server = "https://" + primary_server + ":2351/";
            } else {
                primary_server = "https://" + primary_server + "/";
            }
            int primPos = 0;
            if(sApiServers.contains("https://lb.softpointcloud.com:2351/")) {
                primPos = 1;
            }

            //Timber.i("Testing Primary Server %s %s", sApiServers.get(primPos), primary_server);
            //if(!TextUtils.isEmpty(getSessionManager().getPrimaryServer()) && !sApiServers.get(primPos).equalsIgnoreCase(primary_server)) {
             //   sApiServers.remove(primary_server);
                //Timber.i("Setting Primary Server %s %s", sApiServers.get(primPos), primary_server);
                sApiServers.add(primPos, primary_server);
            //}
        }

        return sApiServers;
    }

    public static void setServers(List<String> ser) {
        synchronized (RetrofitFactory.class) {

            // Create a new object so that the old one can be used
            sApiServers = new ArrayList<>();

            sApiServers.clear();
            if(ser.contains("https://lb.softpointcloud.com:2351/")) {
                ser.remove("https://lb.softpointcloud.com:2351/");
                ser.add(0, "https://lb.softpointcloud.com:2351/");
            }
            sApiServers.addAll(ser);

            switchServer(0);
        }
    }

    public static String getServer() {
        return getServers().get(sCurrentServer);
    }

    public static Retrofit getInstance() {

        if(sRetrofit == null) {
            synchronized (RetrofitFactory.class) {
                if(sRetrofit == null) {

                    // Basic interceptor that will change the server to the correct one as needed
                    Interceptor interceptor = new PaymentApiInterceptor();

                    OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
                            .readTimeout(30, TimeUnit.SECONDS)
                            .addInterceptor(interceptor)
                            .connectTimeout(30, TimeUnit.SECONDS);

                    // If we are in debug mode we can print out a lot of things

                        HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor();
                        logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                        okHttpClientBuilder.addInterceptor(logInterceptor);


                    sOkHttpClient = okHttpClientBuilder.build();

                    sRetrofit = new Retrofit.Builder()
                            .baseUrl(getServer())
                            .addConverterFactory(ScalarsConverterFactory.create())
                            .addConverterFactory(GsonConverterFactory.create())
                            .client(sOkHttpClient)
                            .build();
                }
            }
        }

        return sRetrofit;
    }
}