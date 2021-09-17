package config;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Build {

    public String PROTOCOL, HOST_DOMAIN, HOST_DOMAIN_LB, HOST, HOST_LB;
    public String KP_PATH_API, KP_PATH_ROOT, KP_PATH_VERSION, KP_PATH_VERSION_VALUE, KP_PATH_API_FULL;
    public String DP_PATH_ROOT, DP_PATH_VERSION, DP_PATH_VERSION_VALUE, DP_PATH_API, DP_PATH_API_FULL;
    public String DP_PATH_INTERNAL, DP_PATH_INCLUDES;
    public String CHECK_HEARTBEAT_PATH, CHECK_HEARTBEAT_ENDPOINT;
    public String GENERATE_TOKEN_PATH, GENERATE_TOKEN_PRODUCT_ENDPOINT;
    public String GENERATE_TOKEN_TYPE_VALUE, GENERATE_TOKEN_KEY_ENDPOINT;
    public String GENERATE_TOKEN_KEY_VALUE, GENERATE_TOKEN_ENDPOINT;
    public String REGISTER_DEVICE_PATH, REGISTER_DEVICE_ENDPOINT;
    public String GENERATE_LOG_PATH, GENERATE_LOG_ENDPOINT;
    public String APPLY_PAYMENT_PATH;
    public String APPLY_PAYMENT_PENDING_ENDPOINT, APPLY_PAYMENT_REQUEST_RESPONSE_ENDPOINT;
    public String APPLY_PAYMENT_CAPTURE_ENDPOINT, APPLY_PAYMENT_REMOVE_ENDPOINT;
    public String CHECK_HEARTBEAT_API, GENERATE_TOKEN_API;
    public String REGISTER_DEVICE_API, GENERATE_LOG_API;
    public String APPLY_PAYMENT_PENDING_API, APPLY_PAYMENT_REQUEST_RESPONSE_API;
    public String APPLY_PAYMENT_CAPTURE_API, APPLY_PAYMENT_REMOVE_API;

    public Build configureBuild(String buildConfigFile) {

        StringBuilder buildStringBuilder = new StringBuilder();

        File file = new File(buildConfigFile);
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            for (String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine()) {
                buildStringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONObject buildJsonObject = new JSONObject(buildStringBuilder.toString());

        this.PROTOCOL = buildJsonObject.optString("protocol", "");
        this.HOST_DOMAIN = buildJsonObject.optString("host_domain", "");
        this.HOST_DOMAIN_LB = buildJsonObject.optString("host_domain_lb", "");
        this.HOST = PROTOCOL + HOST_DOMAIN;
        this.HOST_LB = PROTOCOL + HOST_DOMAIN_LB;
        this.KP_PATH_API = buildJsonObject.optString("kp_path_api", "");
        this.KP_PATH_ROOT = buildJsonObject.optString("kp_path_root", "");
        this.KP_PATH_VERSION = buildJsonObject.optString("kp_path_version", "");
        this.KP_PATH_VERSION_VALUE = buildJsonObject.optString("kp_path_version_value", "");
        this.DP_PATH_ROOT = buildJsonObject.optString("dp_path_root", "");
        this.DP_PATH_VERSION = buildJsonObject.optString("dp_path_version", "");
        this.DP_PATH_VERSION_VALUE = buildJsonObject.optString("dp_path_version_value", "");
        this.DP_PATH_API = buildJsonObject.optString("dp_path_api", "");
        this.DP_PATH_API_FULL = DP_PATH_ROOT + DP_PATH_VERSION + DP_PATH_VERSION_VALUE + DP_PATH_API;
        this.KP_PATH_API_FULL = KP_PATH_API + KP_PATH_ROOT + KP_PATH_VERSION + KP_PATH_VERSION_VALUE;
        this.DP_PATH_INTERNAL = buildJsonObject.optString("dp_path_internal", "");
        this.DP_PATH_INCLUDES = buildJsonObject.optString("dp_path_includes", "");
        this.CHECK_HEARTBEAT_PATH = DP_PATH_ROOT + DP_PATH_INCLUDES;
        this.CHECK_HEARTBEAT_ENDPOINT = buildJsonObject.optString("check_heartbeat_endpoint", "");
        this.GENERATE_TOKEN_PATH = DP_PATH_ROOT + DP_PATH_INTERNAL;
        this.GENERATE_TOKEN_PRODUCT_ENDPOINT = buildJsonObject.optString("generate_token_product_endpoint", "");
        this.GENERATE_TOKEN_TYPE_VALUE = buildJsonObject.optString("generate_token_type_value", "");
        this.GENERATE_TOKEN_KEY_ENDPOINT = buildJsonObject.optString("generate_token_key_endpoint", "");
        this.GENERATE_TOKEN_KEY_VALUE = buildJsonObject.optString("generate_token_key_value", "");
        this.GENERATE_TOKEN_ENDPOINT = GENERATE_TOKEN_PRODUCT_ENDPOINT + GENERATE_TOKEN_TYPE_VALUE + GENERATE_TOKEN_KEY_ENDPOINT + GENERATE_TOKEN_KEY_VALUE;
        this.REGISTER_DEVICE_PATH = KP_PATH_API_FULL;
        this.REGISTER_DEVICE_ENDPOINT = buildJsonObject.optString("register_device_endpoint");
        this.GENERATE_LOG_PATH = DP_PATH_API_FULL;
        this.GENERATE_LOG_ENDPOINT = buildJsonObject.optString("generate_log_endpoint", "");
        this.APPLY_PAYMENT_PATH = DP_PATH_API_FULL;
        this.APPLY_PAYMENT_PENDING_ENDPOINT = buildJsonObject.optString("apply_payment_pending_endpoint", "");
        this.APPLY_PAYMENT_REQUEST_RESPONSE_ENDPOINT = buildJsonObject.optString("apply_payment_request_response_endpoint", "");
        this.APPLY_PAYMENT_CAPTURE_ENDPOINT = buildJsonObject.optString("apply_payment_capture_endpoint", "");
        this.APPLY_PAYMENT_REMOVE_ENDPOINT = buildJsonObject.optString("apply_payment_remove_endpoint", "");
        this.CHECK_HEARTBEAT_API = CHECK_HEARTBEAT_PATH + CHECK_HEARTBEAT_ENDPOINT;
        this.GENERATE_TOKEN_API = GENERATE_TOKEN_PATH + GENERATE_TOKEN_ENDPOINT;
        this.REGISTER_DEVICE_API = REGISTER_DEVICE_PATH + REGISTER_DEVICE_ENDPOINT;
        this.GENERATE_LOG_API = GENERATE_LOG_PATH + GENERATE_LOG_ENDPOINT;
        this.APPLY_PAYMENT_PENDING_API = APPLY_PAYMENT_PATH + APPLY_PAYMENT_PENDING_ENDPOINT;
        this.APPLY_PAYMENT_REQUEST_RESPONSE_API = APPLY_PAYMENT_PATH = APPLY_PAYMENT_REQUEST_RESPONSE_ENDPOINT;
        this.APPLY_PAYMENT_CAPTURE_API = APPLY_PAYMENT_PATH + APPLY_PAYMENT_CAPTURE_ENDPOINT;
        this.APPLY_PAYMENT_REMOVE_API = APPLY_PAYMENT_PATH + APPLY_PAYMENT_REMOVE_ENDPOINT;
        return this;
    }
}
