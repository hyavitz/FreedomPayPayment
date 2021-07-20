//package paxs300.payment;
//
//import android.app.Dialog;
//import android.content.Intent;
//import android.os.Build;
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.util.Log;
//import android.view.Window;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.Nullable;
//import androidx.annotation.WorkerThread;
//
//import com.EnrollPay.models.commands.CommandApplyDiscount;
//import com.EnrollPay.models.commands.CommandRequestCard;
//import com.EnrollPay.models.transaction.CardInformation;
//import com.EnrollPay.models.transaction.CardPresented;
//import com.EnrollPay.models.transaction.DiscountApplied;
//import com.EnrollPay.models.transaction.PaymentDecision;
//import com.EnrollPay.models.transaction.Transaction;
//import com.google.gson.Gson;
//import com.pax.poslink.CommSetting;
//import com.pax.poslink.ManageRequest;
//import com.pax.poslink.POSLinkAndroid;
//import com.pax.poslink.PaymentRequest;
//import com.pax.poslink.PosLink;
//import com.pax.poslink.ProcessTransResult;
//import com.pax.poslink.ReportRequest;
//import com.pax.poslink.poslink.POSLinkCreator;
//import com.softpointdev.quickpoint.R;
//import com.softpointdev.quickpoint.activity.payment.loyalty.enrollpay.ApprovalInformation;
//import com.softpointdev.quickpoint.activity.payment.loyalty.enrollpay.EnrollAndPay;
//import com.softpointdev.quickpoint.activity.payment.loyalty.enrollpay.QuestionAnswer;
//import com.softpointdev.quickpoint.activity.payment.loyalty.enrollpay.pojo.CommandDisplayThankyou;
//import com.softpointdev.quickpoint.network.softpoint.pojo.PaymentDetails;
//import com.softpointdev.quickpoint.setting.SessionManager;
//import com.softpointdev.quickpoint.setting.Tips;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.xmlpull.v1.XmlPullParser;
//import org.xmlpull.v1.XmlPullParserException;
//import org.xmlpull.v1.XmlPullParserFactory;
//
//import java.io.IOException;
//import java.io.StringReader;
//import java.text.DecimalFormat;
//import java.util.concurrent.TimeUnit;
//
//import javax.inject.Inject;
//
//import dagger.hilt.android.AndroidEntryPoint;
//import lombok.NonNull;
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//import timber.log.Timber;
//
//import static com.softpointdev.commonpoint.common.ui.BaseApplication.getStaticContext;
//import static com.softpointdev.quickpoint.QuickPointApplication.getEnrollPayApi;
//import static com.softpointdev.quickpoint.QuickPointApplication.getKioskpointApi;
//import static com.softpointdev.quickpoint.QuickPointApplication.getSessionManager;
//@AndroidEntryPoint
//public class PAXDevicePaymentActivity extends DevicePaymentActivity {
//
//
//    private EnrollAndPay ep;
//    private String origRef;
//    private String transactionID = "";
//    private String cardToken;
//    private String expDate;
//    private int f6;
//    private int l4;
//
//    private String preentrytype;
//
//    private final int REQUEST_SURCHARGE = 2805;
//    private String approvedAmount = "";
//    private String approvedTip = "";
//    private PosLink posLink;
//
//    private String surchargeCardType = "", surchargeAmount = "", paymentBeforeSurcharge, tipBeforeSurcharge;
//
//    private final int AFTER_SALE = 1;
//    private final int AFTER_AUTH_PAYMENT = 2;
//    private final int AFTER_TOKENIZE_SALE = 3;
//    private final int AFTER_DO_AUTH = 4;
//    private final int AFTER_DO_POST_AUTH = 5;
//    private boolean isCredit;
//
//    @Inject SessionManager sessionManager;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        paymentBeforeSurcharge = getIntent().getStringExtra("paymentBeforeSurcharge");
//        tipBeforeSurcharge = getIntent().getStringExtra("tipBeforeSurcharge");
//        surchargeAmount = getIntent().getStringExtra("surchargeAmount");
//        surchargeCardType = getIntent().getStringExtra("surchargeCardType");
//    }
//
//    protected EnrollAndPay getEnroll() {
//        if(ep == null) {
//            synchronized (this) {
//                if(ep == null) {
//                    ep = new EnrollAndPay(this, new EnrollAndPay.EPListener() {
//                        @Override
//                        public void onSuccess(JSONObject obj, double total, double tip) {
//                            processEnrollCommand(obj, total, tip);
//                        }
//
//                        @Override
//                        public void onFailure(String message) {
//                            PaymentDetails paymentDetails = new PaymentDetails();
//                            paymentDetails.setState(PaymentDetails.State.ERROR);
//                            paymentDetails.setResult_message(message);
//                            observeData(paymentDetails);
//                        }
//                    });
//                }
//            }
//        }
//
//        return ep;
//    }
//
//    public void makePayment(double total, double tip) {
//        if(total < 0) {
//            isCredit = true;
//            total = Math.abs(total);
//        }
//
//        if(getSessionManager().getEPEnabled() && !isCredit) {
//
//            // TODO: start E&P payment
//            getEnroll().startTransaction(total, tip, getIntent().getStringExtra("webhook_id"));
//
//        } else if(((!TextUtils.isEmpty(getSessionManager().getPayRoc()) && getSessionManager().getPayRoc().equalsIgnoreCase("Yes"))
//                || getIntent().getBooleanExtra("doPreAuth", false)) && !isCredit){
//            doAuthPayment(total, tip);
//        } else {
//            initiatePayment(total, tip);
//        }
//    }
//
//    private PaymentDetails authDetails;
//    CommandRequestCard cardRequest;
//    private Double tax =0.0;
//
//    private void processEnrollCommand(JSONObject object, double total, double tip) {
//
//        System.out.println("<><> out " + object.toString());
//
//
//        switch (object.optInt("CommandType", 0)) {
//            case 1: // Display Transaction
//                // This command should never happen...
//                break;
//            case 2: // Request Card
//                System.out.println("<><> Request Card");
//                cardRequest = new Gson().fromJson(object.toString(), CommandRequestCard.class);
//                new Thread(() -> {
//                    try {
//                        transactionTokenize(total, tip); // attempt to get a token from the card for later use
//                    } catch (Exception e) {
//                        System.out.println("<><> regular payment");
//                        initiatePayment(total, tip); // TODO: need to move some of the code in here to the EnorllAndPay class
//                    }
//                }).start();
//
//                break;
//            case 3: // Display Enrollment
//                getEnroll().displayEnrollFragment(object, total, tip);
//                break;
//            case 4: // Display Discount Select
//                getEnroll().displayDiscountSelect(object, total, tip);
//                break;
//            case 7: // Request Authorization
//
//                // This request completes the authorization of a payment for a set amount
//                System.out.println("<><> Request Auth");
//                new Thread(() -> {
//                    try {
//                        if(!TextUtils.isEmpty(cardToken)) {
//                            System.out.println("<><> Tokenize sale");
//                            transactionTokenizeSale(cardToken, total, tip);
//                        } else {
//                            System.out.println("<><> Sale :(");
//                            authPayment(transactionID, (total + tax), tip);
//                        }
//                    } catch (Exception e) {
//                        Timber.w(e, "<><> failed :(");
//                        transactionTokenizeSale("" , total, tip);
//
//                    }
//                }).start();
//                break;
//
//            case 8: // Payment Complete
//
//                System.out.println("<><> Payment Complete");
//
//                Transaction commandComplete = new Transaction();
//                commandComplete.setOrderId(getEnroll().getEpOrderId());
//
//                System.out.println("<><> Payment Responding " + new Gson().toJson(commandComplete));
//
//                getEnrollPayApi().transactionComplete(getSessionManager().getEPJWT(), commandComplete).enqueue(new Callback<String>() {
//                    @Override
//                    public void onResponse(Call<String> call, Response<String> response) {
//                        try {
//                            JSONObject object = new JSONObject(response.body());
//                            processEnrollCommand(object, total, tip);
//                        } catch (Exception e) {
//                            Timber.w(e);
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Call<String> call, Throwable t) {
//
//                    }
//                });
//
//                break;
//
//            case 9: // Display Question
//                System.out.println("<><> Question");
//                // TODO: I don't think there is actually a way to leave if this is being displayed.
//                getEnroll().displayQuestion(object, total, tip);
//
//                /*if(authDetails != null) {
//                    observeData(authDetails);
//                    authDetails = null;
//                    return;
//                }*/
//                break;
//            case 10: // Display Thank you
//                System.out.println("<><> Thank you");
//
//                CommandDisplayThankyou disThankyou = new Gson().fromJson(object.toString(), CommandDisplayThankyou.class);
//
//                // No questions to display so return right away.
//                if(disThankyou.getAmountTotal() == null) {
//                    observeData(authDetails);
//                    authDetails = null;
//                    return;
//                }
//
//                getEnroll().displayThankyou(disThankyou, total, tip, (rate) -> {
//                    QuestionAnswer ans = new QuestionAnswer();
//                    ans.setOrderId(getEnroll().getEpOrderId());
//                    try {
//                        ans.setOrderReviewQuestionId(disThankyou.getRecipients().get(0).getQuestions().get(0).getOrderReviewQuestionId());
//                        ans.setConsumerId(disThankyou.getRecipients().get(0).getConsumerId());
//                        //ans.setQuestionId(1);
//                        ans.setScore(rate);
//                    } catch (Exception e) {
//                        ans.setOrderReviewQuestionId(0);
//                        ans.setConsumerId(0);
//                        ans.setScore(0);
//                    }
//
//                    getEnrollPayApi().questionRequest(getSessionManager().getEPJWT(), ans).enqueue(new Callback<String>() {
//                        @Override
//                        public void onResponse(Call<String> call, Response<String> response) {
//                            try {
//                                JSONObject object = new JSONObject(response.body());
//                                processEnrollCommand(object, total, tip);
//                            } catch (Exception e) {
//                                System.out.println("Display Thank You Failed");
//                            }
//                        }
//
//                        @Override
//                        public void onFailure(Call<String> call, Throwable t) {
//
//                        }
//                    });
//                });
//                break;
//            case 13: // Apply Discount
//
//                System.out.println("<><> Apply Discount");
//                getEnroll().displayNewTotal(object, total, tip, (rate) -> {
//
//                    CommandApplyDiscount applyCommand = new Gson().fromJson(object.toString(), CommandApplyDiscount.class);
//                    final Double[] amountTotal = new Double[1];
//                    getKioskpointApi().applyDiscount(sessionManager.getDiscountId(),Double.toString(applyCommand.getAmount()*100),"Enroll & Pay Discount", getIntent().getStringExtra("unique_id"),"yes").enqueue(new Callback<String>() {
//                        @Override
//                        public void onResponse(Call<String> call, Response<String> response) {
//                            try {
//                                System.out.println("Returned Data: !! " + response.body());
//                                JSONObject json = new JSONObject(response.body());
//                                if(json.optInt("ResponseCode", 0) == 1) {
//                                    if(json.has("ResponseData")) {
//                                        JSONObject responseData = json.getJSONObject("ResponseData");
//                                        amountTotal[0] = responseData.getDouble("SubTotalDec");
//                                        tax = responseData.getDouble("TaxDec");
//                                    } else {
//                                        amountTotal[0] = total - applyCommand.getAmount();
//                                        // tax = responseData.getDouble("TaxDec");
//                                    }
//
//                                    DiscountApplied apply = new DiscountApplied();
//                                    apply.setOrderId(getEnroll().getEpOrderId());
//                                    apply.setOrderPaymentId(cardRequest.getOrderPaymentId());
//                                    apply.setAppliedAmount(applyCommand.getAmount());
//                                    apply.setInternalIdent(applyCommand.getInternalIdent());
//                                    apply.setAppliedCount(1.0);
//                                    apply.setReason("SUCCESS");
//
//                                    getEnrollPayApi().discountApplied(getSessionManager().getEPJWT(), apply).enqueue(new Callback<String>() {
//                                        @Override
//                                        public void onResponse(Call<String> call, Response<String> response) {
//                                            try {
//                                                JSONObject object = new JSONObject(response.body());
//                                                processEnrollCommand(object, amountTotal[0], tip);
//                                            } catch (Exception e) {
//                                                Timber.w(e);
//                                            }
//                                        }
//
//                                        @Override
//                                        public void onFailure(Call<String> call, Throwable t) {
//// No idea
//                                        }
//                                    });
//                                } else {
//                                    // TODO: Failed to apply discount
//                                    PaymentDetails paymentDetails = new PaymentDetails();
//                                    paymentDetails.setResult_message("Unable to apply discount");
//                                    paymentDetails.setResult_code("");
//                                    paymentDetails.setState(PaymentDetails.State.DECLINED);
//                                    observeData(paymentDetails);
//                                }
//                            }
//                            catch (JSONException e) {
//                                e.printStackTrace();
//                                System.out.println("Returned Data: !! " + response.body());
//
//                                PaymentDetails paymentDetails = new PaymentDetails();
//                                paymentDetails.setResult_message("Unable to apply discount");
//                                paymentDetails.setResult_code("");
//                                paymentDetails.setState(PaymentDetails.State.DECLINED);
//                                observeData(paymentDetails);
//                            }
//                        }
//
//                        @Override
//                        public void onFailure(Call<String> call, Throwable t) {
//                            amountTotal[0] = total;
//                            DiscountApplied apply = new DiscountApplied();
//                            apply.setOrderId(getEnroll().getEpOrderId());
//                            apply.setOrderPaymentId(cardRequest.getOrderPaymentId());
//                            apply.setAppliedAmount(applyCommand.getAmount());
//                            apply.setInternalIdent(applyCommand.getInternalIdent());
//                            apply.setAppliedCount(1.0);
//                            apply.setReason("SUCCESS");
//
//                            getEnrollPayApi().discountApplied(getSessionManager().getEPJWT(), apply).enqueue(new Callback<String>() {
//                                @Override
//                                public void onResponse(Call<String> call, Response<String> response) {
//                                    try {
//                                        JSONObject object = new JSONObject(response.body());
//                                        processEnrollCommand(object, amountTotal[0], tip);
//                                    } catch (Exception e) {
//                                        Timber.w(e);
//                                    }
//                                }
//
//                                @Override
//                                public void onFailure(Call<String> call, Throwable t) {
//// No idea
//                                }
//                            });
//
//                        }
//                    });
//
//                });
//
//                break;
//
//            case 14: // Card Presented
//            case 15: // Payment Requested
//            case 16: // Order Completed
//                if(TextUtils.isEmpty(cardToken)) {
//                    cardRequest = new Gson().fromJson(object.toString(), CommandRequestCard.class);
//                    new Thread(() -> {
//                        try {
//                            transactionTokenize(total, tip); // attempt to get a token from the card for later use
//                        } catch (Exception e) {
//                            System.out.println("<><> regular payment");
//                            initiatePayment(total, tip); // TODO: need to move some of the code in here to the EnorllAndPay class
//                        }
//                    }).start();
//
//                }else {
//                    getEnroll().completePayment(object, total, tip);
//                    if (authDetails != null) {
//                        observeData(authDetails);
//                        authDetails = null;
//                        return;
//                    }
//                }
//                break;
//            case 17: // Order Canceled
//            case 18: // Discount Applied
//            case 19: // Discount Select List
//            case 20: // Request Payment Adjustment
//            case 21: // Idle
//                // This command should never happen
//                break;
//            default:
//                if(authDetails != null) {
//                    observeData(authDetails);
//                    authDetails = null;
//                    return;
//                }
//                break;
//        }
//    }
//
//    public void transactionTokenize(double total, double tip) {
//        long timeout = (getTriggeredTime().getTime() + TimeUnit.SECONDS.toMillis(getSessionManager().getTimeout())) - System.currentTimeMillis();
//
//        String ticket_id = getIntent().getStringExtra("ticket_id");
//        if (TextUtils.isEmpty(ticket_id)) {
//            ticket_id = "";
//        }
//
//        Timber.tag("PAX BroadPOS Payment").i("Starting tokenize: %s %s %s %s", ticket_id, timeout, total, tip);
//        Timber.tag("PAX BroadPOS Payment").i("Finding Model %s", Build.MODEL);
//
//        posLink = createAndValidatePOSLink();
//
//        Timber.tag("PAX BroadPOS Payment").i("Link Setup");
//
//        PaymentRequest paymentRequest = new PaymentRequest();
//        paymentRequest.TenderType = paymentRequest.ParseTenderType("CREDIT");
//        if(getSessionManager().getEPEnabled()) {
//            paymentRequest.TransType = paymentRequest.ParseTransType("AUTH");
//            paymentRequest.ExtData = "<TokenRequest>1</TokenRequest>";
//        } else {
//            paymentRequest.TransType = paymentRequest.ParseTransType("SALE");
//            if (getSessionManager().isSaleRequestToken()) {
//                paymentRequest.ExtData = "<TokenRequest>1</TokenRequest>";
//            }
//            if (getSessionManager().getTips() == Tips.BEFORE && getSessionManager().getProcessor().equalsIgnoreCase("Heartland")) {
//                if (TextUtils.isEmpty(paymentRequest.ExtData)) {
//                    paymentRequest.ExtData = "<TipRequest>1</TipRequest>";
//                } else {
//                    paymentRequest.ExtData += "<TipRequest>1</TipRequest>";
//                }
//            }
//        }
//
//        //paymentRequest.TransType = paymentRequest.ParseTransType("TOKENIZE");
//
//        Timber.tag("PAX BroadPOS Payment").i("RefNum " + getPendingId() + "; " + getTransactionNo());
//
//        paymentRequest.Amount = Math.round(total * 100) + "";
//        paymentRequest.TipAmt = Math.round(tip * 100) + "";
//        paymentRequest.ECRRefNum = getPendingId();
//        //paymentRequest.ECRRefNum = "000000";
//        //paymentRequest.ECRTransID = getTransactionNo();
//        paymentRequest.InvNum = getIntent().getStringExtra("webhook_id");
//        //paymentRequest.PONum = getIntent().getStringExtra("webhook_id");
//
//        posLink.PaymentRequest = paymentRequest;
//
//        ProcessTransResult ptr;
//        if(authResult == null) {
//            Timber.tag("PAX BroadPOS Payment").i("Processing Tokenize " + new Gson().toJson(paymentRequest));
//            insertPaymentRequestResponse(new Gson().toJson(paymentRequest), "", "", "");
//            ptr = posLink.ProcessTrans();
//            authResult = ptr;
//            insertPaymentRequestResponse("", new Gson().toJson(posLink.PaymentResponse), "", "");
//        } else {
//            ptr = authResult;
//        }
//
//        Timber.tag("PAX BroadPOS Payment").i("Tokenize Finished: " + ptr.Code + ": " + ptr.Msg);
//        Timber.tag("PAX BroadPOS Payment").i("Tokenize Finished: %s", new Gson().toJson(posLink.PaymentResponse));
//
//        if(ptr.Code == ProcessTransResult.ProcessTransResultCode.OK) { // Easy success
//
//            if(posLink.PaymentResponse.ResultCode.equalsIgnoreCase("000000")) {
//
//                String status;
//                if (getIntent().getBooleanExtra("wh_isRefund", false)) {
//                    status = "Refunded";
//                } else {
//                    status = "Paid";
//                }
//
//                String holderName = "";
//                String entryMode = "";
//                String token = "";
//                String href = null;
//                String pay_tip = Math.round(tip * 100) + "";
//                int first6 = 0;
//                String expDate = "";
//                //Parse the extra data
//                try {
//                    //String extraDataValue = "<Attributes>" + output.ExtData + "</Attributes>";
//                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
//                    factory.setNamespaceAware(true);
//                    XmlPullParser xpp = factory.newPullParser();
//                    String currentTag = null;
//                    xpp.setInput( new StringReader( posLink.PaymentResponse.ExtData ) ); // pass input whatever xml you have
//                    int eventType = xpp.getEventType();
//                    while (eventType != XmlPullParser.END_DOCUMENT) {
//                        if(eventType == XmlPullParser.START_TAG) {
//                            currentTag = xpp.getName();
//                        } else if(eventType == XmlPullParser.END_TAG) {
//                            currentTag = null;
//                        } else if(eventType == XmlPullParser.TEXT) {
//                            String currentTagValue = xpp.getText();
//                            if (currentTag != null){
//                                Timber.tag("PAX BroadPOS Payment").i("Tag Found: %s, %s", currentTag.toUpperCase(), currentTagValue);
//                                switch (currentTag.toUpperCase()){
//                                    case "TIPAMOUNT":
//                                        if(getSessionManager().getTips() == Tips.BEFORE && getSessionManager().getProcessor().equalsIgnoreCase("Heartland")) {
//                                            pay_tip = currentTagValue;
//                                        }
//                                        break;
//                                    case "TOKEN":
//                                        //cardInformation.setUniqueCardIdent(currentTagValue);
//                                        token = currentTagValue;
//                                        cardToken = token;
//                                        break;
//                                    case "CARDBIN":
//
//                                        try {
//                                            first6 = Integer.parseInt(currentTagValue);
//                                            f6 = first6;
//                                        } catch (Exception e) {
//                                            Timber.w(e, "Exception getting first6");
//                                        }
//                                        break;
//                                    case "PLNAMEONCARD":
//                                        holderName = currentTagValue;
//                                        break;
//                                    case "EXPDATE":
//                                        expDate = currentTagValue;
//                                        this.expDate = expDate;
//                                        break;
//                                    case "PLENTRYMODE":
//                                        entryMode = currentTagValue;
//                                        break;
//                                    case "HREF":
//                                        href = currentTagValue;
//                                        break;
//                                }
//                            }
//                        }
//                        eventType = xpp.next();
//                    }
//
//                } catch (XmlPullParserException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                int cardType;
//                switch (posLink.PaymentResponse.CardType)
//                {
//                    case "MASTERCARD":
//                    case "02":
//                    case "MASTERCARDFLEET":
//                        cardType = 3;
//                        break;
//                    case "VISA":
//                    case "01":
//                    case "VISAFLEET":
//                    case "09":
//                        cardType = 2;
//                        break;
//                    case "AMEX":
//                    case "03":
//                        cardType = 1;
//                        break;
//                    case "DISCOVER":
//                    case "04":
//                        cardType = 34;
//                        break;
//                    case "DINERCLUB":
//                    case "05":
//                        cardType = 10;
//                        break;
//                    case "JCB":
//                    case "07":
//                        cardType = 11;
//                        break;
//                    default:
//                        cardType = 13;
//                        break;
//                }
//
//                if (TextUtils.isEmpty(token))
//                {
//                    int last = 0;
//                    try {
//                        last = Integer.parseInt( posLink.PaymentResponse.BogusAccountNum);
//                        l4 = last;
//                    } catch (Exception e) {
//                        Timber.w(e, "Exception getting last4");
//                    }
//                    token = first6 + last + expDate + holderName + cardType;
//                    //cardInformation.setUniqueCardIdent(cardInformation.getPANFirstSix() + cardInformation.getPANLastFour() + expDate + cardInformation.getNameOnCard() + cardInformation.getCardType() );
//                } else {
//                    Timber.tag("PAX BroadPOS Payment").i("WE HAVE A TOKEN! lets use that %s", token);
//                }
//
//                PaymentDetails paymentDetails = new PaymentDetails();
//                paymentDetails.setState(PaymentDetails.State.INSERT_PAYMENT);
//                if(pay_tip != null) {
//                    try {
//                        if(Long.parseLong(pay_tip)>0) {
//                            Long realAmount = Long.parseLong(posLink.PaymentResponse.ApprovedAmount) - Long.parseLong(pay_tip);
//                            paymentDetails.setAmount(realAmount + "");
//                        } else {
//                            paymentDetails.setAmount(posLink.PaymentResponse.ApprovedAmount);
//                        }
//                    }catch (Exception e){
//                        paymentDetails.setAmount(posLink.PaymentResponse.ApprovedAmount);
//                    }
//                }
//                else {
//                    paymentDetails.setAmount(posLink.PaymentResponse.ApprovedAmount);
//                }
//                paymentDetails.setTipAmount(pay_tip);
//                paymentDetails.setResult_code(posLink.PaymentResponse.ResultCode);
//
//                paymentDetails.setCardType(posLink.PaymentResponse.CardType);
//                paymentDetails.setLast4(posLink.PaymentResponse.BogusAccountNum);
//                paymentDetails.setStatus(status);
//                paymentDetails.setPayment_status(posLink.PaymentResponse.HostResponse);
//                if(TextUtils.isEmpty(href)) {
//                    paymentDetails.setHref(posLink.PaymentResponse.HostCode);
//                    paymentDetails.setTransactionNo(posLink.PaymentResponse.HostCode);
//                } else {
//                    paymentDetails.setHref(href);
//                    paymentDetails.setTransactionNo(href);
//                }
//
//                origRef = posLink.PaymentResponse.RefNum;
//                transactionID = paymentDetails.getTransactionNo();
//
//                paymentDetails.setEntryType(entryMode);
//                paymentDetails.setAuth_code(posLink.PaymentResponse.AuthCode);
//                paymentDetails.setRef_num(posLink.PaymentResponse.RefNum);
//                //paymentDetails.setAio_accountid(object.getJSONObject("packetData").optString("accountId"));
//                paymentDetails.setResult_message(posLink.PaymentResponse.Message);
//
//                //paymentDetails.setIntegrator_version(object.getJSONObject("packetData").optString("integratorVersion"));
//                //paymentDetails.setFw_version(object.getJSONObject("packetData").optString("FWVersion"));
//                paymentDetails.setPay_response(new Gson().toJson(posLink.PaymentResponse));
//                paymentDetails.setCardHolder(holderName);
//                //paymentDetails.setEmvTagData(object.getJSONObject("packetData").optString("receiptEmvTagMap"));
//                paymentDetails.setRawData(new Gson().toJson(posLink.PaymentResponse));
//                paymentDetails.setPaymentBeforeSurcharge(paymentBeforeSurcharge);
//                paymentDetails.setTipBeforeSurcharge(tipBeforeSurcharge);
//                paymentDetails.setSurchargeAmount(surchargeAmount);
//                paymentDetails.setSurchargeCardType(surchargeCardType);
//
//                paymentDetails.setToken(cardToken);
//                paymentDetails.setExp_date(this.expDate);
//
//                if(getSessionManager().getEPEnabled()) {
//
//                    CardPresented pre = new CardPresented();
//                    pre.setOrderId(getEnroll().getEpOrderId());
//                    pre.setOrderPaymentId(cardRequest.getOrderPaymentId());
//                    pre.setCardType(cardType);
//                    pre.setNameOnCard(holderName);
//                    pre.setPANFirstSix(first6);
//                    pre.setPANLastFour(Integer.parseInt( posLink.PaymentResponse.BogusAccountNum));
//                    pre.setUniqueCardIdent(token);
//
//                    System.out.println("<><> " + new Gson().toJson(pre));
//
//                    getEnrollPayApi().transctionCardPresented(getSessionManager().getEPJWT(), pre).enqueue(new Callback<String>() {
//                        @Override
//                        public void onResponse(Call<String> call, Response<String> response) {
//                            try {
//                                JSONObject object = new JSONObject(response.body());
//                                processEnrollCommand(object, total, tip);
//                            } catch (Exception e) {
//                                Timber.w(e);
//                            }
//                        }
//
//                        @Override
//                        public void onFailure(Call<String> call, Throwable t) {
//
//                        }
//                    });
//
//                } else {
//                    observeData(paymentDetails);
//                }
//
//            } else {
//
//                PaymentDetails paymentDetails = new PaymentDetails();
//                paymentDetails.setState(PaymentDetails.State.DECLINED);
//                paymentDetails.setResult_code(posLink.PaymentResponse.ResultCode);
//                paymentDetails.setResult_message(posLink.PaymentResponse.ResultTxt);
//                paymentDetails.setResult_detailedMessage(posLink.PaymentResponse.Message);
//                paymentDetails.setRawData(new Gson().toJson(posLink.PaymentResponse));
//                paymentDetails.setAmount(total + "");
//                paymentDetails.setTipAmount(tip + "");
//                observeData(paymentDetails);
//            }
//
//        } else { // Easy Fail
//
//            PaymentDetails paymentDetails = new PaymentDetails();
//            paymentDetails.setState(PaymentDetails.State.DECLINED);
//            paymentDetails.setResult_code(posLink.PaymentResponse.ResultCode);
//            paymentDetails.setResult_message(posLink.PaymentResponse.ResultTxt);
//            paymentDetails.setResult_detailedMessage(posLink.PaymentResponse.Message);
//            paymentDetails.setRawData(new Gson().toJson(posLink.PaymentResponse));
//            paymentDetails.setAmount(total + "");
//            paymentDetails.setTipAmount(tip + "");
//            observeData(paymentDetails);
//        }
//    }
//
//    public void transactionTokenizeSale(String cardToken, double total, double tip) {
//        long timeout = (getTriggeredTime().getTime() + TimeUnit.SECONDS.toMillis(getSessionManager().getTimeout())) - System.currentTimeMillis();
//
//        String ticket_id = getIntent().getStringExtra("ticket_id");
//        if (TextUtils.isEmpty(ticket_id)) {
//            ticket_id = "";
//        }
//
//        Timber.tag("PAX BroadPOS Payment").i("Starting Payment: %s %s %s %s", ticket_id, timeout, total, tip);
//
//        posLink = createAndValidatePOSLink();
//
//        Timber.tag("PAX BroadPOS Payment").i("Link Setup");
//        Timber.tag("PAX BroadPOS Payment").i("RefNum %s", ticket_id);
//        Timber.tag("PAX BroadPOS Payment").i("Token %s", cardToken);
//
//        PaymentRequest paymentRequest = new PaymentRequest();
//        paymentRequest.TenderType = paymentRequest.ParseTenderType("CREDIT");
//        paymentRequest.TransType = paymentRequest.ParseTransType("SALE");
//
//        long baseAmount = Math.round(total * 100);
//        paymentRequest.Amount = baseAmount + "";
//        paymentRequest.TipAmt = Math.round(tip * 100) + "";
//        paymentRequest.ECRRefNum = ticket_id.replace("-", "");
//        paymentRequest.ExtData = "<Token>" + cardToken + "</Token>";
//        if(!TextUtils.isEmpty(expDate)) {
//            paymentRequest.ExtData = paymentRequest.ExtData + "<ExpDate>" + expDate + "</ExpDate>";
//        }
//
//        posLink.PaymentRequest = paymentRequest;
//
//        Timber.tag("PAX BroadPOS Payment").i("Request " + new Gson().toJson(paymentRequest));
//        insertPaymentRequestResponse(new Gson().toJson(paymentRequest), "", "", "");
//        ProcessTransResult ptr = posLink.ProcessTrans();
//        insertPaymentRequestResponse("", new Gson().toJson(posLink.PaymentResponse), "", "");
//        Timber.tag("PAX BroadPOS Payment").i("Response " + new Gson().toJson(posLink.PaymentResponse));
//
//        if(ptr.Code == ProcessTransResult.ProcessTransResultCode.OK) { // Easy success
//
//            if(posLink.PaymentResponse.ResultCode.equalsIgnoreCase("000000")) {
//
//                String status;
//                if (getIntent().getBooleanExtra("wh_isRefund", false)) {
//                    status = "Refunded";
//                } else {
//                    status = "Paid";
//                }
//
//                String holderName = "";
//                String entryMode = "";
//                String token = "";
//                String href = null;
//                int first6 = 0;
//                String expDate = "";
//                //Parse the extra data
//                try {
//                    //String extraDataValue = "<Attributes>" + output.ExtData + "</Attributes>";
//                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
//                    factory.setNamespaceAware(true);
//                    XmlPullParser xpp = factory.newPullParser();
//                    String currentTag = null;
//                    xpp.setInput( new StringReader( posLink.PaymentResponse.ExtData ) ); // pass input whatever xml you have
//                    int eventType = xpp.getEventType();
//                    while (eventType != XmlPullParser.END_DOCUMENT) {
//                        if(eventType == XmlPullParser.START_TAG) {
//                            currentTag = xpp.getName();
//                        } else if(eventType == XmlPullParser.END_TAG) {
//                            currentTag = null;
//                        } else if(eventType == XmlPullParser.TEXT) {
//                            String currentTagValue = xpp.getText();
//                            if (currentTag != null){
//                                Timber.tag("PAX BroadPOS Payment").i("Tag Found: %s, %s", currentTag.toUpperCase(), currentTagValue);
//                                switch (currentTag.toUpperCase()){
//                                    case "TOKEN":
//                                        //cardInformation.setUniqueCardIdent(currentTagValue);
//                                        token = currentTagValue;
//                                        cardToken = token;
//                                        break;
//                                    case "CARDBIN":
//
//                                        try {
//                                            first6 = Integer.parseInt(currentTagValue);
//                                        } catch (Exception e) {
//                                            Timber.w(e, "Exception getting first6");
//                                        }
//                                        break;
//                                    case "PLNAMEONCARD":
//                                        holderName = currentTagValue;
//                                        break;
//                                    case "EXPDATE":
//                                        expDate = currentTagValue;
//                                        this.expDate = expDate;
//                                        break;
//                                    case "PLENTRYMODE":
//                                        entryMode = currentTagValue;
//                                        break;
//                                    case "HREF":
//                                        href = currentTagValue;
//                                        break;
//                                }
//                            }
//                        }
//                        eventType = xpp.next();
//                    }
//
//                } catch (XmlPullParserException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                int cardType;
//                switch (posLink.PaymentResponse.CardType)
//                {
//                    case "MASTERCARD":
//                    case "02":
//                    case "MASTERCARDFLEET":
//                        cardType = 3;
//                        break;
//                    case "VISA":
//                    case "01":
//                    case "VISAFLEET":
//                    case "09":
//                        cardType = 2;
//                        break;
//                    case "AMEX":
//                    case "03":
//                        cardType = 1;
//                        break;
//                    case "DISCOVER":
//                    case "04":
//                        cardType = 34;
//                        break;
//                    case "DINERCLUB":
//                    case "05":
//                        cardType = 10;
//                        break;
//                    case "JCB":
//                    case "07":
//                        cardType = 11;
//                        break;
//                    default:
//                        cardType = 13;
//                        break;
//                }
//
//                if (TextUtils.isEmpty(token))
//                {
//                    int last = 0;
//                    try {
//                        last = Integer.parseInt( posLink.PaymentResponse.BogusAccountNum);
//                    } catch (Exception e) {
//                        Timber.w(e, "Exception getting last4");
//                    }
//                    token = first6 + last + expDate + holderName + cardType;
//                    //cardInformation.setUniqueCardIdent(cardInformation.getPANFirstSix() + cardInformation.getPANLastFour() + expDate + cardInformation.getNameOnCard() + cardInformation.getCardType() );
//                }
//
//                PaymentDetails paymentDetails = new PaymentDetails();
//                paymentDetails.setState(PaymentDetails.State.INSERT_PAYMENT);
//                paymentDetails.setAmount(posLink.PaymentResponse.ApprovedAmount);
//                long approvedAmount = Long.parseLong(posLink.PaymentResponse.ApprovedAmount);
//                if (approvedAmount > baseAmount) {
//                    paymentDetails.setTipAmount(approvedAmount - baseAmount + "");
//                } else {
//                    paymentDetails.setTipAmount("0");
//                }
//                paymentDetails.setResult_code(posLink.PaymentResponse.ResultCode);
//
//                paymentDetails.setCardType(posLink.PaymentResponse.CardType);
//                paymentDetails.setLast4(posLink.PaymentResponse.BogusAccountNum);
//                paymentDetails.setStatus(status);
//                paymentDetails.setPayment_status(posLink.PaymentResponse.HostResponse);
//                if(TextUtils.isEmpty(href)) {
//                    paymentDetails.setHref(posLink.PaymentResponse.HostCode);
//                    paymentDetails.setTransactionNo(posLink.PaymentResponse.HostCode);
//                } else {
//                    paymentDetails.setHref(href);
//                    paymentDetails.setTransactionNo(href);
//                }
//
//                origRef = posLink.PaymentResponse.RefNum;
//                transactionID = paymentDetails.getTransactionNo();
//
//                paymentDetails.setEntryType(entryMode);
//                paymentDetails.setAuth_code(posLink.PaymentResponse.AuthCode);
//                paymentDetails.setRef_num(posLink.PaymentResponse.RefNum);
//                //paymentDetails.setAio_accountid(object.getJSONObject("packetData").optString("accountId"));
//                paymentDetails.setResult_message(posLink.PaymentResponse.Message);
//
//                //paymentDetails.setIntegrator_version(object.getJSONObject("packetData").optString("integratorVersion"));
//                //paymentDetails.setFw_version(object.getJSONObject("packetData").optString("FWVersion"));
//                paymentDetails.setPay_response(new Gson().toJson(posLink.PaymentResponse));
//                paymentDetails.setCardHolder(holderName);
//                //paymentDetails.setEmvTagData(object.getJSONObject("packetData").optString("receiptEmvTagMap"));
//                paymentDetails.setRawData(new Gson().toJson(posLink.PaymentResponse));
//                paymentDetails.setPaymentBeforeSurcharge(paymentBeforeSurcharge);
//                paymentDetails.setTipBeforeSurcharge(tipBeforeSurcharge);
//                paymentDetails.setSurchargeAmount(surchargeAmount);
//                paymentDetails.setSurchargeCardType(surchargeCardType);
//
//                paymentDetails.setToken(cardToken);
//                paymentDetails.setExp_date(this.expDate);
//
//                if(getSessionManager().getEPEnabled()) {
//
//                    PaymentDecision des = new PaymentDecision();
//                    des.setOrderId(getEnroll().getEpOrderId());
//                    des.setOrderPaymentId(cardRequest.getOrderPaymentId());
//
//                    des.setAmountApproved(Double.parseDouble(paymentDetails.getAmount()));
//                    des.setAmountGratuity(Double.parseDouble(paymentDetails.getTipAmount()));
//                    des.setReferenceIdent(paymentDetails.getTransactionNo());
//                    des.setVoidIdent(posLink.PaymentResponse.HostCode);
//
//                    ApprovalInformation approvalInformation= new ApprovalInformation();
//                    approvalInformation.SetAuthCode(posLink.PaymentResponse.AuthCode);
//                    approvalInformation.SetRefNum(posLink.PaymentResponse.RefNum);
//
//                    des.setApprovalIdent(new Gson().toJson(approvalInformation));
//
//                    CardInformation cardInformation = new CardInformation();
//                    cardInformation.setNameOnCard(holderName);
//                    cardInformation.setPANFirstSix(f6);
//                    cardInformation.setPANLastFour(l4);
//                    if(TextUtils.isEmpty(cardToken)) {
//                        cardInformation.setUniqueCardIdent(token);
//                    } else {
//                        cardInformation.setUniqueCardIdent(cardToken);
//                    }
//                    cardInformation.setCardType(cardType);
//                    cardInformation.setPaymentType(8); // TODO: fix to be correct value based on return
//
//                    des.setCardInformation(cardInformation);
//
//                    System.out.println("<><> " + new Gson().toJson(des));
//
//                    getEnrollPayApi().paymentDecision(getSessionManager().getEPJWT(), des).enqueue(new Callback<String>() {
//                        @Override
//                        public void onResponse(Call<String> call, Response<String> response) {
//                            try {
//                                JSONObject object = new JSONObject(response.body());
//                                processEnrollCommand(object, total, tip);
//                            } catch (Exception e) {
//                                Timber.w(e);
//                            }
//                        }
//
//                        @Override
//                        public void onFailure(Call<String> call, Throwable t) {
//
//                        }
//                    });
//
//                }
//
//                authDetails = paymentDetails;
//                observeData(paymentDetails);
//            } else {
//
//                PaymentDetails paymentDetails = new PaymentDetails();
//                paymentDetails.setState(PaymentDetails.State.DECLINED);
//                paymentDetails.setResult_code(posLink.PaymentResponse.ResultCode);
//                paymentDetails.setResult_message(posLink.PaymentResponse.ResultTxt);
//                paymentDetails.setResult_detailedMessage(posLink.PaymentResponse.Message);
//                paymentDetails.setRawData(new Gson().toJson(posLink.PaymentResponse));
//                paymentDetails.setAmount(total + "");
//                paymentDetails.setTipAmount(tip + "");
//                observeData(paymentDetails);
//            }
//
//        } else { // Easy Fail
//
//            PaymentDetails paymentDetails = new PaymentDetails();
//            paymentDetails.setState(PaymentDetails.State.DECLINED);
//            paymentDetails.setResult_code(posLink.PaymentResponse.ResultCode);
//            paymentDetails.setResult_message(posLink.PaymentResponse.ResultTxt);
//            paymentDetails.setResult_detailedMessage(posLink.PaymentResponse.Message);
//            paymentDetails.setRawData(new Gson().toJson(posLink.PaymentResponse));
//            paymentDetails.setAmount(total + "");
//            paymentDetails.setTipAmount(tip + "");
//            if (getSessionManager().isPayAppCatcher()) {
//                handleSaleAfterError(total, tip, getPendingId(), paymentDetails, AFTER_TOKENIZE_SALE);
//            } else {
//                observeData(paymentDetails);
//            }
//
//        }
//    }
//
//    ProcessTransResult authResult;
//    private void authPayment(String refNum, double total, double tip) {
//        new Thread(() -> {
//
//            long timeout = (getTriggeredTime().getTime() + TimeUnit.SECONDS.toMillis(getSessionManager().getTimeout())) - System.currentTimeMillis();
//
//            String ticket_id = getIntent().getStringExtra("ticket_id");
//            if (TextUtils.isEmpty(ticket_id)) {
//                ticket_id = "";
//            }
//
//            Timber.tag("PAX BroadPOS Payment").i("Starting Payment: %s %s %s %s", ticket_id, timeout, total, tip);
//
//            posLink = createAndValidatePOSLink();
//
//            Timber.tag("PAX BroadPOS Payment").i("Link Setup");
//            Timber.tag("PAX BroadPOS Payment").i("RefNum %s", ticket_id);
//
//            PaymentRequest paymentRequest = new PaymentRequest();
//            paymentRequest.TenderType = paymentRequest.ParseTenderType("CREDIT");
//            paymentRequest.TransType = paymentRequest.ParseTransType("POSTAUTH");
//
//            //paymentRequest.TransType = paymentRequest.ParseTransType("TOKENIZE");
//            long baseAmount = Math.round(total * 100);
//            paymentRequest.Amount = baseAmount + "";
//            paymentRequest.TipAmt = Math.round(tip * 100) + "";
//            paymentRequest.ECRRefNum = ticket_id.replace("-", "");
//            paymentRequest.OrigRefNum = origRef;
//            paymentRequest.ExtData = "<HRefNum>" + refNum + "</HRefNum>";
//
//            posLink.PaymentRequest = paymentRequest;
//
//            ProcessTransResult ptr;
//            if(authResult == null) {
//                Timber.tag("PAX BroadPOS Payment").i("Processing Auth %s", new Gson().toJson(posLink.PaymentRequest));
//                insertPaymentRequestResponse(new Gson().toJson(paymentRequest), "", "", "");
//                ptr = posLink.ProcessTrans();
//                authResult = ptr;
//                insertPaymentRequestResponse("", new Gson().toJson(posLink.PaymentResponse), "", "");
//            } else {
//                ptr = authResult;
//            }
//
//            if(ptr.Code == ProcessTransResult.ProcessTransResultCode.OK) { // Easy success
//
//                if(posLink.PaymentResponse != null && posLink.PaymentResponse.ResultCode != null && posLink.PaymentResponse.ResultCode.equalsIgnoreCase("000000")) {
//
//                    String status;
//                    if (getIntent().getBooleanExtra("wh_isRefund", false)) {
//                        status = "Refunded";
//                    } else {
//                        status = "Paid";
//                    }
//
//                    String holderName = "";
//                    String entryMode = "";
//                    String token = "";
//                    String href = null;
//                    int first6 = 0;
//                    String expDate = "";
//                    //Parse the extra data
//                    try {
//                        //String extraDataValue = "<Attributes>" + output.ExtData + "</Attributes>";
//                        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
//                        factory.setNamespaceAware(true);
//                        XmlPullParser xpp = factory.newPullParser();
//                        String currentTag = null;
//                        xpp.setInput( new StringReader( posLink.PaymentResponse.ExtData ) ); // pass input whatever xml you have
//                        int eventType = xpp.getEventType();
//                        while (eventType != XmlPullParser.END_DOCUMENT) {
//                            if(eventType == XmlPullParser.START_TAG) {
//                                currentTag = xpp.getName();
//                            } else if(eventType == XmlPullParser.END_TAG) {
//                                currentTag = null;
//                            } else if(eventType == XmlPullParser.TEXT) {
//                                String currentTagValue = xpp.getText();
//                                if (currentTag != null){
//                                    Timber.tag("PAX BroadPOS Payment").i("Tag Found: %s, %s", currentTag.toUpperCase(), currentTagValue);
//                                    switch (currentTag.toUpperCase()){
//                                        case "TOKEN":
//                                            //cardInformation.setUniqueCardIdent(currentTagValue);
//                                            token = currentTagValue;
//                                            cardToken = token;
//                                            break;
//                                        case "CARDBIN":
//
//                                            try {
//                                                first6 = Integer.parseInt(currentTagValue);
//                                            } catch (Exception e) {
//                                                Timber.w(e, "Exception getting first6");
//                                            }
//                                            break;
//                                        case "PLNAMEONCARD":
//                                            holderName = currentTagValue;
//                                            break;
//                                        case "EXPDATE":
//                                            expDate = currentTagValue;
//                                            this.expDate = expDate;
//                                            break;
//                                        case "PLENTRYMODE":
//                                            entryMode = currentTagValue;
//                                            break;
//                                        case "HREF":
//                                            href = currentTagValue;
//                                            break;
//                                    }
//                                }
//                            }
//                            eventType = xpp.next();
//                        }
//
//                    } catch (XmlPullParserException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//                    int cardType;
//                    switch (posLink.PaymentResponse.CardType)
//                    {
//                        case "MASTERCARD":
//                        case "02":
//                        case "MASTERCARDFLEET":
//                            cardType = 3;
//                            break;
//                        case "VISA":
//                        case "01":
//                        case "VISAFLEET":
//                        case "09":
//                            cardType = 2;
//                            break;
//                        case "AMEX":
//                        case "03":
//                            cardType = 1;
//                            break;
//                        case "DISCOVER":
//                        case "04":
//                            cardType = 34;
//                            break;
//                        case "DINERCLUB":
//                        case "05":
//                            cardType = 10;
//                            break;
//                        case "JCB":
//                        case "07":
//                            cardType = 11;
//                            break;
//                        default:
//                            cardType = 13;
//                            break;
//                    }
//
//                    if (TextUtils.isEmpty(token))
//                    {
//                        int last = 0;
//                        try {
//                            last = Integer.parseInt( posLink.PaymentResponse.BogusAccountNum);
//                        } catch (Exception e) {
//                            Timber.w(e, "Exception getting last4");
//                        }
//                        token = first6 + last + expDate + holderName + cardType;
//                        //cardInformation.setUniqueCardIdent(cardInformation.getPANFirstSix() + cardInformation.getPANLastFour() + expDate + cardInformation.getNameOnCard() + cardInformation.getCardType() );
//                    }
//
//                    PaymentDetails paymentDetails = new PaymentDetails();
//                    paymentDetails.setState(PaymentDetails.State.INSERT_PAYMENT);
//                    paymentDetails.setAmount(posLink.PaymentResponse.ApprovedAmount);
//
//                    long approvedAmount = Long.parseLong(posLink.PaymentResponse.ApprovedAmount);
//                    if (approvedAmount > baseAmount) {
//                        paymentDetails.setTipAmount(approvedAmount - baseAmount + "");
//                    } else {
//                        paymentDetails.setTipAmount("0");
//                    }
//                    paymentDetails.setResult_code(posLink.PaymentResponse.ResultCode);
//
//                    paymentDetails.setCardType(posLink.PaymentResponse.CardType);
//                    paymentDetails.setLast4(posLink.PaymentResponse.BogusAccountNum);
//                    paymentDetails.setStatus(status);
//                    paymentDetails.setPayment_status(posLink.PaymentResponse.HostResponse);
//                    if(TextUtils.isEmpty(href)) {
//                        paymentDetails.setHref(posLink.PaymentResponse.HostCode);
//                        paymentDetails.setTransactionNo(posLink.PaymentResponse.HostCode);
//                    } else {
//                        paymentDetails.setHref(href);
//                        paymentDetails.setTransactionNo(href);
//                    }
//
//                    origRef = posLink.PaymentResponse.RefNum;
//                    transactionID = paymentDetails.getTransactionNo();
//
//                    paymentDetails.setEntryType(entryMode);
//                    paymentDetails.setAuth_code(posLink.PaymentResponse.AuthCode);
//                    paymentDetails.setRef_num(posLink.PaymentResponse.RefNum);
//                    //paymentDetails.setAio_accountid(object.getJSONObject("packetData").optString("accountId"));
//                    paymentDetails.setResult_message(posLink.PaymentResponse.Message);
//
//                    //paymentDetails.setIntegrator_version(object.getJSONObject("packetData").optString("integratorVersion"));
//                    //paymentDetails.setFw_version(object.getJSONObject("packetData").optString("FWVersion"));
//                    paymentDetails.setPay_response(new Gson().toJson(posLink.PaymentResponse));
//                    paymentDetails.setCardHolder(holderName);
//                    //paymentDetails.setEmvTagData(object.getJSONObject("packetData").optString("receiptEmvTagMap"));
//                    paymentDetails.setRawData(new Gson().toJson(posLink.PaymentResponse));
//                    paymentDetails.setPaymentBeforeSurcharge(paymentBeforeSurcharge);
//                    paymentDetails.setTipBeforeSurcharge(tipBeforeSurcharge);
//                    paymentDetails.setSurchargeAmount(surchargeAmount);
//                    paymentDetails.setSurchargeCardType(surchargeCardType);
//
//                    paymentDetails.setToken(cardToken);
//                    paymentDetails.setExp_date(this.expDate);
//
//                    if(getSessionManager().getEPEnabled()) {
//
//                        PaymentDecision des = new PaymentDecision();
//                        des.setOrderId(getEnroll().getEpOrderId());
//                        des.setOrderPaymentId(cardRequest.getOrderPaymentId());
//
//                        des.setAmountApproved(Double.parseDouble(paymentDetails.getAmount()));
//                        des.setAmountGratuity(Double.parseDouble(paymentDetails.getTipAmount()));
//                        des.setReferenceIdent(paymentDetails.getTransactionNo());
//                        des.setVoidIdent(posLink.PaymentResponse.HostCode);
//
//                        ApprovalInformation approvalInformation= new ApprovalInformation();
//                        approvalInformation.SetAuthCode(posLink.PaymentResponse.AuthCode);
//                        approvalInformation.SetRefNum(posLink.PaymentResponse.RefNum);
//
//                        des.setApprovalIdent(new Gson().toJson(approvalInformation));
//
//                        /*CardInformation cardInformation = new CardInformation();
//                        cardInformation.setNameOnCard(holderName);
//                        cardInformation.setPANFirstSix(first6);
//                        cardInformation.setPANLastFour(Integer.parseInt( posLink.PaymentResponse.BogusAccountNum));
//                        cardInformation.setUniqueCardIdent(token);
//                        cardInformation.setCardType(cardType);*/
//                        //cardInformation.setPaymentType(8); // TODO: fix to be correct value based on return
//
//                        //des.setCardInformation(cardInformation);
//
//                        System.out.println("<><> " + new Gson().toJson(des));
//
//                        getEnrollPayApi().paymentDecision(getSessionManager().getEPJWT(), des).enqueue(new Callback<String>() {
//                            @Override
//                            public void onResponse(Call<String> call, Response<String> response) {
//                                try {
//                                    JSONObject object = new JSONObject(response.body());
//                                    processEnrollCommand(object, total, tip);
//                                } catch (Exception e) {
//                                    Timber.w(e);
//                                }
//                            }
//
//                            @Override
//                            public void onFailure(Call<String> call, Throwable t) {
//
//                            }
//                        });
//
//                    }
//
//                    authDetails = paymentDetails;
//                    //observeData(paymentDetails);
//                } else {
//
//                    PaymentDetails paymentDetails = new PaymentDetails();
//                    paymentDetails.setState(PaymentDetails.State.DECLINED);
//                    paymentDetails.setResult_code(posLink.PaymentResponse.ResultCode);
//                    paymentDetails.setResult_message(posLink.PaymentResponse.ResultTxt);
//                    paymentDetails.setResult_detailedMessage(posLink.PaymentResponse.Message);
//                    paymentDetails.setRawData(new Gson().toJson(posLink.PaymentResponse));
//                    paymentDetails.setAmount(total + "");
//                    paymentDetails.setTipAmount(tip + "");
//                    observeData(paymentDetails);
//                }
//
//            } else { // Easy Fail
//
//                PaymentDetails paymentDetails = new PaymentDetails();
//                paymentDetails.setState(PaymentDetails.State.DECLINED);
//                paymentDetails.setResult_code(posLink.PaymentResponse.ResultCode);
//                paymentDetails.setResult_message(posLink.PaymentResponse.ResultTxt);
//                paymentDetails.setResult_detailedMessage(posLink.PaymentResponse.Message);
//                paymentDetails.setRawData(new Gson().toJson(posLink.PaymentResponse));
//                paymentDetails.setAmount(total + "");
//                paymentDetails.setTipAmount(tip + "");
//
//                if (getSessionManager().isPayAppCatcher()) {
//                    handleSaleAfterError(total, tip, getPendingId(), paymentDetails, AFTER_AUTH_PAYMENT);
//                } else {
//                    observeData(paymentDetails);
//                }
//
//            }
//
//        }).start();
//    }
//
//    private void initiatePayment(double total, double tip) {
//
//        new Thread(() -> {
//
//            long timeout = (getTriggeredTime().getTime() + TimeUnit.SECONDS.toMillis(getSessionManager().getTimeout())) - System.currentTimeMillis();
//
//            String ticket_id = getIntent().getStringExtra("ticket_id");
//            if (TextUtils.isEmpty(ticket_id)) {
//                ticket_id = "";
//            }
//
//            Timber.tag("PAX BroadPOS Payment").i("Starting Payment: %s %s %s %s", ticket_id, timeout, total, tip);
//
//            posLink = createAndValidatePOSLink();
//            boolean isSaleRequest = false;
//
//            Timber.tag("PAX BroadPOS Payment").i("Link Setup");
//
//            PaymentRequest paymentRequest = new PaymentRequest();
//            paymentRequest.TenderType = paymentRequest.ParseTenderType("CREDIT");
//            if(isCredit){
//                paymentRequest.TransType = paymentRequest.ParseTransType("RETURN");
//            }else if(getSessionManager().getEPEnabled()) {
//                paymentRequest.TransType = paymentRequest.ParseTransType("AUTH");
//            } else {
//                isSaleRequest = true;
//                paymentRequest.TransType = paymentRequest.ParseTransType("SALE");
//                if (getSessionManager().isSaleRequestToken()) {
//                    paymentRequest.ExtData = "<TokenRequest>1</TokenRequest>";
//                }
//                if (getSessionManager().getTips() == Tips.BEFORE && getSessionManager().getProcessor().equalsIgnoreCase("Heartland")) {
//                    if (TextUtils.isEmpty(paymentRequest.ExtData)) {
//                        paymentRequest.ExtData = "<TipRequest>1</TipRequest>";
//                    } else {
//                        paymentRequest.ExtData += "<TipRequest>1</TipRequest>";
//                    }
//                }
//            }
//            //paymentRequest.TransType = paymentRequest.ParseTransType("TOKENIZE");
//
//            Timber.tag("PAX BroadPOS Payment").i("RefNum " + getPendingId() + "; " + getTransactionNo());
//
//            paymentRequest.Amount = Math.round(total * 100) + "";
//            paymentRequest.TipAmt = Math.round(tip * 100) + "";
//            paymentRequest.ECRRefNum = getPendingId();
//            //paymentRequest.ECRRefNum = "000000";
//            //paymentRequest.ECRTransID = getTransactionNo();
//            paymentRequest.InvNum = getIntent().getStringExtra("webhook_id");
//            //paymentRequest.PONum = getIntent().getStringExtra("webhook_id");
//
//            posLink.PaymentRequest = paymentRequest;
//
//            Timber.tag("PAX BroadPOS Payment").i("Processing  %s", new Gson().toJson(posLink.PaymentRequest));
//            insertPaymentRequestResponse(new Gson().toJson(paymentRequest), "", "", "");
//            ProcessTransResult ptr = posLink.ProcessTrans();
//
//            Timber.tag("PAX BroadPOS Payment").i("Payment Finished: " + ptr.Code + ": " + ptr.Msg);
//            Timber.tag("PAX BroadPOS Payment").i("Payment Finished: %s", new Gson().toJson(posLink.PaymentResponse));
//
//            //if (false) {//hardcode to test catcher
//            if(ptr.Code == ProcessTransResult.ProcessTransResultCode.OK) { // Easy success
//                insertPaymentRequestResponse("", new Gson().toJson(posLink.PaymentResponse), "", "");
//
//                if(getSessionManager().getTips() == Tips.BEFORE &&
//                        getSessionManager().getProcessor().equalsIgnoreCase("CyberSource") && tip == 0) {
//                    cyberSourceFinishTransaction(posLink.PaymentResponse.RefNum);
//                }
//
//                PaymentDetails paymentDetails = new PaymentDetails();
//                String holderName = "";
//                String entryMode = "";
//                String token = "";
//                String href = null;
//                String pay_tip = Math.round(tip * 100) + "";
//                int first6 = 0;
//                String expDate = "";
//                //Parse the extra data
//                try {
//                    //String extraDataValue = "<Attributes>" + output.ExtData + "</Attributes>";
//                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
//                    factory.setNamespaceAware(true);
//                    XmlPullParser xpp = factory.newPullParser();
//                    String currentTag = null;
//                    xpp.setInput( new StringReader( posLink.PaymentResponse.ExtData ) ); // pass input whatever xml you have
//                    int eventType = xpp.getEventType();
//                    while (eventType != XmlPullParser.END_DOCUMENT) {
//                        if(eventType == XmlPullParser.START_TAG) {
//                            currentTag = xpp.getName();
//                        } else if(eventType == XmlPullParser.END_TAG) {
//                            currentTag = null;
//                        } else if(eventType == XmlPullParser.TEXT) {
//                            String currentTagValue = xpp.getText();
//                            if (currentTag != null){
//                                Timber.tag("PAX BroadPOS Payment").i("Tag Found: %s, %s", currentTag.toUpperCase(), currentTagValue);
//                                switch (currentTag.toUpperCase()){
//                                    case "TIPAMOUNT":
//                                        if(getSessionManager().getTips() == Tips.BEFORE && getSessionManager().getProcessor().equalsIgnoreCase("Heartland")) {
//                                            pay_tip = currentTagValue;
//                                        }
//                                        break;
//                                    case "TOKEN":
//                                        //cardInformation.setUniqueCardIdent(currentTagValue);
//                                        token = currentTagValue;
//                                        cardToken = token;
//                                        break;
//                                    case "CARDBIN":
//
//                                        try {
//                                            first6 = Integer.parseInt(currentTagValue);
//                                        } catch (Exception e) {
//                                            Timber.w(e, "Exception getting first6");
//                                        }
//                                        break;
//                                    case "PLNAMEONCARD":
//                                        holderName = currentTagValue;
//                                        break;
//                                    case "EXPDATE":
//                                        expDate = currentTagValue;
//                                        this.expDate = expDate;
//                                        break;
//                                    case "PLENTRYMODE":
//                                        entryMode = currentTagValue;
//                                        break;
//                                    case "HREF":
//                                        href = currentTagValue;
//                                        break;
//                                }
//                            }
//                        }
//                        eventType = xpp.next();
//                    }
//
//                } catch (XmlPullParserException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                paymentDetails.setEntryType(entryMode);
//                paymentDetails.setAuth_code(posLink.PaymentResponse.AuthCode);
//                paymentDetails.setRef_num(posLink.PaymentResponse.RefNum);
//                paymentDetails.setResult_message(posLink.PaymentResponse.Message);
//                paymentDetails.setResult_detailedMessage(posLink.PaymentResponse.Message);
//                paymentDetails.setResult_code(posLink.PaymentResponse.ResultCode);
//                paymentDetails.setCardType(posLink.PaymentResponse.CardType);
//                paymentDetails.setLast4(posLink.PaymentResponse.BogusAccountNum);
//                paymentDetails.setCardHolder(holderName);
//                if(TextUtils.isEmpty(href)) {
//                    paymentDetails.setHref(posLink.PaymentResponse.HostCode);
//                    paymentDetails.setTransactionNo(posLink.PaymentResponse.HostCode);
//                } else {
//                    paymentDetails.setHref(href);
//                    paymentDetails.setTransactionNo(href);
//                }
//                paymentDetails.setToken(cardToken);
//                paymentDetails.setExp_date(this.expDate);
//
//                try {
//                    paymentDetails.setPay_response(new Gson().toJson(posLink.PaymentResponse));
//                    paymentDetails.setRawData(new Gson().toJson(posLink.PaymentResponse));
//                } catch (Exception e) {
//                    Timber.w(e, "PaymentResponse to Gson failed");
//                }
//
//                if(posLink.PaymentResponse.ResultCode.equalsIgnoreCase("000000")) {
//                    String status;
//                    if (getIntent().getBooleanExtra("wh_isRefund", false)) {
//                        status = "Refunded";
//                    } else {
//                        status = "Paid";
//                    }
//
//                    int cardType;
//                    switch (posLink.PaymentResponse.CardType)
//                    {
//                        case "MASTERCARD":
//                        case "02":
//                        case "MASTERCARDFLEET":
//                            cardType = 3;
//                            break;
//                        case "VISA":
//                        case "01":
//                        case "VISAFLEET":
//                        case "09":
//                            cardType = 2;
//                            break;
//                        case "AMEX":
//                        case "03":
//                            cardType = 1;
//                            break;
//                        case "DISCOVER":
//                        case "04":
//                            cardType = 34;
//                            break;
//                        case "DINERCLUB":
//                        case "05":
//                            cardType = 10;
//                            break;
//                        case "JCB":
//                        case "07":
//                            cardType = 11;
//                            break;
//                        default:
//                            cardType = 13;
//                            break;
//                    }
//
//                    if (TextUtils.isEmpty(token))
//                    {
//                        int last = 0;
//                        try {
//                            last = Integer.parseInt( posLink.PaymentResponse.BogusAccountNum);
//                        } catch (Exception e) {
//                            Timber.w(e, "Exception getting last4");
//                        }
//                        token = first6 + last + expDate + holderName + cardType;
//                        //cardInformation.setUniqueCardIdent(cardInformation.getPANFirstSix() + cardInformation.getPANLastFour() + expDate + cardInformation.getNameOnCard() + cardInformation.getCardType() );
//                    }
//
//                    paymentDetails.setState(PaymentDetails.State.INSERT_PAYMENT);
//                    if(pay_tip != null) {
//                        try {
//                            if(Long.parseLong(pay_tip)>0) {
//                                Long realAmount = Long.parseLong(posLink.PaymentResponse.ApprovedAmount) - Long.parseLong(pay_tip);
//                                paymentDetails.setAmount(realAmount + "");
//                            } else {
//                                paymentDetails.setAmount(posLink.PaymentResponse.ApprovedAmount);
//                            }
//                        }catch (Exception e){
//                            paymentDetails.setAmount(posLink.PaymentResponse.ApprovedAmount);
//                        }
//                    }
//                    else {
//                        paymentDetails.setAmount(posLink.PaymentResponse.ApprovedAmount);
//                    }
//                    paymentDetails.setTipAmount(pay_tip);
//                    paymentDetails.setStatus(status);
//                    paymentDetails.setPayment_status(posLink.PaymentResponse.HostResponse);
//
//                    origRef = posLink.PaymentResponse.RefNum;
//                    transactionID = paymentDetails.getTransactionNo();
//
//                    paymentDetails.setPaymentBeforeSurcharge(paymentBeforeSurcharge);
//                    paymentDetails.setTipBeforeSurcharge(tipBeforeSurcharge);
//                    paymentDetails.setSurchargeAmount(surchargeAmount);
//                    paymentDetails.setSurchargeCardType(surchargeCardType);
//                    paymentDetails.setCredit(isCredit);
//
//                    if(getSessionManager().getEPEnabled() && !isCredit) {
//
//                        CardPresented pre = new CardPresented();
//                        pre.setOrderId(getEnroll().getEpOrderId());
//                        pre.setOrderPaymentId(cardRequest.getOrderPaymentId());
//                        pre.setCardType(cardType);
//                        pre.setNameOnCard(holderName);
//                        pre.setPANFirstSix(first6);
//                        pre.setPANLastFour(Integer.parseInt( posLink.PaymentResponse.BogusAccountNum));
//                        pre.setUniqueCardIdent(token);
//
//                        System.out.println("<><> " + new Gson().toJson(pre));
//
//                        getEnrollPayApi().transctionCardPresented(getSessionManager().getEPJWT(), pre).enqueue(new Callback<String>() {
//                            @Override
//                            public void onResponse(Call<String> call, Response<String> response) {
//                                try {
//                                    JSONObject object = new JSONObject(response.body());
//                                    processEnrollCommand(object, total, tip);
//                                } catch (Exception e) {
//                                    Timber.w(e);
//                                }
//                            }
//
//                            @Override
//                            public void onFailure(Call<String> call, Throwable t) {
//
//                            }
//                        });
//
//                    } else {
//                        observeData(paymentDetails);
//                    }
//
//                } else {
//                    paymentDetails.setState(PaymentDetails.State.DECLINED);
//                    paymentDetails.setResult_message(posLink.PaymentResponse.ResultTxt);
//                    paymentDetails.setResult_detailedMessage(posLink.PaymentResponse.Message);
//                    paymentDetails.setAmount(total + "");
//                    paymentDetails.setTipAmount(tip + "");
//                    observeData(paymentDetails);
//                }
//
//            } else { // Easy Fail
//                insertPaymentRequestResponse("", "Declined: " + ptr.Code + " - " + ptr.Msg, "", "");
//                PaymentDetails paymentDetails = new PaymentDetails();
//                paymentDetails.setState(PaymentDetails.State.DECLINED);
//                paymentDetails.setResult_code("-5-" + ptr.Code);
//                paymentDetails.setResult_message("Transaction Failed: " + ptr.Msg);
//                paymentDetails.setRawData(new Gson().toJson(posLink.PaymentResponse));
//                paymentDetails.setAmount(total + "");
//                paymentDetails.setTipAmount(tip + "");
//                if (getSessionManager().isPayAppCatcher() && isSaleRequest) {
//                    handleSaleAfterError(total, tip, getPendingId(), paymentDetails, AFTER_SALE);
//                } else {
//                    observeData(paymentDetails);
//                }
//            }
//
//        }).start();
//    }
//
//    private void handleSaleAfterError(double total, double tip, String ECRRefNum, PaymentDetails originalResult, int which) {
//        new Thread(() -> {
//            posLink = createAndValidatePOSLink(true);
//            ReportRequest reportRequest = new ReportRequest();
//            reportRequest.EDCType = reportRequest.ParseEDCType("ALL");
//            reportRequest.TransType = reportRequest.ParseTransType("LOCALDETAILREPORT");
//            reportRequest.ECRRefNum = ECRRefNum;
//            reportRequest.LastTransaction = "1";
//
//            posLink.ReportRequest = reportRequest;
//
//            ProcessTransResult ptr = posLink.ProcessTrans();
//
//            try {
//                Timber.tag("PAX BroadPOS LocalRpt").i("Request " + new Gson().toJson(posLink.ReportRequest));
//            } catch (Exception e) {
//                Timber.tag("PAX BroadPOS LocalRpt").w(e, "Request log failed");
//            }
//
//            try {
//                Timber.tag("PAX BroadPOS LocalRpt").i("Done " + new Gson().toJson(ptr));
//            } catch (Exception e) {
//                Timber.tag("PAX BroadPOS LocalRpt").w(e, "Result log failed");
//            }
//
//            boolean isPaymentSuccessful;
//            if (ptr.Code != ProcessTransResult.ProcessTransResultCode.OK) {
//                observeData(originalResult);
//            } else if (posLink.ReportResponse != null) {
//                try {
//                    Timber.tag("PAX BroadPOS LocalRpt").i("ReportResponse: " + new Gson().toJson(posLink.ReportResponse));
//                    insertPaymentRequestResponse("", new Gson().toJson(posLink.ReportResponse), "", "");
//                } catch (Exception e) {
//                    Timber.tag("PAX BroadPOS LocalRpt").w(e, "Result log failed");
//                }
//                isPaymentSuccessful = posLink.ReportResponse.ResultCode.equalsIgnoreCase("000000");
//                if (posLink.ReportResponse.ResultCode.equalsIgnoreCase("100023")
//                    /*&& posLink.ReportResponse.ResultTxt.equalsIgnoreCase("NOT FOUND")*/) {
//                    Timber.tag("PAX BroadPOS LocalRpt").i("Error on ECRRefNum " + ECRRefNum + ", resultTxt: " + posLink.ReportResponse.ResultTxt);
//                }
//
//                //ER 02-06-2021: When sending LastTransaction=1, it ignores ECRRefNum so the following validation is needed to make sure the LastTransaction is really the one for the ECRRefNum we're looking for.
//                if (!ECRRefNum.equalsIgnoreCase(posLink.ReportResponse.ECRRefNum)) {
//                    Timber.tag("PayAppCatcher").i("No Transaction found for ECRRefNum ["+ECRRefNum+"], last transaction is for ECRRefNum ["+posLink.ReportResponse.ECRRefNum+"]");
//                    isPaymentSuccessful = false;
//                }
//
//                if (!isPaymentSuccessful) {
//                    observeData(originalResult);
//                } else {
//                    String status;
//                    if (getIntent().getBooleanExtra("wh_isRefund", false)) {
//                        status = "Refunded";
//                    } else {
//                        status = "Paid";
//                    }
//
//                    String holderName = "";
//                    String entryMode = "";
//                    String token = "";
//                    String href = null;
//                    String pay_tip = Math.round(tip * 100) + "";
//                    int first6 = 0;
//                    String expDate = "";
//                    //Parse the extra data
//                    try {
//                        //String extraDataValue = "<Attributes>" + output.ExtData + "</Attributes>";
//                        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
//                        factory.setNamespaceAware(true);
//                        XmlPullParser xpp = factory.newPullParser();
//                        String currentTag = null;
//                        xpp.setInput( new StringReader( posLink.ReportResponse.ExtData ) ); // pass input whatever xml you have
//                        int eventType = xpp.getEventType();
//                        while (eventType != XmlPullParser.END_DOCUMENT) {
//                            if(eventType == XmlPullParser.START_TAG) {
//                                currentTag = xpp.getName();
//                            } else if(eventType == XmlPullParser.END_TAG) {
//                                currentTag = null;
//                            } else if(eventType == XmlPullParser.TEXT) {
//                                String currentTagValue = xpp.getText();
//                                if (currentTag != null){
//                                    Timber.tag("PAX BroadPOS Payment").i("Tag Found: %s, %s", currentTag.toUpperCase(), currentTagValue);
//                                    switch (currentTag.toUpperCase()){
//                                        case "TIPAMOUNT":
//                                            if(getSessionManager().getTips() == Tips.BEFORE && getSessionManager().getProcessor().equalsIgnoreCase("Heartland")) {
//                                                pay_tip = currentTagValue;
//                                            }
//                                            break;
//                                        case "TOKEN":
//                                            //cardInformation.setUniqueCardIdent(currentTagValue);
//                                            token = currentTagValue;
//                                            cardToken = token;
//                                            break;
//                                        case "CARDBIN":
//
//                                            try {
//                                                first6 = Integer.parseInt(currentTagValue);
//                                            } catch (Exception e) {
//                                                Timber.w(e, "Exception getting first6");
//                                            }
//                                            break;
//                                        case "PLNAMEONCARD":
//                                            holderName = currentTagValue;
//                                            break;
//                                        case "EXPDATE":
//                                            expDate = currentTagValue;
//                                            this.expDate = expDate;
//                                            break;
//                                        case "PLENTRYMODE":
//                                            entryMode = currentTagValue;
//                                            if(which == AFTER_DO_POST_AUTH){
//                                                entryMode = preentrytype;
//                                            }else {
//                                                preentrytype = entryMode;
//                                            }
//                                            break;
//                                        case "HREF":
//                                            href = currentTagValue;
//                                            break;
//                                    }
//                                }
//                            }
//                            eventType = xpp.next();
//                        }
//
//                    } catch (XmlPullParserException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//                    int cardType;
//                    switch (posLink.ReportResponse.CardType)
//                    {
//                        case "MASTERCARD":
//                        case "02":
//                        case "MASTERCARDFLEET":
//                            cardType = 3;
//                            break;
//                        case "VISA":
//                        case "01":
//                        case "VISAFLEET":
//                        case "09":
//                            cardType = 2;
//                            break;
//                        case "AMEX":
//                        case "03":
//                            cardType = 1;
//                            break;
//                        case "DISCOVER":
//                        case "04":
//                            cardType = 34;
//                            break;
//                        case "DINERCLUB":
//                        case "05":
//                            cardType = 10;
//                            break;
//                        case "JCB":
//                        case "07":
//                            cardType = 11;
//                            break;
//                        default:
//                            cardType = 13;
//                            break;
//                    }
//
//                    if (TextUtils.isEmpty(token))
//                    {
//                        int last = 0;
//                        try {
//                            last = Integer.parseInt( posLink.ReportResponse.BogusAccountNum);
//                        } catch (Exception e) {
//                            Timber.w(e, "Exception getting last4");
//                        }
//                        token = first6 + last + expDate + holderName + cardType;
//                        //cardInformation.setUniqueCardIdent(cardInformation.getPANFirstSix() + cardInformation.getPANLastFour() + expDate + cardInformation.getNameOnCard() + cardInformation.getCardType() );
//                    }
//
//                    PaymentDetails paymentDetails = new PaymentDetails();
//                    paymentDetails.setState(PaymentDetails.State.INSERT_PAYMENT);
//                    if(pay_tip != null) {
//                        try {
//                            if(Long.parseLong(pay_tip)>0) {
//                                Long realAmount = Long.parseLong(posLink.ReportResponse.ApprovedAmount) - Long.parseLong(pay_tip);
//                                paymentDetails.setAmount(realAmount + "");
//                            } else {
//                                paymentDetails.setAmount(posLink.ReportResponse.ApprovedAmount);
//                            }
//                        }catch (Exception e){
//                            paymentDetails.setAmount(posLink.ReportResponse.ApprovedAmount);
//                        }
//                    }
//                    else {
//                        paymentDetails.setAmount(posLink.ReportResponse.ApprovedAmount);
//                    }
//                    paymentDetails.setTipAmount(pay_tip);
//                    paymentDetails.setResult_code(posLink.ReportResponse.ResultCode);
//
//                    paymentDetails.setCardType(posLink.ReportResponse.CardType);
//                    paymentDetails.setLast4(posLink.ReportResponse.BogusAccountNum);
//                    paymentDetails.setStatus(status);
//                    paymentDetails.setPayment_status(posLink.ReportResponse.HostResponse);
//                    if(TextUtils.isEmpty(href)) {
//                        paymentDetails.setHref(posLink.ReportResponse.HostCode);
//                        paymentDetails.setTransactionNo(posLink.ReportResponse.HostCode);
//                    } else {
//                        paymentDetails.setHref(href);
//                        paymentDetails.setTransactionNo(href);
//                    }
//
//                    origRef = posLink.ReportResponse.RefNum;
//                    transactionID = paymentDetails.getTransactionNo();
//
//                    paymentDetails.setEntryType(entryMode);
//                    paymentDetails.setAuth_code(posLink.ReportResponse.AuthCode);
//                    paymentDetails.setRef_num(posLink.ReportResponse.RefNum);
//                    //paymentDetails.setAio_accountid(object.getJSONObject("packetData").optString("accountId"));
//                    paymentDetails.setResult_message(posLink.ReportResponse.Message);
//
//                    //paymentDetails.setIntegrator_version(object.getJSONObject("packetData").optString("integratorVersion"));
//                    //paymentDetails.setFw_version(object.getJSONObject("packetData").optString("FWVersion"));
//                    paymentDetails.setPay_response(new Gson().toJson(posLink.ReportResponse));
//                    paymentDetails.setCardHolder(holderName);
//                    //paymentDetails.setEmvTagData(object.getJSONObject("packetData").optString("receiptEmvTagMap"));
//                    paymentDetails.setRawData(new Gson().toJson(posLink.ReportResponse));
//
//                    paymentDetails.setPaymentBeforeSurcharge(paymentBeforeSurcharge);
//                    paymentDetails.setTipBeforeSurcharge(tipBeforeSurcharge);
//                    paymentDetails.setSurchargeAmount(surchargeAmount);
//                    paymentDetails.setSurchargeCardType(surchargeCardType);
//
//                    paymentDetails.setToken(cardToken);
//                    paymentDetails.setExp_date(this.expDate);
//
//                    switch (which){
//                        case AFTER_SALE:
//                            if(getSessionManager().getEPEnabled()) {
//
//                                CardPresented pre = new CardPresented();
//                                pre.setOrderId(getEnroll().getEpOrderId());
//                                pre.setOrderPaymentId(cardRequest.getOrderPaymentId());
//                                pre.setCardType(cardType);
//                                pre.setNameOnCard(holderName);
//                                pre.setPANFirstSix(first6);
//                                pre.setPANLastFour(Integer.parseInt( posLink.ReportResponse.BogusAccountNum));
//                                pre.setUniqueCardIdent(token);
//
//                                System.out.println("<><> " + new Gson().toJson(pre));
//
//                                getEnrollPayApi().transctionCardPresented(getSessionManager().getEPJWT(), pre).enqueue(new Callback<String>() {
//                                    @Override
//                                    public void onResponse(Call<String> call, Response<String> response) {
//                                        try {
//                                            JSONObject object = new JSONObject(response.body());
//                                            processEnrollCommand(object, total, tip);
//                                        } catch (Exception e) {
//                                            Timber.w(e);
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onFailure(Call<String> call, Throwable t) {
//
//                                    }
//                                });
//
//                            } else {
//                                observeData(paymentDetails);
//                            }
//                            break;
//                        case AFTER_AUTH_PAYMENT:
//                            paymentDetails.setAmount(posLink.ReportResponse.ApprovedAmount);
//                            long approvedAmount = Long.parseLong(posLink.ReportResponse.ApprovedAmount);
//                            if (approvedAmount > Math.round(total * 100)) {
//                                paymentDetails.setTipAmount(approvedAmount - Math.round(total * 100) + "");
//                            } else {
//                                paymentDetails.setTipAmount("0");
//                            }
//                            if(getSessionManager().getEPEnabled()) {
//
//                                PaymentDecision des = new PaymentDecision();
//                                des.setOrderId(getEnroll().getEpOrderId());
//                                des.setOrderPaymentId(cardRequest.getOrderPaymentId());
//
//                                des.setAmountApproved(Double.parseDouble(paymentDetails.getAmount()));
//                                des.setAmountGratuity(Double.parseDouble(paymentDetails.getTipAmount()));
//                                des.setReferenceIdent(paymentDetails.getTransactionNo());
//                                des.setVoidIdent(posLink.ReportResponse.HostCode);
//
//                                ApprovalInformation approvalInformation= new ApprovalInformation();
//                                approvalInformation.SetAuthCode(posLink.ReportResponse.AuthCode);
//                                approvalInformation.SetRefNum(posLink.ReportResponse.RefNum);
//
//                                des.setApprovalIdent(new Gson().toJson(approvalInformation));
//
//                                System.out.println("<><> " + new Gson().toJson(des));
//
//                                getEnrollPayApi().paymentDecision(getSessionManager().getEPJWT(), des).enqueue(new Callback<String>() {
//                                    @Override
//                                    public void onResponse(Call<String> call, Response<String> response) {
//                                        try {
//                                            JSONObject object = new JSONObject(response.body());
//                                            processEnrollCommand(object, total, tip);
//                                        } catch (Exception e) {
//                                            Timber.w(e);
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onFailure(Call<String> call, Throwable t) {
//
//                                    }
//                                });
//
//                            }
//
//                            authDetails = paymentDetails;
//                            break;
//                        case AFTER_TOKENIZE_SALE:
//                            paymentDetails.setAmount(posLink.ReportResponse.ApprovedAmount);
//                            approvedAmount = Long.parseLong(posLink.ReportResponse.ApprovedAmount);
//                            if (approvedAmount > Math.round(total * 100)) {
//                                paymentDetails.setTipAmount(approvedAmount - Math.round(total * 100) + "");
//                            } else {
//                                paymentDetails.setTipAmount("0");
//                            }
//                            if(getSessionManager().getEPEnabled()) {
//
//                                PaymentDecision des = new PaymentDecision();
//                                des.setOrderId(getEnroll().getEpOrderId());
//                                des.setOrderPaymentId(cardRequest.getOrderPaymentId());
//
//                                des.setAmountApproved(Double.parseDouble(paymentDetails.getAmount()));
//                                des.setAmountGratuity(Double.parseDouble(paymentDetails.getTipAmount()));
//                                des.setReferenceIdent(paymentDetails.getTransactionNo());
//                                des.setVoidIdent(posLink.ReportResponse.HostCode);
//
//                                ApprovalInformation approvalInformation= new ApprovalInformation();
//                                approvalInformation.SetAuthCode(posLink.ReportResponse.AuthCode);
//                                approvalInformation.SetRefNum(posLink.ReportResponse.RefNum);
//
//                                des.setApprovalIdent(new Gson().toJson(approvalInformation));
//
//                                CardInformation cardInformation = new CardInformation();
//                                cardInformation.setNameOnCard(holderName);
//                                cardInformation.setPANFirstSix(f6);
//                                cardInformation.setPANLastFour(l4);
//                                if(TextUtils.isEmpty(cardToken)) {
//                                    cardInformation.setUniqueCardIdent(token);
//                                } else {
//                                    cardInformation.setUniqueCardIdent(cardToken);
//                                }
//                                cardInformation.setCardType(cardType);
//                                cardInformation.setPaymentType(8); // TODO: fix to be correct value based on return
//
//                                des.setCardInformation(cardInformation);
//
//                                System.out.println("<><> " + new Gson().toJson(des));
//
//                                getEnrollPayApi().paymentDecision(getSessionManager().getEPJWT(), des).enqueue(new Callback<String>() {
//                                    @Override
//                                    public void onResponse(Call<String> call, Response<String> response) {
//                                        try {
//                                            JSONObject object = new JSONObject(response.body());
//                                            processEnrollCommand(object, total, tip);
//                                        } catch (Exception e) {
//                                            Timber.w(e);
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onFailure(Call<String> call, Throwable t) {
//
//                                    }
//                                });
//
//                            }
//
//                            authDetails = paymentDetails;
//                            observeData(paymentDetails);
//                            break;
//                        case AFTER_DO_AUTH:
//                            getPayROCInfo(posLink.ReportResponse.ApprovedAmount, pay_tip, first6);
//                            break;
//                        case AFTER_DO_POST_AUTH:
//                            observeData(paymentDetails);
//                            break;
//                    }
//
//                }
//            } else {
//                Timber.tag("PAX BroadPOS LocalRpt").i("ReportResponse is null.");
//                observeData(originalResult);
//            }
//
//        }).start();
//    }
//
//    private void doAuthPayment(double total, double tip){
//        new Thread(() -> {
//
//            long timeout = (getTriggeredTime().getTime() + TimeUnit.SECONDS.toMillis(getSessionManager().getTimeout())) - System.currentTimeMillis();
//
//            String ticket_id = getIntent().getStringExtra("ticket_id");
//            if (TextUtils.isEmpty(ticket_id)) {
//                ticket_id = "";
//            }
//
//            double amountTemp = total * 100;
//            double tipTemp = tip * 100;
//
//            String pay_amount = Math.round(total * 100) + "";
//            String pay_tip = Math.round(tip * 100) + "";
//
//            /*if(getSessionManager().getPayRoc().equalsIgnoreCase("Yes")){
//
//                // for Payroc or other preauth you cannot charge more, so we need to add in the possible surcharge here and charge less if its not needed
//                double surchargePCT = Double.parseDouble(getSessionManager().getSurchargeCcPct()) / 10000;
//
//                amountTemp += (amountTemp * surchargePCT);
//                tipTemp += (tipTemp * surchargePCT);
//
//                pay_amount = Math.round(amountTemp) + "";
//                pay_tip = Math.round(tipTemp) + "";
//            }*/
//
//            Timber.tag("PAX BroadPOS Payment").i("Starting Payment: %s %s %s %s", ticket_id, timeout, total, tip);
//
//            posLink = createAndValidatePOSLink();
//
//            Timber.tag("PAX BroadPOS Payment").i("Link Setup");
//
//            PaymentRequest paymentRequest = new PaymentRequest();
//            paymentRequest.TenderType = paymentRequest.ParseTenderType("CREDIT");
//            paymentRequest.TransType = paymentRequest.ParseTransType("AUTH");
//            //paymentRequest.TransType = paymentRequest.ParseTransType("TOKENIZE");
//
//            Timber.tag("PAX BroadPOS Payment").i("RefNum " + getPendingId() + "; " + getTransactionNo());
//
//            paymentRequest.Amount = pay_amount;
//            // Pre auth doesn't like this being a thing apparently... so just do the tip amount later.
//            paymentRequest.TipAmt = "0"; //pay_tip;
//            paymentRequest.ECRRefNum = getPendingId();
//            //paymentRequest.ECRRefNum = "000000";
//            //paymentRequest.ECRTransID = getTransactionNo();
//            paymentRequest.InvNum = getIntent().getStringExtra("webhook_id");
//            //paymentRequest.PONum = getIntent().getStringExtra("webhook_id");
//
//            posLink.PaymentRequest = paymentRequest;
//
//            Timber.tag("PAX BroadPOS Payment").i("Processing Auth Payment %s", new Gson().toJson(posLink.PaymentRequest));
//            insertPaymentRequestResponse(new Gson().toJson(paymentRequest), "", "", "");
//            ProcessTransResult ptr = posLink.ProcessTrans();
//            insertPaymentRequestResponse("", new Gson().toJson(posLink.PaymentResponse), "", "");
//
//            Timber.tag("PAX BroadPOS Payment").i("Payment Finished: " + ptr.Code + ": " + ptr.Msg);
//            Timber.tag("PAX BroadPOS Payment").i("Payment Finished: %s", new Gson().toJson(posLink.PaymentResponse));
//
//
//            if(ptr.Code == ProcessTransResult.ProcessTransResultCode.OK) { // Easy success
//
//                if(posLink.PaymentResponse.ResultCode.equalsIgnoreCase("000000")) {
//
//                    String status;
//                    if (getIntent().getBooleanExtra("wh_isRefund", false)) {
//                        status = "Refunded";
//                    } else {
//                        status = "Paid";
//                    }
//
//                    String holderName = "";
//                    String entryMode = "";
//                    String token = "";
//                    String href = null;
//                    int first6 = 0;
//                    String expDate = "";
//                    //Parse the extra data
//                    try {
//                        //String extraDataValue = "<Attributes>" + output.ExtData + "</Attributes>";
//                        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
//                        factory.setNamespaceAware(true);
//                        XmlPullParser xpp = factory.newPullParser();
//                        String currentTag = null;
//                        xpp.setInput( new StringReader( posLink.PaymentResponse.ExtData ) ); // pass input whatever xml you have
//                        int eventType = xpp.getEventType();
//                        while (eventType != XmlPullParser.END_DOCUMENT) {
//                            if(eventType == XmlPullParser.START_TAG) {
//                                currentTag = xpp.getName();
//                            } else if(eventType == XmlPullParser.END_TAG) {
//                                currentTag = null;
//                            } else if(eventType == XmlPullParser.TEXT) {
//                                String currentTagValue = xpp.getText();
//                                if (currentTag != null){
//                                    Timber.tag("PAX BroadPOS Payment").i("Tag Found: %s, %s", currentTag.toUpperCase(), currentTagValue);
//                                    switch (currentTag.toUpperCase()){
//                                        case "TIPAMOUNT":
//                                            if(getSessionManager().getTips() == Tips.BEFORE && getSessionManager().getProcessor().equalsIgnoreCase("Heartland")) {
//                                                pay_tip = currentTagValue;
//                                            }
//                                            break;
//                                        case "TOKEN":
//                                            //cardInformation.setUniqueCardIdent(currentTagValue);
//                                            token = currentTagValue;
//                                            cardToken = token;
//                                            break;
//                                        case "CARDBIN":
//
//                                            try {
//                                                first6 = Integer.parseInt(currentTagValue);
//                                            } catch (Exception e) {
//                                                Timber.w(e, "Exception getting first6");
//                                            }
//                                            break;
//                                        case "PLNAMEONCARD":
//                                            holderName = currentTagValue;
//                                            break;
//                                        case "EXPDATE":
//                                            expDate = currentTagValue;
//                                            this.expDate = expDate;
//                                            break;
//                                        case "PLENTRYMODE":
//                                            entryMode = currentTagValue;
//                                            preentrytype = entryMode;
//                                            break;
//                                        case "HREF":
//                                            href = currentTagValue;
//                                            break;
//                                    }
//                                }
//                            }
//                            eventType = xpp.next();
//                        }
//
//                    } catch (XmlPullParserException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//                    origRef = posLink.PaymentResponse.RefNum;
//                    if (TextUtils.isEmpty(href)) {
//                        href = posLink.PaymentResponse.HostCode;
//                    }
//                    transactionID = href;
//
//                    if(!TextUtils.isEmpty(getSessionManager().getPayRoc()) && getSessionManager().getPayRoc().equalsIgnoreCase("Yes")) {
//                        getPayROCInfo(posLink.PaymentResponse.ApprovedAmount, pay_tip, first6);
//                    }else if(getIntent().getBooleanExtra("doPreAuth", false)){
//                        PaymentDetails paymentDetails = new PaymentDetails();
//                        paymentDetails.setState(PaymentDetails.State.INSERT_PAYMENT);
//                        if(pay_tip != null) {
//                            try {
//                                if(Long.parseLong(pay_tip)>0) {
//                                    Long realAmount = Long.parseLong(posLink.PaymentResponse.ApprovedAmount) - Long.parseLong(pay_tip);
//                                    paymentDetails.setAmount(realAmount + "");
//                                } else {
//                                    paymentDetails.setAmount(posLink.PaymentResponse.ApprovedAmount);
//                                }
//                            }catch (Exception e){
//                                paymentDetails.setAmount(posLink.PaymentResponse.ApprovedAmount);
//                            }
//                        }
//                        else {
//                            paymentDetails.setAmount(posLink.PaymentResponse.ApprovedAmount);
//                        }
//                        paymentDetails.setTipAmount(pay_tip);
//                        paymentDetails.setResult_code(posLink.PaymentResponse.ResultCode);
//
//                        paymentDetails.setCardType(posLink.PaymentResponse.CardType);
//                        paymentDetails.setLast4(posLink.PaymentResponse.BogusAccountNum);
//                        paymentDetails.setStatus(status);
//                        paymentDetails.setPayment_status(posLink.PaymentResponse.HostResponse);
//                        if(TextUtils.isEmpty(href)) {
//                            paymentDetails.setHref(posLink.PaymentResponse.HostCode);
//                            paymentDetails.setTransactionNo(posLink.PaymentResponse.HostCode);
//                        } else {
//                            paymentDetails.setHref(href);
//                            paymentDetails.setTransactionNo(href);
//                        }
//
//                        origRef = posLink.PaymentResponse.RefNum;
//                        transactionID = paymentDetails.getTransactionNo();
//
//                        paymentDetails.setEntryType(entryMode);
//                        paymentDetails.setAuth_code(posLink.PaymentResponse.AuthCode);
//                        paymentDetails.setRef_num(posLink.PaymentResponse.RefNum);
//                        //paymentDetails.setAio_accountid(object.getJSONObject("packetData").optString("accountId"));
//                        paymentDetails.setResult_message(posLink.PaymentResponse.Message);
//
//                        //paymentDetails.setIntegrator_version(object.getJSONObject("packetData").optString("integratorVersion"));
//                        //paymentDetails.setFw_version(object.getJSONObject("packetData").optString("FWVersion"));
//                        paymentDetails.setPay_response(new Gson().toJson(posLink.PaymentResponse));
//                        paymentDetails.setCardHolder(holderName);
//                        //paymentDetails.setEmvTagData(object.getJSONObject("packetData").optString("receiptEmvTagMap"));
//                        paymentDetails.setRawData(new Gson().toJson(posLink.PaymentResponse));
//
//                        paymentDetails.setPaymentBeforeSurcharge(paymentBeforeSurcharge);
//                        paymentDetails.setTipBeforeSurcharge(tipBeforeSurcharge);
//                        paymentDetails.setSurchargeAmount(surchargeAmount);
//                        paymentDetails.setSurchargeCardType(surchargeCardType);
//
//                        paymentDetails.setToken(cardToken);
//                        paymentDetails.setExp_date(this.expDate);
//
//                        observeData(paymentDetails);
//                    }
//
//                } else {
//
//                    PaymentDetails paymentDetails = new PaymentDetails();
//                    paymentDetails.setState(PaymentDetails.State.DECLINED);
//                    paymentDetails.setResult_code(posLink.PaymentResponse.ResultCode);
//                    paymentDetails.setResult_message(posLink.PaymentResponse.ResultTxt);
//                    paymentDetails.setResult_detailedMessage(posLink.PaymentResponse.Message);
//                    paymentDetails.setRawData(new Gson().toJson(posLink.PaymentResponse));
//                    paymentDetails.setAmount(total + "");
//                    paymentDetails.setTipAmount(tip + "");
//                    observeData(paymentDetails);
//                }
//
//            } else { // Easy Fail
//
//                PaymentDetails paymentDetails = new PaymentDetails();
//                paymentDetails.setState(PaymentDetails.State.DECLINED);
//                paymentDetails.setResult_code(posLink.PaymentResponse.ResultCode);
//                paymentDetails.setResult_message(posLink.PaymentResponse.ResultTxt);
//                paymentDetails.setResult_detailedMessage(posLink.PaymentResponse.Message);
//                paymentDetails.setRawData(new Gson().toJson(posLink.PaymentResponse));
//                paymentDetails.setAmount(total + "");
//                paymentDetails.setTipAmount(tip + "");
//                if (getSessionManager().isPayAppCatcher()) {
//                    handleSaleAfterError(total, tip, getPendingId(), paymentDetails, AFTER_DO_AUTH);
//                } else {
//                    observeData(paymentDetails);
//                }
//
//            }
//
//        }).start();
//    }
//
//    private void getPayROCInfo(String amount, String tip, int first6){
//        approvedAmount = amount;
//        approvedTip = tip;
//        getKioskpointApi().getPayRocInfo(first6, safeString(getIntent().getStringExtra("unique_id")))
//                .enqueue(new Callback<String>() {
//                    @Override
//                    public void onResponse(Call<String> call, Response<String> response) {
//                        try{
//                            JSONObject jsonObject = new JSONObject(response.body());
//                            if(jsonObject.getInt("ResponseCode") == 1){
//                                if(jsonObject.optString("applySurcharge", "No").equalsIgnoreCase("Yes")){
//                                    /*Intent intent = new Intent(PAXDevicePaymentActivity.this, SurchargeSelectorActivity.class);
//                                    intent.putExtras(getIntent().getExtras());
//                                    intent.putExtra("isPayRoc", true);
//                                    startActivityForResult(intent, REQUEST_SURCHARGE);*/
//
//                                    double surchargeOnPayment = 0d;
//                                    double surchargeOnTip = 0d;
//
//                                    double surchargePCT = Double.parseDouble(getSessionManager().getSurchargeCcPct()) / 10000;
//
//                                    double amountBeforeSurcharge = (getIntent().getDoubleExtra("amount", 0d));
//                                    surchargeOnPayment = getValueWithoutRounding((amountBeforeSurcharge * surchargePCT));
//
//                                    double tip = 0d;
//                                    if(getIntent().getDoubleExtra("tip", 0d) > 0)
//                                        tip =(getIntent().getDoubleExtra("tip", 0d));
//                                    surchargeOnTip = getValueWithoutRounding((tip * surchargePCT));
//
//                                    double surcharge = getValueWithoutRounding(surchargeOnPayment + surchargeOnTip);
//                                    double total = amountBeforeSurcharge + surcharge;
//
//                                    paymentBeforeSurcharge = String.format("%.2f", amountBeforeSurcharge);
//                                    tipBeforeSurcharge = String.format("%.2f", tip);
//                                    surchargeCardType = "Credit";
//                                    surchargeAmount = String.format("%.2f", surcharge);
//
//                                    Log.d("PAX BroadPOS Payment", String.format("Surcharge Data: %s - %s;", surchargeOnPayment, amountBeforeSurcharge));
//                                    Log.d("PAX BroadPOS Payment", String.format("Surcharge Data: %s - %s;", getSessionManager().getSurchargeCcPct(), surchargePCT));
//                                    Log.d("PAX BroadPOS Payment", String.format("Starting Payment 2: %s - %s - %s;", surchargeAmount, total, amount));
//                                    Log.d("PAX BroadPOS Payment", String.format("Starting Payment 2: %s - %s;", surchargeOnTip, tipBeforeSurcharge));
//
//                                    showCCSurchargeAlert(amountBeforeSurcharge, surcharge, total, tip, surchargeOnTip);
//                                }else{
//
//                                    Log.d("PAX BroadPOS Payment", "No Surcharge stuff");
//
//                                    // Set amount before charging
//                                    double amountBeforeSurcharge = getIntent().getDoubleExtra("amount", 0d);
//                                    double total = amountBeforeSurcharge;
//                                    approvedAmount = String.format("%.2f", total);
//
//                                    doPostAuth(transactionID, total, Double.parseDouble(tip) / 100);
//                                }
//                            }else{
//
//                                Log.d("PAX BroadPOS Payment", "No Surcharge stuff 2");
//
//                                // Set amount before charging
//                                double amountBeforeSurcharge = getIntent().getDoubleExtra("amount", 0d);
//                                double total = amountBeforeSurcharge;
//                                approvedAmount = String.format("%.2f", total);
//
//                                Toast.makeText(PAXDevicePaymentActivity.this, jsonObject.getString("ResponseMessage"), Toast.LENGTH_LONG).show();
//                                doPostAuth(transactionID, total, Double.parseDouble(tip) / 100);
//                            }
//                        } catch (JSONException e) {
//
//                            double amountBeforeSurcharge = getIntent().getDoubleExtra("amount", 0d);
//                            approvedAmount = String.format("%.2f", amountBeforeSurcharge);
//
//                            Log.d("PAX BroadPOS Payment", "No Surcharge stuff 3", e);
//                            doPostAuth(transactionID, amountBeforeSurcharge, Double.parseDouble(tip) / 100);
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Call<String> call, Throwable t) {
//
//                    }
//                });
//    }
//
//    private void showCCSurchargeAlert(double amount, double surchargeAmount, double totalAmount, double pay_tip, double surchargeTip){
//        final Dialog dialogX = new Dialog(PAXDevicePaymentActivity.this);
//        dialogX.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialogX.setContentView(R.layout.dialog_surcharge_message);
//        dialogX.setCancelable(false);
//
//        TextView mText = dialogX.findViewById(R.id.alert_text);
//        mText.setText(String.format(getString(R.string.msg_surcharge_alert), (Double.parseDouble(getSessionManager().getSurchargeCcPct()) / 100) + "%"));
//
//        dialogX.findViewById(R.id.btnOk).setOnClickListener(view -> {
//            try {
//                dialogX.dismiss();
//            }catch (Exception ex){
//                ex.printStackTrace();
//            }
//
//            doPostAuth(transactionID, amount, pay_tip, surchargeAmount);
//        });
//
//        dialogX.findViewById(R.id.btnCancel).setOnClickListener(view -> {
//            try {
//                dialogX.dismiss();
//            }catch (Exception ex){
//                ex.printStackTrace();
//            }
//
//            PaymentDetails paymentDetails = new PaymentDetails();
//            paymentDetails.setResult_message("User cancelled surcharge");
//            paymentDetails.setResult_code("");
//            paymentDetails.setState(PaymentDetails.State.DECLINED);
//            observeData(paymentDetails);
//        });
//
//        try {
//            if (!isFinishing()) {
//                dialogX.show();
//            }
//        } catch (Exception e) {
//            Timber.tag("showCCSurchargeAlert").w(e, "Exception");
//        }
//    }
//
//    private double getValueWithoutRounding(double value){
//        DecimalFormat df = new DecimalFormat();
//        df.setMaximumFractionDigits(2);
//        return Double.parseDouble(df.format(value));
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if(requestCode == REQUEST_SURCHARGE){
//            if(resultCode == RESULT_OK) {
//                double surchargeAmount = data.getDoubleExtra("surchargeAmount", 0d);
//                double total = Double.parseDouble(approvedAmount) + surchargeAmount;
//                doPostAuth(transactionID, total, Double.parseDouble(approvedTip));
//            }else{
//                doPostAuth(transactionID, Double.parseDouble(approvedAmount), Double.parseDouble(approvedTip));
//            }
//        }
//    }
//
//    @Override
//    public void cancelPayment() {
//
//        if(posLink == null) {
//            posLink = createAndValidatePOSLink();
//        }
//
//        if(posLink != null){
//            Timber.tag("PAX BroadPOS Payment").i("Cancel payment");
//            posLink.CancelTrans();
//        } else {
//            Timber.tag("PAX BroadPOS Payment").i("Cancel payment ignored, posLink is null.");
//        }
//    }
//
//    private void doPostAuth(String refNum, double total, double tip){
//        doPostAuth(refNum, total, tip, 0d);
//    }
//    private void doPostAuth(String refNum, double total, double tip, double surcharge){
//        new Thread(() -> {
//
//            long timeout = (getTriggeredTime().getTime() + TimeUnit.SECONDS.toMillis(getSessionManager().getTimeout())) - System.currentTimeMillis();
//
//            String ticket_id = getIntent().getStringExtra("ticket_id");
//            if (TextUtils.isEmpty(ticket_id)) {
//                ticket_id = "";
//            }
//
//            Timber.tag("PAX BroadPOS Payment").i("Starting Payment: %s %s %s %s", ticket_id, timeout, total, tip);
//
//            posLink = createAndValidatePOSLink();
//
//            Timber.tag("PAX BroadPOS Payment").i("Link Setup");
//
//            PaymentRequest paymentRequest = new PaymentRequest();
//            paymentRequest.TenderType = paymentRequest.ParseTenderType("CREDIT");
//            paymentRequest.TransType = paymentRequest.ParseTransType("POSTAUTH");
//            //paymentRequest.TransType = paymentRequest.ParseTransType("TOKENIZE");
//
//            Timber.tag("PAX BroadPOS Payment").i("RefNum " + getPendingId() + "; " + getTransactionNo());
//
//            paymentRequest.Amount = Math.round(total * 100) + "";
//            paymentRequest.TipAmt = Math.round((tip + surcharge) * 100) + "";
//            paymentRequest.ECRRefNum = getPendingId();
//            paymentRequest.OrigRefNum = origRef;
//            paymentRequest.ExtData = "<HRefNum>" + refNum + "</HRefNum>";
//            //paymentRequest.ECRRefNum = "000000";
//            //paymentRequest.ECRTransID = getTransactionNo();
//            paymentRequest.InvNum = getIntent().getStringExtra("webhook_id");
//            //paymentRequest.PONum = getIntent().getStringExtra("webhook_id");
//
//            posLink.PaymentRequest = paymentRequest;
//
//            Timber.tag("PAX BroadPOS Payment").i("Processing %s", new Gson().toJson(posLink.PaymentRequest));
//            insertPaymentRequestResponse(new Gson().toJson(paymentRequest), "", "", "");
//            ProcessTransResult ptr = posLink.ProcessTrans();
//            insertPaymentRequestResponse("", new Gson().toJson(posLink.PaymentResponse), "", "");
//
//            Timber.tag("PAX BroadPOS Payment").i("Payment Finished: " + ptr.Code + ": " + ptr.Msg);
//            Timber.tag("PAX BroadPOS Payment").i("Payment Finished: %s", new Gson().toJson(posLink.PaymentResponse));
//
//
//            if(ptr.Code == ProcessTransResult.ProcessTransResultCode.OK) { // Easy success
//
//                if(posLink.PaymentResponse.ResultCode.equalsIgnoreCase("000000")) {
//
//                    String status;
//                    if (getIntent().getBooleanExtra("wh_isRefund", false)) {
//                        status = "Refunded";
//                    } else {
//                        status = "Paid";
//                    }
//
//                    String holderName = "";
//                    String entryMode = "";
//                    String token = "";
//                    String href = null;
//                    String pay_tip = Math.round((tip) * 100) + "";
//                    int first6 = 0;
//                    String expDate = "";
//                    //Parse the extra data
//                    try {
//                        //String extraDataValue = "<Attributes>" + output.ExtData + "</Attributes>";
//                        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
//                        factory.setNamespaceAware(true);
//                        XmlPullParser xpp = factory.newPullParser();
//                        String currentTag = null;
//                        xpp.setInput( new StringReader( posLink.PaymentResponse.ExtData ) ); // pass input whatever xml you have
//                        int eventType = xpp.getEventType();
//                        while (eventType != XmlPullParser.END_DOCUMENT) {
//                            if(eventType == XmlPullParser.START_TAG) {
//                                currentTag = xpp.getName();
//                            } else if(eventType == XmlPullParser.END_TAG) {
//                                currentTag = null;
//                            } else if(eventType == XmlPullParser.TEXT) {
//                                String currentTagValue = xpp.getText();
//                                if (currentTag != null){
//                                    Timber.tag("PAX BroadPOS Payment").i("Tag Found: %s, %s", currentTag.toUpperCase(), currentTagValue);
//                                    switch (currentTag.toUpperCase()){
//                                        case "TIPAMOUNT":
//                                            if(getSessionManager().getTips() == Tips.BEFORE && getSessionManager().getProcessor().equalsIgnoreCase("Heartland")) {
//                                                pay_tip = currentTagValue;
//                                            }
//                                            break;
//                                        case "TOKEN":
//                                            //cardInformation.setUniqueCardIdent(currentTagValue);
//                                            token = currentTagValue;
//                                            cardToken = token;
//                                            break;
//                                        case "CARDBIN":
//
//                                            try {
//                                                first6 = Integer.parseInt(currentTagValue);
//                                            } catch (Exception e) {
//                                                Timber.w(e, "Exception getting first6");
//                                            }
//                                            break;
//                                        case "PLNAMEONCARD":
//                                            holderName = currentTagValue;
//                                            break;
//                                        case "EXPDATE":
//                                            expDate = currentTagValue;
//                                            this.expDate = expDate;
//                                            break;
//                                        case "PLENTRYMODE":
//                                            entryMode = currentTagValue;
//                                            entryMode = preentrytype;
//                                            break;
//                                        case "HREF":
//                                            href = currentTagValue;
//                                            break;
//                                    }
//                                }
//                            }
//                            eventType = xpp.next();
//                        }
//
//                    } catch (XmlPullParserException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//                    int cardType;
//                    switch (posLink.PaymentResponse.CardType)
//                    {
//                        case "MASTERCARD":
//                        case "02":
//                        case "MASTERCARDFLEET":
//                            cardType = 3;
//                            break;
//                        case "VISA":
//                        case "01":
//                        case "VISAFLEET":
//                        case "09":
//                            cardType = 2;
//                            break;
//                        case "AMEX":
//                        case "03":
//                            cardType = 1;
//                            break;
//                        case "DISCOVER":
//                        case "04":
//                            cardType = 34;
//                            break;
//                        case "DINERCLUB":
//                        case "05":
//                            cardType = 10;
//                            break;
//                        case "JCB":
//                        case "07":
//                            cardType = 11;
//                            break;
//                        default:
//                            cardType = 13;
//                            break;
//                    }
//
//                    if (TextUtils.isEmpty(token))
//                    {
//                        int last = 0;
//                        try {
//                            last = Integer.parseInt( posLink.PaymentResponse.BogusAccountNum);
//                        } catch (Exception e) {
//                            Timber.w(e, "Exception getting last4");
//                        }
//                        token = first6 + last + expDate + holderName + cardType;
//                        //cardInformation.setUniqueCardIdent(cardInformation.getPANFirstSix() + cardInformation.getPANLastFour() + expDate + cardInformation.getNameOnCard() + cardInformation.getCardType() );
//                    }
//
//                    PaymentDetails paymentDetails = new PaymentDetails();
//                    paymentDetails.setState(PaymentDetails.State.INSERT_PAYMENT);
//                    if(pay_tip != null) {
//                        try {
//                            if(Long.parseLong(pay_tip)>0) {
//                                Long realAmount = Long.parseLong(posLink.PaymentResponse.ApprovedAmount) - Long.parseLong(pay_tip);
//                                paymentDetails.setAmount(realAmount + "");
//                            } else {
//                                paymentDetails.setAmount(posLink.PaymentResponse.ApprovedAmount);
//                            }
//                        }catch (Exception e){
//                            paymentDetails.setAmount(posLink.PaymentResponse.ApprovedAmount);
//                        }
//                    }
//                    else {
//                        paymentDetails.setAmount(posLink.PaymentResponse.ApprovedAmount);
//                    }
//                    paymentDetails.setTipAmount(pay_tip);
//                    paymentDetails.setResult_code(posLink.PaymentResponse.ResultCode);
//
//                    paymentDetails.setCardType(posLink.PaymentResponse.CardType);
//                    paymentDetails.setLast4(posLink.PaymentResponse.BogusAccountNum);
//                    paymentDetails.setStatus(status);
//                    paymentDetails.setPayment_status(posLink.PaymentResponse.HostResponse);
//                    if(TextUtils.isEmpty(href)) {
//                        paymentDetails.setHref(posLink.PaymentResponse.HostCode);
//                        paymentDetails.setTransactionNo(posLink.PaymentResponse.HostCode);
//                    } else {
//                        paymentDetails.setHref(href);
//                        paymentDetails.setTransactionNo(href);
//                    }
//
//                    origRef = posLink.PaymentResponse.RefNum;
//                    transactionID = paymentDetails.getTransactionNo();
//
//                    paymentDetails.setEntryType(entryMode);
//                    paymentDetails.setAuth_code(posLink.PaymentResponse.AuthCode);
//                    paymentDetails.setRef_num(posLink.PaymentResponse.RefNum);
//                    //paymentDetails.setAio_accountid(object.getJSONObject("packetData").optString("accountId"));
//                    paymentDetails.setResult_message(posLink.PaymentResponse.Message);
//
//                    //paymentDetails.setIntegrator_version(object.getJSONObject("packetData").optString("integratorVersion"));
//                    //paymentDetails.setFw_version(object.getJSONObject("packetData").optString("FWVersion"));
//                    paymentDetails.setPay_response(new Gson().toJson(posLink.PaymentResponse));
//                    paymentDetails.setCardHolder(holderName);
//                    //paymentDetails.setEmvTagData(object.getJSONObject("packetData").optString("receiptEmvTagMap"));
//                    paymentDetails.setRawData(new Gson().toJson(posLink.PaymentResponse));
//
//                    paymentDetails.setPaymentBeforeSurcharge(paymentBeforeSurcharge);
//                    paymentDetails.setTipBeforeSurcharge(tipBeforeSurcharge);
//                    paymentDetails.setSurchargeAmount(surchargeAmount);
//                    paymentDetails.setSurchargeCardType(surchargeCardType);
//
//                    paymentDetails.setToken(cardToken);
//                    paymentDetails.setExp_date(this.expDate);
//
//                    observeData(paymentDetails);
//
//                } else {
//
//                    PaymentDetails paymentDetails = new PaymentDetails();
//                    paymentDetails.setState(PaymentDetails.State.DECLINED);
//                    paymentDetails.setResult_code(posLink.PaymentResponse.ResultCode);
//                    paymentDetails.setResult_message(posLink.PaymentResponse.ResultTxt);
//                    paymentDetails.setResult_detailedMessage(posLink.PaymentResponse.Message);
//                    paymentDetails.setRawData(new Gson().toJson(posLink.PaymentResponse));
//                    paymentDetails.setAmount(total + "");
//                    paymentDetails.setTipAmount(tip + "");
//                    observeData(paymentDetails);
//                }
//
//            } else { // Easy Fail
//
//                PaymentDetails paymentDetails = new PaymentDetails();
//                paymentDetails.setState(PaymentDetails.State.DECLINED);
//                paymentDetails.setResult_code(posLink.PaymentResponse.ResultCode);
//                paymentDetails.setResult_message(posLink.PaymentResponse.ResultTxt);
//                paymentDetails.setResult_detailedMessage(posLink.PaymentResponse.Message);
//                paymentDetails.setRawData(new Gson().toJson(posLink.PaymentResponse));
//                paymentDetails.setAmount(total + "");
//                paymentDetails.setTipAmount(tip + "");
//                if (getSessionManager().isPayAppCatcher()) {
//                    handleSaleAfterError(total, tip, getPendingId(), paymentDetails, AFTER_DO_POST_AUTH);
//                } else {
//                    observeData(paymentDetails);
//                }
//
//            }
//
//        }).start();
//    }
//
//    // We want this value for checking and we want it here as we want it to always set on app startup.
//    private static String oldValue = "";
//
//    @WorkerThread
//    public static PosLink createAndValidatePOSLink() {
//        return createAndValidatePOSLink(false);
//    }
//
//    @WorkerThread
//    public static PosLink createAndValidatePOSLink(boolean skipSettings) {
//        // TODO: Init BroadPOS and check if we need to do anything with it.
//
//        CommSetting commSetting = new CommSetting();
//        if (Build.MODEL.startsWith("E")) {
//            commSetting.setType(CommSetting.USB);
//        } else if (Build.MODEL.startsWith("A") || Build.MODEL.startsWith("S9")) {
//            commSetting.setType(CommSetting.AIDL);
//        } else {
//            commSetting.setType(CommSetting.TCP);
//        }
//        commSetting.setTimeOut("-1");
//        commSetting.setSerialPort("COM1");
//        commSetting.setBaudRate("9600");
//        commSetting.setDestIP("172.16.20.15");
//        commSetting.setDestPort("10009");
//        commSetting.setMacAddr("");
//        commSetting.setEnableProxy(false);
//
//        Timber.tag("PAX BroadPOS Setup").d("COMM Created");
//
//        POSLinkAndroid.init(getStaticContext(), commSetting);
//        final PosLink posLink = POSLinkCreator.createPoslink(getStaticContext());
//        posLink.SetCommSetting(commSetting);
//
//        Timber.tag("PAX BroadPOS Setup").i("Link Setup");
//
//        ManageRequest manageRequest;
//
//        if(!skipSettings) {
//            if (true) {
//                manageRequest = new ManageRequest();
//                manageRequest.TransType = manageRequest.ParseTransType("SETVAR");
//                manageRequest.EDCType = manageRequest.ParseEDCType("CREDIT");
//                manageRequest.VarName = "menuTimeout";
//                manageRequest.VarValue = "600";
//                manageRequest.VarName2 = "accountMenuTimeout";
//                manageRequest.VarValue2 = "600";
//
//                posLink.ManageRequest = manageRequest;
//                ProcessTransResult ptr = posLink.ProcessTrans();
//
//                Timber.tag("PAX BroadPOS Setup").i("Setup Finished: " + ptr.Code + ": " + ptr.Msg);
//                Timber.tag("PAX BroadPOS Setup").i("Setup Finished: %s", new Gson().toJson(posLink.ManageResponse));
//            }
//
//            // If we are a BofA device we need to do a few extra steps to make sure everything is correct. Other BroadPOS apps dont need this.
//            // Eduardo decided to call BofA CyberSource... probably because thats what their online APIs are called but still.
//            if (getSessionManager().getProcessor().equalsIgnoreCase("CyberSource")) {
//
//                manageRequest = new ManageRequest();
//                manageRequest.TransType = manageRequest.ParseTransType("GETVAR");
//                manageRequest.EDCType = manageRequest.ParseEDCType("CREDIT");
//                manageRequest.VarName = "solutionId";
//
//                posLink.ManageRequest = manageRequest;
//
//                Timber.tag("PAX BroadPOS Setup").i("Processing");
//                System.out.println("REQUEST " + new Gson().toJson(manageRequest));
//                ProcessTransResult ptr = posLink.ProcessTrans();
//                System.out.println("RESPONSE " + new Gson().toJson(posLink.ManageResponse));
//
//                Timber.tag("PAX BroadPOS Setup").i("Setup Check: " + ptr.Code + ": " + ptr.Msg);
//                Timber.tag("PAX BroadPOS Setup").i("Setup Check: %s", new Gson().toJson(posLink.ManageResponse));
//
//                String solutionId = "";
//                try {
//                    solutionId = posLink.ManageResponse.VarValue.substring(0, 4) + getSessionManager().getSolutionId();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                // check if different and we need to reset it
//                if (!oldValue.equalsIgnoreCase(manageRequest.VarValue) || !solutionId.equalsIgnoreCase(manageRequest.VarValue)) {
//
//                    // Set command
//                    manageRequest = new ManageRequest();
//                    manageRequest.TransType = manageRequest.ParseTransType("SETVAR");
//                    manageRequest.EDCType = manageRequest.ParseEDCType("CREDIT");
//                    manageRequest.VarName = "solutionId";
//                    //"MTL29899"
//                    try {
//                        Timber.tag("PAX BroadPOS Setup").i("Setting value");
//                        manageRequest.VarValue = solutionId;
//                    } catch (Exception e) {
//                        Timber.tag("PAX BroadPOS Setup").i(e, "Error setting value, using fallback");
//                        manageRequest.VarValue = "S82F";
//                    }
//
//                    // We now know the new value so set it so that we don't always do this extra step
//                    oldValue = manageRequest.VarValue;
//
//                    posLink.ManageRequest = manageRequest;
//                    ptr = posLink.ProcessTrans();
//
//                    Timber.tag("PAX BroadPOS Setup").i("Setup Finished: " + ptr.Code + ": " + ptr.Msg);
//                    Timber.tag("PAX BroadPOS Setup").i("Setup Finished: %s", new Gson().toJson(posLink.ManageResponse));
//                }
//
//                Timber.tag("PAX BroadPOS Setup").i("Setup Complete");
//            }
//        }
//        return posLink;
//    }
//
//    public void transactionAuthorization(@Nullable String token,  double amount) {
//        if(!getSessionManager().getProcessor().equalsIgnoreCase("TSYS")) {
//            throw new UnsupportedOperationException("Authorization Transaction not supported");
//        }
//
//        // We do a general auth transaction for TSYS
//        if(getSessionManager().getProcessor().equalsIgnoreCase("TSYS")) {
//
//
//            posLink = createAndValidatePOSLink();
//
//            Timber.tag("PAX BroadPOS Payment").i("Link Setup");
//
//            PaymentRequest paymentRequest = new PaymentRequest();
//            paymentRequest.TenderType = paymentRequest.ParseTenderType("CREDIT");
//            paymentRequest.TransType = paymentRequest.ParseTransType("AUTH");
//
//            Timber.tag("PAX BroadPOS Payment").i("RefNum " + getPendingId() + "; " + getTransactionNo());
//
//            paymentRequest.Amount = Math.round(amount * 100) + "";
//            paymentRequest.ECRRefNum = getPendingId();
//            paymentRequest.OrigRefNum = origRef;
//
//            paymentRequest.InvNum = getIntent().getStringExtra("webhook_id");
//
//            posLink.PaymentRequest = paymentRequest;
//
//            Timber.tag("PAX BroadPOS Payment").i("Processing %s", new Gson().toJson(posLink.PaymentRequest));
//            insertPaymentRequestResponse(new Gson().toJson(paymentRequest), "", "", "");
//            ProcessTransResult ptr = posLink.ProcessTrans();
//            insertPaymentRequestResponse("", new Gson().toJson(posLink.PaymentResponse), "", "");
//
//            Timber.tag("PAX BroadPOS Payment").i("Payment Finished: " + ptr.Code + ": " + ptr.Msg);
//            Timber.tag("PAX BroadPOS Payment").i("Payment Finished: %s", new Gson().toJson(posLink.PaymentResponse));
//
//            // TODO: parse response
//        }
//    }
//
//    public void transactionIncrement(@Nullable String transaction,  double amount) {
//        if(!getSessionManager().getProcessor().equalsIgnoreCase("TSYS")) {
//            throw new UnsupportedOperationException("Authorization Transaction not supported");
//        }
//
//        // TSYS does not have an increment so we need to assume that it works
//        if(getSessionManager().getProcessor().equalsIgnoreCase("TSYS")) {
//
//            // TODO: fake response, assumed approval
//        }
//    }
//
//    public void transactionCompletion(@NonNull String transaction, double amount, double tip) {
//        if(!getSessionManager().getProcessor().equalsIgnoreCase("TSYS")) {
//            throw new UnsupportedOperationException("Authorization Transaction not supported");
//        }
//
//        // We add any extra amount to the tip, because thats the only way it works atm
//        if(getSessionManager().getProcessor().equalsIgnoreCase("TSYS")) {
//
//            posLink = createAndValidatePOSLink();
//
//            Timber.tag("PAX BroadPOS Payment").i("Link Setup");
//
//            PaymentRequest paymentRequest = new PaymentRequest();
//            paymentRequest.TenderType = paymentRequest.ParseTenderType("CREDIT");
//            paymentRequest.TransType = paymentRequest.ParseTransType("POSTAUTH");
//
//            Timber.tag("PAX BroadPOS Payment").i("RefNum " + getPendingId() + "; " + getTransactionNo());
//
//            paymentRequest.Amount = Math.round(amount * 100) + "";
//            paymentRequest.ECRRefNum = getPendingId();
//            paymentRequest.OrigRefNum = origRef;
//
//            paymentRequest.InvNum = getIntent().getStringExtra("webhook_id");
//
//            posLink.PaymentRequest = paymentRequest;
//
//            Timber.tag("PAX BroadPOS Payment").i("Processing %s", new Gson().toJson(posLink.PaymentRequest));
//            insertPaymentRequestResponse(new Gson().toJson(paymentRequest), "", "", "");
//            ProcessTransResult ptr = posLink.ProcessTrans();
//            insertPaymentRequestResponse("", new Gson().toJson(posLink.PaymentResponse), "", "");
//
//            Timber.tag("PAX BroadPOS Payment").i("Payment Finished: " + ptr.Code + ": " + ptr.Msg);
//            Timber.tag("PAX BroadPOS Payment").i("Payment Finished: %s", new Gson().toJson(posLink.PaymentResponse));
//
//            // TODO: everything...
//        }
//    }
//
//    /**
//     * TODO: cleanup this as its got a lot of junk
//     *
//     * Bank of America (CyberSource) needs us to adjust a transaction for zero when it is done when
//     * we do tips before because of how PAX programed it. This is according to BofA.
//     */
//    private void cyberSourceFinishTransaction(String refNum) {
//
//        final PosLink posLink = createAndValidatePOSLink(false);
//
//        PaymentRequest paymentRequest = new PaymentRequest();
//        paymentRequest.TenderType = paymentRequest.ParseTenderType("CREDIT");
//        paymentRequest.TransType = paymentRequest.ParseTransType("ADJUST");
//        paymentRequest.Amount = "0";
//        String ECRRefNum = "" + ((int) (Math.random() * 10000));
//        paymentRequest.ECRRefNum = ECRRefNum;
//        paymentRequest.OrigRefNum = refNum;
//        //paymentRequest.ExtData = "<HRefNum>" + transactionID + "</HRefNum>";
//        paymentRequest.InvNum = "" + ((int) (Math.random() * 10000));
//
//        posLink.PaymentRequest = paymentRequest;
//
//        Timber.d("PAX BroadPOS Payment %s", new Gson().toJson(paymentRequest));
//
//        ProcessTransResult ptr = posLink.ProcessTrans();
//        String pay_response = new Gson().toJson(posLink.PaymentResponse);
//        Timber.d("PAX BroadPOS Payment %s", pay_response);
//
//        if (ptr.Code == ProcessTransResult.ProcessTransResultCode.OK) { // Easy success
//            if (posLink.PaymentResponse.ResultCode.equalsIgnoreCase("000000")) {
//
//                // TODO: confirm it was done correctly maybe? shouldn't really be needed
//            }
//        }
//    }
//}
