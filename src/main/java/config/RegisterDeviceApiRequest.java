package config;

import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

import static data.Constant.REGISTER_DEVICE_API;

public interface RegisterDeviceApiRequest {

    @FormUrlEncoded
    @POST(REGISTER_DEVICE_API)
    Call<String> registerDevice(@FieldMap Map<String, String> options);

    @POST(REGISTER_DEVICE_API)
    Call<String> registerDevice(
            @Query("token") String token, @Query("location_id") String locationId,
            @Query("device_id") String deviceId, @Query("device_type") String deviceType,
            @Query("application") String application, @Query("app_version") String applicationVersion);
}