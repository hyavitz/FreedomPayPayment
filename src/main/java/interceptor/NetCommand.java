package interceptor;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class NetCommand {

    @SerializedName("ResponseCode") private int responseCode;
    @SerializedName("ResponseMessage") private String responseMessage;
    @SerializedName("command") private Command command = Command.SALE;
    @SerializedName("unique_id") private String uniqueId;
    @SerializedName("webhook_id") private String webhookId;
    @SerializedName("unique_ticket_id") private String uniqueTicketId;
    @SerializedName("integrated_payment_id") private String paymentId; // TODO: maybe move this away?

    public enum Command {
        @SerializedName("ready") READY,
        @SerializedName("recieved") RECEIVED,
        @SerializedName("waiting") WAITING,
        @SerializedName("sync") SYNC,
        @SerializedName("kill") KILL,
        @SerializedName("failed") FAILED,
        @SerializedName("ping") PING,
        @SerializedName("pong") PONG,
        @SerializedName(value = "webhook", alternate = {"sale"}) SALE,
        @SerializedName("cancel") CANCEL,
        @SerializedName("token") TOKEN,
        @SerializedName("refund") REFUND,
        @SerializedName("void") VOID,
        @SerializedName("pre_auth") PRE_AUTH,
        @SerializedName("post_auth") POST_AUTH,
        @SerializedName("receipt") RECEIPT
    }
}