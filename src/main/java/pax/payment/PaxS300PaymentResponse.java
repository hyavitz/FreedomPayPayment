package pax.payment;

import com.pax.poslink.PaymentResponse;
import com.pax.poslink.PosLink;

public class PaxS300PaymentResponse {

    /** RESPONSE */

    private String signatureResponse;
    private String plCardPresent;
    private String plStreet;
    private String plEntryMode;
    private String plNameOnCard;
    private String plZip;
    private String plPoNumber;
    private String plCustCode;
    private String globalId;

    // amount information
    private String amountDue;
    private String tipAmount;
    private String cashBackAmount;
    private String merchantFee;
    private String taxAmount;
    private String serviceFee;

    // account information
    private String expDate;
    private String ebtType;
    private String voucherNum;
    private String newAccountNo;
    private String cvvMessage;

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
    private String ecrRefNum; // TODO: Generate from POS and assign to webhook transactionNo for follow-on transactions (PaymentRequest -> PaymentResponse)

    // avs information
    private String avsMessage;
    private String zipCode;
    private String address1;
    private String address2;

    // commercial information
    private String taxExempt;
    private String taxExemptId;
    private String merchantTaxId;
    private String destinationZipCode;
    private String productDescription;

    // moto ecommerce information
    private String motoECommerceMode;
    private String motoECommerceTransType;
    private String eCommerceSecureType;
    private String motoECommerceOrderNum;
    private String installments;
    private String currentInstallment;
    private String hostTraceNum;
    private String batchNum;
    private String transactionIdentifier;

    // additional information
    private String tableNum;
    private String guestNum;
    private String ticketNum;
    private String disAmtNum;
    private String chgAmtNum;
    private String signStatusNum;
    private String fpsOffline;
    private String fpsSignStatusNum;
    private String fpsReceiptModeNum;
    private String edcType;
    private String origTip;
    private String token;
    private String hRef;
    private String addlRspData;
    private String signData;
    private String cardBin;
    private String reversedAmt;
    private String reversalStatusNum;
    private String newCardBin;
    private String programType;
    private String sn;
    private String printLine1;
    private String printLine2;
    private String printLine3;
    private String printLine4;
    private String printLine5;
    private String settlementDate;
    private String hostEchoData;
    private String pinStatusNum;
    private String eWicBenefitExpd;
    private String eWicBalance;
    private String eWicDetail;
    private String invNum;

    // emv contactless information
    private String tc;
    private String tvr;
    private String aid;
    private String tsi;
    private String atc;
    private String appLab;
    private String appPn;
    private String iad;
    private String arc;
    private String cid;
    private String cvm;

    // failed emv information
    private String ac;
    private String aip;
    private String avn;
    private String iAuthD;
    private String cDol2;
    private String hRed;
    private String tacDefault;
    private String tacDenial;
    private String tacOnline;
    private String iacDefault;
    private String iacDenial;
    private String iacOnline;
    private String auc;

    // fleet card information
    private String odometer;
    private String vehicleNo;
    private String jobNo;
    private String driverId;
    private String employeeNo;
    private String licenseNo;
    private String jobId;
    private String departmentNo;
    private String customerData;
    private String userId;
    private String vehicleId;

    private static final PaymentResponse paymentResponse = new PaymentResponse();

    public static PaymentResponse getPaymentResponse(PosLink posLink) {

        String extraData = posLink.PaymentResponse.ExtData;






        return paymentResponse;
    }
}
