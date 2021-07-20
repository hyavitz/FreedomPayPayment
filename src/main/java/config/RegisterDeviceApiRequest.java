package config;

import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

public interface RegisterDeviceApiRequest {

    //String DEVICE_REGISTER_DATAPOINT_VERSION_PATH = "DPS/v" + BuildConfig.DPS_API_VERSION + "/";
    String DEVICE_REGISTER_KIOSK_VERSION_PATH = "APIAPPS/KioskPoint/v" + BuildConfig.API_VERSION;
    String DEVICE_REGISTER_ENDPOINT = "/qp_verify_location_id.php";
    String DEVICE_REGISTER_API = /*DEVICE_REGISTER_DATAPOINT_VERSION_PATH +*/ DEVICE_REGISTER_KIOSK_VERSION_PATH + DEVICE_REGISTER_ENDPOINT;

   // @FormUrlEncoded
    @POST(DEVICE_REGISTER_API)
    Call<String> registerDevice(@Query("location_id") String locationId,
                                @Query("token") String token,
                                @Query("device_type") String deviceType,
                                @Query("device_id") String deviceId,
                                @Query("application") String application,
                                @Query("app_version") String applicationVersion);

//    @FormUrlEncoded
//    @POST(DEVICE_REGISTER_API)
//    Call<String> registerDevice(@FieldMap Map<String, String> options);
}