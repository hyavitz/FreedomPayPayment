package network;

import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Heartbeat {

    public static volatile Boolean hasNetworkConnection;
    private static long expiration = System.currentTimeMillis();
    private static final int TIMEOUT = 30000;

    public static boolean checkHeartbeat() {

        Call<String> checkHeartbeatCall = CheckHeartbeatApiController.getCheckHeartbeatApiCall();

        checkHeartbeatCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {

                // TODO: onSuccess
                hasNetworkConnection = true;
                // TODO: !onSuccess
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {

                // TODO: onFailure
                hasNetworkConnection = false;
            }
        });

        // TODO: Wait for it...
        while (hasNetworkConnection == null) {}
        return hasNetworkConnection;
    }
}