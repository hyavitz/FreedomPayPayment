package freedompay.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.*;

/*

    This class constructs an object with properties represented by the XML
    response from FreedomPay transaction requests.  Most of these fields
    will rarely be used, but must be available.  Required fields are marked.

    @author Hunter Yavitz - 3/10/21

 */

@Data @NoArgsConstructor @JacksonXmlRootElement(localName = "POSResponse")
public class Response {

    @JsonProperty("POSResponse")
    private String posResponse; // Required
    @JsonProperty("Decision")
    private String decision; // Required
    @JsonProperty("ErrorCode")
    private String errorCode; // Required
    @JsonProperty("Message")
    private String message; // Required
    @JsonProperty("RequestId")
    private String requestId; // Required
    @JsonProperty("RequestGuid")
    private String requestGuid; // Required
    @JsonProperty("ApprovalCode")
    private String approvalCode; // Required
    @JsonProperty("MerchantReferenceCode")
    private String merchantReferenceCode; // Required
    @JsonProperty("AccountBalance")
    private String accountBalance;
    @JsonProperty("ApprovedAmount")
    private String approvedAmount;
    @JsonProperty("TipAmount")
    private String tipAmount;
    @JsonProperty("Token")
    private String token;
    @JsonProperty("MaskedCardNumber")
    private String maskedCardNumber;
    @JsonProperty("CardType")
    private String cardType;
    @JsonProperty("NameOnCard")
    private String nameOnCard;
    @JsonProperty("IssuerName")
    private String issuerName;
    @JsonProperty("EmvTagData")
    private String emvTagData;
    @JsonProperty("ExchangeRate")
    private String exchangeRate;
    @JsonProperty("ForeignCurrencyAlpha")
    private String foreignCurrencyAlpha;
    @JsonProperty("ForeignCurrency")
    private String foreignCurrency;
    @JsonProperty("ForeignAmount")
    private String foreignAmount;
    @JsonProperty("Margin")
    private String margin;
    @JsonProperty("RateSource")
    private String rateSource;
    @JsonProperty("RateLifeTime")
    private String rateLifeTime;
    @JsonProperty("Signature")
    private String signature;
    @JsonProperty("TokenExpiration")
    private String tokenExpiration;
    @JsonProperty("AvsCode")
    private String avsCode;
    @JsonProperty("CvCode")
    private String cvCode;
    @JsonProperty("DCCAccepted")
    private String dccAccepted;
    @JsonProperty("ReceiptText")
    private String receiptText;
    @JsonProperty("EntryMode")
    private String entryMode;
    @JsonProperty("PinVerified")
    private String pinVerified;
    @JsonProperty("Network")
    private String network;
    @JsonProperty("ReconciliationId")
    private String reconciliationId;
    @JsonProperty("emvAID")
    private String emvAID;
    @JsonProperty("emvTVR")
    private String emvTVR;
    @JsonProperty("emvIAD")
    private String emvIAD;
    @JsonProperty("emvTSI")
    private String emvTSI;
    @JsonProperty("emvARC")
    private String emvARC;
    @JsonProperty("ChipData")
    private String chipData;
    @JsonProperty("Tag50")
    private String tag50;
    @JsonProperty("Tag5F2A")
    private String tag5F2A;
    @JsonProperty("Tag5F34")
    private String tag5F34;
    @JsonProperty("Tag82")
    private String tag82;
    @JsonProperty("Tag95")
    private String tag95;
    @JsonProperty("Tag9A")
    private String tag9A;
    @JsonProperty("Tag9C")
    private String tag9C;
    @JsonProperty("Tag9F02")
    private String tag9F02;
    @JsonProperty("Tag9F03")
    private String tag9F03;
    @JsonProperty("Tag9F07")
    private String tag9F07;
    @JsonProperty("Tag9F0D")
    private String tag9F0D;
    @JsonProperty("Tag9F0E")
    private String tag9F0E;
    @JsonProperty("Tag9F0F")
    private String tag9F0F;
    @JsonProperty("Tag9F10")
    private String tag9F10;
    @JsonProperty("Tag9F12")
    private String tag9F12;
    @JsonProperty("Tag9F1A")
    private String tag9F1A;
    @JsonProperty("Tag9F26")
    private String tag9F26;
    @JsonProperty("Tag9F27")
    private String tag9F27;
    @JsonProperty("Tag9F34")
    private String tag9F34;
    @JsonProperty("Tag9F36")
    private String tag9F36;
    @JsonProperty("Tag9F37")
    private String tag9F37;
    @JsonProperty("CVMMethod")
    private String cvmMethod;
    @JsonProperty("prevRequestId")
    private String prevRequestId;
    @JsonProperty("CheckTransactionStatus")
    private String checkTransactionStatus;
    @JsonProperty("CheckTraceId")
    private String checkTraceId;
    @JsonProperty("CheckApprovalCode")
    private String checkApprovalCode;
    @JsonProperty("CheckNumber")
    private String checkNumber;
    @JsonProperty("CheckReturnFee")
    private String checkReturnFee;
    @JsonProperty("CheckReturnNote")
    private String checkReturnNote;
    @JsonProperty("CheckTerminalId")
    private String checkTerminalId;
    @JsonProperty("CheckMerchantId")
    private String checkMerchantId;
    @JsonProperty("CheckDenialRecordNumber")
    private String checkDenialRecordNumber;
    @JsonProperty("DeviceSerialNumber")
    private String deviceSerialNumber;
    @JsonProperty("DeviceModel")
    private String deviceModel;
    @JsonProperty("FirmwareVersion")
    private String firmwareVersion;
    @JsonProperty("EmvEnabled")
    private String emvEnabled;
    @JsonProperty("TransactionId")
    private String transactionId;
    @JsonProperty("ExpiryDate")
    private String expiryDate;
    @JsonProperty("Track2m")
    private String track2m;
    @JsonProperty("DeviceVerified")
    private String deviceVerified;
    @JsonProperty("SignatureRequired")
    private String signatureRequired;
    @JsonProperty("Brand")
    private String brand;
    @JsonProperty("CashBackAmount")
    private String cashBackAmount;
}