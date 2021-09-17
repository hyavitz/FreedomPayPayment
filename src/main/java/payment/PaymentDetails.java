package payment;

import lombok.Data;

@Data
public class PaymentDetails {

    private String transactionRemainingAmount;
    private String remainingBalance;
    private String extraBalance;
    private String rawData;
    private String rawResponse;
    private String hostCode;
    private String hostResponse;
    private String hostDetailedMessage;
    private String approvedAmount;
    private String requestedAmount;
    private String invoice;
    private String customerCode;
    private String decision;
    private String status;
    private String message;
    private String token;
    private String merchantReferenceCode;
    private String requestId;
    private String amount;
    private String tip;
    private String cardType;
    private String cardHolder;
    private String maskedCardNumber;
    private String aioAccountId;
    private String authCode;
    private String referenceNumber;
    private String entryMode;
    private String hostReferenceNumber;
    private String resultCode;
    private String resultMessage;
    private String resultMessageDetail;
    private String transactionNumber;

    private String versionIntegrator;
    private String fwVersion;
    private String resultResponse;
    private String giftCardNumber;
    private String emvTagData;
    private String expiryDate;
    private boolean isPreAuth;
    private boolean isIncrementalAuth;
    private String preAuthIntegratedId;

    private String amountBeforeSurcharge;
    private String tipBeforeSurcharge;
    private String surcharge;
    private String surchargeCardType;

    private String totalProcessed;
    private String originalAmount;
    private String originalTip;

    private boolean isCredit;
}