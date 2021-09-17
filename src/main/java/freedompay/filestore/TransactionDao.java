package freedompay.filestore;

import freedompay.pojo.Item;
import freedompay.pojo.Items;
import java.io.*;
import java.util.ArrayList;

public class TransactionDao {

    public static boolean saveTransactionDetail(String[] customerData, String[] transactionData, Items items) throws IOException {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(customerData[0] + ".txt"))) {
            writer.write(" " + customerData[0] + "\n"); // customer code
            writer.write(" " + customerData[1] + "\n"); // first name
            writer.write(" " + customerData[2] + "\n"); // last name
            writer.write(" " + customerData[3] + "\n"); // street
            writer.write(" " + customerData[4] + "\n"); // city
            writer.write(" " + customerData[5] + "\n"); // state
            writer.write(" " + customerData[6] + "\n"); // zip

            writer.write(" " + transactionData[0] + "\n"); // invoice
            writer.write(" " + transactionData[1] + "\n"); // request id
            writer.write(" " + transactionData[2] + "\n"); // merchant reference code
            writer.write(" " + transactionData[3] + "\n"); // token

//            for (Item item : items.items) {
//                writer.write(" " + item + "\n");
//            }

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static ArrayList<String> loadTransactionDetail(String customerCode) throws FileNotFoundException {

        ArrayList<String> transactionData = new ArrayList<>();
        String datum = null;

        try (BufferedReader reader = new BufferedReader(new FileReader(customerCode + ".txt"))) {
            while ((datum = reader.readLine()) != null) {
                transactionData.add(datum);
            }

            return transactionData;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}