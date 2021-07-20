package interceptor;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import config.DeviceConfig;
import okhttp3.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static interceptor.RetrofitFactory.*;

public class PaymentApiInterceptor implements Interceptor {

    private static final SimpleDateFormat LOG_TIME = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    private static final AtomicInteger API_COUNT = new AtomicInteger(0);

    private static volatile String apiToken = "";
    private static volatile long refreshToken = 0;
    private static volatile long heartbeatCheck = 0;

    private static long recheck = 0;

    static final String HEADER_IGNORE_HEARTBEAT = "IGNORE_HEARTBEAT";
    static final String HEADER_IGNORE_TOKEN = "IGNORE_TOKEN";
    static final String HEADER_IGNORE_ARGUMENTS = "IGNORE_ARGUMENTS";
    static final String HEADER_IGNORE_FAILOVER = "IGNORE_FAILOVER";

    @Override
    public Response intercept(Chain chain) throws IOException {

        int count = API_COUNT.incrementAndGet();

        Request request = chain.request();

        boolean checkHeartbeat = !Boolean.parseBoolean(request.header(HEADER_IGNORE_HEARTBEAT));
        boolean checkToken = !Boolean.parseBoolean(request.header(HEADER_IGNORE_TOKEN));
        boolean addArguments = !Boolean.parseBoolean(request.header(HEADER_IGNORE_ARGUMENTS));
        boolean checkFailover = !Boolean.parseBoolean(request.header(HEADER_IGNORE_FAILOVER));

        request.newBuilder().addHeader("Cache-Control", "no-cache"); // Is this actually needed?

        if(heartbeatCheck < System.currentTimeMillis() && checkHeartbeat) {
            synchronized (PaymentApiInterceptor.class) {
                if (heartbeatCheck < System.currentTimeMillis()) {
                    if (recheck == 0 && getCurrentServer() != 0) {
                        recheck = System.currentTimeMillis() + (20 * 60 / 1000);
                    }

                    if (getCurrentServer() == 0) {
                        recheck = 0;
                    } else {

                        if (recheck != 0 && recheck < System.currentTimeMillis()) {
                            switchServer(0);
                        }
                    }

                    // Check Heartbeat of server
                    for (int i = 0; i < getNumberOfServers(); i++) {

                        if (checkServerHeartbeat(count, chain, getCurrentServerName())) {
                            heartbeatCheck = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(30, TimeUnit.SECONDS);
                            break;
                        }

                        switchServer();
                        if (i == (getNumberOfServers() - 1)) {
                            throw new IOException("All Servers Offline");
                        }
                    }
                }
            }
        }

        // Make Generate token call/check
        if(checkToken) {
            for (int i = 0; i < getNumberOfServers(); i++) {

                if (getApiToken(count, chain, getCurrentServerName())) {
                    break;
                }

                switchServer();
            }
        }

        // Make actual API call
        int numServers = getNumberOfServers();
        if (numServers < 1) { numServers = 1;}
        for(int i = 0; i < numServers; i++) {
            String startcall = LOG_TIME.format(Calendar.getInstance().getTime());
            try {
                String serverName;
                if (request.url().port() == 2351 || serverIncludesPort(request.url().host() + ":" + request.url().port())) {
                    serverName = request.url().host() + ":" + request.url().port();
                } else {
                    serverName = request.url().host();
                }


                DeviceConfig config = new DeviceConfig("./config/config.json");

                HttpUrl.Builder constructor = request.url().newBuilder();

                if (addArguments) {
                    constructor.setQueryParameter("token", apiToken)
                            .setQueryParameter("location_id", config.LOCATION_ID)
                            .setQueryParameter("device_type", config.DEVICE)
                            .setQueryParameter("device_id", config.SERIAL_NUMBER)
                            .setQueryParameter("application", config.APPLICATION)
                            .setQueryParameter("app_version", config.VERSION_NAME);
                }

                HttpUrl builder = constructor.build();
                Request.Builder rBuilder = request.newBuilder();

                if(checkFailover) {
                    rBuilder.url(builder.toString()
                            .replace("https://", "")
                            .replace("http://", "")
                            .replace(serverName + "/", getCurrentServerName()));
                }

                request = rBuilder.build();

                String requestUrl = request.url().toString();
                Response response = chain.withConnectTimeout(30, TimeUnit.SECONDS)
                        .withReadTimeout(30, TimeUnit.SECONDS)
                        .withWriteTimeout(30, TimeUnit.SECONDS)
                        .proceed(request);

                if(response.isSuccessful()) {
                    addLog(startcall, "API " + count + " " + requestUrl,String.format("Completed in %s", new SimpleDateFormat("mm:ss.SSS", Locale.US).format((response.receivedResponseAtMillis() - response.sentRequestAtMillis()))), true);

                    return response;
                }

                if(response.code() < 500) {
                }

            } catch (Exception e) {
                addLog(startcall, "API " + count + " " + request.url(),"API Request failed",false);
                if(i == (getNumberOfServers() - 1)) {
                    switchServer();
                    throw new IOException("All " + getNumberOfServers() + " Servers Offline");
                }
            }

            switchServer();
            if(i == (getNumberOfServers() - 1)) {
                throw new IOException("All " + getNumberOfServers() + " Servers Offline");
            }
        }

        throw new IOException("All Servers Offline");
    }

