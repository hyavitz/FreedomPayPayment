package interceptor;

public class WebhookSaleInformation {
    public String command;
    public String triggered_at;
    public String ResponseCode;
    public ResponseMessage responseMessage;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getTriggered_at() {
        return triggered_at;
    }

    public void setTriggered_at(String triggered_at) {
        this.triggered_at = triggered_at;
    }

    public String getResponseCode() {
        return ResponseCode;
    }

    public void setResponseCode(String responseCode) {
        ResponseCode = responseCode;
    }

    public ResponseMessage getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }

    @Override
    public String toString() {
        return "WebhookSaleInformation{" +
                "command='" + command + '\'' +
                ", triggered_at='" + triggered_at + '\'' +
                ", ResponseCode='" + ResponseCode + '\'' +
                ", responseMessage=" + responseMessage +
                '}';
    }
}
