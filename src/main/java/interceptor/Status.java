package interceptor;

public enum Status {
    TIP_ADJUST ("TipAdjust"),
    PAID ("Paid"),
    DECLINED ("Declined"),
    PARTIAL ("Partial");

    private String value;

    Status(String status_value) {
        this.value = status_value;
    }

    public String getValue() {
        return value;
    }
}
