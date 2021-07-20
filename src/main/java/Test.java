import config.Device;
import config.RegisterDeviceApiController;
import freedompay.FreedomPayPaymentDevice;
import interceptor.*;
import lombok.SneakyThrows;
import network.Token;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import payment.IPaymentDevice;
import pax.PaxS300PaymentDevice;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import worldpay.WorldPayPaymentDevice;
import java.io.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Test {

//    private static final ExecutorService threadpool = Executors.newCachedThreadPool();
//    private static final Future<String> futureToken = threadpool.submit(Token::getToken);
//    private static String locationId, token, deviceType, deviceId, application, applicationVersion;

    private static NetRegisterDevice netRegisterDevice;

    //
    public static void main(String[] args) {

//        while (!futureToken.isDone()) {
//        }
//        try {
//            token = futureToken.get();
//        } catch (InterruptedException | ExecutionException e) {
//            e.printStackTrace();
//        }
//        System.out.println("Token>> " + token);
//        getDeviceInfo();

//        if (Device.registerDevice() != null) {
//            try {
//                startInterceptorClient();
//            } catch (IOException e) {
//                // TODO: Alert POS Device Register Failed - Tier 2
//                e.printStackTrace();
//            }
//
//        }

        String token = Token.getToken();


        NetRegisterDevice netRegisterDevice = Device.registerDevice(token);



        IPaymentDevice paymentDevice = new PaxS300PaymentDevice();
        Client.CommandListener listener = new CommandListener(paymentDevice);
        InterceptorClient ic = InterceptorClient.getInstance().addCommandHandler(listener);
        System.out.println("IC " + ic);
        System.out.println("TEST.REGISTER" + netRegisterDevice);
        ic.register = netRegisterDevice;
        ic.start();


//                paymentDevice = new PaxS300PaymentDevice();
//                listener = new CommandListener(paymentDevice);
//                ic = InterceptorClient.getInstance().addCommandHandler(listener);
//                ic.register = netRegisterDevice;
//                ic.start();



    }
}

//    //
//    private static void getDeviceInfo() {
//
//        // TODO: Source from config.json
//
////        try {
////            while (!futureTask.isDone()) {
////                System.out.print(".");
////            }
////            token = futureTask.get();
////            System.out.println("Token.get is:" + token);
////        } catch (InterruptedException | ExecutionException e) {
////            e.printStackTrace();
////        } finally {
////            threadpool.shutdown();
////        }
//
////        locationId = "10009";
////        deviceType = "PAX";
////        deviceId = "53096522";
////        application = "QuickPoint";
////        applicationVersion = "024";
////        System.out.println("Got device info: " + locationId + " " + deviceType + " " + deviceId + " " + application + " " + applicationVersion);
//
//        // TODO: For some reason, Token.getToken.onResponse happens now...
//    }
//
//    //
//    public static void registerDevice() {
//
//        // TODO: Investigate reducing this API payload
//        Call<String> deviceRegisterCall = RegisterDeviceApiController.getRegisterDeviceApiCall(locationId, token, deviceType, deviceId, application, applicationVersion);
//
//        //
//        deviceRegisterCall.enqueue(new Callback<>() {
//
//            //
//            @SneakyThrows
//            @Override
//            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
//
//                //
//                if (response.isSuccessful()) {
//
//                    //
//                    assert response.body() != null;
//                    JSONObject jsonObject = new JSONObject(response.body());
//                    if (jsonObject.optString("ResponseMessage", "").equalsIgnoreCase("Log In Successful.")) {
//                        netRegisterDevice = new NetRegisterDevice()
//                                .setLocationId(locationId)
//                                .setDevice(deviceType)
//                                .setSerialNumber(deviceId)
//                                .setApplication(application)
//                                .setVersion(Integer.parseInt(applicationVersion));
//
//                        //
//                        startInterceptorClient();
//
//                    //
//                    } else {
//                        System.out.println("Device failed to register.");
//                    }
//                }
//            }
//
//            //
//            @Override
//            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
//                System.out.println("Response failed to register device.");
//            }
//        });
//    }
//
//    //
//    public static void startInterceptorClient() throws IOException {
//
//
//        //
//        switch (deviceType) {
//
//            //
//            case "FreedomPay":
//                paymentDevice = new FreedomPayPaymentDevice();
//                listener = new CommandListener(paymentDevice);
//                ic = InterceptorClient.getInstance().addCommandHandler(listener);
//                ic.register = netRegisterDevice;
//                ic.start();
//                break;
//            //
//            case "WorldPay":
//
//            //
//            case "Exadigm":
//                System.out.println("Starting device");
//                paymentDevice = new WorldPayPaymentDevice();
//                listener = new CommandListener(paymentDevice);
//                ic = InterceptorClient.getInstance().addCommandHandler(listener);
//                ic.register = netRegisterDevice;
//                ic.start();
//                break;
//
//            //
//            case "PAX":
//                paymentDevice = new PaxS300PaymentDevice();
//                listener = new CommandListener(paymentDevice);
//                ic = InterceptorClient.getInstance().addCommandHandler(listener);
//                ic.register = netRegisterDevice;
//                ic.start();
//                break;
//
//            //
//            default:
//        }
//    }
//}

//        static Gson gson = new Gson();
//        String code = CustomerCode.generateCustomerCode("Fred", "123 E Main St", "New York");
//        String[] customerData = {code, "Fred", "Jones", "123 E Main St", "New York", "NY", "98765"};
//        String[] transactionData = {"12345", "987654321", "123456789", "1111222233334444"};
//        Items items = new Items();
//        Item item1 = new Item("123", "Nice Product", "It's really neat!", "$20", "1", "20.00", ".80", "S");
//        Item item2 = new Item("234", "Cool Product", "It's really neat!", "$25", "1", "25.00", "1.20", "S");
//        Item item3 = new Item("345", "Fine Product", "It's really neat!", "$30", "1", "30.00", "1.60", "S");
//        items.setItems(new Item[] {item1, item2, item3});
//
//        try {
//            TransactionDao.saveTransactionDetail(customerData, transactionData, items);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        ArrayList<String> data = null;
//        try {
//            data = TransactionDao.loadTransactionDetail(code);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        System.out.println("Customer Code: " + data.get(0));
//        System.out.println("First Name: " + data.get(1));
//        System.out.println("Last Name: " + data.get(2));
//        System.out.println("Street: " + data.get(3));
//        System.out.println("City: " + data.get(4));
//        System.out.println("State: " + data.get(5));
//        System.out.println("Zip: " + data.get(6));
//        System.out.println("\n");
//        System.out.println("Invoice: " + data.get(7));
//        System.out.println("Request ID: " + data.get(8));
//        System.out.println("Merchant Reference Code: " + data.get(9));
//        System.out.println("Token: " + data.get(10));
//        System.out.println("\n");
//
//        Item[] itms = new Item[(data.size() - 10)];
//        Items itemz = new Items();
//
//        for (int i = 11; i < data.size(); i++) {
//            String itm = data.get(i);
//            itm = itm.substring(itm.indexOf("Item") + 4);
//            System.out.println(itm);
//            itms[i-10] = gson.fromJson(itm, Item.class);
//        }
//
//        itemz.setItems(itms);
//        System.out.println(itemz);