package freedompay.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.*;

@Data @JacksonXmlRootElement(localName = "POSRequest")
public class Request {

    /*

        This class constructs an object with properties represented by the XML
        request for FreedomPay transactions.  Most of these fields will rarely
        be used, but must be available.

        Fields necessary for Level 2 / Level 3 compliance are marked.

        Required fields are marked.

        @author Hunter Yavitz - 3/10/21

     */

    @JsonProperty("ChargeAmount")
    private String chargeAmount; // Level 1 -> POS
    @JsonProperty("StoreId")
    private String storeId; // Level 1 -> POS
    @JsonProperty("TerminalId")
    private String terminalId; // Level 1 -> POS
    @JsonProperty("WorkstationId")
    private String workstationId; // Level 1 (Where applicable) -> POS
    @JsonProperty("InvoiceNumber")
    private String invoiceNumber; // Level 1 -> POS
    @JsonProperty("MerchantTaxId")
    private String merchantTaxId; // Level 2 * - Config
    @JsonProperty("MerchantPostalCode")
    private String merchantPostalCode; // Level 2 * - Config
    @JsonProperty("MerchantStateCode")
    private String merchantStateCode; // Level 2 * - Config
    @JsonProperty("CustomerCode")
    private String customerCode; // Level 2 * - aka PurchaserCode -> POS
    @JsonProperty("CustomerId")
    private String customerId; // Level 2 -> POS
    @JsonProperty("CustomerPODate")
    private String customerPODate; // Level 2 -> POS
    @JsonProperty("CustomerPONumber")
    private String customerPONumber; // Level 2 -> POS
    @JsonProperty("TaxAmount")
    private String taxAmount; // Level 2 * - Sum(Item(taxAmount)) Must be included in totalAmount -> POS
    @JsonProperty("CardNumber")
    private String cardNumber; // -> POI
    @JsonProperty("ExpiryDate")
    private String expiryDate; // -> POI
    @JsonProperty("CvNumber")
    private String cvNumber; // -> POI
    @JsonProperty("RequestGUID")
    private String requestGuid; // -> FCC
    @JsonProperty("CardType")
    private String cardType; // Req -> POI
    @JsonProperty("IssuerCode")
    private String issuerCode; // Req where cardType = atoken - card issuer "" -> Config
    @JsonProperty("VerbalAuthCode")
    private String verbalAuthCode; // Bank authorization when offline - Send with RequestID -> Bank
    @JsonProperty("TipAmount")
    private String tipAmount; // Must be included in totalAmount -> POS
    @JsonProperty("TokenType")
    private String tokenType; // 6 or 10 where follow-on requests -> FCC / POS
    @JsonProperty("AllowPartial")
    private String allowPartial; // -> Config
    @JsonProperty("ClientEnvironment")
    private String clientEnvironment; // Req -> POS
    @JsonProperty("FloorLimit")
    private String floorLimit; // -> Config
    @JsonProperty("Pos_originalChipData")
    private String posOriginalChipData; // Initial auth requests -> POI
    @JsonProperty("Pos_chipData")
    private String posChipData; // Follow-on capture requests -> POS
    @JsonProperty("Pos_trackKsn")
    private String posTrackKsn; // -> POI
    @JsonProperty("Pos_track2e")
    private String posTrack2E; // -> POI
    @JsonProperty("Pos_track2len")
    private String posTrack2Len; // -> POI
    @JsonProperty("PSTAmount")
    private String pstAmount;
    @JsonProperty("QRCodeData")
    private String qrCodeData;
    @JsonProperty("QSTAmount")
    private String qstAmount;
    @JsonProperty("QuickServeFloorLimit")
    private String quickServeFloorLimit; // -> Config
    @JsonProperty("Pos_msrmode")
    private String posMsrMode; // -> POI
    @JsonProperty("Pos_sequenceNumber")
    private String posSequenceNumber;
    @JsonProperty("Pos_msrType")
    private String posMsrType; // -> POI
    @JsonProperty("Pos_encMode")
    private String posEncMode; // -> POI
    @JsonProperty("ENCMode")
    private String encMode; // Req where applicable -> POI
    @JsonProperty("Pos_entryMode")
    private String posEntryMode; // -> POI
    @JsonProperty("MaskedCardNumber")
    private String maskedCardNumber; // -> FCC
    @JsonProperty("MerchantReferenceCode")
    private String merchantReferenceCode; // Req -> POS
    @JsonProperty("RequestId")
    private String requestId; // Req where follow-on requests -> FCC
    @JsonProperty("ClerkId")
    private String clerkId; // -> POS
    @JsonProperty("IndustryType")
    private String industryType; // -> POS
    @JsonProperty("Hotel_FolioNumber")
    private String hotelFolioNumber; // -> POS
    @JsonProperty("Hotel_CheckInDate")
    private String hotelCheckInDate; // -> POS
    @JsonProperty("Hotel_CheckOutDate")
    private String hotelCheckOutDate; // -> POS
    @JsonProperty("Hotel_RoomRate")
    private String hotelRoomRate; // -> POS
    @JsonProperty("HSTAmount")
    private String hstAmount;
    @JsonProperty("BillTo_FirstName")
    private String billToFirstName; // -> POS
    @JsonProperty("BillTo_LastName")
    private String billToLastName; // -> POS
    @JsonProperty("BillTo_Street1")
    private String billToStreet1; // -> POS
    @JsonProperty("BillTo_Street2")
    private String billToStreet2; // -> POS
    @JsonProperty("CardDataBlockString")
    private String cardDataBlockString;
    @JsonProperty("CardIssuer")
    private String cardIssuer; // -> POI
    @JsonProperty("BillTo_PhoneNumber")
    private String billToPhoneNumber; // -> POS
    @JsonProperty("BillTo_City")
    private String billToCity; // -> POS
    @JsonProperty("BillTo_State")
    private String billToState; // -> POS
    @JsonProperty("BillTo_PostalCode")
    private String billToPostalCode; // -> POI
    @JsonProperty("UseDCC")
    private String useDCC; // -> Config
    @JsonProperty("Offline")
    private String offline; // Flag where request made offline -> POS / FCC
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
    private String cardPassword; // -> POS
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
    private String checkMICR;
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
    private String requestType; // -> POS
    @JsonProperty("LaneId")
    private String laneId;
    @JsonProperty("Application")
    private String application; // Req
    @JsonProperty("ApplicationVersion")
    private String applicationVersion; // Req
    @JsonProperty("MiddlewareName")
    private String middlewareName; // Req
    @JsonProperty("MiddlewareVersion")
    private String middlewareVersion; // Req
    @JsonProperty("ClientApplication")
    private String clientApplication; // -> FCC
    @JsonProperty("ClientApplicationVersion")
    private String clientApplicationVersion; // -> FCC
    @JsonProperty("EntryMethod")
    private String entryMethod;
    @JsonProperty("Items")
    private Items items; // Nested Tag -> Item.class
}