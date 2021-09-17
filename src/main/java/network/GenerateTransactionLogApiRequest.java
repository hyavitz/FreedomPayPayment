package network;

import data.Constant;
import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

import static data.Constant.GENERATE_LOG_API;

public interface GenerateTransactionLogApiRequest {

    @POST(GENERATE_LOG_API)
    Call<String> generateLog(@Query("from") String from, @Query("position") String position, @Query("where") String where,
                             @Query("exact") String exact, @Query("token") String token, @Query("location_id") String location_id,
                             @Query("device_type") String deviceType, @Query("device_id") String deviceId,
                             @Query("application") String application, @Query("app_version") String applicationVersion);
}