package payment;

import freedompay.FreedomPayPaymentDevice;
import freedompay.pojo.Items;
import worldpay.WorldPayPaymentDevice;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

public class PaymentGUI {

    // Singleton
    private static PaymentGUI paymentGUI;
    private static PaymentDetails paymentDetails;
    private static PaymentUtil paymentUtil;

    //
    IPaymentDevice paymentDevice;

    // Frame
    public JFrame paymentGUIJFrame;

    // Dimensions
    private int width;
    private int height;

    //
    private int frameWidth;
    private int frameHeight;

    //
    private int x1;
    private int x2;
    private int x3;
    private int x4;

    //
    private int y1;
    private int y2;
    private int y3;
    private int y4;
    private int y5;
    private int y6;
    private int y7;
    private int y8;

    // Device Type
    private JLabel deviceTypeJLabel;
    private JComboBox deviceTypeJComboBox;

    // Device Type Values
    private String[] deviceTypeArray;
    private String deviceType;

    // Transaction Amount
    private JLabel transactionAmountJLabel;
    private JTextField transactionAmountJTextField;

    // Transaction Tax
    private JLabel transactionTaxJLabel;
    private JTextField transactionTaxJTextField;

    // Transaction Tip
    private JLabel transactionTipJLabel;
    private JTextField transactionTipJTextField;

    // Transaction Values
    private int transactionAmount;
    private int transactionTax;
    private int transactionTip;

    // Transaction Type
    private JLabel transactionTypeJLabel;
    private JComboBox transactionTypeJComboBox;

    // Transaction Type Values
    private String[] transactionTypeArray;
    private String transactionType;

    //
    private String[] itemTypeArray;

    // Location ID
    private JLabel locationIdJLabel;
    private JTextField locationIdJTextField;

    // Location ID Value
    private String locationId;

    // Terminal ID
    private JLabel terminalIdJLabel;
    private JTextField terminalIdJTextField;

    // Terminal ID Value
    private String terminalId;

    // Submit Payment
    private JButton submitPaymentJButton;

    // Decision
    private JLabel decisionJLabel;
    private JTextField decisionJTextField;

    // Decision Value
    private String decision;

    // Message
    private JLabel messageJLabel;
    private JTextField messageJTextField;

    // Message Value
    private String message;

    // Token
    private JLabel tokenJLabel;
    private JTextField tokenJTextField;

    // Token Value
    private String token;

    //
    private JLabel itemJLabel;
    private JComboBox itemJComboBox;

    //
    private JButton addItemJButton;

    //
    private String[] cart;
    private Items items;

    //
    private int doohickeyCount = 0;
    private int thingamabob = 0;
    private int whatchamacallit = 0;

    //
    private JLabel cartJLabel;
    private JTextArea cartJTextArea;

    //
    private DecimalFormat df = new DecimalFormat("0.#");

    // Get Singleton Instance
    public static PaymentGUI getPaymentGUIInstance() {
        if (paymentGUI != null) { return paymentGUI; }
        return new PaymentGUI();
    }

