package freedompay.network;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This utility class provides a HttpUrlConnection to be used
 * by the FreedomPayDataStreamManager class, called by the
 * FreedomPayPaymentDevice class.
 *
 * @author Hunter Yavitz - 8/10/21 - Revision
 */
public class FreedomPayConnectionHelper {

    private FreedomPayConnectionHelper(){}

    // Return FPConnection instance
    public static HttpURLConnection getFreedomPayConnectionInstance() throws IOException {
        URL fpUrl = new URL("http://127.0.0.1:1011/"); // TODO: Source from config

        // Instantiate connection object
        HttpURLConnection freedomPayConnection = (HttpURLConnection) fpUrl.openConnection();
        freedomPayConnection.setRequestMethod("GET");
        freedomPayConnection.setRequestProperty("accept", "application/xml");
        freedomPayConnection.setRequestProperty("Content-Type", "application/xml");
        freedomPayConnection.setDoOutput(true);
        System.out.println("Connection: " + freedomPayConnection);
        return freedomPayConnection;
    }
}