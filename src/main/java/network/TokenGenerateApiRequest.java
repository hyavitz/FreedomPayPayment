package network;

import retrofit2.Call;
import retrofit2.http.GET;

public interface TokenGenerateApiRequest {

    final static String TOKEN_GENERATION_PATH = "/DPS/internal";
    final static String TOKEN_GENERATION_ENDPOINT = "/generatetoken.php";
    final static String TOKEN_GENERATION_API = /*TOKEN_GENERATION_HOST + */ TOKEN_GENERATION_PATH + TOKEN_GENERATION_ENDPOINT;

    @GET(TOKEN_GENERATION_API)
    Call<String> generateToken();
}