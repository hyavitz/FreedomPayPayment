package freedompay.filestore;

public class CustomerCode {

    public static String generateCustomerCode(String firstname, String street1, String city) {
        return String.valueOf((firstname + street1 + city).hashCode()).replace("-", "");
    }
}