//package paxs300;
//
//import com.google.gson.Gson;
//import com.pax.poslink.*;
//import interceptor.AlertDialogStyle;
//import org.jetbrains.annotations.Nullable;
//import payment.IPaymentDevice;
//import payment.PaymentDetails;
//import payment.PaymentUtil;
//
//
//import java.awt.*;
//import java.io.IOException;
//import java.text.DecimalFormat;
//
//import static payment.PaymentUtil.observeData;
//
//public class S300PaymentDevice implements IPaymentDevice {
//
//    @Override
//    public void makePayment(int amount, int tip, int tax) throws IOException, InterruptedException {
//
//    }
//
//    @Override
//    public void cancelPayment() throws IOException, InterruptedException {
//        if (posLink == null) {
//            posLink = createPosLink();
//        }
//        if (posLink != null) {
//            posLink.CancelTrans();
//        }
//    }
//
//    @Override
//    public void voidPayment() throws IOException, InterruptedException {
//
//    }
//
//    @Override
//    public void refundPayment(int amount) throws IOException, InterruptedException {
//
//    }
//
//    @Override
//    public void createToken() throws IOException {
//
//    }
//
//    private void getPayRocInfo(String amount, String tip, int first6) {
//        approvedAmount = amount;
//        approveTip = tip;
//
//        getUniqueId();
//
//        if (applySurcharge) {
//            double surchargeOnPayment = 0D;
//            double surchargeOnTip = 0D;
//
//            double surchargePercent = Double.parseDouble(getSurchargePercent() / 10000);
//
//            double amountBeforeSurcharge = getAmount();
//            surchargeOnPayment = getValuePrecise(amountBeforeSurcharge * surchargePercent);
//
//            double tipAmount = 0D;
//            if (getTip() > 0) {
//                tipAmount = getTip();
//            }
//            surchargeOnTip = getValuePrecise(tipAmount * surchargePercent);
//
//            double surcharge = getValuePrecise(surchargeOnPayment + surchargeOnTip);
//            double total = amountBeforeSurcharge + surcharge;
//
//            paymentBeforeSurcharge = String.format("%.2f", amountBeforeSurcharge);
//            tipBeforeSurcharge = String.format("%.2f", tipAmount);
//            surchargeCardType = "Credit";
//            surchargeAmount = String.format("%.2f", surcharge);
//
//            showCcSurchargeAlert(amountBeforeSurcharge, surcharge, total, tip, surchargeOnTip);
//
//        } else {
//
//            double amountBeforeSurcharge = getAmount();
//            double total = amountBeforeSurcharge;
//            approvedAmount = String.format("%.2f", total);
//
//            doPostAuth(transactionId, total, (Double.parseDouble(tip) / 100));
//        }
//    }
//
//    private void showCcSurchargeAlert(double amount, double surchargeAmount, double totalAmount, double paymentTip, double surchargeTip) {
//
//        boolean surchargeApproved = PaymentUtil.buildAlertDialog("Surcharge Alert", "This transaction will carry a " + surchargeAmount + " surcharge.", AlertDialogStyle.SIGNATURE, "");
//
//        if (surchargeApproved) {
//
//            doPostAuth(transactionId, amount, paymentTip, surchargeAmount);
//
//        } else {
//
//            PaymentDetails paymentDetails = new PaymentDetails();
//            paymentDetails.setResultMessage("User Cancelled");
//            paymentDetails.setResultCode("");
//            paymentDetails.setState(PaymentDetails.State.DECLINED);
//            observeData(paymentDetails);
//        }
//    }
//
//    private double getValuePrecise(double value) {
//
//        DecimalFormat df = new DecimalFormat();
//        df.setMaximumFractionDigits(2);
//        return Double.parseDouble(df.format(value));
//    }
//
//    protected void onResult(int requestCode, int resultCode, @Nullable ExtraData extraData) {
//
//        if (requestCode == REQUEST_SURCHARGE) {
//            if (resultCode == RESULT_OK) {
//                double surchargeAmount = extraData.getSurchargeAmount();
//                double total = Double.parseDouble(approvedAmount) + surchargeAmount;
//                doPostAuth(transactionId, total, Double.parseDouble(approvedTip));
//            } else {
//                doPostAuth(transactionId, Double.parseDouble(approvedAmount), Double.parseDouble(approvedTip));
//            }
//        }
//    }
//
//    private void doPostAuth(String referenceNumber, double total, double tip) {
//
//        doPostAuth(referenceNumber, total, tip, 0D);
//    }
//
//    private void doPostAuth(String referenceNumber, double total, double tip, double surcharge) {
//
//        new Thread(() -> {
//
//            long timeout = 30000;
//
//            String ticketId = ticket_id;
//
//            if (ticketId == null || ticketId.equals("") || ticketId.isEmpty()) {
//                ticketId = "";
//            }
//
//            final PosLink posLink = createPosLink();
//
//            PaymentRequest paymentRequest = new PaymentRequest();
//            paymentRequest.TenderType = paymentRequest.ParseTenderType("CREDIT");
//            paymentRequest.TransType = paymentRequest.ParseTransType("POSTAUTH");
//
//            paymentRequest.Amount = Math.round(total * 100) + "";
//            paymentRequest.TipAmt = Math.round((tip + surcharge) * 100) + "";
//
//            paymentRequest.ECRRefNum = "" + ((int) (Math.random() * 10000));
//            paymentRequest.OrigRefNum = originalReferenceNumber;
//            paymentRequest.ExtData = "<HRefNum>" + referenceNumber + "</HRefNum>";
//
//            paymentRequest.InvNum = webhookId;
//
//            posLink.PaymentRequest = paymentRequest;
//            System.out.println(new Gson().toJson(paymentRequest));
//
//            ProcessTransResult processTransResult = posLink.ProcessTrans();
//            System.out.println(new Gson().toJson(posLink.PaymentResponse));
//
//            System.out.println("CODE: " + processTransResult.Code);
//            System.out.println("MSG: " + processTransResult.Msg);
//
//            if (processTransResult.Code == ProcessTransResult.ProcessTransResultCode.OK && posLink.PaymentResponse.ResultCode.equalsIgnoreCase("000000")) {
//
//                String status;
//
//                if (/*isRefund*/false) {
//                    status = "Refunded";
//                } else {
//                    status = "Paid";
//                }
//
//                String holderName = "";
//                String entryMode = "";
//                String token = "";
//                String href = "";
//                String paymentTip = Math.round((tip) * 100) + "";
//                int first6 = 0;
//                String expDate = "";
//
//                try {
//
//                    /*
//                    Parse extra data from XML
//                        get instance from factory
//                        set namespaceaware true
//                     */
//
//                    String currentTag = null;
//
//                    /*
//                    pass XML as String data
//
//                     */
//
//                    /*
//                    switch (currentTag)
//                        TIPAMOUNT
//                            paymentTip = currentTagValue
//                        TOKEN
//                            token = currentTagValue
//                        CARDBIN
//                            first6 = Integer.parseInt(currentTagValue)
//                        PLNAMEONCARD
//                            holdername = currentTagValue
//                        EXPDATE
//                            this.expDate = currentTagValue
//                        PLENTRYMODE
//                            entryMode = currentTagValue
//                        HREF
//                            href = currentTagValue
//
//                    get next tag
//
//                     */
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                int cardType;
//
//                switch (posLink.PaymentResponse.CardType) {
//                    case "MASTERCARD":
//                    case "02":
//                    case "MASTERCARDFLEET":
//                        cardType = 3;
//                    case "VISA":
//                    case "01":
//                    case "VISAFLEET":
//                    case "09":
//                        cardType = 2;
//                    case "AMEX":
//                    case "03":
//                        cardType = 1;
//                    case "DISCOVER":
//                    case "04":
//                        cardType = 34;
//                    case "DINERCLUB":
//                    case "05":
//                        cardType = 10;
//                    case "JCB":
//                    case "07":
//                        cardType = 11;
//                    default:
//                        cardType = 13;
//                }
//
//                if (token == null || token.equals("") || token.isEmpty()) {
//                    int last = 0;
//                    try {
//                        last = Integer.parseInt(posLink.PaymentResponse.BogusAccountNum);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                    token = first6 + last + expDate + holderName + cardType;
//                }
//
//
//                PaymentDetails paymentDetails = new PaymentDetails();
//                paymentDetails.setState(PaymentDetails.State.INSERT_PAYMENT);
//
//                if (paymentTip != null) {
//                    try {
//                        if (Long.parseLong(paymentTip) > 0) {
//                            long realAmount = Long.parseLong(posLink.PaymentResponse.ApprovedAmount) - Long.parseLong(paymentTip);
//                            paymentDetails.setAmount(realAmount + "");
//                        } else {
//                            paymentDetails.setAmount(posLink.PaymentResponse.ApprovedAmount);
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        paymentDetails.setAmount(posLink.PaymentResponse.ApprovedAmount);
//                    }
//                } else {
//                    paymentDetails.setAmount(posLink.PaymentResponse.ApprovedAmount);
//                }
//
//                paymentDetails.setTipAmount(paymentTip);
//                paymentDetails.setResultCode(posLink.PaymentResponse.ResultCode);
//
//                paymentDetails.setCardType(posLink.PaymentResponse.CardType);
//                paymentDetails.setLast4(posLink.PaymentResponse.BogusAccountNum);
//
//                paymentDetails.setStatus(status);
//                paymentDetails.setPaymentStatus(posLink.PaymentResponse.HostResponse);
//
//                if (href == null || href.equals("") || href.isEmpty()) {
//                    paymentDetails.setHref(posLink.PaymentResponse.HostCode);
//                    paymentDetails.setTransactionNumber(posLink.PaymentResponse.HostCode);
//                } else {
//                    paymentDetails.setHref(href);
//                    paymentDetails.setTransactionNumber(href);
//                }
//
//                originalReferenceNumber = posLink.PaymentResponse.RefNum;
//                transactionId = paymentDetails.getTransactionNumber();
//
//                paymentDetails.setEntryType(entryMode);
//                paymentDetails.setAuthCode(posLink.PaymentResponse.AuthCode);
//                paymentDetails.setReferenceNumber(posLink.PaymentResponse.RefNum);
//                paymentDetails.setResultMessage(posLink.PaymentResponse.Message);
//                paymentDetails.setPaymentResponse(new Gson().toJson(posLink.PaymentResponse));
//                paymentDetails.setCardHolder(holderName);
//                paymentDetails.setRawData(new Gson().toJson(posLink.PaymentResponse));
//                paymentDetails.setPaymentBeforeSurcharge(paymentBeforeSurcharge);
//                paymentDetails.setTipBeforeSurcharge(tipBeforeSurcharge);
//                paymentDetails.setSurchargeAmount(surchargeAmount);
//                paymentDetails.setSurchargeCardType(surchargeCardType);
//
//                paymentDetails.setToken(cardToken);
//                paymentDetails.setExpirationDate(this.expDate);
//
//                observeData(paymentDetails);
//
//            } else if (processTransResult.Code == ProcessTransResult.ProcessTransResultCode.OK && !posLink.PaymentResponse.ResultCode.equalsIgnoreCase("000000")) {
//
//                PaymentDetails paymentDetails = new PaymentDetails();
//                paymentDetails.setState(PaymentDetails.State.DECLINED);
//                paymentDetails.setResultCode(posLink.PaymentResponse.ResultCode);
//                paymentDetails.setResultMessage(posLink.PaymentResponse.ResultTxt);
//                paymentDetails.setResultMessageDetailed(posLink.PaymentResponse.Message);
//                paymentDetails.setRawData(new Gson().toJson(posLink.PaymentResponse));
//                paymentDetails.setAmount(total + "");
//                paymentDetails.setTipAmount(tip + "");
//                observeData(paymentDetails);
//            } else {
//
//                PaymentDetails paymentDetails = new PaymentDetails();
//                paymentDetails.setState(PaymentDetails.State.DECLINED);
//                paymentDetails.setResultCode(posLink.PaymentResponse.ResultCode);
//                paymentDetails.setResultMessage(posLink.PaymentResponse.ResultTxt);
//                paymentDetails.setResultMessageDetailes(posLink.PaymentResponse.Message);
//                paymentDetails.setRawData(new Gson().toJson(posLink.PaymentResponse));
//                paymentDetails.setAmount(total + "");
//                paymentDetails.setTipAmount(tip + "");
//                observeData(paymentDetails);
//            }
//        }).start();
//    }
//
//    private static String oldValue = "";
//
//    public static PosLink createPosLink() {
//        return createPosLink(false);
//    }
//
//    public static PosLink createPosLink(boolean applySettings) {
//
//        CommSetting commSetting = new CommSetting();
//        commSetting.setType(CommSetting.TCP);
//
//        commSetting.setTimeOut("-1");
//        commSetting.setDestIP("192.168.1.6");
//        commSetting.setDestPort("10009");
//        commSetting.setEnableProxy(false);
//
//        final PosLink posLink = new PosLink();
//        posLink.SetCommSetting(commSetting);
//
//        ManageRequest manageRequest;
//        ProcessTransResult processTransResult;
//
//        if (applySettings) {
//
//            manageRequest = new ManageRequest();
//            manageRequest.TransType = manageRequest.ParseTransType("SETVAR");
//            manageRequest.EDCType = manageRequest.ParseEDCType("CREDIT");
//            manageRequest.VarName1 = "menuTimeout";
//            manageRequest.VarValue1 = "600";
//            manageRequest.VarName2 = "accountMenuTimeout";
//            manageRequest.VarValue2 = "600";
//
//            posLink.ManageRequest = manageRequest;
//            System.out.println("REQUEST: " + new Gson().toJson(manageRequest));
//
//            processTransResult = posLink.ProcessTrans();
//            System.out.println("RESPONSE: " + new Gson().toJson(posLink.ManageResponse));
//
//            System.out.println("CODE: " + processTransResult.Code);
//            System.out.println("MSG: " + processTransResult.Msg);
//
//            /*
//            Obligatory support for BofA
//             */
//            if (/*CyberSource*/false) {
//
//                manageRequest = new ManageRequest();
//                manageRequest.TransType = manageRequest.ParseTransType("GETVAR");
//                manageRequest.EDCType = manageRequest.ParseEDCType("CREDIT");
//                manageRequest.VarName1 = "solutionId";
//
//                posLink.ManageRequest = manageRequest;
//                System.out.println("REQUEST: " + new Gson().toJson(manageRequest));
//
//                processTransResult = posLink.ProcessTrans();
//                System.out.println("RESPONSE: " + new Gson().toJson(posLink.ManageResponse));
//
//                System.out.println("CODE: " + processTransResult.Code);
//                System.out.println("MSG: " + processTransResult.Msg);
//
//                String solutionId = "";
//
//                try {
//                    solutionId = posLink.ManageResponse.VarValue1.substring(0, 4) + getSolutionId();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                if (!oldValue.equalsIgnoreCase(manageRequest.VarValue1) || !solutionId.equalsIgnoreCase(manageRequest.VarValue1)) {
//
//                    manageRequest = new ManageRequest();
//                    manageRequest.TransType = manageRequest.ParseTransType("SETVAR");
//                    manageRequest.EDCType = manageRequest.ParseEDCType("CREDIT");
//                    manageRequest.VarName1 = "solutionId";
//
//                    try {
//                        manageRequest.VarValue1 = solutionId;
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        manageRequest.VarValue1 = "S82F";
//                    }
//
//                    oldValue = manageRequest.VarValue1;
//
//                    posLink.ManageRequest = manageRequest;
//                    System.out.println("REQUEST: " + new Gson().toJson(manageRequest));
//
//                    processTransResult = posLink.ProcessTrans();
//                    System.out.println("RESPONSE: " + new Gson().toJson(posLink.ManageResponse));
//
//                    System.out.println("CODE: " + processTransResult.Code);
//                    System.out.println("MSG: " + processTransResult.Msg);
//                }
//            }
//        }
//        return posLink;
//    }
//
//
//
//    public void authorizeTransaction(@Nullable String token, double amount) {
//
//        if (/*!TSYS*/true) {
//
//            System.out.println("Auth not supported");
//
//        } else {
//
//            final PosLink posLink = createPosLink();
//
//            PaymentRequest paymentRequest = new PaymentRequest();
//
//            paymentRequest.TenderType = paymentRequest.ParseTenderType("CREDIT");
//            paymentRequest.TransType = paymentRequest.ParseTransType("AUTH");
//
//            paymentRequest.Amount = Math.round(amount * 100) + "";
//            paymentRequest.ECRRefNum = "" + ((int) (Math.random() * 10000));
//            paymentRequest.OrigRefNum = originalReferenceNumber;
//
//            paymentRequest.InvNum = webhookId;
//
//            posLink.PaymentRequest = paymentRequest;
//            System.out.println(new Gson().toJson(paymentRequest));
//
//            ProcessTransResult processTransResult = posLink.ProcessTrans();
//            PaymentResponse paymentResponse = posLink.PaymentResponse;
//            System.out.println(new Gson().toJson(paymentResponse));
//
//            System.out.println("CODE: " + processTransResult.Code);
//            System.out.println("MSG: " + processTransResult.Msg);
//        }
//    }
//
//    public void incrementTransaction(@Nullable String transaction, double amount) {
//
//    }
//
//    public void completeTransaction(@Nullable String transaction, double amount, double tip) {
//
//        if (/*!TSYS*/true) {
//
//            System.out.println("Auth not supported");
//
//        } else {
//
//            final PosLink posLink = createPosLink();
//
//            PaymentRequest paymentRequest = new PaymentRequest();
//
//            paymentRequest.TenderType = paymentRequest.ParseTenderType("CREDIT");
//            paymentRequest.TransType = paymentRequest.ParseTransType("POSTAUTH");
//
//            paymentRequest.Amount = Math.round(amount * 100) + "";
//            paymentRequest.ECRRefNum = "" + ((int) (Math.random() * 10000));
//            paymentRequest.OrigRefNum = originalReferenceNumber;
//
//            paymentRequest.InvNum = webhookId;
//
//            posLink.PaymentRequest = paymentRequest;
//            System.out.println(new Gson().toJson(paymentRequest));
//
//            ProcessTransResult processTransResult = posLink.ProcessTrans();
//
//            PaymentResponse paymentResponse = posLink.PaymentResponse;
//            System.out.println(new Gson().toJson(paymentResponse));
//
//            System.out.println("CODE: " + processTransResult.Code);
//            System.out.println("MSG: " + processTransResult.Msg);
//        }
//    }
//
//    /*
//    Obligatory support for BofA
//     */
//    public void cyberSourceCompleteTransaction(String originalReferenceNumber) {
//
//        final PosLink posLink = createPosLink(false);
//
//        PaymentRequest paymentRequest = new PaymentRequest();
//
//        paymentRequest.TenderType = paymentRequest.ParseTenderType("CREDIT");
//        paymentRequest.TransType = paymentRequest.ParseTransType("ADJUST"); // Unsupported transaction
//        paymentRequest.Amount = "0";
//
//        paymentRequest.ECRRefNum = "" + ((int) (Math.random() * 10000));
//        paymentRequest.OrigRefNum = originalReferenceNumber;
//        paymentRequest.InvNum = "" + ((int) (Math.random()));
//
//        posLink.PaymentRequest = paymentRequest;
//
//        ProcessTransResult processTransResult = posLink.ProcessTrans();
//
//        String paymentResponse = new Gson().toJson(posLink.PaymentResponse);
//        System.out.println(paymentResponse);
//
//        if (processTransResult.Code != ProcessTransResult.ProcessTransResultCode.OK || !posLink.PaymentResponse.ResultCode.equalsIgnoreCase("000000")) {
//            System.out.println("Something went wrong...");
//        }
//
//        System.out.println("CODE: " + processTransResult.Code);
//        System.out.println("MSG: " + processTransResult.Msg);
//    }
//}