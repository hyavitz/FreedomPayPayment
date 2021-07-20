package payment;

import lombok.Data;

@Data
public class PaymentDetails {

    private String invoice;
    private String customerCode;
    private String decision;
    private String message;
    private String token;
    private String merchantReferenceCode;
    private String requestId;
}