    // Singleton Constructor
    private PaymentGUI() {

        // Config Values
        locationId = "1496617013";
        terminalId = "2510855011";

        // Dimensions
        width = 200;
        height = 30;

        //
        frameWidth = 680;
        frameHeight = 400;

        //
        x1 = 10;
        x2 = 220;
        x3 = 430;
        x4 = 640;

        //
        y1 = 10;
        y2 = 40;
        y3 = 100;
        y4 = 130;
        y5 = 190;
        y6 = 220;
        y7 = 280;
        y8 = 310;

        // Frame
        paymentGUIJFrame = new JFrame("Payment Application");

        // Device Type
        deviceTypeArray = new String[]{"WorldPay", "FreedomPay", "PAX"};

        //
        deviceTypeJLabel = new JLabel("Device Type");
        deviceTypeJLabel.setBounds(x1, y1, width, height);

        //
        deviceTypeJComboBox = new JComboBox(deviceTypeArray);
        deviceTypeJComboBox.setBounds(x1, y2, width, height);

        // Transaction Amount
        transactionAmountJLabel = new JLabel("Amount");
        transactionAmountJLabel.setBounds(x2, y1, (width / 3) - 3, height);

        //
        transactionAmountJTextField = new JTextField();
        transactionAmountJTextField.setBounds(x2, y2, (width / 3) - 3, height);
        transactionAmountJTextField.setText("0");
        //transactionAmountJTextField.setEnabled(false);

        // Transaction Tax
        transactionTaxJLabel = new JLabel("Tax");
        transactionTaxJLabel.setBounds(x2 + (width / 3), y1, (width / 3) - 3, height);

        //
        transactionTaxJTextField = new JTextField();
        transactionTaxJTextField.setBounds(x2 + (width / 3), y2, (width / 3) - 3, height);
        transactionTaxJTextField.setText("0");
        //transactionTaxJTextField.setEnabled(false);

        // Transaction Tip
        transactionTipJLabel = new JLabel("Tip");
        transactionTipJLabel.setBounds(x2 + ((width / 3) * 2), y1, (width / 3) - 3, height);

        //
        transactionTipJTextField = new JTextField();
        transactionTipJTextField.setBounds(x2 + ((width / 3) * 2), y2, (width / 3) - 3, height);
        transactionTipJTextField.setText("0");

        // Transaction Type
        transactionTypeArray = new String[]{"Sale", "Refund", "Void", "Cancel", "Token"};

        //
        transactionTypeJLabel = new JLabel("Transaction Type");
        transactionTypeJLabel.setBounds(x3, y1, width, height);

        //
        transactionTypeJComboBox = new JComboBox(transactionTypeArray);
        transactionTypeJComboBox.setBounds(x3, y2, width, height);

        //
        itemTypeArray = new String[]{"1 Whatchma-callit $10", "1 Thing-A-Ma-Bob $15", "1 Whatz-It $20"};
        itemJLabel = new JLabel("Items");
        itemJLabel.setBounds(x1, y3, width, height);

        //
        itemJComboBox = new JComboBox(itemTypeArray);
        itemJComboBox.setBounds(x1, y4, width, height);

        // Location ID
        locationIdJLabel = new JLabel("Location ID");
        locationIdJLabel.setBounds(x1, y5, width, height);

        locationIdJTextField = new JTextField();
        locationIdJTextField.setBounds(x1, y6, width, height);
        locationIdJTextField.setText(locationId);
        locationIdJTextField.setEnabled(false);

        // Terminal ID
        terminalIdJLabel = new JLabel("Terminal ID");
        terminalIdJLabel.setBounds(x2, y5, width, height);

        terminalIdJTextField = new JTextField();
        terminalIdJTextField.setBounds(x2, y6, width, height);
        terminalIdJTextField.setText(terminalId);
        terminalIdJTextField.setEnabled(false);

        // Submit Payment
        submitPaymentJButton = new JButton("Pay");
        submitPaymentJButton.setBounds(x3, y6, width, height);
        submitPaymentJButton.setEnabled(false);
        submitPaymentJButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Submit payment
                try {
                    submitPayment();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
        });

