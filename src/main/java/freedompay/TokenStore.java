package freedompay;

import java.io.*;

public class TokenStore {

    public static boolean storeToken(String customerCode, String token) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(customerCode + ".txt"));
            writer.write(" " + token);
            System.out.println("Storing token from " + customerCode + ".txt");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getToken(String customerCode) {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(customerCode + ".txt"));
            System.out.println("Getting token from " + customerCode + ".txt");
            if (reader.read() != -1) {
                return reader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}