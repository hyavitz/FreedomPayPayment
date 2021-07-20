package pax.payment;

import com.pax.poslink.AddlRspData;
import com.pax.poslink.ManageRequest;
import com.pax.poslink.PaymentRequest;
import interceptor.CommandListener;
import payment.InvoiceNumber;

/**
 * This class returns a PaymentRequest object used by PosLink.
 * It accepts 3 required parameters and several optional.  It
 * is called by the PaxS300PaymentDevice class when building
 * a transaction.
 *
 * 1) Instantiate PaymentRequest and TransactionBehavior objects
 * 2) Assign TenderType and TransactionType to PaymentRequest (i.e. credit, sale)
 * 3) Assign BehaviorType to PaymentRequest (i.e. tip prompt)
 * 4) Assign currency values (i.e. amount, tip, tax)
 * 5) Assign transaction references (i.e. ecrn, orig ecrn, invoice)
 * 6) Return PaymentRequest object
 *
 * @author Hunter Yavitz 6/22/21
 */

public class PaxS300PaymentRequest {

    // fields
    private String amount;
    private String cashBackAmt;
    private String fuelAmt;
    private String clerkId;
    private String cssPath;
    private String customFields;
    private String userId;
    private String password;
    private String customerName;
    private String zip;
    private String tipAmt;
    private String taxAmt;
    private String street;
    private String street2;
    private String serverId;
    private String surchargeAmt;
    private String autoSubmit;
    private String poNum;
    private String origRefNum;
    private String merchantKey;
    private String invNum;
    private String ecrRefNum; // TODO: Generate from POS assign to webhook transactionNo for follow-on transactions (PaymentRequest -> PaymentResponse)
    private String authCode;
    private String ecrTransId;
    private String origEcrRefNum; // TODO: Source from ECRRefNum of prior transactions (PaymentResponse -> PaymentRequest)
    private String commercialCard;
    private String continuousScreen;
    private String serviceFee;
    private String giftCardType;
    private String restaurant;
    private String transactionBehavior;
    private String hostGateway;
    private String original;
    private String fleetCard;
    private String multiMerchant;
    private String lodgingInfo;

    /** REQUEST EXTRA DATA */

    // account information
    private String account;
    private String expDate;
    private String cvv;
    private String ebtFoodStampVoucher;
    private String ebtType;
    private String voucherNum;
    private String force;
    private String firstName;
    private String lastName;
    private String countryCode;
    private String stateCode;
    private String cityName;
    private String emailAddress;
    private String globalUid;

    // check information
    private String checkSaleType;
    private String checkRoutingNum;
    private String checkNum;
    private String checkType;
    private String checkIdType;
    private String checkIdValue;
    private String birth;
    private String phoneNum;

    // trace information
    private String timeStamp;

    // cashier information
    private String shiftId;

    // commercial information
    private String customerCode;
    private String taxExempt;
    private String taxExemptId;
    private String merchantTaxId;
    private String destinationZipCode;
    private String productDescription;
    private String localTax;
    private String nationalTax;
    private String customerTaxId;
    private String summaryCommodityCode;
    private String discountAmt;
    private String freightAmt;
    private String dutyAmt;
    private String shipFromZipCode;
    private String vatInvoiceRefNum;
    private String orderDate;
    private String vatTaxAmt;
    private String alternateTaxAmt;
    private String alternateTaxId;

    // moto ecommerce information
    private String motoECommerceMode;
    private String motoECommerceTransType;
    private String eCommerceSecureType;
    private String motoECommerceOrderNum;
    private String installments;
    private String currentInstallment;

    // additional information
    private String tableNum;
    private String guestNum;
    private String signatureCapture;
    private String ticketNum;
    private String hRefNum;
    private String tipRequest;
    private String signUploadFlag;
    private String reportStatus;
    private String token;
    private String tokenRequest;
    private String cardType;
    private String cardTypeBitmap;
    private String passthruData;
    private String returnReason;
    private String origTransDate;
    private String origPan;
    private String origExpiryDate;
    private String origTransTime;
    private String disProgPrompts;
    private String gatewayId;
    private String posEchoData;
    private String getSign;
    private String entryModeBitmap;
    private String receiptPrint;
    private String cpMode;
    private String fleetPromptCode;
    private String debitNetwork;
    private String stationNo;
    private String origSettlementDate;
    private String origTransType;
    private String origAmount;
    private String origBatchNum;
    private String origTransId;
    private String userLanguage;
    private String addlRspDataRequest;
    private String forceCc;
    private String forceFsa;
    private String customizeData1;
    private String customizeData2;
    private String customizeData3;

