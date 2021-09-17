package interceptor;

import lombok.Data;

@Data
public class ResponseMessage {

    private int unique_webhook_id;
    private String opened_at;
    private String ov_ticket_id;
    private String amount;
    private String ov_tender_type_id;
    private String ov_location_id;
    private String ov_terminal_id;
    private String tip;
    private String location_id;
    private String unique_ticket_id;
    private String ov_payment_id;
}