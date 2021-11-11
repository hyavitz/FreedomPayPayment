package config;

import interceptor.NetRegisterDevice;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Device {

    public static NetRegisterDevice netRegisterDevice;
    private static String locationId, deviceId, deviceType, application, applicationVersion;

    /**
     * Editable device configurations located in device_config.json file, to be stored locally.
     *
     * @return true if file read succeeds.
     */
    private static boolean configureDeviceFromJson() {

        System.out.println("<3.2><>configureDevice");
        StringBuilder data = new StringBuilder("");

        // TODO: Supply correct path
        File file = new File("C:\\Users\\Hunter\\Documents\\GitHub\\FreedomPayPayment\\src\\main\\java\\config\\device_config.json");

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                System.out.println(">>>" + line + "<<<");
                data.append(line);
            }

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        JSONObject jsonObject = new JSONObject(data.toString());
        System.out.println("Device.configureDevice() : " + jsonObject);

        locationId = jsonObject.optString("location_id", null);
        deviceId = jsonObject.optString("device_id", null);
        deviceType = jsonObject.optString("device_type", null);
        application = jsonObject.optString("application", null);
        applicationVersion = jsonObject.optString("app_version", null);

        assert locationId != null;
        assert deviceId != null;
        assert deviceType != null;
        assert application != null;
        assert applicationVersion != null;

        return true;
    }

    public static void registerDevice(String token) {

        System.out.println("<3.1><>registerDevice with token[" + token + "]");

        if (token != null) {

            if (configureDeviceFromJson()) {

                System.out.println("<3.3><>configureDeviceFromJson: true");

                Call<String> registerDeviceCall = RegisterDeviceApiController.getRegisterDeviceApiCall(token, locationId, deviceId, deviceType, application, applicationVersion);

                registerDeviceCall.enqueue(new Callback<>() {

                    @SneakyThrows
                    @Override
                    public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {

                        System.out.println("<3.4><>registerDeviceCall.onResponse");
                        assert response.body() != null;

                        JSONObject jsonObject = new JSONObject(response.body());

                        if (jsonObject.optString("ResponseCode", "").equalsIgnoreCase("1")) {

                            netRegisterDevice = new NetRegisterDevice()
                                    .setLocationId(locationId)
                                    .setSerialNumber(deviceId)
                                    .setDevice(deviceType)
                                    .setApplication(application)
                                    .setVersionName(applicationVersion)
                                    .setVersion(Integer.parseInt(applicationVersion))
                                    .setServerVersion(1l); // TODO: Source value
                        }

                        System.out.println("<3.6><>returning netRegisterDevice as [" + netRegisterDevice + "]");
                        System.out.println("Register Device finished?");
                    }

                    @Override
                    public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                        System.out.println("<3.5><>registerDeviceCall.onFailure");
                    }
                });
            }
        }
    }
}