package pax.network;

import com.pax.poslink.CommSetting;

/**
 * This class builds a CommSetting object for PosLink.  It is called by PaxS300PaymentDevice to apply
 * communication settings needed to connect to POS workstation.
 *
 * @author Hunter Yavitz 7/2/21
 */
public class PaxS300CommConfig {

    // TODO: Source this dynamically from a config.json
    public static CommSetting getCommSetting() {

        CommSetting commSetting = new CommSetting();
        commSetting.setType(CommSetting.TCP);
        commSetting.setTimeOut("-1");
        commSetting.setDestIP("192.168.1.6");
        commSetting.setDestPort("10008");

        return commSetting;
    }
}