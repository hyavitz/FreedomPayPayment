package interceptor;

public class Constant {
    private static final String TOKEN_PARAM_KEY = "7894ab5a-7ed0-457e-8020-c5d03dc50226";
    private static final String TOKEN_PARAM_PRODUCT = "application";
    static final String HEARTBEAT_SERVER = "DPS/includes/test_sessions.php";
    public static final String GENERATE_TOKEN = "DPS/internal/generatetoken_secure.php?product=" + TOKEN_PARAM_PRODUCT + "&key=" + TOKEN_PARAM_KEY;

    /**
     * HeartbeatResponse is the health check done before a server is called to make sure that the server
     * is active and able to get requests and respond to them.
     *
     * @author Joshua Monson - 11/20/2019
     */
}