        //
        addItemJButton = new JButton("Add to Cart");
        addItemJButton.setBounds(x2, y4, width, height);
        addItemJButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addToCart();
            }
        });

        // Decision
        decisionJLabel = new JLabel("Decision");
        decisionJLabel.setBounds(x1, (y7), width, height);

        //
        decisionJTextField = new JTextField();
        decisionJTextField.setBounds(x1, (y8), width, height);
        decisionJTextField.setEditable(false);

        // Message
        messageJLabel = new JLabel("Message");
        messageJLabel.setBounds(x2, (y7), width, height);

        //
        messageJTextField = new JTextField();
        messageJTextField.setBounds(x2, (y8), width, height);
        messageJTextField.setEditable(false);

        // Token
        tokenJLabel = new JLabel("Token");
        tokenJLabel.setBounds(x3, (y7), width, height);

        //
        tokenJTextField = new JTextField();
        tokenJTextField.setBounds(x3, (y8), width, height);
        tokenJTextField.setEditable(false);

        //
        cartJLabel = new JLabel("Cart");
        cartJLabel.setBounds(x3, y3, width, height);

        //
        cartJTextArea = new JTextArea();
        cartJTextArea.setBounds(x3, y4, width, (height * 2));

        // Device Type
        paymentGUIJFrame.add(deviceTypeJLabel);
        paymentGUIJFrame.add(deviceTypeJComboBox);

        // Transaction Amount
        paymentGUIJFrame.add(transactionAmountJLabel);
        paymentGUIJFrame.add(transactionAmountJTextField);

        // Transaction Tax
        paymentGUIJFrame.add(transactionTaxJLabel);
        paymentGUIJFrame.add(transactionTaxJTextField);

        // Transaction Tip
        paymentGUIJFrame.add(transactionTipJLabel);
        paymentGUIJFrame.add(transactionTipJTextField);

        // Transaction Type
        paymentGUIJFrame.add(transactionTypeJLabel);
        paymentGUIJFrame.add(transactionTypeJComboBox);

        //
        paymentGUIJFrame.add(itemJLabel);
        paymentGUIJFrame.add(itemJComboBox);

        // Location
        paymentGUIJFrame.add(locationIdJLabel);
        paymentGUIJFrame.add(locationIdJTextField);

        // Terminal ID
        paymentGUIJFrame.add(terminalIdJLabel);
        paymentGUIJFrame.add(terminalIdJTextField);

        // Submit
        paymentGUIJFrame.add(submitPaymentJButton);

        //
        paymentGUIJFrame.add(addItemJButton);

        // Decision
        paymentGUIJFrame.add(decisionJLabel);
        paymentGUIJFrame.add(decisionJTextField);

        // Message
        paymentGUIJFrame.add(messageJLabel);
        paymentGUIJFrame.add(messageJTextField);

        // Token
        paymentGUIJFrame.add(tokenJLabel);
        paymentGUIJFrame.add(tokenJTextField);

        //
        paymentGUIJFrame.add(cartJLabel);
        paymentGUIJFrame.add(cartJTextArea);

        // Frame Config
        paymentGUIJFrame.setLayout(null);
        paymentGUIJFrame.setSize(frameWidth, frameHeight);
        paymentGUIJFrame.setLocationRelativeTo(null);
        paymentGUIJFrame.setVisible(true);
    }

    public void setDecisionField(String detailsDecision) {
        decisionJTextField.setText(detailsDecision);
    }

    public void setMessageField(String detailsMessage) {
        messageJTextField.setText(detailsMessage);
    }

    public void setTokenField(String detailsToken) { tokenJTextField.setText(detailsToken); }

    public static void main(String[] args) {
        new PaymentGUI();
    }

    private void addToCart() {

        cartJTextArea.setText(cartJTextArea.getText() + itemJComboBox.getItemAt(itemJComboBox.getSelectedIndex()) + "\n");
        String amount = itemJComboBox.getItemAt(itemJComboBox.getSelectedIndex()).toString().substring(itemJComboBox.getItemAt(itemJComboBox.getSelectedIndex()).toString().indexOf("$")+1);
        System.out.println(amount);

        updateAmount(Double.parseDouble(transactionAmountJTextField.getText().toString())
                + Double.parseDouble(itemJComboBox.getItemAt(itemJComboBox.getSelectedIndex()).toString().substring(itemJComboBox.getItemAt(itemJComboBox.getSelectedIndex()).toString().indexOf("$") + 1)));

        activatePay();
    }

    private void updateAmount(double amount) {
        transactionAmountJTextField.setText(String.valueOf(amount));
        transactionTaxJTextField.setText(String.valueOf(amount * .08));
    }

    private void activatePay() {
        if (transactionAmountJTextField.getText() != null) {
            submitPaymentJButton.setEnabled(true);
        }
    }

    private void buildItems() {

        int cartSize = cartJTextArea.getText().split("\n").length;
        int i = 0;
        cart = new String[cartSize];
        cart = cartJTextArea.getText().split("\n");

        try (FileWriter fileWriter = new FileWriter("items.txt")) {
            for (String item : cart) {
                fileWriter.write(item + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void buildDetails() {

        //
        decisionJTextField.setText("");
        messageJTextField.setText("");
        deviceType = deviceTypeJComboBox.getItemAt(deviceTypeJComboBox.getSelectedIndex()).toString();
        transactionAmount = Integer.parseInt(df.format((Double.parseDouble(transactionAmountJTextField.getText()) * 100)));
        transactionTax = Integer.parseInt(df.format((Double.parseDouble(transactionTaxJTextField.getText())) * 100));
        transactionTip = Integer.parseInt(df.format((Double.parseDouble(transactionTipJTextField.getText())) * 100));
        transactionType = transactionTypeJComboBox.getItemAt(transactionTypeJComboBox.getSelectedIndex()).toString();
        locationId = locationIdJTextField.getText();
        terminalId = terminalIdJTextField.getText();
        token = tokenJTextField.getText();
        System.out.println("Submitting payment..");
    }

    private void buildDevice() throws IOException {
        switch (deviceType) {
            case "WorldPay":
                paymentDevice = new WorldPayPaymentDevice();
                break;
            case "FreedomPay":
                paymentDevice = new FreedomPayPaymentDevice();
                break;
            case "PAX":
                paymentDevice = new pax.PaxS300PaymentDevice();
                break;
        }
    }

    private void submitPayment() throws IOException, InterruptedException {

        //
        buildItems();

        //
        buildDetails();

        //
        buildDevice();

        //
        processPayment(paymentDevice);
    }

    private void processPayment(IPaymentDevice paymentDevice) throws IOException, InterruptedException {

        switch (transactionType) {
            case "Sale":
                paymentDevice.makePayment(transactionAmount, transactionTip, transactionTax);
//                new Thread(()->startTimer()).start();
                break;
            case "Refund":
                paymentDevice.refundPayment(transactionAmount);
                break;
            case "Void":
                paymentDevice.voidPayment();
                break;
            case "Cancel":
                paymentDevice.cancelPayment();
                break;
            case "Token":
                paymentDevice.createToken();
                break;
        }
    }

//    private void startTimer() {
//        long timer = System.currentTimeMillis();
//        while ((System.currentTimeMillis() - timer) < 2000) {
//        }
//        transactionType = "Cancel";
//        try {
//            System.out.println("CANCELing");
//            processPayment(paymentDevice);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
}