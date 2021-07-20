package payment;

import interceptor.AlertDialogStyle;

import javax.swing.*;

public class PaymentUtil {

    static String decision;
    static String message;
    static String token;

    public static void observeData(PaymentDetails details) {

        decision = details.getDecision();
        message = details.getMessage();
        token = details.getToken();
    }

    public static String getToken() {
        return token;
    }

    public static boolean buildAlertDialog(String alert, String message, AlertDialogStyle alertStyle, String optional) {

        JFrame jf = new JFrame();
        jf.setAlwaysOnTop(true);
        Object[] options = {"YES", "NO"};
        String opt = optional;
        int prompt = -1;
        boolean choice = false;
        boolean approved = false;

        switch (alertStyle.ordinal()) {
            case 0:
                //Signature
                prompt = JOptionPane.showOptionDialog(jf, message, alert, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,null,null, options);
                approved = (prompt == 0);
                choice = approved;
                System.out.println("Signature approved: " + approved);
                break;
            case 1:
                //Cash Back
                prompt = JOptionPane.showOptionDialog(jf, message, alert, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,null,null, options);
                choice = (prompt == 0);
                System.out.println("Cashback: " + choice);
                if (choice) {
                    String cashBack = JOptionPane.showInputDialog(jf, "How much cash back?", "Amount");
                    if (Integer.parseInt(cashBack) > 0 && Integer.parseInt(cashBack) < 999) {
                        approved = true;
                    } else {
                        approved = false;
                    }
                }
                System.out.println("Cash back approved: " + approved);
                break;
            case 2:
                //Call Bank
                prompt = JOptionPane.showOptionDialog(jf, message, alert, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, options);
                choice = (prompt == 0);
                System.out.println("Call bank?: " + choice);
                if (choice) {
                    String bankAuthNumber = JOptionPane.showInputDialog(jf, "Call 1-888-555-5555", "Phone Auth");
                    if (bankAuthNumber.equals(opt)) {
                        approved = true;
                    } else {
                        approved = false;
                    }
                    System.out.println("Bank auth approved: " + approved);
                }
                break;
        }

       return choice;
    }
}
