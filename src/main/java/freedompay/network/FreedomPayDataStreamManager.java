package freedompay.network;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

public class FreedomPayDataStreamManager {

    /**

        This class opens a data stream using an HTTP URL connection
        which sends a byte array and accepts a string response.  It
        manages its own connection internally.

        This is called by the FPPaymentDevice class.

        @author Hunter Yavitz - 3/10/21

     */

    // Declare string builder for response
    private final StringBuilder responseStringBuilder = new StringBuilder();

    // Return FPDataStream instance
    public static FreedomPayDataStreamManager getFPDataStreamInstance() { return new FreedomPayDataStreamManager(); }

    // Private constructor
    private FreedomPayDataStreamManager() {}

    // Open data stream
    public String openStream(HttpURLConnection connection, byte[] dataByteArray) throws IOException {

        // Send byte array as request
        try {
            DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
            writer.write(dataByteArray);
            writer.flush();

            // Accept string as response
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    responseStringBuilder.append(line);
                    responseStringBuilder.append(System.lineSeparator());
                }

            } catch (IOException e) {

                // Maybe there's no connection
                e.printStackTrace();
            }

        } finally {

            // Always close connection
            connection.disconnect();
        }

        // Return string as response
        return responseStringBuilder.toString();
    }
}