package freedompay.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.*;

/**
 * This data class extends the TransactionRequest class
 * and represents a freedom pay request object.  It is
 * called by FreedomPayPaymentDevice and used to parcel
 * POS transaction requests to / from XML.  This class
 * contains a comprehensive list of potential fields,
 * though not every field will be used for every transaction.
 *
 * @author Hunter Yavitz - 8/10/21 - Revision
 */

@Data @JacksonXmlRootElement(localName = "POSRequest")
public class FreedomPayTransactionRequest extends TransactionRequest {

    @JsonProperty("ChargeAmount")
    private String chargeAmount;
    @JsonProperty("StoreId")
    private String storeId;
    @JsonProperty("TerminalId")
    private String terminalId;
    @JsonProperty("WorkstationId")
    private String workstationId;
    @JsonProperty("InvoiceNumber")
    private String invoiceNumber;
    @JsonProperty("MerchantTaxId")
    private String merchantTaxId;
    @JsonProperty("MerchantPostalCode")
    private String merchantPostalCode;
    @JsonProperty("MerchantStateCode")
    private String merchantStateCode;
    @JsonProperty("CustomerCode")
    private String customerCode;
    @JsonProperty("CustomerId")
    private String customerId;
    @JsonProperty("CustomerPODate")
    private String customerPoDate;
    @JsonProperty("CustomerPONumber")
    private String customerPoNumber;
    @JsonProperty("TaxAmount")
    private String taxAmount;
    @JsonProperty("CardNumber")
    private String cardNumber;
    @JsonProperty("ExpiryDate")
    private String expiryDate;
    @JsonProperty("CvNumber")
    private String cvNumber;
    @JsonProperty("RequestGUID")
    private String requestGuid;
    @JsonProperty("CardType")
    private String cardType;
    @JsonProperty("IssuerCode")
    private String issuerCode;
    @JsonProperty("VerbalAuthCode")
    private String verbalAuthCode;
    @JsonProperty("TipAmount")
    private String tipAmount;
    @JsonProperty("TokenType")
    private String tokenType;
    @JsonProperty("AllowPartial")
    private String allowPartial;
    @JsonProperty("ClientEnvironment")
    private String clientEnvironment;
    @JsonProperty("FloorLimit")
    private String floorLimit;
    @JsonProperty("Pos_originalChipData")
    private String posOriginalChipData;
    @JsonProperty("Pos_chipData")
    private String posChipData;
    @JsonProperty("Pos_trackKsn")
    private String posTrackKsn;
    @JsonProperty("Pos_track2e")
    private String posTrack2E;
    @JsonProperty("Pos_track2len")
    private String posTrack2Len;
    @JsonProperty("PSTAmount")
    private String pstAmount;
    @JsonProperty("QRCodeData")
    private String qrCodeData;
    @JsonProperty("QSTAmount")
    private String qstAmount;
    @JsonProperty("QuickServeFloorLimit")
    private String quickServeFloorLimit;
    @JsonProperty("Pos_msrmode")
    private String posMsrMode;
    @JsonProperty("Pos_sequenceNumber")
    private String posSequenceNumber;
    @JsonProperty("Pos_msrType")
    private String posMsrType;
    @JsonProperty("Pos_encMode")
    private String posEncMode;
    @JsonProperty("ENCMode")
    private String encMode;
    @JsonProperty("Pos_entryMode")
    private String posEntryMode;
    @JsonProperty("MaskedCardNumber")
    private String maskedCardNumber;
    @JsonProperty("MerchantReferenceCode")
    private String merchantReferenceCode;
    @JsonProperty("RequestId")
    private String requestId;
    @JsonProperty("ClerkId")
    private String clerkId;
    @JsonProperty("IndustryType")
    private String industryType;
    @JsonProperty("Hotel_FolioNumber")
    private String hotelFolioNumber;
    @JsonProperty("Hotel_CheckInDate")
    private String hotelCheckInDate;
    @JsonProperty("Hotel_CheckOutDate")
    private String hotelCheckOutDate;
    @JsonProperty("Hotel_RoomRate")
    private String hotelRoomRate;
    @JsonProperty("HSTAmount")
    private String hstAmount;
    @JsonProperty("BillTo_FirstName")
    private String billToFirstName;
    @JsonProperty("BillTo_LastName")
    private String billToLastName;
    @JsonProperty("BillTo_Street1")
    private String billToStreet1;
    @JsonProperty("BillTo_Street2")
    private String billToStreet2;
    @JsonProperty("CardDataBlockString")
    private String cardDataBlockString;
    @JsonProperty("CardIssuer")
    private String cardIssuer;
    @JsonProperty("BillTo_PhoneNumber")
    private String billToPhoneNumber;
    @JsonProperty("BillTo_City")
    private String billToCity;
    @JsonProperty("BillTo_State")
    private String billToState;
    @JsonProperty("BillTo_PostalCode")
    private String billToPostalCode;
    @JsonProperty("UseDCC")
    private String useDCC;
    @JsonProperty("Offline")
    private String offline;
    @JsonProperty("SigData")
    private String sigData;
    @JsonProperty("SignatureFormat")
    private String signatureFormat;
    @JsonProperty("EnableAVS")
    private String enableAVS;
    @JsonProperty("EodGroupCode")
    private String eodGroupCode;
    @JsonProperty("FallBackReason")
    private String fallBackReason;
    @JsonProperty("UpdateToken")
    private String updateToken;
    @JsonProperty("RegisterNumber")
    private String registerNumber;
    @JsonProperty("TokenDynExp")
    private String tokenDynExp;
    @JsonProperty("ReceiptFormat")
    private String receiptFormat;
    @JsonProperty("ReceiptEol")
    private String receiptEol;
    @JsonProperty("ReceiptWidth")
    private String receiptWidth;
    @JsonProperty("ReceiptMargin")
    private String receiptMargin;
    @JsonProperty("Recurring")
    private String recurring;
    @JsonProperty("SafMode")
    private String safMode;
    @JsonProperty("DisableSAF")
    private String disableSAF;
    @JsonProperty("PaymentMethod")
    private String paymentMethod;
    @JsonProperty("CardPassword")
    private String cardPassword;
    @JsonProperty("ForcedEntryMode")
    private String forcedEntryMode;
    @JsonProperty("ForceManual")
    private String forceManual;
    @JsonProperty("GSTAmount")
    private String gstAmount;
    @JsonProperty("SignatureFormatType")
    private String signatureFormatType;
    @JsonProperty("CommerceIndicator")
    private String commerceIndicator;
    @JsonProperty("Currency")
    private String currency;
    @JsonProperty("InstallmentCount")
    private String installmentCount;
    @JsonProperty("InstallmentNumber")
    private String installmentNumber;
    @JsonProperty("InternalRequestType")
    private String internalRequestType;
    @JsonProperty("InvoiceDate")
    private String invoiceDate;
    @JsonProperty("CheckBankTransitNumber")
    private String checkBankTransitNumber;
    @JsonProperty("CheckAccountNumber")
    private String checkAccountNumber;
    @JsonProperty("CheckNumber")
    private String checkNumber;
    @JsonProperty("CheckMicr")
    private String checkMicr;
    @JsonProperty("CheckType")
    private String checkType;
    @JsonProperty("CheckReaderStatus")
    private String checkReaderStatus;
    @JsonProperty("CheckAch")
    private String checkAch;
    @JsonProperty("BillTo_DriverLicenseNumber")
    private String billToDriverLicenseNumber;
    @JsonProperty("BillTo_DriverLicenseIssuer")
    private String billToDriverLicenseIssuer;
    @JsonProperty("RequestType")
    private String requestType;
    @JsonProperty("LaneId")
    private String laneId;
    @JsonProperty("Application")
    private String application;
    @JsonProperty("ApplicationVersion")
    private String applicationVersion;
    @JsonProperty("MiddlewareName")
    private String middlewareName;
    @JsonProperty("MiddlewareVersion")
    private String middlewareVersion;
    @JsonProperty("ClientApplication")
    private String clientApplication;
    @JsonProperty("ClientApplicationVersion")
    private String clientApplicationVersion;
    @JsonProperty("EntryMethod")
    private String entryMethod;
    @JsonProperty("Items")
    private Items items;
}