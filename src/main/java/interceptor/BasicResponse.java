package interceptor;

import com.google.gson.annotations.SerializedName;
import lombok.*;
import org.jetbrains.annotations.Nullable;

/**
 * BasicResponse is the most basic response that any SoftPoint API should return. The only required
 * field however is the ResponseCode as all others are optional. This code tells us if the API call
 * was a success if it returns the code 1. All other codes are errors or problems unless stated
 * otherwise by the API itself. This class is generally extended for other SoftPoint API POJOs as
 * it is the default response.
 *
 * @author Joshua Monson - 11/20/2019
 */
@Data
public class BasicResponse {

    public BasicResponse() {}
    public BasicResponse (int responseCode, @Nullable String responseMessage) {
        this.ResponseCode = responseCode;
        this.ResponseMessage = responseMessage;
    }
    /**
     * The response code of the API. This is generally 1 if successful or any other number if failed
     * The number returned in a failure is the reason for the failure.
     */
    @SerializedName("ResponseCode") private int ResponseCode;

    /**
     * If this is returned and the response is successful this will either be the data of the API or
     * ignorable, generally the latter. If the response is a failure this will normally be the
     * English error message for why the call failed.
     */
    @SerializedName("ResponseMessage") @Nullable private String ResponseMessage;

    /**
     * Extra error data. This is generally not returned or used.
     */
    @Nullable
    private ErrorDetails ErrorDetails;
}