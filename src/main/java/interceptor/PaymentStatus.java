package interceptor;

public enum PaymentStatus {

    APPROVED ("approved"),
    FAILED ("failed");

    private String value;

    PaymentStatus(String status_value) {
        this.value = status_value;
    }

    public String getValue() {
        return value;
    }
}
