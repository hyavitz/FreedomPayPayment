package freedompay.network;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FreedomPayConnectionManager {

    private static final String fpUrl = "http://127.0.0.1:1011";

    public static String getConnection(byte[] dataByteArray) throws IOException {

        URL url = new URL(fpUrl);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.setRequestProperty("accept", "application/xml");
        httpURLConnection.setRequestProperty("Content-Type", "application/xml");
        httpURLConnection.setDoOutput(true);

        DataOutputStream writer = new DataOutputStream(httpURLConnection.getOutputStream());
        writer.write(dataByteArray);
        writer.flush();

        BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        System.out.println(response);
        return response.toString();
    }
}