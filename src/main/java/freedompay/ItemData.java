package freedompay;

import freedompay.pojo.Item;
import freedompay.pojo.Items;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ItemData {

    private static BufferedReader bufferedReader;
    private static String data;

    private static String[] itemsPresent;

    private static Items items;
    private static Item[] itemz;

    public static Items getItemData() throws IOException {

        int i = 0;

        File file = new File("items.txt");

        try {
            int itemArraySize = 0;
            data = "";

            bufferedReader = new BufferedReader(new FileReader(file));
            while ((data = bufferedReader.readLine()) != null) {
                itemArraySize++;
            }

            itemz = new Item[itemArraySize];

            int idx = -1;
            data = "";

            bufferedReader = new BufferedReader(new FileReader(file));

            while ((data = bufferedReader.readLine()) != null) {

                String[] datum = data.split(" ");

                Item item = new Item();
                item.setQuantity(datum[0]);
                item.setProductName(datum[1]);
                item.setUnitPrice(datum[2].replace("$", ""));
                item.setProductCode(String.valueOf(datum[1].hashCode()));
                item.setProductDescription("A really great " + datum[1]);
                item.setTotalAmount(String.valueOf(Integer.parseInt(item.getUnitPrice()) * Integer.parseInt(item.getQuantity())));
                item.setTaxAmount(String.valueOf(Integer.parseInt((item.getTotalAmount())) * 0.08D));
                item.setSaleCode("S");
                itemz[++idx] = item;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            bufferedReader.close();
        }

        items = new Items();
        items.setItems(itemz);

        return items;
    }
}
