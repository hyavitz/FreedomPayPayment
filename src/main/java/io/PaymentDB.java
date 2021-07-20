package io;

import java.io.*;

public class PaymentDB {

    public static boolean saveData(String[] data, String file_name) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file_name))) {
            for (int i = 0; i < data.length; i++) {
                writer.write(data[i] + "\n");
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean saveData(String data, String file_name) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file_name))) {
            writer.write(data);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String loadData(String file_name) {
        String data;
        try (BufferedReader reader = new BufferedReader(new FileReader(file_name))) {
            data = reader.readLine();
            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String[] loadData(String file_name, boolean multi) {
        int i = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file_name))) {
            while (reader.readLine() != null) {
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] data = new String[i];
        i = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file_name))) {
            while (reader.readLine() != null) {
                data[i++] = reader.readLine();
            }
            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}