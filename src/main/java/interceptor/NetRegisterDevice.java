package interceptor;

import com.google.gson.annotations.SerializedName;
//import com.softpointdev.commonpoint.device.DeviceType;

import lombok.Data;

/**
 * Basic message from the client to register a device with the QuickPoint server.
 *
 * @author Joshua Monson - 11/25/2019
 */
@Data
public class NetRegisterDevice {

    /**
     * The location this device is currently running on according to the devices configuration
     */
    @SerializedName("location_id") private String locationId;

    /**
     * The detected or set device type
     */
    @SerializedName("device") private /*DeviceType*/ String device;

    /**
     * The Serial Number of the device
     */
    @SerializedName("serial_number") private String serialNumber;

    /**
     * The application that is being run
     */
    @SerializedName("application") private String application;

    /**
     * The version name (x.x.x) of the application running
     */
    @SerializedName("version_name") private String versionName;

    /**
     * THe integer version code of the application running
     */
    @SerializedName("version") private int version;

    /**
     * The server version that we are expecting to be able to use
     */
    @SerializedName("server_version") private long serverVersion;
}