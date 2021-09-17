import config.Device;
import interceptor.NetRegisterDevice;
import network.Heartbeat;
import network.Token;
import payment.IPaymentDevice;
import payment.PaymentGUI;

import java.io.IOException;

import static freedompay.FreedomPayPaymentDevice.getFreedomPayPaymentDeviceInstance;

public class Test {

    public static NetRegisterDevice netRegisterDevice;

    public static void main(String[] args) {

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

        PaymentGUI.getPaymentGUIInstance();


//        Client.CommandListener listener = new CommandListener(paymentDevice);
//        InterceptorClient ic = InterceptorClient.getInstance().addCommandHandler(listener);
//        InterceptorClient.register = netRegisterDevice;
//        ic.start();
    }
}