import config.Device;
import interceptor.Client;
import interceptor.CommandListener;
import interceptor.InterceptorClient;
import interceptor.NetRegisterDevice;
import io.FileWatcher;
import network.Heartbeat;
import network.Token;
import pax.PAXS300PaymentDevice;
import payment.IPaymentDevice;
import payment.PaymentGUI;

import java.io.FileWriter;
import java.io.IOException;

import static freedompay.FreedomPayPaymentDevice.getFreedomPayPaymentDeviceInstance;

public class Test {

    public static NetRegisterDevice netRegisterDevice = new NetRegisterDevice();

    public static void main(String[] args) throws IOException, InterruptedException {

        Heartbeat.checkHeartbeat();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            // TODO: Alert POS - Heartbeat Failure - (1)
        }
        System.out.println("<1><>HEARTBEAT: " + Heartbeat.hasNetworkConnection);

        Token.generateToken();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            // TODO: Alert POS - Token Generate Failure - (1)
        }
        System.out.println("<2><>TOKEN: " + Token.token);


        Device.registerDevice(Token.token);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            // TODO: Alert POS - Device Register Failure - (1)
        }
        netRegisterDevice = Device.netRegisterDevice;
        System.out.println("<3><>NET REGISTER DEVICE: " + netRegisterDevice);

        assert Heartbeat.hasNetworkConnection && Token.token != null && netRegisterDevice != null;

//        PaymentGUI.getPaymentGUIInstance();

//        FileWatcher fileWatcher = new FileWatcher();
//        fileWatcher.setWatchService();

        IPaymentDevice paymentDevice = new PAXS300PaymentDevice();
        Client.CommandListener listener = new CommandListener(paymentDevice);
        InterceptorClient ic = InterceptorClient.getInstance().addCommandHandler(listener);
        InterceptorClient.netRegisterDevice = netRegisterDevice;
        ic.start();
    }
}