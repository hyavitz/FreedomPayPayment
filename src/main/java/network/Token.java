package network;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Token {

    private static volatile String token;

    public static String getToken() {

        // TODO: Investigate reducing this API payload
        Call<String> generateTokenCall = TokenGenerateApiController.getGenerateTokenApiCall();

        generateTokenCall.enqueue(new Callback<>() {

            @SneakyThrows
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {

                if (response.isSuccessful()) {
                    token = response.body();
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

        // TODO: Pause here until token has value
        while (token == null) {}

        return token;
    }
}