package config;

import interceptor.NetRegisterDevice;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.*;
import java.util.Optional;

public class Device {

    private static boolean isRegisterDevice;
    private static NetRegisterDevice netRegisterDevice;
    private static String locationId, deviceType, deviceId, application, applicationVersion, token;

    /**
     * Editable device configurations located in device_config.json file, to be stored locally.
     *
      * @return true if file read succeeds.
     */
    private static boolean configureDeviceFromJson() {

        StringBuilder data = new StringBuilder("");

        // TODO: Supply correct path
        File file = new File("C:\\Users\\Hunter\\IdeaProjects\\FreedomPayPayment\\src\\main\\java\\config\\device_config.json");

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                System.out.println(line);
                data.append(line);
            }

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        JSONObject jsonObject = new JSONObject(data.toString());

        if (jsonObject != null) {

            locationId = jsonObject.optString("location_id", null);
            deviceType = jsonObject.optString("device_type", null);
            deviceId = jsonObject.optString("device_id", null);
            application = jsonObject.optString("application", null);
            applicationVersion = jsonObject.optString("app_version", null);

            System.out.println(jsonObject);

        return true;

        }

        // TODO: Send to server

        /**
         * API Call to register device
         *
         * dp_verfiy_location
         *
         */




//        if (jsonObject.optString("ResponseMessage", "").equalsIgnoreCase("Log In Successful.")) {
//            netRegisterDevice = new NetRegisterDevice()
//                    .setLocationId(locationId)
//                    .setDevice(deviceType)
//                    .setSerialNumber(deviceId)
//                    .setApplication(application)
//                    .setVersion(Integer.parseInt(applicationVersion));
//            return true;
//        }


        return false;
    }


        public static NetRegisterDevice registerDevice(String token) {
            System.out.println("WE ARE REGISTERING DEVICE, OR TRYING TO");

            if (!configureDeviceFromJson()) {
                System.out.println("Is configureFromDevice null?");
                return null;
            }

            System.out.println("DEVICE.java");
            System.out.println(locationId);
            System.out.println(token);
            System.out.println(deviceType);
            System.out.println(deviceId);
            System.out.println(application);
            System.out.println(applicationVersion);

            // TODO: Investigate reducing this API payload
            Call<String> registerDeviceCall = RegisterDeviceApiController.getRegisterDeviceApiCall(locationId, token, deviceType, deviceId, application, applicationVersion);

            registerDeviceCall.enqueue(new Callback<>() {

                @SneakyThrows
                @Override
                public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {

                    assert response.body() != null;

                    JSONObject jsonObject = new JSONObject(response.body());
                    System.out.println("DEVICE.JsonObject" + jsonObject);

                    // TODO: THis is right
                    if (jsonObject.optString("ResponseMessage", "").equalsIgnoreCase("Log In Successful.")) {
                        System.out.println("LOGIN SUCCEED");

                        netRegisterDevice = new NetRegisterDevice()
                                .setLocationId(locationId)
                                .setDevice(deviceType)
                                .setSerialNumber(deviceId)
                                .setApplication(application)
                                .setVersion(Integer.parseInt(applicationVersion));

                        System.out.println(netRegisterDevice + "<<<<<<<<<<<<<<<");
                        System.out.println();

                        isRegisterDevice = response.isSuccessful();


                    }
                }

                @Override
                public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                    isRegisterDevice = false;
                }
            });

            // TODO: Wait until not null
            System.out.print("W");
            while (netRegisterDevice == null) {
                System.out.print("");

            }

            System.out.println("DEVICE.REGISTER" + netRegisterDevice);
            return netRegisterDevice;
        }
    }
