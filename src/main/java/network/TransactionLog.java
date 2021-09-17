package network;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionLog {

    public static boolean generateLog(String action, String message) throws InterruptedException {

        System.out.println("<>GENERATING TRANSACTION LOG<>");

        /**
         * TODO:
         * ov_ticket_id -> AP TICKET DETAIL
         * transactionNo -> AP PAYMENT DETAIL -> Unique per transaction
         * payment_id -> AP PAYMENT DETAIL -> Unique per payment
         *
         * ticket# -> ov_ticket_id
         * table ->
         * seat -> 99
         * employee_id -> 001
         * emp_id -> 102
         * server -> 1234
         *
         *
         * trans_status -> paid
         * open ->
         * close ->
         * status -> paid / active / pending / removed
         * type -> dpov
         *
         *
         *
         */

        Token.generateToken();
        String token = Token.token;

        Thread.sleep(1000);
        System.out.println("Token for transaction log is: " + token);

        String locationId = "10009";
        String from = "FOCUS";
        String position = "StartEnd";
        String where = action; // Action
        String exact = message; // Message
        String deviceId = "53096522";
        String deviceType = "PAX";
        String application = "QuickPoint";
        String applicationVersion = "024";

        Call<String> generateLogCall = GenerateTransactionLogApiController.getGenerateLogApiCall(from, position, where, exact, token, locationId, deviceType, deviceId, application, applicationVersion);

        generateLogCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {

                assert response.body() != null;

                JSONObject jsonObject = new JSONObject(response.body());
                System.out.println("JSON Object is: " + jsonObject);

                if (jsonObject.optString("ResponseMessage", "").equalsIgnoreCase("Success"));

                // TODO: This would be where we construct something, maybe a log?
                System.out.println("Response was success for log generetae");
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
            }
        });

        return true;
    }
}