package config;

import com.google.gson.*;
import lombok.Data;

import java.io.*;

public class DeviceConfig {

    /**
        This class constructs a device configuration object with member fields
        that are defined in a config.txt file, injected at construction.

        This is called by the PaymentDevice class and the InterceptorClient class
        but should be removed from the InterceptorClient and replaced with
        call from server.

        @author Hunter Yavitz - 3/10/21
     */

    public final String DEVICE;
    public final String SERIAL_NUMBER;
    public final String APPLICATION;
    public final int VERSION;
    public final String VERSION_NAME;
    public final long SERVER_VERSION;
    public final long STORE_ID;
    public final String LOCATION_ID;
    public final long TERMINAL_ID;
    public final int LANE_ID;
    public final String CLIENT_ENVIRONMENT;
    public final int INVOICE;
    public final String MERCHANT_ID;

    Config config;

    public DeviceConfig(String deviceConfigFilePath) {

        File file = new File(deviceConfigFilePath);

        try (Reader reader = new FileReader(file)) {

            Gson gson = new Gson();

            try {
                config = gson.fromJson(reader, Config.class);
            } catch (Exception e) {
                e.printStackTrace();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        this.LOCATION_ID = config.location_id;
        this.DEVICE = config.device;
        this.SERIAL_NUMBER = config.serial_number;
        this.APPLICATION = config.application;
        this.VERSION = Integer.parseInt(config.version);
        this.VERSION_NAME = config.version_name;
        this.SERVER_VERSION = Long.parseLong(config.server_version);
        this.STORE_ID = Long.parseLong(config.store_id);
        this.TERMINAL_ID = Long.parseLong(config.terminal_id);
        this.LANE_ID = Integer.parseInt(config.lane_id);
        this.CLIENT_ENVIRONMENT = config.client_environment;
        this.INVOICE = Integer.parseInt(config.invoice);
        this.MERCHANT_ID = config.merchant_id;
    }

    @Data
    static class Config {
        String location_id;
        String device;
        String serial_number;
        String application;
        String version;
        String version_name;
        String server_version;
        String store_id;
        String terminal_id;
        String lane_id;
        String client_environment;
        String invoice;
        String merchant_id;
    }
}