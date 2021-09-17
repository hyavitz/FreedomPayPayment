package network;

import retrofit2.Call;
import retrofit2.http.GET;

import static data.Constant.CHECK_HEARTBEAT_API;


public interface CheckHeartbeatApiRequest {

    @GET(CHECK_HEARTBEAT_API)
    Call<String> checkHeartbeat();
}