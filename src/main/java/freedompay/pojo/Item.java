package freedompay.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.*;

/**
 *
 * This class accepts at least 7 required properties and some number of optional properties
 * and returns an Item object that represents some type of goods or service sold.  The Item
 * objects created here should be added to the Items object.  From there it can be added to
 * receipts or parsed into JSON/XML as needed.
 *
 * This class is called by the FPPaymentDevice, Receipt, and Items classes.
 *
 * @author Hunter Yavitz - 06/10/21
 *
 */

@Data
public class Item {

    // Required
    @JsonProperty(value = "productCode", required = true)
    @JacksonXmlProperty(namespace = "http://freeway.freedompay.com/")
    private String productCode; // Required if no product UPC, no product SKU -> POS

    @JsonProperty("productUPC")
    @JacksonXmlProperty(namespace = "http://freeway.freedompay.com/")
    private String productUPC; // Required if no product code, no product SKU -> POS

    @JsonProperty("productSKU")
    @JacksonXmlProperty(namespace = "http://freeway.freedompay.com/")
    private String productSKU; // Required if no product code, no product UPC -> POS

    @JsonProperty(value = "productName", required = true)
    @JacksonXmlProperty(namespace = "http://freeway.freedompay.com/")
    private String productName; // Required -> POS

    @JsonProperty(value = "productDescription", required = true)
    @JacksonXmlProperty(namespace = "http://freeway.freedompay.com/")
    private String productDescription; // Required -> POS

    @JsonProperty(value = "unitPrice", required = true)
    @JacksonXmlProperty(namespace = "http://freeway.freedompay.com/")
    private String unitPrice; // Required -> POS

    @JsonProperty(value = "quantity", required = true)
    @JacksonXmlProperty(namespace = "http://freeway.freedompay.com/")
    private String quantity; // Required -> POS

    @JsonProperty(value = "totalAmount", required = true)
    @JacksonXmlProperty(namespace = "http://freeway.freedompay.com/")
    private String totalAmount; // Required ((UnitPrice * Quantity) - DiscountAmount) -> POS

    @JsonProperty(value = "taxAmount", required = true)
    @JacksonXmlProperty(namespace = "http://freeway.freedompay.com/")
    private String taxAmount; // Required -> POS

    @JsonProperty(value = "saleCode", required = true)
    @JacksonXmlProperty(namespace = "http://freeway.freedompay.com/")
    private String saleCode; // Required -> POS

    // Optional
    @JsonProperty("productMake")
    @JacksonXmlProperty(namespace = "http://freeway.freedompay.com/")
    private String productMake;

    @JsonProperty("productModel")
    @JacksonXmlProperty(namespace = "http://freeway.freedompay.com/")
    private String productModel;

    @JsonProperty("productPartNumber")
    @JacksonXmlProperty(namespace = "http://freeway.freedompay.com/")
    private String productPartNumber;

    @JsonProperty("productYear")
    @JacksonXmlProperty(namespace = "http://freeway.freedompay.com/")
    private String productYear;

    @JsonProperty("productSerial1")
    @JacksonXmlProperty(namespace = "http://freeway.freedompay.com/")
    private String productSerial1;

    @JsonProperty("productSerial2")
    @JacksonXmlProperty(namespace = "http://freeway.freedompay.com/")
    private String productSerial2;

    @JsonProperty("productSerial3")
    @JacksonXmlProperty(namespace = "http://freeway.freedompay.com/")
    private String productSerial3;

    @JsonProperty("customerAssetId")
    @JacksonXmlProperty(namespace = "http://freeway.freedompay.com/")
    private String customerAssetId;

    @JsonProperty("promoCode")
    @JacksonXmlProperty(namespace = "http://freeway.freedompay.com/")
    private String promoCode;

    @JsonProperty("freightAmount")
    @JacksonXmlProperty(namespace = "http://freeway.freedompay.com/")
    private String freightAmount;

    @JsonProperty("customFormatId")
    @JacksonXmlProperty(namespace = "http://freeway.freedompay.com/")
    private String customFormatId;

    @JsonProperty("custom1")
    @JacksonXmlProperty(namespace = "http://freeway.freedompay.com/")
    private String custom1;

    @JsonProperty("custom2")
    @JacksonXmlProperty(namespace = "http://freeway.freedompay.com/")
    private String custom2;

    @JsonProperty("custom3")
    @JacksonXmlProperty(namespace = "http://freeway.freedompay.com/")
    private String custom3;

    @JsonProperty("custom4")
    @JacksonXmlProperty(namespace = "http://freeway.freedompay.com/")
    private String custom4;

    @JsonProperty("custom5")
    @JacksonXmlProperty(namespace = "http://freeway.freedompay.com/")
    private String custom5;

    @JsonProperty("custom6")
    @JacksonXmlProperty(namespace = "http://freeway.freedompay.com/")
    private String custom6;

    @JsonProperty("custom7")
    @JacksonXmlProperty(namespace = "http://freeway.freedompay.com/")
    private String custom7;

    @JsonProperty("custom8")
    @JacksonXmlProperty(namespace = "http://freeway.freedompay.com/")
    private String custom8;

    @JsonProperty("custom9")
    @JacksonXmlProperty(namespace = "http://freeway.freedompay.com/")
    private String custom9;

    @JsonProperty("origUnitPrice")
    @JacksonXmlProperty(namespace = "http://freeway.freedompay.com/")
    private String origUnitPrice; // If unitPrice is modified after applying discounts

    @JsonProperty("origTotalAmount")
    @JacksonXmlProperty(namespace = "http://freeway.freedompay.com/")
    private String origTotalAmount; // If totalAmount is modified after applying discounts

    @JsonProperty("commodityCode")
    @JacksonXmlProperty(namespace = "http://freeway.freedompay.com/")
    private String commodityCode;

    @JsonProperty("taxIncludedFlag")
    @JacksonXmlProperty(namespace = "http://freeway.freedompay.com/")
    private String taxIncludedFlag; // If totalAmount includes tax: set to "Y", else "N" and include taxAmount value - Default > "N"

    @JsonProperty("discountAmount")
    @JacksonXmlProperty(namespace = "http://freeway.freedompay.com/")
    private String discountAmount; // Line item discounts

    @JsonProperty("discountFlag")
    @JacksonXmlProperty(namespace = "http://freeway.freedompay.com/")
    private String discountFlag; // If DiscountAmount > 0: default "Y", else "N"

    @JsonProperty("unitOfMeasure")
    @JacksonXmlProperty(namespace = "http://freeway.freedompay.com/")
    private String unitOfMeasure;

    public Item (String productCode, String productName, String productDescription, String unitPrice, String quantity, String totalAmount, String taxAmount, String saleCode) {
        this.productCode = productCode;
        this.productName = productName;
        this.productDescription = productDescription;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.taxAmount = taxAmount;
        this.saleCode = saleCode;
    }

    public Item() {
        this.productCode = "Some Product Code";
        this.productName = "Some Product Name";
        this.productDescription = "Some Product Description";
        this.unitPrice = "Some Unit Price";
        this.quantity = "Some Quantity";
        this.totalAmount = "Some Total Amount";
        this.taxAmount = "Some Tax Amount";
        this.saleCode = "Some Sale Code";
    }

    @Override
    public String toString() {

        return "Item{" +
                "productName='" + productName + '\'' +
                ", unitPrice='" + unitPrice + '\'' +
                ", quantity='" + quantity + '\'' +
                '}';
    }
}