    private boolean serverIncludesPort(String serverName) {

        boolean hasPort = false;
        for(int i = 0; i < getNumberOfServers(); i++) {
            if (getServers().get(i).contains(serverName)) {
                hasPort = true;
                break;
            }
        }
        return hasPort;
    }

    private void addLog(String time, String action, String message, boolean success) {}

    private boolean checkServerHeartbeat(int count, Chain chain, String host) {
        String startcall = LOG_TIME.format(Calendar.getInstance().getTime());

        Request heartbeat = chain.request().newBuilder()
                .cacheControl(CacheControl.FORCE_NETWORK)
                .url(host + Constant.HEARTBEAT_SERVER)
                .build();

        String url = heartbeat.url().toString();

        try {

            Response response = chain.withConnectTimeout(7, TimeUnit.SECONDS)
                    .withReadTimeout(7, TimeUnit.SECONDS)
                    .withWriteTimeout(7, TimeUnit.SECONDS)
                    .proceed(heartbeat); // TODO: Eliminate SSLPeerUnverifiedException

            if(response.body() != null) {
                String data = response.body().string();

                HeartbeatResponse hResponse = new Gson().fromJson(data, HeartbeatResponse.class);

                if (hResponse.getResponseCode() == 1) {
                    addLog(startcall, "API Heartbeat " + count + " " + url, data, true);

                    return true;
                } else {
                    addLog(startcall, "API Heartbeat " + count + " " + url, data, false);
                }
            }

        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            addLog(startcall, "API Heartbeat " + count + " " + url, "Parsing response failed: " + e.getMessage(), false);
        } catch (IOException e) {
            e.printStackTrace();
            addLog(startcall, "API Heartbeat " + count + " " + url, "Request Failed: " + e.getMessage(), false);
        } catch (Exception e) {
            System.out.println("SSL Peer Unverified Exception");
            e.printStackTrace();
            addLog(startcall, "API Heartbeat " + count + " " + url, "General Exception: " + e.getMessage(), false);
        }

        return false;
    }

    private boolean getApiToken(int count, Chain chain, String host) {
        String startcall = LOG_TIME.format(Calendar.getInstance().getTime());
        if (String.valueOf(apiToken).equals("") && refreshToken > System.currentTimeMillis()) {
            return true;
        }

        // We only want one of these running at a time. and can easily do it as success will skip the network call for the next x of time
        synchronized (PaymentApiInterceptor.class) {

            if (String.valueOf(apiToken).equals("") && refreshToken > System.currentTimeMillis()) {
                return true;
            } else {
                // We need to generate a new token

                Request token = chain.request().newBuilder()
                        .cacheControl(CacheControl.FORCE_NETWORK)
                        .url(host + Constant.GENERATE_TOKEN)
                        .build();

                String url = token.url().toString();

                try {

                    Response response = chain.withConnectTimeout(5, TimeUnit.SECONDS)
                            .withReadTimeout(5, TimeUnit.SECONDS)
                            .withWriteTimeout(5, TimeUnit.SECONDS)
                            .proceed(token);

                    if(response.body() != null) {
                        String data = response.body().string();

                        BasicResponse hResponse = new Gson().fromJson(data, BasicResponse.class);

                        if (hResponse.getResponseCode() == 1) {
                            addLog(startcall, "API Token " + count + " " + url, data, true);

                            apiToken = hResponse.getResponseMessage();
                            refreshToken = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(30, TimeUnit.MINUTES);

                            return true;
                        } else {
                            addLog(startcall, "API Token " + count + " " + url, data, false);
                        }
                    }

                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                    addLog(startcall, "API Token " + count + " " + url, "Parsing response failed: " + e.getMessage(), false);
                } catch (IOException e) {
                    e.printStackTrace();
                    addLog(startcall, "API Token " + count + " " + url, "Request Failed: " + e.getMessage(), false);
                } catch (Exception e) {
                    e.printStackTrace();
                    addLog(startcall, "API Token " + count + " " + url, "General Exception: " + e.getMessage(), false);
                }
                return false;
            }
        }
    }
}