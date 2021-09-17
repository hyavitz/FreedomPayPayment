package data;

public final class Constant {

    public static final String PROTOCOL = "https://";

    public static final String HOST_DOMAIN = "softpointdev.com/";
    public static final String HOST_DOMAIN_LB = "{'/*TBD*/', '/*TBD*/', '/*TBD*/'}";

    public static final String HOST = PROTOCOL + HOST_DOMAIN;
    public static final String HOST_LB = PROTOCOL + HOST_DOMAIN_LB;

    public static final String KP_PATH_API = "APIAPPS/";
    public static final String KP_PATH_ROOT = "KioskPoint/";
    public static final String KP_PATH_VERSION = "v";
    public static final String KP_PATH_VERSION_VALUE = "023/";

    public static final String DP_PATH_ROOT = "DPS/";
    public static final String DP_PATH_VERSION = "version/";
    public static final String DP_PATH_VERSION_VALUE = "061/";
    public static final String DP_PATH_API = "api/";

    public static final String DP_PATH_API_FULL = DP_PATH_ROOT + DP_PATH_VERSION + DP_PATH_VERSION_VALUE + DP_PATH_API;
    public static final String KP_PATH_API_FULL = KP_PATH_API + KP_PATH_ROOT + KP_PATH_VERSION + KP_PATH_VERSION_VALUE;

    public static final String DP_PATH_INTERNAL = "internal/";
    public static final String DP_PATH_INCLUDES = "includes/";

    public static final String CHECK_HEARTBEAT_PATH = DP_PATH_ROOT + DP_PATH_INCLUDES;
    public static final String CHECK_HEARTBEAT_ENDPOINT = "test_sessions.php";

    public static final String GENERATE_TOKEN_PATH = DP_PATH_ROOT + DP_PATH_INTERNAL;
    public static final String GENERATE_TOKEN_PRODUCT_ENDPOINT = "generatetoken_secure.php?product=";
    public static final String GENERATE_TOKEN_TYPE_VALUE = "application";
    public static final String GENERATE_TOKEN_KEY_ENDPOINT = "&key=";
    public static final String GENERATE_TOKEN_KEY_VALUE = "7894ab5a-7ed0-457e-8020-c5d03dc50226";
    public static final String GENERATE_TOKEN_ENDPOINT = GENERATE_TOKEN_PRODUCT_ENDPOINT + GENERATE_TOKEN_TYPE_VALUE + GENERATE_TOKEN_KEY_ENDPOINT + GENERATE_TOKEN_KEY_VALUE;

    public static final String REGISTER_DEVICE_ENDPOINT = "qp_verify_location_id.php";

    public static final String GENERATE_LOG_PATH = DP_PATH_API_FULL;
    public static final String GENERATE_LOG_ENDPOINT = "dp_time_marker.php";

    public static final String APPLY_PAYMENT_PATH =  DP_PATH_API_FULL;
    public static final String APPLY_PAYMENT_PENDING_ENDPOINT = "dp_ov_insert_payment_pending.php";
    public static final String APPLY_PAYMENT_REQUEST_RESPONSE_ENDPOINT = "dp_insert_payment_request_response.php";
    public static final String APPLY_PAYMENT_CAPTURE_ENDPOINT = "dp_ov_insert_wh_payment.php";
    public static final String APPLY_PAYMENT_REMOVE_ENDPOINT = "dp_ov_remove_payment.php";

    public static final String CHECK_HEARTBEAT_API = CHECK_HEARTBEAT_PATH + CHECK_HEARTBEAT_ENDPOINT;
    public static final String GENERATE_TOKEN_API = GENERATE_TOKEN_PATH + GENERATE_TOKEN_ENDPOINT;
    public static final String REGISTER_DEVICE_API = KP_PATH_API_FULL + REGISTER_DEVICE_ENDPOINT;
    public static final String GENERATE_LOG_API = GENERATE_LOG_PATH + GENERATE_LOG_ENDPOINT;
    public static final String APPLY_PAYMENT_PENDING_API = APPLY_PAYMENT_PATH + APPLY_PAYMENT_PENDING_ENDPOINT;
    public static final String APPLY_PAYMENT_REQUEST_RESPONSE_API = APPLY_PAYMENT_PATH + APPLY_PAYMENT_REQUEST_RESPONSE_ENDPOINT;
    public static final String APPLY_PAYMENT_CAPTURE_API = APPLY_PAYMENT_PATH + APPLY_PAYMENT_CAPTURE_ENDPOINT;
    public static final String APPLY_PAYMENT_REMOVE_API = APPLY_PAYMENT_PATH + APPLY_PAYMENT_REMOVE_ENDPOINT;
}