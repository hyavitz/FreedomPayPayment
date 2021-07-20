package interceptor;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
public class HeartbeatResponse extends BasicResponse {

    @SerializedName("ResponseError")
    private String responseError;
}
