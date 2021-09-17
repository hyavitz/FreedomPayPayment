package network;

import retrofit2.Call;
import retrofit2.http.GET;

import static data.Constant.GENERATE_TOKEN_API;

public interface GenerateTokenApiRequest {

    @GET(GENERATE_TOKEN_API)
    Call<String> generateToken();
}