package network;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Token {

    public static volatile String token;
    private static long expiration = System.currentTimeMillis();
    private static final int TIMEOUT = 30000;

    public static boolean refresh() {
        if ((System.currentTimeMillis() - expiration) > TIMEOUT) {
            expiration = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public static void generateToken() {
        System.out.println("Expiration: " + expiration);
        if (refresh() || token == null) {
            // TODO: Investigate reducing this API payload
            Call<String> generateTokenCall = GenerateTokenApiController.getGenerateTokenApiCall();

            generateTokenCall.enqueue(new Callback<>() {

                @SneakyThrows
                @Override
                public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {

                    if (response.isSuccessful()) {
                        assert response.body() != null;
                        JSONObject responseJsonObject = new JSONObject(response.body());
                        token = responseJsonObject.optString("ResponseMessage", "");
                        System.out.println("Token API response success and token is: " + token);
                    } else {
                        System.out.println("Token API response fail.");
                    }
                }

                @Override
                public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                    System.out.println("Token API response fail.");
                }
            });
        }
    }
}