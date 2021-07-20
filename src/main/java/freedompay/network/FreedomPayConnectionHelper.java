package freedompay.network;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class FreedomPayConnectionHelper {

    /**
        This class returns an instance of a connection object
        used by the FPPaymentDevice class.  The URL provided
        is the local loopback address pointing to the service
        running in the dev environment and should be changed
        to the IP address of the specific FCC server at
        implementation.

        This is called from the FPPaymentDevice class.

        @author Hunter Yavitz - 3/10/21
     */

    // Instantiate connection object
    public final HttpURLConnection fpConnection;

    // Return FPConnection instance
    public static FreedomPayConnectionHelper getFPConnectionInstance() throws IOException {
        return new FreedomPayConnectionHelper();
    }

    // Private constructor
    private FreedomPayConnectionHelper() throws IOException {

        // Modify to point to location-specific FCC service IP address
        URL FP_URL = new URL("http://127.0.0.1:1011/");

        // Assign values - should be constant
        this.fpConnection = (HttpURLConnection) FP_URL.openConnection();
        this.fpConnection.setRequestMethod("GET");
        this.fpConnection.setRequestProperty("accept", "application/xml");
        this.fpConnection.setRequestProperty("Content-Type", "application/xml");
        this.fpConnection.setDoOutput(true);
    }
}