    // fleet card transactions
    private String odometer;
    private String vehicleNo;
    private String jobNo;
    private String driverId;
    private String employeeNo;
    private String licenseNo;
    private String jobId;
    private String departmentNo;
    private String customerData;
    private String _userId;
    private String vehicleId;

    // multi-merchant applications
    private String mmId;
    private String mmName;

    private static final PaymentRequest paymentRequest = new PaymentRequest();
    private static final PaymentRequest.TransactionBehavior transactionBehaviour = new PaymentRequest.TransactionBehavior();

    // Sale - Card Present and Required Amount (Tax and Tip Nullable)
    public static PaymentRequest getPaymentRequestSale(int amount, int tip, int tax) {

        // Configure tender and transaction
        paymentRequest.TenderType = paymentRequest.ParseTenderType("CREDIT");
        paymentRequest.TransType = paymentRequest.ParseTransType("SALE");

        // Switch behavior flag to make tip request
        transactionBehaviour.TipRequestFlag = "1";
        paymentRequest.TransactionBehavior = transactionBehaviour;

        // Required currency field
        paymentRequest.Amount = Math.round(amount) + "";
        paymentRequest.TipAmt = Math.round(tip) + "";
        paymentRequest.TaxAmt = Math.round(tax) + "";

        // Assign transaction reference (ECRN + Invoice)
        //paymentRequest.ECRRefNum = InvoiceNumber.generateInvoiceNumber(); // TODO: Generate value for follow-on transactions
        System.out.println("paymentRequest.ECRRefNum is being set to: " + CommandListener.href);
        paymentRequest.ECRRefNum = CommandListener.href;
        paymentRequest.InvNum = paymentRequest.ECRRefNum;

        CommandListener.isVoid = false;

        return paymentRequest;
    }

    // Return - Card Present and Optional Amount (Partial Allowed)
    public static PaymentRequest getPaymentRequestReturn(int amount) {

        // Configure tender and transaction
        paymentRequest.TenderType = paymentRequest.ParseTenderType("CREDIT");
        paymentRequest.TransType = paymentRequest.ParseTransType("RETURN");

        // Required currency field
        paymentRequest.Amount = amount + "";

        // Assign transaction reference (ECRN)
        paymentRequest.ECRRefNum = InvoiceNumber.generateInvoiceNumber(); // TODO: Generate value for follow-on transactions

        return paymentRequest;
    }

    // Void - Original Reference Number (or Transaction Number on Request)
    public static PaymentRequest getPaymentRequestVoid(String referenceNumber) {

        // Configure tender and transaction
        paymentRequest.TenderType = paymentRequest.ParseTenderType("CREDIT");
        paymentRequest.TransType = paymentRequest.ParseTransType("VOID");

        // Assign transaction reference (ECRN + Original ECRN)
        paymentRequest.ECRRefNum = CommandListener.href;
        // paymentRequest.OrigRefNum = referenceNumber; // TODO: Source value for follow-on transactions
        // ManageRequest manageRequest = new ManageRequest(); // TODO: PaymentResponse.ExtraData.HRef -> PaymentRequest.ExtraData.HRefNum
        paymentRequest.OrigECRRefNum = CommandListener.href;


        //
        System.out.println("CommandListener.href is: " + CommandListener.href); // TODO: Why is this empty?
        System.out.println("PaymentRequest is assigning OrigRefNum to " + paymentRequest.OrigRefNum); // TODO: Why is this empty?
        paymentRequest.Amount = "0";
        paymentRequest.TipAmt = "";
        paymentRequest.TaxAmt = "";

        CommandListener.isVoid = true;

        return paymentRequest;
    }

    // Adjust - Original Reference Number with Total Adjusted Amount or Tip
    public static PaymentRequest getPaymentRequestAdjust(String referenceNumber, int amount, int tip) {

        // Configure tender and transaction
        paymentRequest.TenderType = paymentRequest.ParseTenderType("CREDIT");
        paymentRequest.TransType = paymentRequest.ParseTransType("ADJUST");

        // Optional currency fields - Adjust by tip or pass -1 to adjust by total amount
        if (tip > 0) {
            paymentRequest.TipAmt = Math.round(tip) + "";
        } else if (amount > 0){
            paymentRequest.Amount = Math.round(amount) + "";
        } else {
            // TODO: Throw Exception - Process as Failure
        }

        // Assign transaction reference
        paymentRequest.ECRRefNum = InvoiceNumber.generateInvoiceNumber(); // TODO: Generate value for follow-on transactions
        paymentRequest.OrigRefNum = referenceNumber; // TODO: Source value for follow-on transactions

        return paymentRequest;
    }
}