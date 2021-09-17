package freedompay.network;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

/**
 * This utility class provides a connection stream
 * using an HttpConnection supplied by FreedomPayConnectionHelper,
 * called by FreedomPayPaymentDevice.
 *
 * @author Hunter Yavitz - 8/10/21 - Revision
 */

public class FreedomPayDataStreamManager {

    private static FreedomPayDataStreamManager freedomPayDataStreamManager;

    public static FreedomPayDataStreamManager getFreedomPayDataStreamManagerInstance() {
        if (freedomPayDataStreamManager == null) {
            freedomPayDataStreamManager = new FreedomPayDataStreamManager();
        }
        return freedomPayDataStreamManager;
    }

    private FreedomPayDataStreamManager() {}

    int i = 0;
    public String openStream(HttpURLConnection connection, byte[] dataByteArray) throws IOException {
        StringBuilder responseStringBuilder = new StringBuilder();
        System.out.println("openStream: " + ++i);

        try  {
            DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
            writer.write(dataByteArray);
            writer.flush();

            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    responseStringBuilder.append(line);
                    responseStringBuilder.append(System.lineSeparator());
                }
            } catch (IOException e) {
                e.printStackTrace();
                // TODO: Alert POS - Connection Failure - (3)
            }

            } finally {
                connection.disconnect();
            }
        return responseStringBuilder.toString();
    